package l2e.gameserver.handler.targethandlers.impl;

import l2e.gameserver.handler.targethandlers.ITargetTypeHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.targets.TargetType;
import l2e.gameserver.model.zone.ZoneId;

public class EnemySummon implements ITargetTypeHandler {
   @Override
   public GameObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
      if (target != null && target.isSummon()) {
         l2e.gameserver.model.actor.Summon targetSummon = (l2e.gameserver.model.actor.Summon)target;
         if (activeChar.isPlayer()
               && activeChar.getSummon() != targetSummon
               && !targetSummon.isDead()
               && (targetSummon.getOwner().getPvpFlag() != 0 || targetSummon.getOwner().getKarma() > 0)
            || targetSummon.getOwner().isInsideZone(ZoneId.PVP) && activeChar.getActingPlayer().isInsideZone(ZoneId.PVP)
            || targetSummon.getOwner().isInDuel()
               && activeChar.getActingPlayer().isInDuel()
               && targetSummon.getOwner().getDuelId() == activeChar.getActingPlayer().getDuelId()) {
            return new Creature[]{targetSummon};
         }
      }

      return EMPTY_TARGET_LIST;
   }

   @Override
   public Enum<TargetType> getTargetType() {
      return TargetType.ENEMY_SUMMON;
   }
}
