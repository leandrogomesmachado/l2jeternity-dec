package l2e.scripts.quests;

import java.util.HashMap;
import java.util.Map;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class _691_MatrasSuspiciousRequest extends Quest {
   private static final String qn = "_691_MatrasSuspiciousRequest";
   private static final int MATRAS = 32245;
   long item_cou = 0L;
   private static final int DYNASTIC_ESSENCE_II = 10413;
   private static final int RED_STONE = 10372;
   private static final Map<Integer, Integer> REWARD_CHANCES = new HashMap<>();

   public _691_MatrasSuspiciousRequest(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(32245);
      this.addTalkId(32245);

      for(int npcId : REWARD_CHANCES.keySet()) {
         this.addKillId(npcId);
      }

      this.questItemIds = new int[]{10372};
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = event;
      QuestState st = player.getQuestState("_691_MatrasSuspiciousRequest");
      if (st == null) {
         return event;
      } else {
         this.item_cou = st.getQuestItemsCount(10372);
         if (event.equalsIgnoreCase("32245-04.htm")) {
            if (player.getLevel() >= 76) {
               st.setState((byte)1);
               st.set("cond", "1");
               st.playSound("ItemSound.quest_accept");
            }
         } else if (event.equalsIgnoreCase("take_reward")) {
            if (this.item_cou >= 744L) {
               st.takeItems(10372, 744L);
               st.giveItems(10413, 1L);
               htmltext = "32245-09.htm";
            } else {
               NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
               html.setFile(player, player.getLang(), "data/scripts/quests/_691_MatrasSuspiciousRequest/" + player.getLang() + "/32245-06.htm");
               html.replace("%itemcount%", Long.toString(this.item_cou));
            }
         } else if (event.equalsIgnoreCase("32245-08.htm")) {
            NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
            html.setFile(player, player.getLang(), "data/scripts/quests/_691_MatrasSuspiciousRequest/" + player.getLang() + "/32245-08.htm");
            html.replace("%itemcount%", Long.toString(this.item_cou));
         } else if (event.equalsIgnoreCase("32245-12.htm")) {
            st.giveItems(57, this.item_cou * 10000L);
            st.takeItems(10372, this.item_cou);
            st.playSound("ItemSound.quest_finish");
            st.exitQuest(true);
         }

         return htmltext;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState("_691_MatrasSuspiciousRequest");
      if (st == null) {
         return htmltext;
      } else {
         if (st.getState() == 0) {
            if (player.getLevel() >= 76) {
               htmltext = "32245-01.htm";
            } else {
               htmltext = "32245-03.htm";
            }
         } else if (st.getState() == 1) {
            this.item_cou = st.getQuestItemsCount(10372);
            if (this.item_cou > 0L) {
               htmltext = "32245-05.htm";
            } else if (this.item_cou == 0L) {
               htmltext = "32245-06.htm";
            } else if (this.item_cou > 0L) {
               NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
               html.setFile(player, player.getLang(), "data/scripts/quests/_691_MatrasSuspiciousRequest/" + player.getLang() + "/32245-06.htm");
               html.replace("%itemcount%", Long.toString(this.item_cou));
            }
         }

         return htmltext;
      }
   }

   @Override
   public final String onKill(Npc npc, Player player, boolean isSummon) {
      Player pl = this.getRandomPartyMember(player, 1);
      if (pl == null) {
         return null;
      } else {
         QuestState st = pl.getQuestState("_691_MatrasSuspiciousRequest");
         int chance = (int)(Config.RATE_QUEST_DROP * (float)REWARD_CHANCES.get(npc.getId()).intValue());
         int numItems = Math.max(chance / 1000, 1);
         chance %= 1000;
         if (getRandom(1000) <= chance) {
            st.giveItems(10372, (long)numItems);
            st.playSound("ItemSound.quest_itemget");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _691_MatrasSuspiciousRequest(691, "_691_MatrasSuspiciousRequest", "");
   }

   static {
      REWARD_CHANCES.put(22363, 890);
      REWARD_CHANCES.put(22364, 261);
      REWARD_CHANCES.put(22365, 560);
      REWARD_CHANCES.put(22366, 560);
      REWARD_CHANCES.put(22367, 190);
      REWARD_CHANCES.put(22368, 129);
      REWARD_CHANCES.put(22369, 210);
      REWARD_CHANCES.put(22370, 787);
      REWARD_CHANCES.put(22371, 257);
      REWARD_CHANCES.put(22372, 656);
   }
}
