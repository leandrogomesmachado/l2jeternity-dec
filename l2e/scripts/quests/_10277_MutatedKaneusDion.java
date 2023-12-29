package l2e.scripts.quests;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _10277_MutatedKaneusDion extends Quest {
   public _10277_MutatedKaneusDion(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30071);
      this.addTalkId(new int[]{30071, 30461});
      this.addKillId(new int[]{18558, 18559});
      this.questItemIds = new int[]{13832, 13833};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         switch(event) {
            case "30071-03.htm":
               st.startQuest();
               break;
            case "30461-03.htm":
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
         switch(npc.getId()) {
            case 30071:
               if (st.isCompleted()) {
                  htmltext = "30071-06.htm";
               } else if (st.isCreated()) {
                  htmltext = player.getLevel() >= 28 ? "30071-01.htm" : "30071-00.htm";
               } else if (st.isCond(2)) {
                  htmltext = "30071-05.htm";
               } else {
                  htmltext = "30071-04.htm";
               }
               break;
            case 30461:
               if (st.isCompleted()) {
                  htmltext = getAlreadyCompletedMsg(player);
               } else if (st.isCond(2)) {
                  htmltext = "30461-02.htm";
               } else {
                  htmltext = "30461-01.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState st = killer.getQuestState(this.getName());
      if (st == null) {
         return super.onKill(npc, killer, isSummon);
      } else {
         int npcId = npc.getId();
         if (killer.getParty() != null) {
            List<QuestState> members = new ArrayList<>();

            for(Player member : killer.getParty().getMembers()) {
               st = member.getQuestState(this.getName());
               if (st != null && st.isStarted()) {
                  members.add(st);
               }
            }

            if (!members.isEmpty()) {
               for(QuestState member : members) {
                  this.rewardItem(npcId, member);
               }
            }
         } else if (st.isStarted()) {
            this.rewardItem(npcId, st);
         }

         return super.onKill(npc, killer, isSummon);
      }
   }

   private final void rewardItem(int npcId, QuestState st) {
      if (npcId == 18558) {
         st.calcDoDropItems(this.getId(), 13832, npcId, 1);
      } else if (npcId == 18559) {
         st.calcDoDropItems(this.getId(), 13833, npcId, 1);
      }

      if (st.hasQuestItems(13832) && st.hasQuestItems(13833)) {
         st.setCond(2, true);
      }
   }

   public static void main(String[] args) {
      new _10277_MutatedKaneusDion(10277, _10277_MutatedKaneusDion.class.getSimpleName(), "");
   }
}
