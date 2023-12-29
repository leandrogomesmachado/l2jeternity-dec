package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _352_HelpRoodRaiseANewPet extends Quest {
   private static final String qn = "_352_HelpRoodRaiseANewPet";
   private static final int ROOD = 31067;
   private static final int LIENRIK_EGG_1 = 5860;
   private static final int LIENRIK_EGG_2 = 5861;

   public _352_HelpRoodRaiseANewPet(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31067);
      this.addTalkId(31067);
      this.addKillId(new int[]{20786, 20787, 21644, 21645});
      this.questItemIds = new int[]{5860, 5861};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_352_HelpRoodRaiseANewPet");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31067-04.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("31067-09.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = Quest.getNoQuestMsg(player);
      QuestState st = player.getQuestState("_352_HelpRoodRaiseANewPet");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 39 && player.getLevel() <= 44) {
                  htmltext = "31067-01.htm";
               } else {
                  htmltext = "31067-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               long eggs1 = st.getQuestItemsCount(5860);
               long eggs2 = st.getQuestItemsCount(5861);
               if (eggs1 + eggs2 == 0L) {
                  htmltext = "31067-05.htm";
               } else {
                  int reward = 2000;
                  if (eggs1 > 0L && eggs2 == 0L) {
                     htmltext = "31067-06.htm";
                     reward = (int)((long)reward + eggs1 * 34L);
                     st.takeItems(5860, -1L);
                     st.rewardItems(57, (long)reward);
                  } else if (eggs1 == 0L && eggs2 > 0L) {
                     htmltext = "31067-08.htm";
                     reward = (int)((long)reward + eggs2 * 1025L);
                     st.takeItems(5861, -1L);
                     st.rewardItems(57, (long)reward);
                  } else if (eggs1 > 0L && eggs2 > 0L) {
                     htmltext = "31067-08.htm";
                     reward = (int)((long)reward + eggs1 * 34L + eggs2 * 1025L + 2000L);
                     st.takeItems(5860, -1L);
                     st.takeItems(5861, -1L);
                     st.rewardItems(57, (long)reward);
                  }
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_352_HelpRoodRaiseANewPet");
      if (st == null) {
         return null;
      } else {
         if (st.isStarted()) {
            int random = st.getRandom(100);
            if (random < 30) {
               st.giveItems(5860, 1L);
               st.playSound("ItemSound.quest_itemget");
            }

            if (random < 7) {
               st.giveItems(5861, 1L);
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _352_HelpRoodRaiseANewPet(352, "_352_HelpRoodRaiseANewPet", "");
   }
}
