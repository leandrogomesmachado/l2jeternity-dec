package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _364_JovialAccordion extends Quest {
   private static final String qn = "_364_JovialAccordion";
   private static final int BARBADO = 30959;
   private static final int SWAN = 30957;
   private static final int SABRIN = 30060;
   private static final int XABER = 30075;
   private static final int CLOTH_CHEST = 30961;
   private static final int BEER_CHEST = 30960;
   private static final int KEY_1 = 4323;
   private static final int KEY_2 = 4324;
   private static final int STOLEN_BEER = 4321;
   private static final int STOLEN_CLOTHES = 4322;
   private static final int ECHO = 4421;

   public _364_JovialAccordion(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30959);
      this.addTalkId(new int[]{30959, 30957, 30060, 30075, 30961, 30960});
      this.questItemIds = new int[]{4323, 4324, 4321, 4322};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_364_JovialAccordion");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30959-02.htm")) {
            st.set("cond", "1");
            st.set("items", "0");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30957-02.htm")) {
            st.set("cond", "2");
            st.giveItems(4323, 1L);
            st.giveItems(4324, 1L);
            st.playSound("ItemSound.quest_middle");
         } else if (event.equalsIgnoreCase("30960-04.htm")) {
            if (st.getQuestItemsCount(4324) == 1L) {
               st.takeItems(4324, 1L);
               if (st.getRandom(10) < 5) {
                  htmltext = "30960-02.htm";
                  st.giveItems(4321, 1L);
                  st.playSound("ItemSound.quest_itemget");
               }
            }
         } else if (event.equalsIgnoreCase("30961-04.htm") && st.getQuestItemsCount(4323) == 1L) {
            st.takeItems(4323, 1L);
            if (st.getRandom(10) < 5) {
               htmltext = "30961-02.htm";
               st.giveItems(4322, 1L);
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_364_JovialAccordion");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 15) {
                  htmltext = "30959-01.htm";
               } else {
                  htmltext = "30959-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               int cond = st.getInt("cond");
               int stolenItems = st.getInt("items");
               switch(npc.getId()) {
                  case 30060:
                     if (st.getQuestItemsCount(4321) == 1L) {
                        htmltext = "30060-01.htm";
                        st.takeItems(4321, 1L);
                        st.playSound("ItemSound.quest_itemget");
                        st.set("items", String.valueOf(stolenItems + 1));
                     } else {
                        htmltext = "30060-02.htm";
                     }
                     break;
                  case 30075:
                     if (st.getQuestItemsCount(4322) == 1L) {
                        htmltext = "30075-01.htm";
                        st.takeItems(4322, 1L);
                        st.playSound("ItemSound.quest_itemget");
                        st.set("items", String.valueOf(stolenItems + 1));
                     } else {
                        htmltext = "30075-02.htm";
                     }
                     break;
                  case 30957:
                     if (cond == 1) {
                        htmltext = "30957-01.htm";
                     } else if (cond == 2) {
                        if (stolenItems > 0) {
                           st.set("cond", "3");
                           st.playSound("ItemSound.quest_middle");
                           if (stolenItems == 2) {
                              htmltext = "30957-04.htm";
                              st.rewardItems(57, 100L);
                           } else {
                              htmltext = "30957-05.htm";
                           }
                        } else if (st.getQuestItemsCount(4323) == 0L && st.getQuestItemsCount(4324) == 0L) {
                           htmltext = "30957-06.htm";
                           st.playSound("ItemSound.quest_finish");
                           st.exitQuest(true);
                        } else {
                           htmltext = "30957-03.htm";
                        }
                     } else if (cond == 3) {
                        htmltext = "30957-07.htm";
                     }
                     break;
                  case 30959:
                     if (cond == 1 || cond == 2) {
                        htmltext = "30959-03.htm";
                     } else if (cond == 3) {
                        htmltext = "30959-04.htm";
                        st.giveItems(4421, 1L);
                        st.playSound("ItemSound.quest_finish");
                        st.exitQuest(true);
                     }
                     break;
                  case 30960:
                     htmltext = "30960-03.htm";
                     if (cond == 2 && st.getQuestItemsCount(4324) == 1L) {
                        htmltext = "30960-01.htm";
                     }
                     break;
                  case 30961:
                     htmltext = "30961-03.htm";
                     if (cond == 2 && st.getQuestItemsCount(4323) == 1L) {
                        htmltext = "30961-01.htm";
                     }
               }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _364_JovialAccordion(364, "_364_JovialAccordion", "");
   }
}
