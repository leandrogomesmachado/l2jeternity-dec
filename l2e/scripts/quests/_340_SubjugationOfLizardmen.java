package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _340_SubjugationOfLizardmen extends Quest {
   private static final String qn = "_340_SubjugationOfLizardmen";
   private static final int WEISZ = 30385;
   private static final int ADONIUS = 30375;
   private static final int LEVIAN = 30037;
   private static final int CHEST = 30989;
   private static final int CARGO = 4255;
   private static final int HOLY = 4256;
   private static final int ROSARY = 4257;
   private static final int TOTEM = 4258;

   public _340_SubjugationOfLizardmen(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30385);
      this.addTalkId(new int[]{30385, 30375, 30037, 30989});
      this.addKillId(new int[]{20008, 20010, 20014, 20357, 21100, 20356, 21101, 25146});
      this.questItemIds = new int[]{4255, 4256, 4257, 4258};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_340_SubjugationOfLizardmen");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30385-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30385-07.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(4255, -1L);
         } else if (event.equalsIgnoreCase("30385-09.htm")) {
            st.takeItems(4255, -1L);
            st.rewardItems(57, 4090L);
         } else if (event.equalsIgnoreCase("30385-10.htm")) {
            st.takeItems(4255, -1L);
            st.rewardItems(57, 4090L);
            st.exitQuest(true);
         } else if (event.equalsIgnoreCase("30375-02.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30037-02.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30989-02.htm")) {
            st.set("cond", "6");
            st.giveItems(4258, 1L);
            st.playSound("ItemSound.quest_middle");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_340_SubjugationOfLizardmen");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() < 17) {
                  htmltext = "30385-01.htm";
                  st.exitQuest(true);
               } else {
                  htmltext = "30385-02.htm";
               }
               break;
            case 1:
               int cond = st.getInt("cond");
               switch(npc.getId()) {
                  case 30037:
                     if (cond == 4) {
                        return "30037-01.htm";
                     } else if (cond == 5) {
                        return "30037-03.htm";
                     } else {
                        if (cond == 6) {
                           htmltext = "30037-04.htm";
                           st.set("cond", "7");
                           st.playSound("ItemSound.quest_middle");
                           st.takeItems(4258, -1L);
                        } else if (cond == 7) {
                           htmltext = "30037-05.htm";
                           return htmltext;
                        }

                        return htmltext;
                     }
                  case 30375:
                     if (cond == 2) {
                        return "30375-01.htm";
                     } else if (cond == 3) {
                        if (st.hasQuestItems(4257) && st.hasQuestItems(4256)) {
                           htmltext = "30375-04.htm";
                           st.set("cond", "4");
                           st.playSound("ItemSound.quest_middle");
                           st.takeItems(4256, -1L);
                           st.takeItems(4257, -1L);
                        } else {
                           htmltext = "30375-03.htm";
                        }

                        return htmltext;
                     } else {
                        if (cond == 4) {
                           return "30375-05.htm";
                        }

                        return htmltext;
                     }
                  case 30385:
                     if (cond == 1) {
                        if (st.getQuestItemsCount(4255) < 30L) {
                           htmltext = "30385-05.htm";
                        } else {
                           htmltext = "30385-06.htm";
                        }

                        return htmltext;
                     } else {
                        if (cond == 2) {
                           htmltext = "30385-11.htm";
                        } else if (cond == 7) {
                           htmltext = "30385-13.htm";
                           st.rewardItems(57, 14700L);
                           st.playSound("ItemSound.quest_finish");
                           st.exitQuest(false);
                           return htmltext;
                        }

                        return htmltext;
                     }
                  case 30989:
                     if (cond == 5) {
                        htmltext = "30989-01.htm";
                     } else {
                        htmltext = "30989-03.htm";
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
      QuestState st = player.getQuestState("_340_SubjugationOfLizardmen");
      if (st == null) {
         return null;
      } else if (npc.getId() == 25146) {
         st.addSpawn(30989, npc, false, 30000);
         return null;
      } else {
         switch(npc.getId()) {
            case 20008:
            case 20010:
            case 20014:
               if (st.getInt("cond") == 1) {
                  st.dropItems(4255, 1, 30L, 400000);
               }
               break;
            case 20356:
            case 20357:
            case 21100:
            case 21101:
               if (st.getInt("cond") == 3) {
                  st.dropItems(4256, 1, 1L, 150000);
                  st.dropItems(4257, 1, 1L, 150000);
               }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _340_SubjugationOfLizardmen(340, "_340_SubjugationOfLizardmen", "");
   }
}
