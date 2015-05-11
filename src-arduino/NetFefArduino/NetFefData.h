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
#ifndef NetFefData_H
#define NetFefData_H

#include <Arduino.h>

#define NETFEF_DATA_VERSION "0"

const byte BROADCAST_ADDRESS[2] = { 0x00, 0x00 };
const byte MASTER_ADDRESS[2] = { 0x00, 0x01 };

class NetFefStructBuilder {
  public:
    NetFefStructBuilder(byte* buffer, unsigned int buffSize);
    NetFefStructBuilder();
    void init(byte* buffer, unsigned int buffSize);
    void reset();
    boolean addParameter(char parameterName, char parameterType, char* value);
    boolean addParameter(char parameterName, char parameterType, unsigned int value);
    boolean addParameter(char parameterName, char parameterType, int value);
    boolean addParameter(char parameterName, char parameterType, unsigned long value);
    boolean addParameter(char parameterName, char parameterType, long value);
    boolean addParameter(char parameterName, NetFefStructBuilder* structBuilder);
    byte* getBytes();
    unsigned int getLength();
    Print* _debug = NULL;
    
  protected:
    byte* _bytes;
    unsigned int _buffSize;
    unsigned int _pos;
    unsigned int _paramCountPos;
    boolean _addByte(byte value);
    boolean _addInt2(unsigned int value);
    boolean _addInt4(unsigned long value);
};

class NetFefFrameBuilder : public NetFefStructBuilder {
  public:
    NetFefFrameBuilder(byte* buffer, unsigned int buffLen);
    NetFefFrameBuilder();
    void reset(const byte* myAddress, const byte* targetAddress, char subject, char command);
    byte* getBytes();
    unsigned int getLength();
    
  private:
};

class NetFefStructReader {
  public:
    NetFefStructReader(unsigned int buffSize);
    NetFefStructReader();
    void init(unsigned int buffSize);
    void reset(byte* buffer, char type);
    byte* getParameter(char parameterName);
    byte* getParameter(char parameterName, byte* previous);
    byte* getFirstParameter();
    byte* getNextParameter(byte* previous);

    Print* _debug = NULL;
    static unsigned int getInt(byte* position);
    unsigned int length;
    
  protected:
    byte* _bytes;
    unsigned int _paramCount;
    unsigned int _paramsPos;
    unsigned int _buffSize;
};

class NetFefFrameReader : public NetFefStructReader {
  public:
    NetFefFrameReader(unsigned int buffSize);
    NetFefFrameReader();
    void reset(byte* buffer);
    boolean isForMe(const byte* myAddress);
    boolean isValid();
    byte* getSenderAddress();
    byte* getSubject();
    byte* getCommand();
    boolean isSubjectAndCommand(char subject, char command);
    boolean isSubject(char subject);
    boolean isCommand(char command);
    
    byte targetAddressLength;
    byte sourceAddressLength;
};

class NetFefParameter {
  public:
    NetFefParameter();
    void reset(byte* parameterPointer);
    int calculateSpace(); // -- It might be a good idea to use unsigned int here, however it is not likely to handle huge values.
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
    NetFefStructReader* getStructValue(NetFefStructReader* structReader);
    Print* _debug = NULL;
    
  private:
    byte* _pp;
};

#endif // -- NetFefData_H
