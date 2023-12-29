package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _217_TestimonyOfTrust extends Quest {
   private static final String qn = "_217_TestimonyOfTrust";
   private static final int MARK_OF_TRUST_ID = 2734;
   private static final int LETTER_TO_ELF_ID = 1558;
   private static final int LETTER_TO_DARKELF_ID = 1556;
   private static final int LETTER_TO_DWARF_ID = 2737;
   private static final int LETTER_TO_ORC_ID = 2738;
   private static final int LETTER_TO_SERESIN_ID = 2739;
   private static final int SCROLL_OF_DARKELF_TRUST_ID = 2740;
   private static final int SCROLL_OF_ELF_TRUST_ID = 2741;
   private static final int SCROLL_OF_DWARF_TRUST_ID = 2742;
   private static final int SCROLL_OF_ORC_TRUST_ID = 2743;
   private static final int RECOMMENDATION_OF_HOLLIN_ID = 2744;
   private static final int ORDER_OF_OZZY_ID = 2745;
   private static final int BREATH_OF_WINDS_ID = 2746;
   private static final int SEED_OF_VERDURE_ID = 2747;
   private static final int LETTER_OF_THIFIELL_ID = 2748;
   private static final int BLOOD_OF_GUARDIAN_BASILISK_ID = 2749;
   private static final int GIANT_APHID_ID = 2750;
   private static final int STAKATOS_FLUIDS_ID = 2751;
   private static final int BASILISK_PLASMA_ID = 2752;
   private static final int HONEY_DEW_ID = 2753;
   private static final int STAKATO_ICHOR_ID = 2754;
   private static final int ORDER_OF_CLAYTON_ID = 2755;
   private static final int PARASITE_OF_LOTA_ID = 2756;
   private static final int LETTER_TO_MANAKIA_ID = 2757;
   private static final int LETTER_OF_MANAKIA_ID = 2758;
   private static final int LETTER_TO_NICHOLA_ID = 2759;
   private static final int ORDER_OF_NICHOLA_ID = 2760;
   private static final int HEART_OF_PORTA_ID = 2761;
   private static final int RewardExp = 1390298;
   private static final int RewardSP = 92782;
   private static final int[] NPCS = new int[]{30191, 30031, 30154, 30358, 30464, 30515, 30531, 30565, 30621, 30657};
   private static final int[] MOBS = new int[]{
      20013, 20157, 20019, 20213, 20230, 20232, 20234, 20036, 20044, 27120, 27121, 20550, 20553, 20082, 20084, 20086, 20087, 20088
   };

   public _217_TestimonyOfTrust(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30191);

      for(int i : NPCS) {
         this.addTalkId(i);
      }

      for(int mob : MOBS) {
         this.addKillId(mob);
      }

      this.questItemIds = new int[]{
         2740,
         2741,
         2742,
         2743,
         2746,
         2747,
         2745,
         1558,
         2755,
         2752,
         2754,
         2753,
         1556,
         2748,
         2739,
         2738,
         2758,
         2757,
         2756,
         2737,
         2759,
         2761,
         2760,
         2744,
         2749,
         2751,
         2750
      };
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_217_TestimonyOfTrust");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30191-04.htm")) {
            htmltext = "30191-04.htm";
            st.set("cond", "1");
            st.set("id", "0");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1558, 1L);
            st.giveItems(1556, 1L);
         } else if (event.equalsIgnoreCase("30154-03.htm")) {
            st.takeItems(1558, 1L);
            st.giveItems(2745, 1L);
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30358-02.htm")) {
            st.takeItems(1556, 1L);
            st.giveItems(2748, 1L);
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30657-03.htm")) {
            if (player.getLevel() >= 38) {
               st.takeItems(2739, 1L);
               st.giveItems(2738, 1L);
               st.giveItems(2737, 1L);
               st.set("cond", "12");
               st.playSound("ItemSound.quest_middle");
            } else {
               htmltext = "30657-02.htm";
            }
         } else if (event.equalsIgnoreCase("30565-02.htm")) {
            st.takeItems(2738, 1L);
            st.giveItems(2757, 1L);
            st.set("cond", "13");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30515-02.htm")) {
            st.takeItems(2757, 1L);
            st.set("cond", "14");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30531-02.htm")) {
            st.takeItems(2737, 1L);
            st.giveItems(2759, 1L);
            st.set("cond", "18");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30621-02.htm")) {
            st.takeItems(2759, 1L);
            st.giveItems(2760, 1L);
            st.set("cond", "19");
            st.playSound("ItemSound.quest_middle");
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_217_TestimonyOfTrust");
      if (st == null) {
         return htmltext;
      } else {
         if (st.isCompleted()) {
            htmltext = getAlreadyCompletedMsg(player);
         }

         int cond = st.getInt("cond");
         int npcId = npc.getId();
         if (npcId == 30191) {
            if (cond == 0) {
               if (player.getRace().ordinal() == 0) {
                  if (player.getLevel() >= 37) {
                     htmltext = "30191-03.htm";
                  } else {
                     htmltext = "30191-01.htm";
                     st.exitQuest(true);
                  }
               } else {
                  htmltext = "30191-02.htm";
                  st.exitQuest(true);
               }
            } else if (cond == 9 && st.getQuestItemsCount(2741) > 0L && st.getQuestItemsCount(2740) > 0L) {
               htmltext = "30191-05.htm";
               st.takeItems(2740, 1L);
               st.takeItems(2741, 1L);
               st.giveItems(2739, 1L);
               st.set("cond", "10");
               st.playSound("Itemsound.quest_middle");
            } else if (cond == 22 && st.getQuestItemsCount(2742) > 0L && st.getQuestItemsCount(2743) > 0L) {
               htmltext = "30191-06.htm";
               st.takeItems(2742, 1L);
               st.takeItems(2743, 1L);
               st.giveItems(2744, 1L);
               st.set("cond", "23");
               st.playSound("Itemsound.quest_middle");
            } else if (cond == 19) {
               htmltext = "30191-07.htm";
            } else if (cond == 1) {
               htmltext = "30191-08.htm";
            } else if (cond == 8) {
               htmltext = "30191-09.htm";
            }
         } else if (npcId == 30154) {
            if (cond == 1 && st.getQuestItemsCount(1558) > 0L) {
               htmltext = "30154-01.htm";
            } else if (cond == 2 && st.getQuestItemsCount(2745) > 0L) {
               htmltext = "30154-04.htm";
            } else if (cond == 3 && st.getQuestItemsCount(2746) > 0L && st.getQuestItemsCount(2747) > 0L) {
               htmltext = "30154-05.htm";
               st.takeItems(2746, 1L);
               st.takeItems(2747, 1L);
               st.takeItems(2745, 1L);
               st.giveItems(2741, 1L);
               st.set("cond", "4");
               st.playSound("Itemsound.quest_middle");
            } else if (cond == 4) {
               htmltext = "30154-06.htm";
            }
         } else if (npcId == 30358) {
            if (cond == 4 && st.getQuestItemsCount(1556) > 0L) {
               htmltext = "30358-01.htm";
            } else if (cond == 8 && st.getQuestItemsCount(2754) + st.getQuestItemsCount(2753) + st.getQuestItemsCount(2752) == 3L) {
               st.takeItems(2752, 1L);
               st.takeItems(2754, 1L);
               st.takeItems(2753, 1L);
               st.giveItems(2740, 1L);
               st.set("cond", "9");
               st.playSound("Itemsound.quest_middle");
               htmltext = "30358-03.htm";
            } else if (cond == 7) {
               htmltext = "30358-04.htm";
            } else if (cond == 5) {
               htmltext = "30358-05.htm";
            }
         } else if (npcId == 30464) {
            if (cond == 5 && st.getQuestItemsCount(2748) > 0L) {
               htmltext = "30464-01.htm";
               st.takeItems(2748, 1L);
               st.giveItems(2755, 1L);
               st.set("cond", "6");
               st.playSound("Itemsound.quest_middle");
            } else if (cond == 6
               && st.getQuestItemsCount(2755) > 0L
               && st.getQuestItemsCount(2754) + st.getQuestItemsCount(2753) + st.getQuestItemsCount(2752) < 3L) {
               htmltext = "30464-02.htm";
            } else if (cond == 7
               && st.getQuestItemsCount(2755) > 0L
               && st.getQuestItemsCount(2754) + st.getQuestItemsCount(2753) + st.getQuestItemsCount(2752) == 3L) {
               st.takeItems(2755, 1L);
               st.set("cond", "8");
               st.playSound("Itemsound.quest_middle");
               htmltext = "30464-03.htm";
            }
         } else if (npcId == 30657) {
            if ((cond == 10 || cond == 11) && st.getQuestItemsCount(2739) > 0L && player.getLevel() >= 38) {
               htmltext = "30657-01.htm";
            } else if ((cond == 10 || cond == 11) && player.getLevel() < 38) {
               htmltext = "30657-02.htm";
               if (cond == 10) {
                  st.set("cond", "11");
               }

               st.playSound("Itemsound.quest_middle");
            } else if (cond == 18) {
               htmltext = "30657-05.htm";
            }
         } else if (npcId == 30565) {
            if (cond == 12 && st.getQuestItemsCount(2738) > 0L) {
               htmltext = "30565-01.htm";
            } else if (cond == 13) {
               htmltext = "30565-03.htm";
            } else if (cond == 16) {
               htmltext = "30565-04.htm";
               st.takeItems(2758, 1L);
               st.giveItems(2743, 1L);
               st.set("cond", "17");
               st.playSound("Itemsound.quest_middle");
            } else if (cond >= 17) {
               htmltext = "30565-05.htm";
            }
         } else if (npcId == 30515) {
            if (cond == 13 && st.getQuestItemsCount(2757) > 0L) {
               htmltext = "30515-01.htm";
            } else if (cond == 14 && st.getQuestItemsCount(2756) < 10L) {
               htmltext = "30515-03.htm";
            } else if (cond == 15 && st.getQuestItemsCount(2756) == 10L) {
               htmltext = "30515-04.htm";
               st.takeItems(2756, -1L);
               st.giveItems(2758, 1L);
               st.set("cond", "16");
               st.playSound("Itemsound.quest_middle");
            } else if (cond == 16) {
               htmltext = "30515-05.htm";
            }
         } else if (npcId == 30531) {
            if (cond == 17 && st.getQuestItemsCount(2737) > 0L) {
               htmltext = "30531-01.htm";
            } else if (cond == 18) {
               htmltext = "30531-03.htm";
            } else if (cond == 21) {
               htmltext = "30531-04.htm";
               st.giveItems(2742, 1L);
               st.set("cond", "22");
               st.playSound("Itemsound.quest_middle");
            } else if (cond == 22) {
               htmltext = "30531-05.htm";
            }
         } else if (npcId == 30621) {
            if (cond == 18 && st.getQuestItemsCount(2759) > 0L) {
               htmltext = "30621-01.htm";
            } else if (cond == 19 && st.getQuestItemsCount(2761) < 1L) {
               htmltext = "30621-03.htm";
            } else if (cond == 20 && st.getQuestItemsCount(2761) >= 1L) {
               htmltext = "30621-04.htm";
               st.takeItems(2761, 1L);
               st.takeItems(2760, 1L);
               st.set("cond", "21");
               st.playSound("Itemsound.quest_middle");
            } else if (cond == 21) {
               htmltext = "30621-05.htm";
            }
         } else if (npcId == 30031 && cond == 23 && st.getQuestItemsCount(2744) > 0L) {
            htmltext = "30031-01.htm";
            st.takeItems(2744, 1L);
            st.giveItems(2734, 1L);
            st.addExpAndSp(1390298, 92782);
            st.giveItems(57, 252212L);
            if (player.getVarInt("2ND_CLASS_DIAMOND_REWARD", 0) == 0) {
               st.giveItems(7562, 96L);
               player.setVar("2ND_CLASS_DIAMOND_REWARD", 1);
            }

            st.playSound("ItemSound.quest_finish");
            st.unset("cond");
            st.setState((byte)2);
            st.exitQuest(false);
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_217_TestimonyOfTrust");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         if (npcId != 20036 && npcId != 20044) {
            if (npcId != 20013 && npcId != 20019) {
               if (npcId == 27120) {
                  if (cond == 2 && st.getQuestItemsCount(2746) == 0L) {
                     if (st.getQuestItemsCount(2747) > 0L) {
                        st.giveItems(2746, 1L);
                        st.set("cond", "3");
                        st.playSound("Itemsound.quest_middle");
                     } else {
                        st.giveItems(2746, 1L);
                        st.playSound("Itemsound.quest_itemget");
                     }
                  }
               } else if (npcId == 27121) {
                  if (cond == 2 && st.getQuestItemsCount(2747) == 0L) {
                     if (st.getQuestItemsCount(2746) > 0L) {
                        st.giveItems(2747, 1L);
                        st.set("cond", "3");
                        st.playSound("Itemsound.quest_middle");
                     } else {
                        st.giveItems(2747, 1L);
                        st.playSound("Itemsound.quest_itemget");
                     }
                  }
               } else if (npcId == 20550) {
                  if (cond == 6 && st.getQuestItemsCount(2749) < 10L && st.getQuestItemsCount(2755) > 0L && st.getQuestItemsCount(2752) == 0L) {
                     if (st.getQuestItemsCount(2749) == 9L) {
                        st.takeItems(2749, -1L);
                        st.giveItems(2752, 1L);
                        if (st.getQuestItemsCount(2754) + st.getQuestItemsCount(2752) + st.getQuestItemsCount(2753) == 3L) {
                           st.set("cond", "7");
                        }

                        st.playSound("Itemsound.quest_middle");
                     } else {
                        st.giveItems(2749, 1L);
                        st.playSound("Itemsound.quest_itemget");
                     }
                  }
               } else if (npcId != 20157 && npcId != 20230 && npcId != 20232 && npcId != 20234) {
                  if (npcId != 20082 && npcId != 20086 && npcId != 20087 && npcId != 20084 && npcId != 20088) {
                     if (npcId == 20553) {
                        if (cond == 14 && st.getQuestItemsCount(2756) < 10L && getRandom(100) < 50) {
                           if (st.getQuestItemsCount(2756) == 9L) {
                              st.giveItems(2756, 1L);
                              st.set("cond", "15");
                              st.playSound("Itemsound.quest_middle");
                           } else {
                              st.giveItems(2756, 1L);
                              st.playSound("Itemsound.quest_itemget");
                           }
                        }
                     } else if (npcId == 20213 && cond == 19 && st.getQuestItemsCount(2761) < 1L) {
                        st.giveItems(2761, 1L);
                        st.set("cond", "20");
                        st.playSound("Itemsound.quest_middle");
                     }
                  } else if (cond == 6 && st.getQuestItemsCount(2750) < 10L && st.getQuestItemsCount(2755) > 0L && st.getQuestItemsCount(2753) == 0L) {
                     if (st.getQuestItemsCount(2750) == 9L) {
                        st.takeItems(2750, -1L);
                        st.giveItems(2753, 1L);
                        if (st.getQuestItemsCount(2754) + st.getQuestItemsCount(2752) + st.getQuestItemsCount(2753) == 3L) {
                           st.set("cond", "7");
                        }

                        st.playSound("Itemsound.quest_middle");
                     } else {
                        st.giveItems(2750, 1L);
                        st.playSound("Itemsound.quest_itemget");
                     }
                  }
               } else if (cond == 6 && st.getQuestItemsCount(2751) < 10L && st.getQuestItemsCount(2755) > 0L && st.getQuestItemsCount(2754) == 0L) {
                  if (st.getQuestItemsCount(2751) == 9L) {
                     st.takeItems(2751, -1L);
                     st.giveItems(2754, 1L);
                     if (st.getQuestItemsCount(2754) + st.getQuestItemsCount(2752) + st.getQuestItemsCount(2753) == 3L) {
                        st.set("cond", "7");
                     }

                     st.playSound("Itemsound.quest_middle");
                  } else {
                     st.giveItems(2751, 1L);
                     st.playSound("Itemsound.quest_itemget");
                  }
               }
            } else if (cond == 2 && st.getQuestItemsCount(2747) == 0L) {
               st.set("id", String.valueOf(st.getInt("id") + 1));
               if (getRandom(100) < st.getInt("id") * 33) {
                  st.addSpawn(27121, npc.getX(), npc.getY(), npc.getZ(), 60000);
                  st.playSound("Itemsound.quest_before_battle");
               }
            }
         } else if (cond == 2 && st.getQuestItemsCount(2746) == 0L) {
            st.set("id", String.valueOf(st.getInt("id") + 1));
            if (getRandom(100) < st.getInt("id") * 33) {
               st.addSpawn(27120, npc.getX(), npc.getY(), npc.getZ(), 60000);
               st.playSound("Itemsound.quest_before_battle");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _217_TestimonyOfTrust(217, "_217_TestimonyOfTrust", "");
   }
}
