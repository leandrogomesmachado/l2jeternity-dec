package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;

public class _002_WhatWomenWant extends Quest {
   public _002_WhatWomenWant(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30223);
      this.addTalkId(30223);
      this.addTalkId(30146);
      this.addTalkId(30150);
      this.addTalkId(30157);
      this.questItemIds = new int[]{1092, 1093, 1094, 689, 693};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30223-04.htm")) {
            st.startQuest();
            st.giveItems(1092, 1L);
         } else if (event.equalsIgnoreCase("30223-08.htm")) {
            st.takeItems(1094, -1L);
            st.giveItems(689, 1L);
            st.setCond(4, true);
         } else if (event.equalsIgnoreCase("30223-10.htm")) {
            st.takeItems(1094, -1L);
            st.calcExpAndSp(this.getId());
            st.calcReward(this.getId(), 1);
            st.exitQuest(false, true);
            showOnScreenMsg(player, NpcStringId.DELIVERY_DUTY_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE, 2, 5000, new String[0]);
            return null;
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         st = this.newQuestState(player);
      }

      String htmltext = getNoQuestMsg(player);
      int npcId = npc.getId();
      int cond = st.getCond();
      switch(st.getState()) {
         case 0:
            if (npcId == 30223) {
               if ((player.getRace().ordinal() == 1 || player.getRace().ordinal() == 0) && player.getLevel() >= 2) {
                  htmltext = "30223-02.htm";
               } else {
                  htmltext = "30223-01.htm";
                  st.exitQuest(true);
               }
            }
            break;
         case 1:
            if (npcId == 30223) {
               switch(cond) {
                  case 1:
                     if (st.getQuestItemsCount(1092) > 0L) {
                        htmltext = "30223-05.htm";
                     }

                     return htmltext;
                  case 2:
                     if (st.getQuestItemsCount(1093) > 0L) {
                        htmltext = "30223-06.htm";
                     }

                     return htmltext;
                  case 3:
                     if (st.getQuestItemsCount(1094) > 0L) {
                        htmltext = "30223-07.htm";
                     }

                     return htmltext;
                  case 4:
                     if (st.getQuestItemsCount(689) > 0L) {
                        htmltext = "30223-11.htm";
                     }

                     return htmltext;
                  case 5:
                     if (st.getQuestItemsCount(693) > 0L) {
                        htmltext = "30223-10.htm";
                        st.takeItems(693, -1L);
                        st.calcExpAndSp(this.getId());
                        st.calcReward(this.getId(), 2);
                        st.exitQuest(false, true);
                     }
               }
            } else if (npcId == 30146) {
               switch(cond) {
                  case 1:
                     if (st.getQuestItemsCount(1092) > 0L) {
                        htmltext = "30146-01.htm";
                        st.takeItems(1092, -1L);
                        st.giveItems(1093, 1L);
                        st.setCond(2, true);
                     }

                     return htmltext;
                  case 2:
                     if (st.getQuestItemsCount(1093) > 0L) {
                        htmltext = "30146-02.htm";
                     }
               }
            } else if (npcId == 30150) {
               switch(cond) {
                  case 2:
                     if (st.getQuestItemsCount(1093) > 0L) {
                        htmltext = "30150-01.htm";
                        st.takeItems(1093, -1L);
                        st.giveItems(1094, 1L);
                        st.setCond(3, true);
                     }

                     return htmltext;
                  case 3:
                     if (st.getQuestItemsCount(1094) > 0L) {
                        htmltext = "30150-02.htm";
                     }
               }
            } else if (npcId == 30157) {
               switch(cond) {
                  case 4:
                     if (st.getQuestItemsCount(689) > 0L) {
                        htmltext = "30157-01.htm";
                        st.takeItems(689, -1L);
                        st.giveItems(693, 1L);
                        st.setCond(5, true);
                     }

                     return htmltext;
                  case 5:
                     if (st.getQuestItemsCount(693) > 0L) {
                        htmltext = "30157-02.htm";
                     }
               }
            }
            break;
         case 2:
            htmltext = getAlreadyCompletedMsg(player);
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new _002_WhatWomenWant(2, _002_WhatWomenWant.class.getSimpleName(), "");
   }
}
