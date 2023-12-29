package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _165_ShilensHunt extends Quest {
   private static final String qn = "_165_ShilensHunt";
   private static final int NELSYA = 30348;
   private static final int DARK_BEZOAR = 1160;
   private static final int LESSER_HEALING_POTION = 1060;

   public _165_ShilensHunt(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30348);
      this.addTalkId(30348);
      this.addKillId(new int[]{20456, 20529, 20532, 20536});
      this.questItemIds = new int[]{1160};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_165_ShilensHunt");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30348-03.htm")) {
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
      QuestState st = player.getQuestState("_165_ShilensHunt");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getRace().ordinal() == 2) {
                  if (player.getLevel() >= 3 && player.getLevel() <= 7) {
                     htmltext = "30348-02.htm";
                  } else {
                     htmltext = "30348-01.htm";
                     st.exitQuest(true);
                  }
               } else {
                  htmltext = "30348-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (st.getQuestItemsCount(1160) >= 13L) {
                  htmltext = "30348-05.htm";
                  st.takeItems(1160, -1L);
                  st.rewardItems(1060, 5L);
                  st.addExpAndSp(1000, 0);
                  st.exitQuest(false);
                  st.playSound("ItemSound.quest_finish");
               } else {
                  htmltext = "30348-04.htm";
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
      QuestState st = player.getQuestState("_165_ShilensHunt");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1 && st.getRandom(10) < 2) {
            st.giveItems(1160, 1L);
            if (st.getQuestItemsCount(1160) == 13L) {
               st.set("cond", "2");
               st.playSound("ItemSound.quest_middle");
            } else {
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _165_ShilensHunt(165, "_165_ShilensHunt", "");
   }
}
