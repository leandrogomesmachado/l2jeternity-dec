package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _345_MethodToRaiseTheDead extends Quest {
   private static final String qn = "_345_MethodToRaiseTheDead";
   private static final int VICTIMS_ARM_BONE = 4274;
   private static final int VICTIMS_THIGH_BONE = 4275;
   private static final int VICTIMS_SKULL = 4276;
   private static final int VICTIMS_RIB_BONE = 4277;
   private static final int VICTIMS_SPINE = 4278;
   private static final int USELESS_BONE_PIECES = 4280;
   private static final int POWDER_TO_SUMMON_DEAD_SOULS = 4281;
   private static final int[] CORPSE_PARTS = new int[]{4274, 4275, 4276, 4277, 4278};
   private static final int Xenovia = 30912;
   private static final int Dorothy = 30970;
   private static final int Orpheus = 30971;
   private static final int Medium_Jar = 30973;
   private static final int BILL_OF_IASON_HEINE = 4310;
   private static final int IMPERIAL_DIAMOND = 3456;

   public _345_MethodToRaiseTheDead(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30970);
      this.addTalkId(new int[]{30970, 30912, 30973, 30971});
      this.addKillId(new int[]{20789, 20791});
      this.questItemIds = new int[]{4274, 4275, 4276, 4277, 4278, 4281, 4280};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_345_MethodToRaiseTheDead");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30970-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30970-06.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30912-04.htm")) {
            if (player.getAdena() >= 1000L) {
               if (st.getQuestItemsCount(4274)
                     + st.getQuestItemsCount(4275)
                     + st.getQuestItemsCount(4276)
                     + st.getQuestItemsCount(4277)
                     + st.getQuestItemsCount(4278)
                  == 5L) {
                  st.set("cond", "3");
                  st.takeItems(57, 1000L);
                  htmltext = "30912-03.htm";
                  st.giveItems(4281, 1L);
                  st.playSound("ItemSound.quest_itemget");
               } else {
                  st.set("cond", "1");
               }
            }
         } else if (event.equalsIgnoreCase("30973-04.htm")) {
            if (st.getInt("cond") == 3) {
               if (st.getQuestItemsCount(4281)
                     + st.getQuestItemsCount(4274)
                     + st.getQuestItemsCount(4275)
                     + st.getQuestItemsCount(4276)
                     + st.getQuestItemsCount(4277)
                     + st.getQuestItemsCount(4278)
                  == 6L) {
                  int chance = getRandom(3);
                  if (chance == 0) {
                     st.set("cond", "6");
                     htmltext = "30973-02a.htm";
                  } else if (chance == 1) {
                     st.set("cond", "6");
                     htmltext = "30973-02b.htm";
                  } else {
                     st.set("cond", "7");
                     htmltext = "30973-02c.htm";
                  }

                  st.takeItems(4281, -1L);
                  st.takeItems(4274, -1L);
                  st.takeItems(4275, -1L);
                  st.takeItems(4276, -1L);
                  st.takeItems(4277, -1L);
                  st.takeItems(4278, -1L);
                  st.playSound("ItemSound.quest_middle");
               } else {
                  st.set("cond", "1");
                  st.takeItems(4281, -1L);
               }
            }
         } else if (event.equalsIgnoreCase("30971-02a.htm")) {
            if (st.getQuestItemsCount(4280) > 0L) {
               htmltext = "30971-02.htm";
            }
         } else if (event.equalsIgnoreCase("30971-03.htm")) {
            if (st.getQuestItemsCount(4280) > 0L) {
               long amount = st.getQuestItemsCount(4280) * 104L;
               st.takeItems(4280, -1L);
               st.rewardItems(57, amount);
            } else {
               htmltext = "30971-02a.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = Quest.getNoQuestMsg(player);
      QuestState st = player.getQuestState("_345_MethodToRaiseTheDead");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 35 && player.getLevel() <= 42) {
                  htmltext = "30970-01.htm";
               } else {
                  htmltext = "30970-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 30912:
                     if (cond == 2) {
                        htmltext = "30912-01.htm";
                     } else if (cond >= 3) {
                        htmltext = "30912-06.htm";
                     }
                     break;
                  case 30970:
                     if (cond == 1) {
                        if (st.getQuestItemsCount(4274)
                              + st.getQuestItemsCount(4275)
                              + st.getQuestItemsCount(4276)
                              + st.getQuestItemsCount(4277)
                              + st.getQuestItemsCount(4278)
                           < 5L) {
                           htmltext = "30970-04.htm";
                        } else {
                           htmltext = "30970-05.htm";
                        }
                     } else if (cond == 2) {
                        htmltext = "30970-07.htm";
                     } else if (cond >= 3 && cond <= 5) {
                        htmltext = "30970-08.htm";
                     } else if (cond >= 6) {
                        long amount = st.getQuestItemsCount(4280) * 70L;
                        st.takeItems(4280, -1L);
                        if (cond == 7) {
                           htmltext = "30970-10.htm";
                           st.rewardItems(57, 3040L + amount);
                           if (st.getRandom(10) < 1) {
                              st.giveItems(3456, 1L);
                           } else {
                              st.giveItems(4310, 5L);
                           }
                        } else {
                           htmltext = "30970-09.htm";
                           st.rewardItems(57, 5390L + amount);
                           st.giveItems(4310, 3L);
                        }

                        st.playSound("ItemSound.quest_finish");
                        st.exitQuest(true);
                     }
                     break;
                  case 30971:
                     htmltext = "30971-01.htm";
                     break;
                  case 30973:
                     htmltext = "30973-01.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_345_MethodToRaiseTheDead");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1) {
            if (st.getRandom(100) < 66) {
               st.giveItems(4280, 1L);
               st.playSound("ItemSound.quest_itemget");
            } else {
               int randomPart = CORPSE_PARTS[getRandom(CORPSE_PARTS.length)];
               if (st.getQuestItemsCount(randomPart) == 0L) {
                  st.giveItems(randomPart, 1L);
                  st.playSound("ItemSound.quest_middle");
               }
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _345_MethodToRaiseTheDead(345, "_345_MethodToRaiseTheDead", "");
   }
}
