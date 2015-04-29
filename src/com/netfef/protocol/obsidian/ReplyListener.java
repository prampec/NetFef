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

import java.util.EventListener;

/**
 * <p>Called when reply requested.</p>
 * <p>User: kelemenb
 * <br/>Date: 4/29/15</p>
 */
public abstract class ReplyListener implements EventListener {
    public abstract void onReply(Frame originalFrame, Frame repliedFrame);
    public void onError(Frame originalFrame) {};
}
