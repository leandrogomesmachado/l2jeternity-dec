package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _050_LanoscosSpecialBait extends Quest {
   public _050_LanoscosSpecialBait(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31570);
      this.addTalkId(31570);
      this.addKillId(21026);
      this.questItemIds = new int[]{7621};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31570-03.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("31570-06.htm")) {
            if (st.getQuestItemsCount(7621) < 100L) {
               htmltext = "31570-07.htm";
            } else {
               st.takeItems(7621, -1L);
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
         int id = st.getState();
         if (npcId == 31570) {
            if (id == 0) {
               if (player.getLevel() < 27) {
                  htmltext = "31570-02a.htm";
                  st.exitQuest(true);
               } else if (player.getSkillLevel(1315) >= 8) {
                  htmltext = "31570-01.htm";
               } else {
                  htmltext = "31570-02.htm";
                  st.exitQuest(true);
               }
            } else if (cond == 1 || cond == 2) {
               if (st.getQuestItemsCount(7621) < 100L) {
                  htmltext = "31570-05.htm";
               } else {
                  htmltext = "31570-04.htm";
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
         if (st.calcDropItems(this.getId(), 7621, npc.getId(), 100)) {
            st.setCond(2);
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _050_LanoscosSpecialBait(50, _050_LanoscosSpecialBait.class.getSimpleName(), "");
   }
}
