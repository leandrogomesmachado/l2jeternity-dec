package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _153_DeliverGoods extends Quest {
   private static final String qn = "_153_DeliverGoods";
   private static final int JacksonId = 30002;
   private static final int SilviaId = 30003;
   private static final int ArnoldId = 30041;
   private static final int RantId = 30054;
   private static final int DeliveryListId = 1012;
   private static final int HeavyWoodBoxId = 1013;
   private static final int ClothBundleId = 1014;
   private static final int ClayPotId = 1015;
   private static final int JacksonsReceipt = 1016;
   private static final int SilviasReceipt = 1017;
   private static final int RantsReceipt = 1018;
   private static final int SoulshotNoGradeId = 1835;
   private static final int RingofKnowledgeId = 875;
   private static final int XpRewardAmount = 600;

   public _153_DeliverGoods(int questId, String name, String descr) {
      super(questId, name, descr);
      this.questItemIds = new int[]{1012, 1013, 1014, 1015, 1016, 1017, 1018};
      this.addStartNpc(30041);
      this.addTalkId(30002);
      this.addTalkId(30003);
      this.addTalkId(30041);
      this.addTalkId(30054);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_153_DeliverGoods");
      if (st != null && npc.getId() == 30041 && event.equalsIgnoreCase("30041-02.htm")) {
         st.setState((byte)1);
         st.set("cond", "1");
         st.playSound("ItemSound.quest_accept");
         st.giveItems(1012, 1L);
         st.giveItems(1013, 1L);
         st.giveItems(1014, 1L);
         st.giveItems(1015, 1L);
      }

      return event;
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_153_DeliverGoods");
      if (st != null) {
         if (npc.getId() == 30041) {
            switch(st.getState()) {
               case 0:
                  if (player.getLevel() >= 2) {
                     htmltext = "30041-01.htm";
                  } else {
                     htmltext = "30041-00.htm";
                  }
                  break;
               case 1:
                  if (st.getInt("cond") == 1) {
                     htmltext = "30041-03.htm";
                  } else if (st.getInt("cond") == 2) {
                     htmltext = "30041-04.htm";
                     st.takeItems(1012, -1L);
                     st.takeItems(1016, -1L);
                     st.takeItems(1017, -1L);
                     st.takeItems(1018, -1L);
                     st.giveItems(875, 1L);
                     st.giveItems(875, 1L);
                     st.addExpAndSp(600, 0);
                     st.exitQuest(false);
                  }
                  break;
               case 2:
                  htmltext = getAlreadyCompletedMsg(player);
            }
         } else {
            if (npc.getId() == 30002) {
               if (st.getQuestItemsCount(1013) > 0L) {
                  htmltext = "30002-01.htm";
                  st.takeItems(1013, -1L);
                  st.giveItems(1016, 1L);
               } else {
                  htmltext = "30002-02.htm";
               }
            } else if (npc.getId() == 30003) {
               if (st.getQuestItemsCount(1014) > 0L) {
                  htmltext = "30003-01.htm";
                  st.takeItems(1014, -1L);
                  st.giveItems(1017, 1L);
                  st.giveItems(1835, 3L);
               } else {
                  htmltext = "30003-02.htm";
               }
            } else if (npc.getId() == 30054) {
               if (st.getQuestItemsCount(1015) > 0L) {
                  htmltext = "30054-01.htm";
                  st.takeItems(1015, -1L);
                  st.giveItems(1018, 1L);
               } else {
                  htmltext = "30054-02.htm";
               }
            }

            if (st.getInt("cond") == 1 && st.getQuestItemsCount(1016) > 0L && st.getQuestItemsCount(1017) > 0L && st.getQuestItemsCount(1018) > 0L) {
               st.set("cond", "2");
               st.playSound("ItemSound.quest_middle");
            }
         }
      }

      return htmltext;
   }

   public static void main(String[] args) {
      new _153_DeliverGoods(153, "_153_DeliverGoods", "");
   }
}
