package l2e.gameserver.handler.effecthandlers.impl;

import l2e.commons.util.Rnd;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.network.SystemMessageId;

public class FakeDeath extends Effect {
   private final int _rate;

   public FakeDeath(Env env, EffectTemplate template) {
      super(env, template);
      this._rate = template.getParameters().getInteger("rate", 0);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.FAKE_DEATH;
   }

   @Override
   public boolean onStart() {
      this.getEffected().startFakeDeath();
      if (this.getEffected().isPlayer() && Rnd.get(100) <= this._rate) {
         this.getEffected().getActingPlayer().setIsFakeDeath(true);
      }

      return true;
   }

   @Override
   public void onExit() {
      this.getEffected().stopFakeDeath(true);
   }

   @Override
   public boolean onActionTime() {
      if (this.getEffected().isDead()) {
         return false;
      } else {
         double manaDam = this.calc();
         if (manaDam > this.getEffected().getCurrentMp() && this.getSkill().isToggle()) {
            this.getEffected().sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
            return false;
         } else {
            this.getEffected().reduceCurrentMp(manaDam);
            return true;
         }
      }
   }
}
