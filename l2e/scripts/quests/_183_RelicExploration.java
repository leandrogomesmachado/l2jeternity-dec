package l2e.scripts.quests;

import l2e.gameserver.instancemanager.QuestManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _183_RelicExploration extends Quest {
   public _183_RelicExploration(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30512);
      this.addTalkId(new int[]{30512, 30673, 30621});
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState qs = this.getQuestState(player, false);
      if (qs == null) {
         return null;
      } else {
         String htmltext = null;
         switch(event) {
            case "30512-04.htm":
               qs.startQuest();
               qs.setMemoState(1);
               htmltext = event;
               break;
            case "30512-02.htm":
               htmltext = event;
               break;
            case "30621-02.htm":
               if (qs.isMemoState(2)) {
                  qs.calcReward(this.getId());
                  if (player.getLevel() < 46) {
                     qs.calcExpAndSp(this.getId());
                  }

                  qs.exitQuest(false, true);
                  htmltext = event;
               }
               break;
            case "30673-02.htm":
            case "30673-03.htm":
               if (qs.isMemoState(1)) {
                  htmltext = event;
               }
               break;
            case "30673-04.htm":
               if (qs.isMemoState(1)) {
                  qs.setMemoState(2);
                  qs.setCond(2, true);
                  htmltext = event;
               }
               break;
            case "Contract":
               QuestState qs184 = player.getQuestState(_184_NikolasCooperationContract.class.getSimpleName());
               QuestState qs185 = player.getQuestState(_185_NikolasCooperationConsideration.class.getSimpleName());
               Quest quest = QuestManager.getInstance().getQuest(_184_NikolasCooperationContract.class.getSimpleName());
               if (quest != null && qs184 == null && qs185 == null) {
                  if (player.getLevel() >= 40) {
                     quest.notifyEvent("30621-03.htm", npc, player);
                  } else {
                     quest.notifyEvent("30621-03a.htm", npc, player);
                  }
               }
               break;
            case "Consideration":
               QuestState qs184 = player.getQuestState(_184_NikolasCooperationContract.class.getSimpleName());
               QuestState qs185 = player.getQuestState(_185_NikolasCooperationConsideration.class.getSimpleName());
               Quest quest = QuestManager.getInstance().getQuest(_185_NikolasCooperationConsideration.class.getSimpleName());
               if (quest != null && qs184 == null && qs185 == null) {
                  if (player.getLevel() >= 40) {
                     quest.notifyEvent("30621-03.htm", npc, player);
                  } else {
                     quest.notifyEvent("30621-03a.htm", npc, player);
                  }
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
         if (npc.getId() == 30512) {
            htmltext = player.getLevel() >= 40 ? "30512-01.htm" : "30512-03.htm";
         }
      } else if (qs.isStarted()) {
         switch(npc.getId()) {
            case 30512:
               htmltext = "30512-05.htm";
               break;
            case 30621:
               if (qs.isMemoState(2)) {
                  htmltext = "30621-01.htm";
               }
               break;
            case 30673:
               if (qs.isMemoState(1)) {
                  htmltext = "30673-01.htm";
               } else if (qs.isMemoState(2)) {
                  htmltext = "30673-05.htm";
               }
         }
      } else if (qs.isCompleted()) {
         htmltext = getAlreadyCompletedMsg(player);
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new _183_RelicExploration(183, _183_RelicExploration.class.getSimpleName(), "");
   }
}
