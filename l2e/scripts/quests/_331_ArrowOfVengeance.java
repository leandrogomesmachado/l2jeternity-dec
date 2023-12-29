package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _331_ArrowOfVengeance extends Quest {
   private static final String qn = "_331_ArrowOfVengeance";
   private static final int BELTON = 30125;
   private static final int HARPY_FEATHER = 1452;
   private static final int MEDUSA_VENOM = 1453;
   private static final int WYRMS_TOOTH = 1454;

   public _331_ArrowOfVengeance(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30125);
      this.addTalkId(30125);
      this.addKillId(new int[]{20145, 20158, 20176});
      this.questItemIds = new int[]{1452, 1453, 1454};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_331_ArrowOfVengeance");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30125-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30125-06.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = Quest.getNoQuestMsg(player);
      QuestState st = player.getQuestState("_331_ArrowOfVengeance");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 32 && player.getLevel() <= 39) {
                  htmltext = "30125-02.htm";
               } else {
                  htmltext = "30125-01.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               long harpyFeather = st.getQuestItemsCount(1452);
               long medusaVenom = st.getQuestItemsCount(1453);
               long wyrmTooth = st.getQuestItemsCount(1454);
               if (harpyFeather + medusaVenom + wyrmTooth > 0L) {
                  htmltext = "30125-05.htm";
                  st.takeItems(1452, -1L);
                  st.takeItems(1453, -1L);
                  st.takeItems(1454, -1L);
                  long reward = harpyFeather * 78L + medusaVenom * 88L + wyrmTooth * 92L;
                  if (harpyFeather + medusaVenom + wyrmTooth > 10L) {
                     reward += 3100L;
                  }

                  st.rewardItems(57, reward);
               } else {
                  htmltext = "30125-04.htm";
               }
               break;
            case 2:
               htmltext = Quest.getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_331_ArrowOfVengeance");
      if (st == null) {
         return null;
      } else {
         if (st.isStarted() && st.getRandom(10) < 5) {
            switch(npc.getId()) {
               case 20145:
                  st.giveItems(1452, 1L);
                  break;
               case 20158:
                  st.giveItems(1453, 1L);
                  break;
               case 20176:
                  st.giveItems(1454, 1L);
            }

            st.playSound("ItemSound.quest_itemget");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _331_ArrowOfVengeance(331, "_331_ArrowOfVengeance", "");
   }
}
