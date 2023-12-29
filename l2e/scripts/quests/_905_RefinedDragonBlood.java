package l2e.scripts.quests;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _905_RefinedDragonBlood extends Quest {
   private static final String qn = "_905_RefinedDragonBlood";
   private static final int[] SEPARATED_SOUL = new int[]{32864, 32865, 32866, 32867, 32868, 32869, 32870, 32891};
   private static final int[] BLUE = new int[]{22852, 22853, 22844, 22845, 22846, 22847};
   private static final int[] RED = new int[]{22848, 22849, 22850, 22851};
   private static final int UNREFINED_RED_DRAGON_BLOOD = 21913;
   private static final int UNREFINED_BLUE_DRAGON_BLOOD = 21914;
   private static final int REFINED_RED_DRAGON_BLOOD = 21903;
   private static final int REFINED_BLUE_DRAGON_BLOOD = 21904;
   private static final int DROP_CHANCE = 20;

   public _905_RefinedDragonBlood(int questId, String name, String descr) {
      super(questId, name, descr);

      for(int npc : SEPARATED_SOUL) {
         this.addStartNpc(npc);
         this.addTalkId(npc);
      }

      for(int first_group : BLUE) {
         this.addKillId(first_group);
      }

      for(int second_group : RED) {
         this.addKillId(second_group);
      }

      this.questItemIds = new int[]{21914, 21913};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_905_RefinedDragonBlood");
      if (st == null) {
         return event;
      } else {
         if (Util.contains(SEPARATED_SOUL, npc.getId())) {
            if (event.equalsIgnoreCase("accept")) {
               st.setState((byte)1);
               st.set("cond", "1");
               st.playSound("ItemSound.quest_accept");
               htmltext = "RefinedDragonBlood-05.htm";
            } else if (event.equalsIgnoreCase("RefinedDragonBlood-12.htm")) {
               st.takeItems(21914, -1L);
               st.takeItems(21913, -1L);
               st.giveItems(21903, 1L);
               st.exitQuest(QuestState.QuestType.DAILY);
               st.playSound("ItemSound.quest_finish");
            } else if (event.equalsIgnoreCase("RefinedDragonBlood-13.htm")) {
               st.takeItems(21914, -1L);
               st.takeItems(21913, -1L);
               st.giveItems(21904, 1L);
               st.exitQuest(QuestState.QuestType.DAILY);
               st.playSound("ItemSound.quest_finish");
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_905_RefinedDragonBlood");
      if (st == null) {
         return htmltext;
      } else {
         if (Util.contains(SEPARATED_SOUL, npc.getId())) {
            switch(st.getState()) {
               case 0:
                  if (player.getLevel() >= 83) {
                     htmltext = "RefinedDragonBlood-01.htm";
                  } else {
                     htmltext = "RefinedDragonBlood-03.htm";
                  }
                  break;
               case 1:
                  if (st.getInt("cond") == 1) {
                     htmltext = "RefinedDragonBlood-06.htm";
                  } else if (st.getInt("cond") == 2) {
                     htmltext = "RefinedDragonBlood-08.htm";
                  }
                  break;
               case 2:
                  if (st.isNowAvailable()) {
                     if (player.getLevel() >= 83) {
                        htmltext = "RefinedDragonBlood-01.htm";
                     } else {
                        htmltext = "RefinedDragonBlood-03.htm";
                     }
                  } else {
                     htmltext = "RefinedDragonBlood-02.htm";
                  }
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_905_RefinedDragonBlood");
      if (st != null && player.isInsideRadius(npc, 2000, false, false)) {
         if (Util.contains(BLUE, npc.getId()) && getRandom(100) < 20 && st.getQuestItemsCount(21914) < 10L) {
            st.giveItems(21914, 1L);
            st.playSound("ItemSound.quest_itemget");
         } else if (Util.contains(RED, npc.getId()) && getRandom(100) < 20 && st.getQuestItemsCount(21913) < 10L) {
            st.giveItems(21913, 1L);
            st.playSound("ItemSound.quest_itemget");
         }
      }

      if (st != null && st.getQuestItemsCount(21914) >= 10L && st.getQuestItemsCount(21913) >= 10L) {
         st.set("cond", "2");
         st.playSound("ItemSound.quest_middle");
      }

      return super.onKill(npc, player, isSummon);
   }

   public static void main(String[] args) {
      new _905_RefinedDragonBlood(905, "_905_RefinedDragonBlood", "");
   }
}
