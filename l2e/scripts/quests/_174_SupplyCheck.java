package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;

public class _174_SupplyCheck extends Quest {
   public _174_SupplyCheck(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32173);
      this.addTalkId(new int[]{32173, 32170, 32167});
      this.questItemIds = new int[]{9792, 9793};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32173-03.htm")) {
            st.startQuest();
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getCond();
         int id = st.getState();
         if (id == 2) {
            htmltext = getAlreadyCompletedMsg(player);
         } else if (id == 0 && npcId == 32173) {
            if (player.getLevel() >= getMinLvl(this.getId())) {
               htmltext = "32173-01.htm";
            } else {
               htmltext = "32173-02.htm";
               st.exitQuest(true);
            }
         } else if (id == 1) {
            if (npcId == 32173) {
               if (cond == 1) {
                  htmltext = "32173-04.htm";
               } else if (cond == 2) {
                  st.setCond(3, true);
                  st.takeItems(9792, -1L);
                  htmltext = "32173-05.htm";
               } else if (cond == 3) {
                  htmltext = "32173-06.htm";
               } else if (cond == 4) {
                  st.calcExpAndSp(this.getId());
                  if (player.getClassId().isMage()) {
                     st.calcReward(this.getId(), 1);
                  } else {
                     st.calcReward(this.getId(), 2);
                  }

                  showOnScreenMsg(player, NpcStringId.DELIVERY_DUTY_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE, 2, 5000, new String[0]);
                  st.exitQuest(false, true);
                  htmltext = "32173-07.htm";
               }
            } else if (npcId == 32170) {
               if (cond == 1) {
                  st.setCond(2, true);
                  st.giveItems(9792, 1L);
                  htmltext = "32170-01.htm";
               } else if (cond == 2) {
                  htmltext = "32170-02.htm";
               }
            } else if (npcId == 32167) {
               if (cond == 3) {
                  st.setCond(4, true);
                  st.giveItems(9793, 1L);
                  htmltext = "32167-01.htm";
               } else if (cond == 4) {
                  htmltext = "32167-02.htm";
               }
            }
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _174_SupplyCheck(174, _174_SupplyCheck.class.getSimpleName(), "");
   }
}
