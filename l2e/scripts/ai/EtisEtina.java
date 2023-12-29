package l2e.scripts.ai;

import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.quest.Quest;

public class EtisEtina extends Fighter {
   private Npc summon1;
   private Npc summon2;

   public EtisEtina(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (attacker != null && actor.getCurrentHpPercents() < 70.0 && actor.isScriptValue(0)) {
         actor.setScriptValue(1);
         this.summon1 = Quest.addSpawn(18950, actor.getLocation(), actor.getGeoIndex(), actor.getReflectionId(), 150);
         this.summon1.setRunning();
         this.summon1.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(100));
         this.summon2 = Quest.addSpawn(18951, actor.getLocation(), actor.getGeoIndex(), actor.getReflectionId(), 150);
         this.summon2.setRunning();
         this.summon2.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(100));
      }

      super.onEvtAttacked(attacker, damage);
   }

   @Override
   protected void onEvtDead(Creature killer) {
      if (this.summon1 != null && !this.summon1.isDead()) {
         this.summon1.decayMe();
      }

      if (this.summon2 != null && !this.summon2.isDead()) {
         this.summon2.decayMe();
      }

      super.onEvtDead(killer);
   }
}
