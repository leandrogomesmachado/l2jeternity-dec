package l2e.scripts.quests;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _10278_MutatedKaneusHeine extends Quest {
   public _10278_MutatedKaneusHeine(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30916);
      this.addTalkId(30916);
      this.addTalkId(30907);
      this.addKillId(18562);
      this.addKillId(18564);
      this.questItemIds = new int[]{13834, 13835};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         switch(event) {
            case "30916-03.htm":
               st.startQuest();
               break;
            case "30907-03.htm":
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
            case 30907:
               if (st.isCompleted()) {
                  htmltext = getAlreadyCompletedMsg(player);
               } else if (st.isCond(2)) {
                  htmltext = "30907-02.htm";
               } else {
                  htmltext = "30907-01.htm";
               }
               break;
            case 30916:
               if (st.isCompleted()) {
                  htmltext = "30916-06.htm";
               } else if (st.isCreated()) {
                  htmltext = player.getLevel() >= 38 ? "30916-01.htm" : "30916-00.htm";
               } else if (st.isCond(2)) {
                  htmltext = "30916-05.htm";
               } else {
                  htmltext = "30916-04.htm";
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
                  this.rewardItem(npcId, member);
               }
            }
         } else {
            this.rewardItem(npcId, st);
         }

         return null;
      }
   }

   private final void rewardItem(int npcId, QuestState st) {
      if (npcId == 18562) {
         st.calcDoDropItems(this.getId(), 13834, npcId, 1);
      } else if (npcId == 18564) {
         st.calcDoDropItems(this.getId(), 13835, npcId, 1);
      }

      if (st.hasQuestItems(13834) && st.hasQuestItems(13835)) {
         st.setCond(2, true);
      }
   }

   public static void main(String[] args) {
      new _10278_MutatedKaneusHeine(10278, _10278_MutatedKaneusHeine.class.getSimpleName(), "");
   }
}
