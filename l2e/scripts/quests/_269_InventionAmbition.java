package l2e.scripts.quests;

import l2e.commons.util.Rnd;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _269_InventionAmbition extends Quest {
   private static final String qn = "_269_InventionAmbition";
   public final int INVENTOR_MARU = 32486;
   public final int[] MOBS = new int[]{21124, 21125, 21126, 21127, 21128, 21129, 21130, 21131};
   public final int ENERGY_ORES = 10866;

   public _269_InventionAmbition(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32486);
      this.addTalkId(32486);

      for(int mob : this.MOBS) {
         this.addKillId(mob);
      }

      this.questItemIds = new int[]{10866};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_269_InventionAmbition");
      if (st == null) {
         return null;
      } else {
         if (event.equals("32486-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equals("32486-05.htm")) {
            st.exitQuest(true);
            st.playSound("ItemSound.quest_finish");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = Quest.getNoQuestMsg(player);
      QuestState st = player.getQuestState("_269_InventionAmbition");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() < 18) {
                  htmltext = "32486-00.htm";
                  st.exitQuest(true);
               } else {
                  htmltext = "32486-01.htm";
               }
               break;
            case 1:
               long count = st.getQuestItemsCount(10866);
               if (count > 0L) {
                  st.giveItems(57, count * 50L + 2044L * (count / 20L));
                  st.takeItems(10866, -1L);
                  htmltext = "32486-07.htm";
               } else {
                  htmltext = "32486-04.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_269_InventionAmbition");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1 && Rnd.chance(60)) {
            st.giveItems(10866, 1L);
            st.playSound("ItemSound.quest_itemget");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _269_InventionAmbition(269, "_269_InventionAmbition", "");
   }
}
