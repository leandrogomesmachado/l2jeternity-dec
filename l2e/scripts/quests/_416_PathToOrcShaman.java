package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.serverpackets.SocialAction;

public class _416_PathToOrcShaman extends Quest {
   private static final String qn = "_416_PathToOrcShaman";
   private static final int TATARU_HESTUI = 30585;
   private static final int UMOS = 30502;
   private static final int H_TOTEM_SPIRIT = 30592;
   private static final int DM_TOTEM_SPIRIT = 30593;
   private static final int MOIRA = 31979;
   private static final int G_TOTEM_SPIRIT = 32057;
   private static final int CARCASS = 32090;
   private static final int[] TALKERS = new int[]{30585, 30502, 30592, 30593, 31979, 32057, 32090};
   private static final int FIRE_CHARM = 1616;
   private static final int KASHA_BEAR_PELT = 1617;
   private static final int KASHA_BSPIDER_HUSK = 1618;
   private static final int FIERY_EGG1 = 1619;
   private static final int HESTUI_MASK = 1620;
   private static final int FIERY_EGG2 = 1621;
   private static final int TOTEM_SPIRIT_CLAW = 1622;
   private static final int TATARUS_LETTER = 1623;
   private static final int FLAME_CHARM = 1624;
   private static final int GRIZZLY_BLOOD = 1625;
   private static final int BLOOD_CAULDRON = 1626;
   private static final int SPIRIT_NET = 1627;
   private static final int BOUND_DURKA_SPIRIT = 1628;
   private static final int DURKA_PARASITE = 1629;
   private static final int TOTEM_SPIRIT_BLOOD = 1630;
   private static final int MASK_OF_MEDIUM = 1631;
   private static final int[] QUESTITEMS = new int[]{1616, 1617, 1618, 1619, 1620, 1621, 1622, 1623, 1624, 1625, 1626, 1627, 1628, 1629, 1630};
   private static final int BEAR = 20335;
   private static final int SPIDER = 20038;
   private static final int SALAMANDER = 20415;
   private static final int TRACKER = 20043;
   private static final int KASHA_SPIDER = 20478;
   private static final int KASHA_BEAR = 20479;
   private static final int SPIRIT = 27056;
   private static final int LEOPARD = 27319;
   private static final int[] MOBS = new int[]{20335, 20038, 20415, 20043, 20478, 20479, 27056, 27319};

   public _416_PathToOrcShaman(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30585);

      for(int talkId : TALKERS) {
         this.addTalkId(talkId);
      }

      for(int mobId : MOBS) {
         this.addKillId(mobId);
      }

      this.questItemIds = QUESTITEMS;
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return super.onAdvEvent(event, npc, player);
      } else {
         if (event.equalsIgnoreCase("1")) {
            st.set("id", "0");
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1616, 1L);
            htmltext = "30585-06.htm";
         } else if (event.equalsIgnoreCase("32057_1")) {
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "14");
            htmltext = "32057-02.htm";
         } else if (event.equalsIgnoreCase("32057_2")) {
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "21");
            htmltext = "32057-05.htm";
         } else if (event.equalsIgnoreCase("32090_1")) {
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "18");
            htmltext = "32090-04.htm";
         } else if (event.equalsIgnoreCase("30585_1")) {
            if (player.getClassId().getId() != 49) {
               htmltext = player.getClassId().getId() == 50 ? "30585-02a.htm" : "30585-02.htm";
            } else if (player.getLevel() < 18) {
               htmltext = "30585-03.htm";
            } else {
               htmltext = st.getQuestItemsCount(1631) != 0L ? "30585-04.htm" : "30585-05.htm";
            }
         } else if (event.equalsIgnoreCase("30585_1a")) {
            htmltext = "30585-10a.htm";
         } else if (event.equalsIgnoreCase("30585_2")) {
            st.takeItems(1622, 1L);
            st.giveItems(1623, 1L);
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            htmltext = "30585-11.htm";
         } else if (event.equalsIgnoreCase("30585_3")) {
            st.takeItems(1622, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "12");
            htmltext = "30585-11a.htm";
         } else if (event.equalsIgnoreCase("30592_1")) {
            htmltext = "30592-02.htm";
         } else if (event.equalsIgnoreCase("30592_2")) {
            st.takeItems(1620, 1L);
            st.takeItems(1621, 1L);
            st.giveItems(1622, 1L);
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
            htmltext = "30592-03.htm";
         } else if (event.equalsIgnoreCase("30502_2")) {
            st.takeItems(1630, st.getQuestItemsCount(1630));
            String isFinished = st.getGlobalQuestVar("1ClassQuestFinished");
            if (isFinished.equalsIgnoreCase("")) {
               st.addExpAndSp(295862, 3440);
            }

            st.giveItems(1631, 1L);
            st.giveItems(57, 163800L);
            st.saveGlobalQuestVar("1ClassQuestFinished", "1");
            st.set("cond", "0");
            player.sendPacket(new SocialAction(player.getObjectId(), 3));
            player.sendPacket(new SocialAction(player.getObjectId(), 15));
            st.exitQuest(false);
            st.playSound("ItemSound.quest_finish");
            htmltext = "30502-07.htm";
         } else if (event.equalsIgnoreCase("30593_1")) {
            htmltext = "30593-02.htm";
         } else if (event.equalsIgnoreCase("30593_2")) {
            st.takeItems(1626, 1L);
            st.giveItems(1627, 1L);
            st.set("cond", "9");
            st.playSound("ItemSound.quest_middle");
            htmltext = "30593-03.htm";
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
         int npcId = npc.getId();
         int state = st.getState();
         int cond = st.getInt("cond");
         if (npcId != 30585 && state != 1) {
            return htmltext;
         } else {
            if (npcId == 30585 && cond == 0) {
               htmltext = "30585-01.htm";
            } else if (npcId == 30585 && cond == 12) {
               htmltext = "30585-11a.htm";
            } else if (npcId == 31979 && cond == 12) {
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "13");
               htmltext = "31979-01.htm";
            } else if (npcId == 31979 && cond == 21) {
               st.giveItems(1631, 1L);
               st.giveItems(57, 81900L);
               st.addExpAndSp(295862, 18194);
               talker.sendPacket(new SocialAction(talker.getObjectId(), 3));
               talker.sendPacket(new SocialAction(talker.getObjectId(), 15));
               st.set("cond", "0");
               st.exitQuest(false);
               st.playSound("ItemSound.quest_finish");
               htmltext = "31979-03.htm";
            } else if (npcId == 31979 && cond == 13) {
               htmltext = "31979-02.htm";
            } else if (npcId == 32057 && cond == 13) {
               htmltext = "32057-01.htm";
            } else if (npcId == 32057 && cond == 14) {
               htmltext = "32057-03.htm";
            } else if (npcId == 32057 && cond == 20) {
               htmltext = "32057-04.htm";
            } else if (npcId == 32057 && cond == 21) {
               htmltext = "32057-05.htm";
            } else if (npcId == 32090 && cond == 15) {
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "16");
               htmltext = "32090-01.htm";
            } else if (npcId == 32090 && cond == 16) {
               htmltext = "32090-01.htm";
            } else if (npcId == 32090 && cond == 17) {
               htmltext = "32090-02.htm";
            } else if (npcId == 32090 && cond == 18) {
               htmltext = "32090-05.htm";
            } else if (npcId == 32090 && cond == 19) {
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "20");
               htmltext = "32090-06.htm";
            } else if (npcId == 32090 && cond == 20) {
               htmltext = "32090-06.htm";
            } else if (npcId == 30585
               && cond > 0
               && st.getQuestItemsCount(1616) == 1L
               && st.getQuestItemsCount(1617) + st.getQuestItemsCount(1618) + st.getQuestItemsCount(1619) < 3L) {
               htmltext = "30585-07.htm";
            } else if (npcId == 30585
               && cond > 0
               && st.getQuestItemsCount(1616) == 1L
               && st.getQuestItemsCount(1617) + st.getQuestItemsCount(1618) + st.getQuestItemsCount(1619) >= 3L) {
               st.takeItems(1616, 1L);
               st.takeItems(1617, 1L);
               st.takeItems(1618, 1L);
               st.takeItems(1619, 1L);
               st.giveItems(1620, 1L);
               st.giveItems(1621, 1L);
               st.set("cond", "3");
               st.playSound("ItemSound.quest_middle");
               htmltext = "30585-08.htm";
            } else if (npcId == 30585 && cond > 0 && st.getQuestItemsCount(1620) == 1L && st.getQuestItemsCount(1621) == 1L) {
               htmltext = "30585-09.htm";
            } else if (npcId == 30585 && cond > 0 && st.getQuestItemsCount(1622) == 1L) {
               htmltext = "30585-10.htm";
            } else if (npcId == 30585 && cond > 0 && st.getQuestItemsCount(1623) == 1L) {
               htmltext = "30585-12.htm";
            } else if (npcId != 30585
               || cond <= 0
               || st.getQuestItemsCount(1625) <= 0L
                  && st.getQuestItemsCount(1624) <= 0L
                  && st.getQuestItemsCount(1626) <= 0L
                  && st.getQuestItemsCount(1627) <= 0L
                  && st.getQuestItemsCount(1628) <= 0L
                  && st.getQuestItemsCount(1630) <= 0L) {
               if (npcId == 30592 && cond > 0 && st.getQuestItemsCount(1620) > 0L && st.getQuestItemsCount(1621) > 0L) {
                  htmltext = "30592-01.htm";
               } else if (npcId == 30592 && cond > 0 && st.getQuestItemsCount(1622) > 0L) {
                  htmltext = "30592-04.htm";
               } else if (npcId != 30592
                  || cond <= 0
                  || st.getQuestItemsCount(1625) <= 0L
                     && st.getQuestItemsCount(1624) <= 0L
                     && st.getQuestItemsCount(1626) <= 0L
                     && st.getQuestItemsCount(1627) <= 0L
                     && st.getQuestItemsCount(1628) <= 0L
                     && st.getQuestItemsCount(1630) <= 0L
                     && st.getQuestItemsCount(1623) <= 0L) {
                  if (npcId == 30502 && cond > 0 && st.getQuestItemsCount(1623) > 0L) {
                     st.giveItems(1624, 1L);
                     st.takeItems(1623, 1L);
                     st.set("cond", "6");
                     st.playSound("ItemSound.quest_middle");
                     htmltext = "30502-01.htm";
                  } else if (npcId == 30502 && cond > 0 && st.getQuestItemsCount(1624) == 1L && st.getQuestItemsCount(1625) < 3L) {
                     htmltext = "30502-02.htm";
                  } else if (npcId == 30502 && cond > 0 && st.getQuestItemsCount(1624) == 1L && st.getQuestItemsCount(1625) >= 3L) {
                     st.takeItems(1624, 1L);
                     st.takeItems(1625, st.getQuestItemsCount(1625));
                     st.giveItems(1626, 1L);
                     st.set("cond", "8");
                     st.playSound("ItemSound.quest_middle");
                     htmltext = "30502-03.htm";
                  } else if (npcId == 30502 && cond > 0 && st.getQuestItemsCount(1626) == 1L) {
                     htmltext = "30502-04.htm";
                  } else if (npcId != 30502 || cond <= 0 || st.getQuestItemsCount(1628) != 1L && st.getQuestItemsCount(1627) != 1L) {
                     if (npcId == 30502 && cond > 0 && st.getQuestItemsCount(1630) == 1L) {
                        htmltext = "30502-06.htm";
                     } else if (npcId == 30593 && cond > 0 && st.getQuestItemsCount(1626) > 0L) {
                        htmltext = "30593-01.htm";
                     } else if (npcId == 30593 && cond > 0 && st.getQuestItemsCount(1627) > 0L && st.getQuestItemsCount(1628) == 0L) {
                        htmltext = "30593-04.htm";
                     } else if (npcId == 30593 && cond > 0 && st.getQuestItemsCount(1627) == 0L && st.getQuestItemsCount(1628) > 0L) {
                        st.takeItems(1628, 1L);
                        st.giveItems(1630, 1L);
                        st.set("cond", "11");
                        st.playSound("ItemSound.quest_middle");
                        htmltext = "30593-05.htm";
                     } else if (npcId == 30593 && cond == 1 && st.getQuestItemsCount(1630) > 0L) {
                        htmltext = "30593-06.htm";
                     }
                  } else {
                     htmltext = "30502-05.htm";
                  }
               } else {
                  htmltext = "30592-05.htm";
               }
            } else {
               htmltext = "30585-13.htm";
            }

            return htmltext;
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState st = killer.getQuestState(this.getName());
      if (st == null) {
         return super.onKill(npc, killer, isSummon);
      } else if (st.getState() != 1) {
         return super.onKill(npc, killer, isSummon);
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         if (npcId == 20479) {
            st.set("id", "0");
            if (cond > 0 && st.getQuestItemsCount(1616) == 1L && st.getQuestItemsCount(1617) < 1L) {
               if (st.getQuestItemsCount(1617) + st.getQuestItemsCount(1618) + st.getQuestItemsCount(1619) == 2L) {
                  st.giveItems(1617, 1L);
                  st.playSound("ItemSound.quest_middle");
                  st.set("cond", "2");
               } else {
                  st.giveItems(1617, 1L);
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         } else if (npcId == 27319) {
            if (cond == 14) {
               st.set("cond", "15");
               st.playSound("ItemSound.quest_middle");
               npc.broadcastNpcSay("My dear friend of " + killer.getName() + ", who has gone on ahead of me!");
            } else if (cond == 16) {
               st.set("cond", "17");
               st.playSound("ItemSound.quest_middle");
               npc.broadcastNpcSay("Listen to Tejakar Gandi, young Oroka! The spirit of the slain leopard is calling you, " + killer.getName() + "!");
            } else if (cond == 18) {
               st.set("cond", "19");
               st.playSound("ItemSound.quest_middle");
            }
         } else if (npcId == 20478) {
            st.set("id", "0");
            if (cond > 0 && st.getQuestItemsCount(1616) == 1L && st.getQuestItemsCount(1618) < 1L) {
               if (st.getQuestItemsCount(1617) + st.getQuestItemsCount(1618) + st.getQuestItemsCount(1619) == 2L) {
                  st.giveItems(1618, 1L);
                  st.playSound("ItemSound.quest_middle");
                  st.set("cond", "2");
               } else {
                  st.giveItems(1618, 1L);
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         } else if (npcId == 20415) {
            st.set("id", "0");
            if (cond > 0 && st.getQuestItemsCount(1616) == 1L && st.getQuestItemsCount(1619) < 1L) {
               if (st.getQuestItemsCount(1617) + st.getQuestItemsCount(1618) + st.getQuestItemsCount(1619) == 2L) {
                  st.giveItems(1619, 1L);
                  st.playSound("ItemSound.quest_middle");
                  st.set("cond", "2");
               } else {
                  st.giveItems(1619, 1L);
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         } else if (npcId == 20335) {
            st.set("id", "0");
            if (cond > 0 && st.getQuestItemsCount(1624) == 1L && st.getQuestItemsCount(1625) < 3L) {
               if (st.getQuestItemsCount(1625) == 2L) {
                  st.giveItems(1625, 1L);
                  st.playSound("ItemSound.quest_middle");
                  st.set("cond", "7");
               } else {
                  st.giveItems(1625, 1L);
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         } else if (npcId == 20038) {
            st.set("id", "0");
            if (cond > 0 && st.getQuestItemsCount(1627) == 1L && st.getQuestItemsCount(1628) == 0L && st.getQuestItemsCount(1629) < 8L) {
               int n = st.getRandom(10);
               if (st.getQuestItemsCount(1629) == 5L && n < 1) {
                  st.takeItems(1629, st.getQuestItemsCount(1629));
                  st.addSpawn(27056);
                  st.playSound("ItemSound.quest_itemget");
               } else if (st.getQuestItemsCount(1629) == 6L && n < 2) {
                  st.takeItems(1629, st.getQuestItemsCount(1629));
                  st.playSound("ItemSound.quest_itemget");
                  st.addSpawn(27056);
               } else if (st.getQuestItemsCount(1629) == 7L && n < 2) {
                  st.takeItems(1629, st.getQuestItemsCount(1629));
                  st.playSound("ItemSound.quest_itemget");
                  st.addSpawn(27056);
               } else if (st.getQuestItemsCount(1629) >= 7L) {
                  st.addSpawn(27056);
                  st.playSound("ItemSound.quest_itemget");
                  st.takeItems(1629, st.getQuestItemsCount(1629));
               } else {
                  st.giveItems(1629, 1L);
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         } else if (npcId == 20043) {
            st.set("id", "0");
            if (st.getInt("cond") > 0 && st.getQuestItemsCount(1627) == 1L && st.getQuestItemsCount(1628) == 0L && st.getQuestItemsCount(1629) < 8L) {
               int n = st.getRandom(10);
               if (st.getQuestItemsCount(1629) == 5L && n < 1) {
                  st.takeItems(1629, st.getQuestItemsCount(1629));
                  st.addSpawn(27056);
                  st.playSound("ItemSound.quest_itemget");
               } else if (st.getQuestItemsCount(1629) == 6L && n < 2) {
                  st.takeItems(1629, st.getQuestItemsCount(1629));
                  st.addSpawn(27056);
                  st.playSound("ItemSound.quest_itemget");
               } else if (st.getQuestItemsCount(1629) == 7L && n < 2) {
                  st.takeItems(1629, st.getQuestItemsCount(1629));
                  st.addSpawn(27056);
                  st.playSound("ItemSound.quest_itemget");
               } else if (st.getQuestItemsCount(1629) >= 7L) {
                  st.takeItems(1629, st.getQuestItemsCount(1629));
                  st.addSpawn(27056);
                  st.playSound("ItemSound.quest_itemget");
               } else {
                  st.giveItems(1629, 1L);
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         } else if (npcId == 27056) {
            st.set("id", "0");
            if (st.getInt("cond") > 0 && st.getQuestItemsCount(1627) == 1L && st.getQuestItemsCount(1628) == 0L) {
               st.giveItems(1628, 1L);
               st.takeItems(1627, 1L);
               st.takeItems(1629, st.getQuestItemsCount(1629));
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "10");
            }
         }

         return super.onKill(npc, killer, isSummon);
      }
   }

   public static void main(String[] args) {
      new _416_PathToOrcShaman(416, "_416_PathToOrcShaman", "");
   }
}
