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

package com.netfef.rs485;

import com.netfef.protocol.Frame;
import com.netfef.protocol.NetFef;
import com.netfef.util.FormatHelper;
import com.pi4j.io.gpio.*;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.EventListener;

/**
 * <p>RS485 based implementation for the NetFef protocol.</p>
 * <p>User: kelemenb
 * <br/>Date: 4/8/15</p>
 */
public class NetFefRs485 {
    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(NetFefRs485.class);

    public static final Pin WRITE_ENABLED_PIN = RaspiPin.GPIO_01;
    private static final int MAX_LEN = 1024;
    private static final long WAIT_TIMEOUT = 200;
    private GpioPinDigitalOutput writeEnablePin;
    private Serial serial;
    private NetFefRs485ReceiveListener listener;
    private GpioController gpio;
    private SerialDataEventListener serialDataEventListener;

    public NetFefRs485(NetFefRs485ReceiveListener listener) {
        this.listener = listener;
    }

    public void init() throws IOException {
        gpio = GpioFactory.getInstance();
        serial = SerialFactory.createInstance();

        writeEnablePin = gpio.provisionDigitalOutputPin(WRITE_ENABLED_PIN, "WriteEnabled", PinState.LOW);

        serialDataEventListener = new SerialDataEventListener() {
            @Override
            public void dataReceived(SerialDataEvent event) {
                byte[] bytes;
                try {
                    bytes = event.getBytes();
                }
                catch (IOException e) {
                    LOG.error("Error receiving bytes", e);
                    listener.handleError(e);
                    return;
                }
                if(bytes.length < 12) {
                    // -- A valid frame is at least 12 byte long.
                    return;
                }
                LOG.trace("Bytes received: " + FormatHelper.byteArrayToString(bytes));
                Frame frame = NetFef.buildFrameObject(bytes, MAX_LEN, NetFef.MASTER_ADDRESS);
                listener.dataReceived(frame);
            }
        };

        serial.open(Serial.DEFAULT_COM_PORT, 9600);
    }

    public void enableRead() {
        serial.addListener(serialDataEventListener);
    }

    public void disableRead() {
        serial.removeListener(serialDataEventListener);
    }

    public void sendData(Frame frame, byte[] myAddress) {
        disableRead();
        writeEnablePin.high();
        byte[] bytesToSend = NetFef.buildFrameBytes(frame, myAddress);
        try {
            serial.write(bytesToSend);
            waitForEcho();
            byte[] read = serial.read();
            // -- TODO: check for collision
            String s = FormatHelper.byteArrayToString(read);
            System.out.println("Echo found " + s);
        }
        catch (IOException e) {
            LOG.error("Error sending bytes", e);
            throw new IllegalStateException("Error sending bytes.");
        }
        writeEnablePin.low();
        enableRead();
    }

    private void waitForEcho() throws IOException {
        Date date = new Date();
        int available;
        while((available = serial.available()) == 0) {
            if(new Date().getTime() > (date.getTime() + WAIT_TIMEOUT) ) {
                throw new IllegalStateException("Own echo not found");
            }
            try {
                Thread.sleep(5);
            }
            catch (InterruptedException e) {
                LOG.error("Interrupted wait.", e);
            }
        }
        if(available < 0) {
            throw new IllegalStateException("Read error");
        }
    }

    public void shutdown() {
        try {
            serial.close();
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }
        gpio.shutdown();
    }

    public static interface NetFefRs485ReceiveListener extends EventListener {

        void dataReceived(Frame frame);

        void handleError(IOException e);
    }
}
