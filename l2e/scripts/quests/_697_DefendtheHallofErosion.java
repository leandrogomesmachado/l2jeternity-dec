package l2e.scripts.quests;

import l2e.gameserver.instancemanager.SoIManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _697_DefendtheHallofErosion extends Quest {
   public _697_DefendtheHallofErosion(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32603);
      this.addTalkId(32603);
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32603-03.htm")) {
            st.startQuest();
         }

         return event;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getCond();
         switch(st.getState()) {
            case 0:
               if (player.getLevel() < getMinLvl(this.getId())) {
                  htmltext = "32603-00.htm";
                  st.exitQuest(true);
               }

               if (SoIManager.getCurrentStage() != 4) {
                  htmltext = "32603-00a.htm";
                  st.exitQuest(true);
               }

               htmltext = "32603-01.htm";
               break;
            case 1:
               if (cond == 1 && st.getInt("defenceDone") == 0) {
                  htmltext = "32603-04.htm";
               } else if (cond == 1 && st.getInt("defenceDone") != 0) {
                  st.calcReward(this.getId());
                  htmltext = "32603-05.htm";
                  st.unset("defenceDone");
                  st.exitQuest(true, true);
               }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _697_DefendtheHallofErosion(697, _697_DefendtheHallofErosion.class.getSimpleName(), "");
   }
}
