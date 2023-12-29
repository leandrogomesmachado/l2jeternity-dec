package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _267_WrathOfVerdure extends Quest {
   private static final String qn = "_267_WrathOfVerdure";
   private static final int GOBLIN_CLUB = 1335;
   private static final int SILVERY_LEAF = 1340;
   private static final int TREANT_BREMEC = 31853;
   private static final int GOBLIN = 20325;

   public _267_WrathOfVerdure(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31853);
      this.addTalkId(31853);
      this.addKillId(20325);
      this.questItemIds = new int[]{1335};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_267_WrathOfVerdure");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31853-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("31853-06.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_267_WrathOfVerdure");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getRace().ordinal() == 1) {
                  if (player.getLevel() >= 4 && player.getLevel() <= 9) {
                     htmltext = "31853-02.htm";
                  } else {
                     htmltext = "31853-01.htm";
                     st.exitQuest(true);
                  }
               } else {
                  htmltext = "31853-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               int count = (int)st.getQuestItemsCount(1335);
               if (count > 0) {
                  htmltext = "31853-05.htm";
                  st.takeItems(1335, -1L);
                  st.rewardItems(1340, (long)count);
               } else {
                  htmltext = "31853-04.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_267_WrathOfVerdure");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1 && st.getRandom(10) < 5) {
            st.giveItems(1335, 1L);
            st.playSound("ItemSound.quest_itemget");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _267_WrathOfVerdure(267, "_267_WrathOfVerdure", "");
   }
}
