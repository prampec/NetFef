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

/**
 * <p>Helps to ready values out of a string array.</p>
 * <p>User: kelemenb
 * <br/>Date: 4/9/15</p>
 */
public class ByteArrayReader {

    private int offset;
    private final int length;
    private final byte[] bytes;

    public ByteArrayReader(byte[] bytes) {
        this.bytes = bytes;
        this.length = bytes.length;
        this.offset = 0;
    }

    public byte readByte() {
        if(offset >= length) {
            throw new IndexOutOfBoundsException("Trying to read from position " + offset + ", but array is only " + length + " long.");
        }
        return bytes[offset++];
    }

    public int readInt2() {
        return getInt2(readByte(), readByte());
    }


    private static int getInt2(byte b0, byte b1) {
        int retVal = Byte.toUnsignedInt(b0) << 8 | Byte.toUnsignedInt(b1);
        return retVal;
    }

    private static int getSignedInt2(byte b0, byte b1) {
        int i0 = Byte.toUnsignedInt(b0);
        int i1 = Byte.toUnsignedInt(b1);
        int min = 1;
        if((i0 & 0b10000000) > 0) {
            i0 = i0 ^ 0b11111111;
            i1 = i1 ^ 0b11111111;
            min = -1;
        }
        int retVal = i0 << 8 | i1;
        return min * (retVal + 1);
    }

    private static long getSignedInt4(byte b0, byte b1, byte b2, byte b3) {
        int i0 = Byte.toUnsignedInt(b0);
        int i1 = Byte.toUnsignedInt(b1);
        int i2 = Byte.toUnsignedInt(b2);
        int i3 = Byte.toUnsignedInt(b3);
        long min = 1;
        if((i0 & 0b10000000) > 0) {
            i0 = i0 ^ 0b11111111;
            i1 = i1 ^ 0b11111111;
            i2 = i2 ^ 0b11111111;
            i3 = i3 ^ 0b11111111;
            min = -1;
        }
        long retVal =i3 | i2<<8 | i1<<16 | i0 <<24;
        return min * (retVal + 1);
    }

    public char readChar() {
        return (char)readByte();
    }

    public int readInt1() {
        return readByte();
    }

    public String readString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < (length-1); i++) {
            byte aByte = readByte();
            sb.append((char)aByte);
        }
        readByte(); // -- String should end with '\0'
        return sb.toString();
    }

    public long readInt4() {
        return readInt4(readByte(), readByte(), readByte(), readByte());
    }

    private long readInt4(byte b0, byte b1, byte b2, byte b3) {
        long retVal = Byte.toUnsignedLong(b3) | Byte.toUnsignedLong(b2)<<8 | Byte.toUnsignedLong(b1)<<16 | Byte.toUnsignedLong(b0) <<24;
        return retVal;
    }

    public int readSignedInt2() {
        return getSignedInt2(readByte(), readByte());
    }

    public long readSignedInt4() {
        return getSignedInt4(readByte(), readByte(), readByte(), readByte());
    }
}
