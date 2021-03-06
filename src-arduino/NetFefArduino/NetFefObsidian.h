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
#ifndef NetFefObsidian_H
#define NetFefObsidian_H

#include <Arduino.h>
#include <SoftTimer.h>
#include "INetFefNetwork.h"

#ifndef COMM_DATA_FRAME_LENGTH
  #define COMM_DATA_FRAME_LENGTH 80
#endif // -- COMM_DATA_FRAME_LENGTH

#define NEXT_POLL_MAX_SECS 5*60
#define JOIN_DROP_TIMEOUT 2L*NEXT_POLL_MAX_SECS

class NetFefObsidian : public Task, public INetFefNetwork
{
  public:
    NetFefObsidian(INetFefPhysicalLayer* physicalLayer, NetFefFrameBuilder* (*onFrameReceived)(NetFefFrameReader* frameReader, NetFefFrameBuilder* frameBuilder),
      RegistrationInfo* (*loadRegistrationInfo)(), void (*saveRegistrationInfo)(RegistrationInfo* registrationInfo),
      char* deviceId, unsigned int pollMeInterval, unsigned long randomSeedValue);
    virtual void begin();
    virtual boolean sendFrame(NetFefFrameBuilder* frameBuilder);
    NetFefFrameBuilder* prepareReply(NetFefFrameReader* frameReader);
    boolean joinedToNetwork;
    Print* _debug = NULL;
    virtual char* getVersion() { return "0"; };
    NetFefFrameBuilder* getFrameBuilder(char subject, char command);

  private:
    static void step(Task* me);
    INetFefPhysicalLayer* _physicalLayer;
    NetFefFrameBuilder* (*_frameReceivedListener)(NetFefFrameReader* frameReader, NetFefFrameBuilder* frameBuilder);
    RegistrationInfo* (*_loadRegistrationInfo)();
    void (*_saveRegistrationInfo)(RegistrationInfo* registrationInfo);
    RegistrationInfo *_registrationInfo;
    unsigned int _commDataFrameLength = COMM_DATA_FRAME_LENGTH;
    unsigned long _lastPollTime;
    static boolean timePassed(unsigned long now, unsigned long start, unsigned long diff) ;
    unsigned long _joinStart;
    unsigned int _joinDelay;
    RegistrationInfo* _generateAddress();
    byte _outDataBuffer[COMM_DATA_FRAME_LENGTH]; // -- TODO: should me->_commDataFrameLength, but want to avoid dynamic buffer allocation.
    NetFefFrameBuilder _frameBuilder = NetFefFrameBuilder();
    NetFefFrameReader _frameReader = NetFefFrameReader();
    NetFefParameter _parameter = NetFefParameter();
    char* _deviceId;
    unsigned int _pollMeInterval;
    char _versionString[10];
};

#endif // -- NetFefObsidian_H
