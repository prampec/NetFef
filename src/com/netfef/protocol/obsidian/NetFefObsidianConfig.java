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

import com.netfef.protocol.NetFefNetworkConfig;

/**
 * <p>Configuration for Obsidian network protocol implementation.</p>
 * <p>User: kelemenb
 * <br/>Date: 4/29/15</p>
 */
public abstract class NetFefObsidianConfig implements NetFefNetworkConfig {
    protected long replyMaxDelayMs;
    protected long replyRepeatCount;
    protected long joinOfferRepeatSecs;
    protected int nextPollMinSecs;
    protected int nextPollMaxSecs;
    protected int pollRetryDelaySecs;

    public long getReplyMaxDelayMs() {
        return replyMaxDelayMs;
    }

    public long getReplyRepeatCount() {
        return replyRepeatCount;
    }

    public long getJoinOfferRepeatSecs() {
        return joinOfferRepeatSecs;
    }

    public int getNextPollMinSecs() {
        return nextPollMinSecs;
    }
    public int getNextPollMaxSecs() {
        return nextPollMaxSecs;
    }

    public int getPollRetryDelaySecs() {
        return pollRetryDelaySecs;
    }
}
