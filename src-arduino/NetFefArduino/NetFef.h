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
    NetFefFrameBuilder(byte* buffer, unsigned int buffLen, const byte* myAddress, const byte* targetAddress, char command);
    boolean addParameter(char parameterName, char parameterType, char* value);
    boolean addParameter(char parameterName, char parameterType, unsigned int value);
    boolean addParameter(char parameterName, char parameterType, int value);
    boolean addParameter(char parameterName, char parameterType, unsigned long value);
    boolean addParameter(char parameterName, char parameterType, long value);
    byte* getFrameBytes();
    unsigned int getFrameLength();
    Print* _debug = NULL;
    
  private:
    byte* _bytes;
    unsigned int _buffLen;
    unsigned int _pos;
    unsigned int _paramCountPos;
    boolean _addByte(byte value);
    boolean _addInt2(unsigned int value);
    boolean _addInt4(unsigned long value);
};

class NetFefFrameReader {
  public:
    NetFefFrameReader(byte* frame, unsigned int frameSize);
    boolean isForMe(const byte* myAddress);
    byte* getSenderAddress();
    byte* getCommand();
    byte* getParameter(char parameterName);
    byte* getNextParameter(byte* previous);

    byte targetAddressLength;
    byte sourceAddressLength;
    Print* _debug = NULL;
    static unsigned int getInt(byte* position);
    unsigned int frameLength;
    
  private:
    byte* _bytes;
    unsigned int _frameSize;
    unsigned int _paramCount;
    unsigned int _paramsPos;
    unsigned int _frameMaxSize;
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
