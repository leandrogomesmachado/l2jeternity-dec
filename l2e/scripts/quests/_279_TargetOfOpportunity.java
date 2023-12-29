package l2e.scripts.quests;

import java.util.Arrays;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _279_TargetOfOpportunity extends Quest {
   private static final String qn = "_279_TargetOfOpportunity";
   private static final int JERIAN = 32302;
   private static final int[] MONSTERS = new int[]{22373, 22374, 22375, 22376};
   private static final int[] SEAL_COMPONENTS = new int[]{15517, 15518, 15519, 15520};
   private static final int[] SEAL_BREAKERS = new int[]{15515, 15516};

   public _279_TargetOfOpportunity(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32302);
      this.addTalkId(32302);

      for(int monster : MONSTERS) {
         this.addKillId(monster);
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_279_TargetOfOpportunity");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("32302-05.htm")) {
            st.setState((byte)1);
            st.set("cond", "1");
            st.set("progress", "1");
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("32302-08.htm")
            && st.getInt("progress") == 1
            && st.getQuestItemsCount(SEAL_COMPONENTS[0]) > 0L
            && st.getQuestItemsCount(SEAL_COMPONENTS[1]) > 0L
            && st.getQuestItemsCount(SEAL_COMPONENTS[2]) > 0L
            && st.getQuestItemsCount(SEAL_COMPONENTS[0]) > 0L) {
            st.takeItems(SEAL_COMPONENTS[0], -1L);
            st.takeItems(SEAL_COMPONENTS[1], -1L);
            st.takeItems(SEAL_COMPONENTS[2], -1L);
            st.takeItems(SEAL_COMPONENTS[3], -1L);
            st.giveItems(SEAL_BREAKERS[0], 1L);
            st.giveItems(SEAL_BREAKERS[1], 1L);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return event;
      }
   }

   @Override
   public final String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_279_TargetOfOpportunity");
      if (st == null) {
         return htmltext;
      } else {
         if (st.getState() == 0) {
            if (player.getLevel() >= 82) {
               htmltext = "32302-01.htm";
            } else {
               htmltext = "32302-02.htm";
            }
         } else if (st.getState() == 1 && st.getInt("progress") == 1) {
            if (st.getQuestItemsCount(SEAL_COMPONENTS[0]) > 0L
               && st.getQuestItemsCount(SEAL_COMPONENTS[1]) > 0L
               && st.getQuestItemsCount(SEAL_COMPONENTS[2]) > 0L
               && st.getQuestItemsCount(SEAL_COMPONENTS[0]) > 0L) {
               htmltext = "32302-07.htm";
            } else {
               htmltext = "32302-06.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      Player pl = this.getRandomPartyMember(player, "progress", "1");
      int idx = Arrays.binarySearch(MONSTERS, npc.getId());
      if (pl != null && idx >= 0) {
         QuestState st = pl.getQuestState("_279_TargetOfOpportunity");
         if (getRandom(1000) < (int)(311.0F * Config.RATE_QUEST_DROP) && st.getQuestItemsCount(SEAL_COMPONENTS[idx]) < 1L) {
            st.giveItems(SEAL_COMPONENTS[idx], 1L);
            if (haveAllExceptThis(st, idx)) {
               st.set("cond", "2");
               st.playSound("ItemSound.quest_middle");
            } else {
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static final boolean haveAllExceptThis(QuestState st, int idx) {
      for(int i = 0; i < SEAL_COMPONENTS.length; ++i) {
         if (i != idx && st.getQuestItemsCount(SEAL_COMPONENTS[i]) < 1L) {
            return false;
         }
      }

      return true;
   }

   public static void main(String[] args) {
      new _279_TargetOfOpportunity(279, "_279_TargetOfOpportunity", "");
   }
}
