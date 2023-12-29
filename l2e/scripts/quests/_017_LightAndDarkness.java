package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _017_LightAndDarkness extends Quest {
   public _017_LightAndDarkness(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31517);
      this.addTalkId(31517);
      this.addTalkId(31508);
      this.addTalkId(31509);
      this.addTalkId(31510);
      this.addTalkId(31511);
      this.questItemIds = new int[]{7168};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31517-02.htm")) {
            st.giveItems(7168, 4L);
            st.startQuest();
         } else if (event.equalsIgnoreCase("31508-02.htm")) {
            st.takeItems(7168, 1L);
            st.setCond(2, true);
         } else if (event.equalsIgnoreCase("31509-02.htm")) {
            st.takeItems(7168, 1L);
            st.setCond(3, true);
         } else if (event.equalsIgnoreCase("31510-02.htm")) {
            st.takeItems(7168, 1L);
            st.setCond(4, true);
         } else if (event.equalsIgnoreCase("31511-02.htm")) {
            st.takeItems(7168, 1L);
            st.setCond(5, true);
         }

         return event;
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
         switch(st.getState()) {
            case 0:
               QuestState st2 = player.getQuestState("_015_SweetWhisper");
               if (st2 == null || st2.getState() != 2) {
                  htmltext = "31517-02b.htm";
                  st.exitQuest(true);
               } else if (player.getLevel() >= 61) {
                  htmltext = "31517-00.htm";
               } else {
                  htmltext = "31517-02a.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 31508:
                     switch(cond) {
                        case 1:
                           if (st.getQuestItemsCount(7168) != 0L) {
                              htmltext = "31508-00.htm";
                           } else {
                              htmltext = "31508-02.htm";
                           }

                           return htmltext;
                        case 2:
                           return "31508-03.htm";
                        default:
                           return htmltext;
                     }
                  case 31509:
                     switch(cond) {
                        case 2:
                           if (st.getQuestItemsCount(7168) != 0L) {
                              htmltext = "31509-00.htm";
                           } else {
                              htmltext = "31509-02.htm";
                           }

                           return htmltext;
                        case 3:
                           return "31509-03.htm";
                        default:
                           return htmltext;
                     }
                  case 31510:
                     switch(cond) {
                        case 3:
                           if (st.getQuestItemsCount(7168) != 0L) {
                              htmltext = "31510-00.htm";
                           } else {
                              htmltext = "31510-02.htm";
                           }

                           return htmltext;
                        case 4:
                           return "31510-03.htm";
                        default:
                           return htmltext;
                     }
                  case 31511:
                     switch(cond) {
                        case 4:
                           if (st.getQuestItemsCount(7168) != 0L) {
                              htmltext = "31511-00.htm";
                           } else {
                              htmltext = "31511-02.htm";
                           }

                           return htmltext;
                        case 5:
                           return "31511-03.htm";
                        default:
                           return htmltext;
                     }
                  case 31512:
                  case 31513:
                  case 31514:
                  case 31515:
                  case 31516:
                  default:
                     return htmltext;
                  case 31517:
                     if (cond > 0 && cond < 5) {
                        if (st.getQuestItemsCount(7168) > 0L) {
                           htmltext = "31517-04.htm";
                        } else {
                           htmltext = "31517-05.htm";
                        }

                        return htmltext;
                     } else {
                        if (cond == 5 && st.getQuestItemsCount(7168) == 0L) {
                           htmltext = "31517-03.htm";
                           st.exitQuest(false, true);
                           return htmltext;
                        }

                        return htmltext;
                     }
               }
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _017_LightAndDarkness(17, _017_LightAndDarkness.class.getSimpleName(), "");
   }
}
