package l2e.gameserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.SevenSignsFestival;
import l2e.gameserver.instancemanager.DoubleSessionManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.type.FunPvpZone;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.gameserver.taskmanager.AttackStanceTaskManager;

public final class SendLogOut extends GameClientPacket {
   protected static final Logger _logAccounting = Logger.getLogger("accounting");

   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         if (player.getActiveEnchantItemId() == -1 && player.getActiveEnchantAttrItemId() == -1) {
            if (player.isLocked()) {
               _log.warning("Player " + player.getName() + " tried to logout during class change.");
               player.sendActionFailed();
            } else if (player.isInFightEvent()) {
               player.sendMessage("Leave Fight Event first!");
               player.sendActionFailed();
            } else if (player.isBlocked()) {
               player.sendActionFailed();
            } else {
               if (player.isInsideZone(ZoneId.FUN_PVP)) {
                  FunPvpZone zone = ZoneManager.getInstance().getZone(player, FunPvpZone.class);
                  if (zone != null && zone.isNoLogoutZone()) {
                     player.sendMessage("You cannot logout while inside at this zone.");
                     player.sendActionFailed();
                     return;
                  }
               }

               if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(player)) {
                  if (!player.isGM() || !Config.GM_RESTART_FIGHTING) {
                     if (Config.DEBUG) {
                        _log.fine("Player " + player.getName() + " tried to logout while fighting.");
                     }

                     player.sendPacket(SystemMessageId.CANT_LOGOUT_WHILE_FIGHTING);
                     player.sendActionFailed();
                  }
               } else {
                  if (player.isFestivalParticipant()) {
                     if (SevenSignsFestival.getInstance().isFestivalInitialized()) {
                        player.sendMessage("You cannot log out while you are a participant in a Festival.");
                        player.sendActionFailed();
                        return;
                     }

                     if (player.isInParty()) {
                        player.getParty().broadCast(SystemMessage.sendString(player.getName() + " has been removed from the upcoming Festival."));
                     }
                  }

                  player.removeFromBossZone();
                  DoubleSessionManager.getInstance().onDisconnect(player);
                  LogRecord record = new LogRecord(Level.INFO, "Disconnected");
                  record.setParameters(new Object[]{this.getClient()});
                  _logAccounting.log(record);
                  player.logout();
               }
            }
         } else {
            if (Config.DEBUG) {
               _log.fine("Player " + player.getName() + " tried to logout while enchanting.");
            }

            player.sendActionFailed();
         }
      }
   }
}
