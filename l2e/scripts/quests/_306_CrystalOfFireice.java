package l2e.scripts.quests;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;

public final class _306_CrystalOfFireice extends Quest {
   private static final String qn = "_306_CrystalOfFireice";
   private static int FLAME_SHARD = 1020;
   private static int ICE_SHARD = 1021;
   private static int ADENA = 57;
   Map<Integer, int[]> droplist = new HashMap<>();

   public _306_CrystalOfFireice(int questId, String name, String descr) {
      super(questId, name, descr);
      this.droplist.put(20109, new int[]{30, FLAME_SHARD});
      this.droplist.put(20110, new int[]{30, ICE_SHARD});
      this.droplist.put(20112, new int[]{40, FLAME_SHARD});
      this.droplist.put(20113, new int[]{40, ICE_SHARD});
      this.droplist.put(20114, new int[]{50, FLAME_SHARD});
      this.droplist.put(20114, new int[]{50, ICE_SHARD});
      this.addStartNpc(30004);
      this.addTalkId(30004);
      this.addKillId(new int[]{20109, 20110, 20112, 20113, 20114, 20115});
      this.questItemIds = new int[]{FLAME_SHARD, ICE_SHARD};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState("_306_CrystalOfFireice");
      if (st == null) {
         return event;
      } else {
         if (event.equalsIgnoreCase("30004-04.htm")) {
            st.set("cond", "1");
            st.setState((byte)1);
            st.playSound("ItemSound.quest_accept");
         } else if (event.equalsIgnoreCase("30004-08.htm")) {
            st.exitQuest(true);
            st.playSound("ItemSound.quest_finish");
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_306_CrystalOfFireice");
      if (st == null) {
         return htmltext;
      } else {
         switch(st.getState()) {
            case 0:
               if (player.getLevel() >= 17) {
                  htmltext = "30004-03.htm";
               } else {
                  htmltext = "30004-02.htm";
                  st.exitQuest(true);
               }
               break;
            case 1:
               long Shrads_count = st.getQuestItemsCount(FLAME_SHARD) + st.getQuestItemsCount(ICE_SHARD);
               long Reward = Shrads_count * 30L + (long)(Shrads_count >= 10L ? 5000 : 0);
               if (Reward > 0L) {
                  st.giveItems(ADENA, Reward);
                  st.takeItems(FLAME_SHARD, -1L);
                  st.takeItems(ICE_SHARD, -1L);
                  htmltext = "30004-07.htm";
               } else {
                  htmltext = "30004-05.htm";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      QuestState st = player.getQuestState("_306_CrystalOfFireice");
      if (st == null) {
         return null;
      } else {
         int npcId = npc.getId();
         if (this.droplist.containsKey(npcId)) {
            int chance = this.droplist.get(npcId)[0];
            int item = this.droplist.get(npcId)[1];
            if (st.getRandom(100) < chance) {
               st.giveItems(item, 1L);
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _306_CrystalOfFireice(306, "_306_CrystalOfFireice", "");
   }
}
