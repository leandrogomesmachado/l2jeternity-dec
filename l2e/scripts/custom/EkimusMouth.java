package l2e.scripts.custom;

import l2e.gameserver.instancemanager.SoIManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class EkimusMouth extends Quest {
   public EkimusMouth(int id, String name, String desc) {
      super(id, name, desc);
      this.addStartNpc(32537);
      this.addFirstTalkId(32537);
      this.addTalkId(32537);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (event.equalsIgnoreCase("hos_enter")) {
         if (SoIManager.getCurrentStage() == 1) {
            htmltext = "32537-1.htm";
         } else if (SoIManager.getCurrentStage() == 4) {
            htmltext = "32537-2.htm";
         }
      } else if (event.equalsIgnoreCase("hoe_enter")) {
         if (SoIManager.getCurrentStage() == 1) {
            htmltext = "32537-3.htm";
         } else if (SoIManager.getCurrentStage() == 4) {
            htmltext = "32537-4.htm";
         }
      }

      return htmltext;
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      return npc.getId() == 32537 ? "32537.htm" : "";
   }

   public static void main(String[] args) {
      new EkimusMouth(-1, EkimusMouth.class.getSimpleName(), "custom");
   }
}
