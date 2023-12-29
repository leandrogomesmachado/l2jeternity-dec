package l2e.gameserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.SevenSignsFestival;
import l2e.gameserver.instancemanager.DoubleSessionManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.type.FunPvpZone;
import l2e.gameserver.network.GameClient;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CharacterSelectionInfo;
import l2e.gameserver.network.serverpackets.RestartResponse;
import l2e.gameserver.taskmanager.AttackStanceTaskManager;

public final class RequestRestart extends GameClientPacket {
   protected static final Logger _logAccounting = Logger.getLogger("accounting");

   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         if (player.getActiveEnchantItemId() != -1 || player.getActiveEnchantAttrItemId() != -1) {
            this.sendPacket(RestartResponse.valueOf(false));
         } else if (player.isLocked()) {
            _log.warning("Player " + player.getName() + " tried to restart during class change.");
            this.sendPacket(RestartResponse.valueOf(false));
         } else if (player.getPrivateStoreType() != 0) {
            player.sendMessage("Cannot restart while trading");
            this.sendPacket(RestartResponse.valueOf(false));
         } else {
            if (player.isInsideZone(ZoneId.FUN_PVP)) {
               FunPvpZone zone = ZoneManager.getInstance().getZone(player, FunPvpZone.class);
               if (zone != null && zone.isNoRestartZone()) {
                  player.sendMessage("You cannot restart while inside at this zone.");
                  this.sendPacket(RestartResponse.valueOf(false));
                  return;
               }
            }

            if (!AttackStanceTaskManager.getInstance().hasAttackStanceTask(player) || player.isGM() && Config.GM_RESTART_FIGHTING) {
               if (player.isBlocked()) {
                  player.sendMessage("You are blocked!");
                  player.sendPacket(RestartResponse.valueOf(false));
               } else if (player.isInFightEvent()) {
                  player.sendMessage("You need to leave Fight Event first!");
                  this.sendPacket(RestartResponse.valueOf(false));
               } else {
                  if (player.isFestivalParticipant()) {
                     if (SevenSignsFestival.getInstance().isFestivalInitialized()) {
                        player.sendMessage("You cannot restart while you are a participant in a festival.");
                        this.sendPacket(RestartResponse.valueOf(false));
                        return;
                     }

                     Party playerParty = player.getParty();
                     if (playerParty != null) {
                        player.getParty().broadcastString(player.getName() + " has been removed from the upcoming festival.");
                     }
                  }

                  if (player.isBlockedFromExit()) {
                     this.sendPacket(RestartResponse.valueOf(false));
                  } else if (player.isInOlympiadMode() && !Config.ALLOW_RESTART_AT_OLY) {
                     player.sendMessage("You cannot restart while you are a participant in olympiad!");
                     this.sendPacket(RestartResponse.valueOf(false));
                  } else {
                     player.removeFromBossZone();
                     DoubleSessionManager.getInstance().onDisconnect(player);
                     GameClient client = this.getClient();
                     LogRecord record = new LogRecord(Level.INFO, "Logged out");
                     record.setParameters(new Object[]{client});
                     _logAccounting.log(record);
                     player.setClient(null);
                     player.deleteMe();
                     client.setActiveChar(null);
                     client.setState(GameClient.GameClientState.AUTHED);
                     this.sendPacket(RestartResponse.valueOf(true));
                     CharacterSelectionInfo cl = new CharacterSelectionInfo(client.getLogin(), client.getSessionId().playOkID1);
                     this.sendPacket(cl);
                     client.setCharSelection(cl.getCharInfo());
                  }
               }
            } else {
               if (Config.DEBUG) {
                  _log.fine("Player " + player.getName() + " tried to logout while fighting.");
               }

               player.sendPacket(SystemMessageId.CANT_RESTART_WHILE_FIGHTING);
               this.sendPacket(RestartResponse.valueOf(false));
            }
         }
      }
   }
}
