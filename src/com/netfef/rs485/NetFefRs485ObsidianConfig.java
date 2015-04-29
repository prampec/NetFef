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

package com.netfef.rs485;

import com.netfef.protocol.obsidian.NetFefObsidianConfig;

/**
 * <p>Obsidian network configuration according to Rs485 speeds.</p>
 * <p>User: kelemenb
 * <br/>Date: 4/29/15</p>
 */
public class NetFefRs485ObsidianConfig extends NetFefObsidianConfig {
    public NetFefRs485ObsidianConfig() {
        replyMaxDelayMs = 1000;
        replyRepeatCount = 3;
        joinOfferRepeatSecs = 2*60;
        nextPollMaxSecs = 5*60;
        pollRetryDelaySecs = 30;

    }
}
