package l2e.gameserver.handler.itemhandlers.impl;

import java.util.logging.Level;
import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.FeedableBeastInstance;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;

public class BeastSpice implements IItemHandler {
   @Override
   public boolean useItem(Playable playable, ItemInstance item, boolean forceUse) {
      if (!playable.isPlayer()) {
         playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
         return false;
      } else {
         Player activeChar = playable.getActingPlayer();
         SkillHolder[] skills = item.getItem().getSkills();
         if (skills == null) {
            _log.log(Level.WARNING, this.getClass().getSimpleName() + ": is missing skills!");
            return false;
         } else if (!(activeChar.getTarget() instanceof FeedableBeastInstance)) {
            activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
            return false;
         } else {
            for(SkillHolder sk : skills) {
               activeChar.useMagic(sk.getSkill(), false, false, true);
            }

            return true;
         }
      }
   }
}
