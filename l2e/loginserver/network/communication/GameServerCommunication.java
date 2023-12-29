package l2e.loginserver.network.communication;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.loginserver.ThreadPoolManager;

public class GameServerCommunication extends Thread {
   private static final Logger _log = Logger.getLogger(GameServerCommunication.class.getName());
   private static final GameServerCommunication instance = new GameServerCommunication();
   private final ByteBuffer _writeBuffer = ByteBuffer.allocate(65536).order(ByteOrder.LITTLE_ENDIAN);
   private Selector _selector;
   private boolean _shutdown;

   public static GameServerCommunication getInstance() {
      return instance;
   }

   private GameServerCommunication() {
   }

   public void openServerSocket(InetAddress address, int tcpPort) throws IOException {
      this._selector = Selector.open();
      ServerSocketChannel selectable = ServerSocketChannel.open();
      selectable.configureBlocking(false);
      selectable.socket().bind(address == null ? new InetSocketAddress(tcpPort) : new InetSocketAddress(address, tcpPort));
      selectable.register(this._selector, selectable.validOps());
   }

   @Override
   public void run() {
      SelectionKey key = null;

      while(!this.isShutdown()) {
         try {
            this._selector.select();
            Set<SelectionKey> keys = this._selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();

            while(iterator.hasNext()) {
               key = iterator.next();
               iterator.remove();
               if (!key.isValid()) {
                  this.close(key);
               } else {
                  int opts = key.readyOps();
                  switch(opts) {
                     case 1:
                        this.read(key);
                        break;
                     case 4:
                        this.write(key);
                        break;
                     case 5:
                        this.write(key);
                        this.read(key);
                        break;
                     case 8:
                        this.close(key);
                        break;
                     case 16:
                        this.accept(key);
                  }
               }
            }
         } catch (ClosedSelectorException var6) {
            _log.warning("Selector " + this._selector + " closed!");
            return;
         } catch (IOException var7) {
            _log.warning("Gameserver disconnected...");
            this.close(key);
         } catch (Exception var8) {
            _log.log(Level.WARNING, "", (Throwable)var8);
         }
      }
   }

   public void accept(SelectionKey key) throws IOException {
      ServerSocketChannel ssc = (ServerSocketChannel)key.channel();
      SocketChannel sc = ssc.accept();
      sc.configureBlocking(false);
      SelectionKey clientKey = sc.register(this._selector, 1);
      GameServerConnection conn;
      clientKey.attach(conn = new GameServerConnection(clientKey));
      conn.setGameServer(new GameServer(conn));
   }

   public void read(SelectionKey key) throws IOException {
      SocketChannel channel = (SocketChannel)key.channel();
      GameServerConnection conn = (GameServerConnection)key.attachment();
      GameServer gs = conn.getGameServer();
      ByteBuffer buf = conn.getReadBuffer();
      int count = channel.read(buf);
      if (count == -1) {
         this.close(key);
      } else if (count != 0) {
         ((Buffer)buf).flip();

         while(this.tryReadPacket(key, gs, buf)) {
         }
      }
   }

   protected boolean tryReadPacket(SelectionKey key, GameServer gs, ByteBuffer buf) throws IOException {
      int pos = buf.position();
      if (buf.remaining() > 2) {
         int size = buf.getShort() & '\uffff';
         if (size <= 2) {
            throw new IOException("Incorrect packet size: <= 2");
         }

         size -= 2;
         if (size <= buf.remaining()) {
            int limit = buf.limit();
            ((Buffer)buf).limit(pos + size + 2);
            ReceivablePacket rp = PacketHandler.handlePacket(gs, buf);
            if (rp != null) {
               rp.setByteBuffer(buf);
               rp.setClient(gs);
               if (rp.read()) {
                  ThreadPoolManager.getInstance().execute(rp);
               }

               rp.setByteBuffer(null);
            }

            ((Buffer)buf).limit(limit);
            ((Buffer)buf).position(pos + size + 2);
            if (!buf.hasRemaining()) {
               ((Buffer)buf).clear();
               return false;
            }

            return true;
         }

         ((Buffer)buf).position(pos);
      }

      buf.compact();
      return false;
   }

   public void write(SelectionKey key) throws IOException {
      GameServerConnection conn = (GameServerConnection)key.attachment();
      GameServer gs = conn.getGameServer();
      SocketChannel channel = (SocketChannel)key.channel();
      ByteBuffer buf = this.getWriteBuffer();
      conn.disableWriteInterest();
      Queue<SendablePacket> sendQueue = conn._sendQueue;
      Lock sendLock = conn._sendLock;
      sendLock.lock();

      boolean done;
      try {
         int i = 0;

         SendablePacket sp;
         while(i++ < 64 && (sp = sendQueue.poll()) != null) {
            int headerPos = buf.position();
            ((Buffer)buf).position(headerPos + 2);
            sp.setByteBuffer(buf);
            sp.setClient(gs);
            sp.write();
            int dataSize = buf.position() - headerPos - 2;
            if (dataSize == 0) {
               ((Buffer)buf).position(headerPos);
            } else {
               ((Buffer)buf).position(headerPos);
               buf.putShort((short)(dataSize + 2));
               ((Buffer)buf).position(headerPos + dataSize + 2);
            }
         }

         done = sendQueue.isEmpty();
         if (done) {
            conn.disableWriteInterest();
         }
      } finally {
         sendLock.unlock();
      }

      ((Buffer)buf).flip();
      channel.write(buf);
      if (buf.remaining() > 0) {
         buf.compact();
         done = false;
      } else {
         ((Buffer)buf).clear();
      }

      if (!done && conn.enableWriteInterest()) {
         this._selector.wakeup();
      }
   }

   private ByteBuffer getWriteBuffer() {
      return this._writeBuffer;
   }

   public void close(SelectionKey key) {
      if (key != null) {
         try {
            try {
               GameServerConnection conn = (GameServerConnection)key.attachment();
               if (conn != null) {
                  conn.onDisconnection();
               }
            } finally {
               key.channel().close();
               key.cancel();
            }
         } catch (IOException var7) {
            _log.log(Level.WARNING, "", (Throwable)var7);
         }
      }
   }

   public boolean isShutdown() {
      return this._shutdown;
   }

   public void setShutdown(boolean shutdown) {
      this._shutdown = shutdown;
   }
}
