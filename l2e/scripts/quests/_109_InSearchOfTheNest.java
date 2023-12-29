package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _109_InSearchOfTheNest extends Quest {
   public _109_InSearchOfTheNest(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31553);
      this.addTalkId(new int[]{31553, 32015, 31554});
      this.questItemIds = new int[]{14858};
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32015-02.htm")) {
            st.giveItems(14858, 1L);
            st.setCond(2, true);
         } else if (event.equalsIgnoreCase("31553-02.htm") && st.isCond(2)) {
            st.takeItems(14858, -1L);
            st.setCond(3, true);
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
         int npcId = npc.getId();
         int id = st.getState();
         if (st.isCompleted()) {
            htmltext = getAlreadyCompletedMsg(player);
         } else if (id == 0) {
            if (player.getLevel() < 81 || npcId != 31553 || st.getQuestItemsCount(7246) <= 0L && st.getQuestItemsCount(7247) <= 0L) {
               htmltext = "31553-00.htm";
               st.exitQuest(true);
            } else {
               st.startQuest();
               htmltext = "31553-00a.htm";
            }
         } else if (id == 1) {
            if (npcId == 32015) {
               if (st.isCond(1)) {
                  htmltext = "32015-01.htm";
               } else if (st.isCond(2)) {
                  htmltext = "32015-03.htm";
               }
            } else if (npcId == 31553) {
               if (st.isCond(1)) {
                  htmltext = "31553-01a.htm";
               } else if (st.isCond(2)) {
                  htmltext = "31553-01.htm";
               } else if (st.isCond(3)) {
                  htmltext = "31553-01b.htm";
               }
            } else if (npcId == 31554 && st.isCond(3)) {
               htmltext = "31554-01.htm";
               st.calcExpAndSp(this.getId());
               st.calcReward(this.getId());
               st.exitQuest(false, true);
            }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _109_InSearchOfTheNest(109, _109_InSearchOfTheNest.class.getSimpleName(), "");
   }
}
