package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _370_AnElderSowsSeeds extends Quest {
   private static final String qn = "_370_AnElderSowsSeeds";
   private static final int CASIAN = 30612;
   private static final int SPELLBOOK_PAGE = 5916;
   private static final int CHAPTER_OF_FIRE = 5917;
   private static final int CHAPTER_OF_WATER = 5918;
   private static final int CHAPTER_OF_WIND = 5919;
   private static final int CHAPTER_OF_EARTH = 5920;

   public _370_AnElderSowsSeeds(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30612);
      this.addTalkId(30612);
      this.addKillId(new int[]{20082, 20084, 20086, 20089, 20090});
      this.questItemIds = new int[]{5916, 5917, 5918, 5919, 5920};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_370_AnElderSowsSeeds");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30612-3.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30612-6.htm")) {
            if (st.getQuestItemsCount(5917) > 0L && st.getQuestItemsCount(5918) > 0L && st.getQuestItemsCount(5919) > 0L && st.getQuestItemsCount(5920) > 0L) {
               htmltext = "30612-8.htm";
               st.takeItems(5917, 1L);
               st.takeItems(5918, 1L);
               st.takeItems(5919, 1L);
               st.takeItems(5920, 1L);
               st.rewardItems(57, 3600L);
            }
         } else if (event.equalsIgnoreCase("30612-9.htm")) {
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = Quest.getNoQuestMsg(player);
      QuestState st = player.getQuestState("_370_AnElderSowsSeeds");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 28 && player.getLevel() <= 42) {
                  htmltext = "30612-0.htm";
               } else {
                  htmltext = "30612-0a.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               htmltext = "30612-4.htm";
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      Player partyMember = this.getRandomPartyMemberState(player, (byte)1);
      if (partyMember == null) {
         return null;
      } else {
         QuestState st = partyMember.getQuestState("_370_AnElderSowsSeeds");
         if (st.isStarted()) {
            st.giveItems(5916, 1L);
            st.playSound("ItemSound.quest_itemget");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _370_AnElderSowsSeeds(370, "_370_AnElderSowsSeeds", "");
   }
}
