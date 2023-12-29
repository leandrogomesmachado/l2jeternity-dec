package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _351_BlackSwan extends Quest {
   private static final String qn = "_351_BlackSwan";
   private static final int GOSTA = 30916;
   private static final int IASON_HEINE = 30969;
   private static final int ROMAN = 30897;
   private static final int ORDER_OF_GOSTA = 4296;
   private static final int LIZARD_FANG = 4297;
   private static final int BARREL_OF_LEAGUE = 4298;
   private static final int BILL_OF_IASON_HEINE = 4310;

   public _351_BlackSwan(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30916);
      this.addTalkId(new int[]{30916, 30969, 30897});
      this.addKillId(new int[]{20784, 20785, 21639, 21640, 21642, 21643});
      this.questItemIds = new int[]{4296, 4298, 4297};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_351_BlackSwan");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30916-03.htm")) {
            st.setState((byte)1);
            st.set("cond", "1");
            st.giveItems(4296, 1L);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30969-02a.htm")) {
            long lizardFangs = st.getQuestItemsCount(4297);
            if (lizardFangs > 0L) {
               htmltext = "30969-02.htm";
               st.takeItems(4297, -1L);
               st.rewardItems(57, lizardFangs * 20L);
            }
         } else if (event.equalsIgnoreCase("30969-03a.htm")) {
            long barrels = st.getQuestItemsCount(4298);
            if (barrels > 0L) {
               htmltext = "30969-03.htm";
               st.takeItems(4298, -1L);
               st.rewardItems(4310, barrels);
               if (st.getInt("cond") == 1) {
                  st.set("cond", "2");
                  st.playSound("ItemSound.quest_middle");
               }
            }
         } else if (event.equalsIgnoreCase("30969-06.htm") && st.getQuestItemsCount(4298) == 0L && st.getQuestItemsCount(4297) == 0L) {
            htmltext = "30969-07.htm";
            st.exitQuest(true);
            st.playSound("ItemSound.quest_finish");
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = Quest.getNoQuestMsg(player);
      QuestState st = player.getQuestState("_351_BlackSwan");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 32 && player.getLevel() <= 36) {
                  htmltext = "30916-01.htm";
               } else {
                  htmltext = "30916-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(npc.getId()) {
                  case 30897:
                     if (st.getQuestItemsCount(4310) > 0L) {
                        htmltext = "30897-01.htm";
                     } else {
                        htmltext = "30897-02.htm";
                     }
                     break;
                  case 30916:
                     htmltext = "30916-04.htm";
                     break;
                  case 30969:
                     htmltext = "30969-01.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_351_BlackSwan");
      if (st == null) {
         return null;
      } else {
         int random = st.getRandom(20);
         if (random < 10) {
            if (random < 5) {
               st.giveItems(4297, 1L);
            } else {
               st.giveItems(4297, 2L);
            }

            if (random == 0) {
               st.giveItems(4298, 1L);
               st.playSound("ItemSound.quest_middle");
            } else {
               st.playSound("ItemSound.quest_itemget");
            }
         } else if (random == 10) {
            st.giveItems(4298, 1L);
            st.playSound("ItemSound.quest_middle");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _351_BlackSwan(351, "_351_BlackSwan", "");
   }
}
