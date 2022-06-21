package io.camunda.operate.logging;

import java.io.IOException;
import java.io.OutputStream;
import org.apache.logging.log4j.core.layout.ByteBufferDestination;

public final class ByteBufferDestinationOutputStream extends OutputStream {
   private final ByteBufferDestination destination;

   public ByteBufferDestinationOutputStream(ByteBufferDestination destination) {
      this.destination = destination;
   }

   public void write(int b) throws IOException {
      byte[] bytes = new byte[]{(byte)b};
      this.write(bytes);
   }

   public void write(byte[] bytes) throws IOException {
      this.write(bytes, 0, bytes.length);
   }

   public void write(byte[] bytes, int off, int len) throws IOException {
      this.destination.writeBytes(bytes, off, len);
   }
}
