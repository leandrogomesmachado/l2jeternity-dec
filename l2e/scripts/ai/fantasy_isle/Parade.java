package l2e.scripts.ai.fantasy_isle;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import l2e.gameserver.Config;
import l2e.gameserver.GameTimeController;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.quest.Quest;
import l2e.scripts.ai.AbstractNpcAI;

public class Parade extends AbstractNpcAI {
   protected final int[] ACTORS = new int[]{
      32381,
      32379,
      32381,
      32382,
      32383,
      32384,
      32381,
      32385,
      32381,
      32384,
      32383,
      32382,
      32386,
      32387,
      32388,
      32389,
      32390,
      32391,
      32392,
      32393,
      32394,
      32395,
      32396,
      32397,
      32398,
      32399,
      32400,
      32401,
      32402,
      32403,
      32404,
      32405,
      32406,
      32407,
      32408,
      32409,
      32411,
      32412,
      32413,
      32414,
      32415,
      32416,
      32417,
      32418,
      32419,
      32420,
      32421,
      32422,
      32423,
      32429,
      32430,
      32447,
      32448,
      32449,
      32450,
      32451,
      32452,
      32453,
      32454,
      32455,
      32456,
      0,
      0,
      0,
      32415
   };
   private final int[][] START1 = new int[][]{{-54780, -56810, -2015, 49152}, {-54860, -56810, -2015, 49152}, {-54940, -56810, -2015, 49152}};
   private final int[][] GOAL1 = new int[][]{{-54780, -57965, -2015, 49152}, {-54860, -57965, -2015, 49152}, {-54940, -57965, -2015, 49152}};
   private final int[][] START2 = new int[][]{{-55715, -58900, -2015, 32768}, {-55715, -58820, -2015, 32768}, {-55715, -58740, -2015, 32768}};
   private final int[][] GOAL2 = new int[][]{{-60850, -58900, -2015, 32768}, {-60850, -58820, -2015, 32768}, {-60850, -58740, -2015, 32768}};
   private final int[][] START3 = new int[][]{{-61790, -57965, -2015, 16384}, {-61710, -57965, -2015, 16384}, {-61630, -57965, -2015, 16384}};
   private final int[][] GOAL3 = new int[][]{{-61790, -53890, -2116, 16384}, {-61710, -53890, -2116, 16384}, {-61630, -53890, -2116, 16384}};
   private final int[][] START4 = new int[][]{{-60840, -52990, -2108, 0}, {-60840, -53070, -2108, 0}, {-60840, -53150, -2108, 0}};
   private final int[][] GOAL4 = new int[][]{{-58620, -52990, -2015, 0}, {-58620, -53070, -2015, 0}, {-58620, -53150, -2015, 0}};
   private final int[][] START5 = new int[][]{{-57233, -53554, -2015, 57344}, {-57290, -53610, -2015, 57344}, {-57346, -53667, -2015, 57344}};
   private final int[][] GOAL5 = new int[][]{{-55338, -55435, -2015, 57344}, {-55395, -55491, -2015, 57344}, {-55451, -55547, -2015, 57344}};
   protected final int[][][] START = new int[][][]{this.START1, this.START2, this.START3, this.START4, this.START5};
   protected final int[][][] GOAL = new int[][][]{this.GOAL1, this.GOAL2, this.GOAL3, this.GOAL4, this.GOAL5};
   protected ScheduledFuture<?> spawnTask;
   protected ScheduledFuture<?> deleteTask;
   protected ScheduledFuture<?> cleanTask;
   protected int npcIndex;
   protected List<Npc> spawns;

   protected void load() {
      this.npcIndex = 0;
      this.spawns = new CopyOnWriteArrayList<>();
   }

   protected void clean() {
      if (this.spawns != null) {
         this.spawns.forEach(Npc::deleteMe);
      }

      this.spawns = null;
   }

   public Parade(String name, String descr) {
      super(name, descr);
      long diff = this.timeLeftMilli(8, 0, 0);
      long cycle = 3600000L;
      SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
      if (Config.DEBUG) {
         this._log
            .info(
               "Fantasy Isle: Parade script starting at "
                  + format.format(Long.valueOf(System.currentTimeMillis() + diff))
                  + " and is scheduled each next "
                  + 1L
                  + " hours."
            );
      }

      ThreadPoolManager.getInstance().scheduleAtFixedRate(new Parade.Start(), diff, 3600000L);
   }

   private long timeLeftMilli(int hh, int mm, int ss) {
      int now = GameTimeController.getInstance().getGameTicks() * 60 / 100;
      int dd = hh * 3600 + mm * 60 + ss - now % 86400;
      if (dd < 0) {
         dd += 86400;
      }

      return (long)dd * 1000L / 6L;
   }

   public static void main(String[] args) {
      new Parade(Parade.class.getSimpleName(), "ai");
   }

   protected class Clean implements Runnable {
      @Override
      public void run() {
         Parade.this.spawnTask.cancel(false);
         Parade.this.spawnTask = null;
         Parade.this.deleteTask.cancel(false);
         Parade.this.deleteTask = null;
         Parade.this.cleanTask.cancel(false);
         Parade.this.cleanTask = null;
         Parade.this.clean();
      }
   }

   protected class Delete implements Runnable {
      @Override
      public void run() {
         if (Parade.this.spawns.size() > 0) {
            for(Npc actor : Parade.this.spawns) {
               if (actor != null) {
                  if (actor.getPlanDistanceSq(actor.getXdestination(), actor.getYdestination()) < 10000.0) {
                     actor.deleteMe();
                     Parade.this.spawns.remove(actor);
                  } else if (!actor.isMoving()) {
                     actor.getAI()
                        .setIntention(
                           CtrlIntention.MOVING, new Location(actor.getXdestination(), actor.getYdestination(), actor.getZdestination(), actor.getHeading())
                        );
                  }
               }
            }

            if (Parade.this.spawns.size() == 0) {
               Parade.this.deleteTask.cancel(false);
            }
         }
      }
   }

   protected class Spawn implements Runnable {
      @Override
      public void run() {
         for(int i = 0; i < 3; ++i) {
            if (Parade.this.npcIndex >= Parade.this.ACTORS.length) {
               Parade.this.spawnTask.cancel(false);
               break;
            }

            int npcId = Parade.this.ACTORS[Parade.this.npcIndex++];
            if (npcId != 0) {
               for(int route = 0; route < 5; ++route) {
                  int[] start = Parade.this.START[route][i];
                  int[] goal = Parade.this.GOAL[route][i];
                  Npc actor = Quest.addSpawn(npcId, start[0], start[1], start[2], start[3], false, 0L);
                  actor.getAI().setIntention(CtrlIntention.MOVING, new Location(goal[0], goal[1], goal[2], goal[3]));
                  Parade.this.spawns.add(actor);
               }
            }
         }
      }
   }

   protected class Start implements Runnable {
      @Override
      public void run() {
         Parade.this.load();
         Parade.this.spawnTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(Parade.this.new Spawn(), 0L, 5000L);
         Parade.this.deleteTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(Parade.this.new Delete(), 10000L, 1000L);
         Parade.this.cleanTask = ThreadPoolManager.getInstance().schedule(Parade.this.new Clean(), 420000L);
      }
   }
}
