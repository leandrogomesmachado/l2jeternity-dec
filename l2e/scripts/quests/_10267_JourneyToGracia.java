package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _10267_JourneyToGracia extends Quest {
   public _10267_JourneyToGracia(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30857);
      this.addTalkId(new int[]{30857, 32548, 32564});
      this.questItemIds = new int[]{13810};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         switch(event) {
            case "30857-06.htm":
               st.giveItems(13810, 1L);
               st.startQuest();
               break;
            case "32564-02.htm":
               st.setCond(2, true);
               break;
            case "32548-02.htm":
               st.calcExpAndSp(this.getId());
               st.calcReward(this.getId());
               st.exitQuest(false, true);
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
               if (npcId == 30857) {
                  htmltext = player.getLevel() < 75 ? "30857-00.htm" : "30857-01.htm";
               }
               break;
            case 1:
               int cond = st.getInt("cond");
               if (npcId == 30857) {
                  htmltext = "30857-07.htm";
               } else if (npcId == 32564) {
                  htmltext = cond == 1 ? "32564-01.htm" : "32564-03.htm";
               } else if (npcId == 32548 && cond == 2) {
                  htmltext = "32548-01.htm";
               }
               break;
            case 2:
               if (npcId == 32548) {
                  htmltext = "32548-03.htm";
               } else if (npcId == 30857) {
                  htmltext = "30857-0a.htm";
               }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _10267_JourneyToGracia(10267, _10267_JourneyToGracia.class.getSimpleName(), "");
   }
}
