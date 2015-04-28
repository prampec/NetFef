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

package com.netfef.data;

import org.junit.Assert;
import org.junit.Test;

/**
 * <p>Test methods for the data algorithms.</p>
 * <p>User: kelemenb
 * <br/>Date: 4/9/15</p>
 */

public class NetFefDataHelperTest {

    @Test
    public void buildToBytes2() {
        byte[] bytes = ByteArrayBuilder.toBytes2(12345);
        Assert.assertArrayEquals("Failed building bytes", new byte[] { 48, 57}, bytes);
    }

    @Test
    public void buildFrameBytesTest() {
        Frame frame = new Frame(new byte[]{0x12, (byte)0xAB}, 't', 't');
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
        byte[] bytes = NetFefDataHelper.buildFrameBytes(frame, NetFefDataHelper.MASTER_ADDRESS);
    }

    @Test
    public void encodeDecodeTest() {
        byte[] bytes = new byte[]{0x00, 0x1d, 0x02, 0x12, (byte)0xab, 0x02, 0x00, 0x01, 0x02, 0x73, 0x63, 0x74, 0x63, 0x63, 0x74, 0x74, 0x73, 0x0a, 0x73, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x00, 0x6e};

        Frame frame = NetFefDataHelper.buildFrameObject(bytes, 100, null);
        Assert.assertNotNull("Decode error: bytes does not form a valid frame", frame);

        byte[] bytes1 = NetFefDataHelper.buildFrameBytes(frame, null);

        Assert.assertArrayEquals("Decoded message does not matches the original one.", bytes, bytes1);
    }

    @Test
    public void encodeDecodeTest2() {
        byte[] bytes  = new byte[]{0x00, 0x2c, 0x02, 0x00, 0x01, 0x02, 0x12, (byte)0xab, 0x06, 0x73, 0x63, 0x74, 0x63, 0x63, 0x74, 0x61, 0x63, 0x56, 0x62, 0x73, 0x07, 0x61, 0x61, 0x38, 0x36, 0x62, 0x62, 0x00, 0x64, 0x53, 0x00, 0x07, 0x61, 0x61, 0x38, 0x36, 0x62, 0x62, 0x00, 0x65, 0x69, 0x30, 0x39, 0x4b};

        Frame frame = NetFefDataHelper.buildFrameObject(bytes, 100, null);
        Assert.assertNotNull("Decode error: bytes does not form a valid frame", frame);

        byte[] bytes1 = NetFefDataHelper.buildFrameBytes(frame, null);

        Assert.assertArrayEquals("Decoded message does not matches the original one.", bytes, bytes1);
    }

    @Test
    public void encodeDecodeTest3() {
        byte[] bytes = new byte[]{0x00, 0x20, 0x02, 0x00, 0x01, 0x02, 0x12, (byte)0xab, 0x05, 0x73, 0x63, 0x74, 0x63, 0x63, 0x74, 0x64, 0x49, (byte)0xcf, (byte)0xc7, 0x66, 0x6c, 0x00, (byte)0xbc, 0x61, (byte)0xbd, 0x67, 0x4c, (byte)0xff, 0x43, (byte)0x9e, 0x43, 0x30};

        Frame frame = NetFefDataHelper.buildFrameObject(bytes, 100, null);
        Assert.assertNotNull("Decode error: bytes does not form a valid frame", frame);

        byte[] bytes1 = NetFefDataHelper.buildFrameBytes(frame, null);

        Assert.assertArrayEquals("Decoded message does not matches the original one.", bytes, bytes1);
    }


    @Test
    public void encodeDecodeTest4() {
        byte[] bytes = new byte[]{0x00, 0x39, 0x02, 0x00, 0x01, 0x02, 0x12, (byte)0xab, 0x09, 0x63, 0x63, 0x74, 0x61, 0x63, 0x47, 0x62, 0x73, 0x07, 0x61, 0x61, 0x37, 0x31, 0x62, 0x62, 0x00, 0x64, 0x53, 0x00, 0x07, 0x61, 0x61, 0x37, 0x31, 0x62, 0x62, 0x00, 0x65, 0x69, 0x22, 0x1d, 0x66, 0x49, (byte)0xdd, 0x55, 0x67, 0x6c, 0x00, 0x0d, 0x5f, (byte)0xcf, 0x68, 0x4c, (byte)0xff, (byte)0xf2, (byte)0xa0, 0x31, 0x31};

        Frame frame = NetFefDataHelper.buildFrameObject(bytes, 100, null);
        Assert.assertNotNull("Decode error: bytes does not form a valid frame", frame);

        byte[] bytes1 = NetFefDataHelper.buildFrameBytes(frame, null);

        Assert.assertArrayEquals("Decoded message does not matches the original one.", bytes, bytes1);
    }

}
