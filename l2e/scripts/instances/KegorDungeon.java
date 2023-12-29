package l2e.scripts.instances;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Attackable;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.entity.Reflection;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;

public final class KegorDungeon extends AbstractReflection {
   private static final Location[] MOB_SPAWNS = new Location[]{
      new Location(185216, -184112, -3308, -15396),
      new Location(185456, -184240, -3308, -19668),
      new Location(185712, -184384, -3308, -26696),
      new Location(185920, -184544, -3308, -32544),
      new Location(185664, -184720, -3308, 27892)
   };

   private KegorDungeon() {
      super(KegorDungeon.class.getSimpleName(), "instances");
      this.addFirstTalkId(18846);
      this.addKillId(new int[]{18846, 22766});
      this.addStartNpc(new int[]{32654, 32653});
      this.addTalkId(new int[]{32654, 32653, 18846});
   }

   private final synchronized void enterInstance(Player player, Npc npc) {
      this.enterInstance(player, npc, new KegorDungeon.KDWorld(), 138);
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
   public String onAdvEvent(String event, Npc npc, Player player) {
      ReflectionWorld world = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      switch(event) {
         case "BUFF":
            if (player != null && !player.isDead() && !npc.isDead() && npc.isInsideRadius(player, 1000, true, false) && npc.isScriptValue(1)) {
               npc.setTarget(player);
               npc.doCast(new SkillHolder(6286, 1).getSkill());
            }

            this.startQuestTimer("BUFF", 30000L, npc, player);
            break;
         case "TIMER":
            if (world != null && world instanceof KegorDungeon.KDWorld) {
               for(Location loc : MOB_SPAWNS) {
                  Attackable spawnedMob = (Attackable)addSpawn(22766, loc, false, 0L, false, world.getReflectionId());
                  spawnedMob.setScriptValue(1);
                  spawnedMob.setIsRunning(true);
                  spawnedMob.getAI().setIntention(CtrlIntention.ATTACK, npc);
                  spawnedMob.addDamageHate(npc, 0, 999999);
               }

               ((KegorDungeon.KDWorld)world).count = MOB_SPAWNS.length;
            }
            break;
         case "FINISH":
            if (world != null && world instanceof KegorDungeon.KDWorld) {
               for(Npc kegor : World.getInstance().getAroundNpc(player)) {
                  if (kegor.getId() == 18846) {
                     kegor.setScriptValue(2);
                     kegor.setWalking();
                     kegor.setTarget(player);
                     kegor.getAI().setIntention(CtrlIntention.FOLLOW, player);
                     this.broadcastNpcSay(kegor, 22, NpcStringId.I_CAN_FINALLY_TAKE_A_BREATHER_BY_THE_WAY_WHO_ARE_YOU_HMM_I_THINK_I_KNOW_WHO_SENT_YOU);
                     break;
                  }
               }

               Reflection inst = ReflectionManager.getInstance().getReflection(world.getReflectionId());
               if (inst != null) {
                  inst.setDuration(3000);
               }
            }
      }

      return super.onAdvEvent(event, npc, player);
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      QuestState qs = player.getQuestState("_10284_AcquisitionOfDivineSword");
      if (qs != null) {
         if (qs.isMemoState(2)) {
            return npc.isScriptValue(0) ? "18846.htm" : "18846-01.htm";
         }

         if (qs.isMemoState(3)) {
            ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
            world.removeAllowed(player.getObjectId());
            player.setReflectionId(0);
            player.teleToLocation(new Location(178823, -184303, -347, 0), 0, true);
            qs.rewardItems(57, 296425L);
            qs.addExpAndSp(921805, 82230);
            qs.exitQuest(false, true);
            return "18846-03.htm";
         }
      }

      return super.onFirstTalk(npc, player);
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      ReflectionWorld world = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      KegorDungeon.KDWorld mmWorld = (KegorDungeon.KDWorld)world;
      if (world == null) {
         return null;
      } else {
         if (npc.getId() == 18846) {
            mmWorld.count = 9999;
            this.broadcastNpcSay(npc, 22, NpcStringId.HOW_COULD_I_FALL_IN_A_PLACE_LIKE_THIS);
            ReflectionManager.getInstance().getReflection(world.getReflectionId()).setDuration(1000);
         } else if (npc.isScriptValue(1)) {
            int count;
            synchronized(mmWorld) {
               count = --mmWorld.count;
            }

            if (count == 0) {
               QuestState qs = player.getQuestState("_10284_AcquisitionOfDivineSword");
               if (qs != null && qs.isMemoState(2)) {
                  qs.setMemoState(3);
                  qs.setCond(6, true);
                  this.startQuestTimer("FINISH", 3000L, npc, player);
               }
            }
         }

         return super.onKill(npc, player, isSummon);
      }
   }

   @Override
   public String onTalk(Npc npc, Player talker) {
      switch(npc.getId()) {
         case 18846:
            QuestState qs = talker.getQuestState("_10284_AcquisitionOfDivineSword");
            if (qs != null && qs.isMemoState(2) && qs.hasQuestItems(15514) && npc.isScriptValue(0)) {
               qs.takeItems(15514, -1L);
               qs.setCond(5, true);
               npc.setScriptValue(1);
               this.startQuestTimer("TIMER", 3000L, npc, talker);
               this.startQuestTimer("BUFF", 3500L, npc, talker);
               return "18846-02.htm";
            }
            break;
         case 32653:
         case 32654:
            QuestState qs = talker.getQuestState("_10284_AcquisitionOfDivineSword");
            if (qs != null && qs.isMemoState(2)) {
               if (!qs.hasQuestItems(15514)) {
                  qs.giveItems(15514, 1L);
               }

               qs.setCond(4, true);
               this.enterInstance(talker, npc);
            }
      }

      return super.onTalk(npc, talker);
   }

   public static void main(String[] args) {
      new KegorDungeon();
   }

   private class KDWorld extends ReflectionWorld {
      private int count;

      private KDWorld() {
      }
   }
}
