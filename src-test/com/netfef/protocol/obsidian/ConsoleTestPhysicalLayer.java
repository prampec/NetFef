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
import com.netfef.protocol.NetFefNetwork;
import com.netfef.protocol.NetFefNetworkConfig;
import com.netfef.protocol.NetFefPhysicalLayer;
import com.netfef.protocol.NetFefReceiveListener;

import java.io.IOException;

/**
 * <p>Simulate physical traffic with console inputs/outputs.</p>
 * <p>User: kelemenb
 * <br/>Date: 4/30/15</p>
 */
public class ConsoleTestPhysicalLayer implements NetFefPhysicalLayer {

    private byte[] emulatedAddress;
    private NetFefReceiveListener listener;
    private Frame emulatedClientLastReceivedFrame;

    public Frame getEmulatedClientLastReceivedFrame() {
        return emulatedClientLastReceivedFrame;
    }

    public ConsoleTestPhysicalLayer(byte[] emulatedAddress) {
        this.emulatedAddress = emulatedAddress;
    }

    @Override
    public void setListener(NetFefReceiveListener listener) {
        this.listener = listener;
    }

    @Override
    public void init() throws IOException {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void sendData(Frame frame, byte[] myAddress) {
        if(myAddress != null) {
            frame.setSenderAddress(myAddress);
        }
        System.out.println("Sending frame: " + frame);
        emulatedClientLastReceivedFrame = frame;
    }

    @Override
    public NetFefNetworkConfig getConfig(Class<? extends NetFefNetwork> networkClass) {
        if(NetFefObsidian.class.isAssignableFrom(networkClass)) {
            return new NetFefObsidianConfig() {
                {
                    replyMaxDelayMs = 1000;
                    replyRepeatCount = 3;
                    joinOfferRepeatSecs = 2*60;
                    nextPollMaxSecs = 5*60;
                    pollRetryDelaySecs = 30;
                }
            };
        }
        else {
            throw new UnsupportedOperationException("Network class " + networkClass.getName() + " configuration is not implemented within " + this.getClass().getName() + " .");
        }

    }

    public void emulateReceive(Frame frame) {
        frame.setSenderAddress(emulatedAddress);
        frame.setTargetAddress(NetFefDataHelper.MASTER_ADDRESS);
        System.out.println("Emulate receiving: " + frame);
        listener.dataReceived(frame);
    }
}
