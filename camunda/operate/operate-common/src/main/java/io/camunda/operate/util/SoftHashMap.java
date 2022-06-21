package io.camunda.operate.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

public class SoftHashMap implements Map {
   private static final int DEFAULT_RETENTION_SIZE = 100;
   private final Map map;
   private final int RETENTION_SIZE;
   private final Queue strongReferences;
   private final ReentrantLock strongReferencesLock;
   private final ReferenceQueue queue;

   public SoftHashMap() {
      this(100);
   }

   public SoftHashMap(int retentionSize) {
      this.RETENTION_SIZE = Math.max(0, retentionSize);
      this.queue = new ReferenceQueue();
      this.strongReferencesLock = new ReentrantLock();
      this.map = new ConcurrentHashMap();
      this.strongReferences = new ConcurrentLinkedQueue();
   }

   public SoftHashMap(Map source) {
      this(100);
      this.putAll(source);
   }

   public SoftHashMap(Map source, int retentionSize) {
      this(retentionSize);
      this.putAll(source);
   }

   public Object get(Object key) {
      this.processQueue();
      Object result = null;
      SoftValue value = (SoftValue)this.map.get(key);
      if (value != null) {
         result = value.get();
         if (result == null) {
            this.map.remove(key);
         } else {
            this.addToStrongReferences(result);
         }
      }

      return result;
   }

   private void addToStrongReferences(Object result) {
      this.strongReferencesLock.lock();

      try {
         this.strongReferences.add(result);
         this.trimStrongReferencesIfNecessary();
      } finally {
         this.strongReferencesLock.unlock();
      }

   }

   private void trimStrongReferencesIfNecessary() {
      while(this.strongReferences.size() > this.RETENTION_SIZE) {
         this.strongReferences.poll();
      }

   }

   private void processQueue() {
      SoftValue sv;
      while((sv = (SoftValue)this.queue.poll()) != null) {
         this.map.remove(sv.key);
      }

   }

   public boolean isEmpty() {
      this.processQueue();
      return this.map.isEmpty();
   }

   public boolean containsKey(Object key) {
      this.processQueue();
      return this.map.containsKey(key);
   }

   public boolean containsValue(Object value) {
      this.processQueue();
      Collection values = this.values();
      return values != null && values.contains(value);
   }

   public void putAll(Map m) {
      if (m != null && !m.isEmpty()) {
         Iterator var2 = m.entrySet().iterator();

         while(var2.hasNext()) {
            Map.Entry entry = (Map.Entry)var2.next();
            this.put(entry.getKey(), entry.getValue());
         }

      } else {
         this.processQueue();
      }
   }

   public Set keySet() {
      this.processQueue();
      return this.map.keySet();
   }

   public Collection values() {
      this.processQueue();
      Collection keys = this.map.keySet();
      if (keys.isEmpty()) {
         return Collections.EMPTY_SET;
      } else {
         Collection values = new ArrayList(keys.size());
         Iterator var3 = keys.iterator();

         while(var3.hasNext()) {
            Object key = var3.next();
            Object v = this.get(key);
            if (v != null) {
               values.add(v);
            }
         }

         return values;
      }
   }

   public Object put(Object key, Object value) {
      this.processQueue();
      SoftValue sv = new SoftValue(value, key, this.queue);
      SoftValue previous = (SoftValue)this.map.put(key, sv);
      this.addToStrongReferences(value);
      return previous != null ? previous.get() : null;
   }

   public Object remove(Object key) {
      this.processQueue();
      SoftValue raw = (SoftValue)this.map.remove(key);
      return raw != null ? raw.get() : null;
   }

   public void clear() {
      this.strongReferencesLock.lock();

      try {
         this.strongReferences.clear();
      } finally {
         this.strongReferencesLock.unlock();
      }

      this.processQueue();
      this.map.clear();
   }

   public int size() {
      this.processQueue();
      return this.map.size();
   }

   public Set entrySet() {
      this.processQueue();
      Collection keys = this.map.keySet();
      if (keys.isEmpty()) {
         return Collections.EMPTY_SET;
      } else {
         Map kvPairs = new HashMap(keys.size());
         Iterator var3 = keys.iterator();

         while(var3.hasNext()) {
            Object key = var3.next();
            Object v = this.get(key);
            if (v != null) {
               kvPairs.put(key, v);
            }
         }

         return kvPairs.entrySet();
      }
   }

   private static class SoftValue extends SoftReference {
      private final Object key;

      private SoftValue(Object value, Object key, ReferenceQueue queue) {
         super(value, queue);
         this.key = key;
      }
   }
}
