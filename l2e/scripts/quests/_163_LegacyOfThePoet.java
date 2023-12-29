package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _163_LegacyOfThePoet extends Quest {
   private static final String qn = "_163_LegacyOfThePoet";
   private static final int STARDEN = 30220;
   private static final int RUMIELS_POEM_1 = 1038;
   private static final int RUMIELS_POEM_2 = 1039;
   private static final int RUMIELS_POEM_3 = 1040;
   private static final int RUMIELS_POEM_4 = 1041;

   public _163_LegacyOfThePoet(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30220);
      this.addTalkId(30220);
      this.addKillId(new int[]{20372, 20373});
      this.questItemIds = new int[]{1038, 1039, 1040, 1041};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_163_LegacyOfThePoet");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30220-07.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_163_LegacyOfThePoet");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getRace().ordinal() == 2) {
                  htmltext = "30220-00.htm";
                  st.exitQuest(true);
               } else if (player.getLevel() >= 11 && player.getLevel() <= 15) {
                  htmltext = "30220-03.htm";
               } else {
                  htmltext = "30220-02.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (st.getQuestItemsCount(1038) == 1L
                  && st.getQuestItemsCount(1039) == 1L
                  && st.getQuestItemsCount(1040) == 1L
                  && st.getQuestItemsCount(1041) == 1L) {
                  htmltext = "30220-09.htm";
                  st.takeItems(1038, 1L);
                  st.takeItems(1039, 1L);
                  st.takeItems(1040, 1L);
                  st.takeItems(1041, 1L);
                  st.rewardItems(57, 13890L);
                  st.addExpAndSp(21643, 943);
                  st.exitQuest(false);
                  st.playSound("ItemSound.quest_finish");
               } else {
                  htmltext = "30220-08.htm";
               }
               break;
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_163_LegacyOfThePoet");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1) {
            if (st.getRandom(10) == 0 && st.getQuestItemsCount(1038) == 0L) {
               st.giveItems(1038, 1L);
               st.playSound("ItemSound.quest_itemget");
            } else if (st.getRandom(10) > 7 && st.getQuestItemsCount(1039) == 0L) {
               st.giveItems(1039, 1L);
               st.playSound("ItemSound.quest_itemget");
            } else if (st.getRandom(10) > 7 && st.getQuestItemsCount(1040) == 0L) {
               st.giveItems(1040, 1L);
               st.playSound("ItemSound.quest_itemget");
            } else if (st.getRandom(10) > 5 && st.getQuestItemsCount(1041) == 0L) {
               st.giveItems(1041, 1L);
               st.playSound("ItemSound.quest_itemget");
            }

            if (st.getQuestItemsCount(1038) + st.getQuestItemsCount(1039) + st.getQuestItemsCount(1040) + st.getQuestItemsCount(1041) == 4L) {
               st.set("cond", "2");
               st.playSound("ItemSound.quest_middle");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _163_LegacyOfThePoet(163, "_163_LegacyOfThePoet", "");
   }
}
