package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _291_RevengeOfTheRedbonnet extends Quest {
   private static final String qn = "_291_RevengeOfTheRedbonnet";
   private static final int BlackWolfPelt = 1482;
   private static final int ScrollOfEscape = 736;
   private static final int GrandmasPearl = 1502;
   private static final int GrandmasMirror = 1503;
   private static final int GrandmasNecklace = 1504;
   private static final int GrandmasHairpin = 1505;

   public _291_RevengeOfTheRedbonnet(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30553);
      this.addTalkId(30553);
      this.addKillId(20317);
      this.questItemIds = new int[]{1482};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_291_RevengeOfTheRedbonnet");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30553-03.htm")) {
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
      QuestState st = player.getQuestState("_291_RevengeOfTheRedbonnet");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (player.getLevel() < 4 && player.getLevel() > 8) {
                  htmltext = "30553-01.htm";
                  st.exitQuest(true);
               } else {
                  htmltext = "30553-02.htm";
               }
               break;
            case 1:
               if (cond == 1) {
                  htmltext = "30553-04.htm";
               } else if (cond == 2) {
                  if (st.getQuestItemsCount(1482) >= 40L) {
                     st.takeItems(1482, -1L);
                     int random = getRandom(100);
                     if (random < 3) {
                        st.giveItems(1502, 1L);
                     } else if (random < 21) {
                        st.giveItems(1503, 1L);
                     } else if (random < 46) {
                        st.giveItems(1504, 1L);
                     } else {
                        st.giveItems(736, 1L);
                        st.giveItems(1505, 1L);
                     }

                     htmltext = "30553-05.htm";
                     st.playSound("ItemSound.quest_finish");
                     st.exitQuest(true);
                  } else {
                     st.set("cond", "1");
                     htmltext = "30553-04.htm";
                  }
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_291_RevengeOfTheRedbonnet");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1 && st.dropQuestItems(1482, 1, 40L, 1000000, true)) {
            st.set("cond", "2");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _291_RevengeOfTheRedbonnet(291, "_291_RevengeOfTheRedbonnet", "");
   }
}
