package l2e.loginserver.network.communication;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.commons.threading.RunnableImpl;
import l2e.loginserver.Config;
import l2e.loginserver.ThreadPoolManager;
import l2e.loginserver.network.communication.loginserverpackets.PingRequest;

public class GameServerConnection {
   private static final Logger _log = Logger.getLogger(GameServerConnection.class.getName());
   final ByteBuffer _readBuffer = ByteBuffer.allocate(65536).order(ByteOrder.LITTLE_ENDIAN);
   final Queue<SendablePacket> _sendQueue = new ArrayDeque<>();
   final Lock _sendLock = new ReentrantLock();
   final AtomicBoolean _isPengingWrite = new AtomicBoolean();
   private final Selector _selector;
   private final SelectionKey _key;
   private GameServer _gameServer;
   private Future<?> _pingTask;
   private int _pingRetry;

   public GameServerConnection(SelectionKey key) {
      this._key = key;
      this._selector = key.selector();
   }

   public void sendPacket(SendablePacket packet) {
      this._sendLock.lock();

      boolean wakeUp;
      label36: {
         try {
            this._sendQueue.add(packet);
            wakeUp = this.enableWriteInterest();
            break label36;
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

   protected boolean disableWriteInterest() throws CancelledKeyException {
      if (this._isPengingWrite.compareAndSet(true, false)) {
         this._key.interestOps(this._key.interestOps() & -5);
         return true;
      } else {
         return false;
      }
   }

   protected boolean enableWriteInterest() throws CancelledKeyException {
      if (!this._isPengingWrite.getAndSet(true)) {
         this._key.interestOps(this._key.interestOps() | 4);
         return true;
      } else {
         return false;
      }
   }

   public void closeNow() {
      this._key.interestOps(8);
      this._selector.wakeup();
   }

   public void onDisconnection() {
      try {
         this.stopPingTask();
         ((Buffer)this._readBuffer).clear();
         this._sendLock.lock();

         try {
            this._sendQueue.clear();
         } finally {
            this._sendLock.unlock();
         }

         this._isPengingWrite.set(false);
         if (this._gameServer != null && this._gameServer.isAuthed()) {
            _log.info("Connection with gameserver IP[" + this.getIpAddress() + "] lost.");
            _log.info("Setting gameserver down.");
            this._gameServer.setDown();
         }

         this._gameServer = null;
      } catch (Exception var5) {
         _log.log(Level.WARNING, "", (Throwable)var5);
      }
   }

   ByteBuffer getReadBuffer() {
      return this._readBuffer;
   }

   GameServer getGameServer() {
      return this._gameServer;
   }

   void setGameServer(GameServer gameServer) {
      this._gameServer = gameServer;
   }

   public String getIpAddress() {
      return ((SocketChannel)this._key.channel()).socket().getInetAddress().getHostAddress();
   }

   public void onPingResponse() {
      this._pingRetry = 0;
   }

   public void startPingTask() {
      if (Config.GAME_SERVER_PING_DELAY != 0L) {
         this._pingTask = ThreadPoolManager.getInstance()
            .scheduleAtFixedRate(new GameServerConnection.PingTask(), Config.GAME_SERVER_PING_DELAY, Config.GAME_SERVER_PING_DELAY);
      }
   }

   public void stopPingTask() {
      if (this._pingTask != null) {
         this._pingTask.cancel(false);
         this._pingTask = null;
      }
   }

   private class PingTask extends RunnableImpl {
      private PingTask() {
      }

      @Override
      public void runImpl() {
         if (Config.GAME_SERVER_PING_RETRY > 0 && GameServerConnection.this._pingRetry > Config.GAME_SERVER_PING_RETRY) {
            GameServerConnection._log.warning("Gameserver IP[" + GameServerConnection.this.getIpAddress() + "]: ping timeout!");
            GameServerConnection.this.closeNow();
         } else {
            GameServerConnection.this._pingRetry++;
            GameServerConnection.this.sendPacket(new PingRequest());
         }
      }
   }
}
