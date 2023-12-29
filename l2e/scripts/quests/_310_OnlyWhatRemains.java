package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _310_OnlyWhatRemains extends Quest {
   public _310_OnlyWhatRemains(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32640);
      this.addTalkId(32640);
      this.addKillId(22617);
      this.addKillId(22618);
      this.addKillId(22619);
      this.addKillId(22620);
      this.addKillId(22621);
      this.addKillId(22622);
      this.addKillId(22623);
      this.addKillId(22624);
      this.addKillId(22625);
      this.addKillId(22626);
      this.addKillId(22627);
      this.addKillId(22628);
      this.addKillId(22629);
      this.addKillId(22630);
      this.addKillId(22631);
      this.addKillId(22632);
      this.addKillId(22633);
      this.registerQuestItems(new int[]{14880});
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         String htmltext = event;
         switch(event) {
            case "32640-04.htm":
               st.startQuest();
            case "32640-02.htm":
            case "32640-03.htm":
            case "32640-05.htm":
            case "32640-06.htm":
            case "32640-07.htm":
               break;
            default:
               htmltext = null;
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
         int cond = st.getCond();
         switch(st.getState()) {
            case 0:
               QuestState prev = player.getQuestState("_240_ImTheOnlyOneYouCanTrust");
               htmltext = player.getLevel() >= 81 && prev != null && prev.isCompleted() ? "32640-01.htm" : "32640-00.htm";
               break;
            case 1:
               if (cond == 1) {
                  if (!st.hasQuestItems(14880)) {
                     htmltext = "32640-08.htm";
                  } else {
                     htmltext = "32640-09.htm";
                  }
               } else if (cond == 2) {
                  st.takeItems(14880, 500L);
                  st.calcReward(this.getId());
                  st.exitQuest(true, true);
                  htmltext = "32640-10.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMember(player, 1);
      if (partyMember == null) {
         return super.onKill(npc, player, isSummon);
      } else {
         QuestState st = partyMember.getQuestState(this.getName());
         if (st.calcDropItems(this.getId(), 14880, npc.getId(), 500)) {
            st.setCond(2);
         }

         return super.onKill(npc, player, isSummon);
      }
   }

   public static void main(String[] args) {
      new _310_OnlyWhatRemains(310, _310_OnlyWhatRemains.class.getSimpleName(), "");
   }
}
