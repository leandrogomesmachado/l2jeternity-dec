package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _639_GuardiansOfTheHolyGrail extends Quest {
   public _639_GuardiansOfTheHolyGrail(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31350);
      this.addTalkId(new int[]{31350, 32008, 32028});

      for(int i = 22789; i <= 22800; ++i) {
         this.addKillId(i);
      }

      this.addKillId(new int[]{18909, 18910});
      this.questItemIds = new int[]{8070, 8071, 8069};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return null;
      } else {
         if (event.equalsIgnoreCase("31350-03.htm")) {
            st.startQuest();
         } else if (event.equalsIgnoreCase("31350-07.htm")) {
            st.exitQuest(true, true);
         } else if (event.equalsIgnoreCase("31350-08.htm")) {
            long items = st.getQuestItemsCount(8069);
            st.takeItems(8069, -1L);
            st.calcRewardPerItem(this.getId(), 1, (int)items);
         } else if (event.equalsIgnoreCase("32008-05.htm")) {
            st.setCond(2, true);
            st.giveItems(8070, 1L);
         } else if (event.equalsIgnoreCase("32028-02.htm")) {
            st.setCond(3, true);
            st.takeItems(8070, -1L);
            st.giveItems(8071, 1L);
         } else if (event.equalsIgnoreCase("32008-07.htm")) {
            st.setCond(4, true);
            st.takeItems(8071, -1L);
         } else if (event.equalsIgnoreCase("32008-08a.htm")) {
            st.takeItems(8069, 4000L);
            st.calcReward(this.getId(), 2);
         } else if (event.equalsIgnoreCase("32008-08b.htm")) {
            st.takeItems(8069, 400L);
            st.calcReward(this.getId(), 3);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = Quest.getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getCond();
         switch(st.getState()) {
            case 0:
               if (npcId == 31350) {
                  if (player.getLevel() >= 73) {
                     htmltext = "31350-01.htm";
                  } else {
                     htmltext = "31350-00.htm";
                     st.exitQuest(true);
                  }
               }
               break;
            case 1:
               if (npcId == 31350) {
                  if (st.getQuestItemsCount(8069) > 0L) {
                     htmltext = "31350-04.htm";
                  } else {
                     htmltext = "31350-05.htm";
                  }
               } else if (npcId == 32008) {
                  if (cond == 1) {
                     htmltext = "32008-01.htm";
                  } else if (cond == 2) {
                     htmltext = "32008-05b.htm";
                  } else if (cond == 3) {
                     htmltext = "32008-06.htm";
                  } else if (cond == 4) {
                     if (st.getQuestItemsCount(8069) < 400L) {
                        htmltext = "32008-08d.htm";
                     } else if (st.getQuestItemsCount(8069) >= 4000L) {
                        htmltext = "32008-08c.htm";
                     } else {
                        htmltext = "32008-08.htm";
                     }
                  }
               } else if (npcId == 32028 && cond == 2) {
                  htmltext = "32028-01.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMemberState(player, (byte)1);
      if (partyMember == null) {
         return super.onKill(npc, player, isSummon);
      } else {
         QuestState st = partyMember.getQuestState(this.getName());
         if (st != null) {
            st.calcDropItems(this.getId(), 8069, npc.getId(), Integer.MAX_VALUE);
         }

         return super.onKill(npc, player, isSummon);
      }
   }

   public static void main(String[] args) {
      new _639_GuardiansOfTheHolyGrail(639, _639_GuardiansOfTheHolyGrail.class.getSimpleName(), "");
   }
}
