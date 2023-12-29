package l2e.scripts.ai.isle_of_prayer;

import java.util.ArrayList;
import l2e.commons.util.Rnd;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.ai.npc.Fighter;
import l2e.gameserver.data.parser.NpcsParser;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.NpcSay;

public class Kechi extends Fighter {
   private static final int[][] guard_run = new int[][]{
      {22309, 153384, 149528, -12136},
      {22309, 153975, 149823, -12152},
      {22309, 154364, 149665, -12151},
      {22309, 153786, 149367, -12151},
      {22310, 154188, 149825, -12152},
      {22310, 153945, 149224, -12151},
      {22417, 154374, 149399, -12152},
      {22417, 153796, 149646, -12159}
   };
   private static NpcStringId[] CHAT = new NpcStringId[]{NpcStringId.HELP_ME, NpcStringId.PREPARE_TO_DIE};
   private int stage = 0;

   public Kechi(Attackable actor) {
      super(actor);
   }

   @Override
   protected void onEvtSpawn() {
      this.stage = 0;
      super.onEvtSpawn();
   }

   @Override
   protected void onEvtAttacked(Creature attacker, int damage) {
      Attackable actor = this.getActiveChar();
      if (!actor.isDead() && attacker != null) {
         double actor_hp_precent = actor.getCurrentHpPercents();
         switch(this.stage) {
            case 0:
               if (actor_hp_precent < 80.0) {
                  this.spawnMobs();
               }
               break;
            case 1:
               if (actor_hp_precent < 60.0) {
                  this.spawnMobs();
               }
               break;
            case 2:
               if (actor_hp_precent < 40.0) {
                  this.spawnMobs();
               }
               break;
            case 3:
               if (actor_hp_precent < 30.0) {
                  this.spawnMobs();
               }
               break;
            case 4:
               if (actor_hp_precent < 20.0) {
                  this.spawnMobs();
               }
               break;
            case 5:
               if (actor_hp_precent < 10.0) {
                  this.spawnMobs();
               }
               break;
            case 6:
               if (actor_hp_precent < 5.0) {
                  this.spawnMobs();
               }
         }

         super.onEvtAttacked(attacker, damage);
      }
   }

   protected void spawnMobs() {
      ++this.stage;
      Attackable actor = this.getActiveChar();
      actor.broadcastPacket(new NpcSay(actor.getObjectId(), 22, actor.getId(), CHAT[Rnd.get(2)]), 2000);

      for(int[] run : guard_run) {
         try {
            Spawner sp = new Spawner(NpcsParser.getInstance().getTemplate(run[0]));
            sp.setLocation(new Location(153384, 149528, -12136));
            sp.setReflectionId(actor.getReflectionId());
            sp.stopRespawn();
            Npc guard = sp.spawnOne(false);
            guard.setRunning();
            guard.getAI().setIntention(CtrlIntention.MOVING, new Location(run[1], run[2], run[3]));
            ArrayList<Creature> chars = new ArrayList<>();

            for(Attackable.AggroInfo info : actor.getAggroList().values()) {
               if (info != null) {
                  chars.add(info.getAttacker());
               }
            }

            Creature hated;
            if (chars.isEmpty()) {
               hated = null;
            } else {
               hated = chars.get(Rnd.get(chars.size()));
            }

            if (hated != null) {
               ((Attackable)guard).addDamageHate(hated, 0, Rnd.get(1, 100));
               guard.setTarget(hated);
               guard.getAI().setIntention(CtrlIntention.ATTACK, hated, null);
            }
         } catch (Exception var11) {
            var11.printStackTrace();
         }
      }
   }
}
