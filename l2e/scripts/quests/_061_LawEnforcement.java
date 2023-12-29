package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.ClassId;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _061_LawEnforcement extends Quest {
   private _061_LawEnforcement(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32222);
      this.addTalkId(32222);
      this.addTalkId(32138);
      this.addTalkId(32469);
   }

   @Override
   public final String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32222-05.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("32138-09.htm")) {
            st.setCond(2, true);
         } else if (event.equalsIgnoreCase("32469-08.htm") || event.equals("32469-09.htm")) {
            player.setClassId(ClassId.judicator.getId());
            player.broadcastCharInfo();
            st.calcReward(this.getId());
            st.exitQuest(false, true);
         }

         return event;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getCond();
         if (st.isCompleted()) {
            return getAlreadyCompletedMsg(player);
         } else {
            if (npcId == 32222) {
               if (cond == 0) {
                  if (player.getRace() == Race.Kamael) {
                     if (player.getClassId() == ClassId.inspector && player.getLevel() >= 76) {
                        return "32222-01.htm";
                     }

                     return "32222-02.htm";
                  }

                  return "32222-03.htm";
               }

               if (cond == 1) {
                  return "32222-06.htm";
               }
            } else if (npcId == 32138) {
               if (cond == 1) {
                  return "32138-01.htm";
               }

               if (cond == 2) {
                  return "32138-10.htm";
               }
            } else if (npcId == 32469 && cond == 2) {
               return "32469-01.htm";
            }

            return htmltext;
         }
      }
   }

   public static void main(String[] args) {
      new _061_LawEnforcement(61, _061_LawEnforcement.class.getSimpleName(), "");
   }
}
