package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _262_TradeWithTheIvoryTower extends Quest {
   private static final String qn = "_262_TradeWithTheIvoryTower";
   private static final int Vollodos = 30137;
   private static final int FUNGUS_SAC = 707;

   public _262_TradeWithTheIvoryTower(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30137);
      this.addTalkId(30137);
      this.addKillId(new int[]{20400, 20007});
      this.questItemIds = new int[]{707};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_262_TradeWithTheIvoryTower");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30137-03.htm")) {
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
      QuestState st = player.getQuestState("_262_TradeWithTheIvoryTower");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 8 && player.getLevel() <= 16) {
                  htmltext = "30137-02.htm";
               } else {
                  htmltext = "30137-01.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (st.getQuestItemsCount(707) < 10L) {
                  htmltext = "30137-04.htm";
               } else {
                  htmltext = "30137-05.htm";
                  st.takeItems(707, -1L);
                  st.rewardItems(57, 3000L);
                  st.playSound("ItemSound.quest_finish");
                  st.exitQuest(true);
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_262_TradeWithTheIvoryTower");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1) {
            int chance = npc.getId() == 20400 ? 4 : 3;
            if (st.getRandom(10) < chance) {
               st.giveItems(707, 1L);
               if (st.getQuestItemsCount(707) < 10L) {
                  st.playSound("ItemSound.quest_itemget");
               } else {
                  st.set("cond", "2");
                  st.playSound("ItemSound.quest_middle");
               }
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _262_TradeWithTheIvoryTower(262, "_262_TradeWithTheIvoryTower", "");
   }
}
