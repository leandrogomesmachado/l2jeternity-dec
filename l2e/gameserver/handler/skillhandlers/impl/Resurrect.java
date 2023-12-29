package l2e.gameserver.handler.skillhandlers.impl;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.handler.skillhandlers.ISkillHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.PetInstance;
import l2e.gameserver.model.entity.events.AbstractFightEvent;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.model.skills.targets.TargetType;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.taskmanager.DecayTaskManager;

public class Resurrect implements ISkillHandler {
   private static final SkillType[] SKILL_IDS = new SkillType[]{SkillType.RESURRECT};

   @Override
   public void useSkill(Creature activeChar, Skill skill, GameObject[] targets) {
      Player player = null;
      if (activeChar.isPlayer()) {
         player = activeChar.getActingPlayer();
      }

      List<Creature> targetToRes = new ArrayList<>();

      for(Creature target : (Creature[])targets) {
         if (target.isPlayer()) {
            Player targetPlayer = target.getActingPlayer();
            if (skill.getTargetType() == TargetType.CORPSE_CLAN && player != null && player.getClanId() != targetPlayer.getClanId()) {
               continue;
            }

            for(AbstractFightEvent e : player.getFightEvents()) {
               if (!e.canRessurect(player, targetPlayer)) {
               }
            }
         }

         if (target.isVisible()) {
            targetToRes.add(target);
         }
      }

      if (targetToRes.isEmpty()) {
         activeChar.abortCast();
      } else {
         for(Creature cha : targetToRes) {
            if (activeChar.isPlayer()) {
               if (cha.isPlayer()) {
                  cha.getActingPlayer().reviveRequest(activeChar.getActingPlayer(), skill, false);
               } else if (cha.isPet()) {
                  ((PetInstance)cha).getOwner().reviveRequest(activeChar.getActingPlayer(), skill, true);
               }
            } else {
               DecayTaskManager.getInstance().cancel(cha);
               cha.doRevive(Formulas.calculateSkillResurrectRestorePercent(skill.getPower(), activeChar));
            }
         }

         activeChar.setChargedShot(activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOTS) ? ShotType.BLESSED_SPIRITSHOTS : ShotType.SPIRITSHOTS, false);
      }
   }

   @Override
   public SkillType[] getSkillIds() {
      return SKILL_IDS;
   }
}
