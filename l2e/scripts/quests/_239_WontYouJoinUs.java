package l2e.scripts.quests;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _239_WontYouJoinUs extends Quest {
   private static final String qn = "_239_WontYouJoinUs";
   private static final int Athenia = 32643;
   private static final int WasteLandfillMachines = 18805;
   private static final int Suppressor = 22656;
   private static final int Exterminator = 22657;
   private static final int DestroyedMachinePiece = 14869;
   private static final int EnchantedGolemFragment = 14870;
   private static final int CerificateOfSupport = 14866;

   public _239_WontYouJoinUs(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(32643);
      this.addTalkId(32643);
      this.addKillId(18805);
      this.addKillId(22656);
      this.addKillId(22657);
      this.questItemIds = new int[]{14869, 14870};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_239_WontYouJoinUs");
      if (st == null) {
         return null;
      } else {
         if (event.equals("32643-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         }

         if (event.equals("32643-07.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_239_WontYouJoinUs");
      if (st == null) {
         return htmltext;
      } else {
         QuestState qs2 = player.getQuestState("_238_SuccesFailureOfBusiness");
         if (npc.getId() == 32643) {
            int cond = st.getInt("cond");
            if (st.getState() == 2) {
               htmltext = "32643-11.htm";
            } else if (cond == 0) {
               if (qs2 == null || qs2.getState() != 2) {
                  htmltext = "32643-00.htm";
               } else if (player.getLevel() >= 82) {
                  htmltext = "32643-01.htm";
               } else {
                  htmltext = "32643-00.htm";
                  st.exitQuest(true);
               }
            } else if (cond == 1) {
               if (st.getQuestItemsCount(14869) >= 1L) {
                  htmltext = "32643-05.htm";
               } else {
                  htmltext = "32643-04.htm";
               }
            } else if (cond == 2) {
               st.takeItems(14869, 10L);
               htmltext = "32643-06.htm";
            } else if (cond == 3) {
               if (st.getQuestItemsCount(14870) >= 1L) {
                  htmltext = "32643-08.htm";
               } else {
                  htmltext = "32643-09.htm";
               }
            } else if (cond == 4 && st.getQuestItemsCount(14870) == 20L) {
               htmltext = "32643-10.htm";
               st.giveItems(57, 283346L);
               st.takeItems(14866, 1L);
               st.takeItems(14870, 20L);
               st.addExpAndSp(1319736, 103553);
               st.unset("cond");
               st.setState((byte)2);
               st.exitQuest(false);
               st.playSound("ItemSound.quest_finish");
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMemberState(player, (byte)1);
      if (partyMember == null) {
         return null;
      } else {
         QuestState st = partyMember.getQuestState("_239_WontYouJoinUs");
         if (st == null) {
            return null;
         } else {
            int cond = st.getInt("cond");
            if (npc.getId() == 18805) {
               if (cond == 1) {
                  int count = 1;

                  int chance;
                  for(chance = (int)(5.0F * Config.RATE_QUEST_DROP); chance > 1000; ++count) {
                     chance -= 1000;
                     if (chance < 5) {
                        chance = 5;
                     }
                  }

                  if (getRandom(1000) <= chance) {
                     st.giveItems(14869, (long)count);
                     st.playSound("ItemSound.quest_itemget");
                     if (st.getQuestItemsCount(14869) == 10L) {
                        st.set("cond", "2");
                        st.playSound("ItemSound.quest_middle");
                     }
                  }
               }
            } else if ((npc.getId() == 22656 || npc.getId() == 22657) && cond == 3) {
               int count = 1;

               int chance;
               for(chance = (int)(5.0F * Config.RATE_QUEST_DROP); chance > 1000; ++count) {
                  chance -= 1000;
                  if (chance < 5) {
                     chance = 5;
                  }
               }

               if (getRandom(1000) <= chance) {
                  st.giveItems(14870, (long)count);
                  st.playSound("ItemSound.quest_itemget");
                  if (st.getQuestItemsCount(14870) == 20L) {
                     st.set("cond", "4");
                     st.playSound("ItemSound.quest_middle");
                  }
               }
            }

            return null;
         }
      }
   }

   public static void main(String[] args) {
      new _239_WontYouJoinUs(239, "_239_WontYouJoinUs", "");
   }
}
