package l2e.gameserver.handler.skillhandlers.impl;

import java.util.Map.Entry;
import l2e.commons.util.Rnd;
import l2e.gameserver.handler.skillhandlers.ISkillHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.ShotType;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.SkillType;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;

public class NegateEffects implements ISkillHandler {
   private static final SkillType[] SKILL_IDS = new SkillType[]{SkillType.NEGATE_EFFECTS};

   @Override
   public void useSkill(Creature activeChar, Skill skill, GameObject[] targets) {
      byte shld = 0;
      boolean ss = skill.useSoulShot() && activeChar.isChargedShot(ShotType.SOULSHOTS);
      boolean sps = skill.useSpiritShot() && activeChar.isChargedShot(ShotType.SPIRITSHOTS);
      boolean bss = skill.useSpiritShot() && activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);

      for(GameObject obj : targets) {
         if (obj instanceof Creature) {
            Creature target = (Creature)obj;
            if (!target.isDead() && (!target.isInvul() || target.isParalyzed())) {
               if (skill.getNegateAbnormalTypes() != null && !skill.getNegateAbnormalTypes().isEmpty()) {
                  this.negateEffects(activeChar, target, skill);
               }

               shld = Formulas.calcShldUse(activeChar, target, skill);
               if (Formulas.calcSkillReflect(target, skill) == 1) {
                  target = activeChar;
               }

               if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, ss, sps, bss)) {
                  skill.getEffects(activeChar, target, new Env(shld, ss, sps, bss), true);
               }
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

      activeChar.setChargedShot(bss ? ShotType.BLESSED_SPIRITSHOTS : ShotType.SPIRITSHOTS, false);
   }

   private void negateEffects(Creature activeChar, Creature target, Skill skill) {
      for(Entry<String, Short> value : skill.getNegateAbnormalTypes().entrySet()) {
         String stackType = value.getKey();
         float stackOrder = (float)value.getValue().shortValue();
         int skillCast = skill.getId();

         for(Effect e : target.getAllEffects()) {
            if (e.getSkill().canBeDispeled()
               && stackType.equalsIgnoreCase(e.getAbnormalType())
               && e.getSkill().getId() != skillCast
               && Rnd.chance(skill.getNegateRate())
               && e.getSkill() != null) {
               if (e.triggersChanceSkill()) {
                  target.removeChanceEffect(e);
               }

               if (stackOrder == -1.0F) {
                  target.stopSkillEffects(e.getSkill().getId());
               } else if (stackOrder >= (float)e.getAbnormalLvl()) {
                  target.stopSkillEffects(e.getSkill().getId());
               }
            }
         }
      }
   }

   @Override
   public SkillType[] getSkillIds() {
      return SKILL_IDS;
   }
}
