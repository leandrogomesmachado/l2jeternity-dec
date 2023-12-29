package l2e.loginserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.loginserver.network.LoginClient;
import org.nio.impl.ReceivablePacket;

public abstract class LoginClientPacket extends ReceivablePacket<LoginClient> {
   private static Logger _log = Logger.getLogger(LoginClientPacket.class.getName());

   @Override
   protected final boolean read() {
      try {
         this.readImpl();
         return true;
      } catch (Exception var2) {
         _log.log(Level.WARNING, "", (Throwable)var2);
         return false;
      }
   }

   @Override
   public void run() {
      try {
         this.runImpl();
      } catch (Exception var2) {
         _log.log(Level.WARNING, "", (Throwable)var2);
      }
   }

   protected abstract void readImpl();

   protected abstract void runImpl() throws Exception;
}
