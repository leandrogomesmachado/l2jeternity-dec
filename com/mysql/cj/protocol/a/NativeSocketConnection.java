package com.mysql.cj.protocol.a;

import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.exceptions.CJOperationNotSupportedException;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.exceptions.FeatureNotAvailableException;
import com.mysql.cj.exceptions.SSLParamsException;
import com.mysql.cj.log.Log;
import com.mysql.cj.protocol.AbstractSocketConnection;
import com.mysql.cj.protocol.FullReadInputStream;
import com.mysql.cj.protocol.PacketSentTimeHolder;
import com.mysql.cj.protocol.ReadAheadInputStream;
import com.mysql.cj.protocol.ServerSession;
import com.mysql.cj.protocol.SocketConnection;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.AsynchronousSocketChannel;

public class NativeSocketConnection extends AbstractSocketConnection implements SocketConnection {
   @Override
   public void connect(String hostName, int portNumber, PropertySet propSet, ExceptionInterceptor excInterceptor, Log log, int loginTimeout) {
      try {
         this.port = portNumber;
         this.host = hostName;
         this.propertySet = propSet;
         this.exceptionInterceptor = excInterceptor;
         this.socketFactory = this.createSocketFactory(propSet.getStringProperty("socketFactory").getStringValue());
         this.mysqlSocket = this.socketFactory.connect(this.host, this.port, propSet.exposeAsProperties(), loginTimeout);
         int socketTimeout = propSet.getIntegerProperty("socketTimeout").getValue();
         if (socketTimeout != 0) {
            try {
               this.mysqlSocket.setSoTimeout(socketTimeout);
            } catch (Exception var9) {
            }
         }

         this.socketFactory.beforeHandshake();
         InputStream rawInputStream;
         if (propSet.getBooleanProperty("useReadAheadInput").getValue()) {
            rawInputStream = new ReadAheadInputStream(this.mysqlSocket.getInputStream(), 16384, propSet.getBooleanProperty("traceProtocol").getValue(), log);
         } else if (propSet.getBooleanProperty("useUnbufferedInput").getValue()) {
            rawInputStream = this.mysqlSocket.getInputStream();
         } else {
            rawInputStream = new BufferedInputStream(this.mysqlSocket.getInputStream(), 16384);
         }

         this.mysqlInput = new FullReadInputStream(rawInputStream);
         this.mysqlOutput = new BufferedOutputStream(this.mysqlSocket.getOutputStream(), 16384);
      } catch (IOException var10) {
         throw ExceptionFactory.createCommunicationsException(propSet, null, new PacketSentTimeHolder() {
         }, null, var10, this.getExceptionInterceptor());
      }
   }

   @Override
   public void performTlsHandshake(ServerSession serverSession) throws SSLParamsException, FeatureNotAvailableException, IOException {
      this.mysqlSocket = this.socketFactory.performTlsHandshake(this, serverSession);
      this.mysqlInput = new FullReadInputStream(
         (InputStream)(this.propertySet.getBooleanProperty("useUnbufferedInput").getValue()
            ? this.getMysqlSocket().getInputStream()
            : new BufferedInputStream(this.getMysqlSocket().getInputStream(), 16384))
      );
      this.mysqlOutput = new BufferedOutputStream(this.getMysqlSocket().getOutputStream(), 16384);
      this.mysqlOutput.flush();
   }

   @Override
   public AsynchronousSocketChannel getAsynchronousSocketChannel() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }
}
