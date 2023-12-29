package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _051_OFullesSpecialBait extends Quest {
   public _051_OFullesSpecialBait(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(31572);
      this.addTalkId(31572);
      this.addKillId(20552);
      this.questItemIds = new int[]{7622};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31572-03.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("31572-07.htm")) {
            if (st.getQuestItemsCount(7622) < 100L) {
               htmltext = "31572-07.htm";
            } else {
               htmltext = "31572-06.htm";
               st.takeItems(7622, -1L);
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

         if (npcId == 31572) {
            if (cond == 1) {
               htmltext = "31572-05.htm";
            } else if (cond == 2) {
               htmltext = "31572-04.htm";
            } else if (cond == 0) {
               if (player.getLevel() > 35 && player.getLevel() < 39) {
                  htmltext = "31572-01.htm";
               } else {
                  htmltext = "31572-02.htm";
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
         if (st.calcDropItems(this.getId(), 7622, npc.getId(), 100)) {
            st.setCond(2);
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _051_OFullesSpecialBait(51, _051_OFullesSpecialBait.class.getSimpleName(), "");
   }
}
