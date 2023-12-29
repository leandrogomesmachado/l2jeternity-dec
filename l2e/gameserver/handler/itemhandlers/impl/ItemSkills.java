package l2e.gameserver.handler.itemhandlers.impl;

import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;

public class ItemSkills extends ItemSkillsTemplate {
   @Override
   public boolean useItem(Playable playable, ItemInstance item, boolean forceUse) {
      Player activeChar = playable.getActingPlayer();
      if (activeChar != null && activeChar.isInOlympiadMode()) {
         activeChar.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
         return false;
      } else {
         if (activeChar != null && (item.isPotion() || item.isElixir())) {
            for(AbstractFightEvent e : playable.getFightEvents()) {
               if (e != null && !e.canUsePotion(playable)) {
                  playable.sendActionFailed();
                  return false;
               }
            }
         }

         return super.useItem(playable, item, forceUse);
      }
   }
}
