package l2e.loginserver.network.communication;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ReceivablePacket extends org.nio.ReceivablePacket<GameServer> {
   public static final Logger _log = Logger.getLogger(ReceivablePacket.class.getName());
   protected GameServer _gs;
   protected ByteBuffer _buf;

   protected void setByteBuffer(ByteBuffer buf) {
      this._buf = buf;
   }

   @Override
   protected ByteBuffer getByteBuffer() {
      return this._buf;
   }

   protected void setClient(GameServer gs) {
      this._gs = gs;
   }

   public GameServer getClient() {
      return this._gs;
   }

   public GameServer getGameServer() {
      return this.getClient();
   }

   @Override
   public final boolean read() {
      try {
         this.readImpl();
      } catch (Exception var2) {
         _log.log(Level.WARNING, "", (Throwable)var2);
      }

      return true;
   }

   @Override
   public final void run() {
      try {
         this.runImpl();
      } catch (Exception var2) {
         _log.log(Level.WARNING, "", (Throwable)var2);
      }
   }

   protected abstract void readImpl();

   protected abstract void runImpl();

   public void sendPacket(SendablePacket packet) {
      this.getGameServer().sendPacket(packet);
   }
}
