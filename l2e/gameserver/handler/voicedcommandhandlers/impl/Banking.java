package l2e.gameserver.handler.voicedcommandhandlers.impl;

import l2e.gameserver.Config;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.model.actor.Player;

public class Banking implements IVoicedCommandHandler {
   private static final String[] _voicedCommands = new String[]{"bank", "withdraw", "deposit"};

   @Override
   public boolean useVoicedCommand(String command, Player activeChar, String params) {
      if (!Config.BANKING_SYSTEM_ENABLED) {
         return false;
      } else {
         if (command.equals("bank")) {
            activeChar.sendMessage(
               ".deposit ("
                  + Config.BANKING_SYSTEM_ADENA
                  + " Adena = "
                  + Config.BANKING_SYSTEM_GOLDBARS
                  + " Goldbar) / .withdraw ("
                  + Config.BANKING_SYSTEM_GOLDBARS
                  + " Goldbar = "
                  + Config.BANKING_SYSTEM_ADENA
                  + " Adena)"
            );
         } else if (command.equals("deposit")) {
            if (activeChar.getInventory().getInventoryItemCount(57, 0) >= (long)Config.BANKING_SYSTEM_ADENA) {
               if (!activeChar.reduceAdena("Goldbar", (long)Config.BANKING_SYSTEM_ADENA, activeChar, false)) {
                  return false;
               }

               activeChar.getInventory().addItem("Goldbar", 2807, (long)Config.BANKING_SYSTEM_GOLDBARS, activeChar, null);
               activeChar.getInventory().updateDatabase();
               activeChar.sendMessage(
                  "Thank you, you now have " + Config.BANKING_SYSTEM_GOLDBARS + " Goldbar(s), and " + Config.BANKING_SYSTEM_ADENA + " less adena."
               );
            } else {
               activeChar.sendMessage("You do not have enough Adena to convert to Goldbar(s), you need " + Config.BANKING_SYSTEM_ADENA + " Adena.");
            }
         } else if (command.equals("withdraw")) {
            if (activeChar.getInventory().getInventoryItemCount(2807, 0) >= (long)Config.BANKING_SYSTEM_GOLDBARS) {
               if (!activeChar.destroyItemByItemId("Adena", 2807, (long)Config.BANKING_SYSTEM_GOLDBARS, activeChar, false)) {
                  return false;
               }

               activeChar.getInventory().addAdena("Adena", (long)Config.BANKING_SYSTEM_ADENA, activeChar, null);
               activeChar.getInventory().updateDatabase();
               activeChar.sendMessage(
                  "Thank you, you now have " + Config.BANKING_SYSTEM_ADENA + " Adena, and " + Config.BANKING_SYSTEM_GOLDBARS + " less Goldbar(s)."
               );
            } else {
               activeChar.sendMessage("You do not have any Goldbars to turn into " + Config.BANKING_SYSTEM_ADENA + " Adena.");
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
