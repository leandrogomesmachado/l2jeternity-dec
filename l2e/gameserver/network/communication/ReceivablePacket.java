package l2e.gameserver.network.communication;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ReceivablePacket extends org.nio.ReceivablePacket<AuthServerCommunication> {
   public static final Logger _log = Logger.getLogger(ReceivablePacket.class.getName());

   public AuthServerCommunication getClient() {
      return AuthServerCommunication.getInstance();
   }

   @Override
   protected ByteBuffer getByteBuffer() {
      return this.getClient().getReadBuffer();
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

   protected void sendPacket(SendablePacket sp) {
      this.getClient().sendPacket(sp);
   }
}
