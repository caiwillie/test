package io.camunda.operate.util;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Random;

public abstract class DateUtil {
   private static final Random RANDOM = new Random();

   public static OffsetDateTime getRandomStartDate() {
      Instant now = Instant.now();
      now = now.minus((long)(5 + RANDOM.nextInt(10)), ChronoUnit.DAYS);
      now = now.minus((long)RANDOM.nextInt(1440), ChronoUnit.MINUTES);
      Clock clock = Clock.fixed(now, ZoneOffset.UTC);
      return OffsetDateTime.now(clock);
   }

   public static OffsetDateTime getRandomEndDate() {
      return getRandomEndDate(false);
   }

   public static OffsetDateTime getRandomEndDate(boolean nullable) {
      if (nullable && RANDOM.nextInt(10) % 3 == 1) {
         return null;
      } else {
         Instant now = Instant.now();
         now = now.minus((long)(1 + RANDOM.nextInt(4)), ChronoUnit.DAYS);
         now = now.minus((long)RANDOM.nextInt(1440), ChronoUnit.MINUTES);
         Clock clock = Clock.fixed(now, ZoneOffset.UTC);
         return OffsetDateTime.now(clock);
      }
   }

   public static OffsetDateTime toOffsetDateTime(Instant timestamp) {
      return OffsetDateTime.ofInstant(timestamp, ZoneOffset.UTC);
   }
}
