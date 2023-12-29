package l2e.gameserver.handler.targethandlers.impl;

import java.util.ArrayList;
import java.util.List;
import l2e.commons.util.Util;
import l2e.gameserver.handler.targethandlers.ITargetTypeHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.targets.TargetType;
import l2e.gameserver.model.zone.ZoneId;

public class AreaSummon implements ITargetTypeHandler {
   @Override
   public GameObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
      List<Creature> targetList = new ArrayList<>();
      Creature var10 = activeChar.getSummon();
      if (var10 == null || !var10.isServitor() || var10.isDead()) {
         return EMPTY_TARGET_LIST;
      } else if (onlyFirst) {
         return new Creature[]{var10};
      } else {
         boolean srcInArena = activeChar.isInsideZone(ZoneId.PVP) && !activeChar.isInsideZone(ZoneId.SIEGE);
         int maxTargets = skill.getAffectLimit();

         for(Creature obj : World.getInstance().getAroundCharacters(var10)) {
            if (obj != null
               && obj != var10
               && obj != activeChar
               && Util.checkIfInRange(skill.getAffectRange(), var10, obj, true)
               && (obj.isAttackable() || obj.isPlayable())
               && Skill.checkForAreaOffensiveSkills(activeChar, obj, skill, srcInArena)) {
               if (maxTargets > 0 && targetList.size() >= maxTargets) {
                  break;
               }

               targetList.add(obj);
            }
         }

         return targetList.isEmpty() ? EMPTY_TARGET_LIST : targetList.toArray(new Creature[targetList.size()]);
      }
   }

   @Override
   public Enum<TargetType> getTargetType() {
      return TargetType.AREA_SUMMON;
   }
}
