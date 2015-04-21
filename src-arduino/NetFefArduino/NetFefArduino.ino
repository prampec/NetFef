/**
 * Two direction communication.
 */
#include <SoftTimer.h>
#include <Task.h>
#include <BlinkTask.h>
#include <LiquidCrystal.h>

#include "NetFef.h"
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
#include "NetFefRs485.h"

#define FIRST_CHAR 'A'
#define LAST_CHAR FIRST_CHAR + 25

#define WRITE_DELAY_MS_MAX 100
#define WRITE_ENABLE_PIN 2
#define LCD_BACKLIGHT_PIN 10

const byte MYADDRESS[2] = { 0x12, 0xab };

LiquidCrystal lcd(4, 5, 6, 7, 8, 9);
Task testTask(WRITE_DELAY_MS_MAX, test);
Task readTask(0, readerJob);
NetFefRs485 netFefRs485(&Serial, WRITE_ENABLE_PIN);

void setup() {
  netFefRs485.begin();
  netFefRs485.setDebug(&lcd);
  lcd.begin(16, 2);
  lcd.print("ready");
//  SoftTimer.add(&testTask);
  SoftTimer.add(&readTask);
  pinMode(LCD_BACKLIGHT_PIN, OUTPUT);
  digitalWrite(LCD_BACKLIGHT_PIN, LOW); // -- Turn on LCD backlight
}

char counter = FIRST_CHAR;
byte data[COMM_DATA_FRAME_LENGTH];
void test(Task* me) {
  NetFefFrameBuilder frameBuilder = NetFefFrameBuilder(data, MYADDRESS, MASTER_ADDRESS, 't');
  lcd.setCursor(0,0);
  if(!netFefRs485.canSend()) {
    lcd.print("Cannot send");
    me->setPeriodMs(5);
    return;
  }
  lcd.print("Sending ");
  lcd.print(counter);
  lcd.print("   ");
  frameBuilder.addParameter('v', 'c', &counter);
  netFefRs485.addDataToQueue(&frameBuilder);

  counter += 1;
  if(counter > LAST_CHAR) {
    counter = FIRST_CHAR;
  }

  me->setPeriodMs( random(WRITE_DELAY_MS_MAX) + MINIMAL_FRAME_SPACING_MS );
}

void readerJob(Task* me) {
  if(netFefRs485.dataAvailable()) {
    lcd.setCursor(0,1);
    byte* data = netFefRs485.readFrame();
    NetFefFrameReader netFefFrameReader = NetFefFrameReader(data);
//netFefFrameReader._debug = &lcd;
    if(netFefFrameReader.isForMe(MYADDRESS)) {
      lcd.print(">");
      byte *p = netFefFrameReader.getCommand();
      while(p != 0) {
        printParameter(p);
        p = netFefFrameReader.getNextParameter(p);
      }
      lcd.print("  ");
    } else {
      lcd.print("Not for me     ");
    }
  }
}

void printParameter(byte* p) {
  if(p == 0) {
    lcd.print("Incompatible frame   ");
    return;
  }
  NetFefParameter parameter = NetFefParameter(p);
//parameter._debug = &lcd;

//  lcd.print(parameter.getName());
//  lcd.print('(');
  lcd.print(parameter.getType());
//  lcd.print(")=");
  lcd.print(":");
  if(parameter.isType('c')) {
    char v = parameter.getCharValue();
    lcd.print(v);
  }
  else if(parameter.isType('B') || parameter.isType('b')) {
    byte v = parameter.getByteValue();
    lcd.print(v);
  }
  else if(parameter.isType('i')) {
    unsigned int v = parameter.getIntValue();
    lcd.print(v);
  }
  else if(parameter.isType('I')) {
    int v = parameter.getSignedIntValue();
    lcd.print(v);
  }
  else if(parameter.isType('l')) {
    unsigned long v = parameter.getLongValue();
    lcd.print(v);
  }
  else if(parameter.isType('L')) {
    long v = parameter.getLongValue();
    lcd.print(v);
  }
  else if(parameter.isType('s') || parameter.isType('S')) {
    char* v = parameter.getStringValue();
    lcd.print(v);
  }
  else {
    lcd.print('?');
  }
  lcd.print(' ');
}
