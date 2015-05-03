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
import com.netfef.protocol.obsidian.ReplyListener;

import java.io.IOException;

/**
 * <p>Network is a group of devices connected together.</p>
 * <p>User: kelemenb
 * <br/>Date: 4/28/15</p>
 */
public interface NetFefNetwork {

    void setListener(NetFefReceiveListener listener);

    void init(NetFefPhysicalLayer physicalLayer, long networkIdentity, byte[] myAddress, PeerPersister peerPersister);

    void sendData(Frame frame);
    void sendData(Frame frame, ReplyListener replyListener);

    void shutdown();
}
