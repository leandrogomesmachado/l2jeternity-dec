package l2e.gameserver.handler.skillhandlers.impl;

import l2e.gameserver.handler.skillhandlers.ISkillHandler;
import l2e.gameserver.instancemanager.HandysBlockCheckerManager;
import l2e.gameserver.model.ArenaParticipantsHolder;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.BlockInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.model.skills.effects.Effect;

public class Dummy implements ISkillHandler {
   private static final SkillType[] SKILL_IDS = new SkillType[]{SkillType.DUMMY};

   @Override
   public void useSkill(Creature activeChar, Skill skill, GameObject[] targets) {
      switch(skill.getId()) {
         case 5852:
         case 5853:
            GameObject obj = targets[0];
            if (obj != null) {
               this.useBlockCheckerSkill(activeChar.getActingPlayer(), skill, obj);
            }
            break;
         default:
            if (skill.hasEffects()) {
               for(Creature target : (Creature[])targets) {
                  skill.getEffects(activeChar, target, true);
               }
            }
      }

      if (skill.hasSelfEffects()) {
         Effect effect = activeChar.getFirstEffect(skill.getId());
         if (effect != null && effect.isSelfEffect()) {
            effect.exit();
         }

         skill.getEffectsSelf(activeChar);
      }

      if (skill.useSpiritShot()) {
         activeChar.setChargedShot(activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOTS) ? ShotType.BLESSED_SPIRITSHOTS : ShotType.SPIRITSHOTS, false);
      } else {
         activeChar.setChargedShot(ShotType.SOULSHOTS, false);
      }
   }

   @Override
   public SkillType[] getSkillIds() {
      return SKILL_IDS;
   }

   private final void useBlockCheckerSkill(Player activeChar, Skill skill, GameObject target) {
      if (target instanceof BlockInstance) {
         BlockInstance block = (BlockInstance)target;
         int arena = activeChar.getBlockCheckerArena();
         if (arena != -1) {
            ArenaParticipantsHolder holder = HandysBlockCheckerManager.getInstance().getHolder(arena);
            if (holder == null) {
               return;
            }

            int team = holder.getPlayerTeam(activeChar);
            int color = block.getColorEffect();
            if (team == 0 && color == 0) {
               block.changeColor(activeChar, holder, team);
            } else if (team == 1 && color == 83) {
               block.changeColor(activeChar, holder, team);
            }
         }
      }
   }
}
