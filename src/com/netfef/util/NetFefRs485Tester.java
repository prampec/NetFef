/*
 * This file is part of the NetFef serial network bus protocol project.
 *
 * Copyright (c) 2015.
 * Author: Balazs Kelemen
 * Contact: prampec+netfef@gmail.com
 *
 * This product is licensed under Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International (CC BY-NC-SA 4.0) license.
 * Please contact the author for a special agreement in case you want to use this creation for commercial purposes!
 */

package com.netfef.util;

import com.netfef.protocol.Frame;
import com.netfef.protocol.NetFef;
import com.netfef.protocol.Parameter;
import com.netfef.protocol.ParameterType;
import com.netfef.rs485.NetFefRs485;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * <p>Basic test application for the project functionality.</p>
 * <p>User: kelemenb
 * <br/>Date: 4/10/15</p>
 */
public class NetFefRs485Tester {

    public static void main(String[] args) throws IOException {
        System.out.println("Starting up...");
        new NetFefRs485Tester().run();
    }

    private void run() throws IOException {
        NetFefRs485 module = new NetFefRs485(new NetFefRs485.NetFefRs485ReceiveListener() {
            @Override
            public void dataReceived(Frame frame) {
                System.out.println("Frame received: " + frame);
            }

            @Override
            public void handleError(IOException e) {
                System.out.println("Error occurred:" + e.getMessage());
            }
        });
        module.init();

        System.out.println("Started. Enter command! Commands are: c, s, q");
        readAndProcessCommands(module);

        System.out.println("Stopping...");
        module.shutdown();
        System.out.println("Stopped.");
    }

    private void readAndProcessCommands(NetFefRs485 module) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        char c = 'A';
        while(true) {
            String line = br.readLine();
            if((line == null) || line.startsWith("q")) {
                return;
            }
            if(line.startsWith("c")) {
                Frame frame = new Frame();
                frame.setTargetAddress(new byte[]{0x12, (byte)0xAB});
                frame.setCommand('t');
                frame.addParameter(new Parameter('v', ParameterType.CHAR, String.valueOf(c)));
                System.out.println("Sending char v=" + c);
                module.sendData(frame, NetFef.MASTER_ADDRESS);

                c += 1;
                if(c == 'Z') {
                    c = 'A';
                }
            }
            if(line.startsWith("s")) {
                Frame frame = new Frame();
                frame.setTargetAddress(new byte[]{0x12, (byte)0xAB});
                frame.setCommand('T');
                String value = "x" + String.valueOf(c) + "x";
                frame.addParameter(new Parameter('v', ParameterType.STRING1, value));
                System.out.println("Sending string v=" + value);
                module.sendData(frame, NetFef.MASTER_ADDRESS);

                c += 1;
                if(c == 'Z') {
                    c = 'A';
                }
            }
        }
    }
}
