package l2e.scripts.quests;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _271_ProofOfValor extends Quest {
   private static final String qn = "_271_ProofOfValor";
   private static final int KASHA_WOLF_FANG = 1473;
   private static final int NECKLACE_OF_VALOR = 1507;
   private static final int NECKLACE_OF_COURAGE = 1506;
   private static final int RUKAIN = 30577;
   private static final int KASHA_WOLF = 20475;

   public _271_ProofOfValor(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30577);
      this.addTalkId(30577);
      this.addKillId(20475);
      this.questItemIds = new int[]{1473};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_271_ProofOfValor");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30577-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            if (st.getQuestItemsCount(1506) >= 1L || st.getQuestItemsCount(1507) >= 1L) {
               htmltext = "30577-07.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_271_ProofOfValor");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (player.getRace().ordinal() == 3) {
                  if (player.getLevel() >= 4 && player.getLevel() <= 8) {
                     htmltext = "30577-02.htm";
                  } else {
                     htmltext = "30577-01.htm";
                     st.exitQuest(true);
                  }
               } else {
                  htmltext = "30577-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (cond == 1) {
                  htmltext = "30577-04.htm";
               } else if (cond == 2) {
                  htmltext = "30577-05.htm";
                  st.takeItems(1473, -1L);
                  if (st.getRandom(100) <= 10) {
                     st.giveItems(1507, 1L);
                  } else {
                     st.giveItems(1506, 1L);
                  }

                  st.unset("cond");
                  st.playSound("ItemSound.quest_finish");
                  st.exitQuest(true);
               }
               break;
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_271_ProofOfValor");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1) {
            int count = (int)st.getQuestItemsCount(1473);
            int chance = (int)(125.0F * Config.RATE_QUEST_DROP);
            int numItems = chance / 100;
            chance %= 100;
            if (st.getRandom(100) <= chance) {
               ++numItems;
            }

            if (numItems > 0) {
               if (count + numItems >= 50) {
                  st.set("cond", "2");
                  st.playSound("ItemSound.quest_middle");
                  numItems = 50 - count;
               } else {
                  st.playSound("ItemSound.quest_itemget");
               }

               st.giveItems(1473, (long)numItems);
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _271_ProofOfValor(271, "_271_ProofOfValor", "");
   }
}
