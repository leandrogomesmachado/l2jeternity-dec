package l2e.scripts.ai;

import java.util.GregorianCalendar;
import l2e.gameserver.data.parser.SpawnParser;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.spawn.Spawner;
import l2e.gameserver.network.NpcStringId;

public class Lindvior extends AbstractNpcAI {
   private static final int LINDVIOR_CAMERA = 18669;
   private static final int TOMARIS = 32552;
   private static final int ARTIUS = 32559;
   private static int LINDVIOR_SCENE_ID = 1;
   private static final int RESET_HOUR = 18;
   private static final int RESET_MIN = 58;
   private static final int RESET_DAY_1 = 3;
   private static final int RESET_DAY_2 = 6;
   private static boolean ALT_MODE = false;
   private static int ALT_MODE_MIN = 60;
   private Npc _lindviorCamera = null;
   private Npc _tomaris = null;
   private Npc _artius = null;

   private Lindvior(String name, String descr) {
      super(name, descr);
      this.scheduleNextLindviorVisit();
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      switch(event) {
         case "tomaris_shout1":
            this.broadcastNpcSay(npc, 23, NpcStringId.HUH_THE_SKY_LOOKS_FUNNY_WHATS_THAT);
            break;
         case "artius_shout":
            this.broadcastNpcSay(npc, 23, NpcStringId.A_POWERFUL_SUBORDINATE_IS_BEING_HELD_BY_THE_BARRIER_ORB_THIS_REACTION_MEANS);
            break;
         case "tomaris_shout2":
            this.broadcastNpcSay(npc, 23, NpcStringId.BE_CAREFUL_SOMETHINGS_COMING);
            break;
         case "lindvior_scene":
            if (npc != null) {
               for(Player pl : World.getInstance().getAroundPlayers(npc, 4000, 200)) {
                  if (pl.getZ() >= 1100 && pl.getZ() <= 3100) {
                     pl.showQuestMovie(LINDVIOR_SCENE_ID);
                  }
               }
            }
            break;
         case "start":
            for(Spawner spawn : SpawnParser.getInstance().getSpawnData()) {
               switch(spawn.getId()) {
                  case 18669:
                     this._lindviorCamera = spawn.getLastSpawn();
                     break;
                  case 32552:
                     this._tomaris = spawn.getLastSpawn();
                     break;
                  case 32559:
                     this._artius = spawn.getLastSpawn();
               }
            }

            this.startQuestTimer("tomaris_shout1", 1000L, this._tomaris, null);
            this.startQuestTimer("artius_shout", 60000L, this._artius, null);
            this.startQuestTimer("tomaris_shout2", 90000L, this._tomaris, null);
            this.startQuestTimer("lindvior_scene", 120000L, this._lindviorCamera, null);
            this.scheduleNextLindviorVisit();
      }

      return super.onAdvEvent(event, npc, player);
   }

   public void scheduleNextLindviorVisit() {
      long delay = ALT_MODE ? (long)(ALT_MODE_MIN * 60000) : this.scheduleNextLindviorDate();
      this.startQuestTimer("start", delay, null, null);
   }

   protected long scheduleNextLindviorDate() {
      GregorianCalendar date = new GregorianCalendar();
      date.set(12, 58);
      date.set(11, 18);
      if (System.currentTimeMillis() >= date.getTimeInMillis()) {
         date.add(7, 1);
      }

      int dayOfWeek = date.get(7);
      if (dayOfWeek <= 3) {
         date.add(7, 3 - dayOfWeek);
      } else if (dayOfWeek <= 6) {
         date.add(7, 6 - dayOfWeek);
      } else {
         date.add(7, 4);
      }

      return date.getTimeInMillis() - System.currentTimeMillis();
   }

   public static void main(String[] args) {
      new Lindvior(Lindvior.class.getSimpleName(), "individual");
   }
}
