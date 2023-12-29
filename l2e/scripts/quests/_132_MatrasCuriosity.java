package l2e.scripts.quests;

import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _132_MatrasCuriosity extends Quest {
   private static String qn = "_132_MatrasCuriosity";
   private final int MATRAS = 32245;
   private final int DEMONPRINCE = 25540;
   private final int RANKU = 25542;
   private final int RANKUSBLUEPRINT = 9800;
   private final int PRINCESBLUEPRINT = 9801;
   private final int ROUGHOREOFFIRE = 10521;
   private final int ROUGHOREOFWATER = 10522;
   private final int ROUGHOREOFTHEEARTH = 10523;
   private final int ROUGHOREOFWIND = 10524;
   private final int ROUGHOREOFDARKNESS = 10525;
   private final int ROUGHOREOFDIVINITY = 10526;

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(qn);
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32245-02.htm")) {
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
            st.setState((byte)1);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(qn);
      if (st == null) {
         st = this.newQuestState(player);
      }

      int npcId = npc.getId();
      byte id = st.getState();
      int cond = st.getInt("cond");
      if (id == 2) {
         if (npcId == 32245) {
            htmltext = getAlreadyCompletedMsg(player);
         }
      } else if (id == 0) {
         if (npcId == 32245 && cond == 0) {
            if (player.getLevel() >= 76) {
               htmltext = "32245-01.htm";
            } else {
               htmltext = "32245-00.htm";
               st.exitQuest(true);
            }
         }
      } else if (id == 1 && npcId == 32245) {
         if (cond == 1) {
            if (st.getQuestItemsCount(9801) == 1L && st.getQuestItemsCount(9800) == 1L) {
               st.set("cond", "2");
               st.playSound("ItemSound.quest_middle");
            } else {
               htmltext = "32245-03.htm";
            }
         } else if (cond == 2) {
            st.takeItems(9800, -1L);
            st.takeItems(9801, -1L);
            st.set("cond", "3");
            st.playSound("ItemSound.quest_middle");
            htmltext = "32245-04.htm";
         } else if (cond == 3) {
            st.giveItems(10521, 1L);
            st.giveItems(10522, 1L);
            st.giveItems(10523, 1L);
            st.giveItems(10524, 1L);
            st.giveItems(10525, 1L);
            st.giveItems(10526, 1L);
            st.giveItems(57, 65884L);
            st.addExpAndSp(50541, 5094);
            st.unset("cond");
            st.exitQuest(false);
            st.playSound("ItemSound.quest_finish");
            htmltext = "32245-05.htm";
         }
      }

      return htmltext;
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      String htmltext = null;
      int npcId = npc.getId();
      if (npcId == 25540 || npcId == 25542) {
         Party party = player.getParty();
         if (party != null) {
            for(Player member : party.getMembers()) {
               QuestState st = member.getQuestState(qn);
               if (st != null && st.getState() == 1) {
                  if (npcId == 25540 && st.getQuestItemsCount(9801) == 0L) {
                     st.giveItems(9801, 1L);
                  } else if (npcId == 25542 && st.getQuestItemsCount(9800) == 0L) {
                     st.giveItems(9800, 1L);
                  }

                  st.playSound("ItemSound.quest_itemget");
                  if (st.getQuestItemsCount(9801) > 0L && st.getQuestItemsCount(9800) > 0L) {
                     st.set("cond", "2");
                     st.playSound("ItemSound.quest_middle");
                  }
               }
            }
         } else {
            QuestState st = player.getQuestState(qn);
            if (st != null && st.getState() == 1) {
               if (npcId == 25540 && st.getQuestItemsCount(9801) == 0L) {
                  st.giveItems(9801, 1L);
               } else if (npcId == 25542 && st.getQuestItemsCount(9800) == 0L) {
                  st.giveItems(9800, 1L);
               }

               st.playSound("ItemSound.quest_itemget");
               if (st.getQuestItemsCount(9801) > 0L && st.getQuestItemsCount(9800) > 0L) {
                  st.set("cond", "2");
                  st.playSound("ItemSound.quest_middle");
               }
            }
         }
      }

      return htmltext;
   }

   public _132_MatrasCuriosity(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addTalkId(32245);
      this.addStartNpc(32245);
      this.addKillId(25540);
      this.addKillId(25542);
   }

   public static void main(String[] args) {
      new _132_MatrasCuriosity(132, qn, "");
   }
}
