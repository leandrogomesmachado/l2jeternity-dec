package l2e.scripts.quests;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _902_ReclaimOurEra extends Quest {
   public _902_ReclaimOurEra(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31340);
      this.addTalkId(31340);
      this.addKillId(new int[]{25309, 25312, 25315, 25299, 25302, 25305, 25667, 25668, 25669, 25670, 25701});
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31340-04.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("31340-06.htm")) {
            st.setCond(2, true);
         } else if (event.equalsIgnoreCase("31340-08.htm")) {
            st.setCond(3, true);
         } else if (event.equalsIgnoreCase("31340-10.htm")) {
            st.setCond(4, true);
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
            case 1:
               switch(st.getCond()) {
                  case 1:
                     return "31340-05.htm";
                  case 2:
                     return "31340-07.htm";
                  case 3:
                     return "31340-09.htm";
                  case 4:
                     return "31340-11.htm";
                  case 5:
                     if (st.getQuestItemsCount(21997) > 0L) {
                        st.takeItems(21997, 1L);
                        st.calcReward(this.getId(), 1);
                     } else if (st.getQuestItemsCount(21998) > 0L) {
                        st.takeItems(21998, 1L);
                        st.calcReward(this.getId(), 2);
                     } else if (st.getQuestItemsCount(21999) > 0L) {
                        st.takeItems(21999, 1L);
                        st.calcReward(this.getId(), 3);
                     }

                     st.exitQuest(QuestState.QuestType.DAILY, true);
                     htmltext = "31340-12.htm";
                     return htmltext;
                  default:
                     return htmltext;
               }
            case 2:
               if (!st.isNowAvailable()) {
                  htmltext = "31340-completed.htm";
                  break;
               } else {
                  st.setState((byte)0);
               }
            case 0:
               htmltext = player.getLevel() >= 80 ? "31340-01.htm" : "31340-00.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      if (killer.isInParty()) {
         for(Player player : killer.getParty().getMembers()) {
            this.rewardPlayer(npc, player);
         }
      } else {
         this.rewardPlayer(npc, killer);
      }

      return super.onKill(npc, killer, isSummon);
   }

   private void rewardPlayer(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st != null && st.isStarted() && player.isInsideRadius(npc, 1500, false, false)) {
         if (st.isCond(2) && Util.contains(new int[]{25309, 25312, 25315, 25299, 25302, 25305}, npc.getId())) {
            if (st.calcDropItems(this.getId(), 21997, npc.getId(), 1)) {
               st.setCond(5);
            }
         } else if (st.isCond(3) && Util.contains(new int[]{25667, 25668, 25669, 25670}, npc.getId())) {
            if (st.calcDropItems(this.getId(), 21998, npc.getId(), 1)) {
               st.setCond(5);
            }
         } else if (st.isCond(4) && npc.getId() == 25701 && st.calcDropItems(this.getId(), 21999, npc.getId(), 1)) {
            st.setCond(5);
         }
      }
   }

   public static void main(String[] args) {
      new _902_ReclaimOurEra(902, _902_ReclaimOurEra.class.getSimpleName(), "");
   }
}
