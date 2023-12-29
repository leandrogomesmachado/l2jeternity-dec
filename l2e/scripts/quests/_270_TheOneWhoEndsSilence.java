package l2e.scripts.quests;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _270_TheOneWhoEndsSilence extends Quest {
   private static final String qn = "_270_TheOneWhoEndsSilence";
   private static final int GREMORY = 32757;
   private static final int DROP_CHANCE = 520;
   private static final int TORCH = 15526;
   private static final int[] MOBS = new int[]{22790, 22791, 22793, 22789, 22797, 22795, 22794, 22796, 22800, 22798, 22799};
   private static final int[] REWARDS = new int[]{10373, 10374, 10375, 10376, 10377, 10378, 10379, 10380, 10381};
   private static final int[] REWARDS2 = new int[]{10398, 10399, 10400, 10401, 10402, 10403, 10404, 10405};
   private static final int[] REWARDS3 = new int[]{5595, 5594, 5593};

   public _270_TheOneWhoEndsSilence(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32757);
      this.addTalkId(32757);

      for(int i : MOBS) {
         this.addKillId(i);
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_270_TheOneWhoEndsSilence");
      if (st == null) {
         return event;
      } else {
         long count = st.getQuestItemsCount(15526);
         long random = (long)getRandom(2);
         if (event.equalsIgnoreCase("32757-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("32757-05.htm")) {
            if (count >= 100L) {
               if (random == 0L) {
                  st.takeItems(15526, 100L);
                  st.rewardItems(REWARDS[getRandom(REWARDS.length - 1)], 1L);
                  st.playSound("ItemSound.quest_middle");
               }

               if (random == 1L) {
                  st.takeItems(15526, 100L);
                  st.rewardItems(REWARDS3[getRandom(REWARDS3.length - 1)], 1L);
                  st.playSound("ItemSound.quest_middle");
               }

               htmltext = "32757-07.htm";
            }
         } else if (event.equalsIgnoreCase("32757-09.htm")) {
            if (count >= 200L) {
               st.takeItems(15526, 200L);
               st.giveItems(REWARDS[getRandom(REWARDS.length - 1)], 1L);
               st.giveItems(REWARDS3[getRandom(REWARDS3.length - 1)], 1L);
               st.playSound("ItemSound.quest_middle");
               htmltext = "32757-07.htm";
            }
         } else if (event.equalsIgnoreCase("32757-10.htm")) {
            if (count >= 300L) {
               st.takeItems(15526, 300L);
               st.giveItems(REWARDS[getRandom(REWARDS.length - 1)], 1L);
               st.giveItems(REWARDS2[getRandom(REWARDS2.length - 1)], 1L);
               st.giveItems(REWARDS3[getRandom(REWARDS3.length - 1)], 1L);
               st.playSound("ItemSound.quest_middle");
               htmltext = "32757-07.htm";
            }
         } else if (event.equalsIgnoreCase("32757-11.htm")) {
            if (count >= 400L) {
               if (random == 0L) {
                  st.takeItems(15526, 400L);
                  st.rewardItems(REWARDS[getRandom(REWARDS.length - 1)], 2L);
                  st.rewardItems(REWARDS3[getRandom(REWARDS3.length - 1)], 1L);
                  st.playSound("ItemSound.quest_middle");
               }

               if (random == 1L) {
                  st.takeItems(15526, 400L);
                  st.rewardItems(REWARDS[getRandom(REWARDS.length - 1)], 1L);
                  st.rewardItems(REWARDS2[getRandom(REWARDS2.length - 1)], 1L);
                  st.rewardItems(REWARDS3[getRandom(REWARDS3.length - 1)], 2L);
                  st.playSound("ItemSound.quest_middle");
               }

               htmltext = "32757-07.htm";
            }
         } else if (event.equalsIgnoreCase("32757-12.htm")) {
            if (count >= 500L) {
               st.takeItems(15526, 500L);
               st.giveItems(REWARDS[getRandom(REWARDS.length - 1)], 2L);
               st.giveItems(REWARDS2[getRandom(REWARDS2.length - 1)], 1L);
               st.giveItems(REWARDS3[getRandom(REWARDS3.length - 1)], 2L);
               st.playSound("ItemSound.quest_middle");
               htmltext = "32757-07.htm";
            }
         } else if (event.equalsIgnoreCase("32757-08.htm")) {
            st.takeItems(15526, -1L);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return htmltext;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_270_TheOneWhoEndsSilence");
      if (st == null) {
         return htmltext;
      } else {
         int id = st.getState();
         int cond = st.getInt("cond");
         int npcId = npc.getId();
         long count = st.getQuestItemsCount(15526);
         if (npcId == 32757) {
            if (id == 0 && cond == 0) {
               QuestState _prev = player.getQuestState("_10288_SecretMission");
               if (player.getLevel() >= 82) {
                  if (_prev != null && _prev.getState() == 2) {
                     htmltext = "32757-01.htm";
                  } else {
                     htmltext = "32757-02a.htm";
                  }
               } else {
                  htmltext = "32757-02.htm";
               }
            } else if (id == 1 && cond == 1) {
               if (count >= 100L) {
                  htmltext = "32757-04.htm";
               } else {
                  htmltext = "32757-05.htm";
               }
            }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(player, 1);
      if (partyMember == null) {
         return null;
      } else {
         QuestState st = partyMember.getQuestState("_270_TheOneWhoEndsSilence");
         if (st == null) {
            return null;
         } else {
            int id = st.getState();
            int cond = st.getInt("cond");
            if (id == 1) {
               long count = st.getQuestItemsCount(15526);
               if (cond == 1) {
                  int chance = (int)(520.0F * Config.RATE_QUEST_DROP);
                  int numItems = chance / 1000;
                  chance %= 1000;
                  if (getRandom(1000) < chance) {
                     ++numItems;
                  }

                  if (numItems > 0) {
                     if ((count + (long)numItems) / 100L > count / 100L) {
                        st.playSound("ItemSound.quest_middle");
                     } else {
                        st.playSound("ItemSound.quest_itemget");
                     }

                     st.giveItems(15526, (long)numItems);
                  }
               }
            }

            return null;
         }
      }
   }

   public static void main(String[] args) {
      new _270_TheOneWhoEndsSilence(270, "_270_TheOneWhoEndsSilence", "");
   }
}
