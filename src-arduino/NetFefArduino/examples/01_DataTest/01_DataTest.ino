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

#include "NetFefData.h"
#include <SoftTimer.h>

#define FIRST_CHAR 'A'
#define LAST_CHAR FIRST_CHAR + 25

#define COMM_DATA_FRAME_LENGTH 200
#define STRUCT_LENGTH 20
byte data[COMM_DATA_FRAME_LENGTH];
byte structData[STRUCT_LENGTH];
NetFefFrameBuilder frameBuilder = NetFefFrameBuilder(data, COMM_DATA_FRAME_LENGTH);
NetFefStructBuilder structBuilder = NetFefStructBuilder(structData, STRUCT_LENGTH);
NetFefFrameReader frameReader = NetFefFrameReader(COMM_DATA_FRAME_LENGTH);
NetFefStructReader structReader = NetFefStructReader(STRUCT_LENGTH);

const byte MYADDRESS[2] = { 0x12, 0xab };

void setup() {
  Serial.begin(9600);
  
  test();
}

char counter = FIRST_CHAR;
void test() {
  frameBuilder.reset(MYADDRESS, MASTER_ADDRESS, 't', 't');
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

  structBuilder.reset();
  structBuilder.addParameter('t', 'l', (unsigned long)12345 * counter);
  structBuilder.addParameter('m', 's', sbuf);
  frameBuilder.addParameter('x', &structBuilder);

  structBuilder.reset();
  structBuilder.addParameter('t', 'l', (unsigned long)12346 * counter);
  structBuilder.addParameter('m', 's', sbuf);
  frameBuilder.addParameter('x', &structBuilder);

  counter += 1;
  if(counter > LAST_CHAR) {
    counter = FIRST_CHAR;
  }
  
  byte* bytes = frameBuilder.getBytes();
  Serial.println("==");
  printBytes(bytes, frameBuilder.getLength(), &Serial);
  Serial.println();

  Serial.println("++");
  frameReader.reset(data);
  //frameReader->_debug = &lcd;
  byte *p = frameReader.getSubject();
  while(p != 0) {
    printParameter(&Serial, p);
    p = frameReader.getNextParameter(p);
    Serial.println();
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

  debug->print(parameter.getName());
  debug->print('(');
  debug->print(parameter.getType());
  debug->print(")=");
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
  else if(parameter.isType('t') || parameter.isType('T')) {
    parameter.getStructValue(&structReader);
    debug->println("[");
    byte *p2 = structReader.getFirstParameter();
    while(p2 != 0) {
      printParameter(&Serial, p2);
      p2 = structReader.getNextParameter(p2);
      Serial.println();
    }
    debug->print("]");
  }
  else {
    debug->print('?');
  }
  debug->print(' ');
}

void printBytes(byte* bytes, unsigned int length, Print* debug) {
  for(int i=0; i<length; i++) {
    if(i != 0) {
      debug->print(", ");
    }
    debug->print("0x");
    debug->print(bytes[i], HEX);
  }
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
