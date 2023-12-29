package l2e.loginserver.network.serverpackets;

import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.loginserver.network.LoginClient;
import org.nio.impl.SendablePacket;

public abstract class LoginServerPacket extends SendablePacket<LoginClient> {
   private static final Logger _log = Logger.getLogger(LoginServerPacket.class.getName());

   @Override
   public final boolean write() {
      try {
         this.writeImpl();
         return true;
      } catch (Exception var2) {
         _log.log(Level.WARNING, "Client: " + this.getClient() + " - Failed writing: " + this.getClass().getSimpleName() + "!", (Throwable)var2);
         return false;
      }
   }

   protected abstract void writeImpl();
}
