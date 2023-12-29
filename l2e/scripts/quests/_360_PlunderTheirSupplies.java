package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _360_PlunderTheirSupplies extends Quest {
   private static final String qn = "_360_PlunderTheirSupplies";
   private static final int COLEMAN = 30873;
   private static final int SUPPLY_ITEM = 5872;
   private static final int SUSPICIOUS_DOCUMENT = 5871;
   private static final int RECIPE_OF_SUPPLY = 5870;

   public _360_PlunderTheirSupplies(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30873);
      this.addTalkId(30873);
      this.addKillId(new int[]{20666, 20669});
      this.questItemIds = new int[]{5870, 5872, 5871};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_360_PlunderTheirSupplies");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30873-2.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30873-6.htm")) {
            st.takeItems(5872, -1L);
            st.takeItems(5871, -1L);
            st.takeItems(5870, -1L);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = Quest.getNoQuestMsg(player);
      QuestState st = player.getQuestState("_360_PlunderTheirSupplies");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 52 && player.getLevel() <= 59) {
                  htmltext = "30873-0.htm";
               } else {
                  htmltext = "30873-0a.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (st.getQuestItemsCount(5872) == 0L) {
                  htmltext = "30873-3.htm";
               } else {
                  htmltext = "30873-5.htm";
                  long reward = 6000L + st.getQuestItemsCount(5872) * 100L + st.getQuestItemsCount(5870) * 6000L;
                  st.takeItems(5872, -1L);
                  st.takeItems(5870, -1L);
                  st.rewardItems(57, reward);
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_360_PlunderTheirSupplies");
      if (st == null) {
         return null;
      } else {
         int chance = st.getRandom(10);
         if (chance == 9) {
            st.giveItems(5871, 1L);
            if (st.getQuestItemsCount(5871) == 5L) {
               st.takeItems(5871, 5L);
               st.giveItems(5870, 1L);
               st.playSound("ItemSound.quest_middle");
            } else {
               st.playSound("ItemSound.quest_itemget");
            }
         } else if (chance < 6) {
            st.giveItems(5872, 1L);
            st.playSound("ItemSound.quest_itemget");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _360_PlunderTheirSupplies(360, "_360_PlunderTheirSupplies", "");
   }
}
