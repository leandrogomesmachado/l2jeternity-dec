package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _290_ThreatRemoval extends Quest {
   public _290_ThreatRemoval(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30201);
      this.addTalkId(30201);
      this.addKillId(new int[]{22775, 22776, 22777, 22778, 22780, 22781, 22782, 22783, 22784, 22785});
      this.questItemIds = new int[]{15714};
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         long count = st.getQuestItemsCount(15714);
         int random = getRandom(1, 3);
         if (event.equalsIgnoreCase("30201-03.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("30201-06.htm")) {
            if (count >= 400L) {
               st.takeItems(15714, 400L);
               st.calcReward(this.getId(), random);
            }
         } else if (event.equalsIgnoreCase("30201-07.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("30201-09.htm")) {
            st.exitQuest(true, true);
         }

         return event;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               QuestState qs = player.getQuestState("_251_NoSecrets");
               htmltext = player.getLevel() >= 82 && qs != null && qs.isCompleted() ? "30201-02.htm" : "30201-01.htm";
               break;
            case 1:
               if (st.isCond(1)) {
                  htmltext = st.getQuestItemsCount(15714) < 400L ? "30201-04.htm" : "30201-05.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(player, 1);
      if (partyMember == null) {
         return null;
      } else {
         QuestState st = partyMember.getQuestState(this.getName());
         if (st != null && st.isCond(1)) {
            st.calcDoDropItems(this.getId(), 15714, npc.getId(), Integer.MAX_VALUE);
         }

         return super.onKill(npc, player, isSummon);
      }
   }

   public static void main(String[] args) {
      new _290_ThreatRemoval(290, _290_ThreatRemoval.class.getSimpleName(), "");
   }
}
