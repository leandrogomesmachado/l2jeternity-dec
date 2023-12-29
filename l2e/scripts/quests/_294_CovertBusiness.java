package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _294_CovertBusiness extends Quest {
   private static final String qn = "_294_CovertBusiness";
   private static final int BatFang = 1491;
   private static final int RingOfRaccoon = 1508;
   private static final int Barded = 20370;
   private static final int Blade = 20480;
   private static final int Keef = 30534;

   public _294_CovertBusiness(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30534);
      this.addTalkId(30534);
      this.addKillId(new int[]{20370, 20480});
      this.questItemIds = new int[]{1491};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_294_CovertBusiness");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30534-03.htm")) {
            st.setState((byte)1);
            st.set("cond", "1");
            st.playSound("ItemSound.quest_accept");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_294_CovertBusiness");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (player.getRace().ordinal() == 4) {
                  if (player.getLevel() >= 10 && player.getLevel() <= 16) {
                     htmltext = "30534-02.htm";
                  } else {
                     htmltext = "30534-01.htm";
                     st.exitQuest(true);
                  }
               } else {
                  htmltext = "30534-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (cond == 1) {
                  htmltext = "30534-04.htm";
               } else if (cond == 2) {
                  htmltext = "30534-05.htm";
                  st.takeItems(1491, -1L);
                  st.giveItems(1508, 1L);
                  st.giveItems(57, 2400L);
                  st.addExpAndSp(0, 600);
                  st.exitQuest(true);
                  st.playSound("ItemSound.quest_finish");
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_294_CovertBusiness");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1) {
            int qty = 0;
            int count = (int)st.getQuestItemsCount(1491);
            qty = 1 + st.getRandom(4);
            if (count + qty >= 100) {
               qty = 100 - count;
               st.set("cond", "2");
               st.playSound("ItemSound.quest_middle");
            } else {
               st.playSound("ItemSound.quest_itemget");
            }

            st.giveItems(1491, (long)qty);
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _294_CovertBusiness(294, "_294_CovertBusiness", "");
   }
}
