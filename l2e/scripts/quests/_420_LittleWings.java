package l2e.scripts.quests;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _420_LittleWings extends Quest {
   private static final String qn = "_420_LittleWings";
   private static final int REQUIRED_EGGS = 20;
   private static final int BACK_DROP = 40;
   private static final int EGG_DROP = 50;
   private static final int FRY_STN = 3816;
   private static final int FRY_STN_DLX = 3817;
   private static final int FSN_LIST = 3818;
   private static final int FSN_LIST_DLX = 3819;
   private static final int TD_BCK_SKN = 3820;
   private static final int JUICE = 3821;
   private static final int SCALE_1 = 3822;
   private static final int EX_EGG = 3823;
   private static final int SCALE_2 = 3824;
   private static final int ZW_EGG = 3825;
   private static final int SCALE_3 = 3826;
   private static final int KA_EGG = 3827;
   private static final int SCALE_4 = 3828;
   private static final int SU_EGG = 3829;
   private static final int SCALE_5 = 3830;
   private static final int SH_EGG = 3831;
   private static final int FRY_DUST = 3499;
   private static final int[] QUESTITEMS = new int[]{3816, 3817, 3818, 3819, 3820, 3821, 3822, 3823, 3824, 3825, 3826, 3827, 3828, 3829, 3830, 3831, 3499};
   private static final int PM_COOPER = 30829;
   private static final int SG_CRONOS = 30610;
   private static final int GD_BYRON = 30711;
   private static final int MC_MARIA = 30608;
   private static final int FR_MYMYU = 30747;
   private static final int DK_EXARION = 30748;
   private static final int DK_ZWOV = 30749;
   private static final int DK_KALIBRAN = 30750;
   private static final int WM_SUZET = 30751;
   private static final int WM_SHAMHAI = 30752;
   private static final int[] TALKERS = new int[]{30829, 30610, 30711, 30608, 30747, 30748, 30749, 30750, 30751, 30752};
   private static final int TD_LORD = 20231;
   private static final int LO_LZRD_W = 20580;
   private static final int MS_SPIDER = 20233;
   private static final int RD_SCVNGR = 20551;
   private static final int BO_OVERLD = 20270;
   private static final int DD_SEEKER = 20202;
   private static final int FLINE = 20589;
   private static final int LIELE = 20590;
   private static final int VL_TREANT = 20591;
   private static final int SATYR = 20592;
   private static final int UNICORN = 20593;
   private static final int FR_RUNNER = 20594;
   private static final int FL_ELDER = 20595;
   private static final int LI_ELDER = 20596;
   private static final int VT_ELDER = 20597;
   private static final int ST_ELDER = 20598;
   private static final int UN_ELDER = 20599;
   private static final int SPIRIT_TIMINIEL = 21797;
   private static final int[] TO_KILL_ID = new int[]{
      20231, 20580, 20233, 20551, 20270, 20202, 20589, 20590, 20591, 20592, 20593, 20594, 20595, 20596, 20597, 20598, 20599, 21797
   };
   private static final int FOOD = 4038;
   private static final int ARMOR = 3912;

   public _420_LittleWings(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30829);

      for(int npcTalkerId : TALKERS) {
         this.addTalkId(npcTalkerId);
      }

      for(int npcToKillId : TO_KILL_ID) {
         this.addKillId(npcToKillId);
      }

      this.questItemIds = QUESTITEMS;
   }

   private String checkEggs(QuestState st, String npc, int progress) {
      String htmltext = null;
      int eggs = 0;
      int whom = st.getInt("dragon");
      if (whom == 1) {
         eggs = 3823;
      } else if (whom == 2) {
         eggs = 3825;
      } else if (whom == 3) {
         eggs = 3827;
      } else if (whom == 4) {
         eggs = 3829;
      } else if (whom == 5) {
         eggs = 3831;
      }

      if (npc.equalsIgnoreCase("mymyu")) {
         if ((progress == 19 || progress == 20) && st.getQuestItemsCount(eggs) == 1L) {
            htmltext = "420_" + npc + "_10.htm";
         } else if (st.getQuestItemsCount(eggs) >= 20L) {
            htmltext = "420_" + npc + "_9.htm";
         } else {
            htmltext = "420_" + npc + "_8.htm";
         }
      } else if (npc.equalsIgnoreCase("exarion") && whom == 1) {
         if (st.getQuestItemsCount(eggs) < 20L) {
            htmltext = "420_" + npc + "_3.htm";
         } else {
            st.takeItems(eggs, 20L);
            st.takeItems(3822, 1L);
            if (progress == 14 || progress == 21) {
               st.set("progress", "19");
            } else if (progress == 15 || progress == 22) {
               st.set("progress", "20");
            }

            st.giveItems(eggs, 1L);
            st.playSound("ItemSound.quest_itemget");
            st.set("cond", "7");
            htmltext = "420_" + npc + "_4.htm";
         }
      } else if (npc.equalsIgnoreCase("zwov") && whom == 2) {
         if (st.getQuestItemsCount(eggs) < 20L) {
            htmltext = "420_" + npc + "_3.htm";
         } else {
            st.takeItems(eggs, 20L);
            st.takeItems(3824, 1L);
            if (progress == 14 || progress == 21) {
               st.set("progress", "19");
            } else if (progress == 15 || progress == 22) {
               st.set("progress", "20");
            }

            st.giveItems(eggs, 1L);
            st.set("cond", "7");
            st.playSound("ItemSound.quest_itemget");
            htmltext = "420_" + npc + "_4.htm";
         }
      } else if (npc.equalsIgnoreCase("kalibran") && whom == 3) {
         if (st.getQuestItemsCount(eggs) < 20L) {
            htmltext = "420_" + npc + "_3.htm";
         } else {
            st.takeItems(eggs, 20L);
            htmltext = "420_" + npc + "_4.htm";
         }
      } else if (npc.equalsIgnoreCase("suzet") && whom == 4) {
         if (st.getQuestItemsCount(eggs) < 20L) {
            htmltext = "420_" + npc + "_4.htm";
         } else {
            st.takeItems(eggs, 20L);
            st.takeItems(3828, 1L);
            if (progress == 14 || progress == 21) {
               st.set("progress", "19");
            } else if (progress == 15 || progress == 22) {
               st.set("progress", "20");
            }

            st.giveItems(eggs, 1L);
            st.set("cond", "7");
            st.playSound("ItemSound.quest_itemget");
            htmltext = "420_" + npc + "_5.htm";
         }
      } else if (npc.equalsIgnoreCase("shamhai") && whom == 5) {
         if (st.getQuestItemsCount(eggs) < 20L) {
            htmltext = "420_" + npc + "_3.htm";
         } else {
            st.takeItems(eggs, 20L);
            st.takeItems(3830, 1L);
            if (progress == 14 || progress == 21) {
               st.set("progress", "19");
            } else if (progress == 15 || progress == 22) {
               st.set("progress", "20");
            }

            st.giveItems(eggs, 1L);
            st.set("cond", "7");
            st.playSound("ItemSound.quest_itemget");
            htmltext = "420_" + npc + "_4.htm";
         }
      }

      return htmltext;
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_420_LittleWings");
      if (st == null) {
         return null;
      } else {
         String htmltext = event;
         int state = st.getState();
         int progress = st.getInt("progress");
         int cond = st.getInt("cond");
         if (state == 0) {
            st.set("cond", "0");
            if (event.equalsIgnoreCase("ido")) {
               st.setState((byte)1);
               st.set("progress", "0");
               st.set("cond", "1");
               st.set("dragon", "0");
               st.playSound("ItemSound.quest_accept");
               htmltext = "Starting.htm";
            }
         } else if (state == 1 && cond < 5) {
            if (event.equalsIgnoreCase("wait")) {
               if (progress == 1 || progress == 2 || progress == 8 || progress == 9) {
                  if (progress != 1 && progress != 8) {
                     st.takeItems(2131, 1L);
                     st.takeItems(1873, 5L);
                     st.takeItems(1875, 1L);
                     st.takeItems(3820, 20L);
                     st.takeItems(3819, 1L);
                     st.giveItems(3817, 1L);
                     htmltext = "420_maria_5.htm";
                  } else {
                     st.takeItems(2130, 1L);
                     st.takeItems(1873, 3L);
                     st.takeItems(3820, 10L);
                     st.takeItems(3818, 1L);
                     st.giveItems(3816, 1L);
                     htmltext = "420_maria_3.htm";
                  }

                  st.takeItems(1870, 10L);
                  st.takeItems(1871, 10L);
                  st.playSound("ItemSound.quest_itemget");
               }
            } else if (event.equalsIgnoreCase("cronos_2")) {
               htmltext = "420_cronos_2.htm";
            } else if (event.equalsIgnoreCase("cronos_3")) {
               htmltext = "420_cronos_3.htm";
            } else if (event.equalsIgnoreCase("cronos_4")) {
               htmltext = "420_cronos_4.htm";
            } else if (event.equalsIgnoreCase("fsn")) {
               st.set("cond", "2");
               if (progress == 0) {
                  st.set("progress", "1");
                  st.giveItems(3818, 1L);
                  st.playSound("ItemSound.quest_itemget");
                  htmltext = "420_cronos_5.htm";
               } else if (progress == 7) {
                  st.set("progress", "8");
                  st.giveItems(3818, 1L);
                  st.playSound("ItemSound.quest_itemget");
                  htmltext = "420_cronos_12.htm";
               }
            } else if (event.equalsIgnoreCase("fsn_dlx")) {
               st.set("cond", "2");
               if (progress == 0) {
                  st.set("progress", "2");
                  st.giveItems(3819, 1L);
                  st.playSound("ItemSound.quest_itemget");
                  htmltext = "420_cronos_6.htm";
               } else if (progress == 7) {
                  st.set("progress", "9");
                  st.giveItems(3819, 1L);
                  st.playSound("ItemSound.quest_itemget");
                  htmltext = "420_cronos_13.htm";
               }
            } else if (event.equalsIgnoreCase("showfsn")) {
               htmltext = "420_byron_2.htm";
            } else if (event.equalsIgnoreCase("askmore")) {
               st.set("cond", "4");
               if (progress == 3) {
                  st.set("progress", "5");
                  htmltext = "420_byron_3.htm";
               } else if (progress == 4) {
                  st.set("progress", "6");
                  htmltext = "420_byron_4.htm";
               }
            } else if (event.equalsIgnoreCase("give_fsn")) {
               st.takeItems(3816, 1L);
               htmltext = "420_mymyu_2.htm";
            } else if (event.equalsIgnoreCase("give_fsn_dlx")) {
               st.takeItems(3817, 1L);
               st.giveItems(3499, 1L);
               st.playSound("ItemSound.quest_itemget");
               htmltext = "420_mymyu_4.htm";
            } else if (event.equalsIgnoreCase("fry_ask")) {
               htmltext = "420_mymyu_5.htm";
            } else if (event.equalsIgnoreCase("ask_abt")) {
               st.set("cond", "5");
               st.giveItems(3821, 1L);
               st.playSound("ItemSound.quest_itemget");
               htmltext = "420_mymyu_6.htm";
            }
         } else if (state == 1 && cond >= 5) {
            if (event.equalsIgnoreCase("exarion_1")) {
               st.giveItems(3822, 1L);
               st.playSound("ItemSound.quest_itemget");
               st.set("dragon", "1");
               st.set("cond", "6");
               st.set("progress", String.valueOf(progress + 9));
               htmltext = "420_exarion_2.htm";
            } else if (event.equalsIgnoreCase("kalibran_1")) {
               st.set("dragon", "3");
               st.set("cond", "6");
               st.giveItems(3826, 1L);
               st.playSound("ItemSound.quest_itemget");
               st.set("progress", String.valueOf(progress + 9));
               htmltext = "420_kalibran_2.htm";
            } else if (event.equalsIgnoreCase("kalibran_2")) {
               if (st.getQuestItemsCount(3826) > 0L) {
                  if (progress == 14 || progress == 21) {
                     st.set("progress", "19");
                  } else if (progress == 15 || progress == 22) {
                     st.set("progress", "20");
                  }

                  st.takeItems(3826, 1L);
                  st.giveItems(3827, 1L);
                  st.set("cond", "7");
                  st.playSound("ItemSound.quest_itemget");
                  htmltext = "420_kalibran_5.htm";
               }
            } else if (event.equalsIgnoreCase("zwov_1")) {
               st.set("dragon", "2");
               st.set("cond", "6");
               st.giveItems(3824, 1L);
               st.playSound("ItemSound.quest_itemget");
               st.set("progress", String.valueOf(progress + 9));
               htmltext = "420_zwov_2.htm";
            } else if (event.equalsIgnoreCase("shamhai_1")) {
               st.set("dragon", "5");
               st.set("cond", "6");
               st.giveItems(3830, 1L);
               st.playSound("ItemSound.quest_itemget");
               st.set("progress", String.valueOf(progress + 9));
               htmltext = "420_shamhai_2.htm";
            } else if (event.equalsIgnoreCase("suzet_1")) {
               htmltext = "420_suzet_2.htm";
            } else if (event.equalsIgnoreCase("suzet_2")) {
               st.set("dragon", "4");
               st.set("cond", "6");
               st.giveItems(3828, 1L);
               st.playSound("ItemSound.quest_itemget");
               st.set("progress", String.valueOf(progress + 9));
               htmltext = "420_suzet_3.htm";
            } else if (event.equalsIgnoreCase("hatch")) {
               int eggs = 0;
               int whom = st.getInt("dragon");
               if (whom == 1) {
                  eggs = 3823;
               } else if (whom == 2) {
                  eggs = 3825;
               } else if (whom == 3) {
                  eggs = 3827;
               } else if (whom == 4) {
                  eggs = 3829;
               } else if (whom == 5) {
                  eggs = 3831;
               }

               if (st.getQuestItemsCount(eggs) > 0L && (progress == 19 || progress == 20)) {
                  st.takeItems(eggs, 1L);
                  if (progress == 19) {
                     st.giveItems(3500 + st.getRandom(3), 1L);
                     st.exitQuest(true);
                     st.playSound("ItemSound.quest_finish");
                     htmltext = "420_mymyu_15.htm";
                  } else if (progress == 20) {
                     st.set("progress", "22");
                     htmltext = "420_mymyu_11.htm";
                  }
               }
            } else if (event.equalsIgnoreCase("give_dust")) {
               if (st.getQuestItemsCount(3499) > 0L) {
                  st.takeItems(3499, 1L);
                  int luck = st.getRandom(2);
                  int qty;
                  int extra;
                  if (luck == 0) {
                     extra = 3912;
                     qty = 1;
                     htmltext = "420_mymyu_13.htm";
                  } else {
                     extra = 4038;
                     qty = 100;
                     htmltext = "420_mymyu_14.htm";
                  }

                  st.giveItems(3500 + st.getRandom(3), 1L);
                  st.giveItems(extra, (long)qty);
                  st.exitQuest(true);
                  st.playSound("ItemSound.quest_finish");
               } else {
                  st.giveItems(3500 + st.getRandom(3), 1L);
                  st.exitQuest(true);
                  st.playSound("ItemSound.quest_finish");
                  htmltext = "420_mymyu_12.htm";
               }
            } else if (event.equalsIgnoreCase("no_dust")) {
               st.giveItems(3500 + st.getRandom(3), 1L);
               st.exitQuest(true);
               st.playSound("ItemSound.quest_finish");
               htmltext = "420_mymyu_12.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player talker) {
      String htmltext = Quest.getNoQuestMsg(talker);
      QuestState st = talker.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         int state = st.getState();
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         int progress = st.getInt("progress");
         long _coal = st.getQuestItemsCount(1870);
         long _char = st.getQuestItemsCount(1871);
         long _gemd = st.getQuestItemsCount(2130);
         long _gemc = st.getQuestItemsCount(2131);
         long _snug = st.getQuestItemsCount(1873);
         long _sofp = st.getQuestItemsCount(1875);
         long _tdbk = st.getQuestItemsCount(3820);
         if (state == 2) {
            st.setState((byte)0);
            state = 0;
         }

         if (npcId == 30829) {
            if (state == 0) {
               if (talker.getLevel() < 35) {
                  st.exitQuest(true);
                  htmltext = "420_low_level.htm";
               }

               htmltext = "Start.htm";
            } else if (state == 1 && cond < 5 && progress == 0) {
               htmltext = "Starting.htm";
            } else {
               htmltext = "Started.htm";
            }
         } else if (npcId == 30610) {
            if (state == 1 && cond < 5) {
               if (progress == 0) {
                  htmltext = "420_cronos_1.htm";
               } else if (progress == 1 || progress == 2 || progress == 8 || progress == 9) {
                  if (st.getQuestItemsCount(3816) == 1L) {
                     st.set("cond", "3");
                     if (progress == 1) {
                        st.set("progress", "3");
                        htmltext = "420_cronos_8.htm";
                     } else if (progress == 8) {
                        st.set("progress", "10");
                        htmltext = "420_cronos_14.htm";
                     }
                  } else if (st.getQuestItemsCount(3817) == 1L) {
                     if (progress == 2) {
                        st.set("progress", "4");
                        htmltext = "420_cronos_8.htm";
                     } else if (progress == 9) {
                        st.set("progress", "11");
                        htmltext = "420_cronos_14.htm";
                     }
                  } else {
                     htmltext = "420_cronos_7.htm";
                  }
               } else if (progress == 3 || progress == 4 || progress == 10 || progress == 11) {
                  htmltext = "420_cronos_9.htm";
               } else if (progress == 5 || progress == 6 || progress == 12 || progress == 13) {
                  htmltext = "420_cronos_11.htm";
               } else if (progress == 7) {
                  htmltext = "420_cronos_10.htm";
               }
            }
         } else if (npcId == 30608) {
            if (state == 1 && cond < 5) {
               if ((progress == 1 || progress == 8) && st.getQuestItemsCount(3818) == 1L
                  || (progress == 2 || progress == 9) && st.getQuestItemsCount(3819) == 1L) {
                  if (progress == 1 || progress == 8) {
                     htmltext = _coal >= 10L && _char >= 10L && _gemd >= 1L && _snug >= 3L && _tdbk >= 10L ? "420_maria_2.htm" : "420_maria_1.htm";
                  } else if (progress == 2 || progress == 9) {
                     htmltext = _coal >= 10L && _char >= 10L && _gemc >= 1L && _snug >= 5L && _sofp >= 1L && _tdbk >= 20L
                        ? "420_maria_4.htm"
                        : "420_maria_1.htm";
                  }
               } else if (progress >= 3 && progress <= 11) {
                  htmltext = "420_maria_6.htm";
               }
            }
         } else if (npcId == 30711) {
            if (state == 1 && cond < 5) {
               if ((progress != 1 && progress != 8 || st.getQuestItemsCount(3818) != 1L)
                  && (progress != 2 && progress != 9 || st.getQuestItemsCount(3819) != 1L)) {
                  if (progress == 7) {
                     htmltext = "420_byron_9.htm";
                  } else if ((progress != 3 || st.getQuestItemsCount(3816) != 1L) && (progress != 4 || st.getQuestItemsCount(3817) != 1L)) {
                     if (progress == 10 && st.getQuestItemsCount(3816) == 1L) {
                        st.set("progress", "12");
                        htmltext = "420_byron_5.htm";
                     } else if (progress == 11 && st.getQuestItemsCount(3817) == 1L) {
                        st.set("progress", "13");
                        htmltext = "420_byron_6.htm";
                     } else if (progress == 5 || progress == 12) {
                        htmltext = "420_byron_7.htm";
                     } else if (progress == 6 || progress == 13) {
                        htmltext = "420_byron_8.htm";
                     }
                  } else {
                     htmltext = "420_byron_1.htm";
                  }
               } else {
                  htmltext = "420_byron_10.htm";
               }
            }
         } else if (npcId == 30747) {
            if (state == 1 && cond < 5) {
               if (progress == 5 || progress == 12) {
                  htmltext = st.getQuestItemsCount(3816) == 1L ? "420_mymyu_1.htm" : "420_mymyu_5.htm";
               } else if (progress == 6 || progress == 13) {
                  htmltext = st.getQuestItemsCount(3817) == 1L ? "420_mymyu_3.htm" : "420_mymyu_5.htm";
               }
            } else if (state == 1 && cond >= 5) {
               if (progress < 14 && st.getQuestItemsCount(3821) == 1L) {
                  htmltext = "420_mymyu_7.htm";
               } else if (progress == 22) {
                  htmltext = "420_mymyu_11.htm";
               } else if (progress > 13) {
                  htmltext = this.checkEggs(st, "mymyu", progress);
               }
            }
         } else if (npcId == 30748) {
            if (state == 1 && cond >= 5) {
               if ((progress == 5 || progress == 6 || progress == 12 || progress == 13) && st.getQuestItemsCount(3821) == 1L) {
                  st.takeItems(3821, 1L);
                  htmltext = "420_exarion_1.htm";
               } else if (progress > 13 && st.getQuestItemsCount(3822) == 1L) {
                  htmltext = this.checkEggs(st, "exarion", progress);
               } else if ((progress == 19 || progress == 20) && st.getQuestItemsCount(3823) == 1L) {
                  htmltext = "420_exarion_5.htm";
               }
            }
         } else if (npcId == 30749) {
            if (state == 1 && cond >= 5) {
               if ((progress == 5 || progress == 6 || progress == 12 || progress == 13) && st.getQuestItemsCount(3821) == 1L) {
                  st.takeItems(3821, 1L);
                  htmltext = "420_zwov_1.htm";
               } else if (progress > 13 && st.getQuestItemsCount(3824) == 1L) {
                  htmltext = this.checkEggs(st, "zwov", progress);
               } else if ((progress == 19 || progress == 20) && st.getQuestItemsCount(3825) == 1L) {
                  htmltext = "420_zwov_5.htm";
               }
            }
         } else if (npcId == 30750) {
            if (state == 1 && cond >= 5) {
               if ((progress == 5 || progress == 6 || progress == 12 || progress == 13) && st.getQuestItemsCount(3821) == 1L) {
                  st.takeItems(3821, 1L);
                  htmltext = "420_kalibran_1.htm";
               } else if (progress > 13 && st.getQuestItemsCount(3826) == 1L) {
                  htmltext = this.checkEggs(st, "kalibran", progress);
               } else if ((progress == 19 || progress == 20) && st.getQuestItemsCount(3827) == 1L) {
                  htmltext = "420_kalibran_6.htm";
               }
            }
         } else if (npcId == 30751) {
            if (state == 1 && cond >= 5) {
               if ((progress == 5 || progress == 6 || progress == 12 || progress == 13) && st.getQuestItemsCount(3821) == 1L) {
                  st.takeItems(3821, 1L);
                  htmltext = "420_suzet_1.htm";
               } else if (progress > 13 && st.getQuestItemsCount(3828) == 1L) {
                  htmltext = this.checkEggs(st, "suzet", progress);
               } else if ((progress == 19 || progress == 20) && st.getQuestItemsCount(3829) == 1L) {
                  htmltext = "420_suzet_6.htm";
               }
            }
         } else if (npcId == 30752 && state == 1 && cond >= 5) {
            if ((progress == 5 || progress == 6 || progress == 12 || progress == 13) && st.getQuestItemsCount(3821) == 1L) {
               st.takeItems(3821, 1L);
               htmltext = "420_shamhai_1.htm";
            } else if (progress > 13 && st.getQuestItemsCount(3830) == 1L) {
               htmltext = this.checkEggs(st, "shamhai", progress);
            } else if ((progress == 19 || progress == 20) && st.getQuestItemsCount(3831) == 1L) {
               htmltext = "420_shamhai_5.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState st = killer.getQuestState(this.getName());
      if (st == null) {
         return super.onKill(npc, killer, isSummon);
      } else {
         int state = st.getState();
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         int whom = st.getInt("dragon");
         int progress = st.getInt("progress");
         long skins = st.getQuestItemsCount(3820);
         long fsn = st.getQuestItemsCount(3818);
         int eggs = 0;
         int eggDropper = 0;
         int scale = 0;
         if ((state != 1 || cond >= 5 || st.getQuestItemsCount(3818) != 1L || skins >= 10L) && (st.getQuestItemsCount(3819) != 1L || skins >= 20L)) {
            if (state == 1 && cond >= 5 && (progress == 14 || progress == 15 || progress == 21 || progress == 22)) {
               if (whom == 1) {
                  eggs = 3823;
                  scale = 3822;
                  eggDropper = 20580;
               } else if (whom == 2) {
                  eggs = 3825;
                  scale = 3824;
                  eggDropper = 20233;
               } else if (whom == 3) {
                  eggs = 3827;
                  scale = 3826;
                  eggDropper = 20551;
               } else if (whom == 4) {
                  eggs = 3829;
                  scale = 3828;
                  eggDropper = 20270;
               } else if (whom == 5) {
                  eggs = 3831;
                  scale = 3830;
                  eggDropper = 20202;
               }

               long prevItems = st.getQuestItemsCount(eggs);
               if (st.getQuestItemsCount(scale) == 1L && prevItems < 20L && npcId == eggDropper) {
                  st.dropQuestItems(eggs, 1, 1, 20L, false, 50.0F, true);
                  npc.broadcastNpcSay("If the eggs get taken, we're dead!");
               }
            } else if (state == 1 && cond < 5 && st.getQuestItemsCount(3817) == 1L && Util.contains(TO_KILL_ID, npcId) && npcId != 21797) {
               st.takeItems(3817, 1L);
               st.set("progress", "7");
               killer.sendMessage("You lost fairy stone deluxe!");
            }
         } else if (npcId == 20231) {
            long count = fsn == 1L ? 10L : 20L;
            st.dropQuestItems(3820, 1, 1, count, false, 40.0F, true);
         }

         return super.onKill(npc, killer, isSummon);
      }
   }

   public static void main(String[] args) {
      new _420_LittleWings(420, "_420_LittleWings", "");
   }
}
