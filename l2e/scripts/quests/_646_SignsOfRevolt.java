package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _646_SignsOfRevolt extends Quest {
   private static final String qn = "_646_SignsOfRevolt";

   public _646_SignsOfRevolt(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32016);
      this.addTalkId(32016);
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_646_SignsOfRevolt");
      if (st == null) {
         return htmltext;
      } else {
         if (npc.getId() == 32016) {
            st.exitQuest(true);
         }

         return "32016-00.htm";
      }
   }

   public static void main(String[] args) {
      new _646_SignsOfRevolt(646, "_646_SignsOfRevolt", "");
   }
}
