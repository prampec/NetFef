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
#ifndef NetFefRs485_H
#define NetFefRs485_H

#include <Arduino.h>
#include <Task.h>

#include "NetFefData.h"
#include "INetFefNetwork.h"

#define MINIMAL_FRAME_SPACING_MS 200
#define COLLISION_PENALTY_MAX_MS 400
#define READ_TIMEOUT_MS 10

#ifndef COMM_QUEUE_LENGTH
  #define COMM_QUEUE_LENGTH 5
#endif // -- COMM_QUEUE_LENGTH
#ifndef COMM_DATA_FRAME_LENGTH
  #define COMM_DATA_FRAME_LENGTH 60
#endif // -- COMM_DATA_FRAME_LENGTH


class NetFefRs485 : public Task, public INetFefPhysicalLayer
{
  public:
    NetFefRs485(HardwareSerial* serial, int writeEnabledPin);
    virtual void begin();
    virtual boolean dataAvailable();
    virtual byte* readFrame();
    virtual void addDataToQueue(NetFefFrameBuilder* frameBuilder);
    boolean canSend();
    void setDebug(Print* print);
    virtual unsigned int getFrameLength() { return COMM_DATA_FRAME_LENGTH; };  // -- TODO: wanted to avoid dynamic buffer allocation.
  
  private:
    boolean _writeFrameCheckCollision(byte* data, int length);
    static void step(Task* me);
    static void copyDataValues(byte* from, byte* to);
    byte _readBuffer[COMM_DATA_FRAME_LENGTH];  // -- TODO: wanted to avoid dynamic buffer allocation.
    byte _commQueue[COMM_QUEUE_LENGTH][COMM_DATA_FRAME_LENGTH];  // -- TODO: wanted to avoid dynamic buffer allocation.
    int _frameSizes[COMM_DATA_FRAME_LENGTH];  // -- TODO: wanted to avoid dynamic buffer allocation.
    unsigned short _queueSize = 0;
    void _debug(char* message);
    int _writeEnabledPin;
    HardwareSerial* _serial;
    void _addDataToQueue(byte* data, int length);
    Print* _debugOut = NULL;
};

#endif // -- NetFefRs485_H
