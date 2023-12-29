package l2e.scripts.custom;

import l2e.gameserver.instancemanager.SoIManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class AbyssGaze extends Quest {
   public AbyssGaze(int id, String name, String desc) {
      super(id, name, desc);
      this.addStartNpc(32540);
      this.addFirstTalkId(32540);
      this.addTalkId(32540);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (event.equalsIgnoreCase("request_permission")) {
         if (SoIManager.getCurrentStage() == 2 || SoIManager.getCurrentStage() == 5) {
            htmltext = "32540-2.htm";
         } else if (SoIManager.getCurrentStage() == 3 && SoIManager.isSeedOpen()) {
            htmltext = "32540-3.htm";
         } else {
            htmltext = "32540-1.htm";
         }
      } else if (event.equalsIgnoreCase("enter_seed") && SoIManager.getCurrentStage() == 3) {
         SoIManager.teleportInSeed(player);
         return null;
      }

      return htmltext;
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      return npc.getId() == 32540 ? "32540.htm" : "";
   }

   public static void main(String[] args) {
      new AbyssGaze(-1, AbyssGaze.class.getSimpleName(), "custom");
   }
}
