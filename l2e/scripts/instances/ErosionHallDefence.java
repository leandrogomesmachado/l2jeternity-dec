package l2e.scripts.instances;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlEvent;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.actor.instance.QuestGuardInstance;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.zone.ZoneType;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.network.serverpackets.NpcSay;

public class ErosionHallDefence extends AbstractReflection {
   public ErosionHallDefence(String name, String descr) {
      super(name, descr);
      this.addStartNpc(new int[]{32535, 32537});
      this.addTalkId(new int[]{32535, 32537});
      this.addSpawnId(new int[]{32535, 32541});
      this.addEnterZoneId(new int[]{20014});
      this.addKillId(new int[]{18708, 18711, 32541});
   }

   @Override
   public final String onEnterZone(Creature character, ZoneType zone) {
      if (character.isPlayer()) {
         ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(character.getReflectionId());
         if (tmpworld instanceof ErosionHallDefence.HEDWorld) {
            ErosionHallDefence.HEDWorld world = (ErosionHallDefence.HEDWorld)tmpworld;
            if (!world.conquestBegun) {
               world.conquestBegun = true;
               this.runTumors(world);
               world.startTime = System.currentTimeMillis();
               world.timerTask = ThreadPoolManager.getInstance().schedule(new ErosionHallDefence.TimerTask(world), 1200000L);
            }
         }
      }

      return super.onEnterZone(character, zone);
   }

   private final synchronized void enterInstance(Player player, Npc npc) {
      this.enterInstance(player, npc, new ErosionHallDefence.HEDWorld(), 120);
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

   protected void runTumors(final ErosionHallDefence.HEDWorld world) {
      final Reflection inst = ReflectionManager.getInstance().getReflection(world.getReflectionId());
      if (inst != null) {
         inst.spawnByGroup("soi_hoe_defence_lifeseed");
         inst.spawnByGroup("soi_hoe_defence_tumor");
         inst.spawnByGroup("soi_hoe_defence_wards");
         inst.spawnByGroup("soi_hoe_defence_mob_1");
         inst.spawnByGroup("soi_hoe_defence_mob_2");
         inst.spawnByGroup("soi_hoe_defence_mob_3");
         inst.spawnByGroup("soi_hoe_defence_mob_4");
         inst.spawnByGroup("soi_hoe_defence_mob_5");
         inst.spawnByGroup("soi_hoe_defence_mob_6");
         inst.spawnByGroup("soi_hoe_defence_mob_7");
         inst.spawnByGroup("soi_hoe_defence_mob_8");

         for(int zoneId = 20008; zoneId < 20029; ++zoneId) {
            this.getActivatedZone(inst, zoneId, true);
         }

         world.agressionTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
               if (!world.conquestEnded) {
                  for(final Npc npc : inst.getNpcs()) {
                     Npc seed = ErosionHallDefence.getNearestSeed(npc);
                     if (seed != null && npc.getAI().getIntention() == CtrlIntention.ACTIVE) {
                        npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, seed, Integer.valueOf(100));
                        ThreadPoolManager.getInstance().schedule(new Runnable() {
                           @Override
                           public void run() {
                              if (npc instanceof Attackable) {
                                 ((Attackable)npc).clearAggroList();
                                 npc.getAI().setIntention(CtrlIntention.ACTIVE);
                                 npc.getAI().setIntention(CtrlIntention.MOVING, Location.findPointToStay(npc, 400, npc.getGeoIndex(), true));
                              }
                           }
                        }, 7000L);
                     }
                  }
               }
            }
         }, 15000L, 25000L);
         world.coffinSpawnTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
               if (!world.conquestEnded) {
                  for(Npc npc : world.deadTumors) {
                     if (npc != null) {
                        ErosionHallDefence.this.spawnNpc(18709, npc.getLocation(), 0, world.getReflectionId());
                     }
                  }
               }
            }
         }, 1000L, 60000L);
         world.aliveTumorSpawnTask = ThreadPoolManager.getInstance()
            .schedule(
               new Runnable() {
                  @Override
                  public void run() {
                     if (!world.conquestEnded) {
                        inst.despawnByGroup("soi_hoe_defence_tumor");
                        inst.spawnByGroup("soi_hoe_defence_alivetumor");
      
                        for(Npc npc : inst.getNpcs()) {
                           if (npc != null && npc.getId() == 18708) {
                              npc.setCurrentHp(npc.getMaxHp() * 0.5);
                           }
                        }
      
                        ExShowScreenMessage msg = new ExShowScreenMessage(
                           NpcStringId.THE_TUMOR_INSIDE_S1_HAS_COMPLETELY_REVIVED_NRECOVERED_NEARBY_UNDEAD_ARE_SWARMING_TOWARD_SEED_OF_LIFE, 2, 1, 8000
                        );
                        msg.addStringParameter("#" + NpcStringId.HALL_OF_EROSION.getId());
                        ErosionHallDefence.this.broadCastPacket(world, msg);
                     }
                  }
               },
               world.tumorRespawnTime
            );
      }

      this.broadCastPacket(world, new ExShowScreenMessage(NpcStringId.YOU_CAN_HEAR_THE_UNDEAD_OF_EKIMUS_RUSHING_TOWARD_YOU_S1_S2_IT_HAS_NOW_BEGUN, 2, 1, 8000));
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getPlayerWorld(player);
      if (tmpworld instanceof ErosionHallDefence.HEDWorld) {
         ErosionHallDefence.HEDWorld world = (ErosionHallDefence.HEDWorld)tmpworld;
         if (event.startsWith("warp") && !world.deadTumors.isEmpty()) {
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
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (npc.getId() == 32537) {
         this.enterInstance(player, npc);
      }

      return null;
   }

   @Override
   public final String onSpawn(Npc npc) {
      switch(npc.getId()) {
         case 32535:
            ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
            if (tmpworld != null && tmpworld instanceof ErosionHallDefence.HEDWorld) {
               ((ErosionHallDefence.HEDWorld)tmpworld).deadTumors.add(npc);
               ((ErosionHallDefence.HEDWorld)tmpworld).addTag(1);
            }
            break;
         case 32541:
            ((QuestGuardInstance)npc).setPassive(true);
            npc.setCurrentHp(500000.0);
      }

      return super.onSpawn(npc);
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld instanceof ErosionHallDefence.HEDWorld) {
         final ErosionHallDefence.HEDWorld world = (ErosionHallDefence.HEDWorld)tmpworld;
         if (npc.getId() == 18708) {
            ((MonsterInstance)npc).dropItem(player, 13797, (long)getRandom(2, 5));
            world.alivetumor.remove(npc);
            npc.deleteMe();
            this.notifyTumorDeath(world);
            final Npc n = this.spawnNpc(32535, npc.getLocation(), 0, world.getReflectionId());
            ExShowScreenMessage msg = new ExShowScreenMessage(
               NpcStringId.THE_TUMOR_INSIDE_S1_HAS_BEEN_DESTROYED_NTHE_NEARBY_UNDEAD_THAT_WERE_ATTACKING_SEED_OF_LIFE_START_LOSING_THEIR_ENERGY_AND_RUN_AWAY,
               2,
               1,
               8000
            );
            msg.addStringParameter("#" + NpcStringId.HALL_OF_EROSION.getId());
            this.broadCastPacket(world, msg);
            ThreadPoolManager.getInstance()
               .schedule(
                  new Runnable() {
                     @Override
                     public void run() {
                        world.deadTumors.remove(n);
                        n.deleteMe();
                        Npc tumor = ErosionHallDefence.this.spawnNpc(18708, n.getLocation(), 0, world.getReflectionId());
                        tumor.setCurrentHp(tumor.getMaxHp() * 0.25);
                        world.alivetumor.add(tumor);
                        --world.tumorKillCount;
                        ExShowScreenMessage msg = new ExShowScreenMessage(
                           NpcStringId.THE_TUMOR_INSIDE_S1_HAS_COMPLETELY_REVIVED_NRECOVERED_NEARBY_UNDEAD_ARE_SWARMING_TOWARD_SEED_OF_LIFE, 2, 1, 8000
                        );
                        msg.addStringParameter("#" + NpcStringId.HALL_OF_EROSION.getId());
                        ErosionHallDefence.this.broadCastPacket(world, msg);
                     }
                  },
                  world.tumorRespawnTime
               );
         } else if (npc.getId() == 25636) {
            this.conquestConclusion(world, true);
         }

         if (npc.getId() == 18711) {
            world.tumorRespawnTime = world.tumorRespawnTime - 5000L;
         }
      }

      return super.onKill(npc, player, isSummon);
   }

   private void notifyTumorDeath(ErosionHallDefence.HEDWorld world) {
      ++world.tumorKillCount;
      if (world.tumorKillCount >= 4 && !world.soulwagonSpawned) {
         world.soulwagonSpawned = true;
         Reflection inst = ReflectionManager.getInstance().getReflection(world.getReflectionId());
         if (inst != null) {
            inst.spawnByGroup("soi_hoe_defence_soulwagon");

            for(Npc npc : inst.getNpcs()) {
               if (npc != null && npc.getId() == 25636) {
                  NpcSay cs = new NpcSay(npc.getObjectId(), 1, npc.getId(), NpcStringId.HA_HA_HA);
                  npc.broadcastPacket(cs);
                  Npc seed = getNearestSeed(npc);
                  if (seed != null) {
                     npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, seed, Integer.valueOf(100));
                  }

                  this.rescheduleFailureTask(world, 180000L);
               }
            }
         }
      }
   }

   @Override
   public String onKillByMob(Npc npc, Npc killer) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld instanceof ErosionHallDefence.HEDWorld) {
         ErosionHallDefence.HEDWorld world = (ErosionHallDefence.HEDWorld)tmpworld;
         world.seedKills++;
         if (world.seedKills >= 4) {
            this.conquestConclusion(world, false);
         }
      }

      return null;
   }

   private void conquestConclusion(ErosionHallDefence.HEDWorld world, boolean win) {
      if (!world.conquestEnded) {
         if (world.timerTask != null) {
            world.timerTask.cancel(false);
         }

         if (world.agressionTask != null) {
            world.agressionTask.cancel(false);
         }

         if (world.coffinSpawnTask != null) {
            world.coffinSpawnTask.cancel(false);
         }

         if (world.aliveTumorSpawnTask != null) {
            world.aliveTumorSpawnTask.cancel(false);
         }

         if (world.failureTask != null) {
            world.failureTask.cancel(false);
         }

         world.conquestEnded = true;
         this.finishInstance(world, 900000, false);
         if (win) {
            Reflection inst = ReflectionManager.getInstance().getReflection(world.getReflectionId());
            if (inst != null) {
               for(int objId : world.getAllowed()) {
                  Player player = World.getInstance().getPlayer(objId);
                  QuestState st = player.getQuestState("_697_DefendtheHallofErosion");
                  if (st != null && st.isCond(1)) {
                     st.set("defenceDone", 1);
                  }
               }

               inst.cleanupNpcs();
            }

            ExShowScreenMessage msg = new ExShowScreenMessage(
               NpcStringId.CONGRATULATIONS_YOU_HAVE_SUCCEEDED_AT_S1_S2_THE_INSTANCE_WILL_SHORTLY_EXPIRE, 2, 1, 8000
            );
            msg.addStringParameter("#" + NpcStringId.HALL_OF_EROSION.getId());
            msg.addStringParameter("#" + NpcStringId.DEFEND.getId());
            this.broadCastPacket(world, msg);
            this.handleReenterTime(world);
         } else {
            ExShowScreenMessage msg = new ExShowScreenMessage(NpcStringId.YOU_HAVE_FAILED_AT_S1_S2_THE_INSTANCE_WILL_SHORTLY_EXPIRE, 2, 1, 8000);
            msg.addStringParameter("#" + NpcStringId.HALL_OF_EROSION.getId());
            msg.addStringParameter("#" + NpcStringId.DEFEND.getId());
            this.broadCastPacket(world, msg);
         }
      }
   }

   private void rescheduleFailureTask(final ErosionHallDefence.HEDWorld world, long time) {
      if (world.failureTask != null) {
         world.failureTask.cancel(false);
         world.failureTask = null;
      }

      world.failureTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
         @Override
         public void run() {
            ErosionHallDefence.this.conquestConclusion(world, false);
         }
      }, time);
   }

   private static Npc getNearestSeed(Npc mob) {
      for(Npc npc : World.getInstance().getAroundNpc(mob, 900, 300)) {
         if (npc.getId() == 32541) {
            return npc;
         }
      }

      return null;
   }

   protected void broadCastPacket(ErosionHallDefence.HEDWorld world, GameServerPacket packet) {
      for(int objId : world.getAllowed()) {
         Player player = World.getInstance().getPlayer(objId);
         if (player != null && player.isOnline() && player.getReflectionId() == world.getReflectionId()) {
            player.sendPacket(packet);
         }
      }
   }

   public static void main(String[] args) {
      new ErosionHallDefence(ErosionHallDefence.class.getSimpleName(), "instances");
   }

   protected class HEDWorld extends ReflectionWorld {
      public List<Npc> alivetumor = new ArrayList<>();
      public List<Npc> deadTumors = new ArrayList<>();
      public long startTime = 0L;
      public ScheduledFuture<?> timerTask;
      public ScheduledFuture<?> agressionTask;
      public ScheduledFuture<?> coffinSpawnTask;
      public ScheduledFuture<?> aliveTumorSpawnTask;
      public ScheduledFuture<?> failureTask;
      public int tumorKillCount = 0;
      protected boolean conquestBegun = false;
      protected boolean conquestEnded = false;
      private boolean soulwagonSpawned = false;
      private int seedKills = 0;
      private long tumorRespawnTime = 180000L;

      public synchronized void addTag(int value) {
         this.setTag(this.getTag() + value);
      }

      public HEDWorld() {
         this.setTag(-1);
      }
   }

   private class TimerTask implements Runnable {
      private final ErosionHallDefence.HEDWorld _world;

      public TimerTask(ErosionHallDefence.HEDWorld world) {
         this._world = world;
      }

      @Override
      public void run() {
         if (ReflectionManager.getInstance().getWorld(this._world.getReflectionId()) == this._world) {
            long time = (this._world.startTime + 1500000L - System.currentTimeMillis()) / 60000L;
            if (time == 0L) {
               ErosionHallDefence.this.conquestConclusion(this._world, false);
            } else {
               ExShowScreenMessage msg = new ExShowScreenMessage(NpcStringId.S1_MINUTES_REMAINING, 2, 1, 8000);
               msg.addStringParameter(Integer.toString((int)(this._world.startTime + 1500000L - System.currentTimeMillis()) / 60000));
               ErosionHallDefence.this.broadCastPacket(this._world, msg);
            }
         }
      }
   }
}
