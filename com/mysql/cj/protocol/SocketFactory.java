package com.mysql.cj.protocol;

import java.io.Closeable;
import java.io.IOException;
import java.util.Properties;

public interface SocketFactory extends SocketMetadata {
   String TCP_KEEP_ALIVE_DEFAULT_VALUE = "true";
   String TCP_RCV_BUF_DEFAULT_VALUE = "0";
   String TCP_SND_BUF_DEFAULT_VALUE = "0";
   String TCP_TRAFFIC_CLASS_DEFAULT_VALUE = "0";
   String TCP_NO_DELAY_DEFAULT_VALUE = "true";

   <T extends Closeable> T connect(String var1, int var2, Properties var3, int var4) throws IOException;

   default void beforeHandshake() throws IOException {
   }

   <T extends Closeable> T performTlsHandshake(SocketConnection var1, ServerSession var2) throws IOException;

   default void afterHandshake() throws IOException {
   }
}
