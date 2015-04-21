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

import org.junit.Assert;
import org.junit.Test;

/**
 * <p>Test methods for the protocol algorithms.</p>
 * <p>User: kelemenb
 * <br/>Date: 4/9/15</p>
 */

public class NetFefTest {

    @Test
    public void buildToBytes2() {
        byte[] bytes = ByteArrayBuilder.toBytes2(12345);
        Assert.assertArrayEquals("Failed building bytes", new byte[] { 48, 105}, bytes);
    }

    @Test
    public void buildFrameBytesTest() {
        Frame frame = new Frame();
        frame.setTargetAddress(new byte[]{0x12, (byte)0xAB});
        frame.setCommand(new Parameter('c', ParameterType.CHAR, 't'));
        frame.addParameter(new Parameter('t', ParameterType.STRING1, "s123456789"));
        {
            Parameter p = new Parameter('b', ParameterType.SIGNED_INTEGER);
            p.setValue(-12345);
            frame.addParameter(p);
        }
        {
            Parameter p = new Parameter('a', ParameterType.SIGNED_LONG);
            p.setValue(-1234567890L);
            frame.addParameter(p);
        }
        byte[] bytes = NetFef.buildFrameBytes(frame, NetFef.MASTER_ADDRESS);
    }

    @Test
    public void encodeDecodeTest() {
        byte[] bytes = new byte[]{0, 26, 2, 18, -85, 2, 0, 1, 2, 99, 99, 116, 116, 115, 10, 115, 49, 50,
                51, 52, 53, 54, 55, 56, 57, 0};

        Frame frame = NetFef.buildFrameObject(bytes, 100, null);

        byte[] bytes1 = NetFef.buildFrameBytes(frame, null);

        Assert.assertArrayEquals("Decoded message does not matches the original one.", bytes, bytes1);
    }

}
