package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _316_DestroyPlaguebringers extends Quest {
   private static final String qn = "_316_DestroyPlaguebringers";
   private static final int ELLIASIN = 30155;
   private static final int WERERAT_FANG = 1042;
   private static final int NORMAL_FANG_REWARD = 60;
   private static final int VAROOL_FOULCLAWS_FANG = 1043;
   private static final int LEADER_FANG_REWARD = 10000;
   private static final int SUKAR_WERERAT = 20040;
   private static final int SUKAR_WERERAT_LEADER = 20047;
   private static final int VAROOL_FOULCLAW = 27020;

   private _316_DestroyPlaguebringers(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30155);
      this.addTalkId(30155);
      this.addKillId(20040);
      this.addKillId(20047);
      this.addKillId(27020);
      this.questItemIds = new int[]{1042, 1043};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_316_DestroyPlaguebringers");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30155-04.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30155-08.htm")) {
            st.exitQuest(true);
            st.playSound("ItemSound.quest_finish");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_316_DestroyPlaguebringers");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         if (cond == 0) {
            if (player.getRace().ordinal() != 1) {
               st.exitQuest(true);
               htmltext = "30155-00.htm";
            } else if (player.getLevel() < 18) {
               st.exitQuest(true);
               htmltext = "30155-02.htm";
            } else {
               htmltext = "30155-03.htm";
            }
         } else {
            long normal = st.getQuestItemsCount(1042);
            long leader = st.getQuestItemsCount(1043);
            if (normal != 0L || leader != 0L) {
               st.takeItems(1042, normal);
               st.takeItems(1043, leader);
               st.rewardItems(57, normal * 60L + leader * 10000L);
               return "30155-07.htm";
            }

            htmltext = "30155-05.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_316_DestroyPlaguebringers");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1) {
            if (npc.getId() == 27020) {
               if (st.getQuestItemsCount(1043) == 0L) {
                  st.giveItems(1043, 1L);
                  st.playSound("ItemSound.quest_middle");
               }
            } else {
               st.giveItems(1042, 1L);
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _316_DestroyPlaguebringers(316, "_316_DestroyPlaguebringers", "");
   }
}
