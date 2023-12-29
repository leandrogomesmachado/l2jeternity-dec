package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;

public class _169_NightmareChildren extends Quest {
   private static final String qn = "_169_NightmareChildren";
   private static final int CRACKED_SKULL = 1030;
   private static final int PERFECT_SKULL = 1031;
   private static final int BONE_GAITERS = 31;
   private static final int VLASTY = 30145;

   public _169_NightmareChildren(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30145);
      this.addTalkId(30145);
      this.addKillId(new int[]{20105, 20025});
      this.questItemIds = new int[]{1030, 1031};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_169_NightmareChildren");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30145-04.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30145-08.htm")) {
            long reward = 17000L + st.getQuestItemsCount(1030) * 20L;
            st.takeItems(1031, -1L);
            st.takeItems(1030, -1L);
            st.giveItems(31, 1L);
            st.rewardItems(57, reward);
            st.addExpAndSp(17475, 818);
            st.exitQuest(false);
            st.playSound("ItemSound.quest_finish");
            showOnScreenMsg(player, NpcStringId.LAST_DUTY_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE, 2, 5000, new String[0]);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = Quest.getNoQuestMsg(player);
      QuestState st = player.getQuestState("_169_NightmareChildren");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getRace().ordinal() == 2) {
                  htmltext = "30145-00.htm";
               }

               if (player.getLevel() >= 15 && player.getLevel() <= 20) {
                  htmltext = "30145-03.htm";
               } else {
                  htmltext = "30145-02.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               int cond = st.getInt("cond");
               if (cond == 1) {
                  if (st.getQuestItemsCount(1030) >= 1L) {
                     htmltext = "30145-06.htm";
                  } else {
                     htmltext = "30145-05.htm";
                  }
               } else if (cond == 2) {
                  htmltext = "30145-07.htm";
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
      QuestState st = player.getQuestState("_169_NightmareChildren");
      if (st == null) {
         return null;
      } else {
         if (st.isStarted()) {
            int chance = getRandom(10);
            if (st.getInt("cond") == 1 && chance == 0) {
               st.set("cond", "2");
               st.giveItems(1031, 1L);
               st.playSound("ItemSound.quest_middle");
            } else if (chance > 6) {
               st.giveItems(1030, 1L);
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _169_NightmareChildren(169, "_169_NightmareChildren", "");
   }
}
