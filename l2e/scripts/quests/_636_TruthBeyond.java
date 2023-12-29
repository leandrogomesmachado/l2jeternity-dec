package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _636_TruthBeyond extends Quest {
   public _636_TruthBeyond(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31329);
      this.addTalkId(31329);
      this.addTalkId(32010);
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if ("31329-04.htm".equalsIgnoreCase(event)) {
            st.startQuest();
         } else if ("32010-02.htm".equalsIgnoreCase(event)) {
            st.calcReward(this.getId());
            st.exitQuest(true, true);
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
         int npcId = npc.getId();
         switch(st.getState()) {
            case 0:
               if (npcId == 31329) {
                  if (st.getQuestItemsCount(8064) != 0L || st.getQuestItemsCount(8067) != 0L) {
                     htmltext = "31329-mark.htm";
                  } else if (player.getLevel() >= 73) {
                     htmltext = "31329-02.htm";
                  } else {
                     st.exitQuest(true);
                     htmltext = "31329-01.htm";
                  }
               } else if (npcId == 32010 && st.getQuestItemsCount(8064) == 1L) {
                  htmltext = "32010-03.htm";
               }
               break;
            case 1:
               if (npcId == 31329) {
                  htmltext = "31329-05.htm";
               } else if (npcId == 32010 && st.isCond(1)) {
                  htmltext = "32010-01.htm";
               }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _636_TruthBeyond(636, _636_TruthBeyond.class.getSimpleName(), "");
   }
}
