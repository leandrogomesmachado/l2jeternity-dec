package l2e.gameserver.handler.admincommandhandlers.impl;

import l2e.gameserver.SevenSigns;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.AutoSpawnHandler;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;

public class Mammon implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_mammon_find", "admin_mammon_respawn"};
   private final boolean _isSealValidation = SevenSigns.getInstance().isSealValidationPeriod();

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      int teleportIndex = -1;
      AutoSpawnHandler.AutoSpawnInstance blackSpawnInst = AutoSpawnHandler.getInstance().getAutoSpawnInstance(31126, false);
      AutoSpawnHandler.AutoSpawnInstance merchSpawnInst = AutoSpawnHandler.getInstance().getAutoSpawnInstance(31113, false);
      if (command.startsWith("admin_mammon_find")) {
         try {
            if (command.length() > 17) {
               teleportIndex = Integer.parseInt(command.substring(18));
            }
         } catch (Exception var8) {
            activeChar.sendMessage("Usage: //mammon_find [teleportIndex] (where 1 = Blacksmith, 2 = Merchant)");
            return false;
         }

         if (!this._isSealValidation) {
            activeChar.sendPacket(SystemMessageId.SSQ_COMPETITION_UNDERWAY);
            return false;
         }

         if (blackSpawnInst != null) {
            Npc blackInst = blackSpawnInst.getNPCInstanceList().peek();
            if (blackInst != null) {
               activeChar.sendMessage("Blacksmith of Mammon: " + blackInst.getX() + " " + blackInst.getY() + " " + blackInst.getZ());
               if (teleportIndex == 1) {
                  activeChar.teleToLocation(blackInst.getLocation(), true);
               }
            }
         } else {
            activeChar.sendMessage("Blacksmith of Mammon isn't registered for spawn.");
         }

         if (merchSpawnInst != null) {
            Npc merchInst = merchSpawnInst.getNPCInstanceList().peek();
            if (merchInst != null) {
               activeChar.sendMessage("Merchant of Mammon: " + merchInst.getX() + " " + merchInst.getY() + " " + merchInst.getZ());
               if (teleportIndex == 2) {
                  activeChar.teleToLocation(merchInst.getLocation(), true);
               }
            }
         } else {
            activeChar.sendMessage("Merchant of Mammon isn't registered for spawn.");
         }
      } else if (command.startsWith("admin_mammon_respawn")) {
         if (!this._isSealValidation) {
            activeChar.sendPacket(SystemMessageId.SSQ_COMPETITION_UNDERWAY);
            return false;
         }

         if (merchSpawnInst != null) {
            long merchRespawn = AutoSpawnHandler.getInstance().getTimeToNextSpawn(merchSpawnInst);
            activeChar.sendMessage("The Merchant of Mammon will respawn in " + merchRespawn / 60000L + " minute(s).");
         } else {
            activeChar.sendMessage("Merchant of Mammon isn't registered for spawn.");
         }

         if (blackSpawnInst != null) {
            long blackRespawn = AutoSpawnHandler.getInstance().getTimeToNextSpawn(blackSpawnInst);
            activeChar.sendMessage("The Blacksmith of Mammon will respawn in " + blackRespawn / 60000L + " minute(s).");
         } else {
            activeChar.sendMessage("Blacksmith of Mammon isn't registered for spawn.");
         }
      }

      return true;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
