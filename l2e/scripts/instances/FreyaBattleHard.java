package l2e.scripts.instances;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import l2e.commons.geometry.Polygon;
import l2e.commons.threading.RunnableImpl;
import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.MountType;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.GrandBossInstance;
import l2e.gameserver.model.actor.instance.NpcInstance;
import l2e.gameserver.model.actor.instance.QuestGuardInstance;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.service.BotFunctions;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.spawn.SpawnTerritory;
import l2e.gameserver.model.stats.NpcStats;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.EventTrigger;
import l2e.gameserver.network.serverpackets.ExChangeClientEffectInfo;
import l2e.gameserver.network.serverpackets.ExSendUIEvent;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;

public class FreyaBattleHard extends AbstractReflection {
   private static final SkillHolder SUICIDE_BREATH = new SkillHolder(6300, 1);
   private static final SkillHolder JINIA_SUPPORT = new SkillHolder(6288, 1);
   private static final SkillHolder KEGOR_SUPPORT = new SkillHolder(6289, 1);
   private static final SkillHolder ANTI_STRIDER = new SkillHolder(4258, 1);
   private static final Location MIDDLE_POINT = new Location(114730, -114805, -11200);
   private static SpawnTerritory CENTRAL_ROOM = new SpawnTerritory()
      .add(
         new Polygon()
            .add(114264, -113672)
            .add(113640, -114344)
            .add(113640, -115240)
            .add(114264, -115912)
            .add(115176, -115912)
            .add(115800, -115272)
            .add(115800, -114328)
            .add(115192, -113672)
            .setZmax(-11225)
            .setZmin(-11225)
      );
   private static final Location[] STATUES_STAGE_1_LOC = new Location[]{
      new Location(113845, -116091, -11168, 8264),
      new Location(113381, -115622, -11168, 8264),
      new Location(113380, -113978, -11168, -8224),
      new Location(113845, -113518, -11168, -8224),
      new Location(115591, -113516, -11168, -24504),
      new Location(116053, -113981, -11168, -24504),
      new Location(116061, -115611, -11168, 24804),
      new Location(115597, -116080, -11168, 24804)
   };
   private static final Location[] STATUES_STAGE_2_LOC = new Location[]{
      new Location(112942, -115480, -10960, 52),
      new Location(112940, -115146, -10960, 52),
      new Location(112945, -114453, -10960, 52),
      new Location(112945, -114123, -10960, 52),
      new Location(116497, -114117, -10960, 32724),
      new Location(116499, -114454, -10960, 32724),
      new Location(116501, -115145, -10960, 32724),
      new Location(116502, -115473, -10960, 32724)
   };
   private static int[] EMMITERS = new int[]{23140202, 23140204, 23140206, 23140208, 23140212, 23140214, 23140216};

   private FreyaBattleHard() {
      super(FreyaBattleHard.class.getSimpleName(), "instances");
      this.addStartNpc(new int[]{32762, 18851, 18850});
      this.addTalkId(new int[]{32762, 32781, 18851});
      this.addAttackId(new int[]{29177, 29180, 18854, 18856});
      this.addKillId(new int[]{29177, 25700, 29180, 18856});
      this.addSpawnId(new int[]{25700, 29180, 18856});
      this.addSpellFinishedId(new int[]{18854});
   }

   @Override
   public String onAdvEvent(String var1, Npc var2, Player var3) {
      if (var1.equals("enterHard")) {
         this.enterInstance(var3, var2);
      } else {
         ReflectionWorld var4 = ReflectionManager.getInstance().getWorld(var2.getReflectionId());
         if (var4 instanceof FreyaBattleHard.IQCNBWorld) {
            FreyaBattleHard.IQCNBWorld var7 = (FreyaBattleHard.IQCNBWorld)var4;
            switch(var1) {
               case "openDoor":
                  if (var2.isScriptValue(0)) {
                     var2.setScriptValue(1);
                     var7.getReflection().openDoor(23140101);
                     var7.controller = (NpcInstance)addSpawn(18919, new Location(114394, -112383, -11200), false, 0L, true, var7.getReflectionId());

                     for(Location var35 : STATUES_STAGE_1_LOC) {
                        Npc var37 = addSpawn(18919, var35, false, 0L, false, var7.getReflectionId());
                        var7.knightStatues.add(var37);
                     }

                     this.startQuestTimer("STAGE_1_MOVIE", 60000L, var7.controller, null);
                  }
                  break;
               case "portInside":
                  if (var3.isInParty() && var3.getParty().isLeader(var3) && var3.getVarB("autoTeleport@", false)) {
                     for(Player var26 : var3.getParty().getMembers()) {
                        if (var26 != null
                           && var26.getObjectId() != var3.getObjectId()
                           && Util.checkIfInRange(1000, var3, var26, true)
                           && var26.getReflectionId() == var7.getReflectionId()
                           && BotFunctions.checkCondition(var26, false)
                           && var26.getIPAddress().equalsIgnoreCase(var3.getIPAddress())) {
                           this.teleportPlayer(var26, new Location(114694, -113700, -11200), var7.getReflectionId());
                        }
                     }
                  }

                  this.teleportPlayer(var3, new Location(114694, -113700, -11200), var7.getReflectionId());
                  break;
               case "STAGE_1_MOVIE":
                  var7.getReflection().closeDoor(23140101);
                  var7.setStatus(1);
                  this.manageMovie(var7, 15);
                  this.startQuestTimer("STAGE_1_START", 53500L, var7.controller, null);
                  break;
               case "STAGE_1_START":
                  var7.freya = (GrandBossInstance)addSpawn(29177, new Location(114720, -117085, -11088, 15956), false, 0L, true, var7.getReflectionId());
                  this.manageScreenMsg(var7, NpcStringId.BEGIN_STAGE_1);
                  this.startQuestTimer("STAGE_1_SPAWN", 2000L, var7.freya, null);
                  break;
               case "STAGE_1_SPAWN":
                  var7.canSpawnMobs = true;
                  this.notifyEvent("START_SPAWN", var7.controller, null);
                  if (!var7.freya.isInCombat()) {
                     this.manageScreenMsg(var7, NpcStringId.FREYA_HAS_STARTED_TO_MOVE);
                     var7.freya.setRunning();
                     var7.freya.getAI().setIntention(CtrlIntention.MOVING, new Location(114730, -114805, -11200));
                  }

                  Reflection var18 = ReflectionManager.getInstance().getReflection(var7.getReflectionId());
                  if (var18 != null) {
                     var7.firstStageGuardSpawn = ThreadPoolManager.getInstance()
                        .scheduleAtFixedRate(new FreyaBattleHard.GuardSpawnTask(1, var7), 2000L, (long)var18.getParams().getInteger("guardsInterval") * 1000L);
                  }
                  break;
               case "STAGE_1_FINISH":
                  var7.canSpawnMobs = false;
                  if (var7.firstStageGuardSpawn != null) {
                     var7.firstStageGuardSpawn.cancel(true);
                  }

                  this.manageDespawnMinions(var7);
                  this.manageMovie(var7, 16);
                  this.startQuestTimer("STAGE_1_PAUSE", 23100L, var7.controller, null);
                  break;
               case "STAGE_1_PAUSE":
                  var7.freya = (GrandBossInstance)addSpawn(29178, new Location(114723, -117502, -10672, 15956), false, 0L, true, var7.getReflectionId());
                  var7.freya.setIsInvul(true);
                  var7.freya.block();
                  var7.freya.disableCoreAI(true);
                  this.manageTimer(var7, 60, NpcStringId.TIME_REMAINING_UNTIL_NEXT_BATTLE);
                  var7.setStatus(2);
                  this.startQuestTimer("STAGE_2_START", 60000L, var7.controller, null);
                  break;
               case "STAGE_2_START":
                  var7.canSpawnMobs = true;
                  this.notifyEvent("START_SPAWN", var7.controller, null);
                  this.manageScreenMsg(var7, NpcStringId.BEGIN_STAGE_2);
                  Reflection var17 = ReflectionManager.getInstance().getReflection(var7.getReflectionId());
                  if (var17 != null) {
                     var7.secondStageGuardSpawn = ThreadPoolManager.getInstance()
                        .scheduleAtFixedRate(new FreyaBattleHard.GuardSpawnTask(2, var7), 2000L, (long)var17.getParams().getInteger("guardsInterval") * 1000L);
                  }

                  int var10 = var17.getParams().getInteger("glakiasTimeLimit");
                  this.startQuestTimer("STAGE_2_FAILED", (long)(var10 * 1000), var7.controller, null);
                  this.manageTimer(var7, var10, NpcStringId.BATTLE_END_LIMIT_TIME);
                  var7.controller.getVariables().set("TIMER_END", System.currentTimeMillis() + (long)(var10 * 1000));
                  break;
               case "STAGE_2_MOVIE":
                  this.manageBlockMinions(var7);
                  this.manageMovie(var7, 23);
                  this.startQuestTimer("STAGE_2_GLAKIAS", 7000L, var7.controller, null);
                  break;
               case "STAGE_2_GLAKIAS":
                  this.manageUnblockMinions(var7);

                  for(Location var36 : STATUES_STAGE_2_LOC) {
                     Npc var38 = addSpawn(18919, var36, false, 0L, false, var7.getReflectionId());
                     var7.knightStatues.add(var38);
                     this.startQuestTimer("SPAWN_KNIGHT", 5000L, var38, null);
                  }

                  addSpawn(25700, new Location(114707, -114799, -11199, 15956), false, 0L, true, var7.getReflectionId());
                  this.startQuestTimer("SHOW_GLAKIAS_TIMER", 3000L, var7.controller, null);
                  break;
               case "STAGE_2_FAILED":
                  if (var7.getStatus() <= 3) {
                     this.doCleanup(var7);
                     this.manageMovie(var7, 22);
                     this.startQuestTimer("STAGE_2_FAILED2", 22000L, var2, null);
                  }
                  break;
               case "STAGE_2_FAILED2":
                  ReflectionManager.getInstance().destroyReflection(var7.getReflectionId());
                  break;
               case "STAGE_3_MOVIE":
                  if (var7.freya != null) {
                     var7.freya.deleteMe();
                  }

                  this.manageMovie(var7, 17);
                  this.startQuestTimer("STAGE_3_START", 21500L, var7.controller, null);
                  break;
               case "STAGE_3_START":
                  for(Player var31 : var7.playersInside) {
                     if (var31 != null) {
                        var31.broadcastPacket(ExChangeClientEffectInfo.STATIC_FREYA_DESTROYED);

                        for(int var16 : EMMITERS) {
                           var31.sendPacket(new EventTrigger(var16, true));
                        }
                     }
                  }

                  this.manageScreenMsg(var7, NpcStringId.BEGIN_STAGE_3);
                  var7.canSpawnMobs = true;
                  Reflection var5 = ReflectionManager.getInstance().getReflection(var7.getReflectionId());
                  if (var5 != null) {
                     var7.thirdStageGuardSpawn = ThreadPoolManager.getInstance()
                        .scheduleAtFixedRate(new FreyaBattleHard.GuardSpawnTask(3, var7), 2000L, (long)var5.getParams().getInteger("guardsInterval") * 1000L);
                  }

                  var7.freya = (GrandBossInstance)addSpawn(29180, new Location(114720, -117085, -11088, 15956), false, 0L, true, var7.getReflectionId());
                  this.notifyEvent("START_SPAWN", var7.controller, null);
                  if (!var7.freya.isInCombat()) {
                     this.manageScreenMsg(var7, NpcStringId.FREYA_HAS_STARTED_TO_MOVE);
                     var7.freya.setRunning();
                     var7.freya.getAI().setIntention(CtrlIntention.MOVING, new Location(114730, -114805, -11200));
                  }
                  break;
               case "SPAWN_SUPPORT":
                  this.manageUnblockMinions(var7);

                  for(Player var30 : var7.playersInside) {
                     var30.setIsInvul(false);
                     var30.unblock();
                  }

                  var7.freya.setIsInvul(false);
                  var7.freya.unblock();
                  var7.canSpawnMobs = true;
                  var7.freya.disableCoreAI(false);
                  this.manageScreenMsg(var7, NpcStringId.BEGIN_STAGE_4);
                  var7.supp_Jinia = (QuestGuardInstance)addSpawn(18850, new Location(114751, -114781, -11205), false, 0L, true, var7.getReflectionId());
                  var7.supp_Jinia.setRunning();
                  var7.supp_Jinia.setIsInvul(true);
                  var7.supp_Jinia.setCanReturnToSpawnPoint(false);
                  var7.supp_Kegor = (QuestGuardInstance)addSpawn(18851, new Location(114659, -114796, -11205), false, 0L, true, var7.getReflectionId());
                  var7.supp_Kegor.setRunning();
                  var7.supp_Kegor.setIsInvul(true);
                  var7.supp_Kegor.setCanReturnToSpawnPoint(false);
                  this.startQuestTimer("GIVE_SUPPORT", 1000L, var7.controller, null);
                  break;
               case "GIVE_SUPPORT":
                  if (var7.isSupportActive) {
                     var7.supp_Jinia.doCast(JINIA_SUPPORT.getSkill());
                     var7.supp_Kegor.doCast(KEGOR_SUPPORT.getSkill());
                  }
                  break;
               case "START_SPAWN":
                  for(Npc var29 : var7.knightStatues) {
                     this.notifyEvent("SPAWN_KNIGHT", var29, null);
                  }
                  break;
               case "SPAWN_KNIGHT":
                  if (var7.canSpawnMobs) {
                     Location var21 = new Location(
                        MIDDLE_POINT.getX() + getRandom(-1000, 1000), MIDDLE_POINT.getY() + getRandom(-1000, 1000), MIDDLE_POINT.getZ()
                     );
                     Attackable var28 = (Attackable)addSpawn(18856, var2.getLocation(), false, 0L, false, var7.getReflectionId());
                     var28.getSpawn().setLocation(var21);
                     var7.spawnedMobs.add(var28);
                  }
                  break;
               case "FIND_TARGET":
                  if (!var2.isDead()) {
                     this.manageRandomAttack(var7, (Attackable)var2);
                  }
                  break;
               case "ELEMENTAL_KILLED":
                  if (var2.getVariables().getInteger("SUICIDE_ON") == 1) {
                     var2.setTarget(var2);
                     var2.doCast(SUICIDE_BREATH.getSkill());
                  }
                  break;
               case "FINISH_WORLD":
                  if (var7.freya != null) {
                     var7.freya.deleteMe();
                  }

                  for(Player var12 : var7.playersInside) {
                     if (var12 != null) {
                        var12.broadcastPacket(ExChangeClientEffectInfo.STATIC_FREYA_DEFAULT);
                     }
                  }
                  break;
               case "SHOW_GLAKIAS_TIMER":
                  int var6 = (int)((var7.controller.getVariables().getLong("TIMER_END", 0L) - System.currentTimeMillis()) / 1000L);
                  this.manageTimer(var7, var6, NpcStringId.BATTLE_END_LIMIT_TIME);
            }
         }
      }

      return super.onAdvEvent(var1, var2, var3);
   }

   @Override
   public String onSpawn(Npc var1) {
      ((Attackable)var1).setOnKillDelay(0);
      return super.onSpawn(var1);
   }

   @Override
   public String onAttack(Npc var1, Player var2, int var3, boolean var4, Skill var5) {
      ReflectionWorld var6 = ReflectionManager.getInstance().getWorld(var1.getReflectionId());
      if (var6 instanceof FreyaBattleHard.IQCNBWorld) {
         FreyaBattleHard.IQCNBWorld var7 = (FreyaBattleHard.IQCNBWorld)var6;
         switch(var1.getId()) {
            case 18854:
               if (var1.getCurrentHp() < var1.getMaxHp() / 20.0 && var1.getVariables().getInteger("SUICIDE_ON", 0) == 0) {
                  var1.getVariables().set("SUICIDE_ON", 1);
                  this.startQuestTimer("ELEMENTAL_KILLED", 1000L, var1, null);
               }
               break;
            case 29180:
               if (var1.getCurrentHp() < var1.getMaxHp() * 0.2 && !var7.isSupportActive) {
                  var7.isSupportActive = true;
                  var7.freya.setIsInvul(true);
                  var7.freya.block();
                  var7.freya.disableCoreAI(true);
                  this.manageBlockMinions(var7);
                  var7.canSpawnMobs = false;

                  for(Player var9 : var7.playersInside) {
                     var9.setIsInvul(true);
                     var9.block();
                     var9.abortAttack();
                  }

                  this.manageMovie(var7, 18);
                  this.startQuestTimer("SPAWN_SUPPORT", 27000L, var7.controller, null);
               }

               if (var2.getMountType() == MountType.STRIDER
                  && var2.getFirstEffect(ANTI_STRIDER.getId()) == null
                  && !var1.isCastingNow()
                  && !var1.isSkillDisabled(ANTI_STRIDER.getSkill())) {
                  var1.setTarget(var2);
                  var1.doCast(ANTI_STRIDER.getSkill());
               }
         }
      }

      return super.onAttack(var1, var2, var3, var4, var5);
   }

   @Override
   public String onSpellFinished(Npc var1, Player var2, Skill var3) {
      ReflectionWorld var4 = ReflectionManager.getInstance().getWorld(var1.getReflectionId());
      if (var4 instanceof FreyaBattleHard.IQCNBWorld) {
         switch(var1.getId()) {
            case 18854:
               if (var3 == SUICIDE_BREATH.getSkill()) {
                  var1.doDie(var1);
               }
         }
      }

      return super.onSpellFinished(var1, var2, var3);
   }

   @Override
   public String onKill(Npc var1, Player var2, boolean var3) {
      ReflectionWorld var4 = ReflectionManager.getInstance().getWorld(var1.getReflectionId());
      if (var4 instanceof FreyaBattleHard.IQCNBWorld) {
         FreyaBattleHard.IQCNBWorld var7 = (FreyaBattleHard.IQCNBWorld)var4;
         switch(var1.getId()) {
            case 18856:
               NpcStats var5 = var7.controller.getVariables();
               int var6 = var5.getInteger("KNIGHT_COUNT");
               if (var6 < 10 && var7.isStatus(2)) {
                  var5.set("KNIGHT_COUNT", ++var6);
                  if (var6 == 10) {
                     this.notifyEvent("STAGE_2_MOVIE", var7.controller, null);
                     var7.setStatus(3);
                  }
               }
               break;
            case 25700:
               if (var7.secondStageGuardSpawn != null) {
                  var7.secondStageGuardSpawn.cancel(true);
               }

               this.manageDespawnMinions(var7);
               this.manageTimer(var7, 60, NpcStringId.TIME_REMAINING_UNTIL_NEXT_BATTLE);
               this.cancelQuestTimer("STAGE_2_FAILED", var7.controller, null);
               this.startQuestTimer("STAGE_3_MOVIE", 60000L, var7.controller, null);
               var7.setStatus(4);
               break;
            case 29177:
               var7.freya.deleteMe();
               var7.freya = null;
               this.notifyEvent("STAGE_1_FINISH", var7.controller, null);
               break;
            case 29180:
               var7.canSpawnMobs = false;
               var7.isSupportActive = false;
               this.doCleanup(var7);
               this.manageMovie(var7, 19);
               this.finishInstance(var7, true);
               this.cancelQuestTimer("GIVE_SUPPORT", var7.controller, null);
               if (var7.supp_Jinia != null) {
                  var7.supp_Jinia.deleteMe();
                  var7.supp_Jinia = null;
               }

               if (var7.supp_Kegor != null) {
                  var7.supp_Kegor.deleteMe();
                  var7.supp_Kegor = null;
               }

               this.startQuestTimer("FINISH_WORLD", 300000L, var7.controller, null);
         }
      }

      return super.onKill(var1, var2, var3);
   }

   private final synchronized void enterInstance(Player var1, Npc var2) {
      if (this.enterInstance(var1, var2, new FreyaBattleHard.IQCNBWorld(), 144)) {
         ReflectionWorld var3 = ReflectionManager.getInstance().getPlayerWorld(var1);

         for(Player var5 : ((FreyaBattleHard.IQCNBWorld)var3).playersInside) {
            if (var5 != null) {
               var5.broadcastPacket(ExChangeClientEffectInfo.STATIC_FREYA_DEFAULT);

               for(int var9 : EMMITERS) {
                  var5.sendPacket(new EventTrigger(var9, false));
               }
            }
         }
      }
   }

   @Override
   protected void onTeleportEnter(Player var1, ReflectionTemplate var2, ReflectionWorld var3, boolean var4) {
      if (var4) {
         var3.addAllowed(var1.getObjectId());
         ((FreyaBattleHard.IQCNBWorld)var3).playersInside.add(var1);
         var1.getAI().setIntention(CtrlIntention.IDLE);
         var1.setReflectionId(var3.getReflectionId());
         Location var5 = var2.getTeleportCoord();
         var1.teleToLocation(var5, true);
         if (var1.hasSummon()) {
            var1.getSummon().getAI().setIntention(CtrlIntention.IDLE);
            var1.getSummon().setReflectionId(var3.getReflectionId());
            var1.getSummon().teleToLocation(var5, true);
         }
      } else {
         Location var6 = var3.isStatus(4) ? new Location(114694, -113700, -11200) : var2.getTeleportCoord();
         var1.getAI().setIntention(CtrlIntention.IDLE);
         var1.setReflectionId(var3.getReflectionId());
         var1.teleToLocation(var6, true);
         if (var1.hasSummon()) {
            var1.getSummon().getAI().setIntention(CtrlIntention.IDLE);
            var1.getSummon().setReflectionId(var3.getReflectionId());
            var1.getSummon().teleToLocation(var6, true);
         }
      }
   }

   private void manageRandomAttack(FreyaBattleHard.IQCNBWorld var1, Attackable var2) {
      ArrayList var3 = new ArrayList();

      for(Player var5 : var1.playersInside) {
         if (var5 != null && !var5.isDead() && var5.getReflectionId() == var1.getReflectionId() && !var5.isInvisible()) {
            var3.add(var5);
         }
      }

      if (var3.size() > 0) {
         Player var6 = (Player)var3.get(Rnd.get(var3.size()));
         var2.addDamageHate(var6, 0, 999);
         var2.setRunning();
         var2.getAI().setIntention(CtrlIntention.ATTACK, var6);
      } else {
         this.startQuestTimer("FIND_TARGET", 10000L, var2, null);
      }
   }

   private void manageDespawnMinions(FreyaBattleHard.IQCNBWorld var1) {
      if (var1 != null) {
         for(Attackable var3 : var1.spawnedMobs) {
            if (var3 != null) {
               this.cancelQuestTimers(var3);
               if (!var3.isDead()) {
                  var3.doDie(var3);
               }
            }
         }
      }
   }

   protected void manageBlockMinions(FreyaBattleHard.IQCNBWorld var1) {
      for(Attackable var3 : var1.spawnedMobs) {
         if (var3 != null && !var3.isDead()) {
            var3.block();
            var3.disableCoreAI(true);
            var3.abortAttack();
         }
      }
   }

   protected void manageUnblockMinions(FreyaBattleHard.IQCNBWorld var1) {
      for(Attackable var3 : var1.spawnedMobs) {
         if (var3 != null && !var3.isDead()) {
            var3.unblock();
            var3.disableCoreAI(false);
         }
      }
   }

   private void manageTimer(FreyaBattleHard.IQCNBWorld var1, int var2, NpcStringId var3) {
      for(Player var5 : var1.playersInside) {
         if (var5 != null && var5.getReflectionId() == var1.getReflectionId()) {
            var5.sendPacket(new ExSendUIEvent(var5, false, false, var2, 0, var3));
         }
      }
   }

   private void manageScreenMsg(FreyaBattleHard.IQCNBWorld var1, NpcStringId var2) {
      ExShowScreenMessage var3 = new ExShowScreenMessage(var2, 2, 6000);

      for(Player var5 : var1.playersInside) {
         if (var5 != null && var5.getReflectionId() == var1.getReflectionId()) {
            var5.sendPacket(var3);
         }
      }
   }

   private void manageMovie(FreyaBattleHard.IQCNBWorld var1, int var2) {
      for(Player var4 : var1.playersInside) {
         if (var4 != null && var4.getReflectionId() == var1.getReflectionId()) {
            var4.showQuestMovie(var2);
         }
      }
   }

   protected void doCleanup(FreyaBattleHard.IQCNBWorld var1) {
      this.manageDespawnMinions(var1);
      if (var1.firstStageGuardSpawn != null) {
         var1.firstStageGuardSpawn.cancel(true);
      }

      if (var1.secondStageGuardSpawn != null) {
         var1.secondStageGuardSpawn.cancel(true);
      }

      if (var1.thirdStageGuardSpawn != null) {
         var1.thirdStageGuardSpawn.cancel(true);
      }
   }

   public static void main(String[] var0) {
      new FreyaBattleHard();
   }

   private class GuardSpawnTask extends RunnableImpl {
      private int _mode;
      private int _knightsMin;
      private int _knightsMax;
      private int _breathMin;
      private int _breathMax;
      private final FreyaBattleHard.IQCNBWorld _world;

      GuardSpawnTask(int var2, FreyaBattleHard.IQCNBWorld var3) {
         this._mode = var2;
         this._world = var3;
         if (this._mode < 1 || this._mode > 3) {
            this._mode = 1;
         }
      }

      @Override
      public void runImpl() throws Exception {
         if (ReflectionManager.getInstance().getWorld(this._world.getReflectionId()) == this._world) {
            Reflection var1 = ReflectionManager.getInstance().getReflection(this._world.getReflectionId());
            if (var1 != null) {
               switch(this._mode) {
                  case 1:
                     String[] var2 = var1.getParams().getString("guardStage1").split(";");
                     this._knightsMin = Integer.parseInt(var2[0]);
                     this._knightsMax = Integer.parseInt(var2[1]);
                     this._breathMin = Integer.parseInt(var2[2]);
                     this._breathMax = Integer.parseInt(var2[3]);
                     break;
                  case 2:
                     String[] var3 = var1.getParams().getString("guardStage2").split(";");
                     this._knightsMin = Integer.parseInt(var3[0]);
                     this._knightsMax = Integer.parseInt(var3[1]);
                     this._breathMin = Integer.parseInt(var3[2]);
                     this._breathMax = Integer.parseInt(var3[3]);
                     break;
                  case 3:
                     String[] var4 = var1.getParams().getString("guardStage3").split(";");
                     this._knightsMin = Integer.parseInt(var4[0]);
                     this._knightsMax = Integer.parseInt(var4[1]);
                     this._breathMin = Integer.parseInt(var4[2]);
                     this._breathMax = Integer.parseInt(var4[3]);
               }

               if (this._world.canSpawnMobs) {
                  for(int var5 = 0; var5 < Rnd.get(this._knightsMin, this._knightsMax); ++var5) {
                     Attackable var6 = (Attackable)Quest.addSpawn(
                        18856,
                        SpawnTerritory.getRandomLoc(FreyaBattleHard.CENTRAL_ROOM, var1.getGeoIndex(), false),
                        false,
                        0L,
                        false,
                        this._world.getReflectionId()
                     );
                     this._world.spawnedMobs.add(var6);
                  }

                  for(int var7 = 0; var7 < Rnd.get(this._breathMin, this._breathMax); ++var7) {
                     Attackable var9 = (Attackable)Quest.addSpawn(
                        18854,
                        SpawnTerritory.getRandomLoc(FreyaBattleHard.CENTRAL_ROOM, var1.getGeoIndex(), false),
                        false,
                        0L,
                        false,
                        this._world.getReflectionId()
                     );
                     this._world.spawnedMobs.add(var9);
                  }

                  if (Rnd.chance(60)) {
                     for(int var8 = 0; var8 < Rnd.get(1, 3); ++var8) {
                        Attackable var10 = (Attackable)Quest.addSpawn(
                           18853,
                           SpawnTerritory.getRandomLoc(FreyaBattleHard.CENTRAL_ROOM, var1.getGeoIndex(), false),
                           false,
                           0L,
                           false,
                           this._world.getReflectionId()
                        );
                        this._world.spawnedMobs.add(var10);
                     }
                  }
               }
            }
         }
      }
   }

   protected class IQCNBWorld extends ReflectionWorld {
      public List<Player> playersInside = new ArrayList<>();
      public List<Npc> knightStatues = new ArrayList<>();
      public List<Attackable> spawnedMobs = new CopyOnWriteArrayList<>();
      public NpcInstance controller = null;
      public GrandBossInstance freya = null;
      public QuestGuardInstance supp_Jinia = null;
      public QuestGuardInstance supp_Kegor = null;
      public boolean isSupportActive = false;
      public boolean canSpawnMobs = false;
      public ScheduledFuture<?> firstStageGuardSpawn;
      public ScheduledFuture<?> secondStageGuardSpawn;
      public ScheduledFuture<?> thirdStageGuardSpawn;
   }
}
