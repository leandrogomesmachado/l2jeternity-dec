package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _623_TheFinestFood extends Quest {
   private static final String qn = "_623_TheFinestFood";
   private static final int LEAF = 7199;
   private static final int MEAT = 7200;
   private static final int HORN = 7201;
   private static final int JEREMY = 31521;
   private static final int FLAVA = 21316;
   private static final int BUFFALO = 21315;
   private static final int ANTELOPE = 21318;

   public _623_TheFinestFood(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31521);
      this.addTalkId(31521);
      this.addKillId(new int[]{21316, 21315, 21318});
      this.questItemIds = new int[]{7199, 7200, 7201};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_623_TheFinestFood");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31521-02.htm")) {
            if (player.getLevel() >= 71 && player.getLevel() <= 78) {
               st.set("cond", "1");
               st.setState((byte)1);
               st.playSound("ItemSound.quest_accept");
            } else {
               htmltext = "31521-03.htm";
            }
         } else if (event.equalsIgnoreCase("31521-05.htm")) {
            st.takeItems(7199, -1L);
            st.takeItems(7200, -1L);
            st.takeItems(7201, -1L);
            int luck = st.getRandom(100);
            if (luck < 11) {
               st.rewardItems(57, 25000L);
               st.giveItems(6849, 1L);
            } else if (luck < 23) {
               st.rewardItems(57, 65000L);
               st.giveItems(6847, 1L);
            } else if (luck < 33) {
               st.rewardItems(57, 25000L);
               st.giveItems(6851, 1L);
            } else {
               st.rewardItems(57, 73000L);
               st.addExpAndSp(230000, 18250);
            }

            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = Quest.getNoQuestMsg(player);
      QuestState st = player.getQuestState("_623_TheFinestFood");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               htmltext = "31521-01.htm";
               break;
            case 1:
               int cond = st.getInt("cond");
               if (cond == 1) {
                  htmltext = "31521-06.htm";
               } else if (cond == 2) {
                  if (st.getQuestItemsCount(7199) >= 100L && st.getQuestItemsCount(7200) >= 100L && st.getQuestItemsCount(7201) >= 100L) {
                     htmltext = "31521-04.htm";
                  } else {
                     htmltext = "31521-07.htm";
                  }
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(player, 1);
      if (partyMember == null) {
         return null;
      } else {
         QuestState st = partyMember.getQuestState("_623_TheFinestFood");
         if (st.getRandom(100) < 66) {
            switch(npc.getId()) {
               case 21315:
                  if (st.getQuestItemsCount(7200) < 100L) {
                     st.giveItems(7200, 1L);
                     if (st.getQuestItemsCount(7199) >= 100L && st.getQuestItemsCount(7200) >= 100L && st.getQuestItemsCount(7201) >= 100L) {
                        st.set("cond", "2");
                        st.playSound("ItemSound.quest_middle");
                     } else {
                        st.playSound("ItemSound.quest_itemget");
                     }
                  }
                  break;
               case 21316:
                  if (st.getQuestItemsCount(7199) < 100L) {
                     st.giveItems(7199, 1L);
                     if (st.getQuestItemsCount(7199) >= 100L && st.getQuestItemsCount(7200) >= 100L && st.getQuestItemsCount(7201) >= 100L) {
                        st.set("cond", "2");
                        st.playSound("ItemSound.quest_middle");
                     } else {
                        st.playSound("ItemSound.quest_itemget");
                     }
                  }
               case 21317:
               default:
                  break;
               case 21318:
                  if (st.getQuestItemsCount(7201) < 100L) {
                     st.giveItems(7201, 1L);
                     if (st.getQuestItemsCount(7199) >= 100L && st.getQuestItemsCount(7200) >= 100L && st.getQuestItemsCount(7201) >= 100L) {
                        st.set("cond", "2");
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
      new _623_TheFinestFood(623, "_623_TheFinestFood", "");
   }
}
