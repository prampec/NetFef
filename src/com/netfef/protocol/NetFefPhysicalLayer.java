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

package com.netfef.protocol;

import com.netfef.data.Frame;

import java.io.IOException;

/**
 * <p>Network is a group of devices connected together.</p>
 * <p>User: kelemenb
 * <br/>Date: 4/28/15</p>
 */
public interface NetFefPhysicalLayer {

    void setListener(NetFefReceiveListener listener);

    void init() throws IOException;

    void shutdown();

    void sendData(Frame frame, byte[] myAddress);

    NetFefNetworkConfig getConfig(Class<? extends NetFefNetwork> networkClass);
}
