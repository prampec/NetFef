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
  netFefRs485.setDebug(&lcd);
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

  me->setPeriodMs( random(WRITE_DELAY_MS_MAX) + MINIMAL_FRAME_SPACING_MS );
}

void readerJob(Task* me) {
  if(netFefRs485.dataAvailable()) {
    lcd.setCursor(0,1);
    byte* data = netFefRs485.readFrame();
    NetFefFrameReader netFefFrameReader = NetFefFrameReader(data);
    if(netFefFrameReader.isForMe(MYADDRESS)) {
      byte *p = netFefFrameReader.getCommand();
      if(p == 0) {
        lcd.print("Incompatible frame   ");
        return;
      }        
      NetFefParameter parameter = NetFefParameter(p);
      if(parameter.isType('c')) {
        char cmd = (char)parameter.getValue1();
        if(cmd == 't') {
          p = netFefFrameReader.getParameter('v');
          if(p == 0) {
            lcd.print("Incompatible frame   ");
            return;
          }        
          parameter = NetFefParameter(p);
          char v = (char)parameter.getValue1();
          
          lcd.print("Received(");
          lcd.print(cmd);
          lcd.print(") v=");
          lcd.print(v);
          lcd.print("  ");
        } else {
          lcd.print("Received(");
          lcd.print(cmd);
          lcd.print(")");
          lcd.print("    ");
        }
      } else {
        lcd.print("Unknown command type   ");
      }
    } else {
      lcd.print("Not for me     ");
    }
  }
}

