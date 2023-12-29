package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _110_ToThePrimevalIsle extends Quest {
   private static final String qn = "_110_ToThePrimevalIsle";
   private static final int ANTON = 31338;
   private static final int MARQUEZ = 32113;
   private static final int[] QUEST_ITEM = new int[]{8777};
   private static final int PLAYER_MIN_LVL = 75;

   public _110_ToThePrimevalIsle(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31338);
      this.addTalkId(31338);
      this.addTalkId(32113);
      this.questItemIds = QUEST_ITEM;
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_110_ToThePrimevalIsle");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("1")) {
            htmltext = "1.htm";
            st.set("cond", "1");
            st.giveItems(QUEST_ITEM[0], 1L);
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("2") && st.getQuestItemsCount(QUEST_ITEM[0]) >= 1L) {
            htmltext = "3.htm";
            st.playSound("ItemSound.quest_finish");
            st.takeItems(57, 169380L);
            st.addExpAndSp(251602, 25245);
            st.takeItems(QUEST_ITEM[0], -1L);
            st.exitQuest(false);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_110_ToThePrimevalIsle");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 75) {
                  htmltext = "0.htm";
               } else {
                  st.exitQuest(true);
                  htmltext = "00.htm";
               }
               break;
            case 1:
               if (npc.getId() == 32113 && st.getInt("cond") == 1) {
                  if (st.getQuestItemsCount(QUEST_ITEM[0]) == 0L) {
                     htmltext = "1a.htm";
                  } else {
                     htmltext = "2.htm";
                  }
               }
               break;
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _110_ToThePrimevalIsle(110, "_110_ToThePrimevalIsle", "");
   }
}
