package l2e.scripts.quests;

import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.NpcStringId;

public class _283_TheFewTheProudTheBrave extends Quest {
   private static final String qn = "_283_TheFewTheProudTheBrave";
   private static int PERWAN = 32133;
   private static int CRIMSON_SPIDER = 22244;
   private static int CRIMSON_SPIDER_CLAW = 9747;

   public _283_TheFewTheProudTheBrave(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(PERWAN);
      this.addTalkId(PERWAN);
      this.addKillId(CRIMSON_SPIDER);
      this.questItemIds = new int[]{CRIMSON_SPIDER_CLAW};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_283_TheFewTheProudTheBrave");
      if (st == null) {
         return event;
      } else {
         int onlyone = st.getInt("onlyone");
         if (event.equalsIgnoreCase("32133-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("32133-06.htm")) {
            long count = st.getQuestItemsCount(CRIMSON_SPIDER_CLAW);
            if (count > 0L) {
               st.takeItems(CRIMSON_SPIDER_CLAW, -1L);
               st.giveItems(57, 45L * count);
               st.playSound("ItemSound.quest_middle");
               if (onlyone == 0) {
                  showOnScreenMsg(player, NpcStringId.LAST_DUTY_COMPLETE_N_GO_FIND_THE_NEWBIE_GUIDE, 2, 5000, new String[0]);
                  st.set("onlyone", "1");
               }
            }
         } else if (event.equalsIgnoreCase("32133-08.htm")) {
            st.takeItems(CRIMSON_SPIDER_CLAW, -1L);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_283_TheFewTheProudTheBrave");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         int id = st.getState();
         long claw = st.getQuestItemsCount(CRIMSON_SPIDER_CLAW);
         if (id == 0 && npc.getId() == PERWAN) {
            if (player.getLevel() < 15) {
               htmltext = "32133-02.htm";
               st.exitQuest(true);
            } else {
               htmltext = "32133-01.htm";
            }
         } else if (id == 1 && npc.getId() == PERWAN) {
            if (claw > 0L) {
               htmltext = "32133-05.htm";
            } else {
               htmltext = "32133-04.htm";
            }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_283_TheFewTheProudTheBrave");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         int chance = getRandom(100);
         if (npcId == CRIMSON_SPIDER && chance < 35) {
            st.giveItems(CRIMSON_SPIDER_CLAW, 1L);
            st.playSound("ItemSound.quest_itemget");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _283_TheFewTheProudTheBrave(283, "_283_TheFewTheProudTheBrave", "");
   }
}
