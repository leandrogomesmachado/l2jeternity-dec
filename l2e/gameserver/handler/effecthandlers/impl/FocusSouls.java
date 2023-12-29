package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.network.SystemMessageId;

public class FocusSouls extends Effect {
   public FocusSouls(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }

   @Override
   public boolean onStart() {
      if (this.getEffected().isPlayer() && !this.getEffected().isAlikeDead()) {
         Player target = this.getEffected().getActingPlayer();
         int maxSouls = (int)target.calcStat(Stats.MAX_SOULS, 0.0, null, null);
         if (maxSouls > 0) {
            int amount = (int)this.calc();
            if (target.getChargedSouls() >= maxSouls) {
               target.sendPacket(SystemMessageId.SOUL_CANNOT_BE_INCREASED_ANYMORE);
               return false;
            }

            int count = target.getChargedSouls() + amount <= maxSouls ? amount : maxSouls - target.getChargedSouls();
            target.increaseSouls(count);
         }

         return true;
      } else {
         return false;
      }
   }
}
