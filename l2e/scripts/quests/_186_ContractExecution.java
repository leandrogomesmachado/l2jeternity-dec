package l2e.scripts.quests;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _186_ContractExecution extends Quest {
   public _186_ContractExecution(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30673);
      this.addTalkId(new int[]{30673, 31437, 30621});

      for(int mobs = 20577; mobs <= 20583; ++mobs) {
         this.addKillId(mobs);
      }

      this.questItemIds = new int[]{10366, 10367};
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
               if (player.getLevel() >= 41 && hasQuestItems(player, 10362)) {
                  qs.startQuest();
                  qs.setMemoState(1);
                  giveItems(player, 10366, 1L);
                  takeItems(player, 10362, -1L);
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
            case "31437-03.htm":
               if (qs.isMemoState(2) && hasQuestItems(player, 10367)) {
                  htmltext = event;
               }
               break;
            case "31437-04.htm":
               if (qs.isMemoState(2) && hasQuestItems(player, 10367)) {
                  qs.setMemoState(3);
                  htmltext = event;
               }
               break;
            case "31437-06.htm":
               if (qs.isMemoState(3)) {
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
      int memoState = qs.getMemoState();
      String htmltext = getNoQuestMsg(player);
      if (qs.isCreated()) {
         if (npc.getId() == 30673) {
            QuestState q184 = player.getQuestState(_184_NikolasCooperationContract.class.getSimpleName());
            if (q184 != null && q184.isCompleted() && hasQuestItems(player, 10362)) {
               htmltext = player.getLevel() >= 41 ? "30673-01.htm" : "30673-02.htm";
            }
         }
      } else if (qs.isStarted()) {
         switch(npc.getId()) {
            case 30621:
               if (memoState == 1) {
                  htmltext = "30621-01.htm";
               } else if (memoState == 2) {
                  htmltext = "30621-04.htm";
               }
               break;
            case 30673:
               if (memoState >= 1) {
                  htmltext = "30673-04.htm";
               }
               break;
            case 31437:
               if (memoState == 2) {
                  if (hasQuestItems(player, 10367)) {
                     htmltext = "31437-02.htm";
                  } else {
                     htmltext = "31437-01.htm";
                  }
               } else if (memoState == 3) {
                  htmltext = "31437-05.htm";
               }
         }
      } else if (qs.isCompleted() && npc.getId() == 30673) {
         htmltext = getAlreadyCompletedMsg(player);
      }

      return htmltext;
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState qs = this.getQuestState(player, false);
      if (qs != null && qs.isMemoState(2) && Util.checkIfInRange(1500, npc, player, false) && qs.calcDropItems(this.getId(), 10367, npc.getId(), 1)) {
         playSound(player, Quest.QuestSound.ITEMSOUND_QUEST_ITEMGET);
      }

      return super.onKill(npc, player, isSummon);
   }

   public static void main(String[] args) {
      new _186_ContractExecution(186, _186_ContractExecution.class.getSimpleName(), "");
   }
}
