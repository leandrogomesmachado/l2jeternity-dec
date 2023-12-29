package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _263_OrcSubjugation extends Quest {
   private static final String qn = "_263_OrcSubjugation";
   private static final int ORC_AMULET = 1116;
   private static final int ORC_NECKLACE = 1117;

   public _263_OrcSubjugation(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30346);
      this.addTalkId(30346);
      this.addKillId(new int[]{20385, 20386, 20387, 20388});
      this.questItemIds = new int[]{1116, 1117};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_263_OrcSubjugation");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30346-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30346-06.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_263_OrcSubjugation");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getRace().ordinal() == 2) {
                  if (player.getLevel() >= 8 && player.getLevel() <= 16) {
                     htmltext = "30346-02.htm";
                  } else {
                     htmltext = "30346-01.htm";
                     st.exitQuest(true);
                  }
               } else {
                  htmltext = "30346-00.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               int amulet = (int)st.getQuestItemsCount(1116);
               int necklace = (int)st.getQuestItemsCount(1117);
               if (amulet == 0 && necklace == 0) {
                  htmltext = "30346-04.htm";
               } else {
                  htmltext = "30346-05.htm";
                  st.rewardItems(57, (long)(amulet * 20 + necklace * 30));
                  st.takeItems(1116, -1L);
                  st.takeItems(1117, -1L);
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_263_OrcSubjugation");
      if (st == null) {
         return null;
      } else {
         if (st.isStarted() && st.getRandom(10) > 4) {
            int item = 1117;
            if (npc.getId() == 20385) {
               item = 1116;
            }

            st.giveItems(item, 1L);
            st.playSound("ItemSound.quest_itemget");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _263_OrcSubjugation(263, "_263_OrcSubjugation", "");
   }
}
