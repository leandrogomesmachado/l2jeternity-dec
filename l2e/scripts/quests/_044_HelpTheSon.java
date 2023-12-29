package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _044_HelpTheSon extends Quest {
   public _044_HelpTheSon(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(30827);
      this.addTalkId(30827);
      this.addTalkId(30505);
      this.addKillId(20921);
      this.addKillId(20920);
      this.addKillId(20919);
      this.questItemIds = new int[]{7553, 7552};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("1")) {
            htmltext = "30827-01.htm";
            st.startQuest();
         } else if (event.equalsIgnoreCase("3") && st.getQuestItemsCount(168) > 0L) {
            htmltext = "30827-03.htm";
            st.takeItems(168, 1L);
            st.setCond(2, true);
         } else if (event.equalsIgnoreCase("4") && st.getQuestItemsCount(7552) >= 30L) {
            htmltext = "30827-05.htm";
            st.takeItems(7552, 30L);
            st.giveItems(7553, 1L);
            st.setCond(4, true);
         } else if (event.equalsIgnoreCase("5") && st.getQuestItemsCount(7553) > 0L) {
            htmltext = "30505-06.htm";
            st.takeItems(7553, 1L);
            st.setCond(5, true);
         } else if (event.equalsIgnoreCase("7")) {
            htmltext = "30827-07.htm";
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
         if (st.isCompleted()) {
            htmltext = getAlreadyCompletedMsg(player);
         } else if (id == 0) {
            if (player.getLevel() >= 24) {
               htmltext = "30827-00.htm";
            } else {
               st.exitQuest(true);
               htmltext = "30827-00a.htm";
            }
         } else if (id == 1) {
            int cond = st.getCond();
            if (npcId == 30827) {
               if (cond == 1) {
                  if (st.getQuestItemsCount(168) == 0L) {
                     htmltext = "30827-01a.htm";
                  } else {
                     htmltext = "30827-02.htm";
                  }
               } else if (cond == 2) {
                  htmltext = "30827-03a.htm";
               } else if (cond == 3) {
                  htmltext = "30827-04.htm";
               } else if (cond == 4) {
                  htmltext = "30827-05a.htm";
               } else if (cond == 5) {
                  htmltext = "30827-06.htm";
               }
            } else if (npcId == 30505) {
               if (cond == 4 && st.getQuestItemsCount(7553) > 0L) {
                  htmltext = "30505-05.htm";
               } else if (cond == 5) {
                  htmltext = "30505-06a.htm";
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
         if (st.calcDropItems(this.getId(), 7552, npc.getId(), 30)) {
            st.setCond(3);
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _044_HelpTheSon(44, _044_HelpTheSon.class.getSimpleName(), "");
   }
}
