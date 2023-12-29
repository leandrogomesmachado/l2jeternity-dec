package l2e.scripts.ai.isle_of_prayer;

import l2e.commons.util.Rnd;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.spawn.Spawner;

public class DarkWaterDragon extends Fighter {
   private static final int SHADE1 = 22268;
   private static final int SHADE2 = 22269;
   private static final int[] MOBS = new int[]{22268, 22269};

   public DarkWaterDragon(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (attacker != null && !actor.isDead()) {
         switch(actor.getScriptValue()) {
            case 0:
               actor.setScriptValue(1);
               this.spawnShades(attacker);
               break;
            case 1:
               if (actor.getCurrentHp() < actor.getMaxHp() / 2.0) {
                  actor.setScriptValue(2);
                  this.spawnShades(attacker);
               }
         }
      }

      super.onEvtAttacked(attacker, damage);
   }

   private void spawnShades(Creature attacker) {
      Attackable actor = this.getActiveChar();

      for(int i = 0; i < 5; ++i) {
         try {
            Spawner sp = new Spawner(NpcsParser.getInstance().getTemplate(MOBS[Rnd.get(MOBS.length)]));
            sp.setLocation(Location.findPointToStay(actor, 100, 120, true));
            sp.stopRespawn();
            Npc npc = sp.doSpawn(true);
            npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Integer.valueOf(Rnd.get(1, 100)));
         } catch (Exception var6) {
            var6.printStackTrace();
         }
      }
   }

   @Override
   protected void onEvtDead(Creature killer) {
      Attackable actor = this.getActiveChar();

      try {
         Spawner sp = new Spawner(NpcsParser.getInstance().getTemplate(18482));
         sp.setLocation(Location.findPointToStay(actor, 100, 120, true));
         sp.stopRespawn();
         sp.doSpawn(true);
      } catch (Exception var4) {
         var4.printStackTrace();
      }

      if (killer != null) {
         Player player = killer.getActingPlayer();
         if (player != null && Rnd.chance(77)) {
            actor.dropItem(player, 9596, 1L);
         }
      }

      super.onEvtDead(killer);
   }
}
