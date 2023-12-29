package l2e.gameserver.network.clientpackets;

import java.nio.BufferUnderflowException;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.GameClient;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ActionFail;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.network.serverpackets.SystemMessage;
import org.nio.impl.ReceivablePacket;

public abstract class GameClientPacket extends ReceivablePacket<GameClient> {
   protected static final Logger _log = Logger.getLogger(GameClientPacket.class.getName());

   @Override
   public boolean read() {
      if (!this.getClient().checkFloodProtection(this.getFloodProtectorType(), this.getClass().getSimpleName())) {
         return false;
      } else {
         try {
            this.readImpl();
            return true;
         } catch (BufferUnderflowException var2) {
            this.getClient().onBufferUnderflow();
         } catch (RuntimeException var3) {
            if (Config.PACKET_HANDLER_DEBUG) {
               _log.log(
                  Level.SEVERE,
                  "Client: " + this.getClient().toString() + " - Failed reading: " + this.getType() + " - Revision: " + 2210 + "",
                  (Throwable)var3
               );
            }
         }

         return false;
      }
   }

   protected abstract void readImpl();

   @Override
   public void run() {
      try {
         this.runImpl();
         if (this.triggersOnActionRequest()) {
            Player actor = this.getClient().getActiveChar();
            if (actor != null && (actor.isSpawnProtected() || actor.isInvul())) {
               actor.onActionRequest();
               if (Config.DEBUG) {
                  _log.info("Spawn protection for player " + actor.getName() + " removed by packet: " + this.getType());
               }
            }
         }
      } catch (Throwable var2) {
         _log.log(
            Level.SEVERE,
            "Client: "
               + this.getClient().toString()
               + " - Failed running: "
               + this.getType()
               + " - L2J Eternity-World Server Version: "
               + 2210
               + " ; "
               + var2.getMessage(),
            var2
         );
         if (this instanceof RequestEnterWorld) {
            this.getClient().closeNow();
         }
      }
   }

   protected abstract void runImpl();

   protected final void sendPacket(GameServerPacket gsp) {
      this.getClient().sendPacket(gsp);
   }

   public void sendPacket(SystemMessageId id) {
      this.sendPacket(SystemMessage.getSystemMessage(id));
   }

   protected boolean triggersOnActionRequest() {
      return true;
   }

   protected final Player getActiveChar() {
      return this.getClient().getActiveChar();
   }

   protected final void sendActionFailed() {
      if (this.getClient() != null) {
         this.getClient().sendPacket(ActionFail.STATIC_PACKET);
      }
   }

   protected String getFloodProtectorType() {
      return this.getClass().getSimpleName();
   }

   public String getType() {
      return "[C] " + this.getClass().getSimpleName();
   }
}
