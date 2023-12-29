package com.mysql.cj.protocol;

import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.exceptions.FeatureNotAvailableException;
import com.mysql.cj.exceptions.SSLParamsException;
import com.mysql.cj.log.Log;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.channels.AsynchronousSocketChannel;

public interface SocketConnection {
   void connect(String var1, int var2, PropertySet var3, ExceptionInterceptor var4, Log var5, int var6);

   void performTlsHandshake(ServerSession var1) throws SSLParamsException, FeatureNotAvailableException, IOException;

   void forceClose();

   NetworkResources getNetworkResources();

   String getHost();

   int getPort();

   Socket getMysqlSocket();

   FullReadInputStream getMysqlInput();

   void setMysqlInput(InputStream var1);

   BufferedOutputStream getMysqlOutput();

   boolean isSSLEstablished();

   SocketFactory getSocketFactory();

   void setSocketFactory(SocketFactory var1);

   ExceptionInterceptor getExceptionInterceptor();

   PropertySet getPropertySet();

   default boolean isSynchronous() {
      return true;
   }

   AsynchronousSocketChannel getAsynchronousSocketChannel();
}
