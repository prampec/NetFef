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

#include "NetFefObsidian.h"

NetFefObsidian::NetFefObsidian(INetFefPhysicalLayer* physicalLayer, NetFefFrameBuilder* (*onFrameReceived)(NetFefFrameReader* frameReader, NetFefFrameBuilder* frameBuilder),
                                RegistrationInfo* (*loadRegistrationInfo)(), void (*saveRegistrationInfo)(RegistrationInfo* registrationInfo)
                                ) :
  Task(10, &(NetFefObsidian::step)) {
  this->_physicalLayer = physicalLayer;
  this->_frameReceivedListener = onFrameReceived;
  this->_loadRegistrationInfo = loadRegistrationInfo;
  this->_saveRegistrationInfo = saveRegistrationInfo;
  this->_joined = false;

  NetFefFrameBuilder fb = NetFefFrameBuilder(this->_outDataBuffer, this->_commDataFrameLength);
  this->_frameBuilder = &fb;
  NetFefFrameReader fr = NetFefFrameReader(this->_commDataFrameLength);
  this->_frameReader = &fr;

}

void NetFefObsidian::begin() {
  randomSeed(analogRead(0)); // -- TODO: use some external random generator
  this->_physicalLayer->begin();
  this->_registrationInfo = this->_loadRegistrationInfo();
  if((this->_registrationInfo->myAddress[0] == 255) && (this->_registrationInfo->myAddress[1] = 255)) {
    this->_generateAddress();
  }
}

// -- static
boolean NetFefObsidian::sendFrame(NetFefFrameBuilder* frameBuilder) {
  if(!this->_physicalLayer->canSend()) {
    return false;
  }
  this->_physicalLayer->addDataToQueue(frameBuilder);
  return true;
}

void NetFefObsidian::step(Task* task) {
  NetFefObsidian* me = (NetFefObsidian*)task;
  unsigned long now = millis();
  if(me->_joined) {
    if(NetFefObsidian::timePassed(now, me->_lastPollTime, JOIN_DROP_TIMEOUT)) {
      // -- Network join lost
      me->_joined = false;
      me->_joinStart = 0;
    }
  }
  
  if(!me->_joined && (me->_joinStart > 0) && NetFefObsidian::timePassed(now, me->_joinStart, me->_joinDelay)) {
    // -- Send join request
    me->_joinStart = 0;
    
    me->_frameBuilder->reset(me->_registrationInfo->myAddress, MASTER_ADDRESS, 'n', 'j');
  
    me->_frameBuilder->addParameter('i', 'l', me->_registrationInfo->registrationId);
    if(me->_physicalLayer->canSend()) {
      me->_physicalLayer->addDataToQueue(me->_frameBuilder);
    }
  }
  
  if(me->_physicalLayer->dataAvailable()) {
    byte* data = me->_physicalLayer->readFrame();
    NetFefFrameReader frameReader = *(me->_frameReader);
    frameReader.reset(data);
    if(frameReader.isForMe(me->_registrationInfo->myAddress)) {
      NetFefParameter parameter = me->_parameter;
      parameter.reset(frameReader.getSubject());
      if(parameter.isType('c') && (parameter.getCharValue() == 'n')) {
        // -- Network management frame
        parameter.reset(frameReader.getCommand());
        if(!me->_joined && parameter.isType('c') && (parameter.getCharValue() == 'j')) {
          // -- Join start
          parameter.reset(frameReader.getParameter('n'));
          unsigned long networkId = parameter.getLongValue();
          if(me->_registrationInfo->networkId != networkId) {
            me->_registrationInfo->networkId = networkId;
            me->_generateAddress();
          }
          parameter.reset(frameReader.getParameter('w'));
          me->_joinStart = now;
          me->_joinDelay = random(parameter.getIntValue());
        }
        else if(!me->_joined && parameter.isType('c') && (parameter.getCharValue() == 'J')) {
          // -- Join request from master
          parameter.reset(frameReader.getParameter('i'));
          if(parameter.getIntValue() == me->_registrationInfo->registrationId) {
            parameter.reset(frameReader.getParameter('d'));
            if(parameter.getCharValue() == 'a') {
              me->_joined = true;
              NetFefFrameBuilder* frameBuilder = me->prepareReply(&frameReader, 'n', 'J');
              frameBuilder->addParameter('d', 's', "test device"); // -- TODO: load this from code
              frameBuilder->addParameter('v', 's', "v0/0/0"); // -- TODO: load this from code
              frameBuilder->addParameter('n', 'i', (unsigned int)30); // -- TODO: load this from code
              if(me->_physicalLayer->canSend()) {
                me->_physicalLayer->addDataToQueue(frameBuilder);
              }
              me->_lastPollTime = now;
            } else {
              // -- Declined this address (and try again next time)
              me->_generateAddress();
            }
          }
        }
        else if(!me->_joined && parameter.isType('c') && (parameter.getCharValue() == 'p')) {
          // -- Notify device about ping frame
          me->_lastPollTime = now;
          NetFefFrameBuilder* frameBuilder = me->_frameReceivedListener(&frameReader, me->prepareReply(&frameReader, 'n', 'p'));
          frameBuilder->addParameter('n', 'i', (unsigned int)30); // -- TODO: load this from code
          if(me->_physicalLayer->canSend()) {
            me->_physicalLayer->addDataToQueue(frameBuilder);
          }
        }
      } else {
        // -- Notify device about frame
        NetFefFrameBuilder* frameBuilder = me->_frameReceivedListener(&frameReader, me->prepareReply(&frameReader, 'n', 'p'));
        if(frameBuilder != 0) {
          if(me->_physicalLayer->canSend()) {
            me->_physicalLayer->addDataToQueue(frameBuilder);
          }
        }
      }
    }
  }
}

boolean NetFefObsidian::timePassed(unsigned long now, unsigned long start, unsigned long diff) {
  unsigned long calc = start + diff;
  return ((now >= calc) && (
      (calc >= start) // -- Nothing was overflown.
      || (start > now) // -- Both timer and interval-end overflows
      ))
    || ((now < start) && (start <= calc)); // -- timer overflows, but interval-end does not
}

RegistrationInfo* NetFefObsidian::_generateAddress() {
  this->_registrationInfo->myAddress[0] = random(1,256);
  this->_registrationInfo->myAddress[1] = random(2,256);
  this->_registrationInfo->registrationId = random();
  return this->_registrationInfo;
}

NetFefFrameBuilder* NetFefObsidian::prepareReply(NetFefFrameReader* frameReader, char subject, char command) {
  this->_frameBuilder->reset(this->_registrationInfo->myAddress, MASTER_ADDRESS, subject, command);
  byte* resetParameterPointer = frameReader->getParameter('r');
  if(resetParameterPointer != 0) {
    this->_parameter.reset(resetParameterPointer);
    this->_frameBuilder->addParameter('R', 'c', this->_parameter.getIntValue());
  }

  return this->_frameBuilder;
}
