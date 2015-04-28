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

class INetFefNetwork
{
  public:
    void begin();
    boolean dataAvailable();
    byte* readFrame();
    void addDataToQueue(NetFefFrameBuilder* frameBuilder);
    boolean canSend();
};

#endif // -- INetFefNetwork_H
