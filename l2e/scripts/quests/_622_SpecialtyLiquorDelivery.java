package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _622_SpecialtyLiquorDelivery extends Quest {
   private static final String qn = "_622_SpecialtyLiquorDelivery";
   private static final int DRINK = 7197;
   private static final int FEE = 7198;
   private static final int JEREMY = 31521;
   private static final int PULIN = 31543;
   private static final int NAFF = 31544;
   private static final int CROCUS = 31545;
   private static final int KUBER = 31546;
   private static final int BEOLIN = 31547;
   private static final int LIETTA = 31267;
   private static final int ADENA = 57;
   private static final int HASTE_POT = 1062;
   private static final int[] RECIPES = new int[]{6847, 6849, 6851};

   public _622_SpecialtyLiquorDelivery(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31521);
      this.addTalkId(new int[]{31521, 31543, 31544, 31545, 31546, 31547, 31267});
      this.questItemIds = new int[]{7197, 7198};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_622_SpecialtyLiquorDelivery");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31521-02.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.giveItems(7197, 5L);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("31547-02.htm")) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7197, 1L);
            st.giveItems(7198, 1L);
         } else if (event.equalsIgnoreCase("31546-02.htm")) {
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7197, 1L);
            st.giveItems(7198, 1L);
         } else if (event.equalsIgnoreCase("31545-02.htm")) {
            st.set("cond", "4");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7197, 1L);
            st.giveItems(7198, 1L);
         } else if (event.equalsIgnoreCase("31544-02.htm")) {
            st.set("cond", "5");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7197, 1L);
            st.giveItems(7198, 1L);
         } else if (event.equalsIgnoreCase("31543-02.htm")) {
            st.set("cond", "6");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7197, 1L);
            st.giveItems(7198, 1L);
         } else if (event.equalsIgnoreCase("31521-06.htm")) {
            st.set("cond", "7");
            st.playSound("ItemSound.quest_middle");
            st.takeItems(7198, 5L);
         } else if (event.equalsIgnoreCase("31267-02.htm")) {
            if (getRandom(5) < 1) {
               st.giveItems(RECIPES[getRandom(RECIPES.length)], 1L);
            } else {
               st.rewardItems(57, 18800L);
               st.rewardItems(1062, 1L);
            }

            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_622_SpecialtyLiquorDelivery");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 68) {
                  htmltext = "31521-01.htm";
               } else {
                  htmltext = "31521-03.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               int cond = st.getInt("cond");
               switch(npc.getId()) {
                  case 31267:
                     if (cond == 7) {
                        htmltext = "31267-01.htm";
                     }
                     break;
                  case 31521:
                     if (cond >= 1 && cond <= 5) {
                        htmltext = "31521-04.htm";
                     } else if (cond == 6) {
                        htmltext = "31521-05.htm";
                     } else if (cond == 7) {
                        htmltext = "31521-06.htm";
                     }
                     break;
                  case 31543:
                     if (cond == 5 && st.getQuestItemsCount(7197) == 1L) {
                        htmltext = "31543-01.htm";
                     } else if (cond >= 6) {
                        htmltext = "31543-03.htm";
                     }
                     break;
                  case 31544:
                     if (cond == 4 && st.getQuestItemsCount(7197) == 2L) {
                        htmltext = "31544-01.htm";
                     } else if (cond >= 5) {
                        htmltext = "31544-03.htm";
                     }
                     break;
                  case 31545:
                     if (cond == 3 && st.getQuestItemsCount(7197) == 3L) {
                        htmltext = "31545-01.htm";
                     } else if (cond >= 4) {
                        htmltext = "31545-03.htm";
                     }
                     break;
                  case 31546:
                     if (cond == 2 && st.getQuestItemsCount(7197) == 4L) {
                        htmltext = "31546-01.htm";
                     } else if (cond >= 3) {
                        htmltext = "31546-03.htm";
                     }
                     break;
                  case 31547:
                     if (cond == 1 && st.getQuestItemsCount(7197) == 5L) {
                        htmltext = "31547-01.htm";
                     } else if (cond >= 2) {
                        htmltext = "31547-03.htm";
                     }
               }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _622_SpecialtyLiquorDelivery(622, "_622_SpecialtyLiquorDelivery", "");
   }
}
