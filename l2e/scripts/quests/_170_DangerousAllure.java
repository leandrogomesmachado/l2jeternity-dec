package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _170_DangerousAllure extends Quest {
   private static final String qn = "_170_DangerousAllure";
   private static final int VELLIOR = 30305;
   private static final int NIGHTMARE_CRYSTAL = 1046;
   private static final int MERKENIS = 27022;

   private _170_DangerousAllure(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30305);
      this.addTalkId(30305);
      this.addKillId(27022);
      this.questItemIds = new int[]{1046};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_170_DangerousAllure");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("1")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            htmltext = "30305-04.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_170_DangerousAllure");
      if (st == null) {
         return htmltext;
      } else {
         if (st.isCompleted()) {
            htmltext = getAlreadyCompletedMsg(player);
         }

         int cond = st.getInt("cond");
         if (cond == 0) {
            if (player.getRace().ordinal() != 2) {
               st.exitQuest(true);
               htmltext = "30305-00.htm";
            } else if (player.getLevel() < 21) {
               st.exitQuest(true);
               htmltext = "30305-02.htm";
            } else {
               htmltext = "30305-03.htm";
            }
         } else if (st.getQuestItemsCount(1046) != 0L) {
            st.exitQuest(false);
            st.rewardItems(57, 102680L);
            st.addExpAndSp(38607, 4018);
            st.playSound("ItemSound.quest_finish");
            htmltext = "30305-06.htm";
         } else {
            htmltext = "30305-05.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_170_DangerousAllure");
      if (st == null) {
         return null;
      } else {
         if (npc.getId() == 27022 && st.getInt("cond") == 1 && st.getQuestItemsCount(1046) == 0L) {
            st.giveItems(1046, 1L);
            st.playSound("ItemSound.quest_middle");
            st.set("cond", "2");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _170_DangerousAllure(170, "_170_DangerousAllure", "");
   }
}
