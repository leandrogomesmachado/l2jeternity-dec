package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _380_BringOutTheFlavorOfIngredients extends Quest {
   private static final String qn = "_380_BringOutTheFlavorOfIngredients";
   private static final int DIRE_WOLF = 20205;
   private static final int KADIF_WEREWOLF = 20206;
   private static final int GIANT_MIST_LEECH = 20225;
   private static final int RITRONS_FRUIT = 5895;
   private static final int MOON_FACE_FLOWER = 5896;
   private static final int LEECH_FLUIDS = 5897;
   private static final int ANTIDOTE = 1831;
   private static final int RITRON_JELLY = 5960;
   private static final int JELLY_RECIPE = 5959;
   private static final int REC_CHANCE = 55;

   public _380_BringOutTheFlavorOfIngredients(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30069);
      this.addTalkId(30069);
      this.addKillId(new int[]{20205, 20206, 20225});
      this.questItemIds = new int[]{5895, 5896, 5897};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_380_BringOutTheFlavorOfIngredients");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30069-04.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30069-12.htm")) {
            st.giveItems(5959, 1L);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_380_BringOutTheFlavorOfIngredients");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 24) {
                  htmltext = "30069-01.htm";
               } else {
                  htmltext = "30069-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               int cond = st.getInt("cond");
               if (cond == 1) {
                  htmltext = "30069-06.htm";
               } else if (cond == 2) {
                  if (st.getQuestItemsCount(1831) >= 2L) {
                     st.takeItems(5895, -1L);
                     st.takeItems(5896, -1L);
                     st.takeItems(5897, -1L);
                     st.takeItems(1831, 2L);
                     st.set("cond", "3");
                     st.playSound("ItemSound.quest_middle");
                     htmltext = "30069-07.htm";
                  } else {
                     htmltext = "30069-06.htm";
                  }
               } else if (cond == 3) {
                  st.set("cond", "4");
                  st.playSound("ItemSound.quest_middle");
                  htmltext = "30069-08.htm";
               } else if (cond == 4) {
                  st.set("cond", "5");
                  st.playSound("ItemSound.quest_middle");
                  htmltext = "30069-09.htm";
               } else if (cond == 5) {
                  st.set("cond", "6");
                  st.playSound("ItemSound.quest_middle");
                  htmltext = "30069-10.htm";
               } else if (cond == 6) {
                  st.giveItems(5960, 1L);
                  if (getRandom(100) < 55) {
                     htmltext = "30069-11.htm";
                  } else {
                     htmltext = "30069-13.htm";
                     st.playSound("ItemSound.quest_finish");
                     st.exitQuest(true);
                  }
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = this.checkPlayerCondition(player, npc, "cond", "1");
      if (st == null) {
         return null;
      } else {
         switch(npc.getId()) {
            case 20205:
               if (st.dropItems(5895, 1, 4L, 100000) && st.getQuestItemsCount(5896) == 20L && st.getQuestItemsCount(5897) == 10L) {
                  st.set("cond", "2");
               }
               break;
            case 20206:
               if (st.dropItems(5896, 1, 20L, 250000) && st.getQuestItemsCount(5895) == 4L && st.getQuestItemsCount(5897) == 10L) {
                  st.set("cond", "2");
               }
               break;
            case 20225:
               if (st.dropItems(5897, 1, 10L, 250000) && st.getQuestItemsCount(5895) == 4L && st.getQuestItemsCount(5896) == 20L) {
                  st.set("cond", "2");
               }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _380_BringOutTheFlavorOfIngredients(380, "_380_BringOutTheFlavorOfIngredients", "");
   }
}
