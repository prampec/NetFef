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

import com.netfef.data.Frame;
import com.netfef.data.NetFefDataHelper;
import com.netfef.data.Parameter;
import com.netfef.data.ParameterType;
import com.netfef.protocol.NetFefReceiveListener;
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
        NetFefRs485 module = new NetFefRs485();
        module.setListener(
                new NetFefReceiveListener() {
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

        System.out.println("Started. Enter command! Commands are: B, b, i, I, l, L, c, s, S, q");
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
            } else {
                Frame frame = new Frame(new byte[]{0x12, (byte)0xAB}, 't', 't');

                for (byte b : line.getBytes()) {
                    Parameter v = null;
                    char p = (char)b;
                    if(p == 'c') {
                        v = new Parameter(c, ParameterType.CHAR, String.valueOf(c));
                        System.out.println("Sending char " + c + "=" + c);
                    } else if(p == 'B') {
                        boolean value = c % 2 == 0;
                        v = new Parameter(c, ParameterType.BOOLEAN);
                        v.setValue(value);
                        System.out.println("Sending boolean " + c + "=" + value);
                    } else if(p == 'b') {
                        byte value = (byte)c;
                        v = new Parameter(c, ParameterType.BYTE);
                        v.setValue(value);
                        System.out.println("Sending byte " + c + "=" + value);
                    } else if(p == 'i') {
                        int value = (int)c * 63;
                        v = new Parameter(c, ParameterType.INTEGER);
                        v.setValue(value);
                        System.out.println("Sending int " + c + "=" + value);
                    } else if(p == 'I') {
                        int value = -(int)c * 37;
                        v = new Parameter(c, ParameterType.SIGNED_INTEGER);
                        v.setValue(value);
                        System.out.println("Sending signed int " + c + "=" + value);
                    } else if(p == 'l') {
                        long value = (int)c * 2345;
                        v = new Parameter(c, ParameterType.LONG);
                        v.setValue(value);
                        System.out.println("Sending long " + c + "=" + value);
                    } else if(p == 'L') {
                        long value = -(int)c * 2345;
                        v = new Parameter(c, ParameterType.SIGNED_LONG);
                        v.setValue(value);
                        System.out.println("Sending signed long " + c + "=" + value);
                    } else if(p == 's') {
                        String value = "x" + String.valueOf(c) + "x";
                        v = new Parameter(c, ParameterType.STRING1, value);
                        System.out.println("Sending string " + c + "=" + value);
                    } else if(p == 'S') {
                        String value = "Xx" + String.valueOf(c) + "xX";
                        v = new Parameter(c, ParameterType.STRING2, value);
                        System.out.println("Sending string " + c + "=" + value);
                    }

                    if(v != null) {
                        frame.addParameter(v);

                        c += 1;
                        if(c == 'Z') {
                            c = 'A';
                        }
                    }
                }
                module.sendData(frame, NetFefDataHelper.MASTER_ADDRESS);

            }
        }
    }
}
