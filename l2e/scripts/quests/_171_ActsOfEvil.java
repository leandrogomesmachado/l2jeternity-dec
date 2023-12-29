package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _171_ActsOfEvil extends Quest {
   private static final String qn = "_171_ActsOfEvil";
   private static final int ALVAH = 30381;
   private static final int ARODIN = 30207;
   private static final int TYRA = 30420;
   private static final int ROLENTO = 30437;
   private static final int NETI = 30425;
   private static final int BURAI = 30617;
   private static final int BLADE_MOLD = 4239;
   private static final int TYRAS_BILL = 4240;
   private static final int RANGERS_REPORT1 = 4241;
   private static final int RANGERS_REPORT2 = 4242;
   private static final int RANGERS_REPORT3 = 4243;
   private static final int RANGERS_REPORT4 = 4244;
   private static final int WEAPON_TRADE_CONTRACT = 4245;
   private static final int ATTACK_DIRECTIVES = 4246;
   private static final int CERTIFICATE = 4247;
   private static final int CARGOBOX = 4248;
   private static final int OL_MAHUM_HEAD = 4249;

   public _171_ActsOfEvil(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30381);
      this.addTalkId(new int[]{30381, 30207, 30420, 30437, 30425, 30617});
      this.addKillId(new int[]{20496, 20497, 20498, 20499, 20062, 20066, 20438});
      this.questItemIds = new int[]{4241, 4242, 4243, 4244, 4249, 4248, 4240, 4247, 4239, 4245};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_171_ActsOfEvil");
      if (st == null) {
         return event;
      } else {
         int cond = st.getInt("cond");
         if (event.equalsIgnoreCase("30381-02.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30207-02.htm") && cond == 1) {
            st.set("cond", "2");
         } else if (event.equalsIgnoreCase("30381-04.htm") && cond == 4) {
            st.set("cond", "5");
         } else if (event.equalsIgnoreCase("30381-07.htm") && cond == 6) {
            st.set("cond", "7");
            st.takeItems(4245, 1L);
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30437-03.htm") && cond == 8) {
            st.giveItems(4248, 1L);
            st.giveItems(4247, 1L);
            st.set("cond", "9");
         } else if (event.equalsIgnoreCase("30617-04.htm") && cond == 9) {
            st.takeItems(4247, 1L);
            st.takeItems(4246, 1L);
            st.takeItems(4248, 1L);
            st.set("cond", "10");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_171_ActsOfEvil");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 27 && player.getLevel() <= 32) {
                  htmltext = "30381-01.htm";
               } else {
                  htmltext = "30381-01a.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 30207:
                     if (cond == 1) {
                        return "30207-01.htm";
                     } else if (cond == 2) {
                        return "30207-01a.htm";
                     } else if (cond == 3) {
                        if (st.getQuestItemsCount(4240) == 1L) {
                           st.takeItems(4240, 1L);
                           htmltext = "30207-03.htm";
                           st.set("cond", "4");
                        } else {
                           htmltext = "30207-01a.htm";
                        }

                        return htmltext;
                     } else {
                        if (cond >= 4) {
                           return "30207-03a.htm";
                        }

                        return htmltext;
                     }
                  case 30381:
                     if (cond >= 1 && cond <= 3) {
                        return "30381-02a.htm";
                     } else if (cond == 4) {
                        return "30381-03.htm";
                     } else if (cond == 5) {
                        if (st.getQuestItemsCount(4241) == 1L
                           && st.getQuestItemsCount(4242) == 1L
                           && st.getQuestItemsCount(4243) == 1L
                           && st.getQuestItemsCount(4244) == 1L) {
                           htmltext = "30381-05.htm";
                           st.takeItems(4241, 1L);
                           st.takeItems(4242, 1L);
                           st.takeItems(4243, 1L);
                           st.takeItems(4244, 1L);
                           st.set("cond", "6");
                        } else {
                           htmltext = "30381-04a.htm";
                        }

                        return htmltext;
                     } else if (cond == 6) {
                        if (st.getQuestItemsCount(4245) == 1L && st.getQuestItemsCount(4246) == 1L) {
                           htmltext = "30381-06.htm";
                        } else {
                           htmltext = "30381-05a.htm";
                        }

                        return htmltext;
                     } else {
                        if (cond >= 7 && cond <= 10) {
                           htmltext = "30381-07a.htm";
                        } else if (cond == 11) {
                           htmltext = "30381-08.htm";
                           st.rewardItems(57, 90000L);
                           st.addExpAndSp(159820, 9182);
                           st.playSound("ItemSound.quest_finish");
                           st.unset("cond");
                           st.exitQuest(false);
                           return htmltext;
                        }

                        return htmltext;
                     }
                  case 30420:
                     if (cond == 2) {
                        if (st.getQuestItemsCount(4239) >= 20L) {
                           st.takeItems(4239, -1L);
                           st.giveItems(4240, 1L);
                           htmltext = "30420-01.htm";
                           st.set("cond", "3");
                        } else {
                           htmltext = "30420-01b.htm";
                        }

                        return htmltext;
                     } else {
                        if (cond == 3) {
                           htmltext = "30420-01a.htm";
                        } else if (cond > 3) {
                           return "30420-02.htm";
                        }

                        return htmltext;
                     }
                  case 30425:
                     if (cond == 7) {
                        htmltext = "30425-01.htm";
                        st.set("cond", "8");
                     } else if (cond >= 8) {
                        return "30425-02.htm";
                     }

                     return htmltext;
                  case 30437:
                     if (cond == 8) {
                        htmltext = "30437-01.htm";
                     } else if (cond >= 9) {
                        return "30437-03a.htm";
                     }

                     return htmltext;
                  case 30617:
                     if (cond == 9 && st.getQuestItemsCount(4247) == 1L && st.getQuestItemsCount(4248) == 1L && st.getQuestItemsCount(4246) == 1L) {
                        htmltext = "30617-01.htm";
                     } else if (cond == 10) {
                        if (st.getQuestItemsCount(4249) >= 30L) {
                           htmltext = "30617-05.htm";
                           st.giveItems(57, 8000L);
                           st.takeItems(4249, -1L);
                           st.set("cond", "11");
                           st.playSound("ItemSound.quest_itemget");
                        } else {
                           htmltext = "30617-04a.htm";
                        }

                        return htmltext;
                     }

                     return htmltext;
                  default:
                     return htmltext;
               }
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_171_ActsOfEvil");
      if (st == null) {
         return null;
      } else {
         int cond = st.getInt("cond");
         int chance = st.getRandom(100);
         switch(npc.getId()) {
            case 20062:
               if (cond == 5) {
                  if (st.getQuestItemsCount(4241) != 1L && chance < 100) {
                     st.giveItems(4241, 1L);
                     st.playSound("ItemSound.quest_itemget");
                  } else if (st.getQuestItemsCount(4242) != 1L && chance < 20) {
                     st.giveItems(4242, 1L);
                     st.playSound("ItemSound.quest_itemget");
                  } else if (st.getQuestItemsCount(4243) != 1L && chance < 20) {
                     st.giveItems(4243, 1L);
                     st.playSound("ItemSound.quest_itemget");
                  } else if (st.getQuestItemsCount(4244) != 1L && chance < 20) {
                     st.giveItems(4244, 1L);
                     st.playSound("ItemSound.quest_itemget");
                  }
               }
               break;
            case 20066:
               if (cond == 6) {
                  if (st.getQuestItemsCount(4245) != 1L && chance < 10) {
                     st.giveItems(4245, 1L);
                     st.playSound("ItemSound.quest_itemget");
                  } else if (st.getQuestItemsCount(4246) != 1L && chance < 10) {
                     st.giveItems(4246, 1L);
                     st.playSound("ItemSound.quest_itemget");
                  }
               }
               break;
            case 20438:
               if (cond == 10) {
                  int heads = (int)st.getQuestItemsCount(4249);
                  if (heads < 30 && chance < 50) {
                     st.giveItems(4249, 1L);
                     if (heads == 29) {
                        st.playSound("ItemSound.quest_middle");
                     } else {
                        st.playSound("ItemSound.quest_itemget");
                     }
                  }
               }
               break;
            case 20496:
            case 20497:
            case 20498:
            case 20499:
               if (cond == 2) {
                  if (chance < 10) {
                     st.addSpawn(27190);
                  }

                  if (chance < 50 && st.getQuestItemsCount(4239) < 20L) {
                     st.giveItems(4239, 1L);
                     if (st.getQuestItemsCount(4239) == 19L) {
                        st.playSound("ItemSound.quest_middle");
                     } else {
                        st.playSound("ItemSound.quest_itemget");
                     }
                  }
               }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _171_ActsOfEvil(171, "_171_ActsOfEvil", "");
   }
}
