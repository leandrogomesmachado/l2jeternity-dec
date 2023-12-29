package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _161_FruitOfTheMotherTree extends Quest {
   private static final String qn = "_161_FruitOfTheMotherTree";
   private static final int ANDELLIA = 30362;
   private static final int THALIA = 30371;
   private static final int ANDELLIA_LETTER = 1036;
   private static final int MOTHERTREE_FRUIT = 1037;

   public _161_FruitOfTheMotherTree(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30362);
      this.addTalkId(30362);
      this.addTalkId(30371);
      this.questItemIds = new int[]{1036, 1037};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_161_FruitOfTheMotherTree");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30362-04.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.giveItems(1036, 1L);
            st.playSound("ItemSound.quest_accept");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_161_FruitOfTheMotherTree");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (player.getRace().ordinal() == 1) {
                  if (player.getLevel() >= 3 && player.getLevel() <= 7) {
                     htmltext = "30362-03.htm";
                  } else {
                     htmltext = "30362-02.htm";
                     st.exitQuest(true);
                  }
               } else {
                  htmltext = "30362-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 30362:
                     if (cond == 1) {
                        htmltext = "30362-05.htm";
                     } else if (st.getQuestItemsCount(1037) == 1L) {
                        htmltext = "30362-06.htm";
                        st.takeItems(1037, 1L);
                        st.rewardItems(57, 1000L);
                        st.addExpAndSp(1000, 0);
                        st.unset("cond");
                        st.exitQuest(false);
                        st.playSound("ItemSound.quest_finish");
                        return htmltext;
                     }

                     return htmltext;
                  case 30371:
                     if (cond == 1 && st.getQuestItemsCount(1036) == 1L) {
                        htmltext = "30371-01.htm";
                        st.takeItems(1036, 1L);
                        st.giveItems(1037, 1L);
                        st.set("cond", "2");
                        st.playSound("ItemSound.quest_middle");
                     } else if (cond == 2 && st.getQuestItemsCount(1037) == 1L) {
                        htmltext = "30371-02.htm";
                        return htmltext;
                     }

                     return htmltext;
                  default:
                     return htmltext;
               }
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _161_FruitOfTheMotherTree(161, "_161_FruitOfTheMotherTree", "");
   }
}
