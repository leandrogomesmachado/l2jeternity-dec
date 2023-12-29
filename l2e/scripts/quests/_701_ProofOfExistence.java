package l2e.scripts.quests;

import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _701_ProofOfExistence extends Quest {
   private static final String qn = "_701_ProofOfExistence";
   private static int ARTIUS = 32559;
   private static int DEADMANS_REMAINS = 13875;
   private static int[] MOBS = new int[]{22606, 22607, 22608, 22609};
   private static int DROP_CHANCE = 80;

   public _701_ProofOfExistence(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(ARTIUS);
      this.addTalkId(ARTIUS);

      for(int i : MOBS) {
         this.addKillId(i);
      }

      this.questItemIds = new int[]{DEADMANS_REMAINS};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_701_ProofOfExistence");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32559-03.htm")) {
            st.setState((byte)1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("32559-quit.htm")) {
            st.exitQuest(true);
            st.playSound("ItemSound.quest_finish");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_701_ProofOfExistence");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               QuestState first = player.getQuestState("_10273_GoodDayToFly");
               if (player.getLevel() >= 78 && first != null && first.isCompleted()) {
                  htmltext = "32559-01.htm";
               } else {
                  htmltext = "32559-00.htm";
               }
               break;
            case 1:
               int itemcount = (int)st.getQuestItemsCount(DEADMANS_REMAINS);
               if (itemcount > 0) {
                  st.takeItems(DEADMANS_REMAINS, -1L);
                  st.rewardItems(57, (long)(itemcount * 2500));
                  st.playSound("ItemSound.quest_itemget");
                  htmltext = "32559-06.htm";
               } else {
                  htmltext = "32559-04.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_701_ProofOfExistence");
      if (st == null) {
         return null;
      } else {
         if (Util.contains(MOBS, npc.getId())) {
            int chance = (int)((float)DROP_CHANCE * Config.RATE_QUEST_DROP);
            int numItems = chance / 100;
            if (st.getRandom(100) < chance) {
               ++numItems;
            }

            if (numItems > 0) {
               st.giveItems(DEADMANS_REMAINS, 1L);
            }

            st.playSound("ItemSound.quest_itemget");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _701_ProofOfExistence(701, "_701_ProofOfExistence", "");
   }
}
