package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _652_AnAgedExAdventurer extends Quest {
   private static String qn = "_652_AnAgedExAdventurer";
   private static final int Tantan = 32012;
   private static final int Sara = 30180;
   private static final int SSC = 1464;
   private static final int EAD = 956;

   public _652_AnAgedExAdventurer(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(32012);
      this.addTalkId(30180);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(qn);
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32012-02.htm") && st.getQuestItemsCount(1464) >= 100L) {
            if (st.getQuestItemsCount(1464) >= 100L) {
               st.set("cond", "1");
               st.setState((byte)1);
               st.takeItems(1464, 100L);
               st.playSound("ItemSound.quest_accept");
               npc.deleteMe();
            } else {
               htmltext = "32012-02a.htm";
            }
         } else if (event.equalsIgnoreCase("32012-03.htm")) {
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
            if (npcId == 32012 & cond == 0) {
               if (player.getLevel() < 46) {
                  htmltext = "32012-00.htm";
                  st.exitQuest(true);
               } else {
                  htmltext = "32012-01.htm";
               }
            }
         } else if (id == 1 && npcId == 30180 && cond == 1) {
            htmltext = "30180-01.htm";
            st.giveItems(57, 10000L);
            if (getRandom(100) < 50) {
               st.giveItems(956, 1L);
            }

            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _652_AnAgedExAdventurer(652, qn, "");
   }
}
