package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _10296_SevenSignsPoweroftheSeal extends Quest {
   public _10296_SevenSignsPoweroftheSeal(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32792);
      this.addTalkId(new int[]{32792, 32787, 32784, 30832, 32593, 32597});
      this.addKillId(18949);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32792-03.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("32784-03.htm")) {
            st.setCond(4, true);
         } else if (event.equalsIgnoreCase("30832-03.htm")) {
            st.setCond(5, true);
         } else if (event.equalsIgnoreCase("32597-03.htm")) {
            if (player.getLevel() >= 81) {
               st.unset("EtisKilled");
               st.calcExpAndSp(this.getId());
               st.calcReward(this.getId());
               st.exitQuest(false, true);
            } else {
               htmltext = "32597-00.htm";
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
         int EtisKilled = st.getInt("EtisKilled");
         if (player.isSubClassActive()) {
            return "no_subclass-allowed.htm";
         } else {
            if (npcId == 32792) {
               if (cond == 0) {
                  QuestState qs = player.getQuestState("_10295_SevenSignsSolinasTomb");
                  if (player.getLevel() >= 81 && qs != null && qs.isCompleted()) {
                     htmltext = "32792-01.htm";
                  } else {
                     htmltext = "32792-00.htm";
                     st.exitQuest(true);
                  }
               } else if (cond == 1) {
                  htmltext = "32792-04.htm";
               } else if (cond == 2) {
                  htmltext = "32792-05.htm";
               } else if (cond >= 3) {
                  htmltext = "32792-06.htm";
               }
            } else if (npcId == 32787) {
               if (cond == 1) {
                  htmltext = "32787-01.htm";
               } else if (cond == 2) {
                  if (EtisKilled == 0) {
                     htmltext = "32787-01.htm";
                  } else {
                     st.setCond(3, true);
                     htmltext = "32787-02.htm";
                  }
               } else if (cond >= 3) {
                  htmltext = "32787-04.htm";
               }
            } else if (npcId == 32784) {
               if (cond == 3) {
                  htmltext = "32784-01.htm";
               } else if (cond >= 4) {
                  htmltext = "32784-03.htm";
               }
            } else if (npcId == 30832) {
               if (cond == 4) {
                  htmltext = "30832-01.htm";
               } else if (cond == 5) {
                  htmltext = "30832-04.htm";
               }
            } else if (npcId == 32593) {
               if (cond == 5) {
                  htmltext = "32593-01.htm";
               }
            } else if (npcId == 32597 && cond == 5) {
               htmltext = "32597-01.htm";
            }

            return htmltext;
         }
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return super.onKill(npc, player, isSummon);
      } else {
         if (npc.getId() == 18949) {
            st.set("EtisKilled", 1);
            player.showQuestMovie(30);
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _10296_SevenSignsPoweroftheSeal(10296, _10296_SevenSignsPoweroftheSeal.class.getSimpleName(), "");
   }
}
