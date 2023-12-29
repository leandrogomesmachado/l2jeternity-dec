package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _152_ShardsOfGolem extends Quest {
   private static final String qn = "_152_ShardsOfGolem";
   private static final int HARRIS = 30035;
   private static final int ALTRAN = 30283;
   private static final int STONE_GOLEM = 20016;
   private static final int HARRYS_RECEIPT1 = 1008;
   private static final int HARRYS_RECEIPT2 = 1009;
   private static final int GOLEM_SHARD = 1010;
   private static final int TOOL_BOX = 1011;
   private static final int WOODEN_BP = 23;

   public _152_ShardsOfGolem(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30035);
      this.addTalkId(30035);
      this.addTalkId(30283);
      this.addKillId(20016);
      this.questItemIds = new int[]{1008, 1009, 1010, 1011};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_152_ShardsOfGolem");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30035-02.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1008, 1L);
         } else if (event.equalsIgnoreCase("30283-02.htm")) {
            st.set("cond", "2");
            st.takeItems(1008, -1L);
            st.giveItems(1009, 1L);
            st.playSound("ItemSound.quest_middle");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_152_ShardsOfGolem");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 10 && player.getLevel() <= 17) {
                  htmltext = "30035-01.htm";
               } else {
                  htmltext = "30035-01a.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 30035:
                     if (cond >= 1 && cond <= 3) {
                        htmltext = "30035-03.htm";
                     } else if (cond == 4 && st.getQuestItemsCount(1011) == 1L) {
                        htmltext = "30035-04.htm";
                        st.takeItems(1011, -1L);
                        st.takeItems(1009, -1L);
                        st.giveItems(23, 1L);
                        st.addExpAndSp(5000, 0);
                        st.playSound("ItemSound.quest_finish");
                        st.unset("cond");
                        st.exitQuest(false);
                        return htmltext;
                     }

                     return htmltext;
                  case 30283:
                     if (cond == 1) {
                        return "30283-01.htm";
                     } else if (cond == 2) {
                        return "30283-03.htm";
                     } else {
                        if (cond == 3) {
                           if (st.getQuestItemsCount(1010) >= 5L && st.getQuestItemsCount(1011) == 0L) {
                              st.set("cond", "4");
                              htmltext = "30283-04.htm";
                              st.takeItems(1010, -1L);
                              st.giveItems(1011, 1L);
                              st.playSound("ItemSound.quest_middle");
                              return htmltext;
                           }
                        } else if (cond == 4) {
                           htmltext = "30283-05.htm";
                           return htmltext;
                        }

                        return htmltext;
                     }
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
      QuestState st = player.getQuestState("_152_ShardsOfGolem");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 2 && st.getRandom(100) < 30) {
            st.giveItems(1010, 1L);
            if (st.getQuestItemsCount(1010) == 5L) {
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "3");
            } else {
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _152_ShardsOfGolem(152, "_152_ShardsOfGolem", "");
   }
}
