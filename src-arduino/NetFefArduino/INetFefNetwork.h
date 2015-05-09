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
#ifndef INetFefNetwork_H
#define INetFefNetwork_H

#include <Arduino.h>
#include "INetFefPhysicalLayer.h"

typedef struct RegistrationInfo
{
  byte myAddress[2];
  unsigned long registrationId;
  unsigned long networkId;
};

class INetFefNetwork
{
  public:
    virtual void begin();
    virtual boolean sendFrame(NetFefFrameBuilder* frameBuilder);
    virtual char* getVersion();
};


#endif // -- INetFefNetwork_H
