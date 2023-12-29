package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _157_RecoverSmuggledGoods extends Quest {
   private static final String qn = "_157_RecoverSmuggledGoods";
   private static final int WILFORD = 30005;
   private static final int TOAD = 20121;
   private static final int ADAMANTITE_ORE = 1024;
   private static final int BUCKLER = 20;

   public _157_RecoverSmuggledGoods(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30005);
      this.addTalkId(30005);
      this.addKillId(20121);
      this.questItemIds = new int[]{1024};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_157_RecoverSmuggledGoods");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30005-05.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_157_RecoverSmuggledGoods");
      if (st == null) {
         return htmltext;
      } else {
         int cond = st.getInt("cond");
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 5 && player.getLevel() <= 9) {
                  htmltext = "30005-03.htm";
               } else {
                  htmltext = "30005-02.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (cond == 1 && st.getQuestItemsCount(1024) < 20L) {
                  htmltext = "30005-06.htm";
               } else if (cond == 2 && st.getQuestItemsCount(1024) == 20L) {
                  htmltext = "30005-07.htm";
                  st.takeItems(1024, 20L);
                  st.giveItems(20, 1L);
                  st.unset("cond");
                  st.exitQuest(false);
                  st.playSound("ItemSound.quest_finish");
               }
               break;
            case 2:
               htmltext = getAlreadyCompletedMsg(player);
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_157_RecoverSmuggledGoods");
      if (st == null) {
         return null;
      } else {
         if (st.getInt("cond") == 1 && st.getQuestItemsCount(1024) < 20L) {
            st.giveItems(1024, 1L);
            if (st.getQuestItemsCount(1024) == 20L) {
               st.playSound("ItemSound.quest_middle");
               st.set("cond", "2");
            } else {
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _157_RecoverSmuggledGoods(157, "_157_RecoverSmuggledGoods", "");
   }
}
