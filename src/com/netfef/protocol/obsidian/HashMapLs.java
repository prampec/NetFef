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

import java.util.*;

/**
 * <p>Link values after each other.</p>
 * <p>User: kelemenb
 * <br/>Date: 4/29/15</p>
 */
public class HashMapLs<T, U> implements Map<T, U> {

    HashMap<T, U> internalMap = new HashMap<>();
    ArrayList<U> values = new ArrayList<>();

    public synchronized U getFirst() {
        return values.size() <= 0 ? null : values.get(0);
    }

    public synchronized U getNextAfter(U value) {
        if((value == null) || (!values.contains(value))) {
            return getFirst();
        }
        int i = values.indexOf(value) + 1;
        if(i >= values.size()) {
            i = 0;
        }
        return values.get(i);
    }

    @Override
    public synchronized int size() {
        return internalMap.size();
    }

    @Override
    public synchronized boolean isEmpty() {
        return internalMap.isEmpty();
    }

    @Override
    public synchronized boolean containsKey(Object key) {
        return internalMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public synchronized U get(Object key) {
        return internalMap.get(key);
    }

    @Override
    public synchronized U put(T key, U value) {
        values.add(value);
        return internalMap.put(key, value);
    }

    @Override
    public synchronized U remove(Object key) {
        U value = internalMap.get(key);
        values.remove(value);
        return internalMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends T, ? extends U> m) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Set<T> keySet() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Collection<U> values() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Set<Entry<T, U>> entrySet() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public boolean equals(Object o) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
