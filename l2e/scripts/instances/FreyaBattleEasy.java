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
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.service.BotFunctions;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.spawn.SpawnTerritory;
import l2e.gameserver.model.stats.NpcStats;
import l2e.gameserver.network.NpcStringId;
import l2e.gameserver.network.serverpackets.EventTrigger;
import l2e.gameserver.network.serverpackets.ExChangeClientEffectInfo;
import l2e.gameserver.network.serverpackets.ExSendUIEvent;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;

public class FreyaBattleEasy extends AbstractReflection {
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

   private FreyaBattleEasy() {
      super(FreyaBattleEasy.class.getSimpleName(), "instances");
      this.addStartNpc(new int[]{32762, 18851, 18850});
      this.addFirstTalkId(new int[]{18851, 18850});
      this.addTalkId(new int[]{32762, 32781, 18851});
      this.addAttackId(new int[]{29177, 29179, 18854, 18855});
      this.addKillId(new int[]{29177, 25699, 29179, 18855});
      this.addSpawnId(new int[]{25699, 29179, 18855});
      this.addSpellFinishedId(new int[]{18854});
   }

   @Override
   public String onAdvEvent(String var1, Npc var2, Player var3) {
      if (var1.equals("enterEasy")) {
         this.enterInstance(var3, var2);
      } else {
         ReflectionWorld var4 = ReflectionManager.getInstance().getWorld(var2.getReflectionId());
         if (var4 instanceof FreyaBattleEasy.IQCNBWorld) {
            FreyaBattleEasy.IQCNBWorld var6 = (FreyaBattleEasy.IQCNBWorld)var4;
            switch(var1) {
               case "openDoor":
                  if (var2.isScriptValue(0)) {
                     var2.setScriptValue(1);
                     var6.getReflection().openDoor(23140101);
                     var6.controller = (NpcInstance)addSpawn(18919, new Location(114394, -112383, -11200), false, 0L, true, var6.getReflectionId());

                     for(Location var40 : STATUES_STAGE_1_LOC) {
                        Npc var42 = addSpawn(18919, var40, false, 0L, false, var6.getReflectionId());
                        var6.knightStatues.add(var42);
                     }

                     for(Player var34 : var6.playersInside) {
                        if (var34 != null && !var34.isDead() && var34.getReflectionId() == var6.getReflectionId()) {
                           QuestState var38 = var34.getQuestState("_10286_ReunionWithSirra");
                           if (var38 != null && var38.isCond(5)) {
                              var38.setCond(6, true);
                           }
                        }
                     }

                     this.startQuestTimer("STAGE_1_MOVIE", 60000L, var6.controller, null);
                  }
                  break;
               case "portInside":
                  if (var3.isInParty() && var3.getParty().isLeader(var3) && var3.getVarB("autoTeleport@", false)) {
                     for(Player var32 : var3.getParty().getMembers()) {
                        if (var32 != null
                           && var32.getObjectId() != var3.getObjectId()
                           && Util.checkIfInRange(1000, var3, var32, true)
                           && var32.getReflectionId() == var6.getReflectionId()
                           && BotFunctions.checkCondition(var32, false)
                           && var32.getIPAddress().equalsIgnoreCase(var3.getIPAddress())) {
                           this.teleportPlayer(var32, new Location(114694, -113700, -11200), var6.getReflectionId());
                        }
                     }
                  }

                  this.teleportPlayer(var3, new Location(114694, -113700, -11200), var6.getReflectionId());
                  break;
               case "killFreya":
                  for(Player var31 : var6.playersInside) {
                     if (var31 != null && var31.getReflectionId() == var6.getReflectionId()) {
                        QuestState var36 = var31.getQuestState("_10286_ReunionWithSirra");
                        if (var36 != null && var36.isCond(6)) {
                           var36.setMemoState(10);
                           var36.setCond(7, true);
                        }
                     }
                  }

                  var6.supp_Kegor.deleteMe();
                  var6.freya.decayMe();
                  this.manageMovie(var6, 20);
                  this.cancelQuestTimer("FINISH_WORLD", var6.controller, null);
                  this.startQuestTimer("FINISH_WORLD", 58500L, var6.controller, null);
                  break;
               case "18851-01.htm":
                  return var1;
               case "STAGE_1_MOVIE":
                  var6.getReflection().closeDoor(23140101);
                  var6.setStatus(1);
                  this.manageMovie(var6, 15);
                  this.startQuestTimer("STAGE_1_START", 53500L, var6.controller, null);
                  break;
               case "STAGE_1_START":
                  var6.freya = (GrandBossInstance)addSpawn(29177, new Location(114720, -117085, -11088, 15956), false, 0L, true, var6.getReflectionId());
                  this.manageScreenMsg(var6, NpcStringId.BEGIN_STAGE_1);
                  this.startQuestTimer("STAGE_1_SPAWN", 2000L, var6.freya, null);
                  break;
               case "STAGE_1_SPAWN":
                  var6.canSpawnMobs = true;
                  this.notifyEvent("START_SPAWN", var6.controller, null);
                  if (!var6.freya.isInCombat()) {
                     this.manageScreenMsg(var6, NpcStringId.FREYA_HAS_STARTED_TO_MOVE);
                     var6.freya.setRunning();
                     var6.freya.getAI().setIntention(CtrlIntention.MOVING, new Location(114730, -114805, -11200));
                  }

                  Reflection var16 = ReflectionManager.getInstance().getReflection(var6.getReflectionId());
                  if (var16 != null) {
                     var6.firstStageGuardSpawn = ThreadPoolManager.getInstance()
                        .scheduleAtFixedRate(new FreyaBattleEasy.GuardSpawnTask(1, var6), 2000L, (long)var16.getParams().getInteger("guardsInterval") * 1000L);
                  }
                  break;
               case "STAGE_1_FINISH":
                  var6.canSpawnMobs = false;
                  if (var6.firstStageGuardSpawn != null) {
                     var6.firstStageGuardSpawn.cancel(true);
                  }

                  this.manageDespawnMinions(var6);
                  this.manageMovie(var6, 16);
                  this.startQuestTimer("STAGE_1_PAUSE", 23100L, var6.controller, null);
                  break;
               case "STAGE_1_PAUSE":
                  var6.freya = (GrandBossInstance)addSpawn(29178, new Location(114723, -117502, -10672, 15956), false, 0L, true, var6.getReflectionId());
                  var6.freya.setIsInvul(true);
                  var6.freya.block();
                  var6.freya.disableCoreAI(true);
                  this.manageTimer(var6, 60, NpcStringId.TIME_REMAINING_UNTIL_NEXT_BATTLE);
                  var6.setStatus(2);
                  this.startQuestTimer("STAGE_2_START", 60000L, var6.controller, null);
                  break;
               case "STAGE_2_START":
                  var6.canSpawnMobs = true;
                  this.notifyEvent("START_SPAWN", var6.controller, null);
                  this.manageScreenMsg(var6, NpcStringId.BEGIN_STAGE_2);
                  Reflection var15 = ReflectionManager.getInstance().getReflection(var6.getReflectionId());
                  if (var15 != null) {
                     var6.secondStageGuardSpawn = ThreadPoolManager.getInstance()
                        .scheduleAtFixedRate(new FreyaBattleEasy.GuardSpawnTask(2, var6), 2000L, (long)var15.getParams().getInteger("guardsInterval") * 1000L);
                  }
                  break;
               case "STAGE_2_MOVIE":
                  this.manageBlockMinions(var6);
                  this.manageMovie(var6, 23);
                  this.startQuestTimer("STAGE_2_GLAKIAS", 7000L, var6.controller, null);
                  break;
               case "STAGE_2_GLAKIAS":
                  this.manageUnblockMinions(var6);

                  for(Location var39 : STATUES_STAGE_2_LOC) {
                     Npc var41 = addSpawn(18919, var39, false, 0L, false, var6.getReflectionId());
                     var6.knightStatues.add(var41);
                     this.startQuestTimer("SPAWN_KNIGHT", 5000L, var41, null);
                  }

                  addSpawn(25699, new Location(114707, -114799, -11199, 15956), false, 0L, true, var6.getReflectionId());
                  break;
               case "STAGE_3_MOVIE":
                  var6.freya.deleteMe();
                  this.manageMovie(var6, 17);
                  this.startQuestTimer("STAGE_3_START", 21500L, var6.controller, null);
                  break;
               case "STAGE_3_START":
                  for(Player var29 : var6.playersInside) {
                     if (var29 != null) {
                        var29.broadcastPacket(ExChangeClientEffectInfo.STATIC_FREYA_DESTROYED);

                        for(int var14 : EMMITERS) {
                           var29.sendPacket(new EventTrigger(var14, true));
                        }
                     }
                  }

                  this.manageScreenMsg(var6, NpcStringId.BEGIN_STAGE_3);
                  var6.canSpawnMobs = true;
                  Reflection var5 = ReflectionManager.getInstance().getReflection(var6.getReflectionId());
                  if (var5 != null) {
                     var6.thirdStageGuardSpawn = ThreadPoolManager.getInstance()
                        .scheduleAtFixedRate(new FreyaBattleEasy.GuardSpawnTask(3, var6), 2000L, (long)var5.getParams().getInteger("guardsInterval") * 1000L);
                  }

                  var6.freya = (GrandBossInstance)addSpawn(29179, new Location(114720, -117085, -11088, 15956), false, 0L, true, var6.getReflectionId());
                  var6.setStatus(4);
                  this.notifyEvent("START_SPAWN", var6.controller, null);
                  if (!var6.freya.isInCombat()) {
                     this.manageScreenMsg(var6, NpcStringId.FREYA_HAS_STARTED_TO_MOVE);
                     var6.freya.setRunning();
                     var6.freya.getAI().setIntention(CtrlIntention.MOVING, new Location(114730, -114805, -11200));
                  }
                  break;
               case "SPAWN_SUPPORT":
                  this.manageUnblockMinions(var6);

                  for(Player var28 : var6.playersInside) {
                     var28.setIsInvul(false);
                     var28.unblock();
                  }

                  var6.freya.setIsInvul(false);
                  var6.freya.unblock();
                  var6.canSpawnMobs = true;
                  var6.freya.disableCoreAI(false);
                  this.manageScreenMsg(var6, NpcStringId.BEGIN_STAGE_4);
                  var6.supp_Jinia = (QuestGuardInstance)addSpawn(18850, new Location(114751, -114781, -11205), false, 0L, true, var6.getReflectionId());
                  var6.supp_Jinia.setRunning();
                  var6.supp_Jinia.setIsInvul(true);
                  var6.supp_Jinia.setCanReturnToSpawnPoint(false);
                  var6.supp_Kegor = (QuestGuardInstance)addSpawn(18851, new Location(114659, -114796, -11205), false, 0L, true, var6.getReflectionId());
                  var6.supp_Kegor.setRunning();
                  var6.supp_Kegor.setIsInvul(true);
                  var6.supp_Kegor.setCanReturnToSpawnPoint(false);
                  this.startQuestTimer("GIVE_SUPPORT", 1000L, var6.controller, null);
                  break;
               case "GIVE_SUPPORT":
                  if (var6.isSupportActive) {
                     var6.supp_Jinia.doCast(JINIA_SUPPORT.getSkill());
                     var6.supp_Kegor.doCast(KEGOR_SUPPORT.getSkill());
                  }
                  break;
               case "START_SPAWN":
                  for(Npc var27 : var6.knightStatues) {
                     this.notifyEvent("SPAWN_KNIGHT", var27, null);
                  }
                  break;
               case "SPAWN_KNIGHT":
                  if (var6.canSpawnMobs) {
                     Location var17 = new Location(
                        MIDDLE_POINT.getX() + getRandom(-1000, 1000), MIDDLE_POINT.getY() + getRandom(-1000, 1000), MIDDLE_POINT.getZ()
                     );
                     Attackable var26 = (Attackable)addSpawn(18855, var2.getLocation(), false, 0L, false, var6.getReflectionId());
                     var26.getSpawn().setLocation(var17);
                     var6.spawnedMobs.add(var26);
                  }
                  break;
               case "FIND_TARGET":
                  if (!var2.isDead()) {
                     this.manageRandomAttack(var6, (Attackable)var2);
                  }
                  break;
               case "ELEMENTAL_KILLED":
                  if (var2.getVariables().getInteger("SUICIDE_ON") == 1) {
                     var2.setTarget(var2);
                     var2.doCast(SUICIDE_BREATH.getSkill());
                  }
                  break;
               case "FINISH_WORLD":
                  if (var6.freya != null) {
                     var6.freya.deleteMe();
                     var6.freya = null;
                  }

                  if (var6.supp_Kegor != null) {
                     var6.supp_Kegor.deleteMe();
                     var6.supp_Kegor = null;
                  }

                  if (var6.supp_Jinia != null) {
                     var6.supp_Jinia.deleteMe();
                     var6.supp_Jinia = null;
                  }

                  if (var6.controller != null) {
                     var6.controller.deleteMe();
                  }

                  for(Player var10 : var6.playersInside) {
                     if (var10 != null) {
                        var10.broadcastPacket(ExChangeClientEffectInfo.STATIC_FREYA_DEFAULT);
                     }
                  }
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
   public String onFirstTalk(Npc var1, Player var2) {
      ReflectionWorld var3 = ReflectionManager.getInstance().getWorld(var1.getReflectionId());
      if (var3 instanceof FreyaBattleEasy.IQCNBWorld) {
         FreyaBattleEasy.IQCNBWorld var4 = (FreyaBattleEasy.IQCNBWorld)var3;
         if (var1.getId() == 18850) {
            var2.sendActionFailed();
            return null;
         }

         if (var1.getId() == 18851) {
            if (var4.isSupportActive) {
               var2.sendActionFailed();
               return null;
            }

            return "18851.htm";
         }
      }

      var2.sendActionFailed();
      return null;
   }

   @Override
   public String onAttack(Npc var1, Player var2, int var3, boolean var4, Skill var5) {
      ReflectionWorld var6 = ReflectionManager.getInstance().getWorld(var1.getReflectionId());
      if (var6 instanceof FreyaBattleEasy.IQCNBWorld) {
         FreyaBattleEasy.IQCNBWorld var7 = (FreyaBattleEasy.IQCNBWorld)var6;
         switch(var1.getId()) {
            case 18854:
               if (var1.getCurrentHp() < var1.getMaxHp() / 20.0 && var1.getVariables().getInteger("SUICIDE_ON", 0) == 0) {
                  var1.getVariables().set("SUICIDE_ON", 1);
                  this.startQuestTimer("ELEMENTAL_KILLED", 1000L, var1, null);
               }
               break;
            case 29179:
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
      if (var4 instanceof FreyaBattleEasy.IQCNBWorld) {
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
      if (var4 instanceof FreyaBattleEasy.IQCNBWorld) {
         FreyaBattleEasy.IQCNBWorld var7 = (FreyaBattleEasy.IQCNBWorld)var4;
         switch(var1.getId()) {
            case 18855:
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
            case 25699:
               if (var7.secondStageGuardSpawn != null) {
                  var7.secondStageGuardSpawn.cancel(true);
               }

               this.manageDespawnMinions(var7);
               var7.canSpawnMobs = false;
               this.manageTimer(var7, 60, NpcStringId.TIME_REMAINING_UNTIL_NEXT_BATTLE);
               this.startQuestTimer("STAGE_3_MOVIE", 60000L, var7.controller, null);
               break;
            case 29177:
               var7.freya.deleteMe();
               var7.freya = null;
               this.notifyEvent("STAGE_1_FINISH", var7.controller, null);
               break;
            case 29179:
               var7.canSpawnMobs = false;
               var7.isSupportActive = false;
               if (var7.thirdStageGuardSpawn != null) {
                  var7.thirdStageGuardSpawn.cancel(true);
               }

               this.doCleanup(var7);
               this.manageMovie(var7, 19);
               this.finishInstance(var7, true);
               this.cancelQuestTimer("GIVE_SUPPORT", var7.controller, null);
               this.startQuestTimer("FINISH_WORLD", 300000L, var7.controller, null);
         }
      }

      return super.onKill(var1, var2, var3);
   }

   private final synchronized void enterInstance(Player var1, Npc var2) {
      if (this.enterInstance(var1, var2, new FreyaBattleEasy.IQCNBWorld(), 139)) {
         ReflectionWorld var3 = ReflectionManager.getInstance().getPlayerWorld(var1);

         for(Player var5 : ((FreyaBattleEasy.IQCNBWorld)var3).playersInside) {
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
         ((FreyaBattleEasy.IQCNBWorld)var3).playersInside.add(var1);
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

   private void manageRandomAttack(FreyaBattleEasy.IQCNBWorld var1, Attackable var2) {
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

   private void manageDespawnMinions(FreyaBattleEasy.IQCNBWorld var1) {
      for(Attackable var3 : var1.spawnedMobs) {
         if (var3 != null && !var3.isDead()) {
            var3.doDie(var3);
            var3.deleteMe();
         }
      }
   }

   protected void manageBlockMinions(FreyaBattleEasy.IQCNBWorld var1) {
      for(Attackable var3 : var1.spawnedMobs) {
         if (var3 != null && !var3.isDead()) {
            var3.block();
            var3.disableCoreAI(true);
            var3.abortAttack();
         }
      }
   }

   protected void manageUnblockMinions(FreyaBattleEasy.IQCNBWorld var1) {
      for(Attackable var3 : var1.spawnedMobs) {
         if (var3 != null && !var3.isDead()) {
            var3.unblock();
            var3.disableCoreAI(false);
         }
      }
   }

   private void manageTimer(FreyaBattleEasy.IQCNBWorld var1, int var2, NpcStringId var3) {
      for(Player var5 : var1.playersInside) {
         if (var5 != null && var5.getReflectionId() == var1.getReflectionId()) {
            var5.sendPacket(new ExSendUIEvent(var5, false, false, var2, 0, var3));
         }
      }
   }

   private void manageScreenMsg(FreyaBattleEasy.IQCNBWorld var1, NpcStringId var2) {
      ExShowScreenMessage var3 = new ExShowScreenMessage(var2, 2, 6000);

      for(Player var5 : var1.playersInside) {
         if (var5 != null && var5.getReflectionId() == var1.getReflectionId()) {
            var5.sendPacket(var3);
         }
      }
   }

   private void manageMovie(FreyaBattleEasy.IQCNBWorld var1, int var2) {
      for(Player var4 : var1.playersInside) {
         if (var4 != null && var4.getReflectionId() == var1.getReflectionId()) {
            var4.showQuestMovie(var2);
         }
      }
   }

   protected void doCleanup(FreyaBattleEasy.IQCNBWorld var1) {
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
      new FreyaBattleEasy();
   }

   private class GuardSpawnTask extends RunnableImpl {
      private int _mode;
      private int _knightsMin;
      private int _knightsMax;
      private int _breathMin;
      private int _breathMax;
      private final FreyaBattleEasy.IQCNBWorld _world;

      GuardSpawnTask(int var2, FreyaBattleEasy.IQCNBWorld var3) {
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
                        18855,
                        SpawnTerritory.getRandomLoc(FreyaBattleEasy.CENTRAL_ROOM, var1.getGeoIndex(), false),
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
                        SpawnTerritory.getRandomLoc(FreyaBattleEasy.CENTRAL_ROOM, var1.getGeoIndex(), false),
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
                           SpawnTerritory.getRandomLoc(FreyaBattleEasy.CENTRAL_ROOM, var1.getGeoIndex(), false),
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
