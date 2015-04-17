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
 * <p>A parameter in the Frame</p>
 * <p>User: kelemenb
 * <br/>Date: 4/8/15</p>
 */
public class Parameter {
    char parameterName;
    ParameterType parameterType;
    Integer length;
    String stringValue;
    Integer intValue;

    public Parameter(char parameterName, ParameterType parameterType, char value) {
        this.parameterName = parameterName;
        this.parameterType = parameterType;
        stringValue = String.valueOf(value);
    }

    public Parameter(char parameterName, ParameterType parameterType) {
        this.parameterType = parameterType;
        this.parameterName = parameterName;
    }

    public Parameter(char parameterName, ParameterType parameterType, String value) {
        this.parameterName = parameterName;
        this.parameterType = parameterType;
        stringValue = value;
    }

    public char getParameterName() {
        return parameterName;
    }

    public ParameterType getParameterType() {
        return parameterType;
    }

    public Integer getLength() {
        return length;
    }

    public String getStringValue() {
        return stringValue;
    }

    public Integer getIntValue() {
        return intValue;
    }

    @Override
    public String toString() {
        return String.valueOf(parameterName) + "(" + parameterType.getVisual() + ")" + stringValue;
    }

    public void setValue(char value) {
        this.stringValue = String.valueOf(value);
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public void setValue(String value) {
        this.stringValue = value;
    }
}
