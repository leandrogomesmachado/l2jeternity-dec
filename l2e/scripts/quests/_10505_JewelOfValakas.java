package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _10505_JewelOfValakas extends Quest {
   private static final String qn = "_10505_JewelOfValakas";
   private static final int KLEIN = 31540;
   private static final int VALAKAS = 29028;
   private static final int EMPTY_CRYSTAL = 21906;
   private static final int FILLED_CRYSTAL_VALAKAS = 21908;
   private static final int VACUALITE_FLOATING_STONE = 7267;
   private static final int JEWEL_OF_VALAKAS = 21896;

   public _10505_JewelOfValakas(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31540);
      this.addTalkId(31540);
      this.addKillId(29028);
      this.questItemIds = new int[]{21906, 21908};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_10505_JewelOfValakas");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31540-04.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(21906, 1L);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_10505_JewelOfValakas");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         int id = st.getState();
         if (st.isCompleted()) {
            htmltext = getAlreadyCompletedMsg(player);
         }

         if (id == 0 && cond == 0) {
            if (npcId == 31540) {
               if (player.getLevel() < 84) {
                  htmltext = "31540-00.htm";
               } else if (st.getQuestItemsCount(7267) < 1L) {
                  htmltext = "31540-00a.htm";
               } else {
                  htmltext = "31540-01.htm";
               }
            }
         } else if (id == 1) {
            if (npcId == 31540) {
               if (cond == 1) {
                  if (st.getQuestItemsCount(21906) < 1L) {
                     htmltext = "31540-08.htm";
                     st.giveItems(21906, 1L);
                  } else {
                     htmltext = "31540-05.htm";
                  }
               } else if (cond == 2) {
                  if (st.getQuestItemsCount(21908) >= 1L) {
                     htmltext = "31540-07.htm";
                     st.takeItems(21908, -1L);
                     st.giveItems(21896, 1L);
                     st.playSound("ItemSound.quest_finish");
                     st.setState((byte)2);
                     st.exitQuest(QuestState.QuestType.DAILY);
                  } else {
                     htmltext = "31540-06.htm";
                  }
               }
            }
         } else if (id == 2 && npcId == 31540) {
            if (st.isNowAvailable()) {
               if (player.getLevel() < 84) {
                  htmltext = "31540-00.htm";
               } else if (st.getQuestItemsCount(7267) < 1L) {
                  htmltext = "31540-00a.htm";
               } else {
                  htmltext = "31540-01.htm";
               }
            } else {
               htmltext = "31540-09.htm";
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
         QuestState st = partyMember.getQuestState("_10505_JewelOfValakas");
         if (st == null) {
            return super.onKill(npc, player, isSummon);
         } else {
            int npcId = npc.getId();
            int cond = st.getInt("cond");
            if (cond == 1 && npcId == 29028) {
               st.takeItems(21906, -1L);
               st.giveItems(21908, 1L);
               st.set("cond", "2");
               st.playSound("ItemSound.quest_middle");
            }

            if (player.getParty() != null) {
               for(Player pmember : player.getParty().getMembers()) {
                  QuestState st2 = pmember.getQuestState("_10505_JewelOfValakas");
                  if (st2 != null && st2.getInt("cond") == 1 && pmember.getObjectId() != partyMember.getObjectId() && npcId == 29028) {
                     st.takeItems(21906, -1L);
                     st.giveItems(21908, 1L);
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
      new _10505_JewelOfValakas(10505, "_10505_JewelOfValakas", "");
   }
}
