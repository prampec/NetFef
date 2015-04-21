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

import com.netfef.util.FormatHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>The NetFef protocol implementation.</p>
 * <p>User: kelemenb
 * <br/>Date: 4/9/15</p>
 */
public class NetFef {
    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(NetFef.class);

    public static byte[] BROADCAST_ADDRESS = new byte[] { 0x00, 0x00 };
    public static byte[] MASTER_ADDRESS = new byte[] { 0x00, 0x01 };

    public static Frame buildFrameObject(byte[] bytes, int maxLen, byte[] acceptAddress) {
        ByteArrayReader bar = new ByteArrayReader(bytes);
        Frame frame = new Frame();
        int len = bar.readInt2();
        if(bytes.length != len) {
            LOG.warn("Frame length consistency mismatch. Provided length: " + len + ", actual length: " + bytes.length);
            return null;
        }
        if(len > maxLen) {
            // -- Frame too long
            LOG.warn("Frame size " + len + " is bigger than maximal accepted length: " + maxLen);
            return null;
        }

        int targetAddressLength = bar.readInt1();
        byte[] targetAddress = new byte[targetAddressLength];
        for (int i = 0; i < targetAddressLength; i++) {
            targetAddress[i] = bar.readByte();
        }
        if((acceptAddress != null) && !Arrays.equals(targetAddress, acceptAddress) && !Arrays.equals(targetAddress, BROADCAST_ADDRESS)) {
            // -- Frame is not for us.
            LOG.info("Skipping frame as it is for " + FormatHelper.byteArrayToString(targetAddress) + " (and we are looking for address " + FormatHelper.byteArrayToString(acceptAddress) + ")");
            return null;
        }
        frame.setTargetAddress(targetAddress);

        int senderAddressLength = bar.readInt1();
        byte[] senderAddress = new byte[senderAddressLength];
        for (int i = 0; i < senderAddressLength; i++) {
            senderAddress[i] = bar.readByte();
        }
        frame.setSenderAddress(senderAddress);

        int parameterCount = bar.readInt1();
        List<Parameter> parameters = readFrameParameters(bar, parameterCount);

        frame.setParameters(parameters);

        return frame;
    }

    private static List<Parameter> readFrameParameters(ByteArrayReader bar, int parameterCount) {
        List<Parameter> parameters = new ArrayList<>();

        LOG.trace("Trying to read " + parameterCount + " parameter(s).");

        for (int i = 0; i < parameterCount; i++) {
            Parameter parameter = readParameter(bar);
            parameters.add(parameter);
        }

        return parameters;
    }

    private static Parameter readParameter(ByteArrayReader bar) {
        char parameterName = bar.readChar();
        char parameterTypeVisual = bar.readChar();
        ParameterType parameterType = ParameterType.getByVisual(parameterTypeVisual);

        Parameter parameter = new Parameter(parameterName, parameterType);

        if(parameterType == ParameterType.BOOLEAN) {
            boolean value = bar.readChar() != 0;
            parameter.setValue(value);
        } else if(parameterType == ParameterType.BYTE) {
            int value = bar.readChar();
            parameter.setValue(value);
        } else if(parameterType == ParameterType.INTEGER) {
            int value = bar.readInt2();
            parameter.setValue(value);
        } else if(parameterType == ParameterType.SIGNED_INTEGER) {
            int value = bar.readInt2();
            if((value & (0x1<<15)) > 0) {
                value ^= 0x1<<15;
                value = -value;
            }
            parameter.setValue(value);
        } else if(parameterType == ParameterType.LONG) {
            long value = bar.readInt4();
            parameter.setValue(value);
        } else if(parameterType == ParameterType.SIGNED_LONG) {
            long value = bar.readInt4();
            if((value & (0x1<<31)) > 0) {
                value ^= 0x1<<31;
                value = -value;
            }
            parameter.setValue(value);
        } else if(parameterType == ParameterType.CHAR) {
            char value = bar.readChar();
            parameter.setValue(value);
        } else if(parameterType == ParameterType.STRING1) {
            int length = bar.readInt1();
            String value = bar.readString(length);
            parameter.setValue(value);
        } else if(parameterType == ParameterType.STRING2) {
            int length = bar.readInt2();
            String value = bar.readString(length);
            parameter.setValue(value);
        }

        return parameter;
    }


    public static byte[] buildFrameBytes(Frame frame, byte[] myAddress) {
        if(myAddress != null) {
            frame.setSenderAddress(myAddress);
        }
        ByteArrayBuilder bab = new ByteArrayBuilder();

        bab.append1(frame.getTargetAddress().length);
        bab.append(frame.getTargetAddress());
        bab.append1(frame.getSenderAddress().length);
        bab.append(frame.getSenderAddress());
        bab.append1(frame.getParameters().size() + 1);
        buildParameterBytes(bab, frame.getCommand());
        for (Parameter parameter : frame.getParameters().values()) {
            buildParameterBytes(bab, parameter);
        }

        bab.insertToBeginning(bab.getSize() + 2);

        return bab.getBytes();
    }

    private static ByteArrayBuilder buildParameterBytes(ByteArrayBuilder bab, Parameter parameter) {
        bab.append(parameter.getParameterName());
        ParameterType parameterType = parameter.getParameterType();
        bab.append(parameterType.getVisual());
        if(parameterType == ParameterType.BOOLEAN) {
            bab.append(parameter.getBooleanValue() ? '1' : '0');
        } else if(parameterType == ParameterType.BYTE) {
            bab.append1(parameter.getIntValue());
        } else if(parameterType == ParameterType.INTEGER) {
            bab.append2(parameter.getIntValue());
        } else if(parameterType == ParameterType.SIGNED_INTEGER) {
            bab.append2(parameter.getIntValue());
        } else if(parameterType == ParameterType.LONG) {
            bab.append4(parameter.getLongValue());
        } else if(parameterType == ParameterType.SIGNED_LONG) {
            bab.append4(parameter.getLongValue());
        } else if(parameterType == ParameterType.CHAR) {
            bab.append(parameter.getStringValue().getBytes());
        } else if(parameterType == ParameterType.STRING1) {
            String stringValue = parameter.getStringValue();
            bab.append1(stringValue.length() + 1);
            bab.append(stringValue.getBytes());
            bab.append('\0');
        } else if(parameterType == ParameterType.STRING2) {
            String stringValue = parameter.getStringValue();
            bab.append2(stringValue.length() + 1);
            bab.append(stringValue.getBytes());
            bab.append('\0');
        } else {
            throw new UnsupportedOperationException("Parameter type " + parameterType + " is not implemented.");
        }

        return bab;
    }

}
