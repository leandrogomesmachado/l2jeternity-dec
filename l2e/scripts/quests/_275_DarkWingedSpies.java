package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _275_DarkWingedSpies extends Quest {
   private static final String qn = "_275_DarkWingedSpies";
   private static final int TANTUS = 30567;
   private static final int DARKWING_BAT = 20316;
   private static final int VARANGKA_TRACKER = 27043;
   private static final int DARKWING_BAT_FANG = 1478;
   private static final int VARANGKAS_PARASITE = 1479;

   public _275_DarkWingedSpies(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30567);
      this.addTalkId(30567);
      this.addKillId(new int[]{20316, 27043});
      this.questItemIds = new int[]{1478, 1479};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_275_DarkWingedSpies");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30567-03.htm")) {
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
      QuestState st = player.getQuestState("_275_DarkWingedSpies");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getRace().ordinal() == 3) {
                  if (player.getLevel() >= 11 && player.getLevel() <= 15) {
                     htmltext = "30567-02.htm";
                  } else {
                     htmltext = "30567-01.htm";
                     st.exitQuest(true);
                  }
               } else {
                  htmltext = "30567-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (st.getQuestItemsCount(1478) < 70L) {
                  htmltext = "30567-04.htm";
               } else {
                  htmltext = "30567-05.htm";
                  st.takeItems(1478, -1L);
                  st.takeItems(1479, -1L);
                  st.rewardItems(57, 4550L);
                  st.playSound("ItemSound.quest_finish");
                  st.exitQuest(true);
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_275_DarkWingedSpies");
      if (st == null) {
         return null;
      } else {
         if (st.isStarted()) {
            switch(npc.getId()) {
               case 20316:
                  if (st.getQuestItemsCount(1478) < 70L) {
                     st.giveItems(1478, 1L);
                     if (st.getQuestItemsCount(1478) < 69L) {
                        st.playSound("ItemSound.quest_itemget");
                     } else {
                        st.playSound("ItemSound.quest_middle");
                        st.set("cond", "2");
                     }

                     if (st.getQuestItemsCount(1478) < 66L && st.getRandom(100) < 10) {
                        st.addSpawn(27043, npc);
                        st.giveItems(1479, 1L);
                     }
                  }
                  break;
               case 27043:
                  if (st.getQuestItemsCount(1478) < 66L && st.getQuestItemsCount(1479) == 1L) {
                     st.takeItems(1479, -1L);
                     st.giveItems(1478, 5L);
                     if (st.getQuestItemsCount(1478) < 65L) {
                        st.playSound("ItemSound.quest_itemget");
                     } else {
                        st.playSound("ItemSound.quest_middle");
                        st.set("cond", "2");
                     }
                  }
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _275_DarkWingedSpies(275, "_275_DarkWingedSpies", "");
   }
}
