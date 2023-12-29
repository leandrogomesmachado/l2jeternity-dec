package l2e.scripts.instances;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.zone.ZoneType;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.network.serverpackets.NpcSay;

public class HeartInfinityDefence extends AbstractReflection {
   public HeartInfinityDefence(String name, String descr) {
      super(name, descr);
      this.addStartNpc(new int[]{32535, 32536, 32539});
      this.addTalkId(new int[]{32535, 32536, 32539});
      this.addEnterZoneId(new int[]{200010});
      this.addKillId(new int[]{18708, 18711});
   }

   private final synchronized void enterInstance(Player player, Npc npc) {
      if (this.enterInstance(player, npc, new HeartInfinityDefence.HIDWorld(), 122)) {
         final ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
         ((HeartInfinityDefence.HIDWorld)world).tumorRespawnTime = 180000L;
         ((HeartInfinityDefence.HIDWorld)world).wagonRespawnTime = 60000L;
         ((HeartInfinityDefence.HIDWorld)world).coffinsCreated = 0;
         ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
               HeartInfinityDefence.this.conquestBegins((HeartInfinityDefence.HIDWorld)world);
            }
         }, 20000L);
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

   private void conquestBegins(final HeartInfinityDefence.HIDWorld world) {
      final Reflection inst = ReflectionManager.getInstance().getReflection(world.getReflectionId());
      if (inst != null) {
         this.broadCastPacket(
            world, new ExShowScreenMessage(NpcStringId.YOU_CAN_HEAR_THE_UNDEAD_OF_EKIMUS_RUSHING_TOWARD_YOU_S1_S2_IT_HAS_NOW_BEGUN, 2, 1, 8000)
         );
         inst.spawnByGroup("soi_hoi_defence_mob_1");
         inst.spawnByGroup("soi_hoi_defence_mob_2");
         inst.spawnByGroup("soi_hoi_defence_mob_3");
         inst.spawnByGroup("soi_hoi_defence_mob_4");
         inst.spawnByGroup("soi_hoi_defence_mob_5");
         inst.spawnByGroup("soi_hoi_defence_mob_6");
         inst.spawnByGroup("soi_hoi_defence_tumors");
         inst.spawnByGroup("soi_hoi_defence_wards");
         inst.getDoor(14240102).openMe();

         for(int zoneId = 20040; zoneId < 20046; ++zoneId) {
            this.getActivatedZone(inst, zoneId, true);
         }

         world.preawakenedEchmus = addSpawn(29161, -179534, 208510, -15496, 16342, false, 0L, false, world.getReflectionId());
         world.coffinSpawnTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
               if (!world.conquestEnded) {
                  for(Npc n : inst.getNpcs()) {
                     if (n != null && n.getId() == 32535 && n.getReflectionId() == world.getReflectionId()) {
                        HeartInfinityDefence.this.spawnNpc(18709, n.getLocation(), 0, world.getReflectionId());
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
                        inst.despawnByGroup("soi_hoi_defence_tumors");
                        inst.spawnByGroup("soi_hoi_defence_alivetumors");
      
                        for(Npc n : inst.getNpcs()) {
                           if (n != null && n.getId() == 18708 && n.getReflectionId() == world.getReflectionId()) {
                              n.setCurrentHp(n.getMaxHp() * 0.5);
                           }
                        }
      
                        ExShowScreenMessage msg = new ExShowScreenMessage(
                           NpcStringId.THE_TUMOR_INSIDE_S1_HAS_COMPLETELY_REVIVED_NEKIMUS_STARTED_TO_REGAIN_HIS_ENERGY_AND_IS_DESPERATELY_LOOKING_FOR_HIS_PREY,
                           2,
                           1,
                           8000
                        );
                        msg.addStringParameter("#" + NpcStringId.HEART_OF_IMMORTALITY.getId());
                        HeartInfinityDefence.this.broadCastPacket(world, msg);
                     }
                  }
               },
               world.tumorRespawnTime
            );
         world.wagonSpawnTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
               Quest.addSpawn(22523, -179544, 207400, -15496, 0, false, 0L, false, world.getReflectionId());
            }
         }, 1000L, world.wagonRespawnTime);
         world.startTime = System.currentTimeMillis();
         world.timerTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new HeartInfinityDefence.TimerTask(world), 298000L, 300000L);
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getPlayerWorld(player);
      if (tmpworld instanceof HeartInfinityDefence.HIDWorld) {
         HeartInfinityDefence.HIDWorld world = (HeartInfinityDefence.HIDWorld)tmpworld;
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
         } else if (event.startsWith("reenterechmus")) {
            player.destroyItemByItemId("SOI", 13797, 3L, player, true);

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
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (npc.getId() == 32539) {
         this.enterInstance(player, npc);
      }

      return "";
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld != null && tmpworld instanceof HeartInfinityDefence.HIDWorld) {
         final HeartInfinityDefence.HIDWorld world = (HeartInfinityDefence.HIDWorld)tmpworld;
         final Location loc = npc.getLocation();
         if (npc.getId() == 18708) {
            ((MonsterInstance)npc).dropItem(player, 13797, (long)getRandom(2, 5));
            npc.deleteMe();
            final Npc deadTumor = this.spawnNpc(32535, loc, 0, world.getReflectionId());
            world.deadTumors.add(deadTumor);
            world.wagonRespawnTime = world.wagonRespawnTime + 10000L;
            ExShowScreenMessage msg = new ExShowScreenMessage(
               NpcStringId.THE_TUMOR_INSIDE_S1_HAS_BEEN_DESTROYED_NTHE_SPEED_THAT_EKIMUS_CALLS_OUT_HIS_PREY_HAS_SLOWED_DOWN, 2, 1, 8000
            );
            msg.addStringParameter("#" + NpcStringId.HEART_OF_IMMORTALITY.getId());
            this.broadCastPacket(world, msg);
            ThreadPoolManager.getInstance()
               .schedule(
                  new Runnable() {
                     @Override
                     public void run() {
                        world.deadTumors.remove(deadTumor);
                        deadTumor.deleteMe();
                        Npc alivetumor = HeartInfinityDefence.this.spawnNpc(18708, loc, 0, world.getReflectionId());
                        if (alivetumor != null) {
                           alivetumor.setCurrentHp(alivetumor.getMaxHp() * 0.25);
                        }
      
                        world.wagonRespawnTime = world.wagonRespawnTime - 10000L;
                        ExShowScreenMessage msg = new ExShowScreenMessage(
                           NpcStringId.THE_TUMOR_INSIDE_S1_HAS_COMPLETELY_REVIVED_NEKIMUS_STARTED_TO_REGAIN_HIS_ENERGY_AND_IS_DESPERATELY_LOOKING_FOR_HIS_PREY,
                           2,
                           1,
                           8000
                        );
                        msg.addStringParameter("#" + NpcStringId.HEART_OF_IMMORTALITY.getId());
                        HeartInfinityDefence.this.broadCastPacket(world, msg);
                     }
                  },
                  world.tumorRespawnTime
               );
         }

         if (npc.getId() == 18711) {
            world.tumorRespawnTime = world.tumorRespawnTime + 5000L;
         }
      }

      return "";
   }

   protected void notifyWagonArrived(Npc npc, HeartInfinityDefence.HIDWorld world) {
      world.coffinsCreated++;
      if (world.coffinsCreated == 20) {
         this.conquestConclusion(world, false);
      } else {
         NpcSay cs = new NpcSay(world.preawakenedEchmus.getObjectId(), 1, world.preawakenedEchmus.getId(), NpcStringId.BRING_MORE_MORE_SOULS);
         world.preawakenedEchmus.broadcastPacket(cs);
         ExShowScreenMessage message = new ExShowScreenMessage(NpcStringId.THE_SOUL_COFFIN_HAS_AWAKENED_EKIMUS, 2, 1, 8000);
         message.addStringParameter(Integer.toString(20 - world.coffinsCreated));
         this.broadCastPacket(world, message);
         int[] spawn = ZoneManager.getInstance().getZoneById(200032).getZone().getRandomPoint();
         addSpawn(
            18713, spawn[0], spawn[1], GeoEngine.getHeight(spawn[0], spawn[1], spawn[2], npc.getGeoIndex()), 0, false, 0L, false, world.getReflectionId()
         );
      }
   }

   protected void conquestConclusion(HeartInfinityDefence.HIDWorld world, boolean win) {
      if (!world.conquestEnded) {
         if (world.timerTask != null) {
            world.timerTask.cancel(false);
         }

         if (world.coffinSpawnTask != null) {
            world.coffinSpawnTask.cancel(false);
         }

         if (world.aliveTumorSpawnTask != null) {
            world.aliveTumorSpawnTask.cancel(false);
         }

         if (world.wagonSpawnTask != null) {
            world.wagonSpawnTask.cancel(false);
         }

         world.conquestEnded = true;
         if (win) {
            this.finishInstance(world, 900000, true);
            ExShowScreenMessage msg = new ExShowScreenMessage(
               NpcStringId.CONGRATULATIONS_YOU_HAVE_SUCCEEDED_AT_S1_S2_THE_INSTANCE_WILL_SHORTLY_EXPIRE, 2, 1, 8000
            );
            msg.addStringParameter("#" + NpcStringId.HEART_OF_IMMORTALITY.getId());
            msg.addStringParameter("#" + NpcStringId.ATTACK.getId());
            this.broadCastPacket(world, msg);
            this.handleReenterTime(world);

            for(int objId : world.getAllowed()) {
               Player player = World.getInstance().getPlayer(objId);
               if (player != null) {
                  QuestState st = player.getQuestState("_697_DefendtheHallofErosion");
                  if (st != null && st.isCond(1)) {
                     st.set("defenceDone", 1);
                  }
               }
            }
         } else {
            this.finishInstance(world, 900000, false);
            ExShowScreenMessage msg = new ExShowScreenMessage(NpcStringId.YOU_HAVE_FAILED_AT_S1_S2_THE_INSTANCE_WILL_SHORTLY_EXPIRE, 2, 1, 8000);
            msg.addStringParameter("#" + NpcStringId.HEART_OF_IMMORTALITY.getId());
            msg.addStringParameter("#" + NpcStringId.ATTACK.getId());
            this.broadCastPacket(world, msg);
         }
      }
   }

   @Override
   public final String onEnterZone(Creature character, ZoneType zone) {
      if (character instanceof Attackable) {
         Attackable npc = (Attackable)character;
         ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
         if (tmpworld instanceof HeartInfinityDefence.HIDWorld) {
            HeartInfinityDefence.HIDWorld world = (HeartInfinityDefence.HIDWorld)tmpworld;
            if (npc.getId() == 22523) {
               this.notifyWagonArrived(npc, world);
               npc.deleteMe();
            }
         }
      }

      return null;
   }

   protected void broadCastPacket(HeartInfinityDefence.HIDWorld world, GameServerPacket packet) {
      for(int objId : world.getAllowed()) {
         Player player = World.getInstance().getPlayer(objId);
         if (player != null && player.isOnline() && player.getReflectionId() == world.getReflectionId()) {
            player.sendPacket(packet);
         }
      }
   }

   public static void main(String[] args) {
      new HeartInfinityDefence(HeartInfinityDefence.class.getSimpleName(), "instances");
   }

   private class HIDWorld extends ReflectionWorld {
      private long tumorRespawnTime = 0L;
      private long wagonRespawnTime = 0L;
      private int coffinsCreated = 0;
      protected boolean conquestEnded = false;
      public List<Npc> deadTumors = new ArrayList<>();
      public long startTime = 0L;
      private Npc preawakenedEchmus = null;
      private ScheduledFuture<?> timerTask = null;
      private ScheduledFuture<?> wagonSpawnTask = null;
      private ScheduledFuture<?> coffinSpawnTask = null;
      private ScheduledFuture<?> aliveTumorSpawnTask = null;

      public HIDWorld() {
      }
   }

   private class TimerTask implements Runnable {
      private final HeartInfinityDefence.HIDWorld _world;

      TimerTask(HeartInfinityDefence.HIDWorld world) {
         this._world = world;
      }

      @Override
      public void run() {
         if (ReflectionManager.getInstance().getWorld(this._world.getReflectionId()) == this._world) {
            long time = (this._world.startTime + 1500000L - System.currentTimeMillis()) / 60000L;
            if (time == 0L) {
               HeartInfinityDefence.this.conquestConclusion(this._world, true);
            } else if (time == 15L) {
               Reflection inst = ReflectionManager.getInstance().getReflection(this._world.getReflectionId());
               if (inst != null) {
                  inst.spawnByGroup("soi_hoi_defence_bosses");
               }

               ExShowScreenMessage msg = new ExShowScreenMessage(NpcStringId.S1_MINUTES_REMAINING, 2, 1, 8000);
               msg.addStringParameter(Integer.toString((int)(this._world.startTime + 1500000L - System.currentTimeMillis()) / 60000));
               HeartInfinityDefence.this.broadCastPacket(this._world, msg);
            }
         }
      }
   }
}
