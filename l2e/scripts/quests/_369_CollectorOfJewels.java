package l2e.scripts.quests;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public class _369_CollectorOfJewels extends Quest {
   private static final String qn = "_369_CollectorOfJewels";
   private static final int NELL = 30376;
   private static final int FLARE_SHARD = 5882;
   private static final int FREEZING_SHARD = 5883;
   private static Map<Integer, Integer> DROPLIST_FREEZE = new HashMap<>();
   private static Map<Integer, Integer> DROPLIST_FLARE = new HashMap<>();

   public _369_CollectorOfJewels(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(30376);
      this.addTalkId(30376);
      DROPLIST_FREEZE.put(20747, 85);
      DROPLIST_FREEZE.put(20619, 73);
      DROPLIST_FREEZE.put(20616, 60);
      DROPLIST_FLARE.put(20612, 77);
      DROPLIST_FLARE.put(20609, 77);
      DROPLIST_FLARE.put(20749, 85);

      for(int mob : DROPLIST_FREEZE.keySet()) {
         this.addKillId(mob);
      }

      for(int mob : DROPLIST_FLARE.keySet()) {
         this.addKillId(mob);
      }

      this.questItemIds = new int[]{5882, 5883};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_369_CollectorOfJewels");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30376-03.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
            st.set("awaitsFreezing", "1");
            st.set("awaitsFlare", "1");
         } else if (event.equalsIgnoreCase("30376-07.htm")) {
            st.playSound("ItemSound.quest_itemget");
         } else if (event.equalsIgnoreCase("30376-08.htm")) {
            st.exitQuest(true);
            st.playSound("ItemSound.quest_finish");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      QuestState st = player.getQuestState("_369_CollectorOfJewels");
      String htmltext = getNoQuestMsg(player);
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 25 && player.getLevel() <= 37) {
                  htmltext = "30376-02.htm";
               } else {
                  htmltext = "30376-01.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               int cond = st.getInt("cond");
               long flare = st.getQuestItemsCount(5882);
               long freezing = st.getQuestItemsCount(5883);
               if (cond == 1) {
                  htmltext = "30376-04.htm";
               } else if (cond == 2 && flare >= 50L && freezing >= 50L) {
                  htmltext = "30376-05.htm";
                  st.set("cond", "3");
                  st.rewardItems(57, 12500L);
                  st.takeItems(5882, -1L);
                  st.takeItems(5883, -1L);
                  st.set("awaitsFreezing", "1");
                  st.set("awaitsFlare", "1");
                  st.playSound("ItemSound.quest_middle");
               } else if (cond == 3) {
                  htmltext = "30376-09.htm";
               } else if (cond == 4 && flare >= 200L && freezing >= 200L) {
                  htmltext = "30376-10.htm";
                  st.playSound("ItemSound.quest_finish");
                  st.rewardItems(57, 63500L);
                  st.takeItems(5882, -1L);
                  st.takeItems(5883, -1L);
                  st.exitQuest(true);
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      int npcId = npc.getId();
      Player partymember = null;
      int item = 0;
      int chance = 0;
      if (DROPLIST_FREEZE.containsKey(npcId)) {
         partymember = this.getRandomPartyMember(player, "awaitsFreezing", "1");
         if (partymember == null) {
            return null;
         }

         item = 5883;
         chance = DROPLIST_FREEZE.get(npcId);
      } else if (DROPLIST_FLARE.containsKey(npcId)) {
         partymember = this.getRandomPartyMember(player, "awaitsFlare", "1");
         if (partymember == null) {
            return null;
         }

         item = 5882;
         chance = DROPLIST_FLARE.get(npcId);
      }

      QuestState st = partymember.getQuestState("_369_CollectorOfJewels");
      int cond = st.getInt("cond");
      if (cond >= 1 && cond <= 3) {
         int max = 0;
         if (cond == 1) {
            max = 50;
         } else if (cond == 3) {
            max = 200;
         }

         if (st.getRandom(100) < chance && st.getQuestItemsCount(item) <= (long)max) {
            st.giveItems(item, 1L);
            if (st.getQuestItemsCount(5883) == (long)max) {
               st.unset("awaitsFreezing");
            } else if (st.getQuestItemsCount(5882) == (long)max) {
               st.unset("awaitsFlare");
            }

            if (st.getQuestItemsCount(5882) == (long)max && st.getQuestItemsCount(5883) == (long)max) {
               st.set("cond", String.valueOf(cond + 1));
               st.playSound("ItemSound.quest_middle");
            } else {
               st.playSound("ItemSound.quest_itemget");
            }
         }
      }

      return null;
   }

   public static void main(String[] args) {
      new _369_CollectorOfJewels(369, "_369_CollectorOfJewels", "");
   }
}
