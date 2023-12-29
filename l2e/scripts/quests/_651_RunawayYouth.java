package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _651_RunawayYouth extends Quest {
   private static String qn = "_651_RunawayYouth";
   private static int IVAN = 32014;
   private static int BATIDAE = 31989;
   protected Npc _npc;
   private static int SOE = 736;

   public _651_RunawayYouth(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(IVAN);
      this.addTalkId(BATIDAE);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(qn);
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32014-03.htm")) {
            if (st.getQuestItemsCount(SOE) > 0L) {
               st.set("cond", "1");
               st.setState((byte)1);
               st.playSound("ItemSound.quest_accept");
               st.takeItems(SOE, 1L);
               npc.deleteMe();
            } else {
               htmltext = "32014-04.htm";
            }
         } else if (event.equalsIgnoreCase("32014-04a.htm")) {
            st.exitQuest(true);
            st.playSound("ItemSound.quest_giveup");
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(qn);
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int id = st.getState();
         int cond = st.getInt("cond");
         if (id == 0) {
            if (npcId == IVAN && cond == 0) {
               if (player.getLevel() >= 26) {
                  htmltext = "32014-02.htm";
               } else {
                  htmltext = "32014-01.htm";
                  st.exitQuest(true);
               }
            }
         } else if (id == 1 && npcId == BATIDAE && cond == 1) {
            htmltext = "31989-01.htm";
            st.giveItems(57, 2883L);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _651_RunawayYouth(651, qn, "");
   }
}
