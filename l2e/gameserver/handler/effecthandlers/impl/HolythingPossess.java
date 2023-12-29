package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public final class HolythingPossess extends Effect {
   public HolythingPossess(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.NONE;
   }

   @Override
   public boolean onStart() {
      if (!this.getEffector().isPlayer()) {
         return false;
      } else {
         Castle castle = CastleManager.getInstance().getCastle(this.getEffector());
         castle.engrave(this.getEffector().getActingPlayer().getClan(), this.getEffected());
         return true;
      }
   }
}
