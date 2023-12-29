package l2e.scripts.quests;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _212_TrialOfDuty extends Quest {
   private static final String qn = "_212_TrialOfDuty";
   private static final int HANNAVALT = 30109;
   private static final int DUSTIN = 30116;
   private static final int SIR_COLLIN_WINDAWOOD = 30311;
   private static final int SIR_ARON_TANFORD = 30653;
   private static final int SIR_KIEL_NIGHTHAWK = 30654;
   private static final int ISAEL_SILVERSHADOW = 30655;
   private static final int SPIRIT_OF_SIR_TALIANUS = 30656;
   private static final int[] TALKERS = new int[]{30109, 30116, 30311, 30653, 30654, 30655, 30656};
   private static final int HANGMAN_TREE = 20144;
   private static final int SKELETON_MARAUDER = 20190;
   private static final int SKELETON_RAIDER = 20191;
   private static final int STRAIN = 20200;
   private static final int GHOUL = 20201;
   private static final int BREKA_ORC_OVERLORD = 20270;
   private static final int SPIRIT_OF_SIR_HEROD = 27119;
   private static final int LETO_LIZARDMAN = 20577;
   private static final int LETO_LIZARDMAN_ARCHER = 20578;
   private static final int LETO_LIZARDMAN_SOLDIER = 20579;
   private static final int LETO_LIZARDMAN_WARRIOR = 20580;
   private static final int LETO_LIZARDMAN_SHAMAN = 20581;
   private static final int LETO_LIZARDMAN_OVERLORD = 20582;
   private static final int[] MOBS = new int[]{20144, 20190, 20191, 20200, 20201, 20270, 27119, 20577, 20578, 20579, 20580, 20581, 20582};
   private static final int LETTER_OF_DUSTIN = 2634;
   private static final int KNIGHTS_TEAR = 2635;
   private static final int MIRROR_OF_ORPIC = 2636;
   private static final int TEAR_OF_CONFESSION = 2637;
   private static final int REPORT_PIECE = 2638;
   private static final int TALIANUSS_REPORT = 2639;
   private static final int TEAR_OF_LOYALTY = 2640;
   private static final int MILITAS_ARTICLE = 2641;
   private static final int SAINTS_ASHES_URN = 2642;
   private static final int ATEBALTS_SKULL = 2643;
   private static final int ATEBALTS_RIBS = 2644;
   private static final int ATEBALTS_SHIN = 2645;
   private static final int LETTER_OF_WINDAWOOD = 2646;
   private static final int OLD_KNIGHT_SWORD = 3027;
   private static final int[] QUESTITEMS = new int[]{2634, 2635, 2636, 2637, 2638, 2639, 2640, 2641, 2642, 2643, 2644, 2645, 2646, 3027};
   private static final int MARK_OF_DUTY = 2633;
   private static final int[] CLASSES = new int[]{4, 19, 32};

   public _212_TrialOfDuty(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30109);

      for(int talkId : TALKERS) {
         this.addTalkId(talkId);
      }

      for(int mobId : MOBS) {
         this.addKillId(mobId);
      }

      this.questItemIds = QUESTITEMS;
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_212_TrialOfDuty");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("1")) {
            htmltext = "30109-04.htm";
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            st.set("cond", "1");
         } else if (event.equalsIgnoreCase("30116_1")) {
            htmltext = "30116-02.htm";
         } else if (event.equalsIgnoreCase("30116_2")) {
            htmltext = "30116-03.htm";
         } else if (event.equalsIgnoreCase("30116_3")) {
            htmltext = "30116-04.htm";
         } else if (event.equalsIgnoreCase("30116_4")) {
            htmltext = "30116-05.htm";
            st.takeItems(2640, 1L);
            st.set("cond", "14");
            st.playSound("ItemSound.quest_middle");
         }

         return htmltext;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player talker) {
      String htmltext = getNoQuestMsg(talker);
      QuestState st = talker.getQuestState("_212_TrialOfDuty");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         int npcId = npc.getId();
         int id = st.getState();
         if (npcId != 30109 && id != 1) {
            return htmltext;
         } else {
            if (id == 0) {
               st.set("cond", "0");
               st.set("onlyone", "0");
               st.set("id", "0");
            }

            if (npcId == 30109 && cond == 0 && st.getInt("onlyone") == 0) {
               if (Util.contains(CLASSES, talker.getClassId().getId())) {
                  if (talker.getLevel() >= 35) {
                     htmltext = "30109-03.htm";
                  } else {
                     htmltext = "30109-01.htm";
                     st.exitQuest(true);
                  }
               } else {
                  htmltext = "30109-02.htm";
                  st.exitQuest(true);
               }
            } else if (npcId == 30109 && cond == 0 && st.getInt("onlyone") == 1) {
               htmltext = Quest.getAlreadyCompletedMsg(talker);
            } else if (npcId == 30109 && cond == 18 && st.getQuestItemsCount(2634) > 0L) {
               htmltext = "30109-05.htm";
               st.set("onlyone", "1");
               st.set("cond", "0");
               st.takeItems(2634, 1L);
               st.addExpAndSp(381288, 24729);
               st.giveItems(57, 69484L);
               if (talker.getVarInt("2ND_CLASS_DIAMOND_REWARD", 0) == 0) {
                  st.giveItems(7562, 61L);
                  talker.setVar("2ND_CLASS_DIAMOND_REWARD", 1);
               }

               st.giveItems(2633, 1L);
               st.exitQuest(false);
               st.playSound("ItemSound.quest_finish");
            } else if (npcId == 30109 && cond == 1) {
               htmltext = "30109-04.htm";
            } else if (npcId == 30653 && cond == 1) {
               htmltext = "30653-01.htm";
               if (st.getQuestItemsCount(3027) == 0L) {
                  st.giveItems(3027, 1L);
               }

               st.set("cond", "2");
               st.playSound("ItemSound.quest_middle");
            } else if (npcId == 30653 && cond == 2 && st.getQuestItemsCount(2635) == 0L) {
               htmltext = "30653-02.htm";
            } else if (npcId == 30653 && cond == 3 && st.getQuestItemsCount(2635) > 0L) {
               htmltext = "30653-03.htm";
               st.takeItems(2635, 1L);
               st.takeItems(3027, 1L);
               st.set("cond", "4");
               st.playSound("ItemSound.quest_middle");
            } else if (npcId == 30653 && cond == 4) {
               htmltext = "30653-04.htm";
            } else if (npcId == 30654 && cond == 4) {
               htmltext = "30654-01.htm";
               st.set("cond", "5");
               st.playSound("ItemSound.quest_middle");
            } else if (npcId == 30654 && cond == 5 && st.getQuestItemsCount(2639) == 0L) {
               htmltext = "30654-02.htm";
            } else if (npcId == 30654 && cond == 6 && st.getQuestItemsCount(2639) > 0L) {
               htmltext = "30654-03.htm";
               st.set("cond", "7");
               st.playSound("ItemSound.quest_middle");
               st.giveItems(2636, 1L);
            } else if (npcId == 30654 && cond == 7) {
               htmltext = "30654-04.htm";
            } else if (npcId == 30654 && cond == 9 && st.getQuestItemsCount(2637) > 0L) {
               htmltext = "30654-05.htm";
               st.takeItems(2637, 1L);
               st.set("cond", "10");
               st.playSound("ItemSound.quest_middle");
            } else if (npcId == 30654 && cond == 10) {
               htmltext = "30654-06.htm";
            } else if (npcId == 30656 && cond == 8 && st.getQuestItemsCount(2636) > 0L) {
               htmltext = "30656-01.htm";
               st.takeItems(2636, 1L);
               st.takeItems(2639, 1L);
               st.giveItems(2637, 1L);
               st.set("cond", "9");
               st.playSound("ItemSound.quest_middle");
            } else if (npcId == 30655 && cond == 10) {
               if (talker.getLevel() >= 35) {
                  htmltext = "30655-02.htm";
                  st.set("cond", "11");
                  st.playSound("ItemSound.quest_middle");
               } else {
                  htmltext = "30655-01.htm";
               }
            } else if (npcId == 30655 && cond == 11) {
               htmltext = "30655-03.htm";
            } else if (npcId == 30655 && cond == 12) {
               htmltext = "30655-04.htm";
               st.takeItems(2641, st.getQuestItemsCount(2641));
               st.giveItems(2640, 1L);
               st.set("cond", "13");
               st.playSound("ItemSound.quest_middle");
            } else if (npcId == 30655 && cond == 13) {
               htmltext = "30655-05.htm";
            } else if (npcId == 30116 && cond == 13 && st.getQuestItemsCount(2640) > 0L) {
               htmltext = "30116-01.htm";
            } else if (npcId == 30116 && cond == 14) {
               htmltext = "30116-06.htm";
            } else if (npcId == 30116 && cond == 15) {
               htmltext = "30116-07.htm";
               st.takeItems(2643, 1L);
               st.takeItems(2644, 1L);
               st.takeItems(2645, 1L);
               st.giveItems(2642, 1L);
               st.set("cond", "16");
               st.playSound("ItemSound.quest_middle");
            } else if (npcId == 30116 && cond == 17) {
               htmltext = "30116-08.htm";
               st.takeItems(2646, 1L);
               st.giveItems(2634, 1L);
               st.set("cond", "18");
               st.playSound("ItemSound.quest_middle");
            } else if (npcId == 30116 && cond == 16) {
               htmltext = "30116-09.htm";
            } else if (npcId == 30116 && cond == 18) {
               htmltext = "30116-10.htm";
            } else if (npcId == 30311 && cond == 16 && st.getQuestItemsCount(2642) > 0L) {
               htmltext = "30311-01.htm";
               st.takeItems(2642, 1L);
               st.giveItems(2646, 1L);
               st.set("cond", "17");
               st.playSound("ItemSound.quest_middle");
            } else if (npcId == 30311 && cond == 14) {
               htmltext = "30311-02.htm";
            }

            return htmltext;
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState st = killer.getQuestState("_212_TrialOfDuty");
      if (st == null) {
         return null;
      } else {
         int cond = st.getInt("cond");
         int npcId = npc.getId();
         if (npcId != 20190 && npcId != 20191) {
            if (npcId == 27119) {
               if (cond == 2 && st.getQuestItemsCount(3027) > 0L) {
                  st.giveItems(2635, 1L);
                  st.playSound("ItemSound.quest_middle");
                  st.set("cond", "3");
               }
            } else if (npcId == 20200) {
               if (cond == 5 && st.getQuestItemsCount(2638) < 10L && st.getQuestItemsCount(2639) == 0L) {
                  if (st.getQuestItemsCount(2638) == 9L) {
                     if (st.getRandom(2) == 1) {
                        st.takeItems(2638, st.getQuestItemsCount(2638));
                        st.giveItems(2639, 1L);
                        st.playSound("ItemSound.quest_middle");
                        st.set("cond", "6");
                     }
                  } else if (st.getRandom(2) == 1) {
                     st.giveItems(2638, 1L);
                     st.playSound("ItemSound.quest_itemget");
                  }
               }
            } else if (npcId == 20201) {
               if (cond == 5 && st.getQuestItemsCount(2638) < 10L && st.getQuestItemsCount(2639) == 0L) {
                  if (st.getQuestItemsCount(2638) == 9L) {
                     if (st.getRandom(2) == 1) {
                        st.takeItems(2638, st.getQuestItemsCount(2638));
                        st.giveItems(2639, 1L);
                        st.playSound("ItemSound.quest_middle");
                        st.set("cond", "6");
                     }
                  } else if (st.getRandom(2) == 1) {
                     st.giveItems(2638, 1L);
                     st.playSound("ItemSound.quest_itemget");
                  }
               }
            } else if (npcId == 20144) {
               if (cond == 7 && st.getRandom(100) < 33) {
                  st.addSpawn(30656, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 300000);
                  st.playSound("ItemSound.quest_middle");
                  st.set("cond", "8");
               }
            } else if (npcId == 20577) {
               if (cond == 11 && st.getQuestItemsCount(2641) < 20L) {
                  if (st.getQuestItemsCount(2641) == 19L) {
                     st.giveItems(2641, 1L);
                     st.playSound("ItemSound.quest_middle");
                     st.set("cond", "12");
                  } else {
                     st.giveItems(2641, 1L);
                     st.playSound("ItemSound.quest_itemget");
                  }
               }
            } else if (npcId == 20578) {
               if (cond == 11 && st.getQuestItemsCount(2641) < 20L) {
                  if (st.getQuestItemsCount(2641) == 19L) {
                     st.giveItems(2641, 1L);
                     st.playSound("ItemSound.quest_middle");
                     st.set("cond", "12");
                  } else {
                     st.giveItems(2641, 1L);
                     st.playSound("ItemSound.quest_itemget");
                  }
               }
            } else if (npcId == 20579) {
               if (cond == 11 && st.getQuestItemsCount(2641) < 20L) {
                  if (st.getQuestItemsCount(2641) == 19L) {
                     st.giveItems(2641, 1L);
                     st.playSound("ItemSound.quest_middle");
                     st.set("cond", "12");
                  } else {
                     st.giveItems(2641, 1L);
                     st.playSound("ItemSound.quest_itemget");
                  }
               }
            } else if (npcId == 20580) {
               if (cond == 11 && st.getQuestItemsCount(2641) < 20L) {
                  if (st.getQuestItemsCount(2641) == 19L) {
                     st.giveItems(2641, 1L);
                     st.playSound("ItemSound.quest_middle");
                     st.set("cond", "12");
                  } else {
                     st.giveItems(2641, 1L);
                     st.playSound("ItemSound.quest_itemget");
                  }
               }
            } else if (npcId == 20581) {
               if (cond == 11 && st.getQuestItemsCount(2641) < 20L) {
                  if (st.getQuestItemsCount(2641) == 19L) {
                     st.giveItems(2641, 1L);
                     st.playSound("ItemSound.quest_middle");
                     st.set("cond", "12");
                  } else {
                     st.giveItems(2641, 1L);
                     st.playSound("ItemSound.quest_itemget");
                  }
               }
            } else if (npcId == 20582) {
               if (cond == 11 && st.getQuestItemsCount(2641) < 20L) {
                  if (st.getQuestItemsCount(2641) == 19L) {
                     st.giveItems(2641, 1L);
                     st.playSound("ItemSound.quest_middle");
                     st.set("cond", "12");
                  } else {
                     st.giveItems(2641, 1L);
                     st.playSound("ItemSound.quest_itemget");
                  }
               }
            } else if (npcId == 20270 && cond == 14 && st.getRandom(2) == 1) {
               if (st.getQuestItemsCount(2643) == 0L) {
                  st.giveItems(2643, 1L);
                  st.playSound("ItemSound.quest_itemget");
               } else if (st.getQuestItemsCount(2644) == 0L) {
                  st.giveItems(2644, 1L);
                  st.playSound("ItemSound.quest_itemget");
               } else if (st.getQuestItemsCount(2645) == 0L) {
                  st.giveItems(2645, 1L);
                  st.set("cond", "15");
                  st.playSound("ItemSound.quest_middle");
               }
            }
         } else if (cond == 2 && st.getRandom(50) < 2) {
            st.addSpawn(27119, npc, true, 0);
            st.playSound("Itemsound.quest_before_battle");
         }

         return super.onKill(npc, killer, isSummon);
      }
   }

   public static void main(String[] args) {
      new _212_TrialOfDuty(212, "_212_TrialOfDuty", "");
   }
}
