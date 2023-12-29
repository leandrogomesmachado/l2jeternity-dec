package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _10504_JewelOfAntharas extends Quest {
   private static final String qn = "_10504_JewelOfAntharas";
   private static final int THEODRIC = 30755;
   private static final int ULTIMATE_ANTHARAS = 29068;
   private static final int CLEAR_CRYSTAL = 21905;
   private static final int FILLED_CRYSTAL_ANTHARAS = 21907;
   private static final int PORTAL_STONE = 3865;
   private static final int JEWEL_OF_ANTHARAS = 21898;

   public _10504_JewelOfAntharas(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30755);
      this.addTalkId(30755);
      this.addKillId(29068);
      this.questItemIds = new int[]{21905, 21907};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_10504_JewelOfAntharas");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30755-04.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(21905, 1L);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_10504_JewelOfAntharas");
      if (st == null) {
         return htmltext;
      } else {
         int npcId = npc.getId();
         int cond = st.getInt("cond");
         int id = st.getState();
         if (id == 0 && cond == 0) {
            if (npcId == 30755) {
               if (player.getLevel() < 84) {
                  htmltext = "30755-00.htm";
               } else if (st.getQuestItemsCount(3865) < 1L) {
                  htmltext = "30755-00a.htm";
               } else {
                  htmltext = "30755-01.htm";
               }
            }
         } else if (id == 1) {
            if (npcId == 30755) {
               if (cond == 1) {
                  if (st.getQuestItemsCount(21905) < 1L) {
                     htmltext = "30755-08.htm";
                     st.giveItems(21905, 1L);
                  } else {
                     htmltext = "30755-05.htm";
                  }
               } else if (cond == 2) {
                  if (st.getQuestItemsCount(21907) >= 1L) {
                     htmltext = "30755-07.htm";
                     st.takeItems(21907, -1L);
                     st.giveItems(21898, 1L);
                     st.playSound("ItemSound.quest_finish");
                     st.setState((byte)2);
                     st.exitQuest(QuestState.QuestType.DAILY);
                  } else {
                     htmltext = "30755-06.htm";
                  }
               }
            }
         } else if (id == 2 && npcId == 30755) {
            if (st.isNowAvailable()) {
               if (player.getLevel() < 84) {
                  htmltext = "30755-00.htm";
               } else if (st.getQuestItemsCount(3865) < 1L) {
                  htmltext = "30755-00a.htm";
               } else {
                  htmltext = "30755-01.htm";
               }
            } else {
               htmltext = "30755-09.htm";
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
         QuestState st = partyMember.getQuestState("_10504_JewelOfAntharas");
         if (st == null) {
            return super.onKill(npc, player, isSummon);
         } else {
            int npcId = npc.getId();
            int cond = st.getInt("cond");
            if (cond == 1 && npcId == 29068) {
               st.takeItems(21905, -1L);
               st.giveItems(21907, 1L);
               st.set("cond", "2");
               st.playSound("ItemSound.quest_middle");
            }

            if (player.getParty() != null) {
               for(Player pmember : player.getParty().getMembers()) {
                  QuestState st2 = pmember.getQuestState("_10504_JewelOfAntharas");
                  if (st2 != null && st2.getInt("cond") == 1 && pmember.getObjectId() != partyMember.getObjectId() && npcId == 29068) {
                     st.takeItems(21905, -1L);
                     st.giveItems(21907, 1L);
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
      new _10504_JewelOfAntharas(10504, "_10504_JewelOfAntharas", "");
   }
}
