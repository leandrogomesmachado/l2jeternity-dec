package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _277_GatekeepersOffering extends Quest {
   private static final String qn = "_277_GatekeepersOffering";
   private static final int TAMIL = 30576;
   private static final int STARSTONE = 1572;
   private static final int GATEKEEPER_CHARM = 1658;
   private static final int GRAYSTONE_GOLEM = 20333;

   public _277_GatekeepersOffering(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30576);
      this.addTalkId(30576);
      this.addKillId(20333);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_277_GatekeepersOffering");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30576-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_277_GatekeepersOffering");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 15 && player.getLevel() <= 21) {
                  htmltext = "30576-02.htm";
               } else {
                  htmltext = "30576-01.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               int cond = st.getInt("cond");
               if (cond == 1 && st.getQuestItemsCount(1572) < 20L) {
                  htmltext = "30576-04.htm";
               } else if (cond == 2 && st.getQuestItemsCount(1572) >= 20L) {
                  htmltext = "30576-05.htm";
                  st.takeItems(1572, -1L);
                  st.rewardItems(1658, 2L);
                  st.exitQuest(true);
                  st.playSound("ItemSound.quest_finish");
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_277_GatekeepersOffering");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1 && st.getRandom(100) < 20) {
            st.giveItems(1572, 1L);
            if (st.getQuestItemsCount(1572) == 20L) {
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "2");
            } else {
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _277_GatekeepersOffering(277, "_277_GatekeepersOffering", "");
   }
}
