package com.mysql.cj.protocol.x;

import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.exceptions.CJCommunicationsException;
import com.mysql.cj.exceptions.CJOperationNotSupportedException;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.exceptions.FeatureNotAvailableException;
import com.mysql.cj.exceptions.SSLParamsException;
import com.mysql.cj.log.Log;
import com.mysql.cj.protocol.AbstractSocketConnection;
import com.mysql.cj.protocol.AsyncSocketFactory;
import com.mysql.cj.protocol.FullReadInputStream;
import com.mysql.cj.protocol.NetworkResources;
import com.mysql.cj.protocol.ServerSession;
import com.mysql.cj.protocol.SocketConnection;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.channels.AsynchronousSocketChannel;

public class XAsyncSocketConnection extends AbstractSocketConnection implements SocketConnection {
   AsynchronousSocketChannel channel;

   @Override
   public void connect(String hostName, int portNumber, PropertySet propSet, ExceptionInterceptor excInterceptor, Log log, int loginTimeout) {
      this.port = portNumber;
      this.host = hostName;
      this.propertySet = propSet;
      this.socketFactory = new AsyncSocketFactory();

      try {
         this.channel = this.socketFactory.connect(hostName, portNumber, propSet.exposeAsProperties(), loginTimeout);
      } catch (CJCommunicationsException var8) {
         throw var8;
      } catch (RuntimeException | IOException var9) {
         throw new CJCommunicationsException(var9);
      }
   }

   @Override
   public void performTlsHandshake(ServerSession serverSession) throws SSLParamsException, FeatureNotAvailableException, IOException {
      this.channel = this.socketFactory.performTlsHandshake(this, serverSession);
   }

   @Override
   public AsynchronousSocketChannel getAsynchronousSocketChannel() {
      return this.channel;
   }

   @Override
   public final void forceClose() {
      try {
         if (this.channel != null && this.channel.isOpen()) {
            this.channel.close();
         }
      } catch (IOException var5) {
      } finally {
         this.channel = null;
      }
   }

   @Override
   public NetworkResources getNetworkResources() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public Socket getMysqlSocket() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public FullReadInputStream getMysqlInput() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public void setMysqlInput(InputStream mysqlInput) {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public BufferedOutputStream getMysqlOutput() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public boolean isSSLEstablished() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public ExceptionInterceptor getExceptionInterceptor() {
      throw (CJOperationNotSupportedException)ExceptionFactory.createException(CJOperationNotSupportedException.class, "Not supported");
   }

   @Override
   public boolean isSynchronous() {
      return false;
   }
}
