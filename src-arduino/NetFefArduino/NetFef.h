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
#ifndef NETFEF_H
#define NETFEF_H

#include <Arduino.h>

const byte BROADCAST_ADDRESS[2] = { 0x00, 0x00 };
const byte MASTER_ADDRESS[2] = { 0x00, 0x01 };

class NetFefFrameBuilder {
  public:
    NetFefFrameBuilder(byte* buffer, const byte* myAddress, const byte* targetAddress, char command);
    void addParameter(char parameterName, char parameterType, char* value);
    void addParameter(char parameterName, char parameterType, unsigned int value);
    void addParameter(char parameterName, char parameterType, int value);
    void addParameter(char parameterName, char parameterType, unsigned long value);
    void addParameter(char parameterName, char parameterType, long value);
    byte* getFrameBytes();
    int getFrameLength();
    
  private:
    byte* _bytes;
    int _pos;
    int _paramCountPos;
    void _addByte(byte value);
    void _addInt2(unsigned int value);
    void _addInt4(unsigned long value);
};

class NetFefFrameReader {
  public:
    NetFefFrameReader(byte* frame);
    boolean isForMe(const byte* myAddress);
    byte* getSenderAddress();
    byte* getCommand();
    byte* getParameter(char parameterName);
    byte* getNextParameter(byte* previous);

    byte targetAddressLength;
    byte sourceAddressLength;
    Print* _debug = NULL;
    static unsigned int getInt(byte* position);
    
  private:
    byte* _bytes;
    int _paramCount;
    int _paramsPos;
};

class NetFefParameter {
  public:
    NetFefParameter(byte* parameterPointer);
    int calculateSpace();
    char getName();
    char getType();
    boolean isType(char parameterType);
    boolean getBooleanValue();
    byte getByteValue();
    int getSignedIntValue();
    unsigned int getIntValue();
    long getLongValue(); // -- We do not have getULongValue method, since long is always 32bits, so you can just cast to unsigned long.
    char getCharValue();
    char* getStringValue();
    Print* _debug = NULL;
    
  private:
    byte* _pp;
};

#endif // -- NETFEF_H
