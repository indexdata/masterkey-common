/*
 * Copyright (c) 1995-2010, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */
package com.indexdata.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author jakub
 */
public class LRUCache<K, V> {

  private int numRelegated = 0;
  //private int numCached = 0;
  private final int CACHE_SIZE;
  private final Map<K, V> recordCache;

  @SuppressWarnings("serial")
  public LRUCache(int cacheSize) {
    CACHE_SIZE = cacheSize;
    recordCache = new LinkedHashMap<K, V>(CACHE_SIZE) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        boolean remove = size() > CACHE_SIZE;
        if (remove) {
          numRelegated++;
        }
        return remove;
      }
    };
  }

  public synchronized V get (K key) {
    return recordCache.get(key);
  }
 
  public synchronized void put (K key, V value) {
    if (value == null) throw new IllegalArgumentException("The cached value cannot be 'null'");
    recordCache.put(key, value);
  }

  public synchronized V remove(K key) {
    return recordCache.remove(key);
  }
}
