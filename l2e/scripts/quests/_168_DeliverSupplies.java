package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _168_DeliverSupplies extends Quest {
   private static final String qn = "_168_DeliverSupplies";
   private static final int JENNA = 30349;
   private static final int ROSELYN = 30355;
   private static final int KRISTIN = 30357;
   private static final int HARANT = 30360;
   private static final int JENNIES_LETTER = 1153;
   private static final int SENTRY_BLADE1 = 1154;
   private static final int SENTRY_BLADE2 = 1155;
   private static final int SENTRY_BLADE3 = 1156;
   private static final int OLD_BRONZE_SWORD = 1157;

   public _168_DeliverSupplies(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30349);
      this.addTalkId(30349);
      this.addTalkId(30355);
      this.addTalkId(30357);
      this.addTalkId(30360);
      this.questItemIds = new int[]{1154, 1157, 1153, 1155, 1156};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_168_DeliverSupplies");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30349-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1153, 1L);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_168_DeliverSupplies");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (player.getRace().ordinal() == 2) {
                  if (player.getLevel() >= 3 && player.getLevel() <= 6) {
                     htmltext = "30349-02.htm";
                  } else {
                     htmltext = "30349-01.htm";
                     st.exitQuest(true);
                  }
               } else {
                  htmltext = "30349-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 30349:
                     if (cond == 1) {
                        return "30349-04.htm";
                     } else if (cond == 2) {
                        htmltext = "30349-05.htm";
                        st.set("cond", "3");
                        st.takeItems(1154, 1L);
                        st.playSound("ItemSound.quest_middle");
                        return htmltext;
                     } else {
                        if (cond == 3) {
                           htmltext = "30349-07.htm";
                        } else if (cond == 4) {
                           htmltext = "30349-06.htm";
                           st.takeItems(1157, 2L);
                           st.rewardItems(57, 820L);
                           st.playSound("ItemSound.quest_finish");
                           st.exitQuest(false);
                           return htmltext;
                        }

                        return htmltext;
                     }
                  case 30355:
                     if (cond == 3) {
                        if (st.getQuestItemsCount(1154) == 0L && st.getQuestItemsCount(1155) == 1L) {
                           htmltext = "30355-01.htm";
                           st.takeItems(1155, 1L);
                           st.giveItems(1157, 1L);
                           if (st.getQuestItemsCount(1157) == 2L) {
                              st.set("cond", "4");
                              st.playSound("ItemSound.quest_middle");
                              return htmltext;
                           }
                        } else if (st.getQuestItemsCount(1155) == 0L) {
                           return "30355-02.htm";
                        }

                        return htmltext;
                     } else {
                        if (cond == 4) {
                           return "30355-02.htm";
                        }

                        return htmltext;
                     }
                  case 30357:
                     if (cond == 3) {
                        if (st.getQuestItemsCount(1156) == 1L && st.getQuestItemsCount(1154) == 0L) {
                           htmltext = "30357-01.htm";
                           st.takeItems(1156, 1L);
                           st.giveItems(1157, 1L);
                           if (st.getQuestItemsCount(1157) == 2L) {
                              st.set("cond", "4");
                              st.playSound("ItemSound.quest_middle");
                              return htmltext;
                           }
                        } else if (st.getQuestItemsCount(1156) == 0L) {
                           return "30357-02.htm";
                        }

                        return htmltext;
                     } else {
                        if (cond == 4) {
                           return "30357-02.htm";
                        }

                        return htmltext;
                     }
                  case 30360:
                     if (cond == 1) {
                        if (st.getQuestItemsCount(1153) == 1L) {
                           htmltext = "30360-01.htm";
                           st.takeItems(1153, 1L);
                           st.giveItems(1154, 1L);
                           st.giveItems(1155, 1L);
                           st.giveItems(1156, 1L);
                           st.set("cond", "2");
                           st.playSound("ItemSound.quest_middle");
                           return htmltext;
                        }
                     } else if (cond == 2) {
                        return "30360-02.htm";
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
      new _168_DeliverSupplies(168, "_168_DeliverSupplies", "");
   }
}
