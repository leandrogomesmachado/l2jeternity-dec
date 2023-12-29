package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _312_TakeAdvantageOfTheCrisis extends Quest {
   public _312_TakeAdvantageOfTheCrisis(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(30535);
      this.addTalkId(30535);
      this.addKillId(new int[]{22678, 22679, 22680, 22681, 22682, 22683, 22684, 22685, 22686, 22687, 22688, 22689, 22690});
      this.questItemIds = new int[]{14875};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30535-25.htm")) {
            st.exitQuest(true);
            st.playSound("ItemSound.quest_finish");
         } else if (event.equalsIgnoreCase("30535-6.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("30535-14.htm")) {
            if (st.getQuestItemsCount(14875) >= 366L) {
               st.takeItems(14875, 366L);
               st.calcReward(this.getId(), 1);
               st.playSound("ItemSound.quest_middle");
               htmltext = "30535-14.htm";
            } else {
               htmltext = "30535-14no.htm";
            }
         } else if (event.equalsIgnoreCase("30535-15.htm")) {
            if (st.getQuestItemsCount(14875) >= 299L) {
               st.takeItems(14875, 299L);
               st.calcReward(this.getId(), 2);
               st.playSound("ItemSound.quest_middle");
               htmltext = "30535-14.htm";
            } else {
               htmltext = "30535-14no.htm";
            }
         } else if (event.equalsIgnoreCase("30535-16.htm")) {
            if (st.getQuestItemsCount(14875) >= 183L) {
               st.takeItems(14875, 183L);
               st.calcReward(this.getId(), 3);
               st.playSound("ItemSound.quest_middle");
               htmltext = "30535-14.htm";
            } else {
               htmltext = "30535-14no.htm";
            }
         } else if (event.equalsIgnoreCase("30535-17.htm")) {
            if (st.getQuestItemsCount(14875) >= 122L) {
               st.takeItems(14875, 122L);
               st.calcReward(this.getId(), 4);
               st.playSound("ItemSound.quest_middle");
               htmltext = "30535-14.htm";
            } else {
               htmltext = "30535-14no.htm";
            }
         } else if (event.equalsIgnoreCase("30535-18.htm")) {
            if (st.getQuestItemsCount(14875) >= 122L) {
               st.takeItems(14875, 122L);
               st.calcReward(this.getId(), 5);
               st.playSound("ItemSound.quest_middle");
               htmltext = "30535-14.htm";
            } else {
               htmltext = "30535-14no.htm";
            }
         } else if (event.equalsIgnoreCase("30535-19.htm")) {
            if (st.getQuestItemsCount(14875) >= 129L) {
               st.takeItems(14875, 129L);
               st.calcReward(this.getId(), 6);
               st.playSound("ItemSound.quest_middle");
               htmltext = "30535-14.htm";
            } else {
               htmltext = "30535-14no.htm";
            }
         } else if (event.equalsIgnoreCase("30535-20.htm")) {
            if (st.getQuestItemsCount(14875) >= 667L) {
               st.takeItems(14875, 667L);
               st.calcReward(this.getId(), 7);
               st.playSound("ItemSound.quest_middle");
               htmltext = "30535-14.htm";
            } else {
               htmltext = "30535-14no.htm";
            }
         } else if (event.equalsIgnoreCase("30535-21.htm")) {
            if (st.getQuestItemsCount(14875) >= 1000L) {
               st.takeItems(14875, 1000L);
               st.calcReward(this.getId(), 8);
               st.playSound("ItemSound.quest_middle");
               htmltext = "30535-.htm";
            } else {
               htmltext = "30535-14no.htm";
            }
         } else if (event.equalsIgnoreCase("30535-22.htm")) {
            if (st.getQuestItemsCount(14875) >= 24L) {
               st.takeItems(14875, 24L);
               st.calcReward(this.getId(), 9);
               st.playSound("ItemSound.quest_middle");
               htmltext = "30535-14.htm";
            } else {
               htmltext = "30535-14no.htm";
            }
         } else if (event.equalsIgnoreCase("30535-23.htm")) {
            if (st.getQuestItemsCount(14875) >= 24L) {
               st.takeItems(14875, 24L);
               st.calcReward(this.getId(), 10);
               st.playSound("ItemSound.quest_middle");
               htmltext = "30535-14.htm";
            } else {
               htmltext = "30535-14no.htm";
            }
         } else if (event.equalsIgnoreCase("30535-24.htm")) {
            if (st.getQuestItemsCount(14875) >= 36L) {
               st.takeItems(14875, 36L);
               st.calcReward(this.getId(), 11);
               st.playSound("ItemSound.quest_middle");
               htmltext = "30535-14.htm";
            } else {
               htmltext = "30535-14no.htm";
            }
         }

         return htmltext;
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
               if (player.getLevel() >= 80) {
                  htmltext = "30535-0.htm";
               } else {
                  st.exitQuest(true);
                  htmltext = "30535-0a.htm";
               }
               break;
            case 1:
               if (st.getQuestItemsCount(14875) == 0L) {
                  htmltext = "30535-6.htm";
               } else {
                  htmltext = "30535-7.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMemberState(player, (byte)1);
      if (partyMember == null) {
         return super.onKill(npc, player, isSummon);
      } else {
         QuestState st = partyMember.getQuestState(this.getName());
         if (st != null) {
            st.calcDropItems(this.getId(), 14875, npc.getId(), Integer.MAX_VALUE);
         }

         return super.onKill(npc, player, isSummon);
      }
   }

   public static void main(String[] args) {
      new _312_TakeAdvantageOfTheCrisis(312, _312_TakeAdvantageOfTheCrisis.class.getSimpleName(), "");
   }
}
