package com.mysql.cj.protocol;

import com.mysql.cj.exceptions.CJCommunicationsException;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AsyncSocketFactory implements SocketFactory {
   AsynchronousSocketChannel channel;

   @Override
   public <T extends Closeable> T connect(String host, int port, Properties props, int loginTimeout) throws IOException {
      try {
         this.channel = AsynchronousSocketChannel.open();
         this.channel.setOption(StandardSocketOptions.SO_SNDBUF, 131072);
         this.channel.setOption(StandardSocketOptions.SO_RCVBUF, 131072);
         Future<Void> connectPromise = this.channel.connect(new InetSocketAddress(host, port));
         connectPromise.get();
      } catch (CJCommunicationsException var6) {
         throw var6;
      } catch (InterruptedException | ExecutionException | RuntimeException | IOException var7) {
         throw new CJCommunicationsException(var7);
      }

      return (T)this.channel;
   }

   @Override
   public <T extends Closeable> T performTlsHandshake(SocketConnection socketConnection, ServerSession serverSession) throws IOException {
      this.channel = ExportControlled.startTlsOnAsynchronousChannel(this.channel, socketConnection);
      return (T)this.channel;
   }
}
