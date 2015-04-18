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
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * <p>RS485 based implementation for the NetFef protocol.</p>
 * <p>User: kelemenb
 * <br/>Date: 4/8/15</p>
 */
public class NetFefRs485 {
    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(NetFefRs485.class);

    public static final Pin WRITE_ENABLED_PIN = RaspiPin.GPIO_01;
    public static final int MINIMAL_FRAME_SPACING_MS = 100;
    private static final int MAX_LEN = 1024;
    private static final long WAIT_TIMEOUT = 200;
    private GpioPinDigitalOutput writeEnablePin;
    private Serial serial;
    private NetFefRs485ReceiveListener listener;
    private GpioController gpio;
    private SerialDataEventListener serialDataEventListener;
    private Queue<byte[]> sendQueue = new ConcurrentLinkedDeque<>();
    private boolean running;
    private Thread sendThread;
    private Random random = new Random();

    public NetFefRs485(NetFefRs485ReceiveListener listener) {
        this.listener = listener;
    }

    public void init() throws IOException {
        running = true;
        gpio = GpioFactory.getInstance();
        serial = SerialFactory.createInstance();

        writeEnablePin = gpio.provisionDigitalOutputPin(WRITE_ENABLED_PIN, "WriteEnabled", PinState.LOW);

        serialDataEventListener = event -> {
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
        };
        enableRead();

        serial.open(Serial.DEFAULT_COM_PORT, 9600);

        sendThread = new Thread(() -> {
            while(NetFefRs485.this.running) {
                byte[] bytes = sendQueue.peek();
                if(bytes != null) {
                    if(NetFefRs485.this.sendDataCheckCollision(bytes)) {
                        sendQueue.remove();
                    }
                }
                try {
                    Thread.sleep(MINIMAL_FRAME_SPACING_MS + random.nextInt(MINIMAL_FRAME_SPACING_MS));
                }
                catch (InterruptedException e) {
                    LOG.error("Interrupted", e);
                }
            }
        });

        sendThread.start();
    }

    public void enableRead() {
        serial.addListener(serialDataEventListener);
    }

    public boolean disableRead() {
        Date start = new Date();
        try {
            while(serial.available() > 0) {
                if(new Date().getTime() > (start.getTime() + WAIT_TIMEOUT)) {
                    LOG.debug("Cannot send: data on channel");
                    return false;
                }
                Thread.sleep(5);
            }
        }
        catch (IOException e) {
            LOG.error("Error on serial.", e);
            return false;
        }
        catch (InterruptedException e) {
            LOG.error("Interrupted", e);
            return false;
        }
        serial.removeListener(serialDataEventListener);
        return true;
    }

    public void sendData(Frame frame, byte[] myAddress) {
        byte[] bytesToSend = NetFef.buildFrameBytes(frame, myAddress);
        sendQueue.add(bytesToSend);
    }

    private boolean sendDataCheckCollision(byte[] bytesToSend) {
        if(!disableRead()) {
            return false;
        }
        writeEnablePin.high();
        try {
            serial.write(bytesToSend);
            if(!waitForEcho()) {
                return false;
            }
            byte[] read = serial.read();
            if(LOG.isInfoEnabled()) {
                String s = FormatHelper.byteArrayToString(read);
                LOG.info("Echo found " + s);
            }
            if(!Arrays.equals(bytesToSend, read)) {
                LOG.info("Collision detected.");
                return false;
            }
        }
        catch (IOException e) {
            LOG.error("Error sending bytes", e);
            return false;
        } finally {
            writeEnablePin.low();
            enableRead();
        }
        return true;
    }

    private boolean waitForEcho() throws IOException {
        Date date = new Date();
        int available;
        while((available = serial.available()) == 0) {
            if(new Date().getTime() > (date.getTime() + WAIT_TIMEOUT) ) {
                LOG.debug("Own echo not found");
                return false;
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
        return true;
    }

    public void shutdown() {
        running = false;
        try {
            sendThread.join();
        }
        catch (InterruptedException e) {
            LOG.error("Thread couldn't be stopped.", e);
        }
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
