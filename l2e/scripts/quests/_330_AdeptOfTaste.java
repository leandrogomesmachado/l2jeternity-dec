package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _330_AdeptOfTaste extends Quest {
   private static final String qn = "_330_AdeptOfTaste";
   private static final int INGREDIENT_LIST = 1420;
   private static final int SONIAS_BOTANYBOOK = 1421;
   private static final int RED_MANDRAGORA_ROOT = 1422;
   private static final int WHITE_MANDRAGORA_ROOT = 1423;
   private static final int RED_MANDRAGORA_SAP = 1424;
   private static final int WHITE_MANDRAGORA_SAP = 1425;
   private static final int JAYCUBS_INSECTBOOK = 1426;
   private static final int NECTAR = 1427;
   private static final int ROYAL_JELLY = 1428;
   private static final int HONEY = 1429;
   private static final int GOLDEN_HONEY = 1430;
   private static final int PANOS_CONTRACT = 1431;
   private static final int HOBGOBLIN_AMULET = 1432;
   private static final int DIONIAN_POTATO = 1433;
   private static final int GLYVKAS_BOTANYBOOK = 1434;
   private static final int GREEN_MARSH_MOSS = 1435;
   private static final int BROWN_MARSH_MOSS = 1436;
   private static final int GREEN_MOSS_BUNDLE = 1437;
   private static final int BROWN_MOSS_BUNDLE = 1438;
   private static final int ROLANTS_CREATUREBOOK = 1439;
   private static final int MONSTER_EYE_BODY = 1440;
   private static final int MONSTER_EYE_MEAT = 1441;
   private static final int JONAS_STEAK_DISH1 = 1442;
   private static final int JONAS_STEAK_DISH2 = 1443;
   private static final int JONAS_STEAK_DISH3 = 1444;
   private static final int JONAS_STEAK_DISH4 = 1445;
   private static final int JONAS_STEAK_DISH5 = 1446;
   private static final int MIRIENS_REVIEW1 = 1447;
   private static final int MIRIENS_REVIEW2 = 1448;
   private static final int MIRIENS_REVIEW3 = 1449;
   private static final int MIRIENS_REVIEW4 = 1450;
   private static final int MIRIENS_REVIEW5 = 1451;
   private static final int ADENA = 57;
   private static final int JONAS_SALAD_RECIPE = 1455;
   private static final int JONAS_SAUCE_RECIPE = 1456;
   private static final int JONAS_STEAK_RECIPE = 1457;

   public _330_AdeptOfTaste(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30469);
      this.addTalkId(new int[]{30469, 30062, 30067, 30069, 30073, 30078, 30461});
      this.addKillId(new int[]{20147, 20154, 20155, 20156, 20204, 20223, 20226, 20228, 20229, 20265, 20266});
      this.questItemIds = new int[]{
         1420,
         1424,
         1425,
         1429,
         1430,
         1433,
         1437,
         1438,
         1441,
         1447,
         1448,
         1449,
         1450,
         1451,
         1442,
         1443,
         1444,
         1445,
         1446,
         1421,
         1422,
         1423,
         1426,
         1427,
         1428,
         1431,
         1432,
         1434,
         1435,
         1436,
         1439,
         1440
      };
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_330_AdeptOfTaste");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30469-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1420, 1L);
         } else if (event.equalsIgnoreCase("30062-05.htm")) {
            st.takeItems(1421, 1L);
            st.takeItems(1422, -1L);
            st.takeItems(1423, -1L);
            st.giveItems(1424, 1L);
            st.playSound("ItemSound.quest_itemget");
         } else if (event.equalsIgnoreCase("30073-05.htm")) {
            st.takeItems(1426, 1L);
            st.takeItems(1427, -1L);
            st.takeItems(1428, -1L);
            st.giveItems(1429, 1L);
            st.playSound("ItemSound.quest_itemget");
         } else if (event.equalsIgnoreCase("30067-05.htm")) {
            st.takeItems(1434, 1L);
            st.takeItems(1435, -1L);
            st.takeItems(1436, -1L);
            st.giveItems(1437, 1L);
            st.playSound("ItemSound.quest_itemget");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_330_AdeptOfTaste");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() < 24) {
                  htmltext = "30469-01.htm";
                  st.exitQuest(true);
               } else {
                  htmltext = "30469-02.htm";
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 30062:
                     if (ingredients_count(st) < 5L) {
                        if (!st.hasQuestItems(1421)) {
                           if (!st.hasQuestItems(1424) && !st.hasQuestItems(1425)) {
                              htmltext = "30062-01.htm";
                              st.giveItems(1421, 1L);
                              st.playSound("ItemSound.quest_itemget");
                           } else {
                              htmltext = "30062-07.htm";
                           }
                        } else if (st.getQuestItemsCount(1422) < 40L || st.getQuestItemsCount(1423) < 40L) {
                           htmltext = "30062-02.htm";
                        } else if (st.getQuestItemsCount(1423) >= 40L) {
                           htmltext = "30062-06.htm";
                           st.takeItems(1421, 1L);
                           st.takeItems(1422, -1L);
                           st.takeItems(1423, -1L);
                           st.giveItems(1425, 1L);
                           st.playSound("ItemSound.quest_itemget");
                        } else {
                           htmltext = "30062-03.htm";
                        }
                     } else {
                        htmltext = "30062-07.htm";
                     }
                     break;
                  case 30067:
                     if (ingredients_count(st) < 5L) {
                        if (!st.hasQuestItems(1434)) {
                           if (!st.hasQuestItems(1437) && !st.hasQuestItems(1438)) {
                              st.giveItems(1434, 1L);
                              htmltext = "30067-01.htm";
                              st.playSound("ItemSound.quest_itemget");
                           } else {
                              htmltext = "30067-07.htm";
                           }
                        } else if (st.getQuestItemsCount(1435) < 20L || st.getQuestItemsCount(1436) < 20L) {
                           htmltext = "30067-02.htm";
                        } else if (st.getQuestItemsCount(1436) >= 20L) {
                           htmltext = "30067-06.htm";
                           st.takeItems(1434, 1L);
                           st.takeItems(1435, -1L);
                           st.takeItems(1436, -1L);
                           st.giveItems(1438, 1L);
                           st.playSound("ItemSound.quest_itemget");
                        } else {
                           htmltext = "30067-03.htm";
                        }
                     } else {
                        htmltext = "30067-07.htm";
                     }
                     break;
                  case 30069:
                     if (ingredients_count(st) < 5L) {
                        if (!st.hasQuestItems(1439)) {
                           if (!st.hasQuestItems(1441)) {
                              htmltext = "30069-01.htm";
                              st.giveItems(1439, 1L);
                              st.playSound("ItemSound.quest_itemget");
                           } else {
                              htmltext = "30069-04.htm";
                           }
                        } else if (st.getQuestItemsCount(1440) < 30L) {
                           htmltext = "30069-02.htm";
                        } else {
                           htmltext = "30069-03.htm";
                           st.takeItems(1439, 1L);
                           st.takeItems(1440, -1L);
                           st.giveItems(1441, 1L);
                           st.playSound("ItemSound.quest_itemget");
                        }
                     } else {
                        htmltext = "30069-04.htm";
                     }
                     break;
                  case 30073:
                     if (ingredients_count(st) < 5L) {
                        if (!st.hasQuestItems(1426)) {
                           if (!st.hasQuestItems(1429) && !st.hasQuestItems(1430)) {
                              htmltext = "30073-01.htm";
                              st.giveItems(1426, 1L);
                              st.playSound("ItemSound.quest_itemget");
                           } else {
                              htmltext = "30073-07.htm";
                           }
                        } else if (st.getQuestItemsCount(1427) < 20L) {
                           htmltext = "30073-02.htm";
                        } else if (st.getQuestItemsCount(1428) < 10L) {
                           htmltext = "30073-03.htm";
                        } else {
                           htmltext = "30073-06.htm";
                           st.takeItems(1426, 1L);
                           st.takeItems(1427, -1L);
                           st.takeItems(1428, -1L);
                           st.giveItems(1430, 1L);
                           st.playSound("ItemSound.quest_itemget");
                        }
                     } else {
                        htmltext = "30073-07.htm";
                     }
                     break;
                  case 30078:
                     if (ingredients_count(st) < 5L) {
                        if (!st.hasQuestItems(1431)) {
                           if (!st.hasQuestItems(1433)) {
                              htmltext = "30078-01.htm";
                              st.giveItems(1431, 1L);
                              st.playSound("ItemSound.quest_itemget");
                           } else {
                              htmltext = "30078-04.htm";
                           }
                        } else if (st.getQuestItemsCount(1432) < 30L) {
                           htmltext = "30078-02.htm";
                        } else {
                           htmltext = "30078-03.htm";
                           st.takeItems(1431, 1L);
                           st.takeItems(1432, -1L);
                           st.giveItems(1433, 1L);
                           st.playSound("ItemSound.quest_itemget");
                        }
                     } else {
                        htmltext = "30078-04.htm";
                     }
                     break;
                  case 30461:
                     if (st.getQuestItemsCount(1420) > 0L) {
                        htmltext = "30461-01.htm";
                     } else if (st.getQuestItemsCount(1420) == 0L && ingredients_count(st) == 0L) {
                        if (has_dish(st) > 0L && has_review(st) == 0L) {
                           if (st.hasQuestItems(1442)) {
                              htmltext = "30461-02t1.htm";
                              st.takeItems(1442, 1L);
                              st.giveItems(1447, 1L);
                              st.playSound("ItemSound.quest_itemget");
                           } else if (st.hasQuestItems(1443)) {
                              htmltext = "30461-02t2.htm";
                              st.takeItems(1443, 1L);
                              st.giveItems(1448, 1L);
                              st.playSound("ItemSound.quest_itemget");
                           } else if (st.hasQuestItems(1444)) {
                              htmltext = "30461-02t3.htm";
                              st.takeItems(1444, 1L);
                              st.giveItems(1449, 1L);
                              st.playSound("ItemSound.quest_itemget");
                           } else if (st.hasQuestItems(1445)) {
                              htmltext = "30461-02t4.htm";
                              st.takeItems(1445, 1L);
                              st.giveItems(1450, 1L);
                              st.playSound("ItemSound.quest_itemget");
                           } else if (st.hasQuestItems(1446)) {
                              htmltext = "30461-02t5.htm";
                              st.takeItems(1446, 1L);
                              st.giveItems(1451, 1L);
                              st.playSound("ItemSound.quest_itemget");
                           }
                        } else if (has_dish(st) == 0L && has_review(st) > 0L) {
                           htmltext = "30461-04.htm";
                        }
                     }
                     break;
                  case 30469:
                     if (st.getQuestItemsCount(1420) > 0L) {
                        if (ingredients_count(st) < 5L) {
                           htmltext = "30469-04.htm";
                        } else if (ingredients_count(st) >= 5L) {
                           switch((int)special_ingredients(st)) {
                              case 0:
                                 if (getRandom(10) < 1) {
                                    htmltext = "30469-05t2.htm";
                                    st.giveItems(1443, 1L);
                                 } else {
                                    htmltext = "30469-05t1.htm";
                                    st.giveItems(1442, 1L);
                                 }
                                 break;
                              case 1:
                                 if (getRandom(10) < 1) {
                                    htmltext = "30469-05t3.htm";
                                    st.giveItems(1444, 1L);
                                 } else {
                                    htmltext = "30469-05t2.htm";
                                    st.giveItems(1443, 1L);
                                 }
                                 break;
                              case 2:
                                 if (getRandom(10) < 1) {
                                    htmltext = "30469-05t4.htm";
                                    st.giveItems(1445, 1L);
                                 } else {
                                    htmltext = "30469-05t3.htm";
                                    st.giveItems(1444, 1L);
                                 }
                                 break;
                              case 3:
                                 if (getRandom(10) < 1) {
                                    htmltext = "30469-05t5.htm";
                                    st.giveItems(1446, 1L);
                                    st.playSound("ItemSound.quest_jackpot");
                                 } else {
                                    htmltext = "30469-05t4.htm";
                                    st.giveItems(1445, 1L);
                                 }
                           }

                           st.takeItems(1420, 1L);
                           st.takeItems(1424, 1L);
                           st.takeItems(1425, 1L);
                           st.takeItems(1429, 1L);
                           st.takeItems(1430, 1L);
                           st.takeItems(1433, 1L);
                           st.takeItems(1437, 1L);
                           st.takeItems(1438, 1L);
                           st.takeItems(1441, 1L);
                           st.playSound("ItemSound.quest_itemget");
                        }
                     } else if (st.getQuestItemsCount(1420) == 0L && ingredients_count(st) == 0L) {
                        if (has_dish(st) > 0L && has_review(st) == 0L) {
                           htmltext = "30469-06.htm";
                        } else if (has_dish(st) == 0L && has_review(st) > 0L) {
                           if (st.hasQuestItems(1447)) {
                              htmltext = "30469-06t1.htm";
                              st.takeItems(1447, 1L);
                              st.rewardItems(57, 7500L);
                              st.addExpAndSp(6000, 0);
                           } else if (st.hasQuestItems(1448)) {
                              htmltext = "30469-06t2.htm";
                              st.takeItems(1448, 1L);
                              st.rewardItems(57, 9000L);
                              st.addExpAndSp(7000, 0);
                           } else if (st.hasQuestItems(1449)) {
                              htmltext = "30469-06t3.htm";
                              st.takeItems(1449, 1L);
                              st.rewardItems(57, 5800L);
                              st.giveItems(1455, 1L);
                              st.addExpAndSp(9000, 0);
                           } else if (st.hasQuestItems(1450)) {
                              htmltext = "30469-06t4.htm";
                              st.takeItems(1450, 1L);
                              st.rewardItems(57, 6800L);
                              st.giveItems(1456, 1L);
                              st.addExpAndSp(10500, 0);
                           } else if (st.hasQuestItems(1451)) {
                              htmltext = "30469-06t5.htm";
                              st.takeItems(1451, 1L);
                              st.rewardItems(57, 7800L);
                              st.giveItems(1457, 1L);
                              st.addExpAndSp(12000, 0);
                           }

                           st.playSound("ItemSound.quest_finish");
                           st.exitQuest(true);
                        }
                     }
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_330_AdeptOfTaste");
      if (st == null) {
         return null;
      } else {
         switch(npc.getId()) {
            case 20147:
               if (st.hasQuestItems(1431)) {
                  st.dropItemsAlways(1432, 1, 30L);
               }
               break;
            case 20154:
            case 20155:
            case 20156:
            case 20223:
               if (st.hasQuestItems(1421)) {
                  st.dropItemsAlways(getRandom(1000) < 975 ? 1422 : 1423, 1, 40L);
               }
               break;
            case 20204:
            case 20229:
               if (st.hasQuestItems(1426) && !st.dropItems(1428, 1, 10L, 50000)) {
                  st.dropItemsAlways(1427, 1, 20L);
               }
               break;
            case 20226:
            case 20228:
               if (st.hasQuestItems(1434)) {
                  st.dropItemsAlways(getRandom(10) < 9 ? 1435 : 1436, 1, 20L);
               }
               break;
            case 20265:
            case 20266:
               if (st.hasQuestItems(1439)) {
                  st.dropItemsAlways(1440, getRandom(2, 3), 30L);
               }
         }

         return null;
      }
   }

   private static long has_review(QuestState st) {
      return st.getQuestItemsCount(1447)
         + st.getQuestItemsCount(1448)
         + st.getQuestItemsCount(1449)
         + st.getQuestItemsCount(1450)
         + st.getQuestItemsCount(1451);
   }

   private static long has_dish(QuestState st) {
      return st.getQuestItemsCount(1442)
         + st.getQuestItemsCount(1443)
         + st.getQuestItemsCount(1444)
         + st.getQuestItemsCount(1445)
         + st.getQuestItemsCount(1446);
   }

   private static long special_ingredients(QuestState st) {
      return st.getQuestItemsCount(1425) + st.getQuestItemsCount(1430) + st.getQuestItemsCount(1438);
   }

   private static long ingredients_count(QuestState st) {
      return st.getQuestItemsCount(1424)
         + st.getQuestItemsCount(1429)
         + st.getQuestItemsCount(1433)
         + st.getQuestItemsCount(1437)
         + st.getQuestItemsCount(1441)
         + special_ingredients(st);
   }

   public static void main(String[] args) {
      new _330_AdeptOfTaste(330, "_330_AdeptOfTaste", "");
   }
}
