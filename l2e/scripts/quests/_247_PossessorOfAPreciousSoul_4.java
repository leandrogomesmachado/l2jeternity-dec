package l2e.scripts.quests;

import l2e.gameserver.model.ClanMember;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.olympiad.Olympiad;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _247_PossessorOfAPreciousSoul_4 extends Quest {
   public _247_PossessorOfAPreciousSoul_4(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(31740);
      this.addTalkId(new int[]{31740, 31745});
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         int cond = st.getCond();
         if (event.equals("31740-3.htm")) {
            if (cond == 0) {
               st.startQuest();
            }
         } else if (event.equals("31740-5.htm")) {
            if (cond == 1) {
               st.setCond(2, true);
               st.takeItems(7679, -1L);
               player.teleToLocation(143209, 43968, -3038, true);
            }
         } else if (event.equals("31745-5.htm") && cond == 2) {
            Olympiad.addNoble(player);
            player.setNoble(true);
            if (player.getClan() != null) {
               player.setPledgeClass(ClanMember.calculatePledgeClass(player));
            } else {
               player.setPledgeClass(5);
            }

            player.sendUserInfo();
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
      } else if (npc.getId() != 31740 && st.getState() != 1) {
         return htmltext;
      } else {
         int cond = st.getCond();
         if (st.getState() == 0) {
            st.set("cond", "0");
         }

         if (player.isSubClassActive()) {
            if (npc.getId() == 31740) {
               if (st.getState() == 2) {
                  htmltext = getAlreadyCompletedMsg(player);
               } else {
                  QuestState qs = player.getQuestState(_246_PossessorOfAPreciousSoul_3.class.getSimpleName());
                  if (qs != null && qs.isCompleted()) {
                     if (cond == 0 || cond == 1) {
                        if (player.getLevel() < 75) {
                           htmltext = "31740-2.htm";
                           st.exitQuest(true);
                        } else if (player.getLevel() >= 75) {
                           htmltext = "31740-1.htm";
                        }
                     } else if (cond == 2) {
                        htmltext = "31740-6.htm";
                     }
                  }
               }
            } else if (npc.getId() == 31745 && cond == 2) {
               htmltext = "31745-1.htm";
            }
         } else {
            htmltext = "31740-0.htm";
         }

         return htmltext;
      }
   }

   public static void main(String[] args) {
      new _247_PossessorOfAPreciousSoul_4(247, _247_PossessorOfAPreciousSoul_4.class.getSimpleName(), "");
   }
}
