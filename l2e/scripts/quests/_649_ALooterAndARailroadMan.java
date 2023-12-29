package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _649_ALooterAndARailroadMan extends Quest {
   private static final String qn = "_649_ALooterAndARailroadMan";
   private static final int THIEF_GUILD_MARK = 8099;
   private static final int OBI = 32052;

   public _649_ALooterAndARailroadMan(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32052);
      this.addTalkId(32052);
      this.addKillId(new int[]{22017, 22018, 22019, 22021, 22022, 22023, 22024, 22026});
      this.questItemIds = new int[]{8099};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_649_ALooterAndARailroadMan");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32052-1.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("32052-3.htm")) {
            if (st.getQuestItemsCount(8099) < 200L) {
               htmltext = "32052-3a.htm";
            } else {
               st.takeItems(8099, -1L);
               st.rewardItems(57, 21698L);
               st.playSound("ItemSound.quest_finish");
               st.exitQuest(true);
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_649_ALooterAndARailroadMan");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 30) {
                  htmltext = "32052-0.htm";
               } else {
                  htmltext = "32052-0a.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (st.getQuestItemsCount(8099) == 200L) {
                  htmltext = "32052-2.htm";
               } else {
                  htmltext = "32052-2a.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_649_ALooterAndARailroadMan");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1 && st.getRandom(10) < 8) {
            st.giveItems(8099, 1L);
            if (st.getQuestItemsCount(8099) == 200L) {
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
      new _649_ALooterAndARailroadMan(649, "_649_ALooterAndARailroadMan", "");
   }
}
