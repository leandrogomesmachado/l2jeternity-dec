package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _266_PleasOfPixies extends Quest {
   private static final String qn = "_266_PleasOfPixies";
   private static final int PREDATORS_FANG = 1334;
   private static final int GLASS_SHARD = 1336;
   private static final int EMERALD = 1337;
   private static final int BLUE_ONYX = 1338;
   private static final int ONYX = 1339;

   public _266_PleasOfPixies(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31852);
      this.addTalkId(31852);
      this.addKillId(new int[]{20525, 20530, 20534, 20537});
      this.questItemIds = new int[]{1334};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_266_PleasOfPixies");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("31852-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_266_PleasOfPixies");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getRace() != Race.Elf) {
                  htmltext = "31852-00.htm";
                  st.exitQuest(true);
               } else if (player.getLevel() >= 3) {
                  htmltext = "31852-02.htm";
               } else {
                  htmltext = "31852-01.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (st.getQuestItemsCount(1334) < 100L) {
                  htmltext = "31852-04.htm";
               } else {
                  htmltext = "31852-05.htm";
                  st.takeItems(1334, -1L);
                  int n = getRandom(100);
                  if (n < 10) {
                     st.rewardItems(1337, 1L);
                     st.playSound("ItemSound.quest_jackpot");
                  } else if (n < 30) {
                     st.rewardItems(1338, 1L);
                  } else if (n < 60) {
                     st.rewardItems(1339, 1L);
                  } else {
                     st.rewardItems(1336, 1L);
                  }

                  st.playSound("ItemSound.quest_finish");
                  st.exitQuest(true);
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_266_PleasOfPixies");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1 && st.dropQuestItems(1334, getRandom(1, 3), 100L, 900000, true)) {
            st.set("cond", "2");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _266_PleasOfPixies(266, "_266_PleasOfPixies", "");
   }
}
