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
import l2e.gameserver.network.SystemMessageId;

public class Area implements ITargetTypeHandler {
   @Override
   public GameObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
      List<Creature> targetList = new ArrayList<>();
      if (target != null && (target != activeChar && !target.isAlikeDead() || skill.getCastRange() < 0) && (target.isAttackable() || target.isPlayable())) {
         boolean srcInArena = activeChar.isInsideZone(ZoneId.PVP) && !activeChar.isInsideZone(ZoneId.SIEGE);
         Creature origin;
         if (skill.getCastRange() >= 0) {
            if (!Skill.checkForAreaOffensiveSkills(activeChar, target, skill, srcInArena)) {
               return EMPTY_TARGET_LIST;
            }

            if (onlyFirst) {
               return new Creature[]{target};
            }

            origin = target;
            targetList.add(target);
         } else {
            origin = activeChar;
         }

         int maxTargets = skill.getAffectLimit();

         for(Creature obj : World.getInstance().getAroundCharacters(activeChar)) {
            if ((obj.isAttackable() || obj.isPlayable())
               && obj != origin
               && Util.checkIfInRange(skill.getAffectRange(), origin, obj, true)
               && Skill.checkForAreaOffensiveSkills(activeChar, obj, skill, srcInArena)) {
               if (maxTargets > 0 && targetList.size() >= maxTargets) {
                  break;
               }

               targetList.add(obj);
            }
         }

         return targetList.isEmpty() ? EMPTY_TARGET_LIST : targetList.toArray(new Creature[targetList.size()]);
      } else {
         activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
         return EMPTY_TARGET_LIST;
      }
   }

   @Override
   public Enum<TargetType> getTargetType() {
      return TargetType.AREA;
   }
}
