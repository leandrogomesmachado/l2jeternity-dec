package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.Summon;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _422_RepentYourSins extends Quest {
   private static final String qn = "_422_RepentYourSins";
   private static final int SCAVENGER_WERERAT_SKULL = 4326;
   private static final int TUREK_WARHOUND_TAIL = 4327;
   private static final int TYRANT_KINGPIN_HEART = 4328;
   private static final int TRISALIM_TARANTULAS_VENOM_SAC = 4329;
   private static final int MANUAL_OF_MANACLES = 4331;
   private static final int PENITENTS_MANACLES = 4425;
   private static final int PENITENTS_MANACLES1 = 4330;
   private static final int PENITENTS_MANACLES2 = 4426;
   private static final int SILVER_NUGGET = 1873;
   private static final int ADAMANTINE_NUGGET = 1877;
   private static final int BLACKSMITHS_FRAME = 1892;
   private static final int COKES = 1879;
   private static final int STEEL = 1880;
   private static final int BLACK_JUDGE = 30981;
   private static final int KATARI = 30668;
   private static final int PIOTUR = 30597;
   private static final int CASIAN = 30612;
   private static final int JOAN = 30718;
   private static final int PUSHKIN = 30300;
   private static final int SCAVENGER_WERERAT = 20039;
   private static final int TUREK_WARHOUND = 20494;
   private static final int TYRANT_KINGPIN = 20193;
   private static final int TRISALIM_TARANTULA = 20561;

   public _422_RepentYourSins(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30981);
      this.addTalkId(30981);
      this.addTalkId(30668);
      this.addTalkId(30597);
      this.addTalkId(30612);
      this.addTalkId(30718);
      this.addTalkId(30300);
      this.addKillId(20039);
      this.addKillId(20494);
      this.addKillId(20193);
      this.addKillId(20561);
      this.questItemIds = new int[]{4326, 4327, 4328, 4329, 4331, 4425, 4330};
   }

   private int findPetLvl(Player player, int itemId) {
      Summon pet = player.getSummon();
      int level = 0;
      if (pet != null) {
         if (pet.getId() == 12564) {
            level = pet.getStat().getLevel();
         } else {
            ItemInstance item = player.getInventory().getItemByItemId(itemId);
            if (item != null) {
               level = item.getEnchantLevel();
            }
         }
      } else {
         ItemInstance item = player.getInventory().getItemByItemId(itemId);
         if (item != null) {
            level = item.getEnchantLevel();
         }
      }

      return level;
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_422_RepentYourSins");
      if (st == null) {
         return null;
      } else {
         if (event.equalsIgnoreCase("Start")) {
            st.playSound("ItemSound.quest_accept");
            st.setState((byte)1);
            if (player.getLevel() <= 20) {
               htmltext = "30981-03.htm";
               st.set("cond", "1");
               st.set("cond", "2");
            } else if (player.getLevel() <= 30) {
               htmltext = "30981-04.htm";
               st.set("cond", "3");
            } else if (player.getLevel() <= 40) {
               htmltext = "30981-05.htm";
               st.set("cond", "4");
            } else {
               htmltext = "30981-06.htm";
               st.set("cond", "5");
            }
         } else if (event.equalsIgnoreCase("1")) {
            if (st.getQuestItemsCount(4330) >= 1L) {
               st.takeItems(4330, -1L);
            }

            if (st.getQuestItemsCount(4426) >= 1L) {
               st.takeItems(4426, -1L);
            }

            if (st.getQuestItemsCount(4425) >= 1L) {
               st.takeItems(4425, -1L);
            }

            htmltext = "30981-11.htm";
            st.set("cond", "16");
            if (player.getLevel() < 85) {
               st.set("level", String.valueOf(player.getLevel()));
               st.giveItems(4425, 1L);
            } else {
               st.set("level", String.valueOf(84));
               st.giveItems(4425, 1L);
            }
         } else if (event.equalsIgnoreCase("2")) {
            htmltext = "30981-14.htm";
         } else if (event.equalsIgnoreCase("3")) {
            int pLevel = this.findPetLvl(player, 4425);
            int level = player.getLevel();
            int oLevel = st.getInt("level");
            Summon pet = player.getSummon();
            if (pet != null && pet.getId() == 12564) {
               htmltext = "30981-16.htm";
            } else {
               int pkRemove = 0;
               if (level > oLevel) {
                  pkRemove = pLevel - level;
               } else {
                  pkRemove = pLevel - oLevel;
               }

               if (pkRemove < 0) {
                  pkRemove = 0;
               }

               pkRemove = st.getRandom(10 + pkRemove) + 1;
               if (player.getPkKills() <= pkRemove) {
                  st.giveItems(4426, 1L);
                  st.takeItems(4425, 1L);
                  htmltext = "30981-15.htm";
                  player.setPkKills(0);
                  st.playSound("ItemSound.quest_finish");
                  st.exitQuest(true);
               } else {
                  st.giveItems(4426, 1L);
                  st.takeItems(4425, 1L);
                  htmltext = "30981-17.htm";
                  int newPkCount = player.getPkKills() - pkRemove;
                  player.setPkKills(newPkCount);
                  st.set("level", "0");
               }
            }
         } else if (event.equalsIgnoreCase("4")) {
            htmltext = "30981-19.htm";
         } else if (event.equalsIgnoreCase("Quit")) {
            htmltext = "30981-20.htm";
            st.playSound("ItemSound.quest_finish");
            st.takeItems(4326, -1L);
            st.takeItems(4327, -1L);
            st.takeItems(4328, -1L);
            st.takeItems(4329, -1L);
            st.takeItems(4330, -1L);
            st.takeItems(4331, -1L);
            st.takeItems(4425, -1L);
            st.exitQuest(true);
         }

         return htmltext;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = Quest.getNoQuestMsg(player);
      QuestState st = player.getQuestState("_422_RepentYourSins");
      if (st == null) {
         return htmltext;
      } else {
         int condition = st.getInt("cond");
         int npcId = npc.getId();
         int id = st.getState();
         switch(npcId) {
            case 30300:
               if (condition >= 14) {
                  if (st.getQuestItemsCount(4331) == 1L) {
                     if (st.getQuestItemsCount(1873) < 10L
                        || st.getQuestItemsCount(1880) < 5L
                        || st.getQuestItemsCount(1877) < 2L
                        || st.getQuestItemsCount(1879) < 10L
                        || st.getQuestItemsCount(1892) < 1L) {
                        htmltext = "30300-02.htm";
                     } else if (st.getQuestItemsCount(1873) >= 10L
                        && st.getQuestItemsCount(1880) >= 5L
                        && st.getQuestItemsCount(1877) >= 2L
                        && st.getQuestItemsCount(1879) >= 10L
                        && st.getQuestItemsCount(1892) >= 1L) {
                        htmltext = "30300-02.htm";
                        st.set("cond", "15");
                        st.takeItems(4331, 1L);
                        st.takeItems(1873, 10L);
                        st.takeItems(1877, 2L);
                        st.takeItems(1879, 10L);
                        st.takeItems(1880, 5L);
                        st.takeItems(1892, 1L);
                        st.giveItems(4330, 1L);
                        st.playSound("ItemSound.quest_middle");
                     }
                  } else if (st.getQuestItemsCount(4330) > 0L || st.getQuestItemsCount(4425) > 0L || st.getQuestItemsCount(4426) > 0L) {
                     htmltext = "30300-03.htm";
                  }
               }
               break;
            case 30597:
               if (condition == 3) {
                  st.set("cond", "7");
                  htmltext = "30597-01.htm";
               } else if (condition == 7) {
                  if (st.getQuestItemsCount(4327) < 10L) {
                     htmltext = "30597-02.htm";
                  } else {
                     st.set("cond", "11");
                     htmltext = "30597-03.htm";
                     st.takeItems(4327, -1L);
                  }
               } else if (condition == 11) {
                  htmltext = "30597-04.htm";
               }
               break;
            case 30612:
               if (condition == 4) {
                  st.set("cond", "8");
                  htmltext = "30612-01.htm";
               } else if (condition == 8) {
                  if (st.getQuestItemsCount(4328) < 1L) {
                     htmltext = "30612-02.htm";
                  } else {
                     st.set("cond", "12");
                     htmltext = "30612-03.htm";
                     st.takeItems(4328, -1L);
                  }
               } else if (condition == 12) {
                  htmltext = "30612-04.htm";
               }
               break;
            case 30668:
               if (condition == 2) {
                  st.set("cond", "6");
                  htmltext = "30668-01.htm";
               } else if (condition == 6) {
                  if (st.getQuestItemsCount(4326) < 10L) {
                     htmltext = "30668-02.htm";
                  } else {
                     st.set("cond", "10");
                     htmltext = "30668-03.htm";
                     st.takeItems(4326, -1L);
                  }
               } else if (condition == 10) {
                  htmltext = "30668-04.htm";
               }
               break;
            case 30718:
               if (condition == 5) {
                  st.set("cond", "9");
                  htmltext = "30718-01.htm";
               } else if (condition == 9) {
                  if (st.getQuestItemsCount(4329) < 3L) {
                     htmltext = "30718-02.htm";
                  } else if (st.getQuestItemsCount(4329) >= 3L) {
                     st.set("cond", "13");
                     htmltext = "30718-03.htm";
                     st.takeItems(4329, -1L);
                  }
               } else if (condition == 13) {
                  htmltext = "30718-04.htm";
               }
               break;
            case 30981:
               if (id == 0) {
                  if (player.getPkKills() >= 1) {
                     htmltext = "30981-02.htm";
                  } else {
                     htmltext = "30981-01.htm";
                     st.exitQuest(true);
                  }
               } else if (condition <= 9) {
                  htmltext = "30981-07.htm";
               } else if (condition == 13 && st.getQuestItemsCount(4426) > 0L) {
                  htmltext = "30981-10.htm";
               } else if (condition <= 13 && condition > 9 && st.getQuestItemsCount(4331) == 0L) {
                  htmltext = "30981-08.htm";
                  st.set("cond", "14");
                  st.giveItems(4331, 1L);
               } else if (condition == 14 && st.getQuestItemsCount(4331) > 0L) {
                  htmltext = "30981-09.htm";
               } else if (condition == 15 && st.getQuestItemsCount(4330) > 0L) {
                  htmltext = "30981-10.htm";
               } else if (condition >= 16) {
                  if (st.getQuestItemsCount(4425) > 0L) {
                     int plevel = this.findPetLvl(player, 4425);
                     int level = player.getLevel();
                     if (st.getInt("level") > level) {
                        level = st.getInt("level");
                     }

                     if (plevel > 0) {
                        if (plevel > level) {
                           htmltext = "30981-13.htm";
                        } else {
                           htmltext = "30981-12.htm";
                        }
                     } else {
                        htmltext = "30981-12.htm";
                     }
                  } else {
                     htmltext = "30981-18.htm";
                  }
               }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_422_RepentYourSins");
      if (st == null) {
         return null;
      } else if (st.getState() != 1) {
         return null;
      } else {
         int condition = st.getInt("cond");
         int npcId = npc.getId();
         long skulls = st.getQuestItemsCount(4326);
         long tails = st.getQuestItemsCount(4327);
         long heart = st.getQuestItemsCount(4328);
         long sacs = st.getQuestItemsCount(4329);
         switch(npcId) {
            case 20039:
               if (condition == 6 && skulls < 10L) {
                  st.giveItems(4326, 1L);
                  if (st.getQuestItemsCount(4326) == 10L) {
                     st.playSound("ItemSound.quest_middle");
                  } else {
                     st.playSound("ItemSound.quest_itemget");
                  }
               }
               break;
            case 20193:
               if (condition == 8 && heart < 1L) {
                  st.giveItems(4328, 1L);
                  st.playSound("ItemSound.quest_middle");
               }
               break;
            case 20494:
               if (condition == 7 && tails < 10L) {
                  st.giveItems(4327, 1L);
                  if (st.getQuestItemsCount(4327) == 10L) {
                     st.playSound("ItemSound.quest_middle");
                  } else {
                     st.playSound("ItemSound.quest_itemget");
                  }
               }
               break;
            case 20561:
               if (condition == 9 && sacs < 3L) {
                  st.giveItems(4329, 1L);
                  if (st.getQuestItemsCount(4329) == 3L) {
                     st.playSound("ItemSound.quest_middle");
                  } else {
                     st.playSound("ItemSound.quest_itemget");
                  }
               }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _422_RepentYourSins(422, "_422_RepentYourSins", "");
   }
}
