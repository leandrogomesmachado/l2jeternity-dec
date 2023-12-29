package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _264_KeenClaws extends Quest {
   private static final String qn = "_264_KeenClaws";
   private static final int WOLF_CLAW = 1367;
   private static final int PAYNE = 30136;
   private static final int GOBLIN = 20003;
   private static final int WOLF = 20456;
   private static final int LeatherSandals = 36;
   private static final int WoodenHelmet = 43;
   private static final int Stockings = 462;
   private static final int HealingPotion = 1061;
   private static final int ShortGloves = 48;
   private static final int ClothShoes = 35;

   public _264_KeenClaws(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30136);
      this.addTalkId(30136);
      this.addKillId(20003);
      this.addKillId(20456);
      this.questItemIds = new int[]{1367};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_264_KeenClaws");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30136-03.htm")) {
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
      QuestState st = player.getQuestState("_264_KeenClaws");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 3 && player.getLevel() <= 9) {
                  htmltext = "30136-02.htm";
               } else {
                  htmltext = "30136-01.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               int count = (int)st.getQuestItemsCount(1367);
               if (count < 50) {
                  htmltext = "30136-04.htm";
               } else {
                  st.takeItems(1367, -1L);
                  int n = st.getRandom(17);
                  if (n == 0) {
                     st.giveItems(43, 1L);
                  } else if (n < 2) {
                     st.giveItems(57, 1000L);
                  } else if (n < 5) {
                     st.giveItems(36, 1L);
                  } else if (n < 8) {
                     st.giveItems(462, 1L);
                     st.giveItems(57, 50L);
                  } else if (n < 11) {
                     st.giveItems(1061, 1L);
                  } else if (n < 14) {
                     st.giveItems(48, 1L);
                  } else {
                     st.giveItems(35, 1L);
                  }

                  htmltext = "30136-05.htm";
                  st.playSound("ItemSound.quest_finish");
                  st.exitQuest(true);
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_264_KeenClaws");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1 && st.getRandom(10) < 8) {
            int qty = st.getRandom(8) + 1;
            int count = (int)st.getQuestItemsCount(1367);
            if (count + qty > 50) {
               qty = 50 - count;
            }

            st.giveItems(1367, (long)qty);
            if (st.getQuestItemsCount(1367) == 50L) {
               st.set("cond", "2");
               st.playSound("ItemSound.quest_middle");
            } else {
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _264_KeenClaws(264, "_264_KeenClaws", "");
   }
}
