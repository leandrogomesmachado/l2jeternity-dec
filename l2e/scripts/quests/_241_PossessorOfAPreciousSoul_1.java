package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _241_PossessorOfAPreciousSoul_1 extends Quest {
   public _241_PossessorOfAPreciousSoul_1(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31739);
      this.addTalkId(new int[]{30692, 30753, 30754, 31042, 31272, 31336, 31739, 31740, 31742, 31743, 31744});
      this.addKillId(new int[]{20244, 20245, 20283, 21508, 21509, 21510, 21511, 21512, 27113, 20669});
      this.questItemIds = new int[]{7587, 7597, 7589, 7588, 7598, 7599};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         int cond = st.getCond();
         if (!player.isSubClassActive()) {
            return null;
         } else {
            if (event.equalsIgnoreCase("31739-4.htm")) {
               if (cond == 0) {
                  st.startQuest();
               }
            } else if (event.equalsIgnoreCase("30753-2.htm")) {
               if (cond == 1) {
                  st.setCond(2, true);
               }
            } else if (event.equalsIgnoreCase("30754-2.htm")) {
               if (cond == 2) {
                  st.setCond(3, true);
               }
            } else if (event.equalsIgnoreCase("31739-8.htm")) {
               if (cond == 4 && st.getQuestItemsCount(7587) > 0L) {
                  st.takeItems(7587, 1L);
                  st.setCond(5, true);
               }
            } else if (event.equalsIgnoreCase("31042-2.htm")) {
               if (cond == 5) {
                  st.setCond(6, true);
               }
            } else if (event.equalsIgnoreCase("31042-5.htm")) {
               if (cond == 7 && st.getQuestItemsCount(7597) >= 10L) {
                  st.takeItems(7597, 10L);
                  st.giveItems(7589, 1L);
                  st.setCond(8, true);
               }
            } else if (event.equalsIgnoreCase("31739-12.htm")) {
               if (cond == 8 && st.getQuestItemsCount(7589) > 0L) {
                  st.takeItems(7589, 1L);
                  st.setCond(9, true);
               }
            } else if (event.equalsIgnoreCase("30692-2.htm")) {
               if (cond == 9 && st.getQuestItemsCount(7588) <= 0L) {
                  st.giveItems(7588, 1L);
                  st.setCond(10, true);
               }
            } else if (event.equalsIgnoreCase("31739-15.htm")) {
               if (cond == 10 && st.getQuestItemsCount(7588) > 0L) {
                  st.takeItems(7588, 1L);
                  st.setCond(11, true);
               }
            } else if (event.equalsIgnoreCase("31742-2.htm")) {
               if (cond == 11) {
                  st.setCond(12, true);
               }
            } else if (event.equalsIgnoreCase("31744-2.htm")) {
               if (cond == 12) {
                  st.setCond(13, true);
               }
            } else if (event.equalsIgnoreCase("31336-2.htm")) {
               if (cond == 13) {
                  st.setCond(14, true);
               }
            } else if (event.equalsIgnoreCase("31336-5.htm")) {
               if (cond == 15 && st.getQuestItemsCount(7598) > 0L) {
                  st.takeItems(7598, 5L);
                  st.giveItems(7599, 1L);
                  st.setCond(16, true);
               }
            } else if (event.equalsIgnoreCase("31743-2.htm")) {
               if (cond == 16 && st.getQuestItemsCount(7599) > 0L) {
                  st.takeItems(7599, 1L);
                  st.setCond(17, true);
               }
            } else if (event.equalsIgnoreCase("31742-5.htm")) {
               if (cond == 17) {
                  st.setCond(18, true);
               }
            } else if (event.equalsIgnoreCase("31740-2.htm")) {
               if (cond == 18) {
                  st.setCond(19, true);
               }
            } else if (event.equalsIgnoreCase("31272-2.htm")) {
               if (cond == 19) {
                  st.setCond(20, true);
               }
            } else if (event.equalsIgnoreCase("31272-5.htm")) {
               if (cond == 20 && st.getQuestItemsCount(6029) >= 5L && st.getQuestItemsCount(6033) > 0L) {
                  st.takeItems(6029, 5L);
                  st.takeItems(6033, 1L);
                  st.setCond(21, true);
               } else {
                  htmltext = "31272-4.htm";
               }
            }

            return htmltext;
         }
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         byte id = st.getState();
         if (npcId != 31739 && id != 1) {
            return htmltext;
         } else {
            int cond = st.getCond();
            if (npcId == 31739) {
               if (cond == 0) {
                  if (id == 2) {
                     htmltext = getAlreadyCompletedMsg(player);
                  } else if (player.getLevel() >= 50 && player.isSubClassActive()) {
                     htmltext = "31739-1.htm";
                  } else {
                     htmltext = "31739-2.htm";
                     st.exitQuest(true);
                  }
               }

               if (!player.isSubClassActive()) {
                  htmltext = "sub.htm";
               } else {
                  switch(cond) {
                     case 1:
                        htmltext = "31739-5.htm";
                     case 2:
                     case 3:
                     case 6:
                     case 7:
                     default:
                        break;
                     case 4:
                        if (st.getQuestItemsCount(7587) == 1L) {
                           htmltext = "31739-6.htm";
                        }
                        break;
                     case 5:
                        htmltext = "31739-9.htm";
                        break;
                     case 8:
                        if (st.getQuestItemsCount(7589) == 1L) {
                           htmltext = "31739-11.htm";
                        }
                        break;
                     case 9:
                        htmltext = "31739-13.htm";
                        break;
                     case 10:
                        if (st.getQuestItemsCount(7588) == 1L) {
                           htmltext = "31739-14.htm";
                        }
                        break;
                     case 11:
                        htmltext = "31739-16.htm";
                  }
               }
            } else if (player.isSubClassActive()) {
               switch(npcId) {
                  case 30692:
                     switch(cond) {
                        case 9:
                           return "30692-1.htm";
                        case 10:
                           return "30692-3.htm";
                        default:
                           return htmltext;
                     }
                  case 30753:
                     switch(cond) {
                        case 1:
                           return "30753-1.htm";
                        case 2:
                           htmltext = "30753-3.htm";
                           return htmltext;
                        default:
                           return htmltext;
                     }
                  case 30754:
                     switch(cond) {
                        case 2:
                           return "30754-1.htm";
                        case 3:
                           return "30754-3.htm";
                        default:
                           return htmltext;
                     }
                  case 31042:
                     switch(cond) {
                        case 5:
                           return "31042-1.htm";
                        case 6:
                           return "31042-4.htm";
                        case 7:
                           if (st.getQuestItemsCount(7597) == 10L) {
                              htmltext = "31042-3.htm";
                           }

                           return htmltext;
                        case 8:
                           htmltext = "31042-6.htm";
                           return htmltext;
                        default:
                           return htmltext;
                     }
                  case 31272:
                     switch(cond) {
                        case 18:
                        case 19:
                        case 20:
                        case 21:
                           return "31272-7.htm";
                        default:
                           return htmltext;
                     }
                  case 31336:
                     switch(cond) {
                        case 13:
                           return "31336-1.htm";
                        case 14:
                           return "31336-4.htm";
                        case 15:
                           if (st.getQuestItemsCount(7598) == 5L) {
                              htmltext = "31336-3.htm";
                           }

                           return htmltext;
                        case 16:
                           return "31336-6.htm";
                        default:
                           return htmltext;
                     }
                  case 31740:
                     switch(cond) {
                        case 18:
                        case 19:
                        case 20:
                        case 21:
                           st.calcExpAndSp(this.getId());
                           st.calcReward(this.getId());
                           st.exitQuest(false, true);
                           return "31740-5.htm";
                        default:
                           return htmltext;
                     }
                  case 31742:
                     switch(cond) {
                        case 11:
                           return "31742-1.htm";
                        case 12:
                           return "31742-3.htm";
                        case 13:
                        case 14:
                        case 15:
                        case 16:
                        default:
                           return htmltext;
                        case 17:
                           return "31742-4.htm";
                        case 18:
                        case 19:
                        case 20:
                        case 21:
                           return "31742-6.htm";
                     }
                  case 31743:
                     switch(cond) {
                        case 16:
                           if (st.getQuestItemsCount(7599) == 1L) {
                              htmltext = "31743-1.htm";
                           }

                           return htmltext;
                        case 17:
                           htmltext = "31743-3.htm";
                           return htmltext;
                        default:
                           return htmltext;
                     }
                  case 31744:
                     switch(cond) {
                        case 12:
                           htmltext = "31744-1.htm";
                           break;
                        case 13:
                           htmltext = "31744-3.htm";
                     }
               }
            } else {
               htmltext = "sub.htm";
            }

            return htmltext;
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      switch(npc.getId()) {
         case 20244:
         case 20245:
         case 20283:
         case 20284:
            Player ptMember = this.getRandomPartyMember(killer, 6);
            if (ptMember != null) {
               QuestState st = ptMember.getQuestState(this.getName());
               if (st != null && st.calcDropItems(this.getId(), 7597, npc.getId(), 10)) {
                  st.setCond(7, true);
               }
            }
            break;
         case 20669:
            Player ptMember = this.getRandomPartyMember(killer, 14);
            if (ptMember != null) {
               QuestState st = ptMember.getQuestState(this.getName());
               if (st != null && st.calcDropItems(this.getId(), 7598, npc.getId(), 5)) {
                  st.setCond(15, true);
               }
            }
            break;
         case 27113:
            Player ptMember = this.getRandomPartyMember(killer, 3);
            if (ptMember != null) {
               QuestState st = ptMember.getQuestState(this.getName());
               if (st != null && st.calcDropItems(this.getId(), 7587, npc.getId(), 1)) {
                  st.setCond(4, true);
               }
            }
      }

      return super.onKill(npc, killer, isSummon);
   }

   public static void main(String[] args) {
      new _241_PossessorOfAPreciousSoul_1(241, _241_PossessorOfAPreciousSoul_1.class.getSimpleName(), "");
   }
}
