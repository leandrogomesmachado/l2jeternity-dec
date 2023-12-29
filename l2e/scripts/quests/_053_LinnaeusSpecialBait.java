package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _053_LinnaeusSpecialBait extends Quest {
   public _053_LinnaeusSpecialBait(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(31577);
      this.addTalkId(31577);
      this.addKillId(20670);
      this.questItemIds = new int[]{7624};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31577-1.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("31577-3.htm")) {
            if (st.getQuestItemsCount(7624) < 100L) {
               htmltext = "31577-5.htm";
            } else {
               htmltext = "31577-3.htm";
               st.takeItems(7624, -1L);
               st.calcReward(this.getId());
               st.exitQuest(false, true);
            }
         }

         return htmltext;
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
               htmltext = player.getLevel() > 59 ? "31577-0.htm" : "31577-0a.htm";
               break;
            case 1:
               htmltext = st.isCond(1) ? "31577-4.htm" : "31577-2.htm";
               break;
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(player, 1);
      if (partyMember == null) {
         return super.onKill(npc, player, isSummon);
      } else {
         QuestState st = partyMember.getQuestState(this.getName());
         if (st.calcDropItems(this.getId(), 7624, npc.getId(), 100)) {
            st.setCond(2);
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _053_LinnaeusSpecialBait(53, _053_LinnaeusSpecialBait.class.getSimpleName(), "");
   }
}
