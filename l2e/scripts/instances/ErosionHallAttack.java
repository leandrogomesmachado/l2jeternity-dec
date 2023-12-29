package l2e.scripts.instances;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.instancemanager.SoIManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MonsterInstance;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.zone.ZoneType;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.network.serverpackets.NpcSay;

public class ErosionHallAttack extends AbstractReflection {
   private static int[][] COHEMENES_SPAWN = new int[][]{
      {25634, -178472, 211823, -12025, 0, 0, -1},
      {25634, -180926, 211887, -12029, 0, 0, -1},
      {25634, -180906, 206635, -12032, 0, 0, -1},
      {25634, -178492, 206426, -12023, 0, 0, -1}
   };

   public ErosionHallAttack(String name, String descr) {
      super(name, descr);
      this.addStartNpc(new int[]{32535, 32537});
      this.addTalkId(new int[]{32535, 32537});
      this.addAttackId(25634);
      this.addEnterZoneId(new int[]{20014});
      this.addKillId(new int[]{18708, 18711, 25634});
   }

   @Override
   public final String onEnterZone(Creature character, ZoneType zone) {
      if (character.isPlayer()) {
         ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(character.getReflectionId());
         if (tmpworld instanceof ErosionHallAttack.HEWorld) {
            ErosionHallAttack.HEWorld world = (ErosionHallAttack.HEWorld)tmpworld;
            if (!world.conquestBegun) {
               world.conquestBegun = true;
               this.runTumors(world);
               world.startTime = System.currentTimeMillis();
               world.timerTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new ErosionHallAttack.TimerTask(world), 298000L, 300000L);
            }
         }
      }

      return super.onEnterZone(character, zone);
   }

   private final synchronized void enterInstance(Player player, Npc npc) {
      this.enterInstance(player, npc, new ErosionHallAttack.HEWorld(), 119);
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

   protected void runTumors(ErosionHallAttack.HEWorld world) {
      Reflection inst = ReflectionManager.getInstance().getReflection(world.getReflectionId());
      if (inst != null) {
         inst.spawnByGroup("soi_hoe_attack_tumors");
         inst.spawnByGroup("soi_hoe_attack_symbols");
         inst.spawnByGroup("soi_hoe_attack_wards");
         inst.spawnByGroup("soi_hoe_attack_mob_1");
         inst.spawnByGroup("soi_hoe_attack_mob_2");
         inst.spawnByGroup("soi_hoe_attack_mob_3");
         inst.spawnByGroup("soi_hoe_attack_mob_4");
         inst.spawnByGroup("soi_hoe_attack_mob_5");
         inst.spawnByGroup("soi_hoe_attack_mob_6");
         inst.spawnByGroup("soi_hoe_attack_mob_7");
         inst.spawnByGroup("soi_hoe_attack_mob_8");

         for(Npc n : inst.getNpcs()) {
            if (n != null && n.getId() == 18708 && n.getReflectionId() == world.getReflectionId()) {
               n.setCurrentHp(n.getMaxHp() * 0.5);
            }
         }
      }

      for(int zoneId = 20008; zoneId < 20029; ++zoneId) {
         this.getActivatedZone(inst, zoneId, true);
      }

      this.broadCastPacket(world, new ExShowScreenMessage(NpcStringId.YOU_CAN_HEAR_THE_UNDEAD_OF_EKIMUS_RUSHING_TOWARD_YOU_S1_S2_IT_HAS_NOW_BEGUN, 2, 1, 8000));
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getPlayerWorld(player);
      if (tmpworld instanceof ErosionHallAttack.HEWorld) {
         ErosionHallAttack.HEWorld world = (ErosionHallAttack.HEWorld)tmpworld;
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
   public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon, Skill skill) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld instanceof ErosionHallAttack.HEWorld) {
         ErosionHallAttack.HEWorld world = (ErosionHallAttack.HEWorld)tmpworld;
         if (!world.isBossAttacked) {
            world.isBossAttacked = true;
            Calendar reenter = Calendar.getInstance();
            reenter.add(10, 24);
            this.setReenterTime(world, reenter.getTimeInMillis());
         }
      }

      return super.onAttack(npc, attacker, damage, isSummon, skill);
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld instanceof ErosionHallAttack.HEWorld) {
         ErosionHallAttack.HEWorld world = (ErosionHallAttack.HEWorld)tmpworld;
         Location loc = npc.getLocation();
         if (npc.getId() == 18708) {
            --world.tumorCount;
            ((MonsterInstance)npc).dropItem(player, 13797, (long)getRandom(2, 5));
            npc.deleteMe();
            Npc n = this.spawnNpc(32535, loc, 0, world.getReflectionId());
            world.deadTumors.add(n);
            world.addTag(1);
            this.notifyTumorDeath(world, n);
            ThreadPoolManager.getInstance().schedule(new ErosionHallAttack.TumorRevival(n, world), world.tumorRespawnTime);
            ThreadPoolManager.getInstance().schedule(new ErosionHallAttack.RegenerationCoffinSpawn(n, world), 20000L);
         } else if (npc.getId() == 25634) {
            npc.broadcastPacket(
               new NpcSay(
                  npc.getObjectId(), 1, npc.getId(), NpcStringId.KEU_I_WILL_LEAVE_FOR_NOW_BUT_DONT_THINK_THIS_IS_OVER_THE_SEED_OF_INFINITY_CAN_NEVER_DIE
               )
            );

            for(int objId : world.getAllowed()) {
               Player pl = World.getInstance().getPlayer(objId);
               QuestState st = pl.getQuestState("_696_ConquertheHallofErosion");
               if (st != null && st.isCond(1)) {
                  st.set("cohemenes", "1");
               }
            }

            this.conquestConclusion(world, true);
            SoIManager.notifyCohemenesKill();
         }

         if (npc.getId() == 18711) {
            world.tumorRespawnTime = world.tumorRespawnTime + 10000L;
         }
      }

      return super.onKill(npc, player, isSummon);
   }

   private void conquestConclusion(ErosionHallAttack.HEWorld world, boolean win) {
      Reflection inst = ReflectionManager.getInstance().getReflection(world.getReflectionId());
      if (inst != null) {
         if (world.timerTask != null) {
            world.timerTask.cancel(false);
         }

         world.conquestEnded = true;
         inst.despawnByGroup("soi_hoe_attack_symbols");
         inst.despawnByGroup("soi_hoe_attack_wards");
         if (world.cohemenes != null) {
            if (!world.cohemenes.isDead()) {
               world.cohemenes.getMinionList().deleteMinions();
               world.cohemenes.deleteMe();
            }

            world.cohemenes = null;
         }

         this.finishInstance(world, 900000, false);
         if (win) {
            ExShowScreenMessage msg = new ExShowScreenMessage(
               NpcStringId.CONGRATULATIONS_YOU_HAVE_SUCCEEDED_AT_S1_S2_THE_INSTANCE_WILL_SHORTLY_EXPIRE, 2, 1, 8000
            );
            msg.addStringParameter("#" + NpcStringId.HALL_OF_EROSION.getId());
            msg.addStringParameter("#" + NpcStringId.ATTACK.getId());
            this.broadCastPacket(world, msg);
            this.handleReenterTime(world);
         } else {
            ExShowScreenMessage msg = new ExShowScreenMessage(NpcStringId.YOU_HAVE_FAILED_AT_S1_S2_THE_INSTANCE_WILL_SHORTLY_EXPIRE, 2, 1, 8000);
            msg.addStringParameter("#" + NpcStringId.HALL_OF_EROSION.getId());
            msg.addStringParameter("#" + NpcStringId.ATTACK.getId());
            this.broadCastPacket(world, msg);
         }

         for(Npc npc : inst.getNpcs()) {
            if (npc != null && (npc.getId() == 18708 || npc.getId() == 32535)) {
               npc.deleteMe();
            }
         }
      }
   }

   private void notifyTumorDeath(ErosionHallAttack.HEWorld world, Npc tumor) {
      if (world.tumorCount == 0 && world.cohemenes == null) {
         ExShowScreenMessage msg = new ExShowScreenMessage(
            NpcStringId.ALL_THE_TUMORS_INSIDE_S1_HAVE_BEEN_DESTROYED_DRIVEN_INTO_A_CORNER_COHEMENES_APPEARS_CLOSE_BY, 2, 1, 8000
         );
         msg.addStringParameter("#" + NpcStringId.HALL_OF_EROSION.getId());
         this.broadCastPacket(world, msg);
         int[] spawn = COHEMENES_SPAWN[getRandom(0, COHEMENES_SPAWN.length - 1)];
         world.cohemenes = addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0L, false, world.getReflectionId());
         world.cohemenes
            .broadcastPacket(
               new NpcSay(
                  world.cohemenes.getObjectId(),
                  1,
                  world.cohemenes.getId(),
                  NpcStringId.CMON_CMON_SHOW_YOUR_FACE_YOU_LITTLE_RATS_LET_ME_SEE_WHAT_THE_DOOMED_WEAKLINGS_ARE_SCHEMING
               )
            );
      } else {
         ExShowScreenMessage msg = new ExShowScreenMessage(
            NpcStringId.THE_TUMOR_INSIDE_S1_HAS_BEEN_DESTROYED_NIN_ORDER_TO_DRAW_OUT_THE_COWARDLY_COHEMENES_YOU_MUST_DESTROY_ALL_THE_TUMORS, 2, 1, 8000
         );
         msg.addStringParameter("#" + NpcStringId.HALL_OF_EROSION.getId());
         this.broadCastPacket(world, msg);
      }

      this.manageRegenZone(tumor, true);
   }

   private void notifyTumorRevival(ErosionHallAttack.HEWorld world, Npc tumor) {
      if (world.tumorCount > 0 && world.cohemenes != null && !world.cohemenes.isDead()) {
         world.cohemenes.getMinionList().deleteMinions();
         world.cohemenes.deleteMe();
         world.cohemenes = null;
      }

      ExShowScreenMessage msg = new ExShowScreenMessage(
         NpcStringId.THE_TUMOR_INSIDE_S1_HAS_COMPLETELY_REVIVED_NTHE_RESTRENGTHENED_COHEMENES_HAS_FLED_DEEPER_INSIDE_THE_SEED, 2, 1, 8000
      );
      msg.addStringParameter("#" + NpcStringId.HALL_OF_EROSION.getId());
      this.broadCastPacket(world, msg);
      this.manageRegenZone(tumor, false);
   }

   protected void broadCastPacket(ErosionHallAttack.HEWorld world, GameServerPacket packet) {
      for(int objId : world.getAllowed()) {
         Player player = World.getInstance().getPlayer(objId);
         if (player != null && player.isOnline() && player.getReflectionId() == world.getReflectionId()) {
            player.sendPacket(packet);
         }
      }
   }

   private void manageRegenZone(Npc npc, boolean doActivate) {
      Reflection reflection = ReflectionManager.getInstance().getReflection(npc.getReflectionId());
      if (reflection != null) {
         int zoneId = 0;
         if (ZoneManager.getInstance().isInsideZone(20000, npc)) {
            zoneId = 20000;
         } else if (ZoneManager.getInstance().isInsideZone(20001, npc)) {
            zoneId = 20001;
         } else if (ZoneManager.getInstance().isInsideZone(20002, npc)) {
            zoneId = 20002;
         } else if (ZoneManager.getInstance().isInsideZone(20003, npc)) {
            zoneId = 20003;
         } else if (ZoneManager.getInstance().isInsideZone(20004, npc)) {
            zoneId = 20004;
         } else if (ZoneManager.getInstance().isInsideZone(20005, npc)) {
            zoneId = 20005;
         } else if (ZoneManager.getInstance().isInsideZone(20006, npc)) {
            zoneId = 20006;
         } else if (ZoneManager.getInstance().isInsideZone(20007, npc)) {
            zoneId = 20007;
         }

         this.getActivatedZone(reflection, zoneId, doActivate);
      }
   }

   public static void main(String[] args) {
      new ErosionHallAttack(ErosionHallAttack.class.getSimpleName(), "instances");
   }

   protected class HEWorld extends ReflectionWorld {
      public List<Npc> deadTumors = new ArrayList<>();
      public int tumorCount = 4;
      public Npc cohemenes = null;
      public boolean isBossAttacked = false;
      public long startTime = 0L;
      private ScheduledFuture<?> timerTask;
      private boolean conquestBegun = false;
      private boolean conquestEnded = false;
      private long tumorRespawnTime = 180000L;

      public synchronized void addTag(int value) {
         this.setTag(this.getTag() + value);
      }

      public HEWorld() {
         this.setTag(-1);
      }
   }

   private class RegenerationCoffinSpawn implements Runnable {
      private final Npc _deadTumor;
      private final ErosionHallAttack.HEWorld _world;

      public RegenerationCoffinSpawn(Npc deadTumor, ErosionHallAttack.HEWorld world) {
         this._deadTumor = deadTumor;
         this._world = world;
      }

      @Override
      public void run() {
         if (ReflectionManager.getInstance().getWorld(this._world.getReflectionId()) == this._world) {
            if (!this._world.conquestEnded) {
               for(int i = 0; i < 4; ++i) {
                  ErosionHallAttack.this.spawnNpc(18710, this._deadTumor.getLocation(), 0, this._world.getReflectionId());
               }
            }
         }
      }
   }

   private class TimerTask implements Runnable {
      private final ErosionHallAttack.HEWorld _world;

      public TimerTask(ErosionHallAttack.HEWorld world) {
         this._world = world;
      }

      @Override
      public void run() {
         if (ReflectionManager.getInstance().getWorld(this._world.getReflectionId()) == this._world) {
            long time = (this._world.startTime + 1500000L - System.currentTimeMillis()) / 60000L;
            if (time == 0L) {
               ErosionHallAttack.this.conquestConclusion(this._world, false);
            } else {
               ExShowScreenMessage msg = new ExShowScreenMessage(NpcStringId.S1_MINUTES_REMAINING, 2, 1, 8000);
               msg.addStringParameter(Integer.toString((int)(this._world.startTime + 1500000L - System.currentTimeMillis()) / 60000));
               ErosionHallAttack.this.broadCastPacket(this._world, msg);
            }
         }
      }
   }

   private class TumorRevival implements Runnable {
      private final Npc _deadTumor;
      private final ErosionHallAttack.HEWorld _world;

      public TumorRevival(Npc deadTumor, ErosionHallAttack.HEWorld world) {
         this._deadTumor = deadTumor;
         this._world = world;
      }

      @Override
      public void run() {
         if (ReflectionManager.getInstance().getWorld(this._world.getReflectionId()) == this._world) {
            if (!this._world.conquestEnded) {
               Npc tumor = ErosionHallAttack.this.spawnNpc(18708, this._deadTumor.getLocation(), 0, this._world.getReflectionId());
               tumor.setCurrentHp(tumor.getMaxHp() * 0.25);
               ++this._world.tumorCount;
               ErosionHallAttack.this.notifyTumorRevival(this._world, this._deadTumor);
               this._world.deadTumors.remove(this._deadTumor);
               this._deadTumor.deleteMe();
               this._world.addTag(-1);
            }
         }
      }
   }
}
