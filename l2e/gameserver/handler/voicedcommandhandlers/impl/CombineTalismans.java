package l2e.gameserver.handler.voicedcommandhandlers.impl;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;

public class CombineTalismans implements IVoicedCommandHandler {
   private static final String[] VOICED_COMMANDS = new String[]{"combinetalismans"};

   @Override
   public boolean useVoicedCommand(String command, Player activeChar, String args) {
      List<int[]> _sameIds = new ArrayList<>();

      for(ItemInstance item : activeChar.getInventory().getItems()) {
         if (item.getMana() > 0 && item.getName().contains("Talisman")) {
            addTalisman(_sameIds, item.getId());
         }
      }

      int newCount = 0;

      for(int[] idCount : _sameIds) {
         if (idCount[1] > 1) {
            int lifeTime = 0;

            for(ItemInstance existingTalisman : activeChar.getInventory().getItemsByItemId(idCount[0])) {
               lifeTime += existingTalisman.getMana();
               activeChar.getInventory().destroyItem("Combine Talismans", existingTalisman, activeChar, null);
            }

            ItemInstance newTalisman = activeChar.addItem("Combine talismans", idCount[0], 1L, null, false);
            newTalisman.setMana(lifeTime);
            newTalisman.updateDatabase();
            ++newCount;
         }
      }

      if (newCount > 0) {
         activeChar.sendMessage("You have combined " + newCount + " talismans.");
         activeChar.sendItemList(false);
      } else {
         activeChar.sendMessage("You don't have Talismans to combine!");
      }

      return true;
   }

   private static void addTalisman(List<int[]> sameIds, int itemId) {
      for(int i = 0; i < sameIds.size(); ++i) {
         if (((int[])sameIds.get(i))[0] == itemId) {
            ++((int[])sameIds.get(i))[1];
            return;
         }
      }

      sameIds.add(new int[]{itemId, 1});
   }

   @Override
   public String[] getVoicedCommandList() {
      return VOICED_COMMANDS;
   }
}
