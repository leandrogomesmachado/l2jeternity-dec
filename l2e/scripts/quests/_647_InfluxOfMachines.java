package l2e.scripts.quests;

import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _647_InfluxOfMachines extends Quest {
   private static final String qn = "_647_InfluxOfMachines";
   private static final int GUTENHAGEN = 32069;
   private static final int BROKEN_GOLEM_FRAGMENT = 15521;
   private static final int[] MOBS = new int[]{22801, 22802, 22803, 22804, 22805, 22806, 22807, 22808, 22809, 22810, 22811, 22812};
   private static final int[] REWARDS = new int[]{6887, 6881, 6897, 7580, 6883, 6899, 6891, 6885, 6893, 6895};

   public _647_InfluxOfMachines(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32069);
      this.addTalkId(32069);

      for(int i : MOBS) {
         this.addKillId(i);
      }

      this.questItemIds = new int[]{15521};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_647_InfluxOfMachines");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32069-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("32069-06.htm")) {
            if (st.getQuestItemsCount(15521) < 500L) {
               htmltext = "32069-07.htm";
            } else if (st.getQuestItemsCount(15521) >= 500L) {
               st.giveItems(REWARDS[getRandom(REWARDS.length)], 1L);
               st.takeItems(15521, 500L);
               st.playSound("ItemSound.quest_finish");
               htmltext = "32069-07.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_647_InfluxOfMachines");
      if (st == null) {
         return htmltext;
      } else {
         if (npc.getId() == 32069) {
            switch(st.getState()) {
               case 0:
                  if (player.getLevel() >= 70) {
                     htmltext = "32069-01.htm";
                  } else {
                     htmltext = "32069-02.htm";
                  }
                  break;
               case 1:
                  if (st.getInt("cond") == 1) {
                     if (st.getQuestItemsCount(15521) < 500L) {
                        htmltext = "32069-05.htm";
                     } else if (st.getQuestItemsCount(15521) >= 500L) {
                        htmltext = "32069-04.htm";
                     }
                  }
            }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_647_InfluxOfMachines");
      if (st != null && st.getState() == 1) {
         if (Util.contains(MOBS, npc.getId())) {
            int chance = (int)(30.0F * Config.RATE_QUEST_DROP);
            int numItems = chance / 100;
            chance %= 100;
            if (st.getRandom(100) < chance) {
               ++numItems;
            }

            if (numItems > 0) {
               st.playSound("ItemSound.quest_itemget");
               st.giveItems(15521, (long)numItems);
            }
         }

         return null;
      } else {
         return null;
      }
   }

   public static void main(String[] args) {
      new _647_InfluxOfMachines(647, "_647_InfluxOfMachines", "");
   }
}
