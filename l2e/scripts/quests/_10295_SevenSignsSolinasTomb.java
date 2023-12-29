package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _10295_SevenSignsSolinasTomb extends Quest {
   public _10295_SevenSignsSolinasTomb(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32792);
      this.addTalkId(new int[]{32792, 32787, 32820, 32857, 32858, 32859, 32860, 32837, 32842, 32793});
      this.addKillId(new int[]{18952, 18953, 18954, 18955});
      this.questItemIds = new int[]{17228, 17229, 17230, 17231};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32792-05.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("32857-02.htm")) {
            if (st.getQuestItemsCount(17231) == 0L) {
               st.giveItems(17231, 1L);
            } else {
               htmltext = "empty-atlar.htm";
            }
         } else if (event.equalsIgnoreCase("32859-02.htm")) {
            if (st.getQuestItemsCount(17228) == 0L) {
               st.giveItems(17228, 1L);
            } else {
               htmltext = "empty-atlar.htm";
            }
         } else if (event.equalsIgnoreCase("32858-02.htm")) {
            if (st.getQuestItemsCount(17230) == 0L) {
               st.giveItems(17230, 1L);
            } else {
               htmltext = "empty-atlar.htm";
            }
         } else if (event.equalsIgnoreCase("32860-02.htm")) {
            if (st.getQuestItemsCount(17229) == 0L) {
               st.giveItems(17229, 1L);
            } else {
               htmltext = "empty-atlar.htm";
            }
         } else if (event.equalsIgnoreCase("32793-04.htm")) {
            st.setCond(2, true);
         } else if (event.equalsIgnoreCase("32793-08.htm")) {
            st.setCond(3, true);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getCond();
         int ac = st.getInt("active");
         if (player.isSubClassActive()) {
            return "no_subclass-allowed.htm";
         } else {
            if (st.getState() == 0) {
               if (npcId == 32792) {
                  QuestState qs = player.getQuestState("_10294_SevenSignToTheMonastery");
                  if (cond == 0) {
                     if (player.getLevel() >= 81 && qs != null && qs.isCompleted()) {
                        htmltext = "32792-01.htm";
                     } else {
                        htmltext = "32792-00a.htm";
                        st.exitQuest(true);
                     }
                  }
               }
            } else if (st.getState() == 1) {
               if (npcId == 32792) {
                  if (cond == 1) {
                     htmltext = "32792-06.htm";
                  } else if (cond == 2) {
                     htmltext = "32792-07.htm";
                  } else if (cond == 3) {
                     if (player.getLevel() >= 81) {
                        htmltext = "32792-08.htm";
                        st.unset("active");
                        st.unset("first");
                        st.unset("second");
                        st.unset("third");
                        st.unset("fourth");
                        st.unset("firstgroup");
                        st.unset("secondgroup");
                        st.unset("thirdgroup");
                        st.unset("fourthgroup");
                        st.unset("activity");
                        st.unset("entermovie");
                        st.calcExpAndSp(this.getId());
                        st.exitQuest(false, true);
                     } else {
                        htmltext = "32792-00.htm";
                     }
                  }
               } else if (npcId == 32787) {
                  htmltext = "32787-01.htm";
               } else if (npcId == 32820) {
                  if (ac == 1) {
                     htmltext = "32820-02.htm";
                  } else {
                     htmltext = "32820-01.htm";
                  }
               } else if (npcId == 32837) {
                  htmltext = "32837-01.htm";
               } else if (npcId == 32842) {
                  htmltext = "32842-01.htm";
               } else if (npcId == 32857) {
                  htmltext = "32857-01.htm";
               } else if (npcId == 32858) {
                  htmltext = "32858-01.htm";
               } else if (npcId == 32859) {
                  htmltext = "32859-01.htm";
               } else if (npcId == 32860) {
                  htmltext = "32860-01.htm";
               } else if (npcId == 32793) {
                  if (cond == 1) {
                     htmltext = "32793-01.htm";
                  } else if (cond == 2) {
                     htmltext = "32793-04.htm";
                  } else if (cond == 3) {
                     htmltext = "32793-08.htm";
                  }
               }
            }

            return htmltext;
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return super.onKill(npc, player, isSummon);
      } else {
         int npcId = npc.getId();
         if (npcId == 18952 || npcId == 18953 || npcId == 18954 || npcId == 18955) {
            switch(npcId) {
               case 18952:
                  st.set("first", "1");
                  break;
               case 18953:
                  st.set("second", "1");
                  break;
               case 18954:
                  st.set("third", "1");
                  break;
               case 18955:
                  st.set("fourth", "1");
            }

            this.checkState(player);
         }

         return null;
      }
   }

   private void checkState(Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st != null) {
         int first = st.getInt("first");
         int second = st.getInt("second");
         int third = st.getInt("third");
         int fourth = st.getInt("fourth");
         if (first == 1 && second == 1 && third == 1 && fourth == 1) {
            player.showQuestMovie(27);
            st.set("active", "1");
         }
      }
   }

   public static void main(String[] args) {
      new _10295_SevenSignsSolinasTomb(10295, _10295_SevenSignsSolinasTomb.class.getSimpleName(), "");
   }
}
