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

import com.netfef.util.FormatHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>The NetFef data implementation.</p>
 * <p>User: kelemenb
 * <br/>Date: 4/9/15</p>
 */
public class NetFefDataHelper {
    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(NetFefDataHelper.class);

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

        // -- Check sum
        int sum = 0;
        for (int i = 0; i < (bytes.length-1); i++) {
            sum += Byte.toUnsignedInt(bytes[i]);
        }
        if((byte)sum != bytes[len-1]) {
            // -- Check sum mismatch
            LOG.warn("Check sum error. Dropping frame.");
            return null;
        }

        int targetAddressLength = bar.readInt1();
        byte[] targetAddress = new byte[targetAddressLength];
        for (int i = 0; i < targetAddressLength; i++) {
            targetAddress[i] = bar.readByte();
        }
        if((acceptAddress != null) && !Arrays.equals(targetAddress, acceptAddress) && !Arrays.equals(targetAddress, BROADCAST_ADDRESS)) {
            // -- Frame is not for us.
            LOG.info("Skipping frame as it is for " + FormatHelper.byteArrayToString3(targetAddress) + " (and we are looking for address " + FormatHelper.byteArrayToString3(acceptAddress) + ")");
            return null;
        }
        frame.setTargetAddress(targetAddress);

        int senderAddressLength = bar.readInt1();
        byte[] senderAddress = new byte[senderAddressLength];
        for (int i = 0; i < senderAddressLength; i++) {
            senderAddress[i] = bar.readByte();
        }
        frame.setSenderAddress(senderAddress);

        List<Parameter> parameters = readStructParameters(bar);

        frame.setParameters(parameters);

        return frame;
    }

    private static List<Parameter> readStructParameters(ByteArrayReader bar) {
        int parameterCount = bar.readInt1();

        List<Parameter> parameters = new ArrayList<>(parameterCount);

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
            int value = bar.readSignedInt2();
            parameter.setValue(value);
        } else if(parameterType == ParameterType.LONG) {
            long value = bar.readInt4();
            parameter.setValue(value);
        } else if(parameterType == ParameterType.SIGNED_LONG) {
            long value = bar.readSignedInt4();
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
        } else if(parameterType == ParameterType.STRUCT1) {
            int length = bar.readInt1();
            List<Parameter> parameters = readStructParameters(bar);
            Struct value = new Struct(parameters);
            parameter.setValue(value);
        } else if(parameterType == ParameterType.STRUCT2) {
            int length = bar.readInt2();
            List<Parameter> parameters = readStructParameters(bar);
            Struct value = new Struct(parameters);
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
        bab.append1(frame.getParameters().size() + 2);
        buildParameterBytes(bab, frame.getSubject());
        buildParameterBytes(bab, frame.getCommand());
        for (Parameter parameter : frame.getParameters()) {
            buildParameterBytes(bab, parameter);
        }

        bab.insertToBeginning(bab.getSize() + 2 + 1);

        // -- Calculate sum
        byte[] bytes = bab.getBytes();
        int sum = 0;
        for (byte aByte : bytes) {
            sum += Byte.toUnsignedInt(aByte);
        }
        byte sumByte = (byte)sum;
        bab.append(new byte[] {sumByte});

        return bab.getBytes();
    }

    private static ByteArrayBuilder buildParameterBytes(ByteArrayBuilder bab, Parameter parameter) {
        bab.append(parameter.getParameterName());
        // -- TODO: Should automatically switch between s/S and t/T types according to the size.
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
        } else if(parameterType == ParameterType.STRUCT1) {
            Struct structValue = parameter.getStructValue();
            ByteArrayBuilder structBytes = getStructBytes(structValue);
            bab.append1(structBytes.getSize());
            bab.append(structBytes.getBytes());
        } else if(parameterType == ParameterType.STRUCT2) {
            Struct structValue = parameter.getStructValue();
            ByteArrayBuilder structBytes = getStructBytes(structValue);
            bab.append2(structBytes.getSize());
            bab.append(structBytes.getBytes());
        } else {
            throw new UnsupportedOperationException("Parameter type " + parameterType + " is not implemented.");
        }

        return bab;
    }

    private static ByteArrayBuilder getStructBytes(Struct struct) {
        ByteArrayBuilder bab = new ByteArrayBuilder();
        bab.append1(struct.getParameters().size()); // -- Param count
        for (Parameter parameter : struct.getParameters()) {
            buildParameterBytes(bab, parameter);
        }
        return bab;
    }

}
