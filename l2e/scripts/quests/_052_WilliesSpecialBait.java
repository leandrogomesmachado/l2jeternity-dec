package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _052_WilliesSpecialBait extends Quest {
   public _052_WilliesSpecialBait(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(31574);
      this.addTalkId(31574);
      this.addKillId(20573);
      this.addKillId(20574);
      this.questItemIds = new int[]{7623};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31574-03.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("31574-07.htm")) {
            if (st.getQuestItemsCount(7623) < 100L) {
               htmltext = "31574-07.htm";
            } else {
               htmltext = "31574-06.htm";
               st.takeItems(7623, -1L);
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
         int npcId = npc.getId();
         int cond = st.getCond();
         if (st.isCompleted()) {
            htmltext = getAlreadyCompletedMsg(player);
         }

         if (npcId == 31574) {
            if (cond == 1) {
               htmltext = "31574-05.htm";
            } else if (cond == 2) {
               htmltext = "31574-04.htm";
            } else if (cond == 0) {
               if (player.getLevel() > 47 && player.getLevel() < 51) {
                  htmltext = "31574-01.htm";
               } else {
                  htmltext = "31574-02.htm";
                  st.exitQuest(true);
               }
            }
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
         if (st.calcDropItems(this.getId(), 7623, npc.getId(), 100)) {
            st.setCond(2);
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _052_WilliesSpecialBait(52, _052_WilliesSpecialBait.class.getSimpleName(), "");
   }
}
