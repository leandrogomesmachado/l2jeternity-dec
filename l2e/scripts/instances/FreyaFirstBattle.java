package l2e.scripts.instances;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.holders.SkillHolder;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.network.NpcStringId;

public final class FreyaFirstBattle extends AbstractReflection {
   private FreyaFirstBattle() {
      super(FreyaFirstBattle.class.getSimpleName(), "instances");
      this.addStartNpc(32781);
      this.addTalkId(32781);
      this.addSeeCreatureId(new int[]{18848, 18849, 18926});
      this.addSpawnId(new int[]{18847});
      this.addSpellFinishedId(new int[]{18847});
   }

   private final synchronized void enterInstance(Player player, Npc npc) {
      if (this.enterInstance(player, npc, new FreyaFirstBattle.IQCWorld(), 137)) {
         ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
         ((FreyaFirstBattle.IQCWorld)world).player = player;
         world.getReflection().openDoor(23140101);
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
   public String onAdvEvent(String event, Npc npc, Player player) {
      switch(event) {
         case "TIMER_MOVING":
            if (npc != null) {
               npc.getAI().setIntention(CtrlIntention.MOVING, new Location(114730, -114805, -11200, 50));
            }
            break;
         case "TIMER_BLIZZARD":
            this.broadcastNpcSay(npc, 22, NpcStringId.I_CAN_NO_LONGER_STAND_BY);
            npc.stopMove(null);
            npc.setTarget(player);
            npc.doCast(new SkillHolder(6276, 1).getSkill());
            break;
         case "TIMER_SCENE_21":
            if (npc != null) {
               player.showQuestMovie(21);
               npc.deleteMe();
               this.startQuestTimer("TIMER_PC_LEAVE", 24000L, npc, player);
            }
            break;
         case "TIMER_PC_LEAVE":
            QuestState qs = player.getQuestState("_10285_MeetingSirra");
            if (qs != null) {
               qs.setMemoState(3);
               qs.setCond(10, true);
               ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
               if (world != null) {
                  world.removeAllowed(player.getObjectId());
               }

               player.setReflectionId(0);
               player.teleToLocation(new Location(113883, -108777, -848, 0), 0, true);
            }
      }

      return super.onAdvEvent(event, npc, player);
   }

   @Override
   public String onSeeCreature(Npc npc, Creature creature, boolean isSummon) {
      if (creature.isPlayer() && npc.isScriptValue(0)) {
         this.broadcastNpcSay(npc, 22, NpcStringId.S1_MAY_THE_PROTECTION_OF_THE_GODS_BE_UPON_YOU, new String[]{creature.getName()});
      }

      return super.onSeeCreature(npc, creature, isSummon);
   }

   @Override
   public final String onSpawn(Npc npc) {
      this.startQuestTimer("TIMER_MOVING", 60000L, npc, null);
      this.startQuestTimer("TIMER_BLIZZARD", 180000L, npc, null);
      return super.onSpawn(npc);
   }

   @Override
   public String onSpellFinished(Npc npc, Player player, Skill skill) {
      ReflectionWorld tmpworld = ReflectionManager.getInstance().getWorld(npc.getReflectionId());
      if (tmpworld != null && tmpworld instanceof FreyaFirstBattle.IQCWorld) {
         FreyaFirstBattle.IQCWorld world = (FreyaFirstBattle.IQCWorld)tmpworld;
         if (skill == new SkillHolder(6276, 1).getSkill() && world.player != null) {
            this.startQuestTimer("TIMER_SCENE_21", 1000L, npc, world.player);
         }
      }

      return super.onSpellFinished(npc, player, skill);
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      this.enterInstance(player, npc);
      return super.onTalk(npc, player);
   }

   public static void main(String[] args) {
      new FreyaFirstBattle();
   }

   protected class IQCWorld extends ReflectionWorld {
      Player player = null;
   }
}
