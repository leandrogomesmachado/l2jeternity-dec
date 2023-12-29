package com.sun.mail.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class WriteTimeoutSocket extends Socket {
   private final Socket socket;
   private final ScheduledExecutorService ses;
   private final int timeout;

   public WriteTimeoutSocket(Socket socket, int timeout) throws IOException {
      this.socket = socket;
      this.ses = Executors.newScheduledThreadPool(1);
      this.timeout = timeout;
   }

   public WriteTimeoutSocket(int timeout) throws IOException {
      this(new Socket(), timeout);
   }

   public WriteTimeoutSocket(InetAddress address, int port, int timeout) throws IOException {
      this(timeout);
      this.socket.connect(new InetSocketAddress(address, port));
   }

   public WriteTimeoutSocket(InetAddress address, int port, InetAddress localAddress, int localPort, int timeout) throws IOException {
      this(timeout);
      this.socket.bind(new InetSocketAddress(localAddress, localPort));
      this.socket.connect(new InetSocketAddress(address, port));
   }

   public WriteTimeoutSocket(String host, int port, int timeout) throws IOException {
      this(timeout);
      this.socket.connect(new InetSocketAddress(host, port));
   }

   public WriteTimeoutSocket(String host, int port, InetAddress localAddress, int localPort, int timeout) throws IOException {
      this(timeout);
      this.socket.bind(new InetSocketAddress(localAddress, localPort));
      this.socket.connect(new InetSocketAddress(host, port));
   }

   @Override
   public void connect(SocketAddress remote) throws IOException {
      this.socket.connect(remote, 0);
   }

   @Override
   public void connect(SocketAddress remote, int timeout) throws IOException {
      this.socket.connect(remote, timeout);
   }

   @Override
   public void bind(SocketAddress local) throws IOException {
      this.socket.bind(local);
   }

   @Override
   public SocketChannel getChannel() {
      return this.socket.getChannel();
   }

   @Override
   public InetAddress getInetAddress() {
      return this.socket.getInetAddress();
   }

   @Override
   public InetAddress getLocalAddress() {
      return this.socket.getLocalAddress();
   }

   @Override
   public int getPort() {
      return this.socket.getPort();
   }

   @Override
   public int getLocalPort() {
      return this.socket.getLocalPort();
   }

   @Override
   public InputStream getInputStream() throws IOException {
      return this.socket.getInputStream();
   }

   @Override
   public synchronized OutputStream getOutputStream() throws IOException {
      return new TimeoutOutputStream(this.socket.getOutputStream(), this.ses, this.timeout);
   }

   @Override
   public void setTcpNoDelay(boolean on) throws SocketException {
      this.socket.setTcpNoDelay(on);
   }

   @Override
   public boolean getTcpNoDelay() throws SocketException {
      return this.socket.getTcpNoDelay();
   }

   @Override
   public void setSoLinger(boolean on, int linger) throws SocketException {
      this.socket.setSoLinger(on, linger);
   }

   @Override
   public int getSoLinger() throws SocketException {
      return this.socket.getSoLinger();
   }

   @Override
   public void sendUrgentData(int data) throws IOException {
      this.socket.sendUrgentData(data);
   }

   @Override
   public void setOOBInline(boolean on) throws SocketException {
      this.socket.setOOBInline(on);
   }

   @Override
   public boolean getOOBInline() throws SocketException {
      return this.socket.getOOBInline();
   }

   @Override
   public void setSoTimeout(int timeout) throws SocketException {
      this.socket.setSoTimeout(timeout);
   }

   @Override
   public int getSoTimeout() throws SocketException {
      return this.socket.getSoTimeout();
   }

   @Override
   public void setSendBufferSize(int size) throws SocketException {
      this.socket.setSendBufferSize(size);
   }

   @Override
   public int getSendBufferSize() throws SocketException {
      return this.socket.getSendBufferSize();
   }

   @Override
   public void setReceiveBufferSize(int size) throws SocketException {
      this.socket.setReceiveBufferSize(size);
   }

   @Override
   public int getReceiveBufferSize() throws SocketException {
      return this.socket.getReceiveBufferSize();
   }

   @Override
   public void setKeepAlive(boolean on) throws SocketException {
      this.socket.setKeepAlive(on);
   }

   @Override
   public boolean getKeepAlive() throws SocketException {
      return this.socket.getKeepAlive();
   }

   @Override
   public void setTrafficClass(int tc) throws SocketException {
      this.socket.setTrafficClass(tc);
   }

   @Override
   public int getTrafficClass() throws SocketException {
      return this.socket.getTrafficClass();
   }

   @Override
   public void setReuseAddress(boolean on) throws SocketException {
      this.socket.setReuseAddress(on);
   }

   @Override
   public boolean getReuseAddress() throws SocketException {
      return this.socket.getReuseAddress();
   }

   @Override
   public void close() throws IOException {
      try {
         this.socket.close();
      } finally {
         this.ses.shutdownNow();
      }
   }

   @Override
   public void shutdownInput() throws IOException {
      this.socket.shutdownInput();
   }

   @Override
   public void shutdownOutput() throws IOException {
      this.socket.shutdownOutput();
   }

   @Override
   public String toString() {
      return this.socket.toString();
   }

   @Override
   public boolean isConnected() {
      return this.socket.isConnected();
   }

   @Override
   public boolean isBound() {
      return this.socket.isBound();
   }

   @Override
   public boolean isClosed() {
      return this.socket.isClosed();
   }

   @Override
   public boolean isInputShutdown() {
      return this.socket.isInputShutdown();
   }

   @Override
   public boolean isOutputShutdown() {
      return this.socket.isOutputShutdown();
   }
}
