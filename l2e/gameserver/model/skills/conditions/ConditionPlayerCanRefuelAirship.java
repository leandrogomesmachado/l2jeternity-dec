package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.ControllableAirShipInstance;
import l2e.gameserver.model.stats.Env;

public class ConditionPlayerCanRefuelAirship extends Condition {
   private final int _val;

   public ConditionPlayerCanRefuelAirship(int val) {
      this._val = val;
   }

   @Override
   public boolean testImpl(Env env) {
      boolean canRefuelAirship = true;
      Player player = env.getPlayer();
      if (player == null
         || player.getAirShip() == null
         || !(player.getAirShip() instanceof ControllableAirShipInstance)
         || player.getAirShip().getFuel() + this._val > player.getAirShip().getMaxFuel()) {
         canRefuelAirship = false;
      }

      return canRefuelAirship;
   }
}
