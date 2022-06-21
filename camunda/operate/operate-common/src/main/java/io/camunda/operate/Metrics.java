package io.camunda.operate;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.util.Collection;
import java.util.Queue;
import java.util.function.ToDoubleFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Metrics {
   private static final Logger logger = LoggerFactory.getLogger(Metrics.class);
   private Timer importBatchTimer;
   public static final String OPERATE_NAMESPACE = "operate.";
   public static final String TIMER_NAME_QUERY = "operate.query";
   public static final String TIMER_NAME_IMPORT_QUERY = "operate.import.query";
   public static final String TIMER_NAME_IMPORT_INDEX_QUERY = "operate.import.index.query";
   public static final String TIMER_NAME_IMPORT_PROCESS_BATCH = "operate.import.process.batch";
   public static final String TIMER_NAME_ARCHIVER_QUERY = "operate.archiver.query";
   public static final String TIMER_NAME_ARCHIVER_REINDEX_QUERY = "operate.archiver.reindex.query";
   public static final String TIMER_NAME_ARCHIVER_DELETE_QUERY = "operate.archiver.delete.query";
   public static final String COUNTER_NAME_EVENTS_PROCESSED = "events.processed";
   public static final String COUNTER_NAME_EVENTS_PROCESSED_FINISHED_WI = "events.processed.finished.process.instances";
   public static final String COUNTER_NAME_COMMANDS = "commands";
   public static final String COUNTER_NAME_ARCHIVED = "archived.process.instances";
   public static final String GAUGE_IMPORT_QUEUE_SIZE = "import.queue.size";
   public static final String TAG_KEY_NAME = "name";
   public static final String TAG_KEY_TYPE = "type";
   public static final String TAG_KEY_PARTITION = "partition";
   public static final String TAG_KEY_STATUS = "status";
   public static final String TAG_VALUE_PROCESSINSTANCES = "processInstances";
   public static final String TAG_VALUE_CORESTATISTICS = "corestatistics";
   public static final String TAG_VALUE_SUCCEEDED = "succeeded";
   public static final String TAG_VALUE_FAILED = "failed";
   @Autowired
   private MeterRegistry registry;

   public void recordCounts(String name, long count, String... tags) {
      this.registry.counter("operate." + name, tags).increment((double)count);
   }

   public void registerGauge(String name, Queue stateObject, ToDoubleFunction<Queue> valueFunction, String... tags) {
      Gauge.builder("operate." + name, stateObject, valueFunction).tags(tags).register(this.registry);
   }

   public void registerGaugeQueueSize(String name, Queue queue, String... tags) {
      this.registerGauge(name, queue, (q) -> {
         return (double)q.size();
      }, tags);
   }

   public Timer getTimer(String name) {
      return this.registry.timer(name, new String[0]);
   }
}
