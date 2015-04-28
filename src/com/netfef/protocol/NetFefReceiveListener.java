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
import java.util.EventListener;

/**
 * <p>Called when frame received.</p>
 * <p>User: kelemenb
 * <br/>Date: 4/28/15</p>
 */
public interface NetFefReceiveListener {

    void dataReceived(Frame frame);

    void handleError(IOException e);
}
