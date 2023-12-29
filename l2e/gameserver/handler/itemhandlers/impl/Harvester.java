package l2e.gameserver.handler.itemhandlers.impl;

import java.util.logging.Level;
import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.instancemanager.CastleManorManager;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;

public class Harvester implements IItemHandler {
   @Override
   public boolean useItem(Playable playable, ItemInstance item, boolean forceUse) {
      if (!playable.isPlayer()) {
         playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
         return false;
      } else if (CastleManorManager.getInstance().isDisabled()) {
         return false;
      } else {
         Player activeChar = playable.getActingPlayer();
         SkillHolder[] skills = item.getItem().getSkills();
         MonsterInstance target = null;
         if (activeChar.getTarget() != null && activeChar.getTarget().isMonster()) {
            target = (MonsterInstance)activeChar.getTarget();
         }

         if (skills == null) {
            _log.log(Level.WARNING, this.getClass().getSimpleName() + ": is missing skills!");
            return false;
         } else if (target != null && target.isDead()) {
            for(SkillHolder sk : skills) {
               activeChar.useMagic(sk.getSkill(), false, false, true);
            }

            return true;
         } else {
            activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
            activeChar.sendActionFailed();
            return false;
         }
      }
   }
}
