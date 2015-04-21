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


    public static int getInt2(byte byte0, byte byte1) {
        int retVal = (int)byte0*255 + byte1;
        return retVal;
    }

    public char readChar() {
        return (char)readByte();
    }

    public int readInt1() {
        return readByte();
    }

    public String readString(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
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
        long retVal = (long)b0 | b1<<8 | b2<<16 | b3 <<24;
        return retVal;
    }
}
