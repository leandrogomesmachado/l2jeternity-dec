package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _164_BloodFiend extends Quest {
   private static final String qn = "_164_BloodFiend";
   private static final int CREAMEES = 30149;
   private static final int KIRUNAK_SKULL = 1044;
   private static final int KIRUNAK = 27021;

   private _164_BloodFiend(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30149);
      this.addTalkId(30149);
      this.addKillId(27021);
      this.questItemIds = new int[]{1044};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_164_BloodFiend");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("1")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            htmltext = "30149-04.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_164_BloodFiend");
      if (st == null) {
         return htmltext;
      } else {
         if (st.isCompleted()) {
            htmltext = getAlreadyCompletedMsg(player);
         }

         int cond = st.getInt("cond");
         if (cond == 0) {
            if (player.getRace().ordinal() == 2) {
               st.exitQuest(true);
               htmltext = "30149-00.htm";
            } else if (player.getLevel() < 21) {
               st.exitQuest(true);
               htmltext = "30149-02.htm";
            } else {
               htmltext = "30149-03.htm";
            }
         } else if (st.getQuestItemsCount(1044) != 0L) {
            st.exitQuest(false);
            st.rewardItems(57, 42130L);
            st.addExpAndSp(35637, 1854);
            st.playSound("ItemSound.quest_finish");
            htmltext = "30149-06.htm";
         } else {
            htmltext = "30149-05.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_164_BloodFiend");
      if (st == null) {
         return null;
      } else {
         if (npc.getId() == 27021 && st.getInt("cond") == 1 && st.getQuestItemsCount(1044) == 0L) {
            st.giveItems(1044, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "2");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _164_BloodFiend(164, "_164_BloodFiend", "");
   }
}
