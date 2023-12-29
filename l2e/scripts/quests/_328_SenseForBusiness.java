package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _328_SenseForBusiness extends Quest {
   private static final String qn = "_328_SenseForBusiness";
   private static final int SARIEN = 30436;
   private static final int MONSTER_EYE_LENS = 1366;
   private static final int MONSTER_EYE_CARCASS = 1347;
   private static final int BASILISK_GIZZARD = 1348;

   public _328_SenseForBusiness(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30436);
      this.addTalkId(30436);
      this.addKillId(new int[]{20055, 20059, 20067, 20068, 20070, 20072});
      this.questItemIds = new int[]{1366, 1347, 1348};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_328_SenseForBusiness");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30436-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if ("30436-06.htm".equals(event)) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_328_SenseForBusiness");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 21 && player.getLevel() <= 32) {
                  htmltext = "30436-02.htm";
               } else {
                  htmltext = "30436-01.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               long carcasses = st.getQuestItemsCount(1347);
               long lenses = st.getQuestItemsCount(1366);
               long gizzards = st.getQuestItemsCount(1348);
               long all = carcasses + lenses + gizzards;
               if (all == 0L) {
                  htmltext = "30436-04.htm";
               } else {
                  htmltext = "30436-05.htm";
                  long reward = 25L * carcasses + 1000L * lenses + 60L * gizzards + (long)(all >= 10L ? 618 : 0);
                  st.takeItems(1347, -1L);
                  st.takeItems(1366, -1L);
                  st.takeItems(1348, -1L);
                  st.rewardItems(57, reward);
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_328_SenseForBusiness");
      if (st == null) {
         return null;
      } else {
         if (st.isStarted()) {
            switch(npc.getId()) {
               case 20055:
               case 20059:
               case 20067:
               case 20068:
                  if (st.getRandom(100) < 2) {
                     st.giveItems(1366, 1L);
                     st.playSound("ItemSound.quest_itemget");
                  } else if (st.getRandom(100) < 33) {
                     st.giveItems(1347, 1L);
                     st.playSound("ItemSound.quest_itemget");
                  }
               case 20056:
               case 20057:
               case 20058:
               case 20060:
               case 20061:
               case 20062:
               case 20063:
               case 20064:
               case 20065:
               case 20066:
               case 20069:
               case 20071:
               default:
                  break;
               case 20070:
               case 20072:
                  if (st.getRandom(100) < 18) {
                     st.giveItems(1348, 1L);
                     st.playSound("ItemSound.quest_itemget");
                  }
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _328_SenseForBusiness(328, "_328_SenseForBusiness", "");
   }
}
