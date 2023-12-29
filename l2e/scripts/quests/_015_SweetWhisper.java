package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _015_SweetWhisper extends Quest {
   public _015_SweetWhisper(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31302);
      this.addTalkId(31302);
      this.addTalkId(31517);
      this.addTalkId(31518);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31302-1.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("31518-1.htm")) {
            st.setCond(2, true);
         } else if (event.equalsIgnoreCase("31517-1.htm")) {
            st.calcExpAndSp(this.getId());
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
         int cond = st.getCond();
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 60) {
                  htmltext = "31302-0.htm";
               } else {
                  htmltext = "31302-0a.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 31302:
                     if (cond >= 1) {
                        htmltext = "31302-1a.htm";
                     }

                     return htmltext;
                  case 31517:
                     if (cond == 2) {
                        htmltext = "31517-0.htm";
                     }

                     return htmltext;
                  case 31518:
                     switch(cond) {
                        case 1:
                           return "31518-0.htm";
                        case 2:
                           htmltext = "31518-1a.htm";
                           return htmltext;
                        default:
                           return htmltext;
                     }
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
      new _015_SweetWhisper(15, _015_SweetWhisper.class.getSimpleName(), "");
   }
}
