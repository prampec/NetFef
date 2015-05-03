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
#include <Task.h>
#include <BlinkTask.h>
#include <LiquidCrystal.h>
#include <EEPROM.h>

#include "NetFefData.h"
#define COMM_QUEUE_LENGTH 5
#define COMM_DATA_FRAME_LENGTH 60
#include "NetFefRs485.h"
#include "NetFefObsidian.h"

#define EEPROM_ADDRESS_FOR_REGISTRATION_INFO 400

#define FIRST_CHAR 'A'
#define LAST_CHAR FIRST_CHAR + 25

//#define WRITE_DELAY_MS_MAX 100
#define WRITE_DELAY_MS_MAX 2000
#define WRITE_ENABLE_PIN 2
#define LCD_BACKLIGHT_PIN 10

const byte MYADDRESS[2] = { 0x12, 0xab };

LiquidCrystal lcd(4, 5, 6, 7, 8, 9);
NetFefRs485 netFefRs485(&Serial, WRITE_ENABLE_PIN);
NetFefObsidian netFefNetwork(&netFefRs485, onFrameReceived, loadRegistrationInfo, saveRegistrationInfo);
Task testTask(WRITE_DELAY_MS_MAX, test);
RegistrationInfo registrationInfo;

void setup() {
  netFefNetwork.begin();
  lcd.begin(16, 2);
  lcd.print("ready");
  SoftTimer.add(&testTask);
  pinMode(LCD_BACKLIGHT_PIN, OUTPUT);
  digitalWrite(LCD_BACKLIGHT_PIN, LOW); // -- Turn on LCD backlight
}

char counter = FIRST_CHAR;
byte data[COMM_DATA_FRAME_LENGTH];
NetFefFrameBuilder frameBuilder = NetFefFrameBuilder(data, COMM_DATA_FRAME_LENGTH);
void test(Task* me) {
  frameBuilder.reset(MYADDRESS, MASTER_ADDRESS, 't', 't');
  lcd.setCursor(0,0);
  if(!netFefRs485.canSend()) {
    lcd.print("Cannot send");
    me->setPeriodMs(5);
    return;
  }
  lcd.print("Sending ");
  lcd.print(counter);
  lcd.print("   ");
  char sbuf[10];
  sprintf(sbuf, "aa%dbb", counter);
//frameBuilder._debug = &lcd;

  frameBuilder.addParameter('a', 'c', &counter);
  frameBuilder.addParameter('b', 's', sbuf);
  frameBuilder.addParameter('d', 'S', sbuf);
  frameBuilder.addParameter('e', 'i', (unsigned int)123 * counter);
  frameBuilder.addParameter('f', 'I', -125 * counter);
  frameBuilder.addParameter('g', 'l', (unsigned long)12345 * counter);
  frameBuilder.addParameter('h', 'L', -12345L * counter);
  netFefNetwork.sendFrame(&frameBuilder);

  counter += 1;
  if(counter > LAST_CHAR) {
    counter = FIRST_CHAR;
  }

  me->setPeriodMs( random(WRITE_DELAY_MS_MAX) + MINIMAL_FRAME_SPACING_MS );
}

RegistrationInfo* loadRegistrationInfo() {
  // -- Load from EEPROM
  EEPROM.get(EEPROM_ADDRESS_FOR_REGISTRATION_INFO, registrationInfo);
  return &registrationInfo;
}

void saveRegistrationInfo(RegistrationInfo* ri) {
  // -- save to EEPROM
  EEPROM.put(EEPROM_ADDRESS_FOR_REGISTRATION_INFO, *ri);
}

NetFefFrameBuilder* onFrameReceived(NetFefFrameReader* frameReader, NetFefFrameBuilder* frameBuilder) {
  lcd.print(">");
  byte *p = frameReader->getCommand();
  while(p != 0) {
    printParameter(p);
    p = frameReader->getNextParameter(p);
  }
  lcd.print("  ");
  if(frameReader->isSubjectAndCommand('n', 'p')) {
    return frameBuilder;
  } else {
    return 0;
  }
}

NetFefParameter parameter = NetFefParameter();
void printParameter(byte* p) {
  if(p == 0) {
    lcd.print("Incompatible frame   ");
    return;
  }
  parameter.reset(p);
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
