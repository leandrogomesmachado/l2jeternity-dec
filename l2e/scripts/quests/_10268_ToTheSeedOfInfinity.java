package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _10268_ToTheSeedOfInfinity extends Quest {
   public _10268_ToTheSeedOfInfinity(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(32548);
      this.addTalkId(32548);
      this.addTalkId(32603);
      this.questItemIds = new int[]{13811};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32548-05.htm")) {
            st.giveItems(13811, 1L);
            st.startQuest();
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
         if (st.isCompleted()) {
            if (npc.getId() == 32603) {
               htmltext = "32530-02.htm";
            } else {
               htmltext = "32548-0a.htm";
            }
         } else if (st.getState() == 0 && npc.getId() == 32548) {
            if (player.getLevel() < 75) {
               htmltext = "32548-00.htm";
            } else {
               htmltext = "32548-01.htm";
            }
         } else if (st.getState() == 1 && npc.getId() == 32548) {
            htmltext = "32548-06.htm";
         } else if (st.getState() == 1 && npc.getId() == 32603) {
            htmltext = "32530-01.htm";
            st.calcExpAndSp(this.getId());
            st.calcReward(this.getId());
            st.exitQuest(false, true);
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _10268_ToTheSeedOfInfinity(10268, _10268_ToTheSeedOfInfinity.class.getSimpleName(), "");
   }
}
