package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.stats.Env;

public class ConditionPlayerBaseStats extends Condition {
   private final BaseStat _stat;
   private final int _value;

   public ConditionPlayerBaseStats(Creature player, BaseStat stat, int value) {
      this._stat = stat;
      this._value = value;
   }

   @Override
   public boolean testImpl(Env env) {
      if (env.getPlayer() == null) {
         return false;
      } else {
         Player player = env.getPlayer();
         switch(this._stat) {
            case Int:
               return player.getINT() >= this._value;
            case Str:
               return player.getSTR() >= this._value;
            case Con:
               return player.getCON() >= this._value;
            case Dex:
               return player.getDEX() >= this._value;
            case Men:
               return player.getMEN() >= this._value;
            case Wit:
               return player.getWIT() >= this._value;
            default:
               return false;
         }
      }
   }
}
