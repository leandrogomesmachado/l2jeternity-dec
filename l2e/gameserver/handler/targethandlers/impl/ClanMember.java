package l2e.gameserver.handler.targethandlers.impl;

import java.util.ArrayList;
import java.util.List;
import l2e.commons.util.Util;
import l2e.gameserver.handler.targethandlers.ITargetTypeHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.targets.TargetType;

public class ClanMember implements ITargetTypeHandler {
   @Override
   public GameObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
      List<Creature> targetList = new ArrayList<>();
      if (!activeChar.isNpc()) {
         return EMPTY_TARGET_LIST;
      } else {
         Npc npc = (Npc)activeChar;
         if (npc.getFaction().isNone()) {
            return new Creature[]{activeChar};
         } else {
            for(Npc newTarget : World.getInstance().getAroundNpc(activeChar)) {
               if (newTarget.isAttackable()
                  && npc.isInFaction((Attackable)newTarget)
                  && Util.checkIfInRange(skill.getCastRange(), activeChar, newTarget, true)
                  && newTarget.getFirstEffect(skill) == null) {
                  targetList.add(newTarget);
                  break;
               }
            }

            if (targetList.isEmpty()) {
               targetList.add(npc);
            }

            return targetList.toArray(new Creature[targetList.size()]);
         }
      }
   }

   @Override
   public Enum<TargetType> getTargetType() {
      return TargetType.CLAN_MEMBER;
   }
}
