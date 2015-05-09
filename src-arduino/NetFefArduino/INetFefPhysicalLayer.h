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
#ifndef INetFefPhysicalLayer_H
#define INetFefPhysicalLayer_H

#include <Arduino.h>
#include "NetFefData.h"

class INetFefPhysicalLayer
{
  public:
    virtual void begin();
    virtual boolean dataAvailable();
    virtual byte* readFrame();
    virtual void addDataToQueue(NetFefFrameBuilder* frameBuilder);
    virtual boolean canSend();
    virtual unsigned int getFrameLength();
    virtual char* getVersion();
};

#endif // -- INetFefPhysicalLayer_H
