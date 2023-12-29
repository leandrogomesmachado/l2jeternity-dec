package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _10283_RequestOfIceMerchant extends Quest {
   private boolean _isBusy = false;
   private int _talker = 0;

   public _10283_RequestOfIceMerchant(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32020);
      this.addTalkId(new int[]{32020, 32022, 32760});
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      if (npc.getId() == 32760 && "DESPAWN".equals(event)) {
         this._isBusy = false;
         this._talker = 0;
         npc.deleteMe();
         return super.onAdvEvent(event, npc, player);
      } else {
         QuestState st = player.getQuestState(this.getName());
         if (st == null) {
            return null;
         } else {
            String htmltext = null;
            switch(event) {
               case "32020-03.htm":
                  htmltext = event;
                  break;
               case "32020-04.htm":
                  st.startQuest();
                  st.setMemoState(1);
                  htmltext = event;
                  break;
               case "32020-05.htm":
               case "32020-06.htm":
                  if (st.isMemoState(1)) {
                     htmltext = event;
                  }
                  break;
               case "32020-07.htm":
                  if (st.isMemoState(1)) {
                     st.setMemoState(2);
                     st.setCond(2);
                     htmltext = event;
                  }
                  break;
               case "32022-02.htm":
                  if (st.isMemoState(2)) {
                     if (!this._isBusy) {
                        this._isBusy = true;
                        this._talker = player.getObjectId();
                        st.setCond(3);
                        addSpawn(32760, 104476, -107535, -3688, 44954, false, 0L, false);
                     } else {
                        htmltext = this._talker == player.getObjectId() ? event : "32022-03.htm";
                     }
                  }
                  break;
               case "32760-02.htm":
               case "32760-03.htm":
                  if (st.isMemoState(2)) {
                     htmltext = event;
                  }
                  break;
               case "32760-04.htm":
                  if (st.isMemoState(2)) {
                     st.calcExpAndSp(this.getId());
                     st.calcReward(this.getId());
                     st.exitQuest(false, true);
                     htmltext = event;
                     this.startQuestTimer("DESPAWN", 2000L, npc, null);
                  }
            }

            return htmltext;
         }
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      String htmltext = getNoQuestMsg(player);
      if (st.isCompleted()) {
         if (npc.getId() == 32020) {
            htmltext = "32020-02.htm";
         } else if (npc.getId() == 32760) {
            htmltext = "32760-06.htm";
         }
      } else if (st.isCreated()) {
         QuestState st1 = player.getQuestState("_115_TheOtherSideOfTruth");
         htmltext = player.getLevel() >= 82 && st1 != null && st1.isCompleted() ? "32020-01.htm" : "32020-08.htm";
      } else if (st.isStarted()) {
         switch(npc.getId()) {
            case 32020:
               if (st.isMemoState(1)) {
                  htmltext = "32020-09.htm";
               } else if (st.isMemoState(2)) {
                  htmltext = "32020-10.htm";
               }
               break;
            case 32022:
               if (st.isMemoState(2)) {
                  htmltext = "32022-01.htm";
               }
               break;
            case 32760:
               if (st.isMemoState(2)) {
                  htmltext = this._talker == player.getObjectId() ? "32760-01.htm" : "32760-05.htm";
               }
         }
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new _10283_RequestOfIceMerchant(10283, _10283_RequestOfIceMerchant.class.getSimpleName(), "");
   }
}
