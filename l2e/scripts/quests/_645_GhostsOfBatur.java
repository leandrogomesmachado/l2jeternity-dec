package l2e.scripts.quests;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _645_GhostsOfBatur extends Quest {
   private static String qn = "_645_GhostsOfBatur";
   private static final int KARUDA = 32017;
   private static final int CURSED_BURIAL = 14861;
   private static final int[] MOBS = new int[]{22703, 22704, 22705, 22706};
   private static final int[] REWARDS = new int[]{9967, 9968, 9969, 9970, 9971, 9972, 9973, 9974, 9975, 10544, 10545};
   private static final int DROP_CHANCE = 400;

   public _645_GhostsOfBatur(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(32017);
      this.addTalkId(32017);

      for(int i : MOBS) {
         this.addKillId(i);
      }

      this.questItemIds = new int[]{14861};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(qn);
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32017-03.htm")) {
            if (player.getLevel() < 80) {
               htmltext = "32017-02.htm";
               st.exitQuest(true);
            } else {
               st.set("cond", "1");
               st.setState((byte)1);
               st.playSound("ItemSound.quest_accept");
            }
         } else if (event.equalsIgnoreCase("32017-06.htm")) {
            if (player.getLevel() < 80) {
               htmltext = "32017-02.htm";
               st.exitQuest(true);
            } else {
               htmltext = "32017-06.htm";
            }
         } else if (event.equalsIgnoreCase("REWARDS")) {
            if (st.getQuestItemsCount(14861) >= 500L) {
               st.takeItems(14861, 500L);
               st.rewardItems(REWARDS[getRandom(REWARDS.length - 1)], 1L);
               st.playSound("ItemSound.quest_middle");
               htmltext = "32017-05c.htm";
            } else {
               htmltext = "32017-07.htm";
            }
         } else if (event.equalsIgnoreCase("LEO")) {
            if (st.getQuestItemsCount(14861) >= 8L) {
               st.takeItems(14861, 8L);
               st.rewardItems(9628, 1L);
               st.playSound("ItemSound.quest_middle");
               htmltext = "32017-05c.htm";
            } else {
               htmltext = "32017-07.htm";
            }
         } else if (event.equalsIgnoreCase("ADA")) {
            if (st.getQuestItemsCount(14861) >= 15L) {
               st.takeItems(14861, 15L);
               st.rewardItems(9629, 1L);
               st.playSound("ItemSound.quest_middle");
               htmltext = "32017-05c.htm";
            } else {
               htmltext = "32017-07.htm";
            }
         } else if (event.equalsIgnoreCase("ORI")) {
            if (st.getQuestItemsCount(14861) >= 12L) {
               st.takeItems(14861, 12L);
               st.rewardItems(9630, 1L);
               st.playSound("ItemSound.quest_middle");
               htmltext = "32017-05c.htm";
            } else {
               htmltext = "32017-07.htm";
            }
         } else if (event.equalsIgnoreCase("32017-08.htm")) {
            st.takeItems(14861, -1L);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
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
         switch(st.getState()) {
            case 0:
               htmltext = "32017-01.htm";
               break;
            case 1:
               switch(st.getInt("cond")) {
                  case 0:
                     htmltext = "32017-04.htm";
                     break;
                  case 1:
                     if (st.getQuestItemsCount(14861) > 0L) {
                        htmltext = "32017-05b.htm";
                     } else {
                        htmltext = "32017-05a.htm";
                     }
                     break;
                  default:
                     htmltext = "32017-02.htm";
                     st.exitQuest(true);
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(player, 1);
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
               long count = st.getQuestItemsCount(14861);
               if (cond == 1) {
                  int chance = (int)(400.0F * Config.RATE_QUEST_DROP);
                  int numItems = chance / 1000;
                  chance %= 1000;
                  if (getRandom(1000) < chance) {
                     ++numItems;
                  }

                  if (numItems > 0) {
                     if ((count + (long)numItems) / 500L > count / 500L) {
                        st.playSound("ItemSound.quest_middle");
                     } else {
                        st.playSound("ItemSound.quest_itemget");
                     }

                     st.giveItems(14861, (long)numItems);
                  }
               }
            }

            return null;
         }
      }
   }

   public static void main(String[] args) {
      new _645_GhostsOfBatur(645, qn, "");
   }
}
