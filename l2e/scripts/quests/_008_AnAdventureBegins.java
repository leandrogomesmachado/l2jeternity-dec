package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _008_AnAdventureBegins extends Quest {
   public _008_AnAdventureBegins(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30134);
      this.addTalkId(30134);
      this.addTalkId(30355);
      this.addTalkId(30144);
      this.questItemIds = new int[]{7573};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30134-03.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("30355-02.htm")) {
            st.giveItems(7573, 1L);
            st.setCond(2, true);
         } else if (event.equalsIgnoreCase("30144-02.htm")) {
            st.takeItems(7573, 1L);
            st.setCond(3, true);
         } else if (event.equalsIgnoreCase("30134-06.htm")) {
            st.calcReward(this.getId());
            st.exitQuest(false, true);
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
      int cond = st.getCond();
      int npcId = npc.getId();
      switch(st.getState()) {
         case 0:
            if (npcId == 30134) {
               if (player.getRace().ordinal() == 2 && player.getLevel() >= 3) {
                  htmltext = "30134-02.htm";
               } else {
                  htmltext = "30134-01.htm";
                  st.exitQuest(true);
               }
            }
            break;
         case 1:
            if (npcId == 30355) {
               switch(cond) {
                  case 1:
                     if (st.getQuestItemsCount(7573) == 0L) {
                        htmltext = "30355-01.htm";
                     }

                     return htmltext;
                  case 2:
                     htmltext = "30355-03.htm";
               }
            } else if (npcId == 30134) {
               switch(cond) {
                  case 1:
                  case 2:
                     htmltext = "30134-04.htm";
                     return htmltext;
                  case 3:
                     htmltext = "30134-05.htm";
               }
            } else if (npcId == 30144 && cond == 2 && st.getQuestItemsCount(7573) > 0L) {
               htmltext = "30144-01.htm";
            }
            break;
         case 2:
            htmltext = getAlreadyCompletedMsg(player);
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new _008_AnAdventureBegins(8, _008_AnAdventureBegins.class.getSimpleName(), "");
   }
}
