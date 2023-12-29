package l2e.gameserver.model.skills.funcs;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;

public class FuncShare extends Func {
   private final Lambda _lambda;

   public FuncShare(Stats pStat, int pOrder, Object owner, Lambda lambda) {
      super(pStat, pOrder, owner);
      this._lambda = lambda;
   }

   @Override
   public void calc(Env env) {
      if (this.cond == null || this.cond.test(env)) {
         Creature ch = env.getCharacter();
         if (ch != null && ch.isServitor()) {
            Summon summon = (Summon)ch;
            Player player = summon.getOwner();
            double value = player.calcStat(this.stat, 0.0, null, null) * this._lambda.calc(env);
            env.addValue(value);
         }
      }
   }
}
