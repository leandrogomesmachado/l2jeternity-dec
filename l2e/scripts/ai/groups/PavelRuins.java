package l2e.scripts.ai.groups;

import l2e.commons.threading.RunnableImpl;
import l2e.commons.util.PositionUtils;
import l2e.commons.util.Rnd;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.spawn.Spawner;

public class PavelRuins extends Fighter {
   private static final int PAVEL_SAFETY_DEVICE = 18917;
   private static final int CRUEL_PINCER_GOLEM_1 = 22801;
   private static final int CRUEL_PINCER_GOLEM_2 = 22802;
   private static final int CRUEL_PINCER_GOLEM_3 = 22803;
   private static final int DRILL_GOLEM_OF_TERROR_1 = 22804;
   private static final int DRILL_GOLEM_OF_TERROR_2 = 22805;
   private static final int DRILL_GOLEM_OF_TERROR_3 = 22806;

   public PavelRuins(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtDead(Creature killer) {
      Attackable actor = this.getActiveChar();
      super.onEvtDead(killer);
      ThreadPoolManager.getInstance().schedule(new PavelRuins.SpawnNext(actor, killer), 5000L);
   }

   private static void spawnNextMob(int npcId, Creature killer, Location loc) {
      try {
         Spawner sp = new Spawner(NpcsParser.getInstance().getTemplate(npcId));
         sp.setX(loc.getX());
         sp.setY(loc.getY());
         sp.setZ(loc.getZ());
         Npc npc = sp.doSpawn(true);
         npc.setHeading(PositionUtils.calculateHeadingFrom(npc, killer));
         npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, Integer.valueOf(1000));
      } catch (Exception var5) {
         var5.printStackTrace();
      }
   }

   private static class SpawnNext extends RunnableImpl {
      private final Attackable _actor;
      private final Creature _killer;

      public SpawnNext(Attackable actor, Creature killer) {
         this._actor = actor;
         this._killer = killer;
      }

      @Override
      public void runImpl() {
         if (Rnd.chance(70)) {
            Location loc = this._actor.getLocation();
            switch(this._actor.getId()) {
               case 18917:
                  loc = new Location(loc.getX() + 30, loc.getY() + -30, loc.getZ());
                  PavelRuins.spawnNextMob(22803, this._killer, loc);
                  loc = new Location(loc.getX() + -30, loc.getY() + 30, loc.getZ());
                  PavelRuins.spawnNextMob(22806, this._killer, loc);
                  break;
               case 22801:
                  PavelRuins.spawnNextMob(22802, this._killer, loc);
                  break;
               case 22803:
                  PavelRuins.spawnNextMob(22801, this._killer, loc);
                  break;
               case 22804:
                  PavelRuins.spawnNextMob(22805, this._killer, loc);
                  break;
               case 22806:
                  PavelRuins.spawnNextMob(22804, this._killer, loc);
            }
         }
      }
   }
}
