package l2e.gameserver.handler.effecthandlers.impl;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.instance.GrandBossInstance;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;

public class TargetCancel extends Effect {
   private final int _chance;

   public TargetCancel(Env env, EffectTemplate template) {
      super(env, template);
      this._chance = template.getParameters().getInteger("chance", 100);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }

   @Override
   public boolean onStart() {
      if (this.getEffected() != null
         && !(this.getEffected() instanceof GrandBossInstance)
         && Formulas.calcProbability((double)this._chance, this.getEffector(), this.getEffected(), this.getSkill(), false)) {
         this.getEffected().setTarget(null);
         if (this.getEffected().isAttackable()) {
            ((Attackable)this.getEffected()).stopHating(this.getEffector());
            ((Attackable)this.getEffected()).setFindTargetDelay(Rnd.get(1000, 2000));
            this.getEffected().abortAttack();
            this.getEffected().abortCast();
         }

         this.getEffected().abortAttack();
         this.getEffected().abortCast();
         this.getEffected().getAI().setIntention(this.getEffected().isAttackable() ? CtrlIntention.ACTIVE : CtrlIntention.IDLE);
         return true;
      } else {
         return false;
      }
   }
}
