package l2e.scripts.quests;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _10279_MutatedKaneusOren extends Quest {
   public _10279_MutatedKaneusOren(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30196);
      this.addTalkId(new int[]{30196, 30189});
      this.addKillId(new int[]{18566, 18568});
      this.questItemIds = new int[]{13836, 13837};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         switch(event) {
            case "30196-03.htm":
               st.startQuest();
               break;
            case "30189-03.htm":
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
            case 30189:
               if (st.isCompleted()) {
                  htmltext = getAlreadyCompletedMsg(player);
               } else if (st.isCond(2)) {
                  htmltext = "30189-02.htm";
               } else {
                  htmltext = "30189-01.htm";
               }
               break;
            case 30196:
               if (st.isCompleted()) {
                  htmltext = "30196-06.htm";
               } else if (st.isCreated()) {
                  htmltext = player.getLevel() >= 48 ? "30196-01.htm" : "30196-00.htm";
               } else if (st.isCond(2)) {
                  htmltext = "30196-05.htm";
               } else {
                  htmltext = "30196-04.htm";
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
      if (npcId == 18566) {
         st.calcDoDropItems(this.getId(), 13836, npcId, 1);
      } else if (npcId == 18568) {
         st.calcDoDropItems(this.getId(), 13837, npcId, 1);
      }

      if (st.hasQuestItems(13836) && st.hasQuestItems(13837)) {
         st.setCond(2, true);
      }
   }

   public static void main(String[] args) {
      new _10279_MutatedKaneusOren(10279, _10279_MutatedKaneusOren.class.getSimpleName(), "");
   }
}
