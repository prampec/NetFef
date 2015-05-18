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

#include "NetFefData.h"

// ============================= ////////////////////////////////// ==================================

NetFefStructBuilder::NetFefStructBuilder(byte* buffer, unsigned int buffSize) {
  this->init(buffer, buffSize);
}
NetFefFrameBuilder::NetFefFrameBuilder(byte* buffer, unsigned int buffSize) : NetFefStructBuilder(buffer, buffSize) {
}

NetFefStructBuilder::NetFefStructBuilder() {
}
NetFefFrameBuilder::NetFefFrameBuilder() : NetFefStructBuilder() {
}

void NetFefStructBuilder::init(byte* buffer, unsigned int buffSize) {
  this->_bytes = buffer;
  this->_buffSize = buffSize;
}

void NetFefStructBuilder::reset() {
  this->_pos = 2; // -- Leave the first two bytes for the length.

  this->_paramCountPos = this->_pos;
  this->_bytes[this->_paramCountPos] = 0;
  this->_pos += 1;
}
void NetFefFrameBuilder::reset(const byte* myAddress, const byte* targetAddress, char subject, char command) {
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
  
  this->setSubject(subject);
  this->setCommand(command);
}

void NetFefFrameBuilder::setSubject(char subject) {
  this->addParameter('s', 'c', &subject);
}
void NetFefFrameBuilder::setCommand(char command) {
  this->addParameter('c', 'c', &command);
}

byte* NetFefStructBuilder::getBytes() {
  
  // -- Write size to the topmost position
  int size = this->_pos;
  this->_pos = 0;
  this->_addInt2(size-2);
  this->_pos = size; // -- Reset cursor position to the end.
  
  return this->_bytes;
}
byte* NetFefFrameBuilder::getBytes() {
  
  // -- Write frame size to the topmost position
  int size = this->_pos;
  this->_pos = 0;
  this->_addInt2(size+1);
  this->_pos = size; // -- Reset cursor position to the end.
  
  // -- Calculate sum
  if(this->hasSpace()) {
    byte sum = 0;
    for(int i = 0; i<this->_pos; i++) {
      sum += this->_bytes[i];
    }
    this->_bytes[this->_pos] = sum;
  }
  
  return this->_bytes;
}

unsigned int NetFefStructBuilder::getLength() {
  return this->_pos;
}
unsigned int NetFefFrameBuilder::getLength() {
  return this->_pos+1;
}

boolean NetFefStructBuilder::hasSpace() {
  return this->_pos < this->_buffSize;
}
boolean NetFefFrameBuilder::hasSpace() {
  return (this->_pos+1) < this->_buffSize;
}

boolean NetFefStructBuilder::addParameter(char parameterName, char parameterType, char* value) {
  unsigned int posSave = this->_pos;
  boolean success = this->_addByte(parameterName) && this->_addByte(parameterType);
  if(success) {
    if(
      (parameterType == 'c')
      || (parameterType == 'B')
      || (parameterType == 'b')
      ) {
      success = this->_addByte(*value);
    }
    else if(parameterType == 's') {
      int len = strlen(value) + 1;
      success = this->_addByte(len);
      if(success && (this->_pos+len) < this->_buffSize) {
        strcpy((char*)this->_bytes+this->_pos, value);
        this->_pos += len;
      } else {
        success = false;
      }
    }
    else if(parameterType == 'S') {
      int len = strlen(value) + 1;
      success = this->_addInt2(len);
      if(success && (this->_pos+len) < this->_buffSize) {
        strcpy((char*)this->_bytes+this->_pos, value);
        this->_pos += len;
      } else {
        success = false;
      }
    }
  }

  success = success && this->hasSpace();
  if(success) {
    this->_bytes[this->_paramCountPos] += 1;
  } else {
    this->_pos = posSave;
  }

  return success;
}

boolean NetFefStructBuilder::addParameter(char parameterName, char parameterType, unsigned int value) {
  unsigned int posSave = this->_pos;
  boolean success = this->_addByte(parameterName) &&  this->_addByte(parameterType);
  if(success) {
    if((parameterType == 'b') || (parameterType == 'B')) {
      success = this->_addByte(value);
    }
    else if((parameterType == 'i') || (parameterType == 'I')) {
      success = this->_addInt2(value);
    }
  }

  success = success && this->hasSpace();
  if(success) {
    this->_bytes[this->_paramCountPos] += 1;
  } else {
    this->_pos = posSave;
  }

  return success;
}

boolean NetFefStructBuilder::addParameter(char parameterName, char parameterType, int value) {
  return this->addParameter(parameterName, parameterType, (unsigned int)value);
}

boolean NetFefStructBuilder::addParameter(char parameterName, char parameterType, unsigned long value) {
  unsigned int posSave = this->_pos;
  boolean success = this->_addByte(parameterName) &&  this->_addByte(parameterType);
  if(success) {
    if((parameterType == 'l') || (parameterType == 'L')) {
      success = this->_addInt4(value);
    }
  }

  success = success && this->hasSpace();
  if(success) {
    this->_bytes[this->_paramCountPos] += 1;
  } else {
    this->_pos = posSave;
  }

  return success;
}

boolean NetFefStructBuilder::addParameter(char parameterName, char parameterType, long value) {
  return this->addParameter(parameterName, 'L', (unsigned long)value);
}

boolean NetFefStructBuilder::addParameter(char parameterName, NetFefStructBuilder* structBuilder) {
  unsigned int posSave = this->_pos;
  boolean success = this->_addByte(parameterName) &&  this->_addByte('T');
  if(success) {
    byte* bytes = structBuilder->getBytes();
    unsigned int toGo = structBuilder->getLength();
    int i = 0;
    while(success && (i < toGo)) {
      success = this->_addByte(*(bytes+i));
      i += 1;
    }
  }

  success = success && this->hasSpace();
  if(success) {
    this->_bytes[this->_paramCountPos] += 1;
  } else {
    this->_pos = posSave;
  }

  return success;
}

boolean NetFefStructBuilder::_addByte(byte value) {
  if((this->_pos+1) >= this->_buffSize) {
    return false;
  }
  this->_bytes[this->_pos++] = value;
  return true;
}

boolean NetFefStructBuilder::_addInt2(unsigned int value) {
  if((this->_pos+2) >= this->_buffSize) {
    return false;
  }
  this->_bytes[this->_pos++] = value >> 8;
  this->_bytes[this->_pos++] = value;
  return true;
}

boolean NetFefStructBuilder::_addInt4(unsigned long value) {
  if((this->_pos+4) >= this->_buffSize) {
    return false;
  }
  this->_bytes[this->_pos++] = value >> 24;
  this->_bytes[this->_pos++] = value >> 16;
  this->_bytes[this->_pos++] = value >> 8;
  this->_bytes[this->_pos++] = value;
  return true;
}





// ============================= ////////////////////////////////// ==================================

NetFefStructReader::NetFefStructReader(unsigned int buffSize) {
  this->init(buffSize);
}
NetFefFrameReader::NetFefFrameReader(unsigned int buffSize) : NetFefStructReader(buffSize) {
}
NetFefStructReader::NetFefStructReader() {
}
NetFefFrameReader::NetFefFrameReader() : NetFefStructReader() {
}
void NetFefStructReader::init(unsigned int buffSize) {
  this->_buffSize = buffSize;
}

void NetFefStructReader::reset(byte* buffer, char type) {
  this->_bytes = buffer;
  
if(this->_debug != NULL) this->_debug->print("%");
  if(type == 't') {
    this->length = *(this->_bytes);
    this->_paramCount = this->_bytes[1];
    this->_paramsPos = 2;
  } else { // -- type == 'T'
    this->length = this->getInt(this->_bytes);
    this->_paramCount = this->_bytes[2];
    this->_paramsPos = 3;
  }
}
void NetFefFrameReader::reset(byte* buffer) {
  NetFefStructReader::reset(buffer, 'T');
if(this->_debug != NULL) this->_debug->print(this->length);
  this->targetAddressLength = this->_bytes[2];
  this->sourceAddressLength = this->_bytes[3 + this->targetAddressLength];
  this->_paramCount = this->_bytes[4 + this->targetAddressLength + this->sourceAddressLength];
  this->_paramsPos = 5 + this->targetAddressLength + this->sourceAddressLength;
if(this->_debug != NULL) this->_debug->print(this->_paramsPos);
if(this->_debug != NULL) this->_debug->print((char)buffer[this->_paramsPos]);
}

boolean NetFefFrameReader::isForMe(const byte* myAddress) {
  if(!this->isValid()) {
    return false; // -- Cannot handle bigger frames than allocated buffers.
  }
  int broadCast = memcmp(this->_bytes+3, BROADCAST_ADDRESS, 2);
  return 0 == broadCast || ((this->targetAddressLength == 2) && (0 == memcmp(this->_bytes+3, myAddress, 2)));
}

boolean NetFefFrameReader::isValid() {
  if(!this->_buffSize > this->length) {
    return false; // -- Cannot handle bigger frames than allocated buffers.
  }
  // -- Check sum
  byte sum = 0;
  for(int i=0; i<this->length-1; i++) {
    sum += this->_bytes[i];
  }
  return sum == this->_bytes[this->length-1];
}

byte* NetFefFrameReader::getSenderAddress() {
  return (this->_bytes + (4 + this->targetAddressLength));
}

byte* NetFefStructReader::getParameter(char parameterName) {
  int pos = this->_paramsPos;
if(this->_debug != NULL) this->_debug->print(this->_paramCount);
  for(int i = 0; i<this->_paramCount; i++) {
if(this->_debug != NULL) this->_debug->print((char)this->_bytes[pos]);
    if(this->_bytes[pos] == parameterName) {
      return this->_bytes + pos;
    } else {
      this->_parameter.reset(this->_bytes + pos);
      int paramSpace = this->_parameter.calculateSpace();
      if(paramSpace == -1) {
        // -- Unsupported parameter type in frame, we cannot continue.
        return 0;
      }
      pos += paramSpace;
if(this->_debug != NULL) this->_debug->print(paramSpace);
    }
  }
  return 0;
}

byte* NetFefStructReader::getParameter(char parameterName, byte* previous) {
  int pos = this->_paramsPos;
  int previousFound = false;
if(this->_debug != NULL) this->_debug->print(this->_paramCount);
  for(int i = 0; i<this->_paramCount; i++) {
if(this->_debug != NULL) this->_debug->print((char)this->_bytes[pos]);
    if(previous == (this->_bytes + pos)) {
      previousFound = true;
    } else if(previousFound && (this->_bytes[pos] == parameterName)) {
      return this->_bytes + pos;
    }
    this->_parameter.reset(this->_bytes + pos);
    int paramSpace = this->_parameter.calculateSpace();
    if(paramSpace == -1) {
      // -- Unsupported parameter type in frame, we cannot continue.
      return 0;
    }
    pos += paramSpace;
if(this->_debug != NULL) this->_debug->print(paramSpace);
  }
  return 0;
}

byte* NetFefStructReader::getFirstParameter() {
  int pos = this->_paramsPos;
  return this->_bytes + pos;
}
byte* NetFefStructReader::getNextParameter(byte* previous) {
  int pos = this->_paramsPos;
if(this->_debug != NULL) this->_debug->print(this->_paramCount);
  for(int i = 0; i<this->_paramCount-1; i++) {
    this->_parameter.reset(this->_bytes + pos);
    int paramSpace = this->_parameter.calculateSpace();
if(this->_debug != NULL) { this->_debug->print('-'); this->_debug->print(paramSpace); }
    if(paramSpace == -1) {
      // -- Unsupported parameter type in frame, we cannot continue.
      return 0;
    }
    if(previous == (this->_bytes + pos)) {
      return this->_bytes + pos + paramSpace;
    }
    pos += paramSpace;
  }
  return 0;
}

byte* NetFefFrameReader::getCommand() {
  return getParameter('c');
}

byte* NetFefFrameReader::getSubject() {
  return getParameter('s');
}

unsigned int NetFefStructReader::getInt(byte* position) {
  return (unsigned int)position[0] << 8 | (unsigned int)position[1];
}

boolean NetFefFrameReader::isSubjectAndCommand(char subject, char command) {
  return this->isSubject(subject) && this->isCommand(command);
}
boolean NetFefFrameReader::isSubject(char subject) {
  this->_parameter.reset(this->getSubject());
  return this->_parameter.isType('c') && (this->_parameter.getCharValue() == subject);
}
boolean NetFefFrameReader::isCommand(char command) {
  this->_parameter.reset(this->getCommand());
  return this->_parameter.isType('c') && (this->_parameter.getCharValue() == command);
}




// ============================= ////////////////////////////////// ==================================

NetFefParameter::NetFefParameter() {
}
void NetFefParameter::reset(byte* parameterPointer) {
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
  else if(this->isType('i') || this->isType('I')) {
    return 4;
  }
  else if(this->isType('l') || this->isType('L')) {
    return 6;
  }
  else if(this->isType('s')) {
    return 3 + this->_pp[2];
  }
  else if(this->isType('S')) {
    return 4 + NetFefStructReader::getInt(this->_pp+2);
  }
  else if(this->isType('t')) {
    return 3 + this->_pp[2];
  }
  else if(this->isType('T')) {
    return 4 + NetFefStructReader::getInt(this->_pp+2);
  }

  return -1; // -- Unsupported type
}
char NetFefParameter::getName() {
  return this->_pp[0];
}
char NetFefParameter::getType() {
  return this->_pp[1];
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
int NetFefParameter::getSignedIntValue() {
  if(this->isType('I')) {
    uint16_t ui = (uint16_t)this->_pp[2] << 8 | (uint16_t)this->_pp[3];
    int16_t i = ui; // -- On 32 bit modells we cannot be sure that int is 16bits long.
    return i;
  }
  return 0; // -- Unsupported type
}
unsigned int NetFefParameter::getIntValue() {
  if(this->isType('i')) {
if(this->_debug != NULL) { this->_debug->print(this->_pp[2],HEX); this->_debug->print("-"); this->_debug->print(this->_pp[3],HEX); }
    return NetFefStructReader::getInt(this->_pp+2);
  }
  return 0; // -- Unsupported type
}
long NetFefParameter::getLongValue() {
  if(this->isType('l') || this->isType('L')) {
    return (long)this->_pp[2] << 24 | (long)this->_pp[3]<<16 | (long)this->_pp[4]<<8 | (long)this->_pp[5];
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
NetFefStructReader* NetFefParameter::getStructValue(NetFefStructReader* structReader) {
  byte* valueStart;
  /*
  if(this->isType('t')) {
    valueStart = this->_pp + 2;
  }
  else if(this->isType('T')) {
    valueStart = this->_pp + 2;
  } else {
    return 0; // -- Unsupported type
  }
  */
  structReader->reset(this->_pp + 2, this->getType());
  return structReader;
}


