package l2e.gameserver.handler.voicedcommandhandlers.impl;

import l2e.gameserver.Config;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;

public class AncientAdenaExchanger implements IVoicedCommandHandler {
   private static final String[] commands = new String[]{"aa", "makeaa"};
   private static final int ANCIENT_ADENA = 5575;
   private static final int BLUE_SEAL_STONE = 6360;
   private static final int GREEN_SEAL_STONE = 6361;
   private static final int RED_SEAL_STONE = 6362;

   @Override
   public boolean useVoicedCommand(String command, Player activeChar, String params) {
      if (!Config.ALLOW_ANCIENT_EXCHANGER_COMMAND) {
         return false;
      } else {
         if (command.equalsIgnoreCase("aa") || command.equalsIgnoreCase("makeaa")) {
            ItemInstance redStones = activeChar.getInventory().getItemByItemId(6362);
            ItemInstance greenStones = activeChar.getInventory().getItemByItemId(6361);
            ItemInstance blueStones = activeChar.getInventory().getItemByItemId(6360);
            int count = 0;
            int aa = 0;
            if (redStones == null && greenStones == null && blueStones == null) {
               activeChar.sendMessage("You do not have any seal stones to exchange.");
               return false;
            }

            if (redStones != null) {
               count = (int)((long)count + redStones.getCount());
               aa = (int)((long)aa + redStones.getCount() * 10L);
               activeChar.destroyItem("AncientAdenaExchanger", redStones, null, true);
            }

            if (greenStones != null) {
               count = (int)((long)count + greenStones.getCount());
               aa = (int)((long)aa + greenStones.getCount() * 5L);
               activeChar.destroyItem("AncientAdenaExchanger", greenStones, null, true);
            }

            if (blueStones != null) {
               count = (int)((long)count + blueStones.getCount());
               aa = (int)((long)aa + blueStones.getCount() * 3L);
               activeChar.destroyItem("AncientAdenaExchanger", blueStones, null, true);
            }

            activeChar.addItem("AncientAdenaExchanger", 5575, (long)aa, activeChar, true);
            activeChar.sendMessage("You have successfully exchanged " + count + " seal stones!");
         }

         return true;
      }
   }

   @Override
   public String[] getVoicedCommandList() {
      return commands;
   }
}
