package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _112_WalkOfFate extends Quest {
   public _112_WalkOfFate(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30572);
      this.addTalkId(new int[]{30572, 32017});
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32017-02.htm")) {
            if (st.isCond(1)) {
               st.calcExpAndSp(this.getId());
               st.calcReward(this.getId());
               st.exitQuest(false, true);
            }
         } else if (event.equalsIgnoreCase("30572-02.htm")) {
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
               if (npcId == 30572) {
                  if (player.getLevel() >= getMinLvl(this.getId()) && player.getLevel() <= getMaxLvl(this.getId())) {
                     htmltext = "30572-01.htm";
                  } else {
                     htmltext = "30572-00.htm";
                     st.exitQuest(true);
                  }
               }
               break;
            case 1:
               if (npcId == 30572) {
                  htmltext = "30572-03.htm";
               } else if (npcId == 32017) {
                  htmltext = "32017-01.htm";
               }
               break;
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _112_WalkOfFate(112, _112_WalkOfFate.class.getSimpleName(), "");
   }
}
