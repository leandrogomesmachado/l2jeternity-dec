package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _357_WarehouseKeepersAmbition extends Quest {
   private static final String qn = "_357_WarehouseKeepersAmbition";
   private static final int SILVA = 30686;
   private static final int JADE_CRYSTAL = 5867;
   private static final int REWARD1 = 900;
   private static final int REWARD2 = 10000;

   public _357_WarehouseKeepersAmbition(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30686);
      this.addTalkId(30686);
      this.addKillId(new int[]{20594, 20595, 20596, 20597});
      this.questItemIds = new int[]{5867};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_357_WarehouseKeepersAmbition");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30686-2.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30686-7.htm")) {
            long count = st.getQuestItemsCount(5867);
            if (count >= 1L) {
               long reward;
               if (count >= 100L) {
                  reward = count * 900L + 10000L;
               } else {
                  reward = count * 900L;
               }

               st.takeItems(5867, -1L);
               st.rewardItems(57, reward);
            } else {
               htmltext = "30686-4.htm";
            }
         } else if (event.equalsIgnoreCase("30686-8.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_357_WarehouseKeepersAmbition");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 47 && player.getLevel() <= 57) {
                  htmltext = "30686-0.htm";
               } else {
                  htmltext = "30686-0a.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (st.getQuestItemsCount(5867) == 0L) {
                  htmltext = "30686-4.htm";
               } else if (st.getQuestItemsCount(5867) >= 1L) {
                  htmltext = "30686-6.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMemberState(player, (byte)1);
      if (partyMember == null) {
         return null;
      } else {
         QuestState st = partyMember.getQuestState("_357_WarehouseKeepersAmbition");
         if (st.getRandom(100) < 50) {
            st.giveItems(5867, 1L);
            st.playSound("ItemSound.quest_itemget");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _357_WarehouseKeepersAmbition(357, "_357_WarehouseKeepersAmbition", "");
   }
}
