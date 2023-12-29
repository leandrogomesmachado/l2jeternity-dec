package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _122_OminousNews extends Quest {
   public _122_OminousNews(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31979);
      this.addTalkId(new int[]{31979, 32017});
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31979-03.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("32017-02.htm")) {
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
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= getMinLvl(this.getId()) && player.getLevel() <= getMaxLvl(this.getId())) {
                  htmltext = "31979-02.htm";
               } else {
                  htmltext = "31979-01.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 31979:
                     return "31979-03.htm";
                  case 32017:
                     htmltext = "32017-01.htm";
                     return htmltext;
                  default:
                     return htmltext;
               }
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _122_OminousNews(122, _122_OminousNews.class.getSimpleName(), "");
   }
}
