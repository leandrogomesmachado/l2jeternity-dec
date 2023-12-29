package l2e.scripts.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class DragonVortex extends Quest {
   protected static final int VORTEX_1 = 32871;
   protected static final int VORTEX_2 = 32892;
   protected static final int VORTEX_3 = 32893;
   protected static final int VORTEX_4 = 32894;
   protected final List<Npc> bosses1 = new ArrayList<>();
   protected final List<Npc> bosses2 = new ArrayList<>();
   protected final List<Npc> bosses3 = new ArrayList<>();
   protected final List<Npc> bosses4 = new ArrayList<>();
   protected boolean progress1 = false;
   protected boolean progress2 = false;
   protected boolean progress3 = false;
   protected boolean progress4 = false;
   protected ScheduledFuture<?> _despawnTask1;
   protected ScheduledFuture<?> _despawnTask2;
   protected ScheduledFuture<?> _despawnTask3;
   protected ScheduledFuture<?> _despawnTask4;
   private long _timeDelay1 = 0L;
   private long _timeDelay2 = 0L;
   private long _timeDelay3 = 0L;
   private long _timeDelay4 = 0L;
   private static final int LARGE_DRAGON_BONE = 17248;
   private static final int[] RAIDS = new int[]{25724, 25723, 25722, 25721, 25720, 25719, 25718};
   protected Npc boss1;
   protected Npc boss2;
   protected Npc boss3;
   protected Npc boss4;
   protected int boss1ObjId = 0;
   protected int boss2ObjId = 0;
   protected int boss3ObjId = 0;
   protected int boss4ObjId = 0;
   private static Location[] BOSS_SPAWN_1 = new Location[]{
      new Location(91948, 113665, -3059), new Location(92486, 113568, -3072), new Location(92519, 114071, -3072), new Location(91926, 114162, -3072)
   };
   private static Location[] BOSS_SPAWN_2 = new Location[]{
      new Location(108953, 112366, -3047), new Location(108500, 112039, -3047), new Location(108977, 111575, -3047), new Location(109316, 112004, -3033)
   };
   private static Location[] BOSS_SPAWN_3 = new Location[]{
      new Location(109840, 125178, -3687), new Location(110461, 125227, -3687), new Location(110405, 125814, -3687), new Location(109879, 125828, -3686)
   };
   private static Location[] BOSS_SPAWN_4 = new Location[]{
      new Location(121543, 113580, -3793), new Location(120877, 113714, -3793), new Location(120848, 113058, -3793), new Location(121490, 113084, -3793)
   };
   private static final int DESPAWN_DELAY = 3600000;

   public DragonVortex(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addFirstTalkId(new int[]{32871, 32892, 32893, 32894});
      this.addStartNpc(new int[]{32871, 32892, 32893, 32894});
      this.addTalkId(new int[]{32871, 32892, 32893, 32894});

      for(int i : RAIDS) {
         this.addKillId(i);
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (event.equalsIgnoreCase("Spawn")) {
         if (npc.getId() == 32871) {
            if (!this.progress1 && (Config.VORTEX_DELAY_TIME <= 0 || this._timeDelay1 <= System.currentTimeMillis())) {
               if (hasQuestItems(player, 17248)) {
                  takeItems(player, 17248, 1L);
                  Location bossSpawn = BOSS_SPAWN_1[getRandom(0, BOSS_SPAWN_1.length - 1)];
                  if (Config.DRAGON_VORTEX_UNLIMITED_SPAWN) {
                     if (Config.VORTEX_DELAY_TIME > 0) {
                        this._timeDelay1 = System.currentTimeMillis() + (long)Config.VORTEX_DELAY_TIME * 1000L;
                     }

                     addSpawn(
                        RAIDS[getRandom(RAIDS.length)],
                        new Location(bossSpawn.getX(), bossSpawn.getY(), bossSpawn.getZ(), bossSpawn.getHeading()).rnd(50, 100, true),
                        false,
                        0L
                     );
                  } else {
                     this.boss1 = addSpawn(
                        RAIDS[getRandom(RAIDS.length)],
                        new Location(bossSpawn.getX(), bossSpawn.getY(), bossSpawn.getZ(), bossSpawn.getHeading()).rnd(50, 100, true),
                        false,
                        0L
                     );
                     this.progress1 = true;
                     if (this.boss1 != null) {
                        this.bosses1.add(this.boss1);
                        this.boss1ObjId = this.boss1.getObjectId();
                     }

                     this._despawnTask1 = ThreadPoolManager.getInstance().schedule(new DragonVortex.SpawnFirstVortrexBoss(), 3600000L);
                  }

                  return "32871-01.htm";
               }

               return "32871-02.htm";
            }

            return "32871-03.htm";
         }

         if (npc.getId() == 32892) {
            if (!this.progress2 && (Config.VORTEX_DELAY_TIME <= 0 || this._timeDelay2 <= System.currentTimeMillis())) {
               if (hasQuestItems(player, 17248)) {
                  takeItems(player, 17248, 1L);
                  Location bossSpawn = BOSS_SPAWN_2[getRandom(0, BOSS_SPAWN_2.length - 1)];
                  if (Config.DRAGON_VORTEX_UNLIMITED_SPAWN) {
                     if (Config.VORTEX_DELAY_TIME > 0) {
                        this._timeDelay2 = System.currentTimeMillis() + (long)Config.VORTEX_DELAY_TIME * 1000L;
                     }

                     addSpawn(
                        RAIDS[getRandom(RAIDS.length)],
                        new Location(bossSpawn.getX(), bossSpawn.getY(), bossSpawn.getZ(), bossSpawn.getHeading()).rnd(50, 100, true),
                        false,
                        0L
                     );
                  } else {
                     this.boss2 = addSpawn(
                        RAIDS[getRandom(RAIDS.length)],
                        new Location(bossSpawn.getX(), bossSpawn.getY(), bossSpawn.getZ(), bossSpawn.getHeading()).rnd(50, 100, true),
                        false,
                        0L
                     );
                     this.progress2 = true;
                     if (this.boss2 != null) {
                        this.bosses2.add(this.boss2);
                        this.boss2ObjId = this.boss2.getObjectId();
                     }

                     this._despawnTask2 = ThreadPoolManager.getInstance().schedule(new DragonVortex.SpawnSecondVortrexBoss(), 3600000L);
                  }

                  return "32871-01.htm";
               }

               return "32871-02.htm";
            }

            return "32871-03.htm";
         }

         if (npc.getId() == 32893) {
            if (!this.progress3 && (Config.VORTEX_DELAY_TIME <= 0 || this._timeDelay3 <= System.currentTimeMillis())) {
               if (hasQuestItems(player, 17248)) {
                  takeItems(player, 17248, 1L);
                  Location bossSpawn = BOSS_SPAWN_3[getRandom(0, BOSS_SPAWN_3.length - 1)];
                  if (Config.DRAGON_VORTEX_UNLIMITED_SPAWN) {
                     if (Config.VORTEX_DELAY_TIME > 0) {
                        this._timeDelay3 = System.currentTimeMillis() + (long)Config.VORTEX_DELAY_TIME * 1000L;
                     }

                     addSpawn(
                        RAIDS[getRandom(RAIDS.length)],
                        new Location(bossSpawn.getX(), bossSpawn.getY(), bossSpawn.getZ(), bossSpawn.getHeading()).rnd(50, 100, true),
                        false,
                        0L
                     );
                  } else {
                     this.boss3 = addSpawn(
                        RAIDS[getRandom(RAIDS.length)],
                        new Location(bossSpawn.getX(), bossSpawn.getY(), bossSpawn.getZ(), bossSpawn.getHeading()).rnd(50, 100, true),
                        false,
                        0L
                     );
                     this.progress3 = true;
                     if (this.boss3 != null) {
                        this.bosses3.add(this.boss3);
                        this.boss3ObjId = this.boss3.getObjectId();
                     }

                     this._despawnTask3 = ThreadPoolManager.getInstance().schedule(new DragonVortex.SpawnThirdVortrexBoss(), 3600000L);
                  }

                  return "32871-01.htm";
               }

               return "32871-02.htm";
            }

            return "32871-03.htm";
         }

         if (npc.getId() == 32894) {
            if (!this.progress4 && (Config.VORTEX_DELAY_TIME <= 0 || this._timeDelay4 <= System.currentTimeMillis())) {
               if (hasQuestItems(player, 17248)) {
                  takeItems(player, 17248, 1L);
                  Location bossSpawn = BOSS_SPAWN_4[getRandom(0, BOSS_SPAWN_4.length - 1)];
                  if (Config.DRAGON_VORTEX_UNLIMITED_SPAWN) {
                     if (Config.VORTEX_DELAY_TIME > 0) {
                        this._timeDelay4 = System.currentTimeMillis() + (long)Config.VORTEX_DELAY_TIME * 1000L;
                     }

                     addSpawn(
                        RAIDS[getRandom(RAIDS.length)],
                        new Location(bossSpawn.getX(), bossSpawn.getY(), bossSpawn.getZ(), bossSpawn.getHeading()).rnd(50, 100, true),
                        false,
                        0L
                     );
                  } else {
                     this.boss4 = addSpawn(
                        RAIDS[getRandom(RAIDS.length)],
                        new Location(bossSpawn.getX(), bossSpawn.getY(), bossSpawn.getZ(), bossSpawn.getHeading()).rnd(50, 100, true),
                        false,
                        0L
                     );
                     this.progress4 = true;
                     if (this.boss4 != null) {
                        this.bosses4.add(this.boss4);
                        this.boss4ObjId = this.boss4.getObjectId();
                     }

                     this._despawnTask4 = ThreadPoolManager.getInstance().schedule(new DragonVortex.SpawnFourthVortrexBoss(), 3600000L);
                  }

                  return "32871-01.htm";
               }

               return "32871-02.htm";
            }

            return "32871-03.htm";
         }
      }

      return super.onAdvEvent(event, npc, player);
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      return "32871.htm";
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      int npcObjId = npc.getObjectId();
      if (this.boss1ObjId != 0 && npcObjId == this.boss1ObjId && this.progress1) {
         this.progress1 = false;
         this.boss1ObjId = 0;
         this.bosses1.clear();
         if (this._despawnTask1 != null) {
            this._despawnTask1.cancel(true);
         }
      }

      if (this.boss2ObjId != 0 && npcObjId == this.boss2ObjId && this.progress2) {
         this.progress2 = false;
         this.boss2ObjId = 0;
         this.bosses2.clear();
         if (this._despawnTask2 != null) {
            this._despawnTask2.cancel(true);
         }
      }

      if (this.boss3ObjId != 0 && npcObjId == this.boss3ObjId && this.progress3) {
         this.progress3 = false;
         this.boss3ObjId = 0;
         this.bosses3.clear();
         if (this._despawnTask3 != null) {
            this._despawnTask3.cancel(true);
         }
      }

      if (this.boss4ObjId != 0 && npcObjId == this.boss4ObjId && this.progress4) {
         this.progress4 = false;
         this.boss4ObjId = 0;
         this.bosses4.clear();
         if (this._despawnTask4 != null) {
            this._despawnTask4.cancel(true);
         }
      }

      return super.onKill(npc, player, isSummon);
   }

   public static void main(String[] args) {
      new DragonVortex(-1, "DragonVortex", "custom");
   }

   protected class SpawnFirstVortrexBoss implements Runnable {
      @Override
      public void run() {
         if (!DragonVortex.this.bosses1.isEmpty()) {
            for(Npc boss : DragonVortex.this.bosses1) {
               if (boss != null) {
                  boss.deleteMe();
                  DragonVortex.this.progress1 = false;
               }
            }

            DragonVortex.this.boss1ObjId = 0;
            DragonVortex.this.bosses1.clear();
         }
      }
   }

   protected class SpawnFourthVortrexBoss implements Runnable {
      @Override
      public void run() {
         if (!DragonVortex.this.bosses4.isEmpty()) {
            for(Npc boss : DragonVortex.this.bosses4) {
               if (boss != null) {
                  boss.deleteMe();
                  DragonVortex.this.progress4 = false;
               }
            }

            DragonVortex.this.boss4ObjId = 0;
            DragonVortex.this.bosses4.clear();
         }
      }
   }

   protected class SpawnSecondVortrexBoss implements Runnable {
      @Override
      public void run() {
         if (!DragonVortex.this.bosses2.isEmpty()) {
            for(Npc boss : DragonVortex.this.bosses2) {
               if (boss != null) {
                  boss.deleteMe();
                  DragonVortex.this.progress2 = false;
               }
            }

            DragonVortex.this.boss2ObjId = 0;
            DragonVortex.this.bosses2.clear();
         }
      }
   }

   protected class SpawnThirdVortrexBoss implements Runnable {
      @Override
      public void run() {
         if (!DragonVortex.this.bosses3.isEmpty()) {
            for(Npc boss : DragonVortex.this.bosses3) {
               if (boss != null) {
                  boss.deleteMe();
                  DragonVortex.this.progress3 = false;
               }
            }

            DragonVortex.this.boss3ObjId = 0;
            DragonVortex.this.bosses3.clear();
         }
      }
   }
}
