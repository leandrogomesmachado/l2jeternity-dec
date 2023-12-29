package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _297_GatekeepersFavor extends Quest {
   private static final String qn = "_297_GatekeepersFavor";
   private static final int WIRPHY = 30540;
   private static final int STARSTONE = 1573;
   private static final int GATEKEEPER_TOKEN = 1659;
   private static final int WHINSTONE_GOLEM = 20521;

   public _297_GatekeepersFavor(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30540);
      this.addTalkId(30540);
      this.addKillId(20521);
      this.questItemIds = new int[]{1573};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_297_GatekeepersFavor");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30540-03.htm")) {
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
      QuestState st = player.getQuestState("_297_GatekeepersFavor");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 15 && player.getLevel() <= 21) {
                  htmltext = "30540-02.htm";
               } else {
                  htmltext = "30540-01.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (cond == 1) {
                  htmltext = "30540-04.htm";
               } else if (cond == 2) {
                  if (st.getQuestItemsCount(1573) == 20L) {
                     htmltext = "30540-05.htm";
                     st.takeItems(1573, 20L);
                     st.rewardItems(1659, 2L);
                     st.playSound("ItemSound.quest_finish");
                     st.exitQuest(true);
                  } else {
                     htmltext = "30540-04.htm";
                  }
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
      QuestState st = player.getQuestState("_297_GatekeepersFavor");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1 && st.getRandom(10) < 5) {
            st.giveItems(1573, 1L);
            if (st.getQuestItemsCount(1573) == 20L) {
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
      new _297_GatekeepersFavor(297, "_297_GatekeepersFavor", "");
   }
}
