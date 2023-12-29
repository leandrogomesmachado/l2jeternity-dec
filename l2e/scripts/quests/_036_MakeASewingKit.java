package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _036_MakeASewingKit extends Quest {
   public _036_MakeASewingKit(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(30847);
      this.addTalkId(30847);
      this.addKillId(20566);
      this.questItemIds = new int[]{7163};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         int cond = st.getCond();
         if (event.equalsIgnoreCase("30847-1.htm") && cond == 0) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("30847-3.htm") && cond == 2) {
            st.takeItems(7163, 5L);
            st.setCond(3, true);
         } else if (event.equalsIgnoreCase("30847-4a.htm")) {
            if (st.getQuestItemsCount(1893) >= 10L && st.getQuestItemsCount(1891) >= 10L) {
               st.takeItems(1893, 10L);
               st.takeItems(1891, 10L);
               st.calcReward(this.getId());
               st.exitQuest(true, true);
            } else {
               htmltext = "30847-4b.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      int cond = st.getCond();
      if (st.isCompleted()) {
         htmltext = getAlreadyCompletedMsg(player);
      }

      if (cond == 0 && st.getQuestItemsCount(7078) == 0L) {
         if (player.getLevel() >= 60) {
            QuestState fwear = player.getQuestState("_037_PleaseMakeMeFormalWear");
            if (fwear == null || fwear.getState() != 1) {
               htmltext = "30847-5.htm";
               st.exitQuest(true);
            } else if (fwear.get("cond").equals("6")) {
               htmltext = "30847-0.htm";
            } else {
               htmltext = "30847-5.htm";
               st.exitQuest(true);
            }
         } else {
            htmltext = "30847-5.htm";
         }
      } else if (cond == 1 && st.getQuestItemsCount(7163) < 5L) {
         htmltext = "30847-1a.htm";
      } else if (cond == 2 && st.getQuestItemsCount(7163) == 5L) {
         htmltext = "30847-2.htm";
      } else if (cond == 3 && st.getQuestItemsCount(1893) >= 10L && st.getQuestItemsCount(1891) >= 10L) {
         htmltext = "30847-4.htm";
      } else {
         htmltext = "30847-3a.htm";
      }

      return htmltext;
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(player, 1);
      if (partyMember == null) {
         return super.onKill(npc, player, isSummon);
      } else {
         QuestState st = partyMember.getQuestState(this.getName());
         if (st.calcDropItems(this.getId(), 7163, npc.getId(), 5)) {
            st.setCond(2);
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _036_MakeASewingKit(36, _036_MakeASewingKit.class.getSimpleName(), "");
   }
}
