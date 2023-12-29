package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _117_TheOceanOfDistantStars extends Quest {
   private static final String qn = "_117_TheOceanOfDistantStars";
   private static final int ABEY = 32053;
   private static final int GHOST = 32055;
   private static final int GHOST_F = 32054;
   private static final int OBI = 32052;
   private static final int BOX = 32076;
   private static final int GREY_STAR = 8495;
   private static final int ENGRAVED_HAMMER = 8488;
   private static final int BANDIT_WARRIOR = 22023;
   private static final int BANDIT_INSPECTOR = 22024;

   public _117_TheOceanOfDistantStars(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32053);
      this.addTalkId(new int[]{32053, 32055, 32054, 32052, 32076});
      this.addKillId(new int[]{22023, 22024});
      this.questItemIds = new int[]{8495, 8488};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_117_TheOceanOfDistantStars");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32053-02.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("32055-02.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32052-02.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32053-04.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32076-02.htm")) {
            st.set("cond", "5");
            st.giveItems(8488, 1L);
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32053-06.htm") && st.getQuestItemsCount(8488) == 1L) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32052-04.htm") && st.getQuestItemsCount(8488) == 1L) {
            st.set("cond", "7");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32052-06.htm") && st.getQuestItemsCount(8495) == 1L) {
            st.set("cond", "9");
            st.takeItems(8495, 1L);
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32055-04.htm") && st.getQuestItemsCount(8488) == 1L) {
            st.set("cond", "10");
            st.takeItems(8488, 1L);
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32054-03.htm")) {
            st.giveItems(57, 17647L);
            st.addExpAndSp(107387, 7369);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(false);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_117_TheOceanOfDistantStars");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 39) {
                  htmltext = "32053-01.htm";
               } else {
                  htmltext = "32053-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               int cond = st.getInt("cond");
               switch(npc.getId()) {
                  case 32052:
                     if (cond == 2) {
                        return "32052-01.htm";
                     } else if (cond >= 2 && cond <= 5) {
                        return "32052-02.htm";
                     } else if (cond == 6 && st.getQuestItemsCount(8488) == 1L) {
                        return "32052-03.htm";
                     } else if (cond == 7 && st.getQuestItemsCount(8488) == 1L) {
                        return "32052-04.htm";
                     } else {
                        if (cond == 8 && st.getQuestItemsCount(8495) == 1L) {
                           htmltext = "32052-05.htm";
                        } else if (cond >= 9) {
                           htmltext = "32052-06.htm";
                           return htmltext;
                        }

                        return htmltext;
                     }
                  case 32053:
                     if (cond == 1 || cond == 2) {
                        return "32053-02.htm";
                     } else if (cond == 3) {
                        return "32053-03.htm";
                     } else if (cond == 4) {
                        return "32053-04.htm";
                     } else {
                        if (cond == 5 && st.getQuestItemsCount(8488) == 1L) {
                           htmltext = "32053-05.htm";
                        } else if (cond >= 6 && st.getQuestItemsCount(8488) == 1L) {
                           return "32053-06.htm";
                        }

                        return htmltext;
                     }
                  case 32054:
                     if (cond == 10) {
                        htmltext = "32054-01.htm";
                     }

                     return htmltext;
                  case 32055:
                     if (cond == 1) {
                        return "32055-01.htm";
                     } else if (cond >= 2 && cond <= 8) {
                        return "32055-02.htm";
                     } else {
                        if (cond == 9 && st.getQuestItemsCount(8488) == 1L) {
                           htmltext = "32055-03.htm";
                        } else if (cond >= 10) {
                           return "32055-05.htm";
                        }

                        return htmltext;
                     }
                  case 32076:
                     if (cond == 4) {
                        htmltext = "32076-01.htm";
                     } else if (cond >= 5) {
                        return "32076-03.htm";
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

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_117_TheOceanOfDistantStars");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 7 && st.getRandom(10) < 2) {
            st.set("cond", "8");
            st.giveItems(8495, 1L);
            st.playSound("ItemSound.quest_itemget");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _117_TheOceanOfDistantStars(117, "_117_TheOceanOfDistantStars", "");
   }
}
