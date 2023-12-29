package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _121_PavelTheGiant extends Quest {
   private static final String qn = "_121_PavelTheGiant";
   private static final int NEWYEAR = 31961;
   private static final int YUMI = 32041;

   public _121_PavelTheGiant(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31961);
      this.addTalkId(31961);
      this.addTalkId(32041);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_121_PavelTheGiant");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31961-2.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("32041-2.htm")) {
            st.addExpAndSp(346320, 26069);
            st.playSound("ItemSound.quest_finish");
            st.unset("cond");
            st.exitQuest(false);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_121_PavelTheGiant");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 46) {
                  htmltext = "31961-1.htm";
               } else {
                  htmltext = "31961-1a.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 31961:
                     return "31961-2a.htm";
                  case 32041:
                     htmltext = "32041-1.htm";
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
      new _121_PavelTheGiant(121, "_121_PavelTheGiant", "");
   }
}
