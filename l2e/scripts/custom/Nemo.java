package l2e.scripts.custom;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class Nemo extends Quest {
   private static final String qn = "Nemo";
   private static final int _nemo = 32735;

   public Nemo(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32735);
      this.addFirstTalkId(32735);
      this.addTalkId(32735);
   }

   @Deprecated
   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("Nemo");
      if (st == null) {
         st = this.newQuestState(player);
      }

      if (event.equalsIgnoreCase("request_collector")) {
         if (st.getQuestItemsCount(15487) <= 0L) {
            player.addItem("Maguen", 15487, 1L, null, true);
            return null;
         }

         htmltext = "32735-2.htm";
      }

      return htmltext;
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("Nemo");
      if (st == null) {
         st = this.newQuestState(player);
      }

      return npc.getId() == 32735 ? "32735.htm" : "";
   }

   public static void main(String[] args) {
      new Nemo(-1, "Nemo", "custom");
   }
}
