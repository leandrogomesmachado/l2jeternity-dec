package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _356_DigUpTheSeaOfSpores extends Quest {
   private static final String qn = "_356_DigUpTheSeaOfSpores";
   private static final int HERB_SPORE = 5866;
   private static final int CARN_SPORE = 5865;
   private static final int GAUEN = 30717;
   private static final int ROTTING_TREE = 20558;
   private static final int SPORE_ZOMBIE = 20562;

   public _356_DigUpTheSeaOfSpores(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30717);
      this.addTalkId(30717);
      this.addKillId(new int[]{20558, 20562});
      this.questItemIds = new int[]{5866, 5865};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_356_DigUpTheSeaOfSpores");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30717-06.htm")) {
            st.setState((byte)1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30717-17.htm")) {
            st.takeItems(5866, 50L);
            st.takeItems(5865, 50L);
            st.rewardItems(57, 20950L);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         } else if (event.equalsIgnoreCase("30717-14.htm")) {
            st.takeItems(5866, 50L);
            st.takeItems(5865, 50L);
            st.addExpAndSp(35000, 2600);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         } else if (event.equalsIgnoreCase("30717-12.htm")) {
            st.takeItems(5866, 50L);
            st.addExpAndSp(24500, 0);
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30717-13.htm")) {
            st.takeItems(5865, 50L);
            st.addExpAndSp(0, 1820);
            st.playSound("ItemSound.quest_middle");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_356_DigUpTheSeaOfSpores");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 43 && player.getLevel() <= 51) {
                  htmltext = "30717-02.htm";
               } else {
                  htmltext = "30717-01.htm";
               }
               break;
            case 1:
               if (cond == 1) {
                  htmltext = "30717-07.htm";
               } else if (cond == 2) {
                  if (st.getQuestItemsCount(5866) >= 50L) {
                     htmltext = "30717-08.htm";
                  } else if (st.getQuestItemsCount(5865) >= 50L) {
                     htmltext = "30717-09.htm";
                  } else {
                     htmltext = "30717-07.htm";
                  }
               } else if (cond == 3) {
                  htmltext = "30717-10.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_356_DigUpTheSeaOfSpores");
      if (st == null) {
         return null;
      } else {
         int cond = st.getInt("cond");
         if (st.isStarted() && cond < 3 && st.getRandom(10) < 5) {
            boolean isItemRewarded = false;
            switch(npc.getId()) {
               case 20558:
                  if (st.getQuestItemsCount(5866) < 50L) {
                     st.giveItems(5866, 1L);
                     isItemRewarded = true;
                  }
                  break;
               case 20562:
                  if (st.getQuestItemsCount(5865) < 50L) {
                     st.giveItems(5865, 1L);
                     isItemRewarded = true;
                  }
            }

            if (isItemRewarded) {
               if (cond == 2 && st.getQuestItemsCount(5865) >= 50L && st.getQuestItemsCount(5866) >= 50L) {
                  st.set("cond", "3");
                  st.playSound("ItemSound.quest_middle");
               } else if (cond != 1 || st.getQuestItemsCount(5865) < 50L && st.getQuestItemsCount(5866) < 50L) {
                  st.playSound("ItemSound.quest_itemget");
               } else {
                  st.set("cond", "2");
                  st.playSound("ItemSound.quest_middle");
               }
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _356_DigUpTheSeaOfSpores(356, "_356_DigUpTheSeaOfSpores", "");
   }
}
