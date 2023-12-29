package l2e.gameserver.handler.voicedcommandhandlers.impl;

import l2e.commons.threading.RunnableImpl;
import l2e.gameserver.Config;
import l2e.gameserver.SevenSignsFestival;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.holder.CharNameHolder;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.type.FunPvpZone;
import l2e.gameserver.network.GameClient;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CharacterSelected;
import l2e.gameserver.network.serverpackets.CharacterSelectionInfo;
import l2e.gameserver.network.serverpackets.RestartResponse;
import l2e.gameserver.taskmanager.AttackStanceTaskManager;

public class Relog implements IVoicedCommandHandler {
   private static String[] _voicedCommands = new String[]{"relog", "restart"};

   @Override
   public boolean useVoicedCommand(String command, Player player, String target) {
      if (!command.equals("relog") && !command.equals("restart")) {
         return false;
      } else if (player != null && Config.ALLOW_RELOG_COMMAND) {
         if (player.getActiveEnchantItemId() != -1 || player.getActiveEnchantAttrItemId() != -1 || player.isLocked() || player.isBlockedFromExit()) {
            return false;
         } else if (player.getPrivateStoreType() != 0) {
            player.sendMessage("Cannot restart while trading");
            return false;
         } else {
            if (player.isInsideZone(ZoneId.FUN_PVP)) {
               FunPvpZone zone = ZoneManager.getInstance().getZone(player, FunPvpZone.class);
               if (zone != null && zone.isNoRestartZone()) {
                  player.sendMessage("You cannot restart while inside at this zone.");
                  return false;
               }
            }

            if (!AttackStanceTaskManager.getInstance().hasAttackStanceTask(player) || player.isGM() && Config.GM_RESTART_FIGHTING) {
               if (player.isBlocked()) {
                  player.sendMessage("You are blocked!");
                  return false;
               } else if (player.isInFightEvent()) {
                  player.sendMessage("You need to leave Fight Event first!");
                  return false;
               } else {
                  if (player.isFestivalParticipant()) {
                     if (SevenSignsFestival.getInstance().isFestivalInitialized()) {
                        player.sendMessage("You cannot restart while you are a participant in a festival.");
                        return false;
                     }

                     Party playerParty = player.getParty();
                     if (playerParty != null) {
                        player.getParty().broadcastString(player.getName() + " has been removed from the upcoming festival.");
                     }
                  }

                  player.removeFromBossZone();
                  final GameClient client = player.getClient();
                  if (client != null && !client.isDetached()) {
                     client.setState(GameClient.GameClientState.AUTHED);
                     synchronized(player) {
                        final int objId = player.getObjectId();
                        Runnable doSelect = new RunnableImpl() {
                           @Override
                           public void runImpl() throws Exception {
                              if (client != null && !client.isDetached()) {
                                 if (!Config.SECOND_AUTH_ENABLED || client.getSecondaryAuth().isAuthed()) {
                                    int slotIdx = client.getSlotForObjectId(objId);
                                    if (slotIdx >= 0) {
                                       Player activeChar = client.loadCharFromDisk(slotIdx);
                                       if (activeChar != null) {
                                          World.getInstance().addToAllPlayers(activeChar);
                                          CharNameHolder.getInstance().addName(activeChar);
                                          activeChar.setClient(client);
                                          client.setActiveChar(activeChar);
                                          activeChar.setOnlineStatus(true, true);
                                          client.setState(GameClient.GameClientState.ENTERING);
                                          client.sendPacket(new CharacterSelected(activeChar, client.getSessionId().playOkID1));
                                       }
                                    }
                                 }
                              }
                           }
                        };
                        player.deleteMe();
                        CharacterSelectionInfo cl = new CharacterSelectionInfo(client.getLogin(), client.getSessionId().playOkID1);
                        client.sendPacket(RestartResponse.valueOf(true));
                        client.setCharSelection(cl.getCharInfo());
                        ThreadPoolManager.getInstance().schedule(doSelect, 333L);
                        return true;
                     }
                  } else {
                     return false;
                  }
               }
            } else {
               player.sendPacket(SystemMessageId.CANT_RESTART_WHILE_FIGHTING);
               return false;
            }
         }
      } else {
         return false;
      }
   }

   @Override
   public String[] getVoicedCommandList() {
      return _voicedCommands;
   }
}
