package l2e.gameserver.handler.effecthandlers.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.tasks.player.BuffsBackTask;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class StealBuffs extends Effect {
   private final String _slot;
   private final int _rate;
   private final int _min;
   private final int _max;

   public StealBuffs(Env env, EffectTemplate template) {
      super(env, template);
      this._slot = template.getParameters().getString("slot", null);
      this._rate = template.getParameters().getInteger("rate", 0);
      this._min = template.getParameters().getInteger("min", 0);
      this._max = template.getParameters().getInteger("max", 0);
   }

   @Override
   public boolean canBeStolen() {
      return false;
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }

   @Override
   public boolean onStart() {
      if (this.getEffected() != null && this.getEffected().isPlayer() && this.getEffector() != this.getEffected()) {
         List<Effect> toSteal = Formulas.calcCancelStealEffects(
            this.getEffector(), this.getEffected(), this.getSkill(), this._slot, this._rate, this._min, this._max, false, true
         );
         if (toSteal.isEmpty()) {
            return false;
         } else {
            boolean isBuffSlot = this._slot.equalsIgnoreCase("buff");
            List<Effect> effects = new ArrayList<>(toSteal.size());
            Map<Skill, Effect> skillIds = new ConcurrentHashMap<>();

            for(Effect eff : toSteal) {
               Skill skill = eff.getSkill();
               if (!skillIds.containsKey(skill)) {
                  skillIds.put(skill, eff);
               }
            }

            Env env = new Env();
            env.setCharacter(this.getEffected());
            env.setTarget(this.getEffector());

            for(Entry<Skill, Effect> stats : skillIds.entrySet()) {
               Skill skill = stats.getKey();
               Effect effect = stats.getValue();
               if (skill.hasEffects()) {
                  env.setSkill(skill);

                  for(EffectTemplate et : skill.getEffectTemplates()) {
                     Effect ef = et.getEffect(env);
                     if (ef != null) {
                        if (Config.RESTORE_DISPEL_SKILLS && isBuffSlot) {
                           if (skill.hasEffectType(EffectType.HEAL_OVER_TIME)
                              || skill.hasEffectType(EffectType.CPHEAL_OVER_TIME)
                              || skill.hasEffectType(EffectType.MANA_HEAL_OVER_TIME)) {
                              continue;
                           }

                           Effect efR = effect.getEffectTemplate().getEffect(new Env(effect.getEffector(), effect.getEffected(), effect.getSkill()));
                           efR.setCount(effect.getTickCount());
                           efR.setAbnormalTime(effect.getAbnormalTime());
                           efR.setFirstTime(effect.getTime());
                           effects.add(efR);
                        }

                        ef.setCount(effect.getTickCount());
                        ef.setAbnormalTime(effect.getAbnormalTime());
                        ef.setFirstTime(effect.getTime());
                        ef.scheduleEffect(true);
                        if (ef.isIconDisplay() && this.getEffector().isPlayer()) {
                           SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
                           sm.addSkillName(effect);
                           this.getEffector().sendPacket(sm);
                        }
                     }
                  }
               }

               this.getEffected().stopSkillEffects(skill.getId());
            }

            if (Config.RESTORE_DISPEL_SKILLS && !effects.isEmpty()) {
               ThreadPoolManager.getInstance()
                  .schedule(new BuffsBackTask(this.getEffected().getActingPlayer(), effects), (long)(Config.RESTORE_DISPEL_SKILLS_TIME * 1000));
            }

            return true;
         }
      } else {
         if (this.getSkill().hasSelfEffects()) {
            Effect effect = this.getEffector().getFirstEffect(this.getSkill().getId());
            if (effect != null && effect.isSelfEffect()) {
               effect.exit();
            }

            this.getSkill().getEffectsSelf(this.getEffector());
         }

         return false;
      }
   }
}
