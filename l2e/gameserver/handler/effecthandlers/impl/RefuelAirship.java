package l2e.gameserver.handler.effecthandlers.impl;

import l2e.gameserver.model.actor.instance.AirShipInstance;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectTemplate;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.model.stats.Env;

public class RefuelAirship extends Effect {
   public RefuelAirship(Env env, EffectTemplate template) {
      super(env, template);
   }

   @Override
   public EffectType getEffectType() {
      return EffectType.REFUEL_AIRSHIP;
   }

   @Override
   public boolean onStart() {
      AirShipInstance ship = this.getEffector().getActingPlayer().getAirShip();
      ship.setFuel(ship.getFuel() + (int)this.calc());
      ship.updateAbnormalEffect();
      return true;
   }
}
