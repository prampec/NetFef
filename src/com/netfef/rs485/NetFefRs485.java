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

import com.netfef.data.ByteArrayBuilder;
import com.netfef.data.Frame;
import com.netfef.data.NetFefDataHelper;
import com.netfef.protocol.NetFefNetwork;
import com.netfef.protocol.NetFefNetworkConfig;
import com.netfef.protocol.NetFefPhysicalLayer;
import com.netfef.protocol.NetFefReceiveListener;
import com.netfef.protocol.obsidian.NetFefObsidian;
import com.netfef.protocol.obsidian.NetFefObsidianConfig;
import com.netfef.util.FormatHelper;
import com.pi4j.io.gpio.*;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * <p>RS485 based implementation for the NetFef protocol.</p>
 * <p>User: kelemenb
 * <br/>Date: 4/8/15</p>
 */
public class NetFefRs485 implements NetFefPhysicalLayer {
    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(NetFefRs485.class);

    public static final Pin WRITE_ENABLED_PIN = RaspiPin.GPIO_01;
    public static final int MINIMAL_FRAME_SPACING_MS = 200;
    public static final int COLLISION_PENALTY_MAX_MS = 400;
    private static final int MAX_LEN = 1024;
    private static final int RECEIVE_MS = 120;
    private GpioPinDigitalOutput writeEnablePin;
    private Serial serial;
    private NetFefReceiveListener listener;
    private GpioController gpio;
    private Queue<byte[]> sendQueue = new ConcurrentLinkedQueue<>();
    private boolean running;
    private Thread sendThread;
    private Random random = new Random();
    private Thread receiveThread;
    private boolean readEnabled;
    private boolean readInProgress = false;

    public NetFefRs485() {
    }

    @Override
    public void setListener(NetFefReceiveListener listener) {
        this.listener = listener;
    }

    @Override
    public void init() throws IOException {
        running = true;
        gpio = GpioFactory.getInstance();
        serial = SerialFactory.createInstance();

        writeEnablePin = gpio.provisionDigitalOutputPin(WRITE_ENABLED_PIN, "WriteEnabled", PinState.LOW);

        enableRead();

        serial.open(Serial.DEFAULT_COM_PORT, 9600);

        sendThread = new Thread(() -> {
            while(NetFefRs485.this.running) {
                byte[] bytes = sendQueue.peek();
                boolean hasCollision = false;
                if(bytes != null) {
                    if(NetFefRs485.this.sendDataCheckCollision(bytes)) {
                        sendQueue.remove();
                    } else {
                        hasCollision = true;
                    }
                }
                try {
                    int millis = MINIMAL_FRAME_SPACING_MS;
                    if(hasCollision) {
                        millis += random.nextInt(COLLISION_PENALTY_MAX_MS);
                    }
                    Thread.sleep(millis);
                }
                catch (InterruptedException e) {
                    LOG.trace("Interrupted");
                }
            }
        });

        sendThread.start();

        receiveThread = new Thread(() -> {
            ByteArrayBuilder bab = new ByteArrayBuilder();
            Long lastSerialRead = null;
            while (NetFefRs485.this.running) {
                if(readEnabled) {
                    try {
                        if (serial.available() > 0) {
                            readInProgress = true;
                            byte[] bytes = serial.read();
                            lastSerialRead = new Date().getTime();
                            bab.append(bytes);
                        }
                        else {
                            if ((lastSerialRead != null) && ((lastSerialRead + RECEIVE_MS) < new Date().getTime())) {

                                byte[] bytes = bab.getBytes();
                                bab.clear();
                                readInProgress = false;
                                lastSerialRead = null;

                                if (bytes.length < 12) {
//System.out.println("An invalid frame with size of " + bytes.length + " received.");
                                    // -- A valid frame is at least 12 byte long.
                                    return;
                                }
                                if (LOG.isTraceEnabled()) {
                                    LOG.trace("Bytes received: " + FormatHelper.byteArrayToString(bytes));
                                }
//System.out.println("Bytes received: " + FormatHelper.byteArrayToString2(bytes));
                                Frame frame = NetFefDataHelper.buildFrameObject(bytes, MAX_LEN, NetFefDataHelper.MASTER_ADDRESS);
                                listener.dataReceived(frame);

                            }
                        }
                    }
                    catch (IOException e) {
                        LOG.error("Error reading from serial", e);
                        sleep(2000);
                    }
                } else {
                    sleep(50);
                }
            }
        });

        receiveThread.start();
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        }
        catch (InterruptedException e1) {
            LOG.error("Interrupted", e1);
        }
    }

    public void enableRead() {
        this.readEnabled = true;
    }

    public boolean disableRead() {
        if(this.readInProgress) {
            return false;
        }
        else {
            this.readEnabled = false;
            return true;
        }
    }

    @Override
    public void sendData(Frame frame, byte[] myAddress) {
        byte[] bytesToSend = NetFefDataHelper.buildFrameBytes(frame, myAddress);
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
            if(LOG.isTraceEnabled()) {
                String s = FormatHelper.byteArrayToString(read);
                LOG.trace("Echo found " + s);
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
            if(new Date().getTime() > (date.getTime() + RECEIVE_MS) ) {
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

    @Override
    public void shutdown() {
        running = false;
        sendThread.interrupt();
        try {
            sendThread.join();
        }
        catch (InterruptedException e) {
            LOG.error("Thread couldn't be stopped.", e);
        }
        receiveThread.interrupt();
        try {
            receiveThread.join();
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

    @Override
    public NetFefNetworkConfig getConfig(Class<? extends NetFefNetwork> networkClass) {
        if(NetFefObsidian.class.isAssignableFrom(networkClass)) {
            return new NetFefRs485ObsidianConfig();
        }
        else {
            throw new UnsupportedOperationException("Network class " + networkClass.getName() + " configuration is not implemented within " + this.getClass().getName() + " .");
        }
    }
}
