package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _10291_FireDragonDestroyer extends Quest {
   public _10291_FireDragonDestroyer(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31540);
      this.addTalkId(31540);
      this.addKillId(29028);
      this.questItemIds = new int[]{15524, 15525};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31540-07.htm")) {
            st.giveItems(15524, 1L);
            st.startQuest();
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
               if (player.getLevel() >= 83 && st.getQuestItemsCount(7267) >= 1L) {
                  htmltext = "31540-01.htm";
               } else if (player.getLevel() < 83) {
                  htmltext = "31540-02.htm";
               } else {
                  htmltext = "31540-04.htm";
               }
               break;
            case 1:
               if (st.isCond(1) && st.getQuestItemsCount(15524) >= 1L) {
                  htmltext = "31540-08.htm";
               } else if (st.isCond(1) && st.getQuestItemsCount(15524) == 0L) {
                  st.giveItems(15524, 1L);
                  htmltext = "31540-09.htm";
               } else if (st.isCond(2)) {
                  st.takeItems(15525, 1L);
                  st.calcExpAndSp(this.getId());
                  st.calcReward(this.getId());
                  st.exitQuest(false, true);
                  htmltext = "31540-10.htm";
               }
               break;
            case 2:
               htmltext = "31540-03.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         if (player.getParty() != null) {
            for(Player partyMember : player.getParty().getMembers()) {
               QuestState qs = partyMember.getQuestState(this.getName());
               if (qs != null && qs.isCond(1) && qs.calcDropItems(this.getId(), 15525, npc.getId(), 1)) {
                  qs.takeItems(15524, 1L);
                  qs.setCond(2);
               }
            }
         } else if (st != null && st.isCond(1) && st.calcDropItems(this.getId(), 15525, npc.getId(), 1)) {
            st.takeItems(15524, 1L);
            st.setCond(2);
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _10291_FireDragonDestroyer(10291, _10291_FireDragonDestroyer.class.getSimpleName(), "");
   }
}
