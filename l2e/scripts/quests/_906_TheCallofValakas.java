package l2e.scripts.quests;

import l2e.commons.util.Rnd;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _906_TheCallofValakas extends Quest {
   private static final String qn = "_906_TheCallofValakas";
   private static final int Klein = 31540;
   private static final int LavasaurusAlphaFragment = 21993;
   private static final int ValakasMinion = 29029;

   public _906_TheCallofValakas(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31540);
      this.addTalkId(31540);
      this.addKillId(29029);
      this.questItemIds = new int[]{21993};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_906_TheCallofValakas");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31540-04.htm")) {
            st.setState((byte)1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("31540-07.htm")) {
            st.takeItems(21993, -1L);
            st.giveItems(21895, 1L);
            st.setState((byte)2);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(QuestState.QuestType.DAILY);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_906_TheCallofValakas");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         if (npc.getId() == 31540) {
            switch(st.getState()) {
               case 0:
                  if (player.getLevel() >= 83) {
                     if (st.getQuestItemsCount(7267) > 0L) {
                        htmltext = "31540-01.htm";
                     } else {
                        htmltext = "31540-00b.htm";
                     }
                  } else {
                     htmltext = "31540-00.htm";
                     st.exitQuest(true);
                  }
                  break;
               case 1:
                  if (cond == 1) {
                     htmltext = "31540-05.htm";
                  } else if (cond == 2) {
                     htmltext = "31540-06.htm";
                  }
                  break;
               case 2:
                  if (st.isNowAvailable()) {
                     if (player.getLevel() >= 83) {
                        if (st.getQuestItemsCount(7267) > 0L) {
                           htmltext = "31540-01.htm";
                        } else {
                           htmltext = "31540-00b.htm";
                        }
                     } else {
                        htmltext = "31540-00.htm";
                        st.exitQuest(true);
                     }
                  } else {
                     htmltext = "31540-00a.htm";
                  }
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
         QuestState st = partyMember.getQuestState("_906_TheCallofValakas");
         if (st == null) {
            return null;
         } else {
            int cond = st.getInt("cond");
            int npcId = npc.getId();
            if (cond == 1 && npcId == 29029 && Rnd.calcChance(40.0)) {
               st.giveItems(21993, 1L);
               st.set("cond", "2");
               st.playSound("ItemSound.quest_middle");
            }

            if (player.getParty() != null) {
               for(Player pmember : player.getParty().getMembers()) {
                  QuestState st2 = pmember.getQuestState("_906_TheCallofValakas");
                  if (st2 != null && cond == 1 && pmember.getObjectId() != partyMember.getObjectId() && npcId == 29029 && Rnd.calcChance(40.0)) {
                     st.giveItems(21993, 1L);
                     st.set("cond", "2");
                     st.playSound("ItemSound.quest_middle");
                  }
               }
            }

            return super.onKill(npc, player, isSummon);
         }
      }
   }

   public static void main(String[] args) {
      new _906_TheCallofValakas(906, "_906_TheCallofValakas", "");
   }
}
