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
import com.netfef.rs485.NetFefRs485;

import java.io.IOException;

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

        System.out.println("Started. Press ENTER to exit.");
        System.in.read();

        System.out.println("Stopping...");
        module.shutdown();
        System.out.println("Stopped.");
    }
}
