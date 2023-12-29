package l2e.gameserver.handler.effecthandlers.impl;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.tasks.player.BuffsBackTask;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;

public class Cancel extends Effect {
   private final String _slot;
   private final int _rate;
   private final int _min;
   private final int _max;
   private final boolean _randomEffects;

   public Cancel(Env env, EffectTemplate template) {
      super(env, template);
      this._slot = template.getParameters().getString("slot", null);
      this._rate = template.getParameters().getInteger("rate", 0);
      this._min = template.getParameters().getInteger("min", 0);
      this._max = template.getParameters().getInteger("max", 0);
      this._randomEffects = template.getParameters().getInteger("randomEffects", 0) == 1;
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.CANCEL;
   }

   @Override
   public boolean onStart() {
      if (this.getEffected().isDead()) {
         return false;
      } else {
         boolean isBuffSlot = this._slot.equalsIgnoreCase("buff");
         List<Effect> canceled = Formulas.calcCancelStealEffects(
            this.getEffector(), this.getEffected(), this.getSkill(), this._slot, this._rate, this._min, this._max, this._randomEffects, false
         );
         List<Effect> effects = new ArrayList<>(canceled.size());

         for(Effect eff : canceled) {
            if (Config.RESTORE_DISPEL_SKILLS) {
               if (this.getSkill().hasEffectType(EffectType.HEAL_OVER_TIME)
                  || this.getSkill().hasEffectType(EffectType.CPHEAL_OVER_TIME)
                  || this.getSkill().hasEffectType(EffectType.MANA_HEAL_OVER_TIME)) {
                  continue;
               }

               if (isBuffSlot) {
                  Effect ef = eff.getEffectTemplate().getEffect(new Env(eff.getEffector(), eff.getEffected(), eff.getSkill()));
                  ef.setCount(eff.getTickCount());
                  ef.setAbnormalTime(eff.getAbnormalTime());
                  ef.setFirstTime(eff.getTime());
                  effects.add(ef);
               }
            }

            if (!isBuffSlot && eff.triggersChanceSkill()) {
               this.getEffected().removeChanceEffect(eff);
            }

            eff.exit();
         }

         if (Config.RESTORE_DISPEL_SKILLS && !effects.isEmpty()) {
            ThreadPoolManager.getInstance()
               .schedule(new BuffsBackTask(this.getEffected().getActingPlayer(), effects), (long)(Config.RESTORE_DISPEL_SKILLS_TIME * 1000));
         }

         return true;
      }
   }
}
