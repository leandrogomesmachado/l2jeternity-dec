package l2e.scripts.quests;

import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _125_TheNameOfEvil1 extends Quest {
   public _125_TheNameOfEvil1(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(32114);
      this.addTalkId(new int[]{32114, 32117, 32119, 32120, 32121});
      this.addKillId(new int[]{22200, 22201, 22202, 22203, 22204, 22205, 22219, 22220, 22224});
      this.questItemIds = new int[]{8779, 8780};
   }

   private String getWordText32119(QuestState st) {
      String htmltext = "32119-04.htm";
      if (st.getInt("T32119") > 0 && st.getInt("E32119") > 0 && st.getInt("P32119") > 0 && st.getInt("U32119") > 0) {
         htmltext = "32119-09.htm";
      }

      return htmltext;
   }

   private String getWordText32120(QuestState st) {
      String htmltext = "32120-04.htm";
      if (st.getInt("T32120") > 0 && st.getInt("O32120") > 0 && st.getInt("O32120_2") > 0 && st.getInt("N32120") > 0) {
         htmltext = "32120-09.htm";
      }

      return htmltext;
   }

   private String getWordText32121(QuestState st) {
      String htmltext = "32121-04.htm";
      if (st.getInt("W32121") > 0 && st.getInt("A32121") > 0 && st.getInt("G32121") > 0 && st.getInt("U32121") > 0) {
         htmltext = "32121-09.htm";
      }

      return htmltext;
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32114-05.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("32114-12.htm")) {
            st.giveItems(8782, 1L);
            st.setCond(2, true);
         } else if (event.equalsIgnoreCase("32114-13.htm")) {
            st.setCond(2, true);
         } else if (event.equalsIgnoreCase("32117-08.htm")) {
            st.setCond(3, true);
         } else if (event.equalsIgnoreCase("32117-16.htm")) {
            st.setCond(5, true);
         } else if (event.equalsIgnoreCase("32119-20.htm")) {
            st.setCond(6, true);
         } else if (event.equalsIgnoreCase("32120-19.htm")) {
            st.setCond(7, true);
         } else if (event.equalsIgnoreCase("32121-23.htm")) {
            st.giveItems(8781, 1L);
            st.setCond(8, true);
         } else if (event.equalsIgnoreCase("T32119")) {
            htmltext = "32119-05.htm";
            if (st.getInt("T32119") < 1) {
               st.set("T32119", "1");
            }
         } else if (event.equalsIgnoreCase("E32119")) {
            htmltext = "32119-06.htm";
            if (st.getInt("E32119") < 1) {
               st.set("E32119", "1");
            }
         } else if (event.equalsIgnoreCase("P32119")) {
            htmltext = "32119-07.htm";
            if (st.getInt("P32119") < 1) {
               st.set("P32119", "1");
            }
         } else if (event.equalsIgnoreCase("U32119")) {
            if (st.getInt("U32119") < 1) {
               st.set("U32119", "1");
            }

            htmltext = this.getWordText32119(st);
         } else if (event.equalsIgnoreCase("T32120")) {
            htmltext = "32120-05.htm";
            if (st.getInt("T32120") < 1) {
               st.set("T32120", "1");
            }
         } else if (event.equalsIgnoreCase("O32120")) {
            htmltext = "32120-06.htm";
            if (st.getInt("O32120") < 1) {
               st.set("O32120", "1");
            }
         } else if (event.equalsIgnoreCase("O32120_2")) {
            htmltext = "32120-07.htm";
            if (st.getInt("O32120_2") < 1) {
               st.set("O32120_2", "1");
            }
         } else if (event.equalsIgnoreCase("N32120")) {
            if (st.getInt("N32120") < 1) {
               st.set("N32120", "1");
            }

            htmltext = this.getWordText32120(st);
         } else if (event.equalsIgnoreCase("W32121")) {
            htmltext = "32121-05.htm";
            if (st.getInt("W32121") < 1) {
               st.set("W32121", "1");
            }
         } else if (event.equalsIgnoreCase("A32121")) {
            htmltext = "32121-06.htm";
            if (st.getInt("A32121") < 1) {
               st.set("A32121", "1");
            }
         } else if (event.equalsIgnoreCase("G32121")) {
            htmltext = "32121-07.htm";
            if (st.getInt("G32121") < 1) {
               st.set("G32121", "1");
            }
         } else if (event.equalsIgnoreCase("U32121")) {
            if (st.getInt("U32121") < 1) {
               st.set("U32121", "1");
            }

            htmltext = this.getWordText32121(st);
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
         int cond = st.getCond();
         switch(npc.getId()) {
            case 32114:
               switch(st.getState()) {
                  case 0:
                     QuestState qs124 = player.getQuestState("_124_MeetingTheElroki");
                     if (qs124 != null && qs124.isCompleted()) {
                        htmltext = "32114-01.htm";
                     } else if (player.getLevel() < 76) {
                        htmltext = "32114-02.htm";
                        st.exitQuest(true);
                     } else {
                        htmltext = "32114-04.htm";
                        st.exitQuest(true);
                     }
                     break;
                  case 1:
                     if (cond == 1) {
                        htmltext = "32114-10.htm";
                     } else if (cond > 1 && cond < 8) {
                        htmltext = "32114-14.htm";
                     } else if (cond == 8) {
                        st.unset("T32119");
                        st.unset("E32119");
                        st.unset("P32119");
                        st.unset("U32119");
                        st.unset("T32120");
                        st.unset("O32120");
                        st.unset("O32120_2");
                        st.unset("N32120");
                        st.unset("W32121");
                        st.unset("A32121");
                        st.unset("G32121");
                        st.unset("U32121");
                        st.unset("cond");
                        htmltext = "32114-15.htm";
                        st.addExpAndSp(859195, 86603);
                        st.exitQuest(false, true);
                     }
                     break;
                  case 2:
                     htmltext = getAlreadyCompletedMsg(player);
               }
            case 32117:
               if (st.isStarted()) {
                  if (cond == 1) {
                     htmltext = "32117-02.htm";
                  } else if (cond == 2) {
                     htmltext = "32117-01.htm";
                  } else if (cond != 3 || st.getQuestItemsCount(8779) >= 2L && st.getQuestItemsCount(8780) >= 2L) {
                     if (cond == 3 && st.getQuestItemsCount(8779) == 2L && st.getQuestItemsCount(8780) == 2L) {
                        htmltext = "32117-11.htm";
                        st.takeItems(8779, 2L);
                        st.takeItems(8780, 2L);
                        st.set("cond", "4");
                        st.playSound("ItemSound.quest_middle");
                     } else if (cond > 4 && cond < 8) {
                        htmltext = "32117-19.htm";
                     } else if (cond == 8) {
                        htmltext = "32117-20.htm";
                     }
                  } else {
                     htmltext = "32117-12.htm";
                  }
               }
            case 32115:
            case 32116:
            case 32118:
            default:
               break;
            case 32119:
               if (st.isStarted()) {
                  if (cond == 5) {
                     htmltext = "32119-01.htm";
                  } else if (cond < 5) {
                     htmltext = "32119-02.htm";
                  } else if (cond > 5) {
                     htmltext = "32119-03.htm";
                  }
               }
               break;
            case 32120:
               if (st.isStarted()) {
                  if (cond == 6) {
                     htmltext = "32120-01.htm";
                  } else if (cond < 6) {
                     htmltext = "32120-02.htm";
                  } else if (cond > 6) {
                     htmltext = "32120-03.htm";
                  }
               }
               break;
            case 32121:
               if (st.isStarted()) {
                  if (cond == 7) {
                     htmltext = "32121-01.htm";
                  } else if (cond < 7) {
                     htmltext = "32121-02.htm";
                  } else if (cond > 7) {
                     htmltext = "32121-03.htm";
                  } else if (cond == 8) {
                     htmltext = "32121-24.htm";
                  }
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         if ((npcId >= 22200 && npcId <= 22202 || npcId == 22219 || npcId == 22224)
            && st.getQuestItemsCount(8779) < 2L
            && Rnd.calcChance((double)(10.0F * Config.RATE_QUEST_DROP))) {
            st.giveItems(8779, 1L);
            st.playSound("ItemSound.quest_middle");
         }

         if ((npcId >= 22203 && npcId <= 22205 || npcId == 22220 || npcId == 22225)
            && st.getQuestItemsCount(8780) < 2L
            && Rnd.calcChance((double)(10.0F * Config.RATE_QUEST_DROP))) {
            st.giveItems(8780, 1L);
            st.playSound("ItemSound.quest_middle");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _125_TheNameOfEvil1(125, _125_TheNameOfEvil1.class.getSimpleName(), "");
   }
}
