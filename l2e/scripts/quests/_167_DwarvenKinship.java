package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _167_DwarvenKinship extends Quest {
   private static final String qn = "_167_DwarvenKinship";
   private static final int CARLON = 30350;
   private static final int NORMAN = 30210;
   private static final int HAPROCK = 30255;
   private static final int CARLON_LETTER = 1076;
   private static final int NORMANS_LETTER = 1106;

   public _167_DwarvenKinship(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30350);
      this.addTalkId(30350);
      this.addTalkId(30210);
      this.addTalkId(30255);
      this.questItemIds = new int[]{1076, 1106};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_167_DwarvenKinship");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30350-04.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.giveItems(1076, 1L);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30255-03.htm")) {
            st.set("cond", "2");
            st.takeItems(1076, 1L);
            st.giveItems(1106, 1L);
            st.rewardItems(57, 2000L);
         } else if (event.equalsIgnoreCase("30255-04.htm")) {
            st.takeItems(1076, 1L);
            st.rewardItems(57, 3000L);
            st.unset("cond");
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
         } else if (event.equalsIgnoreCase("30210-02.htm")) {
            st.takeItems(1106, 1L);
            st.rewardItems(57, 20000L);
            st.unset("cond");
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_167_DwarvenKinship");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 15) {
                  htmltext = "30350-03.htm";
               } else {
                  htmltext = "30350-02.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 30210:
                     if (cond == 2) {
                        htmltext = "30210-01.htm";
                     }

                     return htmltext;
                  case 30255:
                     if (cond == 1) {
                        htmltext = "30255-01.htm";
                     } else if (cond == 2) {
                        htmltext = "30255-05.htm";
                        return htmltext;
                     }

                     return htmltext;
                  case 30350:
                     if (cond == 1) {
                        htmltext = "30350-05.htm";
                     }

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
      new _167_DwarvenKinship(167, "_167_DwarvenKinship", "");
   }
}
