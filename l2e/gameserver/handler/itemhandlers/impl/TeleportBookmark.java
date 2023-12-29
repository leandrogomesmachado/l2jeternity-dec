package l2e.gameserver.handler.itemhandlers.impl;

import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class TeleportBookmark implements IItemHandler {
   @Override
   public boolean useItem(Playable playable, ItemInstance item, boolean forceUse) {
      if (!playable.isPlayer()) {
         playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
         return false;
      } else {
         Player player = playable.getActingPlayer();
         if (player.getBookMarkSlot() >= 9) {
            player.sendPacket(SystemMessageId.YOUR_NUMBER_OF_MY_TELEPORTS_SLOTS_HAS_REACHED_ITS_MAXIMUM_LIMIT);
            return false;
         } else {
            player.destroyItem("Consume", item.getObjectId(), 1L, null, false);
            player.setBookMarkSlot(player.getBookMarkSlot() + 3);
            player.sendPacket(SystemMessageId.THE_NUMBER_OF_MY_TELEPORTS_SLOTS_HAS_BEEN_INCREASED);
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
            sm.addItemName(item.getId());
            player.sendPacket(sm);
            return true;
         }
      }
   }
}
