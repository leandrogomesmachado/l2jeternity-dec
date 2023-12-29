package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _192_SevenSignSeriesOfDoubt extends Quest {
   public _192_SevenSignSeriesOfDoubt(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30676);
      this.addTalkId(new int[]{30191, 30197, 30200, 32568, 30676});
      this.questItemIds = new int[]{13813, 13814, 13815};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (npc.getId() == 30676) {
            if (event.equalsIgnoreCase("30676-03.htm")) {
               st.startQuest();
            } else {
               if (event.equals("8")) {
                  st.setCond(2, true);
                  player.showQuestMovie(8);
                  this.startQuestTimer("playertele", 32000L, npc, player);
                  return "";
               }

               if (event.equalsIgnoreCase("playertele")) {
                  player.teleToLocation(81654, 54851, -1513, true);
                  return "";
               }

               if (event.equalsIgnoreCase("30676-12.htm")) {
                  st.takeItems(13814, 1L);
                  st.giveItems(13815, 1L);
                  st.setCond(7, true);
               }
            }
         } else if (npc.getId() == 30197) {
            if (event.equalsIgnoreCase("30197-03.htm")) {
               st.takeItems(13813, 1L);
               st.setCond(4, true);
            }
         } else if (npc.getId() == 30200) {
            if (event.equalsIgnoreCase("30200-04.htm")) {
               st.setCond(5, true);
            }
         } else if (npc.getId() == 32568) {
            if (event.equalsIgnoreCase("32568-02.htm")) {
               st.giveItems(13814, 1L);
               st.setCond(6, true);
            }
         } else if (npc.getId() == 30191 && event.equalsIgnoreCase("30191-03.htm")) {
            st.takeItems(13815, 1L);
            st.calcExpAndSp(this.getId());
            st.exitQuest(false, true);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (npc.getId() == 30676) {
                  if (player.getLevel() >= 79) {
                     htmltext = "30676-01.htm";
                  } else {
                     htmltext = "30676-00.htm";
                     st.exitQuest(true);
                  }
               } else if (npc.getId() == 32568) {
                  htmltext = "32568-04.htm";
               }
               break;
            case 1:
               if (npc.getId() == 30676) {
                  if (st.isCond(1)) {
                     htmltext = "30676-04.htm";
                  } else if (st.isCond(2)) {
                     htmltext = "30676-05.htm";
                     st.giveItems(13813, 1L);
                     st.setCond(3, true);
                  } else if (st.getCond() >= 3 && st.getCond() <= 5) {
                     htmltext = "30676-06.htm";
                  } else if (st.isCond(6)) {
                     htmltext = "30676-07.htm";
                  }
               } else if (npc.getId() == 30197) {
                  if (st.isCond(3)) {
                     htmltext = "30197-01.htm";
                  } else if (st.getCond() >= 4 && st.getCond() <= 7) {
                     htmltext = "30197-04.htm";
                  }
               } else if (npc.getId() == 30200) {
                  if (st.isCond(4)) {
                     htmltext = "30200-01.htm";
                  } else if (st.getCond() >= 5 && st.getCond() <= 7) {
                     htmltext = "30200-05.htm";
                  }
               } else if (npc.getId() == 32568) {
                  if (st.getCond() >= 1 && st.getCond() <= 4) {
                     htmltext = "32568-03.htm";
                  } else if (st.isCond(5)) {
                     htmltext = "32568-01.htm";
                  }
               } else if (npc.getId() == 30191 && st.isCond(7)) {
                  htmltext = "30191-01.htm";
               }
               break;
            case 2:
               if (npc.getId() == 30676) {
                  htmltext = "30676-13.htm";
               }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _192_SevenSignSeriesOfDoubt(192, _192_SevenSignSeriesOfDoubt.class.getSimpleName(), "");
   }
}
