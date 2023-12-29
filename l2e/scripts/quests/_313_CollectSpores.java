package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _313_CollectSpores extends Quest {
   private static final String qn = "_313_CollectSpores";
   private static final int Herbiel = 30150;
   private static final int SporeSac = 1118;

   public _313_CollectSpores(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30150);
      this.addTalkId(30150);
      this.addKillId(20509);
      this.questItemIds = new int[]{1118};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_313_CollectSpores");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30150-05.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_313_CollectSpores");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 8 && player.getLevel() <= 13) {
                  htmltext = "30150-03.htm";
               } else {
                  htmltext = "30150-02.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (cond == 1) {
                  htmltext = "30150-06.htm";
               } else if (cond == 2) {
                  if (st.getQuestItemsCount(1118) < 10L) {
                     st.set("cond", "1");
                     htmltext = "30150-06.htm";
                  } else {
                     htmltext = "30150-07.htm";
                     st.takeItems(1118, -1L);
                     st.rewardItems(57, 3500L);
                     st.playSound("ItemSound.quest_finish");
                     st.exitQuest(true);
                  }
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_313_CollectSpores");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1 && st.dropQuestItems(1118, 1, 10L, 700000, true)) {
            st.set("cond", "2");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _313_CollectSpores(313, "_313_CollectSpores", "");
   }
}
