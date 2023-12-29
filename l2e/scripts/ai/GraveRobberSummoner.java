package l2e.scripts.ai;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.npc.Mystic;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.templates.npc.MinionData;
import l2e.gameserver.model.actor.templates.npc.MinionTemplate;
import l2e.gameserver.model.skills.funcs.Func;
import l2e.gameserver.model.stats.Env;
import l2e.gameserver.model.stats.Stats;

public class GraveRobberSummoner extends Mystic {
   private static final int[] Servitors = new int[]{22683, 22684, 22685, 22686};
   private int _lastMinionCount = 1;

   public GraveRobberSummoner(Attackable actor) {
      super(actor);
      actor.addStatFunc(new GraveRobberSummoner.FuncMulMinionCount(Stats.MAGIC_DEFENCE, 48, actor));
      actor.addStatFunc(new GraveRobberSummoner.FuncMulMinionCount(Stats.POWER_DEFENCE, 48, actor));
   }

   @Override
   protected void onEvtSpawn() {
      super.onEvtSpawn();
      Attackable actor = this.getActiveChar();
      actor.getMinionList().addMinion(new MinionData(new MinionTemplate(Servitors[Rnd.get(Servitors.length)], Rnd.get(2))), true);
      this._lastMinionCount = Math.max(actor.getMinionList().getAliveMinions().size(), 1);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (attacker != null && !actor.isDead()) {
         this._lastMinionCount = Math.max(actor.getMinionList().getAliveMinions().size(), 1);
         super.onEvtAttacked(attacker, damage);
      }
   }

   @Override
   protected void onEvtDead(Creature killer) {
      Attackable actor = this.getActiveChar();
      actor.getMinionList().deleteMinions();
      super.onEvtDead(killer);
   }

   protected class FuncMulMinionCount extends Func {
      public FuncMulMinionCount(Stats stat, int order, Object owner) {
         super(stat, order, owner);
      }

      @Override
      public void calc(Env env) {
         env._value *= (double)GraveRobberSummoner.this._lastMinionCount;
      }
   }
}
