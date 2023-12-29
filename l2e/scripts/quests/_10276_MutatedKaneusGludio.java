package l2e.scripts.quests;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _10276_MutatedKaneusGludio extends Quest {
   public _10276_MutatedKaneusGludio(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30332);
      this.addTalkId(new int[]{30332, 30344});
      this.addKillId(new int[]{18554, 18555});
      this.questItemIds = new int[]{13830, 13831};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         switch(event) {
            case "30332-03.htm":
               st.startQuest();
               break;
            case "30344-03.htm":
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
            case 30332:
               if (st.isCompleted()) {
                  htmltext = "30332-06.htm";
               } else if (st.isCreated()) {
                  htmltext = player.getLevel() >= 18 ? "30332-01.htm" : "30332-00.htm";
               } else if (st.isCond(2)) {
                  htmltext = "30332-05.htm";
               } else {
                  htmltext = "30332-04.htm";
               }
               break;
            case 30344:
               if (st.isCompleted()) {
                  htmltext = getAlreadyCompletedMsg(player);
               } else if (st.isCond(2)) {
                  htmltext = "30344-02.htm";
               } else {
                  htmltext = "30344-01.htm";
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
         if (npcId == 18554 || npcId == 18555) {
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
         }

         return null;
      }
   }

   private final void rewardItem(int npcId, QuestState st) {
      if (npcId == 18554) {
         st.calcDoDropItems(this.getId(), 13830, npcId, 1);
      } else if (npcId == 18555) {
         st.calcDoDropItems(this.getId(), 13831, npcId, 1);
      }

      if (st.hasQuestItems(13830) && st.hasQuestItems(13831)) {
         st.setCond(2, true);
      }
   }

   public static void main(String[] args) {
      new _10276_MutatedKaneusGludio(10276, _10276_MutatedKaneusGludio.class.getSimpleName(), "");
   }
}
