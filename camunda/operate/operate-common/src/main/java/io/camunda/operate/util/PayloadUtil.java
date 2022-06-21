package io.camunda.operate.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.operate.exceptions.OperateRuntimeException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PayloadUtil {
   @Autowired
   private ObjectMapper objectMapper;

   public Map parsePayload(String payload) throws IOException {
      Map map = new LinkedHashMap();
      this.traverseTheTree(this.objectMapper.readTree(payload), map, "");
      return map;
   }

   public String readStringFromClasspath(String filename) {
      try {
         InputStream inputStream = PayloadUtil.class.getResourceAsStream(filename);

         String var3;
         try {
            if (inputStream == null) {
               throw new OperateRuntimeException("Failed to find " + filename + " in classpath ");
            }

            var3 = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
         } catch (Throwable var6) {
            if (inputStream != null) {
               try {
                  inputStream.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (inputStream != null) {
            inputStream.close();
         }

         return var3;
      } catch (IOException var7) {
         throw new OperateRuntimeException("Failed to load file " + filename + " from classpath ", var7);
      }
   }

   private void traverseTheTree(JsonNode jsonNode, Map map, String path) {
      if (jsonNode.isValueNode()) {
         Object value;
         value = null;
         label34:
         switch (jsonNode.getNodeType()) {
            case BOOLEAN:
               value = jsonNode.booleanValue();
               break;
            case NUMBER:
               switch (jsonNode.numberType()) {
                  case INT:
                  case LONG:
                  case BIG_INTEGER:
                     value = jsonNode.longValue();
                     break label34;
                  case FLOAT:
                  case DOUBLE:
                  case BIG_DECIMAL:
                     value = jsonNode.doubleValue();
                  default:
                     break label34;
               }
            case STRING:
               value = jsonNode.textValue();
            case NULL:
            case BINARY:
         }

         map.put(path, value);
      } else if (jsonNode.isContainerNode()) {
         if (jsonNode.isObject()) {
            Iterator fieldIterator = jsonNode.fieldNames();

            while(fieldIterator.hasNext()) {
               String fieldName = (String)fieldIterator.next();
               JsonNode var10001 = jsonNode.get(fieldName);
               String var10003 = path.isEmpty() ? "" : path + ".";
               this.traverseTheTree(var10001, map, var10003 + fieldName);
            }
         } else if (jsonNode.isArray()) {
            int i = 0;

            for(Iterator var7 = jsonNode.iterator(); var7.hasNext(); ++i) {
               JsonNode child = (JsonNode)var7.next();
               this.traverseTheTree(child, map, path + "[" + i + "]");
            }
         }
      }

   }
}
