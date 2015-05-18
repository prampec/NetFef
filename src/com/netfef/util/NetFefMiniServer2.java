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
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * <p>Test class for Obsidian protocol.</p>
 * <p>User: kelemenb
 * <br/>Date: 4/30/15</p>
 */
public class NetFefMiniServer2 {
    byte[] aPeerAddress;

    public static void main(String[] args) throws IOException {
        new NetFefMiniServer2().run();
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

        System.out.println("Started. Enter command! Commands are: q, t, l, s, r, i, n, f");
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
                if(line.startsWith("t")) {
                    frame = new Frame(NetFefDataHelper.BROADCAST_ADDRESS, 'm', 't');
                    frame.addParameter(new Parameter('i', ParameterType.STRING1, formatDate()));
                    frame.addParameter(new Parameter('h', ParameterType.INTEGER, formatDate2()));
                }
                else if(line.startsWith("l")) { // -- List log
                    frame = new Frame(aPeerAddress, 'm', 'l');
                    frame.addParameter(new Parameter('i', ParameterType.BYTE, 0));
                    forceReply = true;
                }
                else if(line.startsWith("s")) {
                    frame = new Frame(aPeerAddress, 'p', 's');
                    {
                        Struct struct = new Struct();
                        struct.addParameter(new Parameter('f', ParameterType.INTEGER, 700));
                        struct.addParameter(new Parameter('u', ParameterType.INTEGER, 800));
                        struct.addParameter(new Parameter('t', ParameterType.INTEGER, 3000));
                        frame.addParameter(new Parameter('p', ParameterType.STRUCT1, struct));
                    }
                    {
                        Struct struct = new Struct();
                        struct.addParameter(new Parameter('f', ParameterType.INTEGER, 800));
                        struct.addParameter(new Parameter('u', ParameterType.INTEGER, 900));
                        struct.addParameter(new Parameter('t', ParameterType.INTEGER, 3200));
                        frame.addParameter(new Parameter('p', ParameterType.STRUCT1, struct));
                    }
//                    frame.addParameter(new Parameter('i', ParameterType.BYTE, 0));
                    forceReply = true;
                }
                else if(line.startsWith("r")) {
                    frame = new Frame(aPeerAddress, 'p', 'r');
                    forceReply = true;
                }
                else if(line.startsWith("i")) {
                    frame = new Frame(aPeerAddress, 'p', 'i');
                    frame.addParameter(new Parameter('t', ParameterType.INTEGER, 3333));
                    forceReply = true;
                }
                else if(line.startsWith("n")) {
                    frame = new Frame(aPeerAddress, 'p', 'm');
                    frame.addParameter(new Parameter('v', ParameterType.CHAR, 'N'));
                    forceReply = true;
                }
                else if(line.startsWith("f")) {
                    frame = new Frame(aPeerAddress, 'p', 'm');
                    frame.addParameter(new Parameter('v', ParameterType.CHAR, 'F'));
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

    private int formatDate2() {
        String hhmm = new SimpleDateFormat("hhmm").format(new Date());
        return Integer.parseInt(hhmm);
    }

    private String formatDate() {
        return new SimpleDateFormat("yyMMddhhmmssF").format(new Date());
    }


}
