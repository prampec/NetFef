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

package com.netfef.protocol.obsidian;

import com.netfef.data.Frame;
import com.netfef.data.NetFefDataHelper;
import com.netfef.data.Parameter;
import com.netfef.data.ParameterType;
import com.netfef.protocol.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * <p>Obsidian is a protocol implementation based on NetFef core components.</p>
 * <p>User: kelemenb
 * <br/>Date: 4/28/15</p>
 */
public class NetFefObsidian implements NetFefNetwork {
    /** Logger. */
    private static final Logger LOG = LoggerFactory.getLogger(NetFefObsidian.class);
    private static final char NETWORK_MANAGEMENT_MESSAGE_SUBJECT = 'n';

    private NetFefPhysicalLayer physicalLayer;
    private byte[] myAddress;
    private PeerPersister peerPersister;
    private NetFefReceiveListener listener;
    private Queue<Frame> sendQueue = new ConcurrentLinkedQueue<>();
    private Queue<OnGoingSend> onGoingSendQueue = new ConcurrentLinkedQueue<>();
    private Map<Frame, OnGoingSend> onGoingSendLookup = new ConcurrentHashMap<>();
    private Thread sendThread;
    private boolean running;
    private Frame waitingReplyFor;
    private long waitStartTime;
    private NetFefObsidianConfig config;
    private Thread joinOfferThread;
    private Thread pollThread;
    private HashMapLs<Address, Peer> registrationLookup = new HashMapLs<>();
    private Map<Integer, ReplyInfo> replyMap = new ConcurrentHashMap<>();

    public void init(NetFefPhysicalLayer physicalLayer, byte[] myAddress, PeerPersister peerPersister) {
        this.physicalLayer = physicalLayer;
        this.myAddress = myAddress;
        this.peerPersister = peerPersister;
        config = (NetFefObsidianConfig)physicalLayer.getConfig(this.getClass());
        try {
            physicalLayer.init();
        }
        catch (IOException e) {
            LOG.error("Error initializing physicalLayer.", e);
        }
        physicalLayer.setListener(new NetFefReceiveListener() {
            @Override
            public void dataReceived(Frame frame) {
                processFrame(frame);
            }

            @Override
            public void handleError(IOException e) {
                LOG.error("Network error occurred.", e);
            }
        });

        sendThread = new Thread(this::sending);
        sendThread.start();

        joinOfferThread = new Thread(this::joinOffer);
        joinOfferThread.start();

        pollThread = new Thread(this::poll);
        pollThread.start();

    }

    private void poll() {
        Peer currentPeer = null;
        while(this.running) {
            currentPeer = registrationLookup.getNextAfter(currentPeer);
            if(currentPeer == null) {
                sleep(500);
            } else if(currentPeer.isActive()) {
                Date now = new Date();
                if((currentPeer.lastSeen.getTime() + 2*config.getNextPollMaxSecs()*1000) > now.getTime()) {
                    currentPeer.setActive(false);
                    peerPersister.persist(currentPeer);
                }

                if(currentPeer.isActive() && currentPeer.nextPollTime.before(now)) {
                    Frame frame = new Frame(myAddress,'n', 'p');
                    final Peer finalCurrentRegistration = currentPeer;
                    this.sendData(frame, new ReplyListener() {
                        @Override
                        public void onReply(Frame originalFrame, Frame repliedFrame) {
                            finalCurrentRegistration.lastSeen = now;
                            Integer n = repliedFrame.hasParameter('n') ? repliedFrame.getParameter('n').getIntValue() : 2*60;
                            if(n > config.getNextPollMaxSecs()) {
                                n = config.getNextPollMaxSecs();
                            }
                            finalCurrentRegistration.nextPollTime = new Date(now.getTime() + n*1000);
                            listener.dataReceived(frame);
                        }
                    });
                }
                sleep(50);
            }
        }
    }

    private void joinOffer() {
        while(this.running) {
            Frame joinOffer = new Frame(NetFefDataHelper.BROADCAST_ADDRESS, NETWORK_MANAGEMENT_MESSAGE_SUBJECT, 'j');
            joinOffer.addParameter(new Parameter('w', ParameterType.INTEGER, 30));
            this.sendData(joinOffer);

            sleep(config.getJoinOfferRepeatSecs() * 1000);
        }

    }

    private void sending() {
        while(this.running) {
            long now = new Date().getTime();

            // -- Test for finished waiting.
            if((waitingReplyFor != null) && (waitStartTime + config.getReplyMaxDelayMs() > now)) {
                OnGoingSend onGoingSend = onGoingSendLookup.get(waitingReplyFor);
                if(onGoingSend == null) {
                    // -- Did not replied within the MaxDelay time, we need to retry the send.
                    onGoingSend = new OnGoingSend(waitingReplyFor);
                    onGoingSendQueue.add(onGoingSend);
                    onGoingSendLookup.put(waitingReplyFor, onGoingSend);
                }
                if(onGoingSend.retryCount >= config.getReplyRepeatCount()) {
                    // -- We already retried the send, we will not try it any more.
                    onGoingSendQueue.remove(onGoingSend);
                    onGoingSendLookup.remove(waitingReplyFor);
                    Parameter replyReferenceParameter = waitingReplyFor.getParameter('r');
                    Integer replyReference = replyReferenceParameter.getIntValue();
                    ReplyInfo replyInfo = replyMap.get(replyReference);
                    replyMap.remove(replyReference);
                    replyInfo.replyListener.onError(waitingReplyFor);
                }
                waitingReplyFor = null;
            }

            if(!sendQueue.isEmpty()) {
                if(waitingReplyFor == null) {
                    // -- Send the message
                    Frame frameToSend = sendQueue.poll();
                    NetFefObsidian.this.physicalLayer.sendData(frameToSend, myAddress);
                    if(frameToSend.hasParameter('r')) {
                        this.waitingReplyFor = frameToSend;
                        this.waitStartTime = now;
                    }
                }
            } else if(!onGoingSendQueue.isEmpty()) {
                // -- Resend an unreplied message
                OnGoingSend onGoingSend = onGoingSendQueue.poll();
                Frame frameToSend = onGoingSend.frame;
                onGoingSend.retryCount += 1;
                NetFefObsidian.this.physicalLayer.sendData(frameToSend, myAddress);
                this.waitingReplyFor = frameToSend;
                this.waitStartTime = now;
                onGoingSendQueue.add(onGoingSend);
            }

            sleep(10);
        }

    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        }
        catch (InterruptedException e) {
            // -- Ignore interrupt.
        }
    }

    private void processFrame(Frame frame) {
        if(frame.hasParameter('R')) {
            // -- A reply received, call the listener
            Parameter replyReferenceParameter = frame.getParameter('R');
            Integer replyReference = replyReferenceParameter.getIntValue();
            ReplyInfo replyInfo = replyMap.get(replyReference);
            if(replyInfo != null) {
                replyMap.remove(replyReference);
                Frame originalFrame = replyInfo.originalFrame;
                waitingReplyFor = null;
                onGoingSendLookup.remove(originalFrame);
                sendQueue.remove(originalFrame);
                replyInfo.replyListener.onReply(originalFrame, frame);
            } else {
                LOG.warn("Replied for an unknown reference number.");
            }
        }
        else if(NETWORK_MANAGEMENT_MESSAGE_SUBJECT == frame.getSubject().getChar()) {
            if('j' == frame.getCommand().getChar()) {
                processJoinRequest(frame);
            }
        } else {
            this.listener.dataReceived(frame);
        }
    }

    private void processJoinRequest(Frame joinRequest) {
        byte[] senderAddress = joinRequest.getSenderAddress();
        long registrationId = 0;
        if (joinRequest.hasParameter('i')) {
            registrationId = joinRequest.getParameter('i').getLongValue();
        }

        Address address = new Address(senderAddress);
        Peer registeredPeer;
        registeredPeer = registrationLookup.get(address);
        if ((registeredPeer == null) || (registeredPeer.getRegistrationId() == registrationId)) {
            final Peer peer = registeredPeer == null ? new Peer(senderAddress, generateRegistrationId()) : registeredPeer;

            Frame joinFrame = new Frame(senderAddress, NETWORK_MANAGEMENT_MESSAGE_SUBJECT, 'J');
            joinFrame.addParameter(new Parameter('d', ParameterType.CHAR, 'a'));
            joinFrame.addParameter(new Parameter('i', ParameterType.LONG, peer.getRegistrationId()));

            this.sendData(joinFrame, new ReplyListener() {
                @Override
                public void onReply(Frame originalFrame, Frame repliedFrame) {
                    peer.lastSeen = new Date();
                    peer.setActive(true);
                    registrationLookup.put(address, peer);
                    peerPersister.persist(peer);
                }
            });
        }
        else {
            // -- Address already in use
            Frame joinDeclineFrame = new Frame(senderAddress, NETWORK_MANAGEMENT_MESSAGE_SUBJECT, 'J');
            joinDeclineFrame.addParameter(new Parameter('d', ParameterType.CHAR, 'd'));

            this.sendData(joinDeclineFrame);
        }
    }

    private long generateRegistrationId() {
        return new Date().getTime();
    }

    @Override
    public void setListener(NetFefReceiveListener listener) {
        this.listener = listener;
    }

    @Override
    public void sendData(Frame frame) {
        sendData(frame, null);
    }
    @Override
    public void sendData(Frame frame, ReplyListener replyListener) {
        if(replyListener != null) {
            int replyReference = generateReplyReference();
            frame.addParameter(new Parameter('r', ParameterType.INTEGER, replyReference));
            replyMap.put(replyReference, new ReplyInfo(frame, replyListener));
        }
        sendQueue.add(frame);
    }

    private int generateReplyReference() {
        while(true) {
            Random random = new Random();
            int nextInt = random.nextInt(1<<17);
            if(!replyMap.containsKey(nextInt)) {
                return nextInt;
            }
        }
    }

    @Override
    public void shutdown() {
        running = false;
        sendThread.interrupt();
        joinOfferThread.interrupt();
        pollThread.interrupt();
        try {
            sendThread.join();
        }
        catch (InterruptedException e) {
            LOG.error("Thread couldn't be stopped.", e);
        }
        try {
            joinOfferThread.join();
        }
        catch (InterruptedException e) {
            LOG.error("Thread couldn't be stopped.", e);
        }
        try {
            pollThread.join();
        }
        catch (InterruptedException e) {
            LOG.error("Thread couldn't be stopped.", e);
        }
    }

    private static class OnGoingSend {
        Frame frame;
        int retryCount = 0;

        public OnGoingSend(Frame frame) {
            this.frame = frame;
        }
    }

    private static class Address {
        byte[] value;

        public Address(byte[] value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Address address = (Address)o;

            if (!Arrays.equals(value, address.value)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(value);
        }
    }

    private static class ReplyInfo {
        Frame originalFrame;
        ReplyListener replyListener;
//        Date creationTime = new Date();

        public ReplyInfo(Frame originalFrame, ReplyListener replyListener) {
            this.originalFrame = originalFrame;
            this.replyListener = replyListener;
        }
    }

}
