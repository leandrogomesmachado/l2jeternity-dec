package l2e.gameserver.handler.effecthandlers.impl;

import l2e.commons.util.Util;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Formulas;

public class TransferHate extends Effect {
   private final int _chance;

   public TransferHate(Env env, EffectTemplate template) {
      super(env, template);
      this._chance = template.hasParameters() ? template.getParameters().getInteger("chance", 100) : 100;
   }

   @Override
   public boolean calcSuccess() {
      return Formulas.calcProbability((double)this._chance, this.getEffector(), this.getEffected(), this.getSkill(), false);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }

   @Override
   public boolean onStart() {
      if (Util.checkIfInRange(this.getSkill().getEffectRange(), this.getEffector(), this.getEffected(), true)) {
         for(Creature obj : World.getInstance().getAroundCharacters(this.getEffector(), this.getSkill().getAffectRange(), 200)) {
            if (obj != null && obj.isAttackable() && !obj.isDead()) {
               Attackable hater = (Attackable)obj;
               int hate = hater.getHating(this.getEffector());
               if (hate > 0) {
                  hater.reduceHate(this.getEffector(), -hate);
                  hater.addDamageHate(this.getEffected(), 0, hate);
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }
}
