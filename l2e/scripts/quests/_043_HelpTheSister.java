package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _043_HelpTheSister extends Quest {
   public _043_HelpTheSister(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(30829);
      this.addTalkId(30829);
      this.addTalkId(30097);
      this.addKillId(20171);
      this.addKillId(20197);
      this.questItemIds = new int[]{7550, 7551};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("1")) {
            htmltext = "30829-01.htm";
            st.startQuest();
         } else if (event.equalsIgnoreCase("3") && st.getQuestItemsCount(220) > 0L) {
            htmltext = "30829-03.htm";
            st.takeItems(220, 1L);
            st.setCond(2, true);
         } else if (event.equalsIgnoreCase("4") && st.getQuestItemsCount(7551) >= 30L) {
            htmltext = "30829-05.htm";
            st.takeItems(7551, 30L);
            st.giveItems(7551, 1L);
            st.setCond(4, true);
         } else if (event.equalsIgnoreCase("5") && st.getQuestItemsCount(7551) > 0L) {
            htmltext = "30097-06.htm";
            st.takeItems(7551, 1L);
            st.setCond(5, true);
         } else if (event.equalsIgnoreCase("7")) {
            htmltext = "30829-07.htm";
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
            if (player.getLevel() >= 26) {
               htmltext = "30829-00.htm";
            } else {
               st.exitQuest(true);
               htmltext = "30829-00a.htm";
            }
         } else if (id == 1) {
            int cond = st.getCond();
            if (npcId == 30829) {
               if (cond == 1) {
                  if (st.getQuestItemsCount(220) == 0L) {
                     htmltext = "30829-01a.htm";
                  } else {
                     htmltext = "30829-02.htm";
                  }
               } else if (cond == 2) {
                  htmltext = "30829-03a.htm";
               } else if (cond == 3) {
                  htmltext = "30829-04.htm";
               } else if (cond == 4) {
                  htmltext = "30829-05a.htm";
               } else if (cond == 5) {
                  htmltext = "30829-06.htm";
               }
            } else if (npcId == 30097) {
               if (cond == 4 && st.getQuestItemsCount(7551) > 0L) {
                  htmltext = "30097-05.htm";
               } else if (cond == 5) {
                  htmltext = "30097-06a.htm";
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
         if (st.calcDropItems(this.getId(), 7551, npc.getId(), 30)) {
            st.setCond(3);
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _043_HelpTheSister(43, _043_HelpTheSister.class.getSimpleName(), "");
   }
}
