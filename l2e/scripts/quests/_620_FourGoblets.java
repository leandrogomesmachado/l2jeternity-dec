package l2e.scripts.quests;

import l2e.gameserver.instancemanager.FourSepulchersManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _620_FourGoblets extends Quest {
   private static final String qn = "_620_FourGoblets";
   private final int NAMELESS_SPIRIT = 31453;
   private final int GHOST_OF_WIGOTH_1 = 31452;
   private final int GHOST_OF_WIGOTH_2 = 31454;
   private final int CONQ_SM = 31921;
   private final int EMPER_SM = 31922;
   private final int SAGES_SM = 31923;
   private final int JUDGE_SM = 31924;
   private final int GHOST_CHAMBERLAIN_1 = 31919;
   private final int GHOST_CHAMBERLAIN_2 = 31920;
   private final int[] MOBS = new int[]{
      18120,
      18121,
      18122,
      18123,
      18124,
      18125,
      18126,
      18127,
      18128,
      18129,
      18130,
      18131,
      18132,
      18133,
      18134,
      18135,
      18136,
      18137,
      18138,
      18139,
      18140,
      18141,
      18142,
      18143,
      18144,
      18145,
      18146,
      18147,
      18148,
      18149,
      18150,
      18151,
      18152,
      18153,
      18154,
      18155
   };
   private final int[] TALKNPCS = new int[]{31452, 31454, 31921, 31922, 31923, 31924, 31919, 31920};
   private final int[] STARTNPCS = new int[]{31921, 31922, 31923, 31924, 31919, 31920};
   private final int GRAVE_PASS = 7261;
   private final int[] GOBLETS = new int[]{7256, 7257, 7258, 7259};
   private final int RELIC = 7254;
   private final int SEALED_BOX = 7255;
   private final int ANTIQUE_BROOCH = 7262;
   private final int[] RCP_REWARDS = new int[]{6881, 6883, 6885, 6887, 6891, 6893, 6895, 6897, 6899, 7580};

   public _620_FourGoblets(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31453);
      this.addTalkId(31453);

      for(int i = 0; i < this.TALKNPCS.length; ++i) {
         this.addTalkId(this.TALKNPCS[i]);
      }

      for(int i = 0; i < this.STARTNPCS.length; ++i) {
         this.addTalkId(this.STARTNPCS[i]);
      }

      for(int i = 0; i < this.MOBS.length; ++i) {
         this.addKillId(this.MOBS[i]);
      }

      this.questItemIds = new int[]{7262, 7255, 7256, 7257, 7258, 7259, 7261};
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_620_FourGoblets");
      if (st == null) {
         return event;
      } else if (event.equalsIgnoreCase("Enter")) {
         FourSepulchersManager.getInstance().tryEntry(npc, player);
         return null;
      } else {
         if (event.equalsIgnoreCase("accept")) {
            if (st.getInt("cond") == 0) {
               if (player.getLevel() >= 74) {
                  st.setState((byte)1);
                  st.playSound("ItemSound.quest_accept");
                  htmltext = "31453-13.htm";
                  st.set("cond", "1");
               } else {
                  htmltext = "31453-12.htm";
                  st.exitQuest(true);
               }
            }
         } else if (event.equalsIgnoreCase("11")) {
            if (st.getQuestItemsCount(7255) >= 1L) {
               htmltext = "31454-13.htm";
            }

            st.takeItems(7255, 1L);
            int reward = 0;
            int rnd = st.getRandom(5);
            if (rnd == 0) {
               st.giveItems(57, 10000L);
               reward = 1;
            } else if (rnd == 1) {
               if (st.getRandom(1000) < 848) {
                  reward = 1;
                  int i = st.getRandom(1000);
                  if (i < 43) {
                     st.giveItems(1884, 42L);
                  } else if (i < 66) {
                     st.giveItems(1895, 36L);
                  } else if (i < 184) {
                     st.giveItems(1876, 4L);
                  } else if (i < 250) {
                     st.giveItems(1881, 6L);
                  } else if (i < 287) {
                     st.giveItems(5549, 8L);
                  } else if (i < 484) {
                     st.giveItems(1874, 1L);
                  } else if (i < 681) {
                     st.giveItems(1889, 1L);
                  } else if (i < 799) {
                     st.giveItems(1877, 1L);
                  } else if (i < 902) {
                     st.giveItems(1894, 1L);
                  } else {
                     st.giveItems(4043, 1L);
                  }
               }

               if (st.getRandom(1000) < 323) {
                  reward = 1;
                  int i = st.getRandom(1000);
                  if (i < 335) {
                     st.giveItems(1888, 1L);
                  } else if (i < 556) {
                     st.giveItems(4040, 1L);
                  } else if (i < 725) {
                     st.giveItems(1890, 1L);
                  } else if (i < 872) {
                     st.giveItems(5550, 1L);
                  } else if (i < 962) {
                     st.giveItems(1893, 1L);
                  } else if (i < 986) {
                     st.giveItems(4046, 1L);
                  } else {
                     st.giveItems(4048, 1L);
                  }
               }
            } else if (rnd == 2) {
               if (st.getRandom(1000) < 847) {
                  reward = 1;
                  int i = st.getRandom(1000);
                  if (i < 148) {
                     st.giveItems(1878, 8L);
                  } else if (i < 175) {
                     st.giveItems(1882, 24L);
                  } else if (i < 273) {
                     st.giveItems(1879, 4L);
                  } else if (i < 322) {
                     st.giveItems(1880, 6L);
                  } else if (i < 357) {
                     st.giveItems(1885, 6L);
                  } else if (i < 554) {
                     st.giveItems(1875, 1L);
                  } else if (i < 685) {
                     st.giveItems(1883, 1L);
                  } else if (i < 803) {
                     st.giveItems(5220, 1L);
                  } else if (i < 901) {
                     st.giveItems(4039, 1L);
                  } else {
                     st.giveItems(4044, 1L);
                  }
               }

               if (st.getRandom(1000) < 251) {
                  reward = 1;
                  int i = st.getRandom(1000);
                  if (i < 350) {
                     st.giveItems(1887, 1L);
                  } else if (i < 587) {
                     st.giveItems(4042, 1L);
                  } else if (i < 798) {
                     st.giveItems(1886, 1L);
                  } else if (i < 922) {
                     st.giveItems(4041, 1L);
                  } else if (i < 966) {
                     st.giveItems(1892, 1L);
                  } else if (i < 996) {
                     st.giveItems(1891, 1L);
                  } else {
                     st.giveItems(4047, 1L);
                  }
               }
            } else if (rnd == 3) {
               if (st.getRandom(1000) < 31) {
                  reward = 1;
                  int i = st.getRandom(1000);
                  if (i < 223) {
                     st.giveItems(730, 1L);
                  } else if (i < 893) {
                     st.giveItems(948, 1L);
                  } else {
                     st.giveItems(960, 1L);
                  }
               }

               if (st.getRandom(1000) < 5) {
                  reward = 1;
                  int i = st.getRandom(1000);
                  if (i < 202) {
                     st.giveItems(729, 1L);
                  } else if (i < 928) {
                     st.giveItems(947, 1L);
                  } else {
                     st.giveItems(959, 1L);
                  }
               }
            } else if (rnd == 4) {
               if (st.getRandom(1000) < 329) {
                  reward = 1;
                  int i = st.getRandom(1000);
                  if (i < 88) {
                     st.giveItems(6698, 1L);
                  } else if (i < 185) {
                     st.giveItems(6699, 1L);
                  } else if (i < 238) {
                     st.giveItems(6700, 1L);
                  } else if (i < 262) {
                     st.giveItems(6701, 1L);
                  } else if (i < 292) {
                     st.giveItems(6702, 1L);
                  } else if (i < 356) {
                     st.giveItems(6703, 1L);
                  } else if (i < 420) {
                     st.giveItems(6704, 1L);
                  } else if (i < 482) {
                     st.giveItems(6705, 1L);
                  } else if (i < 554) {
                     st.giveItems(6706, 1L);
                  } else if (i < 576) {
                     st.giveItems(6707, 1L);
                  } else if (i < 640) {
                     st.giveItems(6708, 1L);
                  } else if (i < 704) {
                     st.giveItems(6709, 1L);
                  } else if (i < 777) {
                     st.giveItems(6710, 1L);
                  } else if (i < 799) {
                     st.giveItems(6711, 1L);
                  } else if (i < 863) {
                     st.giveItems(6712, 1L);
                  } else if (i < 927) {
                     st.giveItems(6713, 1L);
                  } else {
                     st.giveItems(6714, 1L);
                  }
               }

               if (st.getRandom(1000) < 54) {
                  reward = 1;
                  int i = st.getRandom(1000);
                  if (i < 100) {
                     st.giveItems(6688, 1L);
                  } else if (i < 198) {
                     st.giveItems(6689, 1L);
                  } else if (i < 298) {
                     st.giveItems(6690, 1L);
                  } else if (i < 398) {
                     st.giveItems(6691, 1L);
                  } else if (i < 499) {
                     st.giveItems(7579, 1L);
                  } else if (i < 601) {
                     st.giveItems(6693, 1L);
                  } else if (i < 703) {
                     st.giveItems(6694, 1L);
                  } else if (i < 801) {
                     st.giveItems(6695, 1L);
                  } else if (i < 902) {
                     st.giveItems(6696, 1L);
                  } else {
                     st.giveItems(6697, 1L);
                  }
               }
            }

            if (reward == 0) {
               if (st.getRandom(2) == 0) {
                  htmltext = "31454-14.htm";
               } else {
                  htmltext = "31454-15.htm";
               }
            }
         } else if (event.equalsIgnoreCase("12")) {
            if (st.getQuestItemsCount(this.GOBLETS[0]) >= 1L
               && st.getQuestItemsCount(this.GOBLETS[1]) >= 1L
               && st.getQuestItemsCount(this.GOBLETS[2]) >= 1L
               && st.getQuestItemsCount(this.GOBLETS[3]) >= 1L) {
               st.takeItems(this.GOBLETS[0], -1L);
               st.takeItems(this.GOBLETS[1], -1L);
               st.takeItems(this.GOBLETS[2], -1L);
               st.takeItems(this.GOBLETS[3], -1L);
               st.giveItems(7262, 1L);
               st.set("cond", "2");
               st.playSound("ItemSound.quest_finish");
               htmltext = "31453-16.htm";
            } else {
               htmltext = "31453-14.htm";
            }
         } else if (event.equalsIgnoreCase("13")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
            htmltext = "31453-18.htm";
         } else if (event.equalsIgnoreCase("14")) {
            htmltext = "31453-13.htm";
            if (st.getInt("cond") == 2) {
               htmltext = "31453-19.htm";
            }
         } else if (event.equalsIgnoreCase("15")) {
            if (st.getQuestItemsCount(7262) >= 1L) {
               player.teleToLocation(178298, -84574, -7216, true);
               htmltext = null;
            } else if (st.getQuestItemsCount(7261) >= 1L) {
               st.takeItems(7261, 1L);
               player.teleToLocation(178298, -84574, -7216, true);
               htmltext = null;
            } else {
               htmltext = "" + npc.getId() + "-0.htm";
            }
         } else if (event.equalsIgnoreCase("16")) {
            if (st.getQuestItemsCount(7262) >= 1L) {
               player.teleToLocation(186942, -75602, -2834, true);
               htmltext = null;
            } else if (st.getQuestItemsCount(7261) >= 1L) {
               st.takeItems(7261, 1L);
               player.teleToLocation(186942, -75602, -2834, true);
               htmltext = null;
            } else {
               htmltext = "" + npc.getId() + "-0.htm";
            }
         } else if (event.equalsIgnoreCase("17")) {
            if (st.getQuestItemsCount(7262) >= 1L) {
               player.teleToLocation(169590, -90218, -2914, true);
            } else {
               st.takeItems(7261, 1L);
               player.teleToLocation(169590, -90218, -2914, true);
            }

            htmltext = "31452-6.htm";
         } else if (event.equalsIgnoreCase("18")) {
            if (st.getQuestItemsCount(this.GOBLETS[0])
                  + st.getQuestItemsCount(this.GOBLETS[1])
                  + st.getQuestItemsCount(this.GOBLETS[2])
                  + st.getQuestItemsCount(this.GOBLETS[3])
               < 3L) {
               htmltext = "31452-3.htm";
            } else if (st.getQuestItemsCount(this.GOBLETS[0])
                  + st.getQuestItemsCount(this.GOBLETS[1])
                  + st.getQuestItemsCount(this.GOBLETS[2])
                  + st.getQuestItemsCount(this.GOBLETS[3])
               == 3L) {
               htmltext = "31452-4.htm";
            } else if (st.getQuestItemsCount(this.GOBLETS[0])
                  + st.getQuestItemsCount(this.GOBLETS[1])
                  + st.getQuestItemsCount(this.GOBLETS[2])
                  + st.getQuestItemsCount(this.GOBLETS[3])
               >= 4L) {
               htmltext = "31452-5.htm";
            }
         } else if (event.equalsIgnoreCase("19")) {
            if (st.getQuestItemsCount(7255) >= 1L) {
               htmltext = "31919-3.htm";
               st.takeItems(7255, 1L);
               int reward = 0;
               int rnd = st.getRandom(5);
               int i = 0;
               if (rnd == 0) {
                  st.giveItems(57, 10000L);
                  reward = 1;
               } else if (rnd == 1) {
                  if (st.getRandom(1000) < 848) {
                     reward = 1;
                     i = st.getRandom(1000);
                     if (i < 43) {
                        st.giveItems(1884, 42L);
                     } else if (i < 66) {
                        st.giveItems(1895, 36L);
                     } else if (i < 184) {
                        st.giveItems(1876, 4L);
                     } else if (i < 250) {
                        st.giveItems(1881, 6L);
                     } else if (i < 287) {
                        st.giveItems(5549, 8L);
                     } else if (i < 484) {
                        st.giveItems(1874, 1L);
                     } else if (i < 681) {
                        st.giveItems(1889, 1L);
                     } else if (i < 799) {
                        st.giveItems(1877, 1L);
                     } else if (i < 902) {
                        st.giveItems(1894, 1L);
                     } else {
                        st.giveItems(4043, 1L);
                     }
                  }

                  if (st.getRandom(1000) < 323) {
                     reward = 1;
                     i = st.getRandom(1000);
                     if (i < 335) {
                        st.giveItems(1888, 1L);
                     } else if (i < 556) {
                        st.giveItems(4040, 1L);
                     } else if (i < 725) {
                        st.giveItems(1890, 1L);
                     } else if (i < 872) {
                        st.giveItems(5550, 1L);
                     } else if (i < 962) {
                        st.giveItems(1893, 1L);
                     } else if (i < 986) {
                        st.giveItems(4046, 1L);
                     } else {
                        st.giveItems(4048, 1L);
                     }
                  }
               } else if (rnd == 2) {
                  if (st.getRandom(1000) < 847) {
                     reward = 1;
                     i = st.getRandom(1000);
                     if (i < 148) {
                        st.giveItems(1878, 8L);
                     } else if (i < 175) {
                        st.giveItems(1882, 24L);
                     } else if (i < 273) {
                        st.giveItems(1879, 4L);
                     } else if (i < 322) {
                        st.giveItems(1880, 6L);
                     } else if (i < 357) {
                        st.giveItems(1885, 6L);
                     } else if (i < 554) {
                        st.giveItems(1875, 1L);
                     } else if (i < 685) {
                        st.giveItems(1883, 1L);
                     } else if (i < 803) {
                        st.giveItems(5220, 1L);
                     } else if (i < 901) {
                        st.giveItems(4039, 1L);
                     } else {
                        st.giveItems(4044, 1L);
                     }
                  }

                  if (st.getRandom(1000) < 251) {
                     reward = 1;
                     i = st.getRandom(1000);
                     if (i < 350) {
                        st.giveItems(1887, 1L);
                     } else if (i < 587) {
                        st.giveItems(4042, 1L);
                     } else if (i < 798) {
                        st.giveItems(1886, 1L);
                     } else if (i < 922) {
                        st.giveItems(4041, 1L);
                     } else if (i < 966) {
                        st.giveItems(1892, 1L);
                     } else if (i < 996) {
                        st.giveItems(1891, 1L);
                     } else {
                        st.giveItems(4047, 1L);
                     }
                  }
               } else if (rnd == 3) {
                  if (st.getRandom(1000) < 31) {
                     reward = 1;
                     i = st.getRandom(1000);
                     if (i < 223) {
                        st.giveItems(730, 1L);
                     } else if (i < 893) {
                        st.giveItems(948, 1L);
                     } else {
                        st.giveItems(960, 1L);
                     }
                  }

                  if (st.getRandom(1000) < 5) {
                     reward = 1;
                     i = st.getRandom(1000);
                     if (i < 202) {
                        st.giveItems(729, 1L);
                     } else if (i < 928) {
                        st.giveItems(947, 1L);
                     } else {
                        st.giveItems(959, 1L);
                     }
                  }
               } else if (rnd == 4) {
                  if (st.getRandom(1000) < 329) {
                     reward = 1;
                     i = st.getRandom(1000);
                     if (i < 88) {
                        st.giveItems(6698, 1L);
                     } else if (i < 185) {
                        st.giveItems(6699, 1L);
                     } else if (i < 238) {
                        st.giveItems(6700, 1L);
                     } else if (i < 262) {
                        st.giveItems(6701, 1L);
                     } else if (i < 292) {
                        st.giveItems(6702, 1L);
                     } else if (i < 356) {
                        st.giveItems(6703, 1L);
                     } else if (i < 420) {
                        st.giveItems(6704, 1L);
                     } else if (i < 482) {
                        st.giveItems(6705, 1L);
                     } else if (i < 554) {
                        st.giveItems(6706, 1L);
                     } else if (i < 576) {
                        st.giveItems(6707, 1L);
                     } else if (i < 640) {
                        st.giveItems(6708, 1L);
                     } else if (i < 704) {
                        st.giveItems(6709, 1L);
                     } else if (i < 777) {
                        st.giveItems(6710, 1L);
                     } else if (i < 799) {
                        st.giveItems(6711, 1L);
                     } else if (i < 863) {
                        st.giveItems(6712, 1L);
                     } else if (i < 927) {
                        st.giveItems(6713, 1L);
                     } else {
                        st.giveItems(6714, 1L);
                     }
                  }

                  if (st.getRandom(1000) < 54) {
                     reward = 1;
                     i = st.getRandom(1000);
                     if (i < 100) {
                        st.giveItems(6688, 1L);
                     } else if (i < 198) {
                        st.giveItems(6689, 1L);
                     } else if (i < 298) {
                        st.giveItems(6690, 1L);
                     } else if (i < 398) {
                        st.giveItems(6691, 1L);
                     } else if (i < 499) {
                        st.giveItems(7579, 1L);
                     } else if (i < 601) {
                        st.giveItems(6693, 1L);
                     } else if (i < 703) {
                        st.giveItems(6694, 1L);
                     } else if (i < 801) {
                        st.giveItems(6695, 1L);
                     } else if (i < 902) {
                        st.giveItems(6696, 1L);
                     } else {
                        st.giveItems(6697, 1L);
                     }
                  }
               }

               if (reward == 0) {
                  if (st.getRandom(2) == 0) {
                     htmltext = "31919-4.htm";
                  } else {
                     htmltext = "31919-5.htm";
                  }
               }
            } else {
               htmltext = "31919-6.htm";
            }
         } else {
            try {
               int eventNumber = Integer.parseInt(event);
               if (this.checkArray(this.RCP_REWARDS, eventNumber)) {
                  st.takeItems(7254, 1000L);
                  st.giveItems(eventNumber, 1L);
                  htmltext = "31454-17.htm";
               }
            } catch (NumberFormatException var9) {
            }
         }

         return htmltext;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_620_FourGoblets");
      int id = st.getState();
      if (id == 0) {
         st.set("cond", "0");
      }

      int npcId = npc.getId();
      int cond = st.getInt("cond");
      if (npcId == 31453) {
         if (cond == 0) {
            if (player.getLevel() >= 74) {
               htmltext = "31453-1.htm";
            } else {
               htmltext = "31453-12.htm";
               st.exitQuest(true);
            }
         } else if (cond == 1) {
            if (st.getQuestItemsCount(this.GOBLETS[0]) >= 1L
               && st.getQuestItemsCount(this.GOBLETS[1]) >= 1L
               && st.getQuestItemsCount(this.GOBLETS[2]) >= 1L
               && st.getQuestItemsCount(this.GOBLETS[3]) >= 1L) {
               htmltext = "31453-15.htm";
            } else {
               htmltext = "31453-14.htm";
            }
         } else if (cond == 2) {
            htmltext = "31453-17.htm";
         }
      } else if (npcId == 31452) {
         if (cond == 1) {
            if (st.getQuestItemsCount(this.GOBLETS[0])
                  + st.getQuestItemsCount(this.GOBLETS[1])
                  + st.getQuestItemsCount(this.GOBLETS[2])
                  + st.getQuestItemsCount(this.GOBLETS[3])
               == 1L) {
               htmltext = "31452-1.htm";
            } else if (st.getQuestItemsCount(this.GOBLETS[0])
                  + st.getQuestItemsCount(this.GOBLETS[1])
                  + st.getQuestItemsCount(this.GOBLETS[2])
                  + st.getQuestItemsCount(this.GOBLETS[3])
               > 1L) {
               htmltext = "31452-2.htm";
            }
         } else if (cond == 2) {
            htmltext = "31452-2.htm";
         }
      } else if (npcId == 31454) {
         if (st.getQuestItemsCount(7254) >= 1000L) {
            if (st.getQuestItemsCount(7255) >= 1L) {
               if (st.getQuestItemsCount(this.GOBLETS[0]) >= 1L
                  && st.getQuestItemsCount(this.GOBLETS[1]) >= 1L
                  && st.getQuestItemsCount(this.GOBLETS[2]) >= 1L
                  && st.getQuestItemsCount(this.GOBLETS[3]) >= 1L) {
                  htmltext = "31454-4.htm";
               } else if (st.getQuestItemsCount(this.GOBLETS[0])
                     + st.getQuestItemsCount(this.GOBLETS[1])
                     + st.getQuestItemsCount(this.GOBLETS[2])
                     + st.getQuestItemsCount(this.GOBLETS[3])
                  > 1L) {
                  htmltext = "31454-8.htm";
               } else {
                  htmltext = "31454-12.htm";
               }
            } else if (st.getQuestItemsCount(this.GOBLETS[0]) >= 1L
               && st.getQuestItemsCount(this.GOBLETS[1]) >= 1L
               && st.getQuestItemsCount(this.GOBLETS[2]) >= 1L
               && st.getQuestItemsCount(this.GOBLETS[3]) >= 1L) {
               htmltext = "31454-3.htm";
            } else if (st.getQuestItemsCount(this.GOBLETS[0])
                  + st.getQuestItemsCount(this.GOBLETS[1])
                  + st.getQuestItemsCount(this.GOBLETS[2])
                  + st.getQuestItemsCount(this.GOBLETS[3])
               > 1L) {
               htmltext = "31454-7.htm";
            } else {
               htmltext = "31454-11.htm";
            }
         } else if (st.getQuestItemsCount(7255) >= 1L) {
            if (st.getQuestItemsCount(this.GOBLETS[0]) >= 1L
               && st.getQuestItemsCount(this.GOBLETS[1]) >= 1L
               && st.getQuestItemsCount(this.GOBLETS[2]) >= 1L
               && st.getQuestItemsCount(this.GOBLETS[3]) >= 1L) {
               htmltext = "31454-2.htm";
            } else if (st.getQuestItemsCount(this.GOBLETS[0])
                  + st.getQuestItemsCount(this.GOBLETS[1])
                  + st.getQuestItemsCount(this.GOBLETS[2])
                  + st.getQuestItemsCount(this.GOBLETS[3])
               > 1L) {
               htmltext = "31454-6.htm";
            } else {
               htmltext = "31454-10.htm";
            }
         } else if (st.getQuestItemsCount(this.GOBLETS[0]) >= 1L
            && st.getQuestItemsCount(this.GOBLETS[1]) >= 1L
            && st.getQuestItemsCount(this.GOBLETS[2]) >= 1L
            && st.getQuestItemsCount(this.GOBLETS[3]) >= 1L) {
            htmltext = "31454-1.htm";
         } else if (st.getQuestItemsCount(this.GOBLETS[0])
               + st.getQuestItemsCount(this.GOBLETS[1])
               + st.getQuestItemsCount(this.GOBLETS[2])
               + st.getQuestItemsCount(this.GOBLETS[3])
            > 1L) {
            htmltext = "31454-5.htm";
         } else {
            htmltext = "31454-9.htm";
         }
      } else if (npcId == 31921) {
         htmltext = "31921-E.htm";
      } else if (npcId == 31922) {
         htmltext = "31922-E.htm";
      } else if (npcId == 31923) {
         htmltext = "31923-E.htm";
      } else if (npcId == 31924) {
         htmltext = "31924-E.htm";
      } else if (npcId == 31919) {
         htmltext = "31919-1.htm";
      }

      return htmltext;
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_620_FourGoblets");
      int npcId = npc.getId();
      if (st != null) {
         int cond = st.getInt("cond");
         if ((cond == 1 || cond == 2) && this.checkArray(this.MOBS, npcId) && st.getRandom(100) < 30) {
            st.giveItems(7255, 1L);
            st.playSound("ItemSound.quest_itemget");
         }
      }

      return null;
   }

   private boolean checkArray(int[] array, int value) {
      for(int i = 0; i < array.length; ++i) {
         if (array[i] == value) {
            return true;
         }
      }

      return false;
   }

   public static void main(String[] args) {
      new _620_FourGoblets(620, "_620_FourGoblets", "");
   }
}
