package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _295_DreamingOfTheSkies extends Quest {
   private static final String qn = "_295_DreamingOfTheSkies";
   private static final int ARIN = 30536;
   private static final int FLOATING_STONE = 1492;
   private static final int RING_OF_FIREFLY = 1509;
   private static final int MAGICAL_WEAVER = 20153;

   public _295_DreamingOfTheSkies(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30536);
      this.addTalkId(30536);
      this.addKillId(20153);
      this.questItemIds = new int[]{1492};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_295_DreamingOfTheSkies");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30536-03.htm")) {
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
      QuestState st = player.getQuestState("_295_DreamingOfTheSkies");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 11 && player.getLevel() <= 15) {
                  htmltext = "30536-02.htm";
               } else {
                  htmltext = "30536-01.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (st.getQuestItemsCount(1492) < 50L) {
                  htmltext = "30536-04.htm";
               } else if (st.getQuestItemsCount(1509) == 0L) {
                  htmltext = "30536-05.htm";
                  st.takeItems(1492, -1L);
                  st.giveItems(1509, 1L);
                  st.addExpAndSp(0, 500);
                  st.playSound("ItemSound.quest_finish");
                  st.exitQuest(true);
               } else {
                  htmltext = "30536-06.htm";
                  st.takeItems(1492, -1L);
                  st.rewardItems(57, 2400L);
                  st.addExpAndSp(0, 500);
                  st.playSound("ItemSound.quest_finish");
                  st.exitQuest(true);
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_295_DreamingOfTheSkies");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1 && st.getRandom(100) < 25) {
            int itemNumber = 1 + st.getRandom(2);
            if (st.getQuestItemsCount(1492) + (long)itemNumber > 50L) {
               itemNumber = (int)(50L - st.getQuestItemsCount(1492));
            }

            st.giveItems(1492, (long)itemNumber);
            if (st.getQuestItemsCount(1492) < 50L) {
               st.playSound("ItemSound.quest_itemget");
            } else {
               st.set("cond", "2");
               st.playSound("ItemSound.quest_middle");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _295_DreamingOfTheSkies(295, "_295_DreamingOfTheSkies", "");
   }
}
