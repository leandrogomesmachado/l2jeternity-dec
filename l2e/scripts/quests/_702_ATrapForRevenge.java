package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _702_ATrapForRevenge extends Quest {
   private static final String qn = "_702_ATrapForRevenge";
   private static final int Plenos = 32563;
   private static final int Lekon = 32557;
   private static final int Tenius = 32555;
   private static final int[] Monsters = new int[]{22612, 22613, 25632, 22610, 22611, 25631, 25626};
   private static final int DrakeFlesh = 13877;
   private static final int RottenBlood = 13878;
   private static final int BaitForDrakes = 13879;
   private static final int VariantDrakeWingHorns = 13880;
   private static final int ExtractedRedStarStone = 14009;

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_702_ATrapForRevenge");
      if (st == null) {
         return getNoQuestMsg(player);
      } else {
         if (event.equalsIgnoreCase("32563-04.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("32563-07.htm")) {
            if (st.hasQuestItems(13877)) {
               htmltext = "32563-08.htm";
            } else {
               htmltext = "32563-07.htm";
            }
         } else if (event.equalsIgnoreCase("32563-09.htm")) {
            long count = st.getQuestItemsCount(13877);
            st.giveItems(57, count * 100L);
            st.takeItems(13877, count);
         } else if (event.equalsIgnoreCase("32563-11.htm")) {
            if (st.hasQuestItems(13880)) {
               long count = st.getQuestItemsCount(13880);
               st.giveItems(57, count * 200000L);
               st.takeItems(13880, count);
               htmltext = "32563-12.htm";
            } else {
               htmltext = "32563-11.htm";
            }
         } else if (event.equalsIgnoreCase("32563-14.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         } else if (event.equalsIgnoreCase("32557-03.htm")) {
            if (!st.hasQuestItems(13878) && st.getQuestItemsCount(14009) < 100L) {
               htmltext = "32557-03.htm";
            } else if (st.hasQuestItems(13878) && st.getQuestItemsCount(14009) < 100L) {
               htmltext = "32557-04.htm";
            } else if (!st.hasQuestItems(13878) && st.getQuestItemsCount(14009) >= 100L) {
               htmltext = "32557-05.htm";
            } else if (st.hasQuestItems(13878) && st.getQuestItemsCount(14009) >= 100L) {
               st.giveItems(13879, 1L);
               st.takeItems(13878, 1L);
               st.takeItems(14009, 100L);
               htmltext = "32557-06.htm";
            }
         } else if (event.equalsIgnoreCase("32555-03.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("32555-05.htm")) {
            st.exitQuest(true);
            st.playSound("ItemSound.quest_finish");
         } else if (event.equalsIgnoreCase("32555-06.htm")) {
            if (st.getQuestItemsCount(13877) < 100L) {
               htmltext = "32555-06.htm";
            } else {
               htmltext = "32555-07.htm";
            }
         } else if (event.equalsIgnoreCase("32555-08.htm")) {
            st.giveItems(13878, 1L);
            st.takeItems(13877, 100L);
         } else if (event.equalsIgnoreCase("32555-10.htm")) {
            if (st.hasQuestItems(13880)) {
               htmltext = "32555-11.htm";
            } else {
               htmltext = "32555-10.htm";
            }
         } else if (event.equalsIgnoreCase("32555-15.htm")) {
            int i0 = getRandom(1000);
            int i1 = getRandom(1000);
            if (i0 >= 500 && i1 >= 600) {
               st.giveItems(57, (long)(getRandom(49917) + 125000));
               if (i1 < 720) {
                  st.giveItems(9628, (long)(getRandom(3) + 1));
                  st.giveItems(9629, (long)(getRandom(3) + 1));
               } else if (i1 < 840) {
                  st.giveItems(9629, (long)(getRandom(3) + 1));
                  st.giveItems(9630, (long)(getRandom(3) + 1));
               } else if (i1 < 960) {
                  st.giveItems(9628, (long)(getRandom(3) + 1));
                  st.giveItems(9630, (long)(getRandom(3) + 1));
               } else if (i1 < 1000) {
                  st.giveItems(9628, (long)(getRandom(3) + 1));
                  st.giveItems(9629, (long)(getRandom(3) + 1));
                  st.giveItems(9630, (long)(getRandom(3) + 1));
               }

               htmltext = "32555-15.htm";
            } else if (i0 >= 500 && i1 < 600) {
               st.giveItems(57, (long)(getRandom(49917) + 125000));
               if (i1 >= 210) {
                  if (i1 < 340) {
                     st.giveItems(9628, (long)(getRandom(3) + 1));
                  } else if (i1 < 470) {
                     st.giveItems(9629, (long)(getRandom(3) + 1));
                  } else if (i1 < 600) {
                     st.giveItems(9630, (long)(getRandom(3) + 1));
                  }
               }

               htmltext = "32555-16.htm";
            } else if (i0 < 500 && i1 >= 600) {
               st.giveItems(57, (long)(getRandom(49917) + 25000));
               if (i1 < 720) {
                  st.giveItems(9628, (long)(getRandom(3) + 1));
                  st.giveItems(9629, (long)(getRandom(3) + 1));
               } else if (i1 < 840) {
                  st.giveItems(9629, (long)(getRandom(3) + 1));
                  st.giveItems(9630, (long)(getRandom(3) + 1));
               } else if (i1 < 960) {
                  st.giveItems(9628, (long)(getRandom(3) + 1));
                  st.giveItems(9630, (long)(getRandom(3) + 1));
               } else if (i1 < 1000) {
                  st.giveItems(9628, (long)(getRandom(3) + 1));
                  st.giveItems(9629, (long)(getRandom(3) + 1));
                  st.giveItems(9630, (long)(getRandom(3) + 1));
               }

               htmltext = "32555-17.htm";
            } else if (i0 < 500 && i1 < 600) {
               st.giveItems(57, (long)(getRandom(49917) + 25000));
               if (i1 >= 210) {
                  if (i1 < 340) {
                     st.giveItems(9628, (long)(getRandom(3) + 1));
                  } else if (i1 < 470) {
                     st.giveItems(9629, (long)(getRandom(3) + 1));
                  } else if (i1 < 600) {
                     st.giveItems(9630, (long)(getRandom(3) + 1));
                  }
               }

               htmltext = "32555-18.htm";
            }

            st.takeItems(13880, 1L);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_702_ATrapForRevenge");
      QuestState prev = player.getQuestState("_10273_GoodDayToFly");
      if (st == null) {
         return htmltext;
      } else {
         if (npc.getId() == 32563) {
            switch(st.getState()) {
               case 0:
                  if (prev != null && prev.getState() == 2 && player.getLevel() >= 78) {
                     htmltext = "32563-01.htm";
                  } else {
                     htmltext = "32563-02.htm";
                  }
                  break;
               case 1:
                  if (st.getInt("cond") == 1) {
                     htmltext = "32563-05.htm";
                  } else {
                     htmltext = "32563-06.htm";
                  }
            }
         }

         if (st.getState() == 1) {
            if (npc.getId() == 32557) {
               switch(st.getInt("cond")) {
                  case 1:
                     htmltext = "32557-01.htm";
                     break;
                  case 2:
                     htmltext = "32557-02.htm";
               }
            } else if (npc.getId() == 32555) {
               switch(st.getInt("cond")) {
                  case 1:
                     htmltext = "32555-01.htm";
                     break;
                  case 2:
                     htmltext = "32555-04.htm";
               }
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(player, 2);
      if (partyMember == null) {
         return null;
      } else {
         QuestState st = partyMember.getQuestState("_702_ATrapForRevenge");
         int chance = getRandom(1000);
         switch(npc.getId()) {
            case 22610:
               if (chance < 485) {
                  st.giveItems(13877, 2L);
               } else {
                  st.giveItems(13877, 1L);
               }
               break;
            case 22611:
               if (chance < 451) {
                  st.giveItems(13877, 2L);
               } else {
                  st.giveItems(13877, 1L);
               }
               break;
            case 22612:
               if (chance < 413) {
                  st.giveItems(13877, 2L);
               } else {
                  st.giveItems(13877, 1L);
               }
               break;
            case 22613:
               if (chance < 440) {
                  st.giveItems(13877, 2L);
               } else {
                  st.giveItems(13877, 1L);
               }
               break;
            case 25626:
               if (chance < 708) {
                  st.giveItems(13880, (long)(getRandom(2) + 1));
               } else if (chance < 978) {
                  st.giveItems(13880, (long)(getRandom(3) + 3));
               } else if (chance < 994) {
                  st.giveItems(13880, (long)(getRandom(4) + 6));
               } else if (chance < 998) {
                  st.giveItems(13880, (long)(getRandom(4) + 10));
               } else if (chance < 1000) {
                  st.giveItems(13880, (long)(getRandom(5) + 14));
               }
               break;
            case 25631:
               if (chance < 485) {
                  st.giveItems(13877, 2L);
               } else {
                  st.giveItems(13877, 1L);
               }
               break;
            case 25632:
               if (chance < 996) {
                  st.giveItems(13877, 1L);
               }
         }

         st.playSound("ItemSound.quest_itemget");
         return null;
      }
   }

   public _702_ATrapForRevenge(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32563);
      this.addTalkId(32563);
      this.addTalkId(32557);
      this.addTalkId(32555);

      for(int i : Monsters) {
         this.addKillId(i);
      }
   }

   public static void main(String[] args) {
      new _702_ATrapForRevenge(702, "_702_ATrapForRevenge", "");
   }
}
