package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _319_ScentOfDeath extends Quest {
   private static final String qn = "_319_ScentOfDeath";
   private static final int MINALESS = 30138;
   private static final int ZOMBIE_SKIN = 1045;

   public _319_ScentOfDeath(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30138);
      this.addTalkId(30138);
      this.addKillId(new int[]{20015, 20020});
      this.questItemIds = new int[]{1045};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_319_ScentOfDeath");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30138-04.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_319_ScentOfDeath");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 11 && player.getLevel() <= 18) {
                  htmltext = "30138-03.htm";
               } else {
                  htmltext = "30138-02.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (st.getQuestItemsCount(1045) == 5L) {
                  htmltext = "30138-06.htm";
                  st.takeItems(1045, 5L);
                  st.rewardItems(57, 3350L);
                  st.rewardItems(1060, 1L);
                  st.playSound("ItemSound.quest_finish");
                  st.exitQuest(true);
               } else {
                  htmltext = "30138-05.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_319_ScentOfDeath");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1 && st.dropQuestItems(1045, 1, 5L, 300000, true)) {
            st.set("cond", "2");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _319_ScentOfDeath(319, "_319_ScentOfDeath", "");
   }
}
