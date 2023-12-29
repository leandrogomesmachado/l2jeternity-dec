package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _627_HeartInSearchOfPower extends Quest {
   public _627_HeartInSearchOfPower(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31518);
      this.addTalkId(31518);
      this.addTalkId(31519);

      for(int mobs = 21520; mobs <= 21541; ++mobs) {
         this.addKillId(mobs);
      }

      this.questItemIds = new int[]{7171};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31518-1.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("31518-3.htm")) {
            st.takeItems(7171, 300L);
            st.giveItems(7170, 1L);
            st.setCond(3, true);
         } else if (event.equalsIgnoreCase("31519-1.htm")) {
            st.takeItems(7170, 1L);
            st.giveItems(7172, 1L);
            st.setCond(4, true);
         } else if (event.equalsIgnoreCase("31518-5.htm") && st.getQuestItemsCount(7172) == 1L) {
            st.takeItems(7172, 1L);
            st.setCond(5, true);
         } else {
            if (event.equalsIgnoreCase("31518-6.htm")) {
               st.calcReward(this.getId(), 1);
            } else if (event.equalsIgnoreCase("31518-7.htm")) {
               st.calcReward(this.getId(), 2);
            } else if (event.equalsIgnoreCase("31518-8.htm")) {
               st.calcReward(this.getId(), 3);
            } else if (event.equalsIgnoreCase("31518-9.htm")) {
               st.calcReward(this.getId(), 4);
            } else if (event.equalsIgnoreCase("31518-10.htm")) {
               st.calcReward(this.getId(), 5);
            }

            st.exitQuest(true, true);
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
         int npcId = npc.getId();
         int cond = st.getCond();
         switch(st.getState()) {
            case 0:
               if (npcId == 31518) {
                  if (player.getLevel() >= 60) {
                     htmltext = "31518-0.htm";
                  } else {
                     htmltext = "31518-0a.htm";
                     st.exitQuest(true);
                  }
               }
               break;
            case 1:
               if (npcId == 31518) {
                  if (cond == 1) {
                     htmltext = "31518-1a.htm";
                  } else if (st.getQuestItemsCount(7171) >= 300L) {
                     htmltext = "31518-2.htm";
                  } else if (st.getQuestItemsCount(7172) > 0L) {
                     htmltext = "31518-4.htm";
                  } else if (cond == 5) {
                     htmltext = "31518-5.htm";
                  }
               } else if (npcId == 31519 && st.getQuestItemsCount(7170) > 0L) {
                  htmltext = "31519-0.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMemberState(player, (byte)1);
      if (partyMember == null) {
         return super.onKill(npc, player, isSummon);
      } else {
         QuestState st = partyMember.getQuestState(this.getName());
         if (st != null && st.isCond(1) && st.calcDropItems(this.getId(), 7171, npc.getId(), 300)) {
            st.setCond(2);
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _627_HeartInSearchOfPower(627, _627_HeartInSearchOfPower.class.getSimpleName(), "");
   }
}
