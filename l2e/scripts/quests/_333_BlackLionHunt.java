package l2e.scripts.quests;

import l2e.commons.util.Rnd;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _333_BlackLionHunt extends Quest {
   private final int[][] _dropList = new int[][]{
      {20160, 1, 3848},
      {20171, 1, 3848},
      {20197, 1, 3848},
      {20200, 1, 3848},
      {20201, 1, 3848},
      {20202, 1, 3848},
      {20198, 1, 3848},
      {20207, 2, 3849},
      {20208, 2, 3849},
      {20209, 2, 3849},
      {20210, 2, 3849},
      {20211, 2, 3849},
      {20251, 3, 3850},
      {20252, 3, 3850},
      {20253, 3, 3850},
      {27151, 3, 3850},
      {20157, 4, 3851},
      {20230, 4, 3851},
      {20232, 4, 3851},
      {20234, 4, 3851},
      {27152, 4, 3851}
   };

   public _333_BlackLionHunt(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30735);
      this.addTalkId(new int[]{30735, 30736, 30471, 30130, 30531, 30737});

      for(int[] drop : this._dropList) {
         this.addKillId(drop[0]);
      }

      this.questItemIds = new int[]{3675, 3676, 3677, 3848, 3849, 3850, 3851, 3671, 3672, 3673, 3674};
   }

   public void giveRewards(QuestState st, int item, long count) {
      st.takeItems(item, count);
      st.calcRewardPerItem(this.getId(), 4, (int)count);
      if (count >= 20L && count < 50L) {
         st.calcReward(this.getId(), 1);
      } else if (count >= 50L && count < 100L) {
         st.calcReward(this.getId(), 2);
      } else if (count >= 100L) {
         st.calcReward(this.getId(), 3);
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         int part = st.getInt("part");
         if (event.equalsIgnoreCase("start")) {
            st.startQuest();
            htmltext = "30735-01.htm";
         } else if (event.equalsIgnoreCase("p1_t")) {
            st.set("part", "1");
            st.giveItems(3671, 1L);
            htmltext = "30735-02.htm";
         } else if (event.equalsIgnoreCase("p2_t")) {
            st.set("part", "2");
            st.giveItems(3672, 1L);
            htmltext = "30735-03.htm";
         } else if (event.equalsIgnoreCase("p3_t")) {
            st.set("part", "3");
            st.giveItems(3673, 1L);
            htmltext = "30735-04.htm";
         } else if (event.equalsIgnoreCase("p4_t")) {
            st.set("part", "4");
            st.giveItems(3674, 1L);
            htmltext = "30735-05.htm";
         } else if (event.equalsIgnoreCase("exit")) {
            st.exitQuest(true);
            htmltext = "30735-exit.htm";
         } else if (event.equalsIgnoreCase("continue")) {
            long claw = st.getQuestItemsCount(3675) / 10L;
            long check_eye = st.getQuestItemsCount(3676);
            if (claw > 0L) {
               st.giveItems(3676, claw);
               long eye = st.getQuestItemsCount(3676);
               st.takeItems(3675, claw * 10L);
               if (eye > 0L && eye < 5L) {
                  st.calcRewardPerItem(this.getId(), 5, (int)claw, true);
               } else if (eye >= 5L && eye < 9L) {
                  st.calcRewardPerItem(this.getId(), 6, (int)claw, true);
               } else if (eye >= 9L) {
                  st.calcRewardPerItem(this.getId(), 7, (int)claw, true);
               }

               if (check_eye > 0L) {
                  htmltext = "30735-06.htm";
               } else {
                  htmltext = "30735-06.htm";
               }
            } else {
               htmltext = "30735-start.htm";
            }
         } else if (event.equalsIgnoreCase("leave")) {
            int order;
            if (part == 1) {
               order = 3671;
            } else if (part == 2) {
               order = 3672;
            } else if (part == 3) {
               order = 3673;
            } else if (part == 4) {
               order = 3674;
            } else {
               order = 0;
            }

            st.set("part", "0");
            if (order > 0) {
               st.takeItems(order, 1L);
            }

            htmltext = "30735-07.htm";
         } else if (event.equalsIgnoreCase("f_info")) {
            int text = st.getInt("text");
            if (text < 4) {
               st.set("text", String.valueOf(text + 1));
               htmltext = "red_foor_text_" + getRandom(1, 19) + ".htm";
            } else {
               htmltext = "red_foor-01.htm";
            }
         } else if (event.equalsIgnoreCase("f_give")) {
            if (st.getQuestItemsCount(3440) > 0L) {
               if (st.getQuestItemsCount(57) >= 650L) {
                  st.takeItems(3440, 1L);
                  st.takeItems(57, 650L);
                  int rand = getRandom(1, 162);
                  if (rand < 21) {
                     st.giveItems(3444, 1L);
                     htmltext = "red_foor-02.htm";
                  } else if (rand < 41) {
                     st.giveItems(3445, 1L);
                     htmltext = "red_foor-03.htm";
                  } else if (rand < 61) {
                     st.giveItems(3446, 1L);
                     htmltext = "red_foor-04.htm";
                  } else if (rand < 74) {
                     st.giveItems(3447, 1L);
                     htmltext = "red_foor-05.htm";
                  } else if (rand < 86) {
                     st.giveItems(3448, 1L);
                     htmltext = "red_foor-06.htm";
                  } else if (rand < 98) {
                     st.giveItems(3449, 1L);
                     htmltext = "red_foor-07.htm";
                  } else if (rand < 99) {
                     st.giveItems(3450, 1L);
                     htmltext = "red_foor-08.htm";
                  } else if (rand < 109) {
                     st.giveItems(3451, 1L);
                     htmltext = "red_foor-09.htm";
                  } else if (rand < 119) {
                     st.giveItems(3452, 1L);
                     htmltext = "red_foor-10.htm";
                  } else if (rand < 123) {
                     st.giveItems(3453, 1L);
                     htmltext = "red_foor-11.htm";
                  } else if (rand < 127) {
                     st.giveItems(3454, 1L);
                     htmltext = "red_foor-12.htm";
                  } else if (rand < 131) {
                     st.giveItems(3455, 1L);
                     htmltext = "red_foor-13.htm";
                  } else if (rand < 132) {
                     st.giveItems(3456, 1L);
                     htmltext = "red_foor-13.htm";
                  } else if (rand < 147) {
                     int random_stat = getRandom(4);
                     if (random_stat == 3) {
                        st.giveItems(3457, 1L);
                        htmltext = "red_foor-14.htm";
                     } else if (random_stat == 0) {
                        st.giveItems(3458, 1L);
                        htmltext = "red_foor-14.htm";
                     } else if (random_stat == 1) {
                        st.giveItems(3459, 1L);
                        htmltext = "red_foor-14.htm";
                     } else if (random_stat == 2) {
                        st.giveItems(3460, 1L);
                        htmltext = "red_foor-14.htm";
                     }
                  } else if (rand <= 162) {
                     int random_tab = getRandom(4);
                     if (random_tab == 0) {
                        st.giveItems(3462, 1L);
                        htmltext = "red_foor-15.htm";
                     } else if (random_tab == 1) {
                        st.giveItems(3463, 1L);
                        htmltext = "red_foor-15.htm";
                     } else if (random_tab == 2) {
                        st.giveItems(3464, 1L);
                        htmltext = "red_foor-15.htm";
                     } else if (random_tab == 3) {
                        st.giveItems(3465, 1L);
                        htmltext = "red_foor-15.htm";
                     }
                  }
               } else {
                  htmltext = "red_foor-no_adena.htm";
               }
            } else {
               htmltext = "red_foor-no_box.htm";
            }
         } else if (event.equalsIgnoreCase("r_give_statue") || event.equalsIgnoreCase("r_give_tablet")) {
            int[] items = new int[]{3457, 3458, 3459, 3460};
            int item = 3461;
            String pieces = "rupio-01.htm";
            String brockes = "rupio-02.htm";
            String complete = "rupio-03.htm";
            if (event.equalsIgnoreCase("r_give_tablet")) {
               items = new int[]{3462, 3463, 3464, 3465};
               item = 3466;
               pieces = "rupio-04.htm";
               brockes = "rupio-05.htm";
               complete = "rupio-06.htm";
            }

            int count = 0;

            for(int id = items[0]; id <= items[items.length - 1]; ++id) {
               if (st.getQuestItemsCount(id) > 0L) {
                  ++count;
               }
            }

            if (count > 3) {
               for(int id = items[0]; id <= items[items.length - 1]; ++id) {
                  st.takeItems(id, 1L);
               }

               if (Rnd.chance(2)) {
                  st.giveItems(item, 1L);
               }
            }

            if (count < 4 && count != 0) {
               htmltext = pieces;
            } else {
               htmltext = "rupio-07.htm";
            }
         } else if (event.equalsIgnoreCase("l_give")) {
            if (st.getQuestItemsCount(3466) > 0L) {
               st.takeItems(3466, 1L);
               st.calcReward(this.getId(), 8);
               htmltext = "lockirin-01.htm";
            } else {
               htmltext = "lockirin-02.htm";
            }
         } else if (event.equalsIgnoreCase("u_give")) {
            if (st.getQuestItemsCount(3461) > 0L) {
               st.takeItems(3461, 1L);
               st.calcReward(this.getId(), 8);
               htmltext = "undiras-01.htm";
            } else {
               htmltext = "undiras-02.htm";
            }
         } else if (event.equalsIgnoreCase("m_give")) {
            if (st.getQuestItemsCount(3440) > 0L) {
               long coins = st.getQuestItemsCount(3677);
               long count = coins / 40L;
               if (count > 2L) {
                  count = 2L;
               }

               st.giveItems(3677, 1L);
               st.calcRewardPerItem(this.getId(), 9, (int)(1L + count));
               st.takeItems(3440, 1L);
               int rand = getRandom(0, 3);
               if (rand == 0) {
                  htmltext = "morgan-01.htm";
               } else if (rand == 1) {
                  htmltext = "morgan-02.htm";
               } else {
                  htmltext = "morgan-02.htm";
               }
            } else {
               htmltext = "morgan-03.htm";
            }
         } else if (event.equalsIgnoreCase("start_parts")) {
            htmltext = "30735-08.htm";
         } else if (event.equalsIgnoreCase("m_reward")) {
            htmltext = "morgan-05.htm";
         } else if (event.equalsIgnoreCase("u_info")) {
            htmltext = "undiras-03.htm";
         } else if (event.equalsIgnoreCase("l_info")) {
            htmltext = "lockirin-03.htm";
         } else if (event.equalsIgnoreCase("p_redfoot")) {
            htmltext = "30735-09.htm";
         } else if (event.equalsIgnoreCase("p_trader_info")) {
            htmltext = "30735-10.htm";
         } else if (event.equalsIgnoreCase("start_chose_parts")) {
            htmltext = "30735-11.htm";
         } else if (event.equalsIgnoreCase("p1_explanation")) {
            htmltext = "30735-12.htm";
         } else if (event.equalsIgnoreCase("p2_explanation")) {
            htmltext = "30735-13.htm";
         } else if (event.equalsIgnoreCase("p3_explanation")) {
            htmltext = "30735-14.htm";
         } else if (event.equalsIgnoreCase("p4_explanation")) {
            htmltext = "30735-15.htm";
         } else if (event.equalsIgnoreCase("f_more_help")) {
            htmltext = "red_foor-16.htm";
         } else if (event.equalsIgnoreCase("r_exit")) {
            htmltext = "30735-16.htm";
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
         int npcId = npc.getId();
         int cond = st.getCond();
         if (cond == 0) {
            st.set("cond", "0");
            st.set("part", "0");
            st.set("text", "0");
            if (npcId == 30735) {
               if (st.getQuestItemsCount(1369) > 0L) {
                  if (player.getLevel() >= 25 && player.getLevel() <= 39) {
                     htmltext = "30735-17.htm";
                  } else {
                     st.exitQuest(true);
                     htmltext = "30735-18.htm";
                  }
               } else {
                  st.exitQuest(true);
                  htmltext = "30735-19.htm";
               }
            }
         } else {
            int part = st.getInt("part");
            if (npcId == 30735) {
               int item;
               if (part == 1) {
                  item = 3848;
               } else if (part == 2) {
                  item = 3849;
               } else if (part == 3) {
                  item = 3850;
               } else {
                  if (part != 4) {
                     return "30735-20.htm";
                  }

                  item = 3851;
               }

               long count = st.getQuestItemsCount(item);
               long box = st.getQuestItemsCount(3440);
               if (box > 0L && count > 0L) {
                  this.giveRewards(st, item, count);
                  htmltext = "30735-21.htm";
               } else if (box > 0L) {
                  htmltext = "30735-22.htm";
               } else if (count > 0L) {
                  this.giveRewards(st, item, count);
                  htmltext = "30735-23.htm";
               } else {
                  htmltext = "30735-24.htm";
               }
            } else if (npcId == 30736) {
               if (st.getQuestItemsCount(3440) > 0L) {
                  htmltext = "red_foor_text_20.htm";
               } else {
                  htmltext = "red_foor_text_21.htm";
               }
            } else if (npcId == 30471) {
               int count = 0;

               for(int i = 3457; i <= 3460; ++i) {
                  if (st.getQuestItemsCount(i) > 0L) {
                     ++count;
                  }
               }

               for(int i = 3462; i <= 3465; ++i) {
                  if (st.getQuestItemsCount(i) > 0L) {
                     ++count;
                  }
               }

               if (count > 0) {
                  htmltext = "rupio-08.htm";
               } else {
                  htmltext = "rupio-07.htm";
               }
            } else if (npcId == 30130) {
               if (st.getQuestItemsCount(3461) > 0L) {
                  return "undiras-04.htm";
               }

               int count = 0;

               for(int i = 3457; i <= 3460; ++i) {
                  if (st.getQuestItemsCount(i) > 0L) {
                     ++count;
                  }
               }

               if (count > 0) {
                  htmltext = "undiras-05.htm";
               } else {
                  htmltext = "undiras-02.htm";
               }
            } else if (npcId == 30531) {
               if (st.getQuestItemsCount(3466) > 0L) {
                  return "lockirin-04.htm";
               }

               int count = 0;

               for(int i = 3462; i <= 3465; ++i) {
                  if (st.getQuestItemsCount(i) > 0L) {
                     ++count;
                  }
               }

               if (count > 0) {
                  htmltext = "lockirin-05.htm";
               } else {
                  htmltext = "lockirin-06.htm";
               }
            } else if (npcId == 30737) {
               if (st.getQuestItemsCount(3440) > 0L) {
                  htmltext = "morgan-06.htm";
               } else {
                  htmltext = "morgan-07.htm";
               }
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(player, 1);
      if (partyMember == null) {
         return super.onKill(npc, player, isSummon);
      } else {
         QuestState st = partyMember.getQuestState(this.getName());
         if (st != null) {
            int npcId = npc.getId();

            for(int[] element : this._dropList) {
               if (element[0] == npcId && st.getInt("part") == element[1]) {
                  st.calcDropItems(this.getId(), element[2], npc.getId(), Integer.MAX_VALUE);
                  st.calcDropItems(this.getId(), 3440, npc.getId(), Integer.MAX_VALUE);
               }
            }

            if (Rnd.chance(4) && (npcId == 20251 || npcId == 20252 || npcId == 20253)) {
               st.addSpawn(21105);
               st.addSpawn(21105);
            }

            if ((npcId == 20157 || npcId == 20230 || npcId == 20232 || npcId == 20234) && Rnd.chance(2)) {
               st.addSpawn(27152);
            }
         }

         return super.onKill(npc, player, isSummon);
      }
   }

   public static void main(String[] args) {
      new _333_BlackLionHunt(333, _333_BlackLionHunt.class.getSimpleName(), "");
   }
}
