package l2e.scripts.quests;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _10280_MutatedKaneusSchuttgart extends Quest {
   public _10280_MutatedKaneusSchuttgart(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31981);
      this.addTalkId(new int[]{31981, 31972});
      this.addKillId(new int[]{18571, 18573});
      this.questItemIds = new int[]{13838, 13839};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         switch(event) {
            case "31981-03.htm":
               st.startQuest();
               break;
            case "31972-03.htm":
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
            case 31972:
               if (st.isCompleted()) {
                  htmltext = getAlreadyCompletedMsg(player);
               } else if (st.isCond(2)) {
                  htmltext = "31972-02.htm";
               } else {
                  htmltext = "31972-01.htm";
               }
               break;
            case 31981:
               if (st.isCompleted()) {
                  htmltext = "31981-06.htm";
               } else if (st.isCreated()) {
                  htmltext = player.getLevel() >= 58 ? "31981-01.htm" : "31981-00.htm";
               } else if (st.isCond(2)) {
                  htmltext = "31981-05.htm";
               } else {
                  htmltext = "31981-04.htm";
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
      if (npcId == 18571) {
         st.calcDoDropItems(this.getId(), 13838, npcId, 1);
      } else if (npcId == 18573) {
         st.calcDoDropItems(this.getId(), 13839, npcId, 1);
      }

      if (st.hasQuestItems(13838) && st.hasQuestItems(13839)) {
         st.setCond(2, true);
      }
   }

   public static void main(String[] args) {
      new _10280_MutatedKaneusSchuttgart(10280, _10280_MutatedKaneusSchuttgart.class.getSimpleName(), "");
   }
}
