package l2e.gameserver.handler.itemhandlers.impl;

import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExChooseInventoryAttributeItem;

public class EnchantAttribute implements IItemHandler {
   @Override
   public boolean useItem(Playable playable, ItemInstance item, boolean forceUse) {
      if (!playable.isPlayer()) {
         playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
         return false;
      } else {
         Player activeChar = playable.getActingPlayer();
         if (activeChar.isCastingNow()) {
            return false;
         } else if (activeChar.isEnchanting()) {
            activeChar.sendPacket(SystemMessageId.ENCHANTMENT_ALREADY_IN_PROGRESS);
            return false;
         } else {
            activeChar.setActiveEnchantAttrItemId(item.getObjectId());
            activeChar.sendPacket(new ExChooseInventoryAttributeItem(item));
            return true;
         }
      }
   }
}
