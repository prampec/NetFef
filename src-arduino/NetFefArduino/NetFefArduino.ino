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
/**
 * Two direction communication.
 */
#include <SoftTimer.h>
#include <Task.h>
#include <BlinkTask.h>
#include <LiquidCrystal.h>

#include "NetFef.h"
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
  lcd.begin(16, 2);
  lcd.print("ready");
  SoftTimer.add(&testTask);
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
  frameBuilder.addParameter('v', 'c', &counter);
  netFefRs485.addDataToQueue(&frameBuilder);

  counter += 1;
  if(counter > LAST_CHAR) {
    counter = FIRST_CHAR;
  }

  me->setPeriodMs( random(WRITE_DELAY_MS_MAX) + COLLOSION_PENALTY_MS );
}

void readerJob(Task* me) {
  if(netFefRs485.dataAvailable()) {
    byte* data = netFefRs485.readFrame();

if(data[0] != 0) {
    lcd.setCursor(0,1);
    lcd.print(data[0]);
    lcd.setCursor(2,1);
    lcd.print((unsigned int)data[0]);
    lcd.print("  ");
}
  }
}

