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
#define COMM_DATA_FRAME_LENGTH 80
#include "NetFefRs485.h"
#include "NetFefObsidian.h"

#define EEPROM_ADDRESS_FOR_REGISTRATION_INFO 400

#define FIRST_CHAR 'A'
#define LAST_CHAR FIRST_CHAR + 25

//#define WRITE_DELAY_MS_MAX 100
#define WRITE_DELAY_MS_MAX 10000
#define WRITE_ENABLE_PIN 2
#define LCD_BACKLIGHT_PIN 10

const byte MYADDRESS[2] = { 0x12, 0xab };

LiquidCrystal lcd(4, 5, 6, 7, 8, 9);
NetFefRs485 netFefRs485(&Serial, WRITE_ENABLE_PIN);
NetFefObsidian netFefNetwork(&netFefRs485, onFrameReceived, loadRegistrationInfo, saveRegistrationInfo, "test device", 30, analogRead(0));
Task testTask(WRITE_DELAY_MS_MAX, test);
RegistrationInfo registrationInfo;

void setup() {
  netFefNetwork.begin();
  lcd.begin(16, 2);
  lcd.print("ready");
  SoftTimer.add(&testTask);
  pinMode(LCD_BACKLIGHT_PIN, OUTPUT);
  digitalWrite(LCD_BACKLIGHT_PIN, LOW); // -- Turn on LCD backlight
  
  netFefNetwork._debug = &lcd;
}

char counter = FIRST_CHAR;
byte data[COMM_DATA_FRAME_LENGTH];
NetFefFrameBuilder frameBuilder = NetFefFrameBuilder(data, COMM_DATA_FRAME_LENGTH);
void test(Task* me) {
  if(netFefNetwork.joinedToNetwork) {
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
lcd.print("$");
  EEPROM.put(EEPROM_ADDRESS_FOR_REGISTRATION_INFO, *ri);
}

NetFefFrameBuilder* onFrameReceived(NetFefFrameReader* frameReader, NetFefFrameBuilder* frameBuilder) {
  if(frameReader->isSubjectAndCommand('n', 'p')) {
    return frameBuilder;
  } else {
    lcd.clear();
  lcd.print(">");
  /*
//frameReader->_debug = &lcd;
lcd.setCursor(0,0);
  byte *p = frameReader->getSubject();
  while(p != 0) {
lcd.setCursor(0,1);
    printParameter(&lcd, p);
lcd.setCursor(0,0);
    p = frameReader->getNextParameter(p);
  }
  lcd.print("  ");
lcd.setCursor(0,0);
    return 0;
  */
  }
}

NetFefParameter parameter = NetFefParameter();
void printParameter(Print* debug, byte* p) {
  if(p == 0) {
    debug->print("Incompatible frame   ");
    return;
  }
  parameter.reset(p);
//parameter._debug = debug;

//  debug->print(parameter.getName());
//  debug->print('(');
  debug->print(parameter.getType());
//  debug->print(")=");
  debug->print(":");
  if(parameter.isType('c')) {
    char v = parameter.getCharValue();
    debug->print(v);
  }
  else if(parameter.isType('B') || parameter.isType('b')) {
    byte v = parameter.getByteValue();
    debug->print(v);
  }
  else if(parameter.isType('i')) {
    unsigned int v = parameter.getIntValue();
    debug->print(v);
  }
  else if(parameter.isType('I')) {
    int v = parameter.getSignedIntValue();
    debug->print(v);
  }
  else if(parameter.isType('l')) {
    unsigned long v = parameter.getLongValue();
    debug->print(v);
  }
  else if(parameter.isType('L')) {
    long v = parameter.getLongValue();
    debug->print(v);
  }
  else if(parameter.isType('s') || parameter.isType('S')) {
    char* v = parameter.getStringValue();
    debug->print(v);
  }
  else {
    debug->print('?');
  }
  debug->print(' ');
}

/*
NetFefFrameReader frameReader = NetFefFrameReader(60);
void setup() {
  Serial.begin(9600);
  Serial.print("Startup.");
  byte frame[] = {0x00, 0x1a, 0x02, 0x00 , 0x00 , 0x02 , 0x00 , 0x01 , 0x04 , 0x73 , 0x63 , 0x6e , 0x63 , 0x63 , 0x6a , 0x77 , 0x69 , 0x00 , 0x1e , 0x6e , 0x6c , 0x00 , 0x00 , 0x30 , 0x39 , (byte)0xd8};
  frameReader.reset(frame);
  frameReader._debug = &Serial;
  Serial.print("Valid: ");
  Serial.println( frameReader.isValid() );

  byte *p = frameReader.getSubject();
  while(p != 0) {
    printParameter(&Serial, p);
    Serial.println();
    p = frameReader.getNextParameter(p);
  }
  
}
*/
