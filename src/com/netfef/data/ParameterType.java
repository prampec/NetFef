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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>Parameter type arriving in the packet</p>
 * <p>User: kelemenb
 * <br/>Date: 4/8/15</p>
 */
public enum ParameterType {
    BOOLEAN('B'),
    /** 8 bit unsigned number */
    BYTE('b'),
    /** 16 bit unsigned number */
    INTEGER('i'),
    /** 16 bit signed number */
    SIGNED_INTEGER('I'),
    /** 32 bit unsigned number */
    LONG('l'),
    /** 32 bit signed number */
    SIGNED_LONG('L'),
    CHAR('c'),
    /** String with followed by 1 byte of length. */
    STRING1('s'),
    /** String with followed by 2 bytes of length. */
    STRING2('S'),
    /** Struct with followed by 1 byte of length. */
    STRUCT1('t'),
    /** Struct with followed by 2 bytes of length. */
    STRUCT2('T'),
    ;

    private char visual;

    ParameterType(char visual) {
        this.visual = visual;
    }

    public char getVisual() {
        return visual;
    }

    private static final Map<Character, ParameterType> byVisualMap;

    static {
        Map<Character, ParameterType> tempMap = new HashMap<>();
        for (ParameterType parameterType : ParameterType.values()) {
            tempMap.put(parameterType.visual, parameterType);
        }
        byVisualMap = Collections.unmodifiableMap(tempMap);
    }

    public static ParameterType getByVisual(char c) {
        return byVisualMap.get(c);
    }
}
