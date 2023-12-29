package org.nio.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SelectorThread<T extends MMOClient> extends Thread {
   private static final Logger _log = Logger.getLogger(SelectorThread.class.getName());
   private final Selector _selector = Selector.open();
   private final IPacketHandler<T> _packetHandler;
   private final IMMOExecutor<T> _executor;
   private final IClientFactory<T> _clientFactory;
   private final IAcceptFilter _acceptFilter;
   private final SelectorConfig _sc;
   private final int HELPER_BUFFER_SIZE;
   private final ByteBuffer DIRECT_WRITE_BUFFER;
   private final ByteBuffer WRITE_BUFFER;
   private final ByteBuffer READ_BUFFER;
   private T WRITE_CLIENT;
   private final Queue<ByteBuffer> _bufferPool;
   private final List<MMOConnection<T>> _connections;
   private final SelectorStats _stats;
   private boolean _shutdown;

   public SelectorThread(
      SelectorConfig sc,
      SelectorStats stats,
      IPacketHandler<T> packetHandler,
      IMMOExecutor<T> executor,
      IClientFactory<T> clientFactory,
      IAcceptFilter acceptFilter
   ) throws IOException {
      this._sc = sc;
      this._stats = stats;
      this._acceptFilter = acceptFilter;
      this._packetHandler = packetHandler;
      this._clientFactory = clientFactory;
      this._executor = executor;
      this._bufferPool = new ArrayDeque<>(this._sc.HELPER_BUFFER_COUNT);
      this._connections = new CopyOnWriteArrayList<>();
      this.DIRECT_WRITE_BUFFER = ByteBuffer.wrap(new byte[this._sc.WRITE_BUFFER_SIZE]).order(this._sc.BYTE_ORDER);
      this.WRITE_BUFFER = ByteBuffer.wrap(new byte[this._sc.WRITE_BUFFER_SIZE]).order(this._sc.BYTE_ORDER);
      this.READ_BUFFER = ByteBuffer.wrap(new byte[this._sc.READ_BUFFER_SIZE]).order(this._sc.BYTE_ORDER);
      this.HELPER_BUFFER_SIZE = Math.max(this._sc.READ_BUFFER_SIZE, this._sc.WRITE_BUFFER_SIZE);

      for(int i = 0; i < this._sc.HELPER_BUFFER_COUNT; ++i) {
         this._bufferPool.add(ByteBuffer.wrap(new byte[this.HELPER_BUFFER_SIZE]).order(this._sc.BYTE_ORDER));
      }
   }

   public void openServerSocket(InetAddress address, int tcpPort) throws IOException {
      ServerSocketChannel selectable = ServerSocketChannel.open();
      selectable.configureBlocking(false);
      selectable.socket().bind(address == null ? new InetSocketAddress(tcpPort) : new InetSocketAddress(address, tcpPort), this._sc.BACKLOG);
      selectable.register(this.getSelector(), selectable.validOps());
      this.setName("SelectorThread:" + selectable.socket().getLocalPort());
   }

   protected ByteBuffer getPooledBuffer() {
      return this._bufferPool.isEmpty() ? ByteBuffer.wrap(new byte[this.HELPER_BUFFER_SIZE]).order(this._sc.BYTE_ORDER) : this._bufferPool.poll();
   }

   protected void recycleBuffer(ByteBuffer buf) {
      if (this._bufferPool.size() < this._sc.HELPER_BUFFER_COUNT) {
         ((Buffer)buf).clear();
         this._bufferPool.add(buf);
      }
   }

   protected void freeBuffer(ByteBuffer buf, MMOConnection<T> con) {
      if (buf == this.READ_BUFFER) {
         ((Buffer)this.READ_BUFFER).clear();
      } else {
         con.setReadBuffer(null);
         this.recycleBuffer(buf);
      }
   }

   @Override
   public void run() {
      int totalKeys = 0;
      Set<SelectionKey> keys = null;
      Iterator<SelectionKey> itr = null;
      Iterator<MMOConnection<T>> conItr = null;
      SelectionKey key = null;
      MMOConnection<T> con = null;
      long currentMillis = 0L;

      while(true) {
         try {
            if (this.isShuttingDown()) {
               this.closeSelectorThread();
               return;
            }

            currentMillis = System.currentTimeMillis();

            for(MMOConnection<T> var20 : this._connections) {
               if (!var20.getClient().isAuthed() && currentMillis - var20.getConnectionOpenTime() >= this._sc.AUTH_TIMEOUT) {
                  this.closeConnectionImpl(var20);
               } else if (!var20.isPengingClose() || var20.isPendingWrite() && currentMillis - var20.getPendingCloseTime() < this._sc.CLOSEWAIT_TIMEOUT) {
                  if (var20.isPendingWrite() && currentMillis - var20.getPendingWriteTime() >= this._sc.INTEREST_DELAY) {
                     var20.enableWriteInterest();
                  }
               } else {
                  this.closeConnectionImpl(var20);
               }
            }

            totalKeys = this.getSelector().selectNow();
            if (totalKeys > 0) {
               keys = this.getSelector().selectedKeys();
               itr = keys.iterator();

               while(itr.hasNext()) {
                  key = itr.next();
                  itr.remove();
                  if (key.isValid()) {
                     try {
                        if (key.isAcceptable()) {
                           this.acceptConnection(key);
                        } else if (key.isConnectable()) {
                           this.finishConnection(key);
                        } else {
                           if (key.isReadable()) {
                              this.readPacket(key);
                           }

                           if (key.isValid() && key.isWritable()) {
                              this.writePacket(key);
                           }
                        }
                     } catch (CancelledKeyException var13) {
                     }
                  }
               }
            } else {
               try {
                  Thread.sleep(this._sc.SLEEP_TIME);
               } catch (InterruptedException var12) {
               }
            }
         } catch (IOException var14) {
            _log.log(Level.SEVERE, "Error in " + this.getName(), (Throwable)var14);

            try {
               Thread.sleep(1000L);
            } catch (InterruptedException var11) {
            }
         }
      }
   }

   protected void finishConnection(SelectionKey key) {
      try {
         ((SocketChannel)key.channel()).finishConnect();
      } catch (IOException var5) {
         MMOConnection<T> con = (MMOConnection)key.attachment();
         T client = con.getClient();
         client.getConnection().onForcedDisconnection();
         this.closeConnectionImpl(client.getConnection());
      }
   }

   protected void acceptConnection(SelectionKey key) {
      ServerSocketChannel ssc = (ServerSocketChannel)key.channel();

      SocketChannel sc;
      try {
         while((sc = ssc.accept()) != null) {
            if (this.getAcceptFilter() != null && !this.getAcceptFilter().accept(sc)) {
               sc.close();
            } else {
               sc.configureBlocking(false);
               SelectionKey clientKey = sc.register(this.getSelector(), 1);
               MMOConnection<T> con = new MMOConnection<>(this, sc.socket(), clientKey);
               T client = this.getClientFactory().create(con);
               client.setConnection(con);
               con.setClient(client);
               clientKey.attach(con);
               this._connections.add(con);
               this._stats.increaseOpenedConnections();
            }
         }
      } catch (IOException var7) {
         _log.log(Level.SEVERE, "Error in " + this.getName(), (Throwable)var7);
      }
   }

   protected void readPacket(SelectionKey key) {
      MMOConnection<T> con = (MMOConnection)key.attachment();
      if (!con.isClosed()) {
         int result = -2;
         ByteBuffer buf;
         if ((buf = con.getReadBuffer()) == null) {
            buf = this.READ_BUFFER;
         }

         if (buf.position() == buf.limit()) {
            _log.log(
               Level.SEVERE,
               "Read buffer exhausted for client : "
                  + con.getClient()
                  + ", try to adjust buffer size, current : "
                  + buf.capacity()
                  + ", primary : "
                  + (buf == this.READ_BUFFER)
                  + ". Closing connection."
            );
            this.closeConnectionImpl(con);
         } else {
            try {
               result = con.getReadableByteChannel().read(buf);
            } catch (IOException var6) {
            }

            if (result > 0) {
               ((Buffer)buf).flip();
               this._stats.increaseIncomingBytes(result);
               int i = 0;

               while(this.tryReadPacket2(key, con, buf)) {
                  ++i;
               }
            } else if (result == 0) {
               this.closeConnectionImpl(con);
            } else if (result == -1) {
               this.closeConnectionImpl(con);
            } else {
               con.onForcedDisconnection();
               this.closeConnectionImpl(con);
            }
         }

         if (buf == this.READ_BUFFER) {
            ((Buffer)buf).clear();
         }
      }
   }

   protected boolean tryReadPacket2(SelectionKey key, MMOConnection<T> con, ByteBuffer buf) {
      if (con.isClosed()) {
         return false;
      } else {
         int pos = buf.position();
         if (buf.remaining() > this._sc.HEADER_SIZE) {
            int size = buf.getShort() & '\uffff';
            if (size <= this._sc.HEADER_SIZE || size > this._sc.PACKET_SIZE) {
               _log.log(Level.SEVERE, "Incorrect packet size : " + size + "! Client : " + con.getClient() + ". Closing connection.");
               this.closeConnectionImpl(con);
               return false;
            }

            size -= this._sc.HEADER_SIZE;
            if (size <= buf.remaining()) {
               this._stats.increaseIncomingPacketsCount();
               this.parseClientPacket(this.getPacketHandler(), buf, size, con);
               ((Buffer)buf).position(pos + size + this._sc.HEADER_SIZE);
               if (!buf.hasRemaining()) {
                  this.freeBuffer(buf, con);
                  return false;
               }

               return true;
            }

            ((Buffer)buf).position(pos);
         }

         if (pos == buf.capacity()) {
            _log.warning(
               "Read buffer exhausted for client : "
                  + con.getClient()
                  + ", try to adjust buffer size, current : "
                  + buf.capacity()
                  + ", primary : "
                  + (buf == this.READ_BUFFER)
                  + "."
            );
         }

         if (buf == this.READ_BUFFER) {
            this.allocateReadBuffer(con);
         } else {
            buf.compact();
         }

         return false;
      }
   }

   protected void allocateReadBuffer(MMOConnection<T> con) {
      con.setReadBuffer(this.getPooledBuffer().put(this.READ_BUFFER));
      ((Buffer)this.READ_BUFFER).clear();
   }

   protected boolean parseClientPacket(IPacketHandler<T> handler, ByteBuffer buf, int dataSize, MMOConnection<T> con) {
      T client = con.getClient();
      int pos = buf.position();
      client.decrypt(buf, dataSize);
      ((Buffer)buf).position(pos);
      if (buf.hasRemaining()) {
         int limit = buf.limit();
         ((Buffer)buf).limit(pos + dataSize);
         ReceivablePacket<T> rp = handler.handlePacket(buf, client);
         if (rp != null) {
            rp.setByteBuffer(buf);
            rp.setClient(client);
            if (rp.read()) {
               con.recvPacket(rp);
            }

            rp.setByteBuffer(null);
         }

         ((Buffer)buf).limit(limit);
      }

      return true;
   }

   protected void writePacket(SelectionKey key) {
      MMOConnection<T> con = (MMOConnection)key.attachment();
      this.prepareWriteBuffer(con);
      ((Buffer)this.DIRECT_WRITE_BUFFER).flip();
      int size = this.DIRECT_WRITE_BUFFER.remaining();
      int result = -1;

      try {
         result = con.getWritableChannel().write(this.DIRECT_WRITE_BUFFER);
      } catch (IOException var6) {
      }

      if (result >= 0) {
         this._stats.increaseOutgoingBytes(result);
         if (result != size) {
            con.createWriteBuffer(this.DIRECT_WRITE_BUFFER);
         }

         if (con.getSendQueue().isEmpty() && !con.hasPendingWriteBuffer()) {
            con.disableWriteInterest();
         } else {
            con.scheduleWriteInterest();
         }
      } else {
         con.onForcedDisconnection();
         this.closeConnectionImpl(con);
      }
   }

   protected T getWriteClient() {
      return this.WRITE_CLIENT;
   }

   protected ByteBuffer getWriteBuffer() {
      return this.WRITE_BUFFER;
   }

   protected void prepareWriteBuffer(MMOConnection<T> con) {
      this.WRITE_CLIENT = con.getClient();
      ((Buffer)this.DIRECT_WRITE_BUFFER).clear();
      if (con.hasPendingWriteBuffer()) {
         con.movePendingWriteBufferTo(this.DIRECT_WRITE_BUFFER);
      }

      if (this.DIRECT_WRITE_BUFFER.hasRemaining() && !con.hasPendingWriteBuffer()) {
         Queue<SendablePacket<T>> sendQueue = con.getSendQueue();

         for(int i = 0; i < this._sc.MAX_SEND_PER_PASS; ++i) {
            con.lock();

            SendablePacket<T> sp;
            try {
               if ((sp = sendQueue.poll()) == null) {
                  break;
               }
            } finally {
               con.unlock();
            }

            try {
               this._stats.increaseOutgoingPacketsCount();
               this.putPacketIntoWriteBuffer(sp, true);
               ((Buffer)this.WRITE_BUFFER).flip();
               if (this.DIRECT_WRITE_BUFFER.remaining() < this.WRITE_BUFFER.limit()) {
                  con.createWriteBuffer(this.WRITE_BUFFER);
                  break;
               }

               this.DIRECT_WRITE_BUFFER.put(this.WRITE_BUFFER);
            } catch (Exception var9) {
               _log.log(Level.SEVERE, "Error in " + this.getName(), (Throwable)var9);
               break;
            }
         }
      }

      ((Buffer)this.WRITE_BUFFER).clear();
      this.WRITE_CLIENT = null;
   }

   protected final void putPacketIntoWriteBuffer(SendablePacket<T> sp, boolean encrypt) {
      ((Buffer)this.WRITE_BUFFER).clear();
      int headerPos = this.WRITE_BUFFER.position();
      ((Buffer)this.WRITE_BUFFER).position(headerPos + this._sc.HEADER_SIZE);
      sp.write();
      int dataSize = this.WRITE_BUFFER.position() - headerPos - this._sc.HEADER_SIZE;
      if (dataSize == 0) {
         ((Buffer)this.WRITE_BUFFER).position(headerPos);
      } else {
         ((Buffer)this.WRITE_BUFFER).position(headerPos + this._sc.HEADER_SIZE);
         if (encrypt) {
            this.WRITE_CLIENT.encrypt(this.WRITE_BUFFER, dataSize);
            dataSize = this.WRITE_BUFFER.position() - headerPos - this._sc.HEADER_SIZE;
         }

         ((Buffer)this.WRITE_BUFFER).position(headerPos);
         this.WRITE_BUFFER.putShort((short)(this._sc.HEADER_SIZE + dataSize));
         ((Buffer)this.WRITE_BUFFER).position(headerPos + this._sc.HEADER_SIZE + dataSize);
      }
   }

   protected SelectorConfig getConfig() {
      return this._sc;
   }

   protected Selector getSelector() {
      return this._selector;
   }

   protected IMMOExecutor<T> getExecutor() {
      return this._executor;
   }

   protected IPacketHandler<T> getPacketHandler() {
      return this._packetHandler;
   }

   protected IClientFactory<T> getClientFactory() {
      return this._clientFactory;
   }

   protected IAcceptFilter getAcceptFilter() {
      return this._acceptFilter;
   }

   protected void closeConnectionImpl(MMOConnection<T> con) {
      try {
         con.onDisconnection();
      } finally {
         try {
            con.close();
         } catch (IOException var99) {
         } finally {
            try {
               con.releaseBuffers();
               con.clearQueues();
               con.getClient().setConnection((T)null);
               con.getSelectionKey().attach(null);
               con.getSelectionKey().cancel();
            } finally {
               this._connections.remove(con);
               this._stats.decreaseOpenedConnections();
            }
         }
      }
   }

   public void shutdown() {
      this._shutdown = true;
   }

   public boolean isShuttingDown() {
      return this._shutdown;
   }

   protected void closeAllChannels() {
      for(SelectionKey key : this.getSelector().keys()) {
         try {
            key.channel().close();
         } catch (IOException var5) {
         }
      }
   }

   protected void closeSelectorThread() {
      this.closeAllChannels();

      try {
         this.getSelector().close();
      } catch (IOException var2) {
      }
   }
}
