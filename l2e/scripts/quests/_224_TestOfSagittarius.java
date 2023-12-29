package l2e.scripts.quests;

import l2e.commons.util.Rnd;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _224_TestOfSagittarius extends Quest {
   private static final String qn = "_224_TestOfSagittarius";
   private static final int BERNARDS_INTRODUCTION_ID = 3294;
   private static final int LETTER_OF_HAMIL3_ID = 3297;
   private static final int HUNTERS_RUNE2_ID = 3299;
   private static final int MARK_OF_SAGITTARIUS_ID = 3293;
   private static final int CRESCENT_MOON_BOW_ID = 3028;
   private static final int TALISMAN_OF_KADESH_ID = 3300;
   private static final int BLOOD_OF_LIZARDMAN_ID = 3306;
   private static final int LETTER_OF_HAMIL1_ID = 3295;
   private static final int LETTER_OF_HAMIL2_ID = 3296;
   private static final int HUNTERS_RUNE1_ID = 3298;
   private static final int TALISMAN_OF_SNAKE_ID = 3301;
   private static final int MITHRIL_CLIP_ID = 3302;
   private static final int STAKATO_CHITIN_ID = 3303;
   private static final int ST_BOWSTRING_ID = 3304;
   private static final int MANASHENS_HORN_ID = 3305;
   private static final int WOODEN_ARROW_ID = 17;

   public _224_TestOfSagittarius(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30702);
      this.addTalkId(30702);
      this.addTalkId(30514);
      this.addTalkId(30626);
      this.addTalkId(30653);
      this.addTalkId(30717);
      this.addKillId(20230);
      this.addKillId(20232);
      this.addKillId(20233);
      this.addKillId(20234);
      this.addKillId(20269);
      this.addKillId(20270);
      this.addKillId(27090);
      this.addKillId(20551);
      this.addKillId(20563);
      this.addKillId(20577);
      this.addKillId(20578);
      this.addKillId(20579);
      this.addKillId(20580);
      this.addKillId(20581);
      this.addKillId(20582);
      this.addKillId(20079);
      this.addKillId(20080);
      this.addKillId(20081);
      this.addKillId(20082);
      this.addKillId(20084);
      this.addKillId(20086);
      this.addKillId(20089);
      this.addKillId(20090);
      this.questItemIds = new int[]{3299, 3028, 3300, 3306, 3294, 3298, 3295, 3301, 3296, 3297, 3302, 3303, 3304, 3305};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_224_TestOfSagittarius");
      if (st == null) {
         return event;
      } else {
         if (event.equals("1")) {
            htmltext = "30702-04.htm";
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(3294, 1L);
         } else if (event.equals("30626_1")) {
            htmltext = "30626-02.htm";
         } else if (event.equals("30626_2")) {
            htmltext = "30626-03.htm";
            st.takeItems(3294, st.getQuestItemsCount(3294));
            st.giveItems(3295, 1L);
            st.set("cond", "2");
         } else if (event.equals("30626_3")) {
            htmltext = "30626-06.htm";
         } else if (event.equals("30626_4")) {
            htmltext = "30626-07.htm";
            st.takeItems(3298, st.getQuestItemsCount(3298));
            st.giveItems(3296, 1L);
            st.set("cond", "5");
         } else if (event.equals("30653_1")) {
            htmltext = "30653-02.htm";
            st.takeItems(3295, st.getQuestItemsCount(3295));
            st.set("cond", "3");
         } else if (event.equals("30514_1")) {
            htmltext = "30514-02.htm";
            st.takeItems(3296, st.getQuestItemsCount(3296));
            st.set("cond", "6");
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_224_TestOfSagittarius");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (npcId == 30702) {
                  if (player.getClassId().getId() != 7 && player.getClassId().getId() != 22 && player.getClassId().getId() != 35) {
                     htmltext = "30702-02.htm";
                     st.exitQuest(true);
                  } else if (st.getPlayer().getLevel() >= 39) {
                     htmltext = "30702-03.htm";
                  } else {
                     htmltext = "30702-01.htm";
                     st.exitQuest(true);
                  }
               }
               break;
            case 1:
               if (npcId == 30702 && cond == 1 && st.getQuestItemsCount(3294) > 0L) {
                  htmltext = "30702-05.htm";
               } else if (npcId == 30626 && cond == 1 && st.getQuestItemsCount(3294) > 0L) {
                  htmltext = "30626-01.htm";
               } else if (npcId == 30626 && cond == 2 && st.getQuestItemsCount(3295) > 0L) {
                  htmltext = "30626-04.htm";
               } else if (npcId == 30626 && cond == 4 && st.getQuestItemsCount(3298) == 10L) {
                  htmltext = "30626-05.htm";
               } else if (npcId == 30626 && cond == 5 && st.getQuestItemsCount(3296) > 0L) {
                  htmltext = "30626-08.htm";
               } else if (npcId == 30626 && cond == 8) {
                  htmltext = "30626-09.htm";
                  st.giveItems(3297, 1L);
                  st.set("cond", "9");
               } else if (npcId == 30626 && cond == 9 && st.getQuestItemsCount(3297) > 0L) {
                  htmltext = "30626-10.htm";
               } else if (npcId == 30626 && cond == 12 && st.getQuestItemsCount(3028) > 0L) {
                  htmltext = "30626-11.htm";
                  st.set("cond", "13");
               } else if (npcId == 30626 && cond == 13) {
                  htmltext = "30626-12.htm";
               } else if (npcId == 30626 && cond == 14 && st.getQuestItemsCount(3300) > 0L) {
                  htmltext = "30626-13.htm";
                  st.takeItems(3028, -1L);
                  st.takeItems(3300, -1L);
                  st.takeItems(3306, -1L);
                  st.giveItems(3293, 1L);
                  if (player.getVarInt("2ND_CLASS_DIAMOND_REWARD", 0) == 0) {
                     st.giveItems(7562, 96L);
                     st.giveItems(8870, 15L);
                     player.setVar("2ND_CLASS_DIAMOND_REWARD", 1);
                  }

                  st.addExpAndSp(894888, 61408);
                  st.giveItems(57, 161806L);
                  st.playSound("ItemSound.quest_finish");
                  st.unset("cond");
                  st.exitQuest(false);
               } else if (npcId == 30653 && cond == 2 && st.getQuestItemsCount(3295) > 0L) {
                  htmltext = "30653-01.htm";
               } else if (npcId == 30653 && cond == 3) {
                  htmltext = "30653-03.htm";
               } else if (npcId == 30514 && cond == 5 && st.getQuestItemsCount(3296) > 0L) {
                  htmltext = "30514-01.htm";
               } else if (npcId == 30514 && cond == 6) {
                  htmltext = "30514-03.htm";
               } else if (npcId == 30514 && cond == 7 && st.getQuestItemsCount(3301) > 0L) {
                  htmltext = "30514-04.htm";
                  st.takeItems(3301, st.getQuestItemsCount(3301));
                  st.set("cond", "8");
               } else if (npcId == 30514 && cond == 8) {
                  htmltext = "30514-05.htm";
               } else if (npcId == 30717 && cond == 9 && st.getQuestItemsCount(3297) > 0L) {
                  htmltext = "30717-01.htm";
                  st.takeItems(3297, st.getQuestItemsCount(3297));
                  st.set("cond", "10");
               } else if (npcId == 30717 && cond == 10) {
                  htmltext = "30717-03.htm";
               } else if (npcId == 30717 && cond == 12) {
                  htmltext = "30717-04.htm";
               } else if (npcId == 30717
                  && cond == 11
                  && st.getQuestItemsCount(3303) > 0L
                  && st.getQuestItemsCount(3302) > 0L
                  && st.getQuestItemsCount(3304) > 0L
                  && st.getQuestItemsCount(3305) > 0L) {
                  htmltext = "30717-02.htm";
                  st.takeItems(3302, st.getQuestItemsCount(3302));
                  st.takeItems(3303, st.getQuestItemsCount(3303));
                  st.takeItems(3304, st.getQuestItemsCount(3304));
                  st.takeItems(3305, st.getQuestItemsCount(3305));
                  st.giveItems(3028, 1L);
                  st.giveItems(17, 10L);
                  st.set("cond", "12");
               }
            case 2:
               if (npcId == 30702) {
                  htmltext = getAlreadyCompletedMsg(player);
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_224_TestOfSagittarius");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         if (npcId != 20079 && npcId != 20080 && npcId != 20081 && npcId != 20084 && npcId != 20086 && npcId != 20089 && npcId != 20090) {
            if (npcId != 20269 && npcId != 20270) {
               if (npcId != 20230 && npcId != 20232 && npcId != 20234) {
                  if (npcId == 20563) {
                     if (cond == 10 && st.getQuestItemsCount(3305) == 0L && Rnd.chance(10)) {
                        st.giveItems(3305, 1L);
                        if (st.getQuestItemsCount(3302) > 0L && st.getQuestItemsCount(3304) > 0L && st.getQuestItemsCount(3303) > 0L) {
                           st.set("cond", "11");
                           st.playSound("ItemSound.quest_middle");
                        } else {
                           st.playSound("ItemSound.quest_itemget");
                        }
                     }
                  } else if (npcId == 20233) {
                     if (cond == 10 && st.getQuestItemsCount(3304) == 0L && Rnd.chance(10)) {
                        st.giveItems(3304, 1L);
                        if (st.getQuestItemsCount(3302) > 0L && st.getQuestItemsCount(3305) > 0L && st.getQuestItemsCount(3303) > 0L) {
                           st.set("cond", "11");
                           st.playSound("ItemSound.quest_middle");
                        } else {
                           st.playSound("ItemSound.quest_itemget");
                        }
                     }
                  } else if (npcId == 20551) {
                     if (cond == 10 && st.getQuestItemsCount(3302) == 0L && Rnd.chance(10)) {
                        st.giveItems(3302, 1L);
                        if (st.getQuestItemsCount(3304) > 0L && st.getQuestItemsCount(3305) > 0L && st.getQuestItemsCount(3303) > 0L) {
                           st.set("cond", "11");
                           st.playSound("ItemSound.quest_middle");
                        } else {
                           st.playSound("ItemSound.quest_itemget");
                        }
                     }
                  } else if (npcId == 20551) {
                     if (cond == 10 && st.getQuestItemsCount(3302) == 0L && Rnd.chance(10)) {
                        if (st.getQuestItemsCount(3304) > 0L && st.getQuestItemsCount(3305) > 0L && st.getQuestItemsCount(3303) > 0L) {
                           st.giveItems(3302, 1L);
                           st.set("cond", "11");
                           st.playSound("ItemSound.quest_middle");
                        } else {
                           st.giveItems(3302, 1L);
                           st.playSound("ItemSound.quest_itemget");
                        }
                     }
                  } else if (npcId != 20577 && npcId != 20578 && npcId != 20579 && npcId != 20580 && npcId != 20581 && npcId != 20582) {
                     if (npcId == 27090 && cond == 13 && st.getQuestItemsCount(3300) == 0L) {
                        if (st.getItemEquipped(5) == 3028) {
                           st.giveItems(3300, 1L);
                           st.set("cond", "14");
                           st.playSound("ItemSound.quest_middle");
                        } else {
                           st.addSpawn(27090);
                        }
                     }
                  } else if (cond == 13) {
                     if (Rnd.chance((double)((st.getQuestItemsCount(3306) - 120L) * 5L))) {
                        st.addSpawn(27090);
                        st.takeItems(3306, st.getQuestItemsCount(3306));
                        st.playSound("ItemSound.quest_before_battle");
                     } else {
                        st.giveItems(3306, 1L);
                        st.playSound("ItemSound.quest_itemget");
                     }
                  }
               } else if (cond == 10 && st.getQuestItemsCount(3303) == 0L && Rnd.chance(10)) {
                  st.giveItems(3303, 1L);
                  if (st.getQuestItemsCount(3302) > 0L && st.getQuestItemsCount(3304) > 0L && st.getQuestItemsCount(3305) > 0L) {
                     st.set("cond", "11");
                     st.playSound("ItemSound.quest_middle");
                  } else {
                     st.playSound("ItemSound.quest_itemget");
                  }
               }
            } else if (cond == 6 && st.getQuestItemsCount(3299) < 10L && Rnd.chance(50)) {
               st.giveItems(3299, 1L);
               if (st.getQuestItemsCount(3299) == 10L) {
                  st.takeItems(3299, 10L);
                  st.giveItems(3301, 1L);
                  st.set("cond", "7");
                  st.playSound("ItemSound.quest_middle");
               } else {
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         } else if (cond == 3 && st.getQuestItemsCount(3298) < 10L && Rnd.chance(50)) {
            st.giveItems(3298, 1L);
            if (st.getQuestItemsCount(3298) == 10L) {
               st.set("cond", "4");
               st.playSound("ItemSound.quest_middle");
            } else {
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _224_TestOfSagittarius(224, "_224_TestOfSagittarius", "");
   }
}
