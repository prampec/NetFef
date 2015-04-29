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

import java.util.Arrays;
import java.util.Date;

/**
 * <p>Slave device on the network.</p>
 * <p>User: kelemenb
 * <br/>Date: 4/30/15</p>
 */
public class Peer {
    byte[] address;
    long registrationId;
    boolean active = true;
    public Date lastSeen;
    public Date nextPollTime;

    public Peer(byte[] address, long registrationId) {
        this.address = address;
        this.registrationId = registrationId;
    }

    public byte[] getAddress() {
        return address;
    }

    public void setAddress(byte[] address) {
        this.address = address;
    }

    public long getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(long registrationId) {
        this.registrationId = registrationId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
    }

    public Date getNextPollTime() {
        return nextPollTime;
    }

    public void setNextPollTime(Date nextPollTime) {
        this.nextPollTime = nextPollTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Peer that = (Peer)o;

        if (registrationId != that.registrationId) {
            return false;
        }
        if (!Arrays.equals(address, that.address)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(address);
        result = 31 * result + (int)(registrationId ^ (registrationId >>> 32));
        return result;
    }
}
