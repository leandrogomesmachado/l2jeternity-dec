package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;

public class _261_CollectorsDream extends Quest {
   private static final String qn = "_261_CollectorsDream";
   private static final int ALSHUPES = 30222;
   private static final int GIANT_SPIDER_LEG = 1087;
   private static final int ADENA = 57;

   public _261_CollectorsDream(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30222);
      this.addTalkId(30222);
      this.addKillId(new int[]{20308, 20460, 20466});
      this.questItemIds = new int[]{1087};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_261_CollectorsDream");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30222-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_261_CollectorsDream");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 15 && player.getLevel() <= 21) {
                  htmltext = "30222-02.htm";
               } else {
                  htmltext = "30222-01.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (st.getInt("cond") == 2) {
                  htmltext = "30222-05.htm";
                  st.takeItems(1087, -1L);
                  st.rewardItems(57, 1000L);
                  st.addExpAndSp(2000, 0);
                  st.exitQuest(true);
                  st.playSound("ItemSound.quest_finish");
                  showOnScreenMsg(player, NpcStringId.LAST_DUTY_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE, 2, 5000, new String[0]);
               } else {
                  htmltext = "30222-04.htm";
               }
               break;
            case 2:
               htmltext = Quest.getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_261_CollectorsDream");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1 && st.getQuestItemsCount(1087) < 8L) {
            st.giveItems(1087, 1L);
            if (st.getQuestItemsCount(1087) == 8L) {
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
      new _261_CollectorsDream(261, "_261_CollectorsDream", "");
   }
}
