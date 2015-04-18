#ifndef NetFefRs485_H
#define NetFefRs485_H

#include <Arduino.h>
#include <Task.h>

#include "NetFef.h"

#define COLLOSION_PENALTY_MS 100
#define COMM_QUEUE_LENGTH 5
#define COMM_DATA_FRAME_LENGTH 50
//#define COMM_DATA_FRAME 1
#define READ_TIMEOUT_MS 10


class NetFefRs485 : public Task
{
  public:
    NetFefRs485(HardwareSerial* serial, int writeEnabledPin);
    void begin();
    boolean dataAvailable();
    byte* readFrame();
    void addDataToQueue(NetFefFrameBuilder* frameBuilder);
    boolean canSend();
    void setDebug(Print* print);
  
  private:
    boolean _writeFrameCheckCollision(byte* data, int length);
    static void step(Task* me);
    static void copyDataValues(byte* from, byte* to);
    byte _readBuffer[COMM_DATA_FRAME_LENGTH];
    byte _commQueue[COMM_QUEUE_LENGTH][COMM_DATA_FRAME_LENGTH];
    int _frameSizes[COMM_DATA_FRAME_LENGTH];
    unsigned short _queueSize = 0;
    void _debug(char* message);
    int _writeEnabledPin;
    HardwareSerial* _serial;
    void _addDataToQueue(byte* data, int length);
    Print* _debugOut = NULL;
};

#endif // -- NetFefRs485
