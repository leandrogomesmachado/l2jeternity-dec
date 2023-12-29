package l2e.scripts.ai.kamaloka;

import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Mystic;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.spawn.Spawner;

public class SeerFlouros extends Mystic {
   private int _hpCount = 0;
   private static final int[] _hps = new int[]{80, 60, 40, 30, 20, 10, 5, -5};

   public SeerFlouros(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (attacker != null && !actor.isDead() && actor.getCurrentHpPercents() < (double)_hps[this._hpCount]) {
         this.spawnMobs(attacker);
         ++this._hpCount;
      }

      super.onEvtAttacked(attacker, damage);
   }

   private void spawnMobs(Creature attacker) {
      Attackable actor = this.getActiveChar();

      for(int i = 0; i < 2; ++i) {
         try {
            Spawner sp = new Spawner(NpcsParser.getInstance().getTemplate(18560));
            sp.setLocation(Location.findPointToStay(actor, 100, 120, true));
            sp.setReflectionId(actor.getReflectionId());
            sp.stopRespawn();
            Npc npc = sp.spawnOne(false);
            npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(100));
         } catch (Exception var6) {
            var6.printStackTrace();
         }
      }
   }

   @Override
   protected void onEvtDead(Creature killer) {
      this._hpCount = 0;
      super.onEvtDead(killer);
   }
}
