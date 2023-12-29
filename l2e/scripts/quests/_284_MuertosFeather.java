package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _284_MuertosFeather extends Quest {
   private static final String qn = "_284_MuertosFeather";
   private static final int TREVOR = 32166;
   private static final int[] MOBS = new int[]{22239, 22240, 22242, 22243, 22245, 22246};
   private static final int FEATHER = 9748;

   public _284_MuertosFeather(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32166);
      this.addTalkId(32166);

      for(int mob : MOBS) {
         this.addKillId(mob);
      }

      this.questItemIds = new int[]{9748};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_284_MuertosFeather");
      if (st == null) {
         return null;
      } else {
         if (event.equalsIgnoreCase("32166-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("32166-06.htm")) {
            long counts = st.getQuestItemsCount(9748) * 45L;
            st.takeItems(9748, -1L);
            st.giveItems(57, counts);
         } else if (event.equalsIgnoreCase("32166-08.htm")) {
            st.takeItems(9748, -1L);
            st.exitQuest(true);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = Quest.getNoQuestMsg(player);
      QuestState st = player.getQuestState("_284_MuertosFeather");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() < 11) {
                  htmltext = "32166-02.htm";
                  st.exitQuest(true);
               } else {
                  htmltext = "32166-01.htm";
               }
               break;
            case 1:
               if (st.getQuestItemsCount(9748) == 0L) {
                  htmltext = "32166-04.htm";
               } else {
                  htmltext = "32166-05.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_284_MuertosFeather");
      if (st == null) {
         return null;
      } else {
         int chance = st.getRandom(100);
         if (st.getInt("cond") == 1 && chance < 70) {
            st.giveItems(9748, 1L);
            st.playSound("ItemSound.quest_itemget");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _284_MuertosFeather(284, "_284_MuertosFeather", "");
   }
}
