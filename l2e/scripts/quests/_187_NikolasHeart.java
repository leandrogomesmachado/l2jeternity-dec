package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _187_NikolasHeart extends Quest {
   public _187_NikolasHeart(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30673);
      this.addTalkId(new int[]{30673, 30512, 30621});
      this.questItemIds = new int[]{10368};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30673-02.htm")) {
            st.startQuest();
            st.takeItems(10362, -1L);
            st.giveItems(10368, 1L);
         } else if (event.equalsIgnoreCase("30621-03.htm")) {
            st.setCond(2, true);
         } else if (event.equalsIgnoreCase("30512-03.htm")) {
            if (player.getLevel() < 47) {
               st.calcExpAndSp(this.getId());
            }

            st.calcReward(this.getId());
            st.exitQuest(false, true);
         }

         return event;
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
         switch(st.getState()) {
            case 0:
               QuestState qs = player.getQuestState("_185_NikolasCooperationConsideration");
               if (npcId == 30673 && qs != null && qs.isCompleted() && hasQuestItems(player, 10362)) {
                  if (player.getLevel() < 41) {
                     htmltext = "30673-00.htm";
                  } else {
                     htmltext = "30673-01.htm";
                  }
               }
               break;
            case 1:
               if (npcId == 30673) {
                  if (cond == 1) {
                     htmltext = "30673-03.htm";
                  }
               } else if (npcId == 30621) {
                  if (cond == 1) {
                     htmltext = "30621-01.htm";
                  } else if (cond == 2) {
                     htmltext = "30621-04.htm";
                  }
               } else if (npcId == 30512 && cond == 2) {
                  htmltext = "30512-01.htm";
               }
               break;
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _187_NikolasHeart(187, _187_NikolasHeart.class.getSimpleName(), "");
   }
}
