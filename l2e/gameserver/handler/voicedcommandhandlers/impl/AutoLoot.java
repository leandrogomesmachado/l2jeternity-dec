package l2e.gameserver.handler.voicedcommandhandlers.impl;

import l2e.gameserver.Config;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.strings.server.ServerMessage;

public class AutoLoot implements IVoicedCommandHandler {
   private static final String[] _voicedCommands = new String[]{"autoloot", "autolootherbs"};

   @Override
   public boolean useVoicedCommand(String command, Player activeChar, String target) {
      if (!Config.ALLOW_AUTOLOOT_COMMAND) {
         return false;
      } else {
         if (command.equalsIgnoreCase("autoloot")) {
            if (activeChar.getUseAutoLoot()) {
               activeChar.setVar("useAutoLoot@", "0");
               activeChar.sendMessage(new ServerMessage("AutoLoot.AUTO_LOOT_DISABLED", activeChar.getLang()).toString());
            } else {
               activeChar.setVar("useAutoLoot@", "1");
               activeChar.sendMessage(new ServerMessage("AutoLoot.AUTO_LOOT_ENABLED", activeChar.getLang()).toString());
            }
         } else if (command.equalsIgnoreCase("autolootherbs")) {
            if (activeChar.getUseAutoLootHerbs()) {
               activeChar.setVar("useAutoLootHerbs@", "0");
               activeChar.sendMessage(new ServerMessage("AutoLoot.HERB_AUTO_LOOT_DISABLED", activeChar.getLang()).toString());
            } else {
               activeChar.setVar("useAutoLootHerbs@", "1");
               activeChar.sendMessage(new ServerMessage("AutoLoot.HERB_AUTO_LOOT_ENABLED", activeChar.getLang()).toString());
            }
         }

         return true;
      }
   }

   @Override
   public String[] getVoicedCommandList() {
      return _voicedCommands;
   }
}
