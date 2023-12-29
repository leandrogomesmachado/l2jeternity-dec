package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _238_SuccesFailureOfBusiness extends Quest {
   public _238_SuccesFailureOfBusiness(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(32641);
      this.addTalkId(32641);
      this.addKillId(18806);
      this.addKillId(22659);
      this.addKillId(22658);
      this.questItemIds = new int[]{14867, 14868};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         if (event.equals("32461-03.htm")) {
            st.startQuest();
         } else if (event.equals("32461-06.htm")) {
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
         switch(st.getState()) {
            case 0:
               QuestState qs = player.getQuestState("_237_WindsOfChange");
               QuestState qs2 = player.getQuestState("_239_WontYouJoinUs");
               if (qs2 != null && qs2.isCompleted()) {
                  htmltext = "32461-10.htm";
               } else if (qs != null && qs.isCompleted() && player.getLevel() >= 82 && st.hasQuestItems(14865)) {
                  htmltext = "32461-01.htm";
               } else {
                  htmltext = "32461-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               switch(st.getCond()) {
                  case 1:
                     return "32461-04.htm";
                  case 2:
                     if (st.getQuestItemsCount(14867) >= 10L) {
                        st.takeItems(14867, -1L);
                        htmltext = "32461-05.htm";
                     }

                     return htmltext;
                  case 3:
                     return "32461-07.htm";
                  case 4:
                     if (st.getQuestItemsCount(14868) >= 20L) {
                        htmltext = "32461-08.htm";
                        st.takeItems(14865, -1L);
                        st.takeItems(14868, -1L);
                        st.calcExpAndSp(this.getId());
                        st.calcReward(this.getId());
                        st.exitQuest(false, true);
                     }

                     return htmltext;
                  default:
                     return htmltext;
               }
            case 2:
               htmltext = "32461-09.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMemberState(player, (byte)1);
      if (partyMember == null) {
         return null;
      } else {
         QuestState st = partyMember.getQuestState(this.getName());
         if (st == null) {
            return null;
         } else {
            if (npc.getId() == 18806) {
               if (st.isCond(1) && st.calcDropItems(this.getId(), 14867, npc.getId(), 10)) {
                  st.setCond(2);
               }
            } else if ((npc.getId() == 22659 || npc.getId() == 22658) && st.isCond(3) && st.calcDropItems(this.getId(), 14868, npc.getId(), 20)) {
               st.setCond(4);
            }

            return null;
         }
      }
   }

   public static void main(String[] args) {
      new _238_SuccesFailureOfBusiness(238, _238_SuccesFailureOfBusiness.class.getSimpleName(), "");
   }
}
