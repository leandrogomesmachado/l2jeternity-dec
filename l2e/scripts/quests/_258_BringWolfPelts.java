package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _258_BringWolfPelts extends Quest {
   private static final String qn = "_258_BringWolfPelts";
   private static final int LECTOR = 30001;
   private static final int WOLF = 20120;
   private static final int ELDER_WOLF = 20442;
   private static final int WOLF_PELT = 702;
   private static final int Cotton_Shirt = 390;
   private static final int Leather_Pants = 29;
   private static final int Leather_Shirt = 22;
   private static final int Short_Leather_Gloves = 1119;
   private static final int Tunic = 426;

   public _258_BringWolfPelts(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30001);
      this.addTalkId(30001);
      this.addKillId(new int[]{20120, 20442});
      this.questItemIds = new int[]{702};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_258_BringWolfPelts");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30001-03.htm")) {
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
      QuestState st = player.getQuestState("_258_BringWolfPelts");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 3 && player.getLevel() <= 9) {
                  htmltext = "30001-02.htm";
               } else {
                  htmltext = "30001-01.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (st.getQuestItemsCount(702) < 40L) {
                  htmltext = "30001-05.htm";
               } else {
                  st.takeItems(702, 40L);
                  int randomNumber = st.getRandom(16);
                  if (randomNumber == 0) {
                     st.giveItems(390, 1L);
                  } else if (randomNumber < 6) {
                     st.giveItems(29, 1L);
                  } else if (randomNumber < 9) {
                     st.giveItems(22, 1L);
                  } else if (randomNumber < 13) {
                     st.giveItems(1119, 1L);
                  } else {
                     st.giveItems(426, 1L);
                  }

                  htmltext = "30001-06.htm";
                  if (randomNumber == 0) {
                     st.playSound("ItemSound.quest_jackpot");
                  } else {
                     st.playSound("ItemSound.quest_finish");
                  }

                  st.exitQuest(true);
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_258_BringWolfPelts");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1 && st.getQuestItemsCount(702) < 40L) {
            st.giveItems(702, 1L);
            if (st.getQuestItemsCount(702) == 40L) {
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "2");
            } else {
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _258_BringWolfPelts(258, "_258_BringWolfPelts", "");
   }
}
