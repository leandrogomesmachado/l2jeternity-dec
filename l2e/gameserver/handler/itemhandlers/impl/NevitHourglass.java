package l2e.gameserver.handler.itemhandlers.impl;

import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class NevitHourglass extends ItemSkills {
   @Override
   public boolean useItem(Playable playable, ItemInstance item, boolean forceUse) {
      if (!playable.isPlayer()) {
         playable.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ITEM_NOT_FOR_PETS));
         return false;
      } else {
         Player activeChar = (Player)playable;
         if (activeChar.getRecommendation().isHourglassBonusActive() > 0L) {
            SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
            sm.addItemName(item.getId());
            activeChar.sendPacket(sm);
            return false;
         } else {
            return super.useItem(playable, item, forceUse);
         }
      }
   }
}
