package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _188_SealRemoval extends Quest {
   public _188_SealRemoval(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30673);
      this.addTalkId(new int[]{30673, 30621, 30970});
      this.questItemIds = new int[]{10369};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState qs = this.getQuestState(player, false);
      if (qs == null) {
         return null;
      } else {
         String htmltext = null;
         switch(event) {
            case "30673-03.htm":
               if (qs.isCreated()) {
                  qs.startQuest();
                  qs.setMemoState(1);
                  giveItems(player, 10369, 1L);
                  htmltext = event;
               }
               break;
            case "30621-02.htm":
               if (qs.isMemoState(1)) {
                  htmltext = event;
               }
               break;
            case "30621-03.htm":
               if (qs.isMemoState(1)) {
                  qs.setMemoState(2);
                  qs.setCond(2, true);
                  htmltext = event;
               }
               break;
            case "30621-04.htm":
               if (qs.isMemoState(2)) {
                  htmltext = event;
               }
               break;
            case "30970-02.htm":
               if (qs.isMemoState(2)) {
                  htmltext = event;
               }
               break;
            case "30970-03.htm":
               if (qs.isMemoState(2)) {
                  if (player.getLevel() < 47) {
                     qs.calcExpAndSp(this.getId());
                  }

                  qs.calcReward(this.getId());
                  qs.exitQuest(false, true);
                  htmltext = event;
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState qs = this.getQuestState(player, true);
      String htmltext = getNoQuestMsg(player);
      if (qs.isCreated()) {
         if (npc.getId() == 30673 && !hasQuestItems(player, 10362)) {
            QuestState q184 = player.getQuestState(_184_NikolasCooperationContract.class.getSimpleName());
            QuestState q185 = player.getQuestState(_185_NikolasCooperationConsideration.class.getSimpleName());
            QuestState q186 = player.getQuestState(_186_ContractExecution.class.getSimpleName());
            QuestState q187 = player.getQuestState(_187_NikolasHeart.class.getSimpleName());
            if (q184 != null && q184.isCompleted() || q185 != null && q185.isCompleted() && q186 == null && q187 == null) {
               htmltext = player.getLevel() >= 41 ? "30673-01.htm" : "30673-02.htm";
            }
         }
      } else if (qs.isStarted()) {
         switch(npc.getId()) {
            case 30621:
               if (qs.isMemoState(1)) {
                  htmltext = "30621-01.htm";
               } else if (qs.isMemoState(2)) {
                  htmltext = "30621-05.htm";
               }
               break;
            case 30673:
               htmltext = "30673-04.htm";
               break;
            case 30970:
               if (qs.isMemoState(2)) {
                  htmltext = "30970-01.htm";
               }
         }
      } else if (qs.isCompleted() && npc.getId() == 30673) {
         htmltext = getAlreadyCompletedMsg(player);
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new _188_SealRemoval(188, _188_SealRemoval.class.getSimpleName(), "");
   }
}
