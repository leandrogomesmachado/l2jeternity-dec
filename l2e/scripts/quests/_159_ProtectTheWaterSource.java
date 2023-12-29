package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _159_ProtectTheWaterSource extends Quest {
   private static final String qn = "_159_ProtectTheWaterSource";
   private static final int ASTERIOS = 30154;
   private static final int PLAGUE_ZOMBIE = 27017;
   private static final int HYACINTH_CHARM1 = 1071;
   private static final int HYACINTH_CHARM2 = 1072;
   private static final int PLAGUE_DUST = 1035;

   public _159_ProtectTheWaterSource(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30154);
      this.addTalkId(30154);
      this.addKillId(27017);
      this.questItemIds = new int[]{1035, 1071, 1072};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_159_ProtectTheWaterSource");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30154-04.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(1071, 1L);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_159_ProtectTheWaterSource");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (player.getRace().ordinal() == 1) {
                  if (player.getLevel() >= 12 && player.getLevel() <= 18) {
                     htmltext = "30154-03.htm";
                  } else {
                     htmltext = "30154-02.htm";
                     st.exitQuest(true);
                  }
               } else {
                  htmltext = "30154-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (cond == 1) {
                  htmltext = "30154-05.htm";
               } else if (cond == 2) {
                  st.set("cond", "3");
                  htmltext = "30154-06.htm";
                  st.takeItems(1035, -1L);
                  st.takeItems(1071, -1L);
                  st.giveItems(1072, 1L);
                  st.playSound("ItemSound.quest_middle");
               } else if (cond == 3) {
                  htmltext = "30154-07.htm";
               } else if (cond == 4) {
                  htmltext = "30154-08.htm";
                  st.takeItems(1035, -1L);
                  st.takeItems(1072, -1L);
                  st.rewardItems(57, 18250L);
                  st.playSound("ItemSound.quest_finish");
                  st.unset("cond");
                  st.exitQuest(false);
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
      QuestState st = player.getQuestState("_159_ProtectTheWaterSource");
      if (st == null) {
         return null;
      } else {
         int cond = st.getInt("cond");
         int count = (int)st.getQuestItemsCount(1035);
         if (cond == 1 && st.getRandom(10) < 4) {
            st.set("cond", "2");
            st.playSound("ItemSound.quest_middle");
            st.giveItems(1035, 1L);
         } else if (cond == 3 && st.getRandom(10) < 4 && count < 5) {
            if (count == 4) {
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "4");
            } else {
               st.playSound("ItemSound.quest_itemget");
            }

            st.giveItems(1035, 1L);
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _159_ProtectTheWaterSource(159, "_159_ProtectTheWaterSource", "");
   }
}
