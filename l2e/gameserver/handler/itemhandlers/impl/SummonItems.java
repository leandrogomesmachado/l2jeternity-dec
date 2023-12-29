package l2e.gameserver.handler.itemhandlers.impl;

import l2e.gameserver.data.parser.PetsParser;
import l2e.gameserver.model.PetData;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.holders.PetItemHolder;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;

public class SummonItems extends ItemSkillsTemplate {
   @Override
   public boolean useItem(Playable playable, ItemInstance item, boolean forceUse) {
      if (!playable.isPlayer()) {
         playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
         return false;
      } else {
         for(AbstractFightEvent e : playable.getFightEvents()) {
            if (e != null && !e.canUseItemSummon(playable)) {
               return false;
            }
         }

         Player activeChar = playable.getActingPlayer();
         if (activeChar.getBlockCheckerArena() != -1 || activeChar.inObserverMode() || activeChar.isAllSkillsDisabled() || activeChar.isCastingNow()) {
            return false;
         } else if (activeChar.isSitting()) {
            activeChar.sendPacket(SystemMessageId.CANT_MOVE_SITTING);
            return false;
         } else if (activeChar.hasSummon() || activeChar.isMounted()) {
            activeChar.sendPacket(SystemMessageId.YOU_ALREADY_HAVE_A_PET);
            return false;
         } else if (!activeChar.isAttackingNow() && !activeChar.isInCombat()) {
            PetData petData = PetsParser.getInstance().getPetDataByItemId(item.getId());
            if (petData != null && petData.getNpcId() != -1) {
               activeChar.addScript(new PetItemHolder(item));
               return super.useItem(playable, item, forceUse);
            } else {
               return false;
            }
         } else {
            activeChar.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT);
            return false;
         }
      }
   }
}
