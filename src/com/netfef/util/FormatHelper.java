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

package com.netfef.util;

/**
 * <p>Help to visualize byte values.</p>
 * <p>User: kelemenb
 * <br/>Date: 4/16/15</p>
 */
public class FormatHelper {

    public static String byteArrayToString(byte[] array) {
        StringBuilder sb = new StringBuilder(3*array.length +10);
        sb.append("(").append(array.length).append(")");
        sb.append("[");

        boolean first = true;
        for (byte b : array) {
            if(!first) {
                sb.append(" ");
            } else {
                first = false;
            }
            sb.append(toHexString(b));
        }
        sb.append("]");

        return sb.toString();
    }

    public static String byteArrayToString2(byte[] array) {
        StringBuilder sb = new StringBuilder(3*array.length +10);
//        sb.append("(").append(array.length).append(")");
        sb.append("{");

        boolean first = true;
        for (byte b : array) {
            if(!first) {
                sb.append(", ");
            } else {
                first = false;
            }
            sb.append("0x").append(toHexString2(b));
        }
        sb.append("}");

        return sb.toString();
    }

    public static String toHexString(byte aByte) {
        String str = Integer.toHexString(byteToInt(aByte));
        String s = str.length() == 1 ? "0" + str : str;
        if((aByte >= ' ') && (aByte <= '~')) {
            s = s + "(" + (char)aByte + ")";
        }
        return s;

    }
    public static String toHexString2(byte aByte) {
        String str = Integer.toHexString(byteToInt(aByte));
        String s = str.length() == 1 ? "0" + str : str;
        return s;

    }
    public static int byteToInt(byte aByte) {
        return aByte & 0xFF;
    }

}
