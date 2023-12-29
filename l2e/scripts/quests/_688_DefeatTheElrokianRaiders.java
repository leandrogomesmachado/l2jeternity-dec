package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _688_DefeatTheElrokianRaiders extends Quest {
   public _688_DefeatTheElrokianRaiders(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(32105);
      this.addTalkId(32105);
      this.addKillId(22214);
      this.questItemIds = new int[]{8785};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         long count = st.getQuestItemsCount(8785);
         if (event.equalsIgnoreCase("32105-02.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("32105-08.htm")) {
            if (count > 0L) {
               st.takeItems(8785, -1L);
               st.giveItems(57, count * 3000L);
            }

            st.exitQuest(true, true);
         } else if (event.equalsIgnoreCase("32105-06.htm")) {
            if (count > 0L) {
               st.takeItems(8785, -1L);
               st.giveItems(57, count * 3000L);
            } else {
               htmltext = "32105-06a.htm";
            }
         } else if (event.equalsIgnoreCase("32105-07.htm")) {
            if (count >= 100L) {
               st.takeItems(8785, 100L);
               st.calcReward(this.getId());
            } else {
               htmltext = "32105-07a.htm";
            }
         } else if (event.equalsIgnoreCase("None")) {
            htmltext = null;
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
               htmltext = player.getLevel() >= 75 ? "32105-01.htm" : "32105-00.htm";
               break;
            case 1:
               htmltext = st.hasQuestItems(8785) ? "32105-05.htm" : "32105-06a.htm";
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
         if (st != null) {
            st.calcDoDropItems(this.getId(), 8785, npc.getId(), Integer.MAX_VALUE);
         }

         return super.onKill(npc, player, isSummon);
      }
   }

   public static void main(String[] args) {
      new _688_DefeatTheElrokianRaiders(688, _688_DefeatTheElrokianRaiders.class.getSimpleName(), "");
   }
}
