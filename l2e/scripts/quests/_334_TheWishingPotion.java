package l2e.scripts.quests;

import l2e.commons.util.Rnd;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _334_TheWishingPotion extends Quest {
   private static final String qn = "_334_TheWishingPotion";
   private static final int GRIMA = 27135;
   private static final int SUCCUBUS_OF_SEDUCTION = 27136;
   private static final int GREAT_DEMON_KING = 27138;
   private static final int SECRET_KEEPER_TREE = 27139;
   private static final int SANCHES = 27153;
   private static final int BONAPARTERIUS = 27154;
   private static final int RAMSEBALIUS = 27155;
   private static final int TORAI = 30557;
   private static final int ALCHEMIST_MATILD = 30738;
   private static final int RUPINA = 30742;
   private static final int WISDOM_CHEST = 30743;
   private static final int WHISPERING_WIND = 20078;
   private static final int ANT_SOLDIER = 20087;
   private static final int ANT_WARRIOR_CAPTAIN = 20088;
   private static final int SILENOS = 20168;
   private static final int TYRANT = 20192;
   private static final int TYRANT_KINGPIN = 20193;
   private static final int AMBER_BASILISK = 20199;
   private static final int HORROR_MIST_RIPPER = 20227;
   private static final int TURAK_BUGBEAR = 20248;
   private static final int TURAK_BUGBEAR_WARRIOR = 20249;
   private static final int GLASS_JAGUAR = 20250;
   private static final int DEMONS_TUNIC_ID = 441;
   private static final int DEMONS_STOCKINGS_ID = 472;
   private static final int SCROLL_OF_ESCAPE_ID = 736;
   private static final int NECKLACE_OF_GRACE_ID = 931;
   private static final int SPELLBOOK_ICEBOLT_ID = 1049;
   private static final int SPELLBOOK_BATTLEHEAL_ID = 1050;
   private static final int DEMONS_BOOTS_ID = 2435;
   private static final int DEMONS_GLOVES_ID = 2459;
   private static final int WISH_POTION_ID = 3467;
   private static final int ANCIENT_CROWN_ID = 3468;
   private static final int CERTIFICATE_OF_ROYALTY_ID = 3469;
   private static final int GOLD_BAR_ID = 3470;
   private static final int ALCHEMY_TEXT_ID = 3678;
   private static final int SECRET_BOOK_ID = 3679;
   private static final int POTION_RECIPE_1_ID = 3680;
   private static final int POTION_RECIPE_2_ID = 3681;
   private static final int MATILDS_ORB_ID = 3682;
   private static final int FORBIDDEN_LOVE_SCROLL_ID = 3683;
   private static final int HEART_OF_PAAGRIO_ID = 3943;
   private static final int AMBER_SCALE_ID = 3684;
   private static final int WIND_SOULSTONE_ID = 3685;
   private static final int GLASS_EYE_ID = 3686;
   private static final int HORROR_ECTOPLASM_ID = 3687;
   private static final int SILENOS_HORN_ID = 3688;
   private static final int ANT_SOLDIER_APHID_ID = 3689;
   private static final int TYRANTS_CHITIN_ID = 3690;
   private static final int BUGBEAR_BLOOD_ID = 3691;
   private static final int DROP_CHANCE_FORBIDDEN_LOVE_SCROLL_ID = 3;
   private static final int DROP_CHANCE_NECKLACE_OF_GRACE_ID = 4;
   private static final int DROP_CHANCE_GOLD_BAR_ID = 10;
   private static final int[][] DROPLIST_COND = new int[][]{
      {1, 2, 27139, 0, 3679, 1, 1000000, 1},
      {3, 0, 20199, 0, 3684, 1, 150000, 1},
      {3, 0, 20078, 0, 3685, 1, 200000, 1},
      {3, 0, 20250, 0, 3686, 1, 350000, 1},
      {3, 0, 20227, 0, 3687, 1, 150000, 1},
      {3, 0, 20168, 0, 3688, 1, 300000, 1},
      {3, 0, 20087, 0, 3689, 1, 400000, 1},
      {3, 0, 20088, 0, 3689, 1, 400000, 1},
      {3, 0, 20192, 0, 3690, 1, 500000, 1},
      {3, 0, 20193, 0, 3690, 1, 500000, 1},
      {3, 0, 20248, 0, 3691, 1, 150000, 1},
      {3, 0, 20249, 0, 3691, 1, 250000, 1}
   };

   public _334_TheWishingPotion(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30738);
      this.addTalkId(30738);
      this.addTalkId(30557);
      this.addTalkId(30743);
      this.addTalkId(30742);

      for(int[] array : DROPLIST_COND) {
         this.addKillId(array[2]);
      }

      this.questItemIds = new int[]{3678, 3679, 3684, 3685, 3686, 3687, 3688, 3689, 3690, 3691};
   }

   public boolean checkIngr(QuestState st) {
      if (st.getQuestItemsCount(3684) == 1L
         && st.getQuestItemsCount(3685) == 1L
         && st.getQuestItemsCount(3686) == 1L
         && st.getQuestItemsCount(3687) == 1L
         && st.getQuestItemsCount(3688) == 1L
         && st.getQuestItemsCount(3689) == 1L
         && st.getQuestItemsCount(3690) == 1L
         && st.getQuestItemsCount(3691) == 1L) {
         st.set("cond", "4");
         return true;
      } else {
         st.set("cond", "3");
         return false;
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_334_TheWishingPotion");
      if (st == null) {
         return null;
      } else {
         String htmltext = event;
         if (event.equalsIgnoreCase("30738-03.htm")) {
            st.setState((byte)1);
            st.set("cond", "1");
            st.giveItems(3678, 1L);
         } else if (event.equalsIgnoreCase("30738-06.htm")) {
            if (st.getQuestItemsCount(3467) == 0L) {
               st.takeItems(3678, -1L);
               st.takeItems(3679, -1L);
               if (st.getQuestItemsCount(3680) == 0L) {
                  st.giveItems(3680, 1L);
               }

               if (st.getQuestItemsCount(3681) == 0L) {
                  st.giveItems(3681, 1L);
               }

               if (st.getQuestItemsCount(3682) == 0L) {
                  htmltext = "30738-06.htm";
               } else {
                  htmltext = "30738-12.htm";
               }

               st.set("cond", "3");
            } else if (st.getQuestItemsCount(3682) >= 1L && st.getQuestItemsCount(3467) >= 1L) {
               htmltext = "30738-13.htm";
            }
         } else if (event.equalsIgnoreCase("30738-10.htm")) {
            if (this.checkIngr(st)) {
               st.playSound("ItemSound.quest_finish");
               st.takeItems(3678, -1L);
               st.takeItems(3679, -1L);
               st.takeItems(3680, -1L);
               st.takeItems(3681, -1L);
               st.takeItems(3684, -1L);
               st.takeItems(3685, -1L);
               st.takeItems(3686, -1L);
               st.takeItems(3687, -1L);
               st.takeItems(3688, -1L);
               st.takeItems(3689, -1L);
               st.takeItems(3690, -1L);
               st.takeItems(3691, -1L);
               if (st.getQuestItemsCount(3682) == 0L) {
                  st.giveItems(3682, 1L);
               }

               st.giveItems(3467, 1L);
               st.set("cond", "0");
            } else {
               htmltext = "<html><head><body>You don't have required items</body></html>";
            }
         } else if (event.equalsIgnoreCase("30738-14.htm")) {
            if (st.getQuestItemsCount(3467) >= 1L) {
               htmltext = "30738-15.htm";
            }
         } else if (event.equalsIgnoreCase("30738-16.htm")) {
            if (st.getQuestItemsCount(3467) >= 1L) {
               st.takeItems(3467, 1L);
               if (Rnd.chance(50)) {
                  st.addSpawn(27136);
                  st.addSpawn(27136);
                  st.addSpawn(27136);
               } else {
                  st.addSpawn(30742);
               }
            } else {
               htmltext = "30738-14.htm";
            }
         } else if (event.equalsIgnoreCase("30738-17.htm")) {
            if (st.getQuestItemsCount(3467) >= 1L) {
               st.takeItems(3467, 1L);
               int WISH_CHANCE = getRandom(100) + 1;
               if (WISH_CHANCE <= 33) {
                  st.addSpawn(27135);
                  st.addSpawn(27135);
                  st.addSpawn(27135);
               } else if (WISH_CHANCE >= 66) {
                  st.giveItems(57, 10000L);
               } else if (Rnd.chance(2)) {
                  st.giveItems(57, (long)(getRandom(10) + 1) * 1000000L);
               } else {
                  st.addSpawn(27135);
                  st.addSpawn(27135);
                  st.addSpawn(27135);
               }
            } else {
               htmltext = "30738-14.htm";
            }
         } else if (event.equalsIgnoreCase("30738-18.htm")) {
            if (st.getQuestItemsCount(3467) >= 1L) {
               st.takeItems(3467, 1L);
               int WISH_CHANCE = getRandom(100) + 1;
               if (WISH_CHANCE <= 33) {
                  st.giveItems(3469, 1L);
               } else if (WISH_CHANCE >= 66) {
                  st.giveItems(3468, 1L);
               } else {
                  st.addSpawn(27153);
               }
            } else {
               htmltext = "30738-14.htm";
            }
         } else if (event.equalsIgnoreCase("30738-19.htm")) {
            if (st.getQuestItemsCount(3467) >= 1L) {
               st.takeItems(3467, 1L);
               int WISH_CHANCE = getRandom(100) + 1;
               if (WISH_CHANCE <= 33) {
                  st.giveItems(1049, 1L);
               } else if (WISH_CHANCE <= 66) {
                  st.giveItems(1050, 1L);
               } else {
                  st.addSpawn(30743);
               }
            } else {
               htmltext = "30738-14.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_334_TheWishingPotion");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         String htmltext = getNoQuestMsg(player);
         int id = st.getState();
         int cond = 0;
         if (id != 0) {
            cond = st.getInt("cond");
         }

         if (npcId == 30738) {
            if (cond == 0) {
               if (player.getLevel() <= 29) {
                  htmltext = "30738-21.htm";
                  st.exitQuest(true);
               } else if (st.getQuestItemsCount(3682) == 0L) {
                  htmltext = "30738-01.htm";
               } else if (st.getQuestItemsCount(3467) == 0L) {
                  st.set("cond", "3");
                  if (st.getQuestItemsCount(3680) == 0L) {
                     st.giveItems(3680, 1L);
                  }

                  if (st.getQuestItemsCount(3681) == 0L) {
                     st.giveItems(3681, 1L);
                  }

                  htmltext = "30738-12.htm";
               } else {
                  htmltext = "30738-11.htm";
               }
            } else if (cond == 1 && st.getQuestItemsCount(3678) == 1L) {
               htmltext = "30738-04.htm";
            } else if (cond == 2) {
               if (st.getQuestItemsCount(3679) == 1L && st.getQuestItemsCount(3678) == 1L) {
                  htmltext = "30738-05.htm";
               }
            } else if (cond == 4) {
               if (this.checkIngr(st)) {
                  htmltext = "30738-08.htm";
               } else {
                  htmltext = "30738-07.htm";
               }
            }
         } else if (npcId == 30557) {
            if (st.getQuestItemsCount(3683) >= 1L) {
               st.takeItems(3683, 1L);
               st.giveItems(57, 500000L);
               htmltext = "30557-01.htm";
            } else {
               htmltext = getNoQuestMsg(player);
            }
         } else if (npcId == 30743) {
            int DROP_CHANCE = getRandom(100) + 1;
            if (DROP_CHANCE <= 20) {
               st.giveItems(1049, 1L);
               st.giveItems(1050, 1L);
               player.getTarget().decayMe();
               htmltext = "30743-06.htm";
            } else if (DROP_CHANCE <= 30) {
               st.giveItems(3943, 1L);
               player.getTarget().decayMe();
               htmltext = "30743-06.htm";
            } else {
               player.getTarget().decayMe();
               htmltext = "30743-0" + String.valueOf(getRandom(5) + 1) + ".htm";
            }
         } else if (npcId == 30742) {
            int DROP_CHANCE = getRandom(100) + 1;
            if (DROP_CHANCE <= 4) {
               st.giveItems(931, 1L);
            } else {
               st.giveItems(736, 1L);
            }

            player.getTarget().decayMe();
            htmltext = "30742-01.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isPet) {
      QuestState st = player.getQuestState("_334_TheWishingPotion");
      if (st == null) {
         return null;
      } else if (st.getState() != 1) {
         return null;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");

         for(int[] element : DROPLIST_COND) {
            if (cond == element[0] && npcId == element[2] && (element[3] == 0 || st.getQuestItemsCount(element[3]) > 0L)) {
               if (element[5] == 0) {
                  st.dropItems(element[4], (long)element[7], (long)element[6]);
               } else if (st.dropQuestItems(element[4], element[7], element[7], (long)element[5], element[6], true)) {
                  if (cond == 3) {
                     this.checkIngr(st);
                  }

                  if (element[1] != cond && element[1] != 0) {
                     st.set("cond", String.valueOf(element[1]));
                     st.setState((byte)1);
                  }
               }
            }
         }

         int DROP_CHANCE = getRandom(100) + 1;
         if (npcId == 27136 && DROP_CHANCE <= 3) {
            st.playSound("ItemSound.quest_itemget");
            st.giveItems(3683, 1L);
         } else if (npcId == 27135 && DROP_CHANCE <= 10) {
            st.playSound("ItemSound.quest_itemget");
            st.giveItems(3470, (long)(getRandom(5) + 1));
         } else if (npcId == 27153 && Rnd.chance(51)) {
            st.addSpawn(27154);
         } else if (npcId == 27154 && Rnd.chance(51)) {
            st.addSpawn(27155);
         } else if (npcId == 27155 && Rnd.chance(51)) {
            st.addSpawn(27138);
         } else if (npcId == 27138 && Rnd.chance(51)) {
            if (DROP_CHANCE <= 25) {
               st.giveItems(2435, 1L);
            } else if (DROP_CHANCE <= 50) {
               st.giveItems(2459, 1L);
            } else if (DROP_CHANCE <= 75) {
               st.giveItems(472, 1L);
            } else {
               st.giveItems(441, 1L);
            }

            st.playSound("ItemSound.quest_itemget");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _334_TheWishingPotion(334, "_334_TheWishingPotion", "");
   }
}
