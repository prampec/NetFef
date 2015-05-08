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
#include <SoftTimer.h>

#include "NetFefRs485.h"

NetFefRs485::NetFefRs485(HardwareSerial* serial, int writeEnabledPin) :
  Task(0, &(NetFefRs485::step))
{
  this->_writeEnabledPin = writeEnabledPin;
  this->_serial = serial;

  pinMode(this->_writeEnabledPin, OUTPUT);
  digitalWrite(this->_writeEnabledPin, LOW);
  pinMode(13, OUTPUT);
  SoftTimer.add(this);
}

void NetFefRs485::begin() {
  this->_serial->begin(9600);
}

boolean NetFefRs485::_writeFrameCheckCollision(byte* data, int length) {
  if(this->_serial->available()) {
    // -- data on the UART first must be read
    this->_debug("data on uart");
    return false;
  }
  digitalWrite(this->_writeEnabledPin, HIGH);
  boolean sendSuccess = true;
  for(int i=0;i<length;i++) {
    byte val = data[i];
    this->_serial->write(val);
    delay(2);
    if(!this->_serial->available()) {
      // -- can not read echo: communication error
      this->_debug("echo not found");
      sendSuccess = false;
      break;
    }
    byte b = this->_serial->read();
    if(b != val) {
      // -- collision detected
      this->_debug("echo missmatch");
      sendSuccess = false;
      break;
    }
  }
  digitalWrite(this->_writeEnabledPin, LOW);
  if(sendSuccess && this->_serial->available()) {
      // -- collision detected
      this->_debug("echo has tail");
      sendSuccess = false;
  }
  return sendSuccess;
}


void NetFefRs485::step(Task* task) {
  NetFefRs485* me = (NetFefRs485*)task;
  if(me->_queueSize == 0) {
    // -- Wait for data on queue.
    me->setPeriodMs(0);
    return;
  }
  
  if(!me->timePassed(millis(), me->_lastAction, MINIMAL_FRAME_SPACING_MS)) {
    me->setPeriodMs( MINIMAL_FRAME_SPACING_MS );
    return;
  }
 
 // -- Data on queue
//    debug("writing data ");
//    lcd.print(me->_queueSize);

    byte* data = me->_commQueue[0];
    int length = me->_frameSizes[0];
    int collisionPenalty = 0;
    if(me->_writeFrameCheckCollision(data, length)) {
      // -- data sent
      for(int i=1; i<me->_queueSize; i++) {
        // -- shift the queue
        NetFefRs485::copyDataValues(me->_commQueue[i], me->_commQueue[i-1]);
        me->_frameSizes[i-1] = me->_frameSizes[i];
      }
      me->_queueSize -= 1;
    } else {
      // -- collision or other problem detected
      collisionPenalty = random(COLLISION_PENALTY_MAX_MS);
    }
    me->_lastAction = millis();
    // -- Wait after write or error.
    me->setPeriodMs( MINIMAL_FRAME_SPACING_MS + collisionPenalty );
digitalWrite(13, LOW);
}

void NetFefRs485::copyDataValues(byte* from, byte* to) {
  for(int i=0; i<COMM_DATA_FRAME_LENGTH;i++) {
    to[i] = from[i];
  }
}

void NetFefRs485::addDataToQueue(NetFefFrameBuilder* frameBuilder) {
    this->_addDataToQueue(frameBuilder->getFrameBytes(), frameBuilder->getFrameLength());
}
void NetFefRs485::_addDataToQueue(byte* data, int length) {
  if(this->_queueSize >= COMM_QUEUE_LENGTH) {
    // -- buffer overflow
    // -- TODO: very big error here
//    error.start(4);
    return;
  }
digitalWrite(13, HIGH);
  _frameSizes[this->_queueSize] = length;
  NetFefRs485::copyDataValues(data, this->_commQueue[this->_queueSize]);
  this->_queueSize += 1;
}

boolean NetFefRs485::dataAvailable() {
  return this->_serial->available();
}

byte* NetFefRs485::readFrame() {
  int i = 0;
  while(this->_serial->available()) {
    int val = this->_serial->read();
    if(i<COMM_DATA_FRAME_LENGTH) {
      this->_readBuffer[i++] = val;
    }
    int j = 0;
    while(!this->_serial->available() && j<10) {
      delay(1);
      j += 1;
    }
  }
  this->_lastAction = millis();
  return this->_readBuffer;
}

boolean NetFefRs485::canSend() {
  return (this->_queueSize < COMM_QUEUE_LENGTH) && !this->_serial->available();
}


void NetFefRs485::setDebug(Print* print) {
  this->_debugOut = print;
}
void NetFefRs485::_debug(char* message) {
}

boolean NetFefRs485::timePassed(unsigned long now, unsigned long start, unsigned long diff) {
  unsigned long calc = start + diff;
  return ((now >= calc) && (
      (calc >= start) // -- Nothing was overflown.
      || (start > now) // -- Both timer and interval-end overflows
      ))
    || ((now < start) && (start <= calc)); // -- timer overflows, but interval-end does not
}
