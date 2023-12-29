package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _189_ContractCompletion extends Quest {
   public _189_ContractCompletion(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31437);
      this.addTalkId(new int[]{31437, 30512, 30673, 30068});
      this.questItemIds = new int[]{10370};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState qs = this.getQuestState(player, false);
      if (qs == null) {
         return null;
      } else {
         String htmltext = null;
         switch(event) {
            case "31437-03.htm":
               if (qs.isCreated()) {
                  qs.startQuest();
                  qs.setMemoState(1);
                  giveItems(player, 10370, 1L);
                  htmltext = event;
               }
               break;
            case "30512-02.htm":
               if (qs.isMemoState(4)) {
                  if (player.getLevel() < 48) {
                     qs.calcExpAndSp(this.getId());
                  }

                  qs.calcReward(this.getId());
                  qs.exitQuest(false, true);
                  htmltext = event;
               }
               break;
            case "30673-02.htm":
               if (qs.isMemoState(1)) {
                  qs.setMemoState(2);
                  qs.setCond(2, true);
                  takeItems(player, 10370, -1L);
                  htmltext = event;
               }
               break;
            case "30068-02.htm":
               if (qs.isMemoState(2)) {
                  htmltext = event;
               }
               break;
            case "30068-03.htm":
               if (qs.isMemoState(2)) {
                  qs.setMemoState(3);
                  qs.setCond(3, true);
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
         if (npc.getId() == 31437) {
            QuestState q186 = player.getQuestState(_186_ContractExecution.class.getSimpleName());
            if (q186 != null && q186.isCompleted()) {
               htmltext = player.getLevel() >= 42 ? "31437-01.htm" : "31437-02.htm";
            }
         }
      } else if (qs.isStarted()) {
         switch(npc.getId()) {
            case 30068:
               switch(qs.getCond()) {
                  case 2:
                     return "30068-01.htm";
                  case 3:
                     return "30068-04.htm";
                  default:
                     return htmltext;
               }
            case 30512:
               if (qs.isMemoState(4)) {
                  htmltext = "30512-01.htm";
               }
               break;
            case 30673:
               switch(qs.getCond()) {
                  case 1:
                     return "30673-01.htm";
                  case 2:
                     return "30673-03.htm";
                  case 3:
                     qs.setMemoState(4);
                     qs.setCond(4, true);
                     return "30673-04.htm";
                  case 4:
                     htmltext = "30673-05.htm";
                     return htmltext;
                  default:
                     return htmltext;
               }
            case 31437:
               if (qs.getMemoState() >= 1) {
                  htmltext = "31437-04.htm";
               }
         }
      } else if (qs.isCompleted() && npc.getId() == 31437) {
         htmltext = getAlreadyCompletedMsg(player);
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new _189_ContractCompletion(189, _189_ContractCompletion.class.getSimpleName(), "");
   }
}
