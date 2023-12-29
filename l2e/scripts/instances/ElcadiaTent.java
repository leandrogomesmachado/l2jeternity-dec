package l2e.scripts.instances;

import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.instancemanager.ReflectionManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.reflection.ReflectionTemplate;
import l2e.gameserver.model.actor.templates.reflection.ReflectionWorld;
import l2e.gameserver.model.quest.QuestState;

public class ElcadiaTent extends AbstractReflection {
   public ElcadiaTent(String name, String descr) {
      super(name, descr);
      this.addStartNpc(32862);
      this.addTalkId(new int[]{32862, 32784});
   }

   private final synchronized void enterInstance(Player player, Npc npc) {
      this.enterInstance(player, npc, new ElcadiaTent.ETentWorld(), 158);
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
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (npc.getId() == 32862) {
         if (player.getQuestState("_10292_SevenSignsGirlofDoubt") != null && player.getQuestState("_10292_SevenSignsGirlofDoubt").getState() == 1) {
            this.enterInstance(player, npc);
            return null;
         }

         if (player.getQuestState("_10292_SevenSignsGirlofDoubt") != null
            && player.getQuestState("_10292_SevenSignsGirlofDoubt").getState() == 2
            && player.getQuestState("_10293_SevenSignsForbiddenBook") == null) {
            this.enterInstance(player, npc);
            return null;
         }

         if (player.getQuestState("_10293_SevenSignsForbiddenBook") != null && player.getQuestState("_10293_SevenSignsForbiddenBook").getState() != 2) {
            this.enterInstance(player, npc);
            return null;
         }

         if (player.getQuestState("_10293_SevenSignsForbiddenBook") != null
            && player.getQuestState("_10293_SevenSignsForbiddenBook").getState() == 2
            && player.getQuestState("_10294_SevenSignsToTheMonasteryOfSilence") == null) {
            this.enterInstance(player, npc);
            return null;
         }

         if (player.getQuestState("_10296_SevenSignsPoweroftheSeal") != null && player.getQuestState("_10296_SevenSignsPoweroftheSeal").getInt("cond") == 3) {
            this.enterInstance(player, npc);
            return null;
         }

         htmltext = "32862.htm";
      } else if (npc.getId() == 32784) {
         ReflectionWorld world = ReflectionManager.getInstance().getPlayerWorld(player);
         if (world != null) {
            world.removeAllowed(player.getObjectId());
         }

         player.setReflectionId(0);
         player.teleToLocation(43316, -87986, -2832, false);
         return null;
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new ElcadiaTent(ElcadiaTent.class.getSimpleName(), "instances");
   }

   private class ETentWorld extends ReflectionWorld {
      public ETentWorld() {
      }
   }
}
