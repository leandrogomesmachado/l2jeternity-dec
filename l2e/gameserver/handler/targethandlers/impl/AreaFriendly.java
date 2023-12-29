package l2e.gameserver.handler.targethandlers.impl;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.Config;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.handler.targethandlers.ITargetTypeHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.targets.TargetType;
import l2e.gameserver.network.SystemMessageId;

public class AreaFriendly implements ITargetTypeHandler {
   @Override
   public GameObject[] getTargetList(Skill skill, Creature activeChar, boolean onlyFirst, Creature target) {
      List<Creature> targetList = new ArrayList<>();
      if (!this.checkTarget(activeChar, target) && skill.getCastRange() >= 0) {
         activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
         return EMPTY_TARGET_LIST;
      } else if (onlyFirst) {
         return new Creature[]{target};
      } else if (activeChar.getActingPlayer().isInOlympiadMode()) {
         return new Creature[]{activeChar};
      } else {
         targetList.add(activeChar);
         if (target != null) {
            if (target != activeChar) {
               targetList.add(target);
            }

            for(Creature obj : World.getInstance().getAroundCharacters(target, skill.getAffectRange(), 200)) {
               if (this.checkTarget(activeChar, obj) && obj != activeChar) {
                  if (skill.getAffectLimit() > 0 && targetList.size() >= skill.getAffectLimit()) {
                     break;
                  }

                  targetList.add(obj);
               }
            }
         }

         return targetList.isEmpty() ? EMPTY_TARGET_LIST : targetList.toArray(new Creature[targetList.size()]);
      }
   }

   private boolean checkTarget(Creature activeChar, Creature target) {
      if (Config.GEODATA && !GeoEngine.canSeeTarget(activeChar, target, false)) {
         return false;
      } else if (!target.isDead() && (target.isPlayer() || target.isSummon())) {
         Player actingPlayer = activeChar.getActingPlayer();
         Player targetPlayer = target.getActingPlayer();
         if (actingPlayer == null || targetPlayer == null) {
            return false;
         } else {
            return actingPlayer.isFriend(targetPlayer);
         }
      } else {
         return false;
      }
   }

   @Override
   public Enum<TargetType> getTargetType() {
      return TargetType.AREA_FRIENDLY;
   }
}
