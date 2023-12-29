package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _10269_ToTheSeedOfDestruction extends Quest {
   public _10269_ToTheSeedOfDestruction(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32548);
      this.addTalkId(new int[]{32548, 32526});
      this.questItemIds = new int[]{13812};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32548-05.htm")) {
            st.giveItems(13812, 1L);
            st.startQuest();
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         switch(st.getState()) {
            case 0:
               if (npcId == 32548) {
                  htmltext = player.getLevel() < 75 ? "32548-00.htm" : "32548-01.htm";
               }
               break;
            case 1:
               if (npcId == 32548) {
                  htmltext = "32548-06.htm";
               } else if (npcId == 32526) {
                  htmltext = "32526-01.htm";
                  st.calcExpAndSp(this.getId());
                  st.calcReward(this.getId());
                  st.exitQuest(false, true);
               }
               break;
            case 2:
               htmltext = npcId == 32526 ? "32526-02.htm" : "32548-0a.htm";
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _10269_ToTheSeedOfDestruction(10269, _10269_ToTheSeedOfDestruction.class.getSimpleName(), "");
   }
}
