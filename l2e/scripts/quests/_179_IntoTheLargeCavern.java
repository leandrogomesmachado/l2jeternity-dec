package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _179_IntoTheLargeCavern extends Quest {
   private static final String qn = "_179_IntoTheLargeCavern";
   private static final int _kekropus = 32138;
   private static final int _nornil = 32258;

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_179_IntoTheLargeCavern");
      if (st == null) {
         return event;
      } else {
         if (npc.getId() == 32138) {
            if (event.equalsIgnoreCase("32138-03.htm")) {
               st.setState((byte)1);
               st.set("cond", "1");
               st.playSound("ItemSound.quest_accept");
            }
         } else if (npc.getId() == 32258) {
            if (event.equalsIgnoreCase("32258-08.htm")) {
               st.giveItems(391, 1L);
               st.giveItems(413, 1L);
               st.playSound("ItemSound.quest_finish");
               st.exitQuest(false);
            } else if (event.equalsIgnoreCase("32258-09.htm")) {
               st.giveItems(847, 2L);
               st.giveItems(890, 2L);
               st.giveItems(910, 1L);
               st.playSound("ItemSound.quest_finish");
               st.exitQuest(false);
            }
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_179_IntoTheLargeCavern");
      if (st == null) {
         return htmltext;
      } else {
         QuestState _prev = player.getQuestState("178_IconicTrinity");
         if (player.getRace().ordinal() != 5) {
            return "32138-00a.htm";
         } else {
            if (_prev == null || _prev.getState() != 2 || player.getLevel() < 17 || player.getClassId().level() != 0) {
               htmltext = "32138-00.htm";
            } else if (npc.getId() == 32138) {
               switch(st.getState()) {
                  case 0:
                     htmltext = "32138-01.htm";
                     break;
                  case 1:
                     if (st.getInt("cond") == 1) {
                        htmltext = "32138-05.htm";
                     }
                     break;
                  case 2:
                     htmltext = getAlreadyCompletedMsg(player);
               }
            } else if (npc.getId() == 32258 && st.getState() == 1) {
               htmltext = "32258-01.htm";
            } else if (npc.getId() == 32258 && st.getState() == 2) {
               htmltext = "32258-exit.htm";
            }

            return htmltext;
         }
      }
   }

   public _179_IntoTheLargeCavern(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32138);
      this.addTalkId(32138);
      this.addTalkId(32258);
   }

   public static void main(String[] args) {
      new _179_IntoTheLargeCavern(179, "_179_IntoTheLargeCavern", "");
   }
}
