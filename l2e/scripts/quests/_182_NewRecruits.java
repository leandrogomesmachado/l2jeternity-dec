package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _182_NewRecruits extends Quest {
   private static final String qn = "_182_NewRecruits";
   private static final int _kekropus = 32138;
   private static final int _nornil = 32258;

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_182_NewRecruits");
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
            if (event.equalsIgnoreCase("32258-04.htm")) {
               st.giveItems(847, 2L);
               st.playSound("ItemSound.quest_finish");
               st.exitQuest(false);
            } else if (event.equalsIgnoreCase("32258-05.htm")) {
               st.giveItems(890, 2L);
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
      QuestState st = player.getQuestState("_182_NewRecruits");
      if (st == null) {
         return htmltext;
      } else if (player.getRace().ordinal() == 5) {
         return "32138-00.htm";
      } else if (player.getLevel() < 17) {
         return "32138-00a.htm";
      } else {
         if (npc.getId() == 32138) {
            switch(st.getState()) {
               case 0:
                  htmltext = "32138-01.htm";
                  break;
               case 1:
                  if (st.getInt("cond") == 1) {
                     htmltext = "32138-04.htm";
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

   public _182_NewRecruits(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32138);
      this.addTalkId(32138);
      this.addTalkId(32258);
   }

   public static void main(String[] args) {
      new _182_NewRecruits(182, "_182_NewRecruits", "");
   }
}
