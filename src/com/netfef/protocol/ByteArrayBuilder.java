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

/**
 * <p>Similar to StringBuilder, this class helps you build byte arrays.</p>
 * <p>User: kelemenb
 * <br/>Date: 4/9/15</p>
 */
public class ByteArrayBuilder {
    byte[] value = new byte[10];
    int count = 0;


    private void ensureCapacityForLength(int minimumCapacity) {
        if (minimumCapacity - value.length > 0) {
            expandCapacity(minimumCapacity);
        }
    }

    private void expandCapacity(int minimumCapacity) {
        int newCapacity = value.length * 2 + 2;
        if (newCapacity - minimumCapacity < 0) {
            newCapacity = minimumCapacity;
        }
        if (newCapacity < 0) {
            if (minimumCapacity < 0) {
                throw new OutOfMemoryError();
            }
            newCapacity = Integer.MAX_VALUE;
        }
        value = Arrays.copyOf(value, newCapacity);
    }

    public ByteArrayBuilder append1(int toAppend) {
//        if (str == null)
//            return appendNull();
        int len = 1;
        ensureCapacityForLength(count + len);
        byte[] source = toBytes1(toAppend);
        System.arraycopy(source, 0, value, count, len);
        count += len;
        return this;
    }

    public static byte[] toBytes1(int value) {
        byte[] bytes = new byte[1];
        bytes[0] = (byte)value;
        return bytes;
    }

    public static byte[] toBytes2(int value) {
        byte[] bytes = new byte[2];
        int x = value / 255;
        bytes[0] = (byte)x;
        bytes[1] = (byte)(value - x*255);
        return bytes;
    }

    public byte[] getBytes() {
        return Arrays.copyOf(value, count);
    }

    public ByteArrayBuilder append(byte[] toAppend) {
        if (toAppend == null)
            return appendNull();
        int len = toAppend.length;
        ensureCapacityForLength(count + len);
        byte[] source = toAppend;
        System.arraycopy(source, 0, value, count, len);
        count += len;
        return this;
    }

    private ByteArrayBuilder appendNull() {
        return append(new byte[]{(byte)0xFF});
    }

    public ByteArrayBuilder append(char toAppend) {
        return this.append((byte)toAppend);
    }

    private ByteArrayBuilder append(byte toAppend) {
//        if (str == null)
//            return appendNull();
        int len = 1;
        ensureCapacityForLength(count + len);
        value[count] = toAppend;
        count += len;
        return this;
    }

    public int getSize() {
        return count;
    }

    public ByteArrayBuilder insert2(int toAppend) {
//        if (str == null)
//            return appendNull();
        int len = 2;
        ensureCapacityForLength(count + len);
        byte[] source = toBytes2(toAppend);
        System.arraycopy(value, 0, value, len, count);
        System.arraycopy(source, 0, value, 0, len);
        count += len;
        return this;
    }
}
