package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.type.WeaponType;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.serverpackets.SocialAction;

public class _415_PathToOrcMonk extends Quest {
   private static final String qn = "_415_PathToOrcMonk";
   private static final int URUTU = 30587;
   private static final int KASMAN = 30501;
   private static final int ROSHEEK = 30590;
   private static final int TORUKU = 30591;
   private static final int MOIRA = 31979;
   private static final int AREN = 32056;
   private static final int[] TALKERS = new int[]{30587, 30501, 30590, 30591, 31979, 32056};
   private static final int POMEGRANATE = 1593;
   private static final int LEATHER_POUCH1 = 1594;
   private static final int LEATHER_POUCH2 = 1595;
   private static final int LEATHER_POUCH3 = 1596;
   private static final int LEATHER_POUCH1FULL = 1597;
   private static final int LEATHER_POUCH2FULL = 1598;
   private static final int LEATHER_POUCH3FULL = 1599;
   private static final int KASHA_BEAR_CLAW = 1600;
   private static final int KASHA_BSPIDER_TALON = 1601;
   private static final int S_SALAMANDER_SCALE = 1602;
   private static final int SCROLL_FIERY_SPIRIT = 1603;
   private static final int ROSHEEKS_LETTER = 1604;
   private static final int GANTAKIS_LETTER = 1605;
   private static final int FIG = 1606;
   private static final int LEATHER_PURSE4 = 1607;
   private static final int LEATHER_POUCH4FULL = 1608;
   private static final int VUKU_TUSK = 1609;
   private static final int RATMAN_FANG = 1610;
   private static final int LANGK_TOOTH = 1611;
   private static final int FELIM_TOOTH = 1612;
   private static final int SCROLL_IRON_WILL = 1613;
   private static final int TORUKUS_LETTER = 1614;
   private static final int KHAVATARI_TOTEM = 1615;
   private static final int SPIDER_TOOTH = 8545;
   private static final int HORN_BAAR = 8546;
   private static final int[] QUESTITEMS = new int[]{
      1593, 1594, 1595, 1596, 1597, 1598, 1599, 1600, 1601, 1602, 1603, 1604, 1605, 1606, 1607, 1608, 1609, 1610, 1611, 1612, 1613, 1614, 8545, 8546
   };
   private static final int F_LIZZARDMAN_WARRIOR = 20014;
   private static final int ORC_FIGHTER = 20017;
   private static final int L_LIZZARDMAN_WARRIOR = 20024;
   private static final int RATMAN_WARRIOR = 20359;
   private static final int SALAMANDER = 20415;
   private static final int TIMBER_SPIDER = 20476;
   private static final int BLADE_SPIDER = 20478;
   private static final int BEAR = 20479;
   private static final int VANUL = 21118;
   private static final int[] KILLS = new int[]{20014, 20017, 20024, 20359, 20415, 20476, 20478, 20479, 21118};

   public _415_PathToOrcMonk(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30587);

      for(int talkId : TALKERS) {
         this.addTalkId(talkId);
      }

      for(int killId : KILLS) {
         this.addKillId(killId);
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
         if (event.equalsIgnoreCase("30587_1")) {
            if (player.getClassId().getId() != 44) {
               if (player.getClassId().getId() == 47) {
                  htmltext = "30587-02a.htm";
                  st.exitQuest(true);
               } else {
                  htmltext = "30587-02.htm";
                  st.exitQuest(true);
               }
            } else if (player.getLevel() < 18) {
               htmltext = "30587-03.htm";
            } else {
               htmltext = st.getQuestItemsCount(1615) != 0L ? "30587-04.htm" : "30587-05.htm";
            }
         } else if (event.equalsIgnoreCase("1")) {
            st.set("id", "0");
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1593, 1L);
            htmltext = "30587-06.htm";
         } else if (event.equalsIgnoreCase("30587-09a.htm")) {
            st.takeItems(1604, 1L);
            st.giveItems(1605, 1L);
            st.set("cond", "9");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30587-09b.htm")) {
            st.takeItems(1604, 1L);
            st.giveItems(1605, 1L);
            st.set("cond", "14");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32056-03.htm")) {
            st.set("cond", "15");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32056-08.htm")) {
            st.set("cond", "19");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(8546, -1L);
         } else if (event.equalsIgnoreCase("31979-03.htm")) {
            st.takeItems(1603, 1L);
            String isFinished = st.getGlobalQuestVar("1ClassQuestFinished");
            if (isFinished.equalsIgnoreCase("")) {
               st.addExpAndSp(295862, 4590);
            }

            st.giveItems(1615, 1L);
            st.giveItems(57, 163800L);
            st.saveGlobalQuestVar("1ClassQuestFinished", "1");
            st.set("cond", "0");
            st.set("onlyone", "1");
            player.sendPacket(new SocialAction(player.getObjectId(), 3));
            st.exitQuest(false);
            st.playSound("ItemSound.quest_finish");
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player talker) {
      String htmltext = Quest.getNoQuestMsg(talker);
      QuestState st = talker.getQuestState("_415_PathToOrcMonk");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int state = st.getState();
         int cond = st.getInt("cond");
         if (npcId != 30587 && state != 1) {
            return htmltext;
         } else {
            if (npcId == 30587 && cond == 0 && st.getInt("onlyone") == 0) {
               htmltext = "30587-01.htm";
            } else if (npcId == 30587 && cond == 0 && st.getInt("onlyone") == 1) {
               htmltext = "30587-04.htm";
            } else if (npcId == 30587
               && cond > 0
               && st.getQuestItemsCount(1603) == 0L
               && st.getQuestItemsCount(1593) == 1L
               && st.getQuestItemsCount(1605) == 0L
               && st.getQuestItemsCount(1604) == 0L
               && st.getQuestItemsCount(1594)
                     + st.getQuestItemsCount(1595)
                     + st.getQuestItemsCount(1596)
                     + st.getQuestItemsCount(1597)
                     + st.getQuestItemsCount(1598)
                     + st.getQuestItemsCount(1599)
                  == 0L) {
               htmltext = "30587-07.htm";
            } else if (npcId == 30587
               && cond > 0
               && st.getQuestItemsCount(1603) == 0L
               && st.getQuestItemsCount(1593) == 0L
               && st.getQuestItemsCount(1605) == 0L
               && st.getQuestItemsCount(1604) == 0L
               && st.getQuestItemsCount(1594)
                     + st.getQuestItemsCount(1595)
                     + st.getQuestItemsCount(1596)
                     + st.getQuestItemsCount(1597)
                     + st.getQuestItemsCount(1598)
                     + st.getQuestItemsCount(1599)
                  == 1L) {
               htmltext = "30587-08.htm";
            } else if (npcId == 30587
               && cond > 0
               && st.getQuestItemsCount(1603) == 1L
               && st.getQuestItemsCount(1593) == 0L
               && st.getQuestItemsCount(1605) == 0L
               && st.getQuestItemsCount(1604) == 1L
               && st.getQuestItemsCount(1594)
                     + st.getQuestItemsCount(1595)
                     + st.getQuestItemsCount(1596)
                     + st.getQuestItemsCount(1597)
                     + st.getQuestItemsCount(1598)
                     + st.getQuestItemsCount(1599)
                  == 0L) {
               htmltext = "30587-09.htm";
            } else if (npcId == 30587 && cond >= 14) {
               htmltext = "30587-09b.htm";
            } else if (npcId == 30587
               && cond > 0
               && st.getQuestItemsCount(1603) == 1L
               && st.getQuestItemsCount(1593) == 0L
               && st.getQuestItemsCount(1605) == 1L
               && st.getQuestItemsCount(1604) == 0L
               && st.getQuestItemsCount(1594)
                     + st.getQuestItemsCount(1595)
                     + st.getQuestItemsCount(1596)
                     + st.getQuestItemsCount(1597)
                     + st.getQuestItemsCount(1598)
                     + st.getQuestItemsCount(1599)
                  == 0L) {
               htmltext = "30587-10.htm";
            } else if (npcId == 30587
               && cond > 0
               && st.getQuestItemsCount(1603) == 1L
               && st.getQuestItemsCount(1593) == 0L
               && st.getQuestItemsCount(1605) == 0L
               && st.getQuestItemsCount(1604) == 0L
               && st.getQuestItemsCount(1594)
                     + st.getQuestItemsCount(1595)
                     + st.getQuestItemsCount(1596)
                     + st.getQuestItemsCount(1597)
                     + st.getQuestItemsCount(1598)
                     + st.getQuestItemsCount(1599)
                  == 0L) {
               htmltext = "30587-11.htm";
            } else if (npcId == 30590 && cond > 0 && st.getQuestItemsCount(1593) > 0L) {
               st.takeItems(1593, 1L);
               st.giveItems(1594, 1L);
               st.set("cond", "2");
               st.playSound("ItemSound.quest_middle");
               htmltext = "30590-01.htm";
            } else if (npcId == 30590 && cond > 0 && st.getQuestItemsCount(1594) > 0L && st.getQuestItemsCount(1597) == 0L) {
               htmltext = "30590-02.htm";
            } else if (npcId == 30590 && cond > 0 && st.getQuestItemsCount(1594) == 0L && st.getQuestItemsCount(1597) > 0L) {
               st.takeItems(1597, 1L);
               st.giveItems(1595, 1L);
               st.set("cond", "4");
               st.playSound("ItemSound.quest_middle");
               htmltext = "30590-03.htm";
            } else if (npcId == 30590 && cond > 0 && st.getQuestItemsCount(1595) == 1L && st.getQuestItemsCount(1598) == 0L) {
               htmltext = "30590-04.htm";
            } else if (npcId == 30590 && cond > 0 && st.getQuestItemsCount(1595) == 0L && st.getQuestItemsCount(1598) == 1L) {
               st.takeItems(1598, 1L);
               st.giveItems(1596, 1L);
               st.set("cond", "6");
               st.playSound("ItemSound.quest_middle");
               htmltext = "30590-05.htm";
            } else if (npcId == 30590 && cond > 0 && st.getQuestItemsCount(1596) == 1L && st.getQuestItemsCount(1599) == 0L) {
               htmltext = "30590-06.htm";
            } else if (npcId == 30590 && cond > 0 && st.getQuestItemsCount(1596) == 0L && st.getQuestItemsCount(1599) == 1L) {
               st.takeItems(1599, 1L);
               st.giveItems(1603, 1L);
               st.giveItems(1604, 1L);
               st.set("cond", "8");
               st.playSound("ItemSound.quest_middle");
               htmltext = "30590-07.htm";
            } else if (npcId == 30590 && cond > 0 && st.getQuestItemsCount(1604) == 1L && st.getQuestItemsCount(1603) == 1L) {
               htmltext = "30590-08.htm";
            } else if (npcId == 30590 && cond > 0 && st.getQuestItemsCount(1604) == 0L && st.getQuestItemsCount(1603) == 1L) {
               htmltext = "30590-09.htm";
            } else if (npcId == 30501 && cond > 0 && st.getQuestItemsCount(1605) > 0L) {
               st.takeItems(1605, 1L);
               st.giveItems(1606, 1L);
               st.set("cond", "10");
               st.playSound("ItemSound.quest_middle");
               htmltext = "30501-01.htm";
            } else if (npcId != 30501
               || cond <= 0
               || st.getQuestItemsCount(1606) <= 0L
               || st.getQuestItemsCount(1607) != 0L && st.getQuestItemsCount(1608) != 0L) {
               if (npcId != 30501 || cond <= 0 || st.getQuestItemsCount(1606) != 0L || st.getQuestItemsCount(1607) != 1L && st.getQuestItemsCount(1608) != 1L) {
                  if (npcId == 30501 && cond > 0 && st.getQuestItemsCount(1613) > 0L) {
                     st.takeItems(1613, 1L);
                     st.takeItems(1603, 1L);
                     st.takeItems(1614, 1L);
                     st.giveItems(1615, 1L);
                     st.giveItems(57, 81900L);
                     st.addExpAndSp(295862, 19344);
                     talker.sendPacket(new SocialAction(talker.getObjectId(), 3));
                     st.set("cond", "0");
                     st.set("onlyone", "1");
                     st.exitQuest(false);
                     st.playSound("ItemSound.quest_finish");
                     htmltext = "30501-04.htm";
                  } else if (npcId == 30591 && cond > 0 && st.getQuestItemsCount(1606) > 0L) {
                     st.takeItems(1606, 1L);
                     st.giveItems(1607, 1L);
                     st.set("cond", "11");
                     st.playSound("ItemSound.quest_middle");
                     htmltext = "30591-01.htm";
                  } else if (npcId == 30591 && cond > 0 && st.getQuestItemsCount(1607) > 0L && st.getQuestItemsCount(1608) == 0L) {
                     htmltext = "30591-02.htm";
                  } else if (npcId == 30591 && cond > 0 && st.getQuestItemsCount(1607) == 0L && st.getQuestItemsCount(1608) == 1L) {
                     st.takeItems(1608, 1L);
                     st.giveItems(1613, 1L);
                     st.giveItems(1614, 1L);
                     st.set("cond", "13");
                     st.playSound("ItemSound.quest_middle");
                     htmltext = "30591-03.htm";
                  } else if (npcId == 30591 && cond > 0 && st.getQuestItemsCount(1613) == 1L && st.getQuestItemsCount(1614) == 1L) {
                     htmltext = "30591-04.htm";
                  } else if (npcId == 32056) {
                     if (cond == 14) {
                        htmltext = "32056-01.htm";
                     } else if (cond == 15) {
                        htmltext = "32056-04.htm";
                     } else if (cond == 16) {
                        st.set("cond", "17");
                        st.playSound("ItemSound.quest_middle");
                        st.takeItems(8545, -1L);
                        htmltext = "32056-05.htm";
                     } else if (cond == 17) {
                        htmltext = "32056-06.htm";
                     } else if (cond == 18) {
                        htmltext = "32056-07.htm";
                     } else if (cond == 19) {
                        htmltext = "32056-09.htm";
                     }
                  } else if (npcId == 31979 && cond == 19) {
                     htmltext = "31979-01.htm";
                  }
               } else {
                  htmltext = "30501-03.htm";
               }
            } else {
               htmltext = "30501-02.htm";
            }

            return htmltext;
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState st = killer.getQuestState("_415_PathToOrcMonk");
      if (st == null) {
         return super.onKill(npc, killer, isSummon);
      } else if (st.getState() != 1) {
         return super.onKill(npc, killer, isSummon);
      } else if (killer.getActiveWeaponItem() != null
         && killer.getActiveWeaponItem().getItemType() != WeaponType.FIST
         && killer.getActiveWeaponItem().getItemType() != WeaponType.DUALFIST) {
         return super.onKill(npc, killer, isSummon);
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         if (npcId == 20479) {
            st.set("id", "0");
            if (cond > 0 && st.getQuestItemsCount(1594) == 1L) {
               if (st.getQuestItemsCount(1600) == 4L) {
                  st.takeItems(1600, st.getQuestItemsCount(1600));
                  st.takeItems(1594, st.getQuestItemsCount(1594));
                  st.giveItems(1597, 1L);
                  st.playSound("ItemSound.quest_middle");
                  st.set("cond", "3");
               } else {
                  st.giveItems(1600, 1L);
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         } else if (npcId == 20415) {
            st.set("id", "0");
            if (cond > 0 && st.getQuestItemsCount(1596) == 1L) {
               if (st.getQuestItemsCount(1602) == 4L) {
                  st.takeItems(1602, st.getQuestItemsCount(1602));
                  st.takeItems(1596, st.getQuestItemsCount(1596));
                  st.giveItems(1599, 1L);
                  st.playSound("ItemSound.quest_middle");
                  st.set("cond", "7");
               } else {
                  st.giveItems(1602, 1L);
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         } else if (npcId == 20476 && cond == 15) {
            if (st.getQuestItemsCount(8545) < 6L && st.getRandom(100) <= 50) {
               if (st.getQuestItemsCount(8545) == 5L) {
                  st.playSound("ItemSound.quest_middle");
                  st.set("cond", "16");
               } else {
                  st.playSound("ItemSound.quest_itemget");
                  st.giveItems(8545, 1L);
               }
            }
         } else if (npcId == 20478) {
            st.set("id", "0");
            if (cond > 0 && st.getQuestItemsCount(1595) == 1L) {
               if (st.getQuestItemsCount(1601) == 4L) {
                  st.takeItems(1601, st.getQuestItemsCount(1601));
                  st.takeItems(1595, st.getQuestItemsCount(1595));
                  st.giveItems(1598, 1L);
                  st.playSound("ItemSound.quest_middle");
                  st.set("cond", "5");
               } else {
                  st.giveItems(1601, 1L);
                  st.playSound("ItemSound.quest_itemget");
               }
            } else if (cond == 15 && st.getQuestItemsCount(8545) < 6L && st.getRandom(100) <= 50) {
               if (st.getQuestItemsCount(8545) == 5L) {
                  st.playSound("ItemSound.quest_middle");
                  st.set("cond", "16");
               } else {
                  st.playSound("ItemSound.quest_itemget");
                  st.giveItems(8545, 1L);
               }
            }
         } else if (npcId == 20017) {
            st.set("id", "0");
            if (cond > 0 && st.getQuestItemsCount(1607) == 1L && st.getQuestItemsCount(1609) < 3L) {
               if (st.getQuestItemsCount(1610) + st.getQuestItemsCount(1611) + st.getQuestItemsCount(1612) + st.getQuestItemsCount(1609) >= 11L) {
                  st.takeItems(1609, st.getQuestItemsCount(1609));
                  st.takeItems(1610, st.getQuestItemsCount(1610));
                  st.takeItems(1611, st.getQuestItemsCount(1611));
                  st.takeItems(1612, st.getQuestItemsCount(1612));
                  st.takeItems(1607, 1L);
                  st.giveItems(1608, 1L);
                  st.playSound("ItemSound.quest_middle");
                  st.set("cond", "12");
               } else {
                  st.giveItems(1609, 1L);
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         } else if (npcId == 20359) {
            st.set("id", "0");
            if (cond > 0 && st.getQuestItemsCount(1607) == 1L && st.getQuestItemsCount(1610) < 3L) {
               if (st.getQuestItemsCount(1610) + st.getQuestItemsCount(1611) + st.getQuestItemsCount(1612) + st.getQuestItemsCount(1609) >= 11L) {
                  st.takeItems(1609, st.getQuestItemsCount(1609));
                  st.takeItems(1610, st.getQuestItemsCount(1610));
                  st.takeItems(1611, st.getQuestItemsCount(1611));
                  st.takeItems(1612, st.getQuestItemsCount(1612));
                  st.takeItems(1607, 1L);
                  st.giveItems(1608, 1L);
                  st.playSound("ItemSound.quest_middle");
                  st.set("cond", "12");
               } else {
                  st.giveItems(1610, 1L);
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         } else if (npcId == 20024) {
            st.set("id", "0");
            if (cond > 0 && st.getQuestItemsCount(1607) == 1L && st.getQuestItemsCount(1611) < 3L) {
               if (st.getQuestItemsCount(1610) + st.getQuestItemsCount(1611) + st.getQuestItemsCount(1612) + st.getQuestItemsCount(1609) >= 11L) {
                  st.takeItems(1609, st.getQuestItemsCount(1609));
                  st.takeItems(1610, st.getQuestItemsCount(1610));
                  st.takeItems(1611, st.getQuestItemsCount(1611));
                  st.takeItems(1612, st.getQuestItemsCount(1612));
                  st.takeItems(1607, 1L);
                  st.giveItems(1608, 1L);
                  st.playSound("ItemSound.quest_middle");
                  st.set("cond", "12");
               } else {
                  st.giveItems(1611, 1L);
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         } else if (npcId == 20014) {
            st.set("id", "0");
            if (cond > 0 && st.getQuestItemsCount(1607) == 1L && st.getQuestItemsCount(1612) < 3L) {
               if (st.getQuestItemsCount(1610) + st.getQuestItemsCount(1611) + st.getQuestItemsCount(1612) + st.getQuestItemsCount(1609) >= 11L) {
                  st.takeItems(1609, st.getQuestItemsCount(1609));
                  st.takeItems(1610, st.getQuestItemsCount(1610));
                  st.takeItems(1611, st.getQuestItemsCount(1611));
                  st.takeItems(1612, st.getQuestItemsCount(1612));
                  st.takeItems(1607, 1L);
                  st.giveItems(1608, 1L);
                  st.playSound("ItemSound.quest_middle");
                  st.set("cond", "12");
               } else {
                  st.giveItems(1612, 1L);
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         } else if (npcId == 21118 && cond == 17) {
            st.giveItems(8546, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "18");
         }

         return super.onKill(npc, killer, isSummon);
      }
   }

   public static void main(String[] args) {
      new _415_PathToOrcMonk(415, "_415_PathToOrcMonk", "");
   }
}
