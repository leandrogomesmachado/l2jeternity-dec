package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _287_FiguringItOut extends Quest {
   public _287_FiguringItOut(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32742);
      this.addTalkId(32742);
      this.addKillId(new int[]{22768, 22769, 22770, 22771, 22772, 22773, 22774});
      this.questItemIds = new int[]{15499};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32742-03.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("32742-05.htm")) {
            if (st.getQuestItemsCount(15499) >= 100L) {
               st.takeItems(15499, 100L);
               calcReward(player, this.getId(), 1, true);
               htmltext = "32742-07.htm";
            } else {
               htmltext = "32742-05.htm";
            }
         } else if (event.equalsIgnoreCase("32742-09.htm")) {
            if (st.getQuestItemsCount(15499) >= 500L) {
               st.takeItems(15499, 500L);
               calcReward(player, this.getId(), 2, true);
               htmltext = "32742-07.htm";
            } else {
               htmltext = "32742-09.htm";
            }
         } else if (event.equalsIgnoreCase("32742-08.htm")) {
            st.exitQuest(true, true);
         }

         return htmltext;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = this.getQuestState(player, true);
      QuestState prev = player.getQuestState(_250_WatchWhatYouEat.class.getSimpleName());
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               htmltext = player.getLevel() >= 82 && prev != null && prev.isCompleted() ? "32742-01.htm" : "32742-02.htm";
               break;
            case 1:
               htmltext = st.getQuestItemsCount(15499) < 100L ? "32742-05.htm" : "32742-04.htm";
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(player, 1);
      if (partyMember == null) {
         return super.onKill(npc, player, isSummon);
      } else {
         QuestState st = partyMember.getQuestState(this.getName());
         if (st.isCond(1)) {
            st.calcDoDropItems(this.getId(), 15499, npc.getId(), Integer.MAX_VALUE);
         }

         return super.onKill(npc, player, isSummon);
      }
   }

   public static void main(String[] args) {
      new _287_FiguringItOut(287, _287_FiguringItOut.class.getSimpleName(), "");
   }
}
