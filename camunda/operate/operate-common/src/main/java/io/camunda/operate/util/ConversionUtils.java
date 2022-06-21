package io.camunda.operate.util;

import java.net.InetSocketAddress;
import java.util.function.Function;

public class ConversionUtils {
   public static final Function<String, Long> stringToLong = (aString) -> {
      return aString == null ? null : Long.valueOf(aString);
   };
   public static final Function<Long, String> longToString = (aLong) -> {
      return aLong == null ? null : String.valueOf(aLong);
   };

   public static String toStringOrNull(Object object) {
      return toStringOrDefault(object, (String)null);
   }

   public static String toStringOrDefault(Object object, String defaultString) {
      return object == null ? defaultString : object.toString();
   }

   public static Long toLongOrNull(String aString) {
      return toLongOrDefault(aString, (Long)null);
   }

   public static Long toLongOrDefault(String aString, Long defaultValue) {
      return aString == null ? defaultValue : Long.valueOf(aString);
   }

   public static String toHostAndPortAsString(InetSocketAddress address) {
      return String.format("%s:%d", address.getHostName(), address.getPort());
   }

   public static boolean stringIsEmpty(String aString) {
      return aString == null || aString.isEmpty();
   }
}
