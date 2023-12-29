package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _042_HelpTheUncle extends Quest {
   public _042_HelpTheUncle(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(30828);
      this.addTalkId(30828);
      this.addTalkId(30735);
      this.addKillId(20068);
      this.addKillId(20266);
      this.questItemIds = new int[]{7548, 7549};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("1")) {
            htmltext = "30828-01.htm";
            st.startQuest();
         } else if (event.equalsIgnoreCase("3") && st.getQuestItemsCount(291) > 0L) {
            htmltext = "30828-03.htm";
            st.takeItems(291, 1L);
            st.setCond(2, true);
         } else if (event.equalsIgnoreCase("4") && st.getQuestItemsCount(7548) >= 30L) {
            htmltext = "30828-05.htm";
            st.takeItems(7548, 30L);
            st.giveItems(7549, 1L);
            st.setCond(4, true);
         } else if (event.equalsIgnoreCase("5") && st.getQuestItemsCount(7549) > 0L) {
            htmltext = "30735-06.htm";
            st.takeItems(7549, 1L);
            st.setCond(5, true);
         } else if (event.equalsIgnoreCase("7")) {
            htmltext = "30828-07.htm";
            st.calcReward(this.getId());
            st.exitQuest(false, true);
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
         int npcId = npc.getId();
         byte id = st.getState();
         int cond = st.getCond();
         if (st.isCompleted()) {
            htmltext = getAlreadyCompletedMsg(player);
         } else if (id == 0) {
            if (player.getLevel() >= 25) {
               htmltext = "30828-00.htm";
            } else {
               htmltext = "30828-00a.htm";
               st.exitQuest(true);
            }
         } else if (id == 1) {
            if (npcId == 30828) {
               if (cond == 1) {
                  if (st.getQuestItemsCount(291) == 0L) {
                     htmltext = "30828-01a.htm";
                  } else {
                     htmltext = "30828-02.htm";
                  }
               } else if (cond == 2) {
                  htmltext = "30828-03a.htm";
               } else if (cond == 3) {
                  htmltext = "30828-04.htm";
               } else if (cond == 4) {
                  htmltext = "30828-05a.htm";
               } else if (cond == 5) {
                  htmltext = "30828-06.htm";
               }
            } else if (npcId == 30735) {
               if (cond == 4 && st.getQuestItemsCount(7549) > 0L) {
                  htmltext = "30735-05.htm";
               } else if (cond == 5) {
                  htmltext = "30735-06a.htm";
               }
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(player, 2);
      if (partyMember == null) {
         return super.onKill(npc, player, isSummon);
      } else {
         QuestState st = partyMember.getQuestState(this.getName());
         if (st.calcDropItems(this.getId(), 7548, npc.getId(), 30)) {
            st.setCond(3);
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _042_HelpTheUncle(42, _042_HelpTheUncle.class.getSimpleName(), "");
   }
}
