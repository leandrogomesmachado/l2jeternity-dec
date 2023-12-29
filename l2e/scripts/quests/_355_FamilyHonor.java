package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _355_FamilyHonor extends Quest {
   private static final String qn = "_355_FamilyHonor";
   private static final int GALIBREDO = 30181;
   private static final int PATRIN = 30929;
   private static final int GALIBREDO_BUST = 4252;
   private static final int WORK_OF_BERONA = 4350;
   private static final int STATUE_PROTOTYPE = 4351;
   private static final int STATUE_ORIGINAL = 4352;
   private static final int STATUE_REPLICA = 4353;
   private static final int STATUE_FORGERY = 4354;

   public _355_FamilyHonor(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30181);
      this.addTalkId(new int[]{30181, 30929});
      this.addKillId(new int[]{20767, 20768, 20769, 20770});
      this.questItemIds = new int[]{4252};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_355_FamilyHonor");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30181-2.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30181-4b.htm")) {
            long count = st.getQuestItemsCount(4252);
            if (count > 0L) {
               htmltext = "30181-4.htm";
               long reward = 2800L + count * 120L;
               if (count >= 100L) {
                  htmltext = "30181-4a.htm";
                  reward += 5000L;
               }

               st.takeItems(4252, count);
               st.rewardItems(57, reward);
            }
         } else if (event.equalsIgnoreCase("30929-7.htm")) {
            if (st.getQuestItemsCount(4350) > 0L) {
               int appraising = st.getRandom(100);
               if (appraising <= 20) {
                  htmltext = "30929-2.htm";
                  st.takeItems(4350, 1L);
               } else if (appraising <= 40 && appraising >= 20) {
                  htmltext = "30929-3.htm";
                  st.takeItems(4350, 1L);
                  st.giveItems(4353, 1L);
               } else if (appraising <= 60 && appraising >= 40) {
                  htmltext = "30929-4.htm";
                  st.takeItems(4350, 1L);
                  st.giveItems(4352, 1L);
               } else if (appraising <= 80 && appraising >= 60) {
                  htmltext = "30929-5.htm";
                  st.takeItems(4350, 1L);
                  st.giveItems(4354, 1L);
               } else if (appraising <= 100 && appraising >= 80) {
                  htmltext = "30929-6.htm";
                  st.takeItems(4350, 1L);
                  st.giveItems(4351, 1L);
               }
            }
         } else if (event.equalsIgnoreCase("30181-6.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_355_FamilyHonor");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 36 && player.getLevel() <= 49) {
                  htmltext = "30181-0.htm";
               } else {
                  htmltext = "30181-0a.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 30181:
                     if (st.getQuestItemsCount(4252) > 0L) {
                        htmltext = "30181-3a.htm";
                     } else {
                        htmltext = "30181-3.htm";
                     }
                     break;
                  case 30929:
                     htmltext = "30929-0.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMemberState(player, (byte)1);
      if (partyMember == null) {
         return null;
      } else {
         QuestState st = partyMember.getQuestState("_355_FamilyHonor");
         int chance = st.getRandom(100);
         if (chance < 40) {
            st.giveItems(4252, 1L);
            if (chance < 20) {
               st.giveItems(4350, 1L);
            }

            st.playSound("ItemSound.quest_itemget");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _355_FamilyHonor(355, "_355_FamilyHonor", "");
   }
}
