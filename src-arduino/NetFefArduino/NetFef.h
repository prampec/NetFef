#ifndef NETFEF_H
#define NETFEF_H

#include <Arduino.h>

const byte BROADCAST_ADDRESS[2] = { 0x00, 0x00 };
const byte MASTER_ADDRESS[2] = { 0x00, 0x01 };

class NetFefFrameBuilder {
  public:
    NetFefFrameBuilder(byte* buffer, const byte* myAddress, const byte* targetAddress, char command);
    void addParameter(char parameterName, char parameterType, char* value);
    byte* getFrameBytes();
    int getFrameLength();
    
  private:
    byte* _bytes;
    int _pos;
    int _paramCountPos;
    void _addByte(byte value);
    void _addInt2(int value);
};

class NetFefFrameReader {
  public:
    NetFefFrameReader(byte* frame);
    boolean isForMe(const byte* myAddress);
    byte* getSenderAddress();
    byte* getCommand();
    byte* getParameter(char parameterName);

    byte targetAddressLength;
    byte sourceAddressLength;
    Print* _debug = NULL;
    
  private:
    byte* _bytes;
    int _paramCount;
    int _paramsPos;
};

class NetFefParameter {
  public:
    NetFefParameter(byte* parameterPointer);
    int calculateSpace();
    boolean isType(char parameterType);
    byte getValue1();
    
  private:
    byte* _pp;
};

#endif // -- NETFEF_H
