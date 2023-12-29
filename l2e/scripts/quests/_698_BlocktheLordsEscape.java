package l2e.scripts.quests;

import l2e.gameserver.Config;
import l2e.gameserver.instancemanager.SoIManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _698_BlocktheLordsEscape extends Quest {
   private static final String qn = "_698_BlocktheLordsEscape";
   private static final int TEPIOS = 32603;
   private static final int VESPER_STONE = 14052;

   public _698_BlocktheLordsEscape(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32603);
      this.addTalkId(32603);
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_698_BlocktheLordsEscape");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32603-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         }

         return event;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_698_BlocktheLordsEscape");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (player.getLevel() < 75 || player.getLevel() > 85) {
                  htmltext = "32603-00.htm";
                  st.exitQuest(true);
               }

               if (SoIManager.getCurrentStage() != 5) {
                  htmltext = "32603-00a.htm";
                  st.exitQuest(true);
               }

               htmltext = "32603-01.htm";
               break;
            case 1:
               if (cond == 1 && st.getInt("defenceDone") == 1) {
                  htmltext = "32603-05.htm";
                  st.giveItems(14052, (long)((int)Config.RATE_QUEST_REWARD * getRandom(5, 8)));
                  st.playSound("ItemSound.quest_finish");
                  st.exitQuest(true);
               } else {
                  htmltext = "32603-04.htm";
               }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _698_BlocktheLordsEscape(698, "_698_BlocktheLordsEscape", "");
   }
}
