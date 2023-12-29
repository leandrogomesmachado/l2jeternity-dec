package l2e.scripts.quests;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _10281_MutatedKaneusRune extends Quest {
   public _10281_MutatedKaneusRune(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31340);
      this.addTalkId(31340);
      this.addTalkId(31335);
      this.addKillId(18577);
      this.questItemIds = new int[]{13840};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         switch(event) {
            case "31340-03.htm":
               st.startQuest();
               break;
            case "31335-03.htm":
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
            case 31335:
               if (st.isCompleted()) {
                  htmltext = Quest.getAlreadyCompletedMsg(player);
               } else if (st.isCond(2)) {
                  htmltext = "31335-02.htm";
               } else {
                  htmltext = "31335-01.htm";
               }
               break;
            case 31340:
               if (st.isCompleted()) {
                  htmltext = "31340-06.htm";
               } else if (st.isCreated()) {
                  htmltext = player.getLevel() >= 68 ? "31340-01.htm" : "31340-00.htm";
               } else if (st.isCond(2)) {
                  htmltext = "31340-05.htm";
               } else {
                  htmltext = "31340-04.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player killer, boolean isSummon) {
      QuestState st = killer.getQuestState(this.getName());
      if (st == null) {
         return null;
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
                  if (member.calcDropItems(this.getId(), 13840, npcId, 1)) {
                     member.setCond(2);
                  }
               }
            }
         } else if (st.calcDropItems(this.getId(), 13840, npcId, 1)) {
            st.setCond(2);
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _10281_MutatedKaneusRune(10281, _10281_MutatedKaneusRune.class.getSimpleName(), "");
   }
}
