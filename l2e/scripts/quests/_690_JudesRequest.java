package l2e.scripts.quests;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _690_JudesRequest extends Quest {
   private static String qn = "_690_JudesRequest";
   private static final int JUDE = 32356;
   private static final int[] MOBS = new int[]{22398, 22399};
   private static final int[] REWARDS = new int[]{
      9975, 9968, 9970, 10545, 9972, 9971, 9974, 9969, 10544, 9967, 10374, 10380, 10378, 10379, 10376, 10373, 10375, 10381, 10377
   };
   private static final int[] MAT = new int[]{
      9624, 9617, 9619, 9621, 9620, 9623, 9618, 9616, 10547, 10546, 10398, 10404, 10402, 10403, 10400, 10397, 10399, 10405, 10406, 10401, 10407
   };
   private static final int EVIL = 10327;
   private static final int DROP_CHANCE = 550;

   public _690_JudesRequest(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(32356);
      this.addTalkId(32356);

      for(int i : MOBS) {
         this.addKillId(i);
      }

      this.questItemIds = new int[]{10327};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(qn);
      if (st == null) {
         return event;
      } else {
         long evil = st.getQuestItemsCount(10327);
         if (event.equalsIgnoreCase("32356-03.htm")) {
            if (player.getLevel() >= 78) {
               st.set("cond", "1");
               st.setState((byte)1);
               st.playSound("ItemSound.quest_accept");
            } else {
               htmltext = "32356-02.htm";
               st.exitQuest(true);
            }
         } else if (event.equalsIgnoreCase("32356-07.htm")) {
            if (evil >= 200L) {
               htmltext = "32356-07.htm";
               st.takeItems(10327, 200L);
               st.giveItems(REWARDS[getRandom(REWARDS.length)], 1L);
            } else {
               htmltext = "32356-05.htm";
            }
         } else if (event.equalsIgnoreCase("32356-08.htm")) {
            st.exitQuest(true);
            st.playSound("ItemSound.quest_giveup");
         } else if (event.equalsIgnoreCase("32356-09.htm") && evil >= 5L) {
            htmltext = "32356-09.htm";
            st.takeItems(10327, 5L);
            st.giveItems(MAT[getRandom(MAT.length)], 1L);
            st.giveItems(MAT[getRandom(MAT.length)], 1L);
            st.giveItems(MAT[getRandom(MAT.length)], 1L);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(qn);
      if (st == null) {
         return htmltext;
      } else {
         byte id = st.getState();
         int cond = st.getInt("cond");
         long evil = st.getQuestItemsCount(10327);
         if (id == 0) {
            if (player.getLevel() >= 78) {
               htmltext = "32356-01.htm";
            } else {
               htmltext = "32356-02.htm";
               st.exitQuest(true);
            }
         } else if (cond == 1 && evil >= 200L) {
            htmltext = "32356-04.htm";
         } else if (cond == 1 && evil >= 5L && evil <= 200L) {
            htmltext = "32356-05.htm";
         } else {
            htmltext = "32356-05a.htm";
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
         QuestState st = partyMember.getQuestState(qn);
         if (st == null) {
            return null;
         } else {
            int id = st.getState();
            int cond = st.getInt("cond");
            if (id == 1) {
               long count = st.getQuestItemsCount(10327);
               if (cond == 1) {
                  int chance = (int)(550.0F * Config.RATE_QUEST_DROP);
                  int numItems = chance / 1000;
                  chance %= 1000;
                  if (getRandom(1000) < chance) {
                     ++numItems;
                  }

                  if (numItems > 0) {
                     if ((count + (long)numItems) / 200L > count / 200L) {
                        st.playSound("ItemSound.quest_middle");
                     } else {
                        st.playSound("ItemSound.quest_itemget");
                     }

                     st.giveItems(10327, (long)numItems);
                  }
               }
            }

            return null;
         }
      }
   }

   public static void main(String[] args) {
      new _690_JudesRequest(690, qn, "");
   }
}
