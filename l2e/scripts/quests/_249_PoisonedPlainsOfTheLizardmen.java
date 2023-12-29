package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _249_PoisonedPlainsOfTheLizardmen extends Quest {
   private static final String qn = "_249_PoisonedPlainsOfTheLizardmen";
   private static final int _mouen = 30196;
   private static final int _johnny = 32744;

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_249_PoisonedPlainsOfTheLizardmen");
      if (st == null) {
         return event;
      } else {
         if (npc.getId() == 30196) {
            if (event.equalsIgnoreCase("30196-03.htm")) {
               st.setState((byte)1);
               st.set("cond", "1");
               st.playSound("ItemSound.quest_accept");
            }
         } else if (npc.getId() == 32744 && event.equalsIgnoreCase("32744-03.htm")) {
            st.unset("cond");
            st.giveItems(57, 83056L);
            st.addExpAndSp(477496, 58743);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_249_PoisonedPlainsOfTheLizardmen");
      if (st == null) {
         return htmltext;
      } else {
         if (npc.getId() == 30196) {
            switch(st.getState()) {
               case 0:
                  if (player.getLevel() >= 82) {
                     htmltext = "30196-01.htm";
                  } else {
                     htmltext = "30196-00.htm";
                  }
                  break;
               case 1:
                  if (st.getInt("cond") == 1) {
                     htmltext = "30196-04.htm";
                  }
                  break;
               case 2:
                  htmltext = "30196-05.htm";
            }
         } else if (npc.getId() == 32744) {
            if (st.getInt("cond") == 1) {
               htmltext = "32744-01.htm";
            } else if (st.getState() == 2) {
               htmltext = "32744-04.htm";
            }
         }

         return htmltext;
      }
   }

   public _249_PoisonedPlainsOfTheLizardmen(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30196);
      this.addTalkId(30196);
      this.addTalkId(32744);
   }

   public static void main(String[] args) {
      new _249_PoisonedPlainsOfTheLizardmen(249, "_249_PoisonedPlainsOfTheLizardmen", "");
   }
}
