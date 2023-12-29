package l2e.scripts.instances;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.instancemanager.SoIManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.funcs.FuncGet;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.network.serverpackets.NpcSay;

public class HeartInfinityAttack extends AbstractReflection {
   public HeartInfinityAttack(String name, String descr) {
      super(name, descr);
      this.addStartNpc(new int[]{32535, 32536, 32540});
      this.addTalkId(new int[]{32535, 32536, 32540});
      this.addSpawnId(new int[]{32535});
      this.addAttackId(29150);
      this.addKillId(new int[]{18708, 18711, 29150});
   }

   private final synchronized void enterInstance(Player player, Npc npc) {
      if (this.enterInstance(player, npc, new HeartInfinityAttack.HIAWorld(), 121)) {
         ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
         Reflection inst = ReflectionManager.getInstance().getReflection(world.getReflectionId());
         if (inst != null) {
            inst.spawnByGroup("soi_hoi_attack_init");
         }
      }
   }

   @Override
   protected void onTeleportEnter(Player player, ReflectionTemplate template, ReflectionWorld world, boolean firstEntrance) {
      if (firstEntrance) {
         world.addAllowed(player.getObjectId());
         player.getAI().setIntention(CtrlIntention.IDLE);
         player.setReflectionId(world.getReflectionId());
         Location teleLoc = template.getTeleportCoord();
         player.teleToLocation(teleLoc, true);
         if (player.hasSummon()) {
            player.getSummon().getAI().setIntention(CtrlIntention.IDLE);
            player.getSummon().setReflectionId(world.getReflectionId());
            player.getSummon().teleToLocation(teleLoc, true);
         }
      } else {
         player.getAI().setIntention(CtrlIntention.IDLE);
         player.setReflectionId(world.getReflectionId());
         Location teleLoc = template.getTeleportCoord();
         player.teleToLocation(teleLoc, true);
         if (player.hasSummon()) {
            player.getSummon().getAI().setIntention(CtrlIntention.IDLE);
            player.getSummon().setReflectionId(world.getReflectionId());
            player.getSummon().teleToLocation(teleLoc, true);
         }
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (npc.getId() == 32540) {
         this.enterInstance(player, npc);
      }

      return "";
   }

   protected void notifyEchmusEntrance(final HeartInfinityAttack.HIAWorld world, Player player) {
      if (!world.conquestBegun) {
         world.conquestBegun = true;
         world.invoker = player;
         ExShowScreenMessage msg = new ExShowScreenMessage(NpcStringId.YOU_WILL_PARTICIPATE_IN_S1_S2_SHORTLY_BE_PREPARED_FOR_ANYTHING, 2, 1, 8000);
         msg.addStringParameter("#" + NpcStringId.HEART_OF_IMMORTALITY.getId());
         msg.addStringParameter("#" + NpcStringId.ATTACK.getId());
         this.broadCastPacket(world, msg);
         ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
               for(int objId : world.getAllowed()) {
                  Player player = World.getInstance().getPlayer(objId);
                  player.showQuestMovie(2);
               }

               ThreadPoolManager.getInstance().schedule(new Runnable() {
                  @Override
                  public void run() {
                     HeartInfinityAttack.this.conquestBegins(world);
                  }
               }, 62500L);
            }
         }, 20000L);
      }
   }

   protected void conquestBegins(HeartInfinityAttack.HIAWorld world) {
      Reflection inst = ReflectionManager.getInstance().getReflection(world.getReflectionId());
      if (inst != null) {
         inst.despawnByGroup("soi_hoi_attack_init");
         inst.spawnByGroup("soi_hoi_attack_mob_1");
         inst.spawnByGroup("soi_hoi_attack_mob_2");
         inst.spawnByGroup("soi_hoi_attack_mob_3");
         inst.spawnByGroup("soi_hoi_attack_mob_4");
         inst.spawnByGroup("soi_hoi_attack_mob_5");
         inst.spawnByGroup("soi_hoi_attack_mob_6");
         inst.spawnByGroup("soi_hoi_attack_tumors");

         for(Npc n : inst.getNpcs()) {
            if (n != null && n.getId() == 18708 && n.getReflectionId() == world.getReflectionId()) {
               n.setCurrentHp(n.getMaxHp() * 0.5);
            }
         }

         inst.spawnByGroup("soi_hoi_attack_wards");
         world.tumorRespawnTime = 150000L;
         world.ekimus = addSpawn(29150, -179537, 208854, -15504, 16384, false, 0L, false, world.getReflectionId());
         world.hounds.add(addSpawn(29151, -179224, 209624, -15504, 16384, false, 0L, false, world.getReflectionId()));
         world.hounds.add(addSpawn(29151, -179880, 209464, -15504, 16384, false, 0L, false, world.getReflectionId()));
         world.lastAction = System.currentTimeMillis();
         world.ekimusActivityTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new HeartInfinityAttack.NotifyEkimusActivity(world), 60000L, 60000L);
         this.handleEkimusStats(world);

         for(int zoneId = 20040; zoneId < 20046; ++zoneId) {
            this.getActivatedZone(inst, zoneId, true);
         }

         inst.getDoor(14240102).openMe();
         this.broadCastPacket(
            world, new ExShowScreenMessage(NpcStringId.YOU_CAN_HEAR_THE_UNDEAD_OF_EKIMUS_RUSHING_TOWARD_YOU_S1_S2_IT_HAS_NOW_BEGUN, 2, 1, 8000)
         );
         if (world.invoker != null) {
            world.ekimus.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, world.invoker, Integer.valueOf(50000));
            NpcSay cs = new NpcSay(
               world.ekimus.getObjectId(), 1, world.ekimus.getId(), NpcStringId.I_SHALL_ACCEPT_YOUR_CHALLENGE_S1_COME_AND_DIE_IN_THE_ARMS_OF_IMMORTALITY
            );
            cs.addStringParameter(world.invoker.getName());
            world.ekimus.broadcastPacket(cs);
         }

         world.startTime = System.currentTimeMillis();
         world.timerTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new HeartInfinityAttack.TimerTask(world), 298000L, 300000L);
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getPlayerWorld(player);
      if (tmpworld instanceof HeartInfinityAttack.HIAWorld) {
         HeartInfinityAttack.HIAWorld world = (HeartInfinityAttack.HIAWorld)tmpworld;
         if (event.startsWith("warpechmus")) {
            ExShowScreenMessage msg = new ExShowScreenMessage(
               NpcStringId.S1S_PARTY_HAS_MOVED_TO_A_DIFFERENT_LOCATION_THROUGH_THE_CRACK_IN_THE_TUMOR, 2, 1, 8000
            );
            msg.addStringParameter(player.getParty() != null ? player.getParty().getLeader().getName() : player.getName());
            this.broadCastPacket(world, msg);

            for(Player partyMember : player.getParty().getMembers()) {
               if (partyMember.isInsideRadius(player, 800, true, false)) {
                  partyMember.teleToLocation(-179548, 209584, -15504, true);
               }
            }

            this.notifyEchmusEntrance(world, player);
         } else if (event.startsWith("reenterechmus")) {
            player.destroyItemByItemId("SOI", 13797, 3L, player, true);
            this.notifyEkimusRoomEntrance(world);

            for(Player partyMember : player.getParty().getMembers()) {
               if (partyMember.isInsideRadius(player, 400, true, false)) {
                  partyMember.teleToLocation(-179548, 209584, -15504, true);
               }
            }
         } else if (event.startsWith("warp") && !world.deadTumors.isEmpty()) {
            player.destroyItemByItemId("SOI", 13797, 1L, player, true);
            Location loc = world.deadTumors.get(getRandom(world.deadTumors.size())).getLocation();
            if (loc != null) {
               ExShowScreenMessage msg = new ExShowScreenMessage(
                  NpcStringId.S1S_PARTY_HAS_MOVED_TO_A_DIFFERENT_LOCATION_THROUGH_THE_CRACK_IN_THE_TUMOR, 2, 1, 8000
               );
               msg.addStringParameter(player.getParty() != null ? player.getParty().getLeader().getName() : player.getName());
               this.broadCastPacket(world, msg);

               for(Player partyMember : player.getParty().getMembers()) {
                  if (partyMember.isInsideRadius(player, 500, true, false)) {
                     partyMember.teleToLocation(loc, true);
                  }
               }
            }
         }
      }

      return "";
   }

   @Override
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld instanceof HeartInfinityAttack.HIAWorld) {
         HeartInfinityAttack.HIAWorld world = (HeartInfinityAttack.HIAWorld)tmpworld;
         if (npc.getId() == 29150) {
            if (world.faildAnnounce) {
               world.faildAnnounce = false;
            }

            world.lastAction = System.currentTimeMillis();

            for(Npc mob : world.hounds) {
               ((MonsterInstance)mob).addDamageHate(attacker, 0, 500);
               mob.setRunning();
               mob.getAI().setIntention(CtrlIntention.ATTACK, attacker);
            }
         }
      }

      return super.onAttack(npc, attacker, damage, isSummon, skill);
   }

   @Override
   public final String onSpawn(Npc npc) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld instanceof HeartInfinityAttack.HIAWorld) {
         HeartInfinityAttack.HIAWorld world = (HeartInfinityAttack.HIAWorld)tmpworld;
         if (npc.getId() == 32535) {
            world.addTag(1);
         }
      }

      return super.onSpawn(npc);
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld instanceof HeartInfinityAttack.HIAWorld) {
         HeartInfinityAttack.HIAWorld world = (HeartInfinityAttack.HIAWorld)tmpworld;
         if (npc.getId() == 18708) {
            ((MonsterInstance)npc).dropItem(player, 13797, (long)getRandom(2, 5));
            npc.deleteMe();
            --world.tumorCount;
            this.notifyTumorDeath(world);
            Npc deadTumor = this.spawnNpc(32535, npc.getLocation(), 0, world.getReflectionId());
            world.deadTumors.add(deadTumor);
            ThreadPoolManager.getInstance().schedule(new HeartInfinityAttack.TumorRevival(deadTumor, world), world.tumorRespawnTime);
            ThreadPoolManager.getInstance().schedule(new HeartInfinityAttack.RegenerationCoffinSpawn(deadTumor, world), 20000L);
         } else if (npc.getId() == 29150) {
            this.conquestConclusion(world, true);
            SoIManager.notifyEkimusKill();
         } else if (npc.getId() == 18711) {
            world.tumorRespawnTime = world.tumorRespawnTime + 8000L;
         }
      }

      return "";
   }

   private void notifyTumorDeath(HeartInfinityAttack.HIAWorld world) {
      if (world.tumorCount < 1) {
         world.houndBlocked = true;

         for(Npc hound : world.hounds) {
            hound.block();
         }

         this.broadCastPacket(
            world,
            new ExShowScreenMessage(NpcStringId.WITH_ALL_CONNECTIONS_TO_THE_TUMOR_SEVERED_EKIMUS_HAS_LOST_ITS_POWER_TO_CONTROL_THE_FERAL_HOUND, 2, 1, 8000)
         );
      } else {
         ExShowScreenMessage msg = new ExShowScreenMessage(NpcStringId.THE_TUMOR_INSIDE_S1_THAT_HAS_PROVIDED_ENERGY_N_TO_EKIMUS_IS_DESTROYED, 2, 1, 8000);
         msg.addStringParameter("#" + NpcStringId.HEART_OF_IMMORTALITY.getId());
         this.broadCastPacket(world, msg);
      }

      this.handleEkimusStats(world);
   }

   protected void notifyTumorRevival(HeartInfinityAttack.HIAWorld world) {
      if (world.tumorCount == 1 && world.houndBlocked) {
         world.houndBlocked = false;

         for(Npc hound : world.hounds) {
            hound.unblock();
         }

         this.broadCastPacket(
            world, new ExShowScreenMessage(NpcStringId.WITH_THE_CONNECTION_TO_THE_TUMOR_RESTORED_EKIMUS_HAS_REGAINED_CONTROL_OVER_THE_FERAL_HOUND, 2, 1, 8000)
         );
      } else {
         ExShowScreenMessage msg = new ExShowScreenMessage(
            NpcStringId.THE_TUMOR_INSIDE_S1_HAS_BEEN_COMPLETELY_RESURRECTED_N_AND_STARTED_TO_ENERGIZE_EKIMUS_AGAIN, 2, 1, 8000
         );
         msg.addStringParameter("#" + NpcStringId.HEART_OF_IMMORTALITY.getId());
         this.broadCastPacket(world, msg);
      }

      this.handleEkimusStats(world);
   }

   private void handleEkimusStats(HeartInfinityAttack.HIAWorld world) {
      double[] a = this.getStatMultiplier(world);
      world.ekimus.removeStatsOwner(this);
      world.ekimus.addStatFunc(new FuncGet(Stats.POWER_ATTACK, 48, this, world.ekimus.getTemplate().getBasePAtk() * 3.0));
      world.ekimus.addStatFunc(new FuncGet(Stats.MAGIC_ATTACK, 48, this, world.ekimus.getTemplate().getBaseMAtk() * 10.0));
      world.ekimus.addStatFunc(new FuncGet(Stats.POWER_DEFENCE, 48, this, world.ekimus.getTemplate().getBasePDef() * a[1]));
      world.ekimus.addStatFunc(new FuncGet(Stats.MAGIC_DEFENCE, 48, this, world.ekimus.getTemplate().getBaseMDef() * a[0]));
      world.ekimus.addStatFunc(new FuncGet(Stats.REGENERATE_HP_RATE, 48, this, world.ekimus.getTemplate().getBaseHpReg() * a[2]));
   }

   private double[] getStatMultiplier(HeartInfinityAttack.HIAWorld world) {
      double[] a = new double[3];
      switch(world.tumorCount) {
         case 0:
            a[0] = 0.12;
            a[1] = 0.06;
            a[2] = 0.25;
            break;
         case 1:
            a[0] = 0.3;
            a[1] = 0.15;
            a[2] = 1.0;
            break;
         case 2:
            a[0] = 0.7;
            a[1] = 0.3;
            a[2] = 2.0;
            break;
         case 3:
            a[0] = 1.0;
            a[1] = 0.4;
            a[2] = 2.5;
            break;
         case 4:
            a[0] = 1.5;
            a[1] = 0.6;
            a[2] = 3.0;
            break;
         case 5:
            a[0] = 1.9;
            a[1] = 0.9;
            a[2] = 3.5;
            break;
         case 6:
            a[0] = 2.0;
            a[1] = 1.0;
            a[2] = 4.0;
      }

      return a;
   }

   public void notifyEkimusRoomEntrance(final HeartInfinityAttack.HIAWorld world) {
      for(Player ch : ZoneManager.getInstance().getZoneById(200032).getPlayersInside()) {
         if (ch != null) {
            ch.teleToLocation(-179537, 211233, -15472, true);
         }
      }

      ThreadPoolManager.getInstance()
         .schedule(
            new Runnable() {
               @Override
               public void run() {
                  HeartInfinityAttack.this.broadCastPacket(
                     world, new ExShowScreenMessage(NpcStringId.EKIMUS_HAS_SENSED_ABNORMAL_ACTIVITY_NTHE_ADVANCING_PARTY_IS_FORCEFULLY_EXPELLED, 2, 1, 8000)
                  );
               }
            },
            10000L
         );
   }

   protected void conquestConclusion(HeartInfinityAttack.HIAWorld world, boolean win) {
      Reflection inst = ReflectionManager.getInstance().getReflection(world.getReflectionId());
      if (inst != null) {
         if (world.timerTask != null) {
            world.timerTask.cancel(false);
         }

         if (world.ekimusActivityTask != null) {
            world.ekimusActivityTask.cancel(false);
         }

         world.conquestEnded = true;
         inst.despawnByGroup("soi_hoi_attack_wards");
         inst.despawnByGroup("soi_hoi_attack_mob_1");
         inst.despawnByGroup("soi_hoi_attack_mob_2");
         inst.despawnByGroup("soi_hoi_attack_mob_3");
         inst.despawnByGroup("soi_hoi_attack_mob_4");
         inst.despawnByGroup("soi_hoi_attack_mob_5");
         inst.despawnByGroup("soi_hoi_attack_mob_6");
         inst.despawnByGroup("soi_hoi_attack_bosses");
         if (world.ekimus != null && !world.ekimus.isDead()) {
            world.ekimus.deleteMe();
         }

         for(Npc npc : world.hounds) {
            if (npc != null) {
               npc.deleteMe();
            }
         }

         if (win) {
            this.finishInstance(world, 900000, true);
            ExShowScreenMessage msg = new ExShowScreenMessage(
               NpcStringId.CONGRATULATIONS_YOU_HAVE_SUCCEEDED_AT_S1_S2_THE_INSTANCE_WILL_SHORTLY_EXPIRE, 2, 1, 8000
            );
            msg.addStringParameter("#" + NpcStringId.HEART_OF_IMMORTALITY.getId());
            msg.addStringParameter("#" + NpcStringId.ATTACK.getId());
            this.broadCastPacket(world, msg);
            this.handleReenterTime(world);
         } else {
            this.finishInstance(world, 900000, false);
            ExShowScreenMessage msg = new ExShowScreenMessage(NpcStringId.YOU_HAVE_FAILED_AT_S1_S2_THE_INSTANCE_WILL_SHORTLY_EXPIRE, 2, 1, 8000);
            msg.addStringParameter("#" + NpcStringId.HEART_OF_IMMORTALITY.getId());
            msg.addStringParameter("#" + NpcStringId.ATTACK.getId());
            this.broadCastPacket(world, msg);
         }

         for(Npc npc : inst.getNpcs()) {
            if (npc != null && (npc.getId() == 18708 || npc.getId() == 32535 || npc.getId() == 18710)) {
               npc.deleteMe();
            }
         }
      }
   }

   protected void broadCastPacket(HeartInfinityAttack.HIAWorld world, GameServerPacket packet) {
      for(int objId : world.getAllowed()) {
         Player player = World.getInstance().getPlayer(objId);
         if (player != null && player.isOnline() && player.getReflectionId() == world.getReflectionId()) {
            player.sendPacket(packet);
         }
      }
   }

   public static void main(String[] args) {
      new HeartInfinityAttack(HeartInfinityAttack.class.getSimpleName(), "instances");
   }

   private class HIAWorld extends ReflectionWorld {
      private long tumorRespawnTime;
      private Player invoker;
      private boolean conquestBegun = false;
      protected boolean conquestEnded = false;
      private boolean houndBlocked = false;
      public List<Npc> deadTumors = new ArrayList<>();
      protected Npc ekimus;
      protected List<Npc> hounds = new ArrayList<>(2);
      public int tumorCount = 6;
      public long startTime = 0L;
      protected ScheduledFuture<?> timerTask;
      protected ScheduledFuture<?> ekimusActivityTask;
      private long lastAction = 0L;
      private boolean faildAnnounce = false;

      public synchronized void addTag(int value) {
         this.setTag(this.getTag() + value);
      }

      public HIAWorld() {
         this.setTag(-1);
      }
   }

   protected class NotifyEkimusActivity implements Runnable {
      private final HeartInfinityAttack.HIAWorld _world;

      public NotifyEkimusActivity(HeartInfinityAttack.HIAWorld world) {
         this._world = world;
      }

      @Override
      public void run() {
         if (ReflectionManager.getInstance().getWorld(this._world.getReflectionId()) == this._world) {
            Long temp = System.currentTimeMillis() - this._world.lastAction;
            if (temp >= 120000L && !this._world.faildAnnounce) {
               ExShowScreenMessage msg = new ExShowScreenMessage(
                  NpcStringId.THERE_IS_NO_PARTY_CURRENTLY_CHALLENGING_EKIMUS_N_IF_NO_PARTY_ENTERS_WITHIN_S1_SECONDS_THE_ATTACK_ON_THE_HEART_OF_IMMORTALITY_WILL_FAIL,
                  2,
                  1,
                  8000
               );
               msg.addStringParameter("60");
               HeartInfinityAttack.this.broadCastPacket(this._world, msg);
               this._world.faildAnnounce = true;
            } else if (temp >= 180000L) {
               ThreadPoolManager.getInstance().schedule(new Runnable() {
                  @Override
                  public void run() {
                     HeartInfinityAttack.this.conquestConclusion(NotifyEkimusActivity.this._world, false);
                  }
               }, 8000L);
            }
         }
      }
   }

   private class RegenerationCoffinSpawn implements Runnable {
      private final Npc _deadTumor;
      private final HeartInfinityAttack.HIAWorld _world;

      public RegenerationCoffinSpawn(Npc deadTumor, HeartInfinityAttack.HIAWorld world) {
         this._deadTumor = deadTumor;
         this._world = world;
      }

      @Override
      public void run() {
         if (ReflectionManager.getInstance().getWorld(this._world.getReflectionId()) == this._world) {
            if (!this._world.conquestEnded) {
               for(int i = 0; i < 4; ++i) {
                  HeartInfinityAttack.this.spawnNpc(18710, this._deadTumor.getLocation(), 0, this._world.getReflectionId());
               }
            }
         }
      }
   }

   private class TimerTask implements Runnable {
      private final HeartInfinityAttack.HIAWorld _world;

      TimerTask(HeartInfinityAttack.HIAWorld world) {
         this._world = world;
      }

      @Override
      public void run() {
         if (ReflectionManager.getInstance().getWorld(this._world.getReflectionId()) == this._world) {
            long time = (this._world.startTime + 1500000L - System.currentTimeMillis()) / 60000L;
            if (time == 0L) {
               HeartInfinityAttack.this.conquestConclusion(this._world, false);
            } else {
               if (time == 20L) {
                  Reflection inst = ReflectionManager.getInstance().getReflection(this._world.getReflectionId());
                  if (inst != null) {
                     inst.spawnByGroup("soi_hoi_attack_bosses");
                  }
               }

               ExShowScreenMessage msg = new ExShowScreenMessage(NpcStringId.S1_MINUTES_REMAINING, 2, 1, 8000);
               msg.addStringParameter(Integer.toString((int)(this._world.startTime + 1500000L - System.currentTimeMillis()) / 60000));
               HeartInfinityAttack.this.broadCastPacket(this._world, msg);
            }
         }
      }
   }

   private class TumorRevival implements Runnable {
      private final Npc _deadTumor;
      private final HeartInfinityAttack.HIAWorld _world;

      public TumorRevival(Npc deadTumor, HeartInfinityAttack.HIAWorld world) {
         this._deadTumor = deadTumor;
         this._world = world;
      }

      @Override
      public void run() {
         if (ReflectionManager.getInstance().getWorld(this._world.getReflectionId()) == this._world) {
            if (!this._world.conquestEnded) {
               Npc alivetumor = HeartInfinityAttack.this.spawnNpc(18708, this._deadTumor.getLocation(), 0, this._world.getReflectionId());
               alivetumor.setCurrentHp(alivetumor.getMaxHp() * 0.25);
               ++this._world.tumorCount;
               HeartInfinityAttack.this.notifyTumorRevival(this._world);
               this._world.deadTumors.add(this._deadTumor);
               this._deadTumor.deleteMe();
               this._world.addTag(-1);
            }
         }
      }
   }
}
