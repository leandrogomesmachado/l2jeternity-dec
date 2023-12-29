package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _190_LostDream extends Quest {
   public _190_LostDream(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30512);
      this.addTalkId(new int[]{30512, 30673, 30621, 30113});
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState qs = this.getQuestState(player, false);
      if (qs == null) {
         return null;
      } else {
         String htmltext = null;
         switch(event) {
            case "30512-03.htm":
               if (qs.isCreated()) {
                  qs.startQuest();
                  qs.setMemoState(1);
                  htmltext = event;
               }
               break;
            case "30512-06.htm":
               if (qs.isMemoState(2)) {
                  qs.setMemoState(3);
                  qs.setCond(3, true);
                  htmltext = event;
               }
               break;
            case "30113-02.htm":
               if (qs.isMemoState(1)) {
                  htmltext = event;
               }
               break;
            case "30113-03.htm":
               if (qs.isMemoState(1)) {
                  qs.setMemoState(2);
                  qs.setCond(2, true);
                  htmltext = event;
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState qs = this.getQuestState(player, true);
      int memoState = qs.getMemoState();
      String htmltext = getNoQuestMsg(player);
      if (qs.isCreated()) {
         if (npc.getId() == 30512) {
            QuestState q187 = player.getQuestState(_187_NikolasHeart.class.getSimpleName());
            if (q187 != null && q187.isCompleted()) {
               htmltext = player.getLevel() >= 42 ? "30512-01.htm" : "30512-02.htm";
            }
         }
      } else if (qs.isStarted()) {
         switch(npc.getId()) {
            case 30113:
               if (memoState == 1) {
                  htmltext = "30113-01.htm";
               } else if (memoState == 2) {
                  htmltext = "30113-04.htm";
               }
               break;
            case 30512:
               if (memoState == 1) {
                  htmltext = "30512-04.htm";
               } else if (memoState == 2) {
                  htmltext = "30512-05.htm";
               } else if (memoState >= 3 && memoState <= 4) {
                  htmltext = "30512-07.htm";
               } else if (memoState == 5) {
                  htmltext = "30512-08.htm";
                  if (player.getLevel() < 48) {
                     qs.calcExpAndSp(this.getId());
                  }

                  qs.calcReward(this.getId());
                  qs.exitQuest(false, true);
               }
               break;
            case 30621:
               if (memoState == 4) {
                  qs.setMemoState(5);
                  qs.setCond(5, true);
                  htmltext = "30621-01.htm";
               } else if (memoState == 5) {
                  htmltext = "30621-02.htm";
               }
               break;
            case 30673:
               if (memoState == 3) {
                  qs.setMemoState(4);
                  qs.setCond(4, true);
                  htmltext = "30673-01.htm";
               } else if (memoState == 4) {
                  htmltext = "30673-02.htm";
               }
         }
      } else if (qs.isCompleted() && npc.getId() == 30512) {
         htmltext = getAlreadyCompletedMsg(player);
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new _190_LostDream(190, _190_LostDream.class.getSimpleName(), "");
   }
}
