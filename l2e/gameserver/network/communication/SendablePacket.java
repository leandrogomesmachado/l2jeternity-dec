package l2e.gameserver.network.communication;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class SendablePacket extends org.nio.SendablePacket<AuthServerCommunication> {
   public static final Logger _log = Logger.getLogger(SendablePacket.class.getName());

   public AuthServerCommunication getClient() {
      return AuthServerCommunication.getInstance();
   }

   @Override
   protected ByteBuffer getByteBuffer() {
      return this.getClient().getWriteBuffer();
   }

   @Override
   public boolean write() {
      try {
         this.writeImpl();
      } catch (Exception var2) {
         _log.log(Level.WARNING, "", (Throwable)var2);
      }

      return true;
   }

   protected abstract void writeImpl();
}
