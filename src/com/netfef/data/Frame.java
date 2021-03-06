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

import java.util.*;

/**
 * <p>A communication package.</p>
 * <p>User: kelemenb
 * <br/>Date: 4/8/15</p>
 */
public class Frame extends Struct {
    byte[] targetAddress;
    byte[] senderAddress;
    Parameter subject;
    Parameter command;

    Frame() {
    }

    public Frame(byte[] targetAddress, char subject, char command) {
        this.targetAddress = targetAddress;
        this.setSubject(subject);
        this.setCommand(command);
    }

    public Frame(byte[] targetAddress, Parameter subject, Parameter command) {
        this.targetAddress = targetAddress;
        this.subject = subject;
        this.command = command;
    }

    public byte[] getTargetAddress() {
        return targetAddress;
    }

    public void setTargetAddress(byte[] targetAddress) {
        this.targetAddress = Arrays.copyOf(targetAddress, targetAddress.length);
    }

    public byte[] getSenderAddress() {
        return senderAddress;
    }

    public void setSenderAddress(byte[] senderAddress) {
        this.senderAddress = Arrays.copyOf(senderAddress, senderAddress.length);
    }

    public Parameter getSubject() {
        return subject;
    }
    public Parameter getCommand() {
        return command;
    }

    public void setSubject(Parameter subject) {
        this.subject = subject;
    }
    public void setCommand(Parameter command) {
        this.command = command;
    }

    public void setSubject(char cmd) {
        this.setSubject(new Parameter('s', ParameterType.CHAR, cmd));
    }
    public void setCommand(char cmd) {
        this.setCommand(new Parameter('c', ParameterType.CHAR, cmd));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("From:");
        addBytes2(sb, senderAddress);
        sb.append(" To:");
        addBytes2(sb, targetAddress);
        sb.append(" Subj:");
        sb.append(subject);
        sb.append(" Cmd:");
        sb.append(command);
        sb.append(" Params:[");
        boolean first = true;
        for (Parameter parameter : getParameters()) {
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

    private StringBuilder addBytes2(StringBuilder sb, byte[] bytes) {
        if(bytes == null) {
            return sb.append("N/A");
        }
        for (byte aByte : bytes) {
            sb.append(FormatHelper.toHexString2(aByte));
        }
        return sb;
    }

    public void setParameters(List<Parameter> parameters) {
        for (Parameter parameter : parameters) {
            if('s' == parameter.getParameterName()) {
                this.subject = parameter;
            } else if('c' == parameter.getParameterName()) {
                this.command = parameter;
            } else {
                this.addParameter(parameter);
            }
        }
    }

}
