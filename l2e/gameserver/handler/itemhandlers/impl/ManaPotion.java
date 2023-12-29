package l2e.gameserver.handler.itemhandlers.impl;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;

public class ManaPotion extends ItemSkills {
   @Override
   public boolean useItem(Playable playable, ItemInstance item, boolean forceUse) {
      if (!Config.ENABLE_MANA_POTIONS_SUPPORT) {
         playable.sendPacket(SystemMessageId.NOTHING_HAPPENED);
         return false;
      } else {
         return super.useItem(playable, item, forceUse);
      }
   }
}
