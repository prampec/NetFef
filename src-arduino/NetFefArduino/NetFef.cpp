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

#include "NetFef.h"

// ============================= ////////////////////////////////// ==================================

NetFefFrameBuilder::NetFefFrameBuilder(byte* buffer, const byte* myAddress, const byte* targetAddress, char command) {
  this->_bytes = buffer;
  this->_pos = 2; // -- Leave the first two bytes for the frame length.

  this->_addByte(2);
  this->_addByte(targetAddress[0]);
  this->_addByte(targetAddress[1]);
  
  this->_addByte(2);
  this->_addByte(myAddress[0]);
  this->_addByte(myAddress[1]);
  
  this->_paramCountPos = this->_pos;
  this->_bytes[this->_paramCountPos] = 0;
  this->_pos += 1;
  
  this->addParameter('c', 'c', &command);
}

byte* NetFefFrameBuilder::getFrameBytes() {
  
  // -- Write frame size to the topmost position
  int frameSize = this->_pos;
  this->_pos = 0;
  this->_addInt2(frameSize);
  this->_pos = frameSize; // -- Reset cursor position to the end.
  
  return this->_bytes;
}

int NetFefFrameBuilder::getFrameLength() {
  return this->_pos;
}

void NetFefFrameBuilder::addParameter(char parameterName, char parameterType, char* value) {
  this->_addByte(parameterName);
  this->_addByte(parameterType);
  if(
    (parameterType == 'c')
    || (parameterType == 'B')
    || (parameterType == 'b')
    ) {
    this->_addByte(*value);
  }
  else if(parameterType == 's') {
    int len = strlen(value) + 1;
    this->_addByte(len);
  // -- TODO: may add some check to prevent buffer overflow
    strcpy((char*)this->_bytes+this->_pos, value);
    this->_pos += len;
  }
  else if(parameterType == 'S') {
    int len = strlen(value) + 1;
    this->_addInt2(len);
    // -- TODO: may add some check to prevent buffer overflow
    strcpy((char*)this->_bytes+this->_pos, value);
    this->_pos += len;
  }
  
  this->_bytes[this->_paramCountPos] += 1;
}

void NetFefFrameBuilder::addParameter(char parameterName, char parameterType, unsigned int value) {
  this->_addByte(parameterName);
  this->_addByte(parameterType);
  if(parameterType == 'b') {
    this->_addByte(value);
  }
  else if(parameterType == 'i') {
    this->_addInt2(value);
  }
}

void NetFefFrameBuilder::_addByte(byte value) {
  // -- TODO: may add some check to prevent buffer overflow
  this->_bytes[this->_pos++] = value;
}

void NetFefFrameBuilder::_addInt2(unsigned int value) {
  int upper = value / 255;
  // -- TODO: may add some check to prevent buffer overflow
  this->_bytes[this->_pos++] = upper;
  this->_bytes[this->_pos++] = value - upper;
}





// ============================= ////////////////////////////////// ==================================

NetFefFrameReader::NetFefFrameReader(byte* frame) {
  this->_bytes = frame;
  
  this->targetAddressLength = this->_bytes[2];
  this->sourceAddressLength = this->_bytes[3 + this->targetAddressLength];
  this->_paramCount = this->_bytes[4 + this->targetAddressLength + this->sourceAddressLength];
  this->_paramsPos = 5 + this->targetAddressLength + this->sourceAddressLength;
}

boolean NetFefFrameReader::isForMe(const byte* myAddress) {
  int broadCast = memcmp(this->_bytes+3, BROADCAST_ADDRESS, 2);
  return 0 == broadCast || ((this->targetAddressLength == 2) && (0 == memcmp(this->_bytes+3, myAddress, 2)));
}

byte* NetFefFrameReader::getSenderAddress() {
  return (this->_bytes + (4 + this->targetAddressLength));
}

byte* NetFefFrameReader::getParameter(char parameterName) {
  int pos = this->_paramsPos;
if(this->_debug != NULL) this->_debug->print(this->_paramCount);
  for(int i = 0; i<this->_paramCount; i++) {
    if(this->_bytes[pos] == parameterName) {
      return this->_bytes + pos;
    } else {
      NetFefParameter parameter = NetFefParameter(this->_bytes + pos);
      int paramSpace = parameter.calculateSpace();
      if(paramSpace == -1) {
        // -- Unsupported parameter type in frame, we cannot continue.
        return 0;
      }
      pos += paramSpace;
    }
  }
  return 0;
}

byte* NetFefFrameReader::getCommand() {
  return getParameter('c');
}






// ============================= ////////////////////////////////// ==================================

NetFefParameter::NetFefParameter(byte* parameterPointer) {
  this->_pp = parameterPointer;
}

int NetFefParameter::calculateSpace() {
  if(
    this->isType('c')
    || this->isType('B')
    || this->isType('b')
  ) {
    return 3;
  }
  else if(this->isType('i')) {
    return 4;
  }
  else if(this->isType('s')) {
    return 2 + this->_pp[2];
  }
  else if(this->isType('s')) {
    return 2 + this->_pp[2]*255 + this->_pp[3];
  }

  return -1; // -- Unsupported type
}

boolean NetFefParameter::isType(char parameterType) {
  return this->_pp[1] == parameterType;
}
boolean NetFefParameter::getBooleanValue() {
  if(this->isType('B')) {
    return this->_pp[2];
  }
  return 0; // -- Unsupported type
}
byte NetFefParameter::getByteValue() {
  if(this->isType('b')) {
    return this->_pp[2];
  }
  return 0; // -- Unsupported type
}
unsigned int NetFefParameter::getIntValue() {
  if(this->isType('i')) {
    return this->_pp[2]*255 + this->_pp[3];
  }
  return 0; // -- Unsupported type
}
char NetFefParameter::getCharValue() {
  if(this->isType('c')) {
    return this->_pp[2];
  }
  return 0; // -- Unsupported type
}
char* NetFefParameter::getStringValue() {
  if(this->isType('s')) {
    return (char*)this->_pp + 3;
  }
  else if(this->isType('S')) {
    return (char*)this->_pp + 4;
  }
  return 0; // -- Unsupported type
}


