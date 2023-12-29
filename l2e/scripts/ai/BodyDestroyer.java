package l2e.scripts.ai;

import java.util.concurrent.ScheduledFuture;
import l2e.commons.threading.RunnableImpl;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;

public class BodyDestroyer extends Fighter {
   private ScheduledFuture<?> _destroyTask;

   public BodyDestroyer(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (attacker != null && actor.isScriptValue(0)) {
         actor.setScriptValue(1);
         actor.addDamageHate(attacker, 0, 9999);
         actor.setTarget(attacker);
         actor.doCast(SkillsParser.getInstance().getInfo(5256, 1));
         attacker.setCurrentHp(1.0);
         this._destroyTask = ThreadPoolManager.getInstance().schedule(new BodyDestroyer.Destroy(attacker), 30000L);
      }

      super.onEvtAttacked(attacker, damage);
   }

   @Override
   protected void onEvtDead(Creature killer) {
      if (this._destroyTask != null) {
         this._destroyTask.cancel(false);
      }

      killer.stopSkillEffects(5256);
      super.onEvtDead(killer);
   }

   private class Destroy extends RunnableImpl {
      Creature _attacker;

      public Destroy(Creature attacker) {
         this._attacker = attacker;
      }

      @Override
      public void runImpl() {
         this._attacker.setCurrentHp(1.0);
      }
   }
}
