package l2e.gameserver.network.communication;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.net.IPSettings;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.network.GameClient;
import l2e.gameserver.network.communication.gameserverpackets.AuthRequest;
import org.HostInfo;
import org.apache.commons.lang3.StringUtils;

public class AuthServerCommunication extends Thread {
   private static final Logger _log = Logger.getLogger(AuthServerCommunication.class.getName());
   private static final AuthServerCommunication instance = new AuthServerCommunication();
   private final Map<String, GameClient> _waitingClients = new HashMap<>();
   private final Map<String, GameClient> _authedClients = new HashMap<>();
   private final ReadWriteLock _lock = new ReentrantReadWriteLock();
   private final Lock _readLock = this._lock.readLock();
   private final Lock _writeLock = this._lock.writeLock();
   private final ByteBuffer _readBuffer = ByteBuffer.allocate(65536).order(ByteOrder.LITTLE_ENDIAN);
   private final ByteBuffer _writeBuffer = ByteBuffer.allocate(65536).order(ByteOrder.LITTLE_ENDIAN);
   private final Queue<SendablePacket> _sendQueue = new ArrayDeque<>();
   private final Lock _sendLock = new ReentrantLock();
   private final AtomicBoolean _isPengingWrite = new AtomicBoolean();
   private SelectionKey _key;
   private Selector _selector;
   private boolean _shutdown;
   private boolean _restart;

   public static final AuthServerCommunication getInstance() {
      return instance;
   }

   private AuthServerCommunication() {
      try {
         this._selector = Selector.open();
      } catch (IOException var2) {
         _log.log(Level.WARNING, "", (Throwable)var2);
      }
   }

   private void connect() throws IOException {
      HostInfo hostInfo = IPSettings.getInstance().getAuthServerHost();
      _log.info("Connecting to authserver on " + hostInfo.getAddress() + ":" + hostInfo.getPort());
      SocketChannel channel = SocketChannel.open();
      channel.configureBlocking(false);
      this._key = channel.register(this._selector, 8);
      channel.connect(new InetSocketAddress(hostInfo.getAddress(), hostInfo.getPort()));
   }

   public void sendPacket(SendablePacket packet) {
      if (!this.isShutdown()) {
         this._sendLock.lock();

         boolean wakeUp;
         label40: {
            try {
               this._sendQueue.add(packet);
               wakeUp = this.enableWriteInterest();
               break label40;
            } catch (CancelledKeyException var7) {
            } finally {
               this._sendLock.unlock();
            }

            return;
         }

         if (wakeUp) {
            this._selector.wakeup();
         }
      }
   }

   private boolean disableWriteInterest() throws CancelledKeyException {
      if (this._isPengingWrite.compareAndSet(true, false)) {
         this._key.interestOps(this._key.interestOps() & -5);
         return true;
      } else {
         return false;
      }
   }

   private boolean enableWriteInterest() throws CancelledKeyException {
      if (!this._isPengingWrite.getAndSet(true)) {
         this._key.interestOps(this._key.interestOps() | 4);
         return true;
      } else {
         return false;
      }
   }

   protected ByteBuffer getReadBuffer() {
      return this._readBuffer;
   }

   protected ByteBuffer getWriteBuffer() {
      return this._writeBuffer;
   }

   @Override
   public void run() {
      while(!this._shutdown) {
         this._restart = false;

         try {
            label57:
            while(!this.isShutdown()) {
               this.connect();
               this._selector.select(5000L);
               Set<SelectionKey> keys = this._selector.selectedKeys();
               if (keys.isEmpty()) {
                  throw new IOException("Connection timeout.");
               }

               Iterator<SelectionKey> iterator = keys.iterator();

               try {
                  while(iterator.hasNext()) {
                     SelectionKey key = iterator.next();
                     iterator.remove();
                     int opts = key.readyOps();
                     switch(opts) {
                        case 8:
                           this.connect(key);
                           break label57;
                     }
                  }
               } catch (CancelledKeyException var7) {
                  break;
               }
            }

            while(!this.isShutdown()) {
               this._selector.select();
               Set<SelectionKey> keys = this._selector.selectedKeys();
               Iterator<SelectionKey> iterator = keys.iterator();

               try {
                  while(iterator.hasNext()) {
                     SelectionKey key = iterator.next();
                     iterator.remove();
                     int opts = key.readyOps();
                     switch(opts) {
                        case 1:
                           this.read(key);
                        case 2:
                        case 3:
                        default:
                           break;
                        case 4:
                           this.write(key);
                           break;
                        case 5:
                           this.write(key);
                           this.read(key);
                     }
                  }
               } catch (CancelledKeyException var8) {
                  break;
               }
            }
         } catch (IOException var9) {
            _log.warning("LoginServer not avaible, trying to reconnect...");
         }

         this.close();

         try {
            Thread.sleep(5000L);
         } catch (InterruptedException var6) {
         }
      }
   }

   private void read(SelectionKey key) throws IOException {
      SocketChannel channel = (SocketChannel)key.channel();
      ByteBuffer buf = this.getReadBuffer();
      int count = channel.read(buf);
      if (count == -1) {
         throw new IOException("End of stream.");
      } else if (count != 0) {
         ((Buffer)buf).flip();

         while(this.tryReadPacket(key, buf)) {
         }
      }
   }

   private boolean tryReadPacket(SelectionKey key, ByteBuffer buf) throws IOException {
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
            ReceivablePacket rp = PacketHandler.handlePacket(buf);
            if (rp != null && rp.read()) {
               ThreadPoolManager.getInstance().execute(rp);
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

   private void write(SelectionKey key) throws IOException {
      SocketChannel channel = (SocketChannel)key.channel();
      ByteBuffer buf = this.getWriteBuffer();
      this._sendLock.lock();

      boolean done;
      try {
         int i = 0;

         SendablePacket sp;
         while(i++ < 64 && (sp = this._sendQueue.poll()) != null) {
            int headerPos = buf.position();
            ((Buffer)buf).position(headerPos + 2);
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

         done = this._sendQueue.isEmpty();
         if (done) {
            this.disableWriteInterest();
         }
      } finally {
         this._sendLock.unlock();
      }

      ((Buffer)buf).flip();
      channel.write(buf);
      if (buf.remaining() > 0) {
         buf.compact();
         done = false;
      } else {
         ((Buffer)buf).clear();
      }

      if (!done && this.enableWriteInterest()) {
         this._selector.wakeup();
      }
   }

   private void connect(SelectionKey key) throws IOException {
      SocketChannel channel = (SocketChannel)key.channel();
      channel.finishConnect();
      key.interestOps(key.interestOps() & -9);
      key.interestOps(key.interestOps() | 1);
      this.sendPacket(new AuthRequest());
   }

   private void close() {
      this._restart = !this._shutdown;
      this._sendLock.lock();

      try {
         this._sendQueue.clear();
      } finally {
         this._sendLock.unlock();
      }

      ((Buffer)this._readBuffer).clear();
      ((Buffer)this._writeBuffer).clear();
      this._isPengingWrite.set(false);

      try {
         if (this._key != null) {
            this._key.channel().close();
            this._key.cancel();
         }
      } catch (IOException var10) {
      }

      this._writeLock.lock();

      try {
         this._waitingClients.clear();
      } finally {
         this._writeLock.unlock();
      }
   }

   public void shutdown() {
      this._shutdown = true;
      this._selector.wakeup();
   }

   public boolean isShutdown() {
      return this._shutdown || this._restart;
   }

   public void restart() {
      this._restart = true;
      this._selector.wakeup();
   }

   public GameClient addWaitingClient(GameClient client) {
      this._writeLock.lock();

      GameClient var2;
      try {
         var2 = this._waitingClients.put(client.getLogin(), client);
      } finally {
         this._writeLock.unlock();
      }

      return var2;
   }

   public GameClient removeWaitingClient(String account) {
      this._writeLock.lock();

      GameClient var2;
      try {
         var2 = this._waitingClients.remove(account);
      } finally {
         this._writeLock.unlock();
      }

      return var2;
   }

   public GameClient addAuthedClient(GameClient client) {
      this._writeLock.lock();

      GameClient var2;
      try {
         var2 = this._authedClients.put(client.getLogin(), client);
      } finally {
         this._writeLock.unlock();
      }

      return var2;
   }

   public GameClient removeAuthedClient(String login) {
      this._writeLock.lock();

      GameClient var2;
      try {
         var2 = this._authedClients.remove(login);
      } finally {
         this._writeLock.unlock();
      }

      return var2;
   }

   public GameClient getAuthedClient(String login) {
      this._readLock.lock();

      GameClient var2;
      try {
         var2 = this._authedClients.get(login);
      } finally {
         this._readLock.unlock();
      }

      return var2;
   }

   public List<GameClient> getAuthedClientsByIP(String ip) {
      List<GameClient> clients = new ArrayList<>();
      this._readLock.lock();

      try {
         for(GameClient client : this._authedClients.values()) {
            if (client.getConnectionAddress().getHostAddress().equalsIgnoreCase(ip)) {
               clients.add(client);
            }
         }
      } finally {
         this._readLock.unlock();
      }

      return clients;
   }

   public List<GameClient> getAuthedClientsByHWID(String hwid) {
      List<GameClient> clients = new ArrayList<>();
      if (StringUtils.isEmpty(hwid)) {
         return clients;
      } else {
         this._readLock.lock();

         try {
            for(GameClient client : this._authedClients.values()) {
               String h = client.getHWID();
               if (!StringUtils.isEmpty(h) && h.equalsIgnoreCase(hwid)) {
                  clients.add(client);
               }
            }
         } finally {
            this._readLock.unlock();
         }

         return clients;
      }
   }

   public GameClient removeClient(GameClient client) {
      this._writeLock.lock();

      GameClient var2;
      try {
         if (!client.isAuthed()) {
            return this._waitingClients.remove(client.getLogin());
         }

         var2 = this._authedClients.remove(client.getLogin());
      } finally {
         this._writeLock.unlock();
      }

      return var2;
   }

   public String[] getAccounts() {
      this._readLock.lock();

      String[] var1;
      try {
         var1 = this._authedClients.keySet().toArray(new String[this._authedClients.size()]);
      } finally {
         this._readLock.unlock();
      }

      return var1;
   }
}
