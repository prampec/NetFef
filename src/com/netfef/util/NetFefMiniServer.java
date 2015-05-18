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
import com.netfef.protocol.NetFefNetwork;
import com.netfef.protocol.NetFefPhysicalLayer;
import com.netfef.protocol.NetFefReceiveListener;
import com.netfef.protocol.obsidian.NetFefObsidian;
import com.netfef.rs485.NetFefRs485;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <p>Test class for Obsidian protocol.</p>
 * <p>User: kelemenb
 * <br/>Date: 4/30/15</p>
 */
public class NetFefMiniServer {

    public static void main(String[] args) throws IOException {
        new NetFefMiniServer().run();
    }

    private void run() throws IOException {
        NetFefNetwork network = new NetFefObsidian();
        NetFefPhysicalLayer physicalLayer = new NetFefRs485();
        network.init(physicalLayer, 12345L, NetFefDataHelper.MASTER_ADDRESS, peer -> {
            System.out.println("Peer registered: " + peer + " Active:" + peer.isActive());
        });
        network.setListener(new NetFefReceiveListener() {
            @Override
            public void dataReceived(Frame frame) {
                System.out.println("Frame received: " + frame);
            }

            @Override
            public void handleError(IOException e) {
                System.out.println("Error occurred: " + e.getMessage());
            }
        });

        System.out.println("Started. Enter command! Commands are: q, t");
        readAndProcessCommands(network);

        System.out.println("Stopping...");
        network.shutdown();
        System.out.println("Stopped.");

    }

    private void readAndProcessCommands(NetFefNetwork network) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String line = br.readLine();
            if ((line == null) || line.startsWith("q")) {
                return;
            }
            else {
                Frame frame = null;
                if(line.startsWith("t")) {
                    frame = new Frame(NetFefDataHelper.BROADCAST_ADDRESS, 'i', 't');
                    frame.addParameter(new Parameter('v', ParameterType.STRING1, formatDate()));
                }

                if(frame != null) {
                    network.sendData(frame);
                }
            }
        }
    }

    private String formatDate() {
        return new SimpleDateFormat("yyMMddhhmmssF").format(new Date());
    }


}
