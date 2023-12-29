package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class ImmobilePetBuff extends Effect {
   private Summon _pet;

   public ImmobilePetBuff(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.BUFF;
   }

   @Override
   public boolean onStart() {
      this._pet = null;
      if (this.getEffected().isSummon() && this.getEffector().isPlayer() && ((Summon)this.getEffected()).getOwner() == this.getEffector()) {
         this._pet = (Summon)this.getEffected();
         this._pet.setIsImmobilized(true);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void onExit() {
      if (this._pet != null) {
         this._pet.setIsImmobilized(false);
      }
   }
}
