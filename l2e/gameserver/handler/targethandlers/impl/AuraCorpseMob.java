package l2e.gameserver.handler.targethandlers.impl;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.handler.targethandlers.ITargetTypeHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.targets.TargetType;

public class AuraCorpseMob implements ITargetTypeHandler {
   @Override
   public GameObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
      List<Creature> targetList = new ArrayList<>();
      int maxTargets = skill.getAffectLimit();

      for(Creature obj : World.getInstance().getAroundCharacters(activeChar, skill.getAffectRange(), 200)) {
         if (obj.isAttackable() && obj.isDead()) {
            if (onlyFirst) {
               return new Creature[]{obj};
            }

            if (maxTargets > 0 && targetList.size() >= maxTargets) {
               break;
            }

            targetList.add(obj);
         }
      }

      return targetList.toArray(new Creature[targetList.size()]);
   }

   @Override
   public Enum<TargetType> getTargetType() {
      return TargetType.AURA_CORPSE_MOB;
   }
}
