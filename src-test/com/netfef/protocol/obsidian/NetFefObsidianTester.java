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

package com.netfef.protocol.obsidian;

import com.netfef.data.Frame;
import com.netfef.data.NetFefDataHelper;
import com.netfef.data.Parameter;
import com.netfef.data.ParameterType;
import com.netfef.protocol.NetFefReceiveListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * <p>Test class for Obsidian protocol.</p>
 * <p>User: kelemenb
 * <br/>Date: 4/30/15</p>
 */
public class NetFefObsidianTester {

    public static final byte[] TARGET_ADDRESS = new byte[]{0x12, (byte)0xAB};
    private Frame lastFrameReceived;

    public static void main(String[] args) throws IOException {
        new NetFefObsidianTester().run();
    }

    private void run() throws IOException {
        NetFefObsidian netFefObsidian = new NetFefObsidian();
        ConsoleTestPhysicalLayer physicalLayer = new ConsoleTestPhysicalLayer(TARGET_ADDRESS);
        netFefObsidian.init(physicalLayer, NetFefDataHelper.MASTER_ADDRESS, peer -> {
            System.out.println("Peer registered: " + peer + " Actite:" + peer.isActive());
        });
        netFefObsidian.setListener(new NetFefReceiveListener() {
            @Override
            public void dataReceived(Frame frame) {
                lastFrameReceived = frame;
                System.out.println("Frame received: " + frame);
            }

            @Override
            public void handleError(IOException e) {
                System.out.println("Error occurred: " + e.getMessage());
            }
        });

        System.out.println("Started. Enter command! Commands are: j1, j2, J, p");
        readAndProcessCommands(netFefObsidian, physicalLayer);

        System.out.println("Stopping...");
        netFefObsidian.shutdown();
        System.out.println("Stopped.");

    }

    private void readAndProcessCommands(NetFefObsidian module, ConsoleTestPhysicalLayer physicalLayer) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String line = br.readLine();
            if ((line == null) || line.startsWith("q")) {
                return;
            }
            else {
                Frame frame = null;
                if(line.startsWith("j1")) {
                    frame = new Frame(TARGET_ADDRESS, 'n', 'j');
                }
                else if(line.startsWith("j2")) {
                    frame = new Frame(TARGET_ADDRESS, 'n', 'j');
                    frame.addParameter(new Parameter('i', ParameterType.INTEGER, 123));
                }
                else if(line.startsWith("J")) {
                    frame = new Frame(TARGET_ADDRESS, 'n', 'J');
                    frame.addParameter(new Parameter('R', ParameterType.INTEGER, physicalLayer.getEmulatedClientLastReceivedFrame().getParameter('r').getIntValue()));
                    frame.addParameter(new Parameter('d', ParameterType.STRING1, "Test client"));
                    frame.addParameter(new Parameter('v', ParameterType.STRING1, "0/0/O0"));
                    frame.addParameter(new Parameter('n', ParameterType.INTEGER, 21));
                }
                else if(line.startsWith("p")) {
                    frame = new Frame(TARGET_ADDRESS, 'n', 'p');
                    frame.addParameter(new Parameter('R', ParameterType.INTEGER, physicalLayer.getEmulatedClientLastReceivedFrame().getParameter('r').getIntValue()));
                    frame.addParameter(new Parameter('n', ParameterType.INTEGER, 21));
                }
                else if(line.startsWith("t")) {
                    frame = new Frame(TARGET_ADDRESS, 't', 't');
                    frame.addParameter(new Parameter('R', ParameterType.INTEGER, physicalLayer.getEmulatedClientLastReceivedFrame().getParameter('r').getIntValue()));
                    frame.addParameter(new Parameter('x', ParameterType.STRING1, "aabbcc"));
                    frame.addParameter(new Parameter('n', ParameterType.INTEGER, 21));
                }
                else if(line.startsWith("y")) {
                    frame = new Frame(TARGET_ADDRESS, 't', 't');
                    frame.addParameter(new Parameter('x', ParameterType.STRING1, "aabbcc"));
                }

                if(frame != null) {
                    physicalLayer.emulateReceive(frame);
                }
            }
        }
    }
}
