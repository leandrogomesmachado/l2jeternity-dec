package l2e.scripts.instances;

import java.util.concurrent.ScheduledFuture;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import org.apache.commons.lang3.ArrayUtils;

public class SufferingHallDefence extends AbstractReflection {
   private static final int[] monsters = new int[]{22509, 22510, 22511, 22512, 22513, 22514, 22515, 18704};

   public SufferingHallDefence(String name, String descr) {
      super(name, descr);
      this.addStartNpc(new int[]{32530, 32537});
      this.addTalkId(new int[]{32530, 32537});
      this.addAttackId(new int[]{25665, 25666});
      this.addSkillSeeId(new int[]{22509, 22510, 22511, 22512, 22513, 22514, 22515});
      this.addKillId(new int[]{18704, 22509, 22510, 22511, 22512, 22513, 22514, 22515, 25665, 25666});
   }

   private final synchronized void enterInstance(Player player, Npc npc) {
      if (this.enterInstance(player, npc, new SufferingHallDefence.SHDWorld(), 116)) {
         ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
         ((SufferingHallDefence.SHDWorld)world).storeTime[0] = System.currentTimeMillis();
         this.startDefence((SufferingHallDefence.SHDWorld)world);
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

      if (npc.getId() == 32537) {
         this.enterInstance(player, npc);
      }

      return null;
   }

   @Override
   public String onSkillSee(Npc npc, Player caster, Skill skill, GameObject[] targets, boolean isSummon) {
      if (skill.hasEffectType(EffectType.REBALANCE_HP, EffectType.HEAL, EffectType.HEAL_PERCENT)) {
         int hate = 2 * skill.getAggroPoints();
         if (hate < 2) {
            hate = 1000;
         }

         ((Attackable)npc).addDamageHate(caster, 0, hate);
      }

      return super.onSkillSee(npc, caster, skill, targets, isSummon);
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld instanceof SufferingHallDefence.SHDWorld) {
         final SufferingHallDefence.SHDWorld world = (SufferingHallDefence.SHDWorld)tmpworld;
         if (ArrayUtils.contains(monsters, npc.getId()) && !this.checkAliveMonsters(world)) {
            if (world.monstersSpawnTask != null) {
               world.monstersSpawnTask.cancel(false);
            }

            world.monstersSpawnTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
               @Override
               public void run() {
                  SufferingHallDefence.this.spawnMonsters(world);
               }
            }, 40000L);
         }

         if (npc.getId() == 18704) {
            npc.deleteMe();
            this.notifyCoffinActivity(npc, world);
            addSpawn(18705, -173704, 218092, -9562, 0, false, 0L, false, npc.getReflectionId());
            world.tumorIndex = 300;
            world.doCountCoffinNotifications = true;
         } else if (npc.getId() == 25665) {
            ThreadPoolManager.getInstance().schedule(new Runnable() {
               @Override
               public void run() {
                  world.storeTime[1] = System.currentTimeMillis();
                  world.calcRewardItemId();
                  if (world.monstersSpawnTask != null) {
                     world.monstersSpawnTask.cancel(false);
                  }

                  if (world.coffinSpawnTask != null) {
                     world.coffinSpawnTask.cancel(false);
                  }

                  Reflection inst = ReflectionManager.getInstance().getReflection(world.getReflectionId());
                  if (inst != null) {
                     inst.spawnByGroup("soi_hos_defence_tepios");
                  }

                  SufferingHallDefence.this.finishInstance(world, 300000, true);
               }
            }, 10000L);
         }
      }

      return super.onKill(npc, player, isSummon);
   }

   public void notifyCoffinActivity(Npc npc, SufferingHallDefence.SHDWorld world) {
      if (world.doCountCoffinNotifications) {
         world.tumorIndex -= 5;
         if (world.tumorIndex == 100) {
            this.broadCastPacket(world, new ExShowScreenMessage(NpcStringId.THE_AREA_NEAR_THE_TUMOR_IS_FULL_OF_OMINOUS_ENERGY, 2, 1, 8000));
         } else if (world.tumorIndex == 30) {
            this.broadCastPacket(world, new ExShowScreenMessage(NpcStringId.YOU_CAN_FEEL_THE_SURGING_ENERGY_OF_DEATH_FROM_THE_TUMOR, 2, 1, 8000));
         }

         if (world.tumorIndex <= 0) {
            if (this.getTumor(world, 18705) != null) {
               this.getTumor(world, 18705).deleteMe();
            }

            Npc aliveTumor = this.spawnNpc(18704, new Location(-173704, 218092, -9562), 0, world.getReflectionId());
            aliveTumor.setCurrentHp(aliveTumor.getMaxHp() * 0.4);
            world.doCountCoffinNotifications = false;
         }
      }
   }

   private void startDefence(final SufferingHallDefence.SHDWorld world) {
      Reflection inst = ReflectionManager.getInstance().getReflection(world.getReflectionId());
      if (inst != null) {
         inst.spawnByGroup("soi_hos_defence_tumor");
         world.doCountCoffinNotifications = true;
         world.coffinSpawnTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
               Quest.addSpawn(18706, -173704, 218092, -9562, 0, false, 0L, false, world.getReflectionId());
            }
         }, 1000L, 10000L);
         world.monstersSpawnTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
            @Override
            public void run() {
               SufferingHallDefence.this.spawnMonsters(world);
            }
         }, 60000L);
      }
   }

   private void spawnMonsters(SufferingHallDefence.SHDWorld world) {
      Reflection inst = ReflectionManager.getInstance().getReflection(world.getReflectionId());
      if (inst != null) {
         if (world.stage > 6) {
            return;
         }

         String group = null;
         switch(world.stage) {
            case 1:
               group = "soi_hos_defence_mobs_1";
               this.getActivatedZone(inst, 20035, true);
               break;
            case 2:
               group = "soi_hos_defence_mobs_2";
               this.getActivatedZone(inst, 20035, false);
               this.getActivatedZone(inst, 20036, true);
               break;
            case 3:
               group = "soi_hos_defence_mobs_3";
               this.getActivatedZone(inst, 20036, false);
               this.getActivatedZone(inst, 20037, true);
               break;
            case 4:
               group = "soi_hos_defence_mobs_4";
               this.getActivatedZone(inst, 20037, false);
               this.getActivatedZone(inst, 20038, true);
               break;
            case 5:
               group = "soi_hos_defence_mobs_5";
               this.getActivatedZone(inst, 20038, false);
               this.getActivatedZone(inst, 20039, true);
               break;
            case 6:
               world.doCountCoffinNotifications = false;
               group = "soi_hos_defence_brothers";
               this.getActivatedZone(inst, 20039, false);
         }

         world.stage++;
         if (group != null) {
            inst.spawnByGroup(group);
         }

         for(Npc n : inst.getNpcs()) {
            if (n != null && !n.isDead() && n.getReflectionId() == world.getReflectionId() && n.isMonster() && ArrayUtils.contains(monsters, n.getId())) {
               n.setRunning();
               n.getAI().setIntention(CtrlIntention.MOVING, Location.findPointToStay(new Location(-173704, 218092, -9562), 200, inst.getGeoIndex(), true));
            }
         }
      }
   }

   private boolean checkAliveMonsters(SufferingHallDefence.SHDWorld world) {
      Reflection inst = ReflectionManager.getInstance().getReflection(world.getReflectionId());
      if (inst != null) {
         for(Npc n : inst.getNpcs()) {
            if (ArrayUtils.contains(monsters, n.getId()) && !n.isDead() && n.getReflectionId() == world.getReflectionId()) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private Npc getTumor(SufferingHallDefence.SHDWorld world, int id) {
      Reflection inst = ReflectionManager.getInstance().getReflection(world.getReflectionId());
      if (inst != null) {
         for(Npc npc : inst.getNpcs()) {
            if (npc != null && npc.getId() == id && !npc.isDead() && npc.getReflectionId() == world.getReflectionId()) {
               return npc;
            }
         }
      }

      return null;
   }

   protected void broadCastPacket(SufferingHallDefence.SHDWorld world, GameServerPacket packet) {
      for(int objId : world.getAllowed()) {
         Player player = World.getInstance().getPlayer(objId);
         if (player != null && player.isOnline() && player.getReflectionId() == world.getReflectionId()) {
            player.sendPacket(packet);
         }
      }
   }

   public static void main(String[] args) {
      new SufferingHallDefence(SufferingHallDefence.class.getSimpleName(), "instances");
   }

   private class SHDWorld extends ReflectionWorld {
      private int stage = 1;
      private ScheduledFuture<?> coffinSpawnTask;
      private ScheduledFuture<?> monstersSpawnTask;
      private boolean doCountCoffinNotifications = false;
      public int tumorIndex = 300;
      public long[] storeTime = new long[]{0L, 0L};

      protected void calcRewardItemId() {
         Long finishDiff = this.storeTime[1] - this.storeTime[0];
         if (finishDiff < 1260000L) {
            this.setTag(13777);
         } else if (finishDiff < 1380000L) {
            this.setTag(13778);
         } else if (finishDiff < 1500000L) {
            this.setTag(13779);
         } else if (finishDiff < 1620000L) {
            this.setTag(13780);
         } else if (finishDiff < 1740000L) {
            this.setTag(13781);
         } else if (finishDiff < 1860000L) {
            this.setTag(13782);
         } else if (finishDiff < 1980000L) {
            this.setTag(13783);
         } else if (finishDiff < 2100000L) {
            this.setTag(13784);
         } else if (finishDiff < 2220000L) {
            this.setTag(13785);
         } else {
            this.setTag(13786);
         }
      }

      public SHDWorld() {
         this.setTag(-1);
      }
   }
}
