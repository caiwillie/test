package io.camunda.operate.util;

import io.camunda.operate.exceptions.OperateRuntimeException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class CollectionUtil {
   public static Object getOrDefaultForNullValue(Map map, Object key, Object defaultValue) {
      Object value = map.get(key);
      return value == null ? defaultValue : value;
   }

   public static Object getOrDefaultFromMap(Map map, Object key, Object defaultValue) {
      return key == null ? defaultValue : map.getOrDefault(key, defaultValue);
   }

   public static Object firstOrDefault(List list, Object defaultValue) {
      return list.isEmpty() ? defaultValue : list.get(0);
   }

   @SafeVarargs
   public static List throwAwayNullElements(Object... array) {
      List listOfNotNulls = new ArrayList();
      Object[] var2 = array;
      int var3 = array.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Object o = var2[var4];
         if (o != null) {
            listOfNotNulls.add(o);
         }
      }

      return listOfNotNulls;
   }

   public static Predicate distinctByKey(Function keyExtractor) {
      Set seen = ConcurrentHashMap.newKeySet();
      return (t) -> {
         return seen.add(keyExtractor.apply(t));
      };
   }

   public static List emptyListWhenNull(List aList) {
      return aList == null ? Collections.emptyList() : aList;
   }

   public static List withoutNulls(Collection aCollection) {
      return aCollection != null ? filter(aCollection, Objects::nonNull) : Collections.emptyList();
   }

   public static Map addToMap(Map map, Object key, Object value) {
      ((List)map.computeIfAbsent(key, (k) -> {
         return new ArrayList();
      })).add(value);
      return map;
   }

   public static Map asMap(Object... keyValuePairs) {
      if (keyValuePairs != null && keyValuePairs.length % 2 == 0) {
         Map result = new HashMap();

         for(int i = 0; i < keyValuePairs.length - 1; i += 2) {
            result.put(keyValuePairs[i].toString(), keyValuePairs[i + 1]);
         }

         return result;
      } else {
         throw new OperateRuntimeException("keyValuePairs should not be null and has a even length.");
      }
   }

   public static List map(Collection sourceList, Function mapper) {
      return map(sourceList.stream(), mapper);
   }

   public static List map(Object[] sourceArray, Function mapper) {
      return map((Stream)Arrays.stream(sourceArray).parallel(), mapper);
   }

   public static List map(Stream sequenceStream, Function mapper) {
      return (List)sequenceStream.map(mapper).collect(Collectors.toList());
   }

   public static List filter(Collection collection, Predicate predicate) {
      return filter(collection.stream(), predicate);
   }

   public static List filter(Stream filterStream, Predicate predicate) {
      return (List)filterStream.filter(predicate).collect(Collectors.toList());
   }

   public static List toSafeListOfStrings(Collection aCollection) {
      return map((Collection)withoutNulls(aCollection), Object::toString);
   }

   public static String[] toSafeArrayOfStrings(Collection aCollection) {
      return (String[])toSafeListOfStrings(aCollection).toArray(new String[0]);
   }

   public static List toSafeListOfStrings(Object... objects) {
      return toSafeListOfStrings((Collection)Arrays.asList(objects));
   }

   public static List toSafeListOfLongs(Collection aCollection) {
      return map((Collection)withoutNulls(aCollection), ConversionUtils.stringToLong);
   }

   public static void addNotNull(Collection collection, Object object) {
      if (collection != null && object != null) {
         collection.add(object);
      }

   }

   public static List fromTo(int from, int to) {
      List result = new ArrayList();

      for(int i = from; i <= to; ++i) {
         result.add(i);
      }

      return result;
   }

   public static boolean isNotEmpty(Collection aCollection) {
      return aCollection != null && !aCollection.isEmpty();
   }

   public static List splitAndGetSublist(List list, int subsetCount, int subsetId) {
      if (subsetId >= subsetCount) {
         return new ArrayList();
      } else {
         Integer size = list.size();
         int bucketSize = (int)Math.round((double)size / (double)subsetCount);
         int start = bucketSize * subsetId;
         int end;
         if (subsetId == subsetCount - 1) {
            end = size;
         } else {
            end = start + bucketSize;
         }

         return new ArrayList(list.subList(start, end));
      }
   }

   public static Object chooseOne(List items) {
      return items.get((new Random()).nextInt(items.size()));
   }

   public static boolean allElementsAreOfType(Class clazz, Object... array) {
      Object[] var2 = array;
      int var3 = array.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         Object element = var2[var4];
         if (!clazz.isInstance(element)) {
            return false;
         }
      }

      return true;
   }

   public static long countNonNullObjects(Object... objects) {
      return Arrays.stream(objects).filter(Objects::nonNull).count();
   }
}
