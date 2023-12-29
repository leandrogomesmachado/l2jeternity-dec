package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.Race;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _320_BonesTellTheFuture extends Quest {
   private static final String qn = "_320_BonesTellTheFuture";
   private final int BONE_FRAGMENT = 809;

   public _320_BonesTellTheFuture(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30359);
      this.addTalkId(30359);
      this.addKillId(new int[]{20517, 20518, 20022, 20455});
      this.questItemIds = new int[]{809};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_320_BonesTellTheFuture");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30359-04.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_320_BonesTellTheFuture");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getRace() != Race.DarkElf) {
                  htmltext = "30359-00.htm";
                  st.exitQuest(true);
               } else if (player.getLevel() >= 10 && player.getLevel() <= 18) {
                  htmltext = "30359-03.htm";
               } else {
                  htmltext = "30359-02.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (st.getQuestItemsCount(809) < 10L) {
                  htmltext = "30359-05.htm";
               } else {
                  htmltext = "30359-06.htm";
                  st.takeItems(809, -1L);
                  st.rewardItems(57, 8470L);
                  st.playSound("ItemSound.quest_finish");
                  st.exitQuest(true);
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_320_BonesTellTheFuture");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1 && st.dropQuestItems(809, 1, 10L, 200000, true)) {
            st.set("cond", "2");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _320_BonesTellTheFuture(320, "_320_BonesTellTheFuture", "");
   }
}
