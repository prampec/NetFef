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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>A communication package.</p>
 * <p>User: kelemenb
 * <br/>Date: 4/8/15</p>
 */
public class Frame {
    byte[] targetAddress;
    byte[] senderAddress;
    Parameter command;
    Map<Character, Parameter> parameters = new HashMap<Character, Parameter>();

    public byte[] getTargetAddress() {
        return targetAddress;
    }

    public void setTargetAddress(byte[] targetAddress) {
        this.targetAddress = targetAddress;
    }

    public byte[] getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(byte[] senderAddress) {
        this.senderAddress = senderAddress;
    }

    public Parameter getCommand() {
        return command;
    }

    public void setCommand(Parameter command) {
        this.command = command;
    }

    Map<Character, Parameter> getParameters() {
        return parameters;
    }

    void setParameters(Map<Character, Parameter> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("From:");
        addBytes(sb, senderAddress);
        sb.append(" To:");
        addBytes(sb, targetAddress);
        sb.append(" Cmd:");
        sb.append(command);
        sb.append(" Params:[");
        boolean first = true;
        for (Parameter parameter : parameters.values()) {
            if(first) {
                first = false;
            } else {
                sb.append(",");
            }
            sb.append(parameter.toString());
        }
        sb.append("]");
        return sb.toString();
    }

    private StringBuilder addBytes(StringBuilder sb, byte[] bytes) {
        if(bytes == null) {
            return sb.append("N/A");
        }
        for (byte aByte : bytes) {
            sb.append(FormatHelper.toHexString(aByte));
        }
        return sb;
    }

    public void addParameter(Parameter parameter) {
        parameters.put(parameter.getParameterName(), parameter);
    }

    public void setParameters(List<Parameter> parameters) {
        for (Parameter parameter : parameters) {
            if('c' == parameter.getParameterName()) {
                this.command = parameter;
            } else {
                this.parameters.put(parameter.getParameterName(), parameter);
            }
        }
    }
}
