package l2e.scripts.ai.dragonvalley;

import l2e.commons.util.NpcUtils;
import l2e.commons.util.Rnd;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.data.parser.SkillsParser;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.instance.MonsterInstance;

public class BleedingFly extends Fighter {
   private int _spawnCount = 0;
   private long _lastAttackTime = 0L;

   public BleedingFly(Attackable actor) {
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
         if (this._lastAttackTime + 300000L < System.currentTimeMillis() && actor.getTemplate().getAggroRange() == 0) {
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
      if (this._spawnCount == 0 && actor.getCurrentHpPercents() < 50.0) {
         ++this._spawnCount;
         this.spawnMinions();
      } else if (this._spawnCount == 1 && actor.getCurrentHpPercents() < 25.0) {
         ++this._spawnCount;
         this.spawnMinions();
         actor.setTarget(actor);
         actor.doCast(SkillsParser.getInstance().getInfo(6915, 3));
      }

      super.thinkAttack();
   }

   private void spawnMinions() {
      Attackable actor = this.getActiveChar();
      int count = 3 + Rnd.get(1, 3);
      actor.setTarget(actor);
      actor.doCast(SkillsParser.getInstance().getInfo(6832, 1));

      for(int i = 0; i < count; ++i) {
         MonsterInstance minion = NpcUtils.spawnSingle(25734, Location.findPointToStay(actor, 250, false));
         minion.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, this.getAttackTarget(), Integer.valueOf(5000));
      }
   }

   @Override
   public int getRateDEBUFF() {
      return 5;
   }

   @Override
   public int getRateDAM() {
      return 80;
   }
}
