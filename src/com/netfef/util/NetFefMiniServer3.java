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

import com.netfef.data.*;
import com.netfef.protocol.NetFefNetwork;
import com.netfef.protocol.NetFefPhysicalLayer;
import com.netfef.protocol.NetFefReceiveListener;
import com.netfef.protocol.obsidian.NetFefObsidian;
import com.netfef.protocol.obsidian.ReplyListener;
import com.netfef.rs485.NetFefRs485;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * <p>Test class for Obsidian protocol.</p>
 * <p>User: kelemenb
 * <br/>Date: 4/30/15</p>
 */
public class NetFefMiniServer3 {
    byte[] aPeerAddress;

    public static void main(String[] args) throws IOException {
        new NetFefMiniServer3().run();
    }

    private void run() throws IOException {
        NetFefNetwork network = new NetFefObsidian();
        NetFefPhysicalLayer physicalLayer = new NetFefRs485();
        network.init(physicalLayer, 12345L, NetFefDataHelper.MASTER_ADDRESS, peer -> {
            System.out.println("Peer registered: " + peer + " Active:" + peer.isActive());
            byte[] address = peer.getAddress();
            aPeerAddress = Arrays.copyOf(address, address.length);
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

        System.out.println("Started. Enter command! Commands are: q, r, b1, b2, s");
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
                boolean forceReply = false;
                if(line.startsWith("r")) { // -- Logic: read analog value
                    frame = new Frame(NetFefDataHelper.BROADCAST_ADDRESS, 'l', 'r');
                }
                else if(line.startsWith("b1")) { // -- Logic: blink mode
                    frame = new Frame(aPeerAddress, 'l', 'b');
                    Struct struct = new Struct();
                    struct.addParameter(new Parameter('n', ParameterType.LONG, 200)); // -- ON (ms)
                    struct.addParameter(new Parameter('f', ParameterType.LONG, 400)); // -- OFF (ms)
                    struct.addParameter(new Parameter('c', ParameterType.BYTE, 3)); // -- Count
                    struct.addParameter(new Parameter('d', ParameterType.LONG, 2000)); // -- Delay (ms)
                    frame.addParameter(new Parameter('v', ParameterType.STRUCT1, struct));
                    forceReply = true;
                }
                else if(line.startsWith("b2")) { // -- Logic: blink mode
                    frame = new Frame(aPeerAddress, 'l', 'b');
                    Struct struct = new Struct();
                    struct.addParameter(new Parameter('n', ParameterType.LONG, 500)); // -- ON (ms)
                    struct.addParameter(new Parameter('f', ParameterType.LONG, 500)); // -- OFF (ms)
                    struct.addParameter(new Parameter('c', ParameterType.BYTE, 0)); // -- Count
                    struct.addParameter(new Parameter('d', ParameterType.LONG, 0)); // -- Delay (ms)
                    frame.addParameter(new Parameter('v', ParameterType.STRUCT1, struct));
                    forceReply = true;
                }
                else if(line.startsWith("s")) {
                    frame = new Frame(aPeerAddress, 'l', 's'); // -- Logic: reset blink
                    forceReply = true;
                }

                if(frame != null) {
                    ReplyListener replyListener = null;
                    if(forceReply) {
                        replyListener = new ReplyListener() {
                            @Override
                            public void onReply(Frame originalFrame, Frame repliedFrame) {
                                System.out.println("Frame replied: " + repliedFrame);
                            }

                            @Override
                            public void onError(Frame originalFrame) {
                                System.out.println("Request not replied.");
                            }
                        };
                    }
                    network.sendData(frame, replyListener);
                }
            }
        }
    }

}
