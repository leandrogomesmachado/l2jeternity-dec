package l2e.gameserver.handler.targethandlers.impl;

import l2e.gameserver.Config;
import l2e.gameserver.handler.targethandlers.ITargetTypeHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.model.skills.targets.TargetType;
import l2e.gameserver.network.SystemMessageId;

public class CorpseMob implements ITargetTypeHandler {
   @Override
   public GameObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
      if (target != null && target.isAttackable() && target.isDead()) {
         if (skill.getSkillType() == SkillType.SUMMON
            && target.isServitor()
            && target.getActingPlayer() != null
            && target.getActingPlayer().getObjectId() == activeChar.getObjectId()) {
            return EMPTY_TARGET_LIST;
         } else {
            return (GameObject[])(skill.getSkillType() == SkillType.DRAIN
                  && ((Attackable)target).isOldCorpse(activeChar.getActingPlayer(), Config.NPC_DECAY_TIME / 2 * 1000, true)
               ? EMPTY_TARGET_LIST
               : new Creature[]{target});
         }
      } else {
         activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
         return EMPTY_TARGET_LIST;
      }
   }

   @Override
   public Enum<TargetType> getTargetType() {
      return TargetType.CORPSE_MOB;
   }
}
