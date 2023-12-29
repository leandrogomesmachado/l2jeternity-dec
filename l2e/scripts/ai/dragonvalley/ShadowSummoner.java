package l2e.scripts.ai.dragonvalley;

import l2e.commons.util.NpcUtils;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.instance.MonsterInstance;

public class ShadowSummoner extends Fighter {
   private long _lastSpawnTime = 0L;
   private long _lastAttackTime = 0L;

   public ShadowSummoner(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtSpawn() {
      this._lastAttackTime = System.currentTimeMillis();
      super.onEvtSpawn();
   }

   @Override
   protected boolean thinkActive() {
      Attackable actor = this.getActiveChar();
      if (this._lastAttackTime != 0L) {
         if (this._lastAttackTime + 300000L < System.currentTimeMillis() && actor.getAggroRange() == 0) {
            actor.getTemplate().setAggroRange(400);
         }

         if (this._lastAttackTime + 1800000L < System.currentTimeMillis()) {
            actor.deleteMe();
         }
      }

      return super.thinkActive();
   }

   @Override
   protected void thinkAttack() {
      Attackable actor = this.getActiveChar();
      if (actor.getCurrentHpPercents() < 50.0 && this._lastSpawnTime + 60000L < System.currentTimeMillis()) {
         this._lastSpawnTime = System.currentTimeMillis();
         MonsterInstance minion = NpcUtils.spawnSingle(25731, Location.findPointToStay(actor, 250, true));
         minion.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, this.getAttackTarget(), Integer.valueOf(5000));
      }

      super.thinkAttack();
   }

   @Override
   public int getRateDEBUFF() {
      return 15;
   }

   @Override
   public int getRateDAM() {
      return 50;
   }
}
