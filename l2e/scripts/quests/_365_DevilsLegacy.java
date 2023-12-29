package l2e.scripts.quests;

import l2e.commons.util.Rnd;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _365_DevilsLegacy extends Quest {
   private static final String qn = "_365_DevilsLegacy";
   private static final int RANDOLF = 30095;
   private static final int[] MOBS = new int[]{20836, 29027, 20845, 21629, 21630, 29026};
   private static final int TREASURE_CHEST = 5873;

   public _365_DevilsLegacy(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30095);
      this.addTalkId(30095);

      for(int mob : MOBS) {
         this.addKillId(mob);
      }

      this.questItemIds = new int[]{5873};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_365_DevilsLegacy");
      if (st == null) {
         return null;
      } else {
         if (event.equalsIgnoreCase("30095-01.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30095-05.htm")) {
            long count = st.getQuestItemsCount(5873);
            if (count > 0L) {
               long reward = count * 5070L;
               st.takeItems(5873, -1L);
               st.giveItems(57, reward);
            } else {
               htmltext = "30095-07.htm";
            }
         } else if (event.equalsIgnoreCase("30095-6.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = Quest.getNoQuestMsg(player);
      QuestState st = player.getQuestState("_365_DevilsLegacy");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getCond();
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 39) {
                  htmltext = "30095-00.htm";
               } else {
                  htmltext = "30095-00a.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (cond == 1) {
                  if (st.getQuestItemsCount(5873) == 0L) {
                     htmltext = "30095-02.htm";
                  } else {
                     htmltext = "30095-04.htm";
                  }
               }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_365_DevilsLegacy");
      if (st != null && st.isStarted()) {
         if (Rnd.chance(25)) {
            st.giveItems(5873, 1L);
            st.playSound("ItemSound.quest_itemget");
         }

         return null;
      } else {
         return null;
      }
   }

   public static void main(String[] args) {
      new _365_DevilsLegacy(365, "_365_DevilsLegacy", "");
   }
}
