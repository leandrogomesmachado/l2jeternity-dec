package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _659_IdRatherBeCollectingFairyBreath extends Quest {
   private static final String qn = "_659_IdRatherBeCollectingFairyBreath";
   private static final int GALATEA = 30634;
   private static final int FAIRY_BREATH = 8286;
   private static final int SOBBING_WIND = 21023;
   private static final int BABBLING_WIND = 21024;
   private static final int GIGGLING_WIND = 21025;

   public _659_IdRatherBeCollectingFairyBreath(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30634);
      this.addTalkId(30634);
      this.addKillId(new int[]{21023, 21024, 21025});
      this.questItemIds = new int[]{8286};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_659_IdRatherBeCollectingFairyBreath");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30634-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30634-06.htm")) {
            int count = (int)st.getQuestItemsCount(8286);
            if (count > 0) {
               st.takeItems(8286, (long)count);
               if (count < 10) {
                  st.rewardItems(57, (long)(count * 50));
               } else {
                  st.rewardItems(57, (long)(count * 50 + 5365));
               }
            }
         } else if (event.equalsIgnoreCase("30634-08.htm")) {
            st.exitQuest(true);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_659_IdRatherBeCollectingFairyBreath");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 26) {
                  htmltext = "30634-02.htm";
               } else {
                  htmltext = "30634-01.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               if (st.getQuestItemsCount(8286) == 0L) {
                  htmltext = "30634-04.htm";
               } else {
                  htmltext = "30634-05.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_659_IdRatherBeCollectingFairyBreath");
      if (st == null) {
         return null;
      } else {
         if (st.isStarted() && st.getRandom(10) < 9) {
            st.giveItems(8286, 1L);
            st.playSound("ItemSound.quest_itemget");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _659_IdRatherBeCollectingFairyBreath(659, "_659_IdRatherBeCollectingFairyBreath", "");
   }
}
