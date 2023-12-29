package l2e.gameserver.handler.itemhandlers.impl;

import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;

public class Elixir extends ItemSkills {
   @Override
   public boolean useItem(Playable playable, ItemInstance item, boolean forceUse) {
      if (!playable.isPlayer()) {
         playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
         return false;
      } else {
         return super.useItem(playable, item, forceUse);
      }
   }
}
