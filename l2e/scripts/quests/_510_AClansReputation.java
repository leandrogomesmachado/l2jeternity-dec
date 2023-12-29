package l2e.scripts.quests;

import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class _510_AClansReputation extends Quest {
   private static final String qn = "_510_AClansReputation";
   private static final int VALDIS = 31331;
   private static final int TYRANNOSAURUS_CLAW = 8767;
   private static final int[] MOBS = new int[]{22215, 22216, 22217};

   public _510_AClansReputation(int questId, String name, String descr) {
      super(questId, name, descr);
      this.addStartNpc(31331);
      this.addTalkId(31331);
      this.addKillId(MOBS);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return getNoQuestMsg(player);
      } else {
         switch(event) {
            case "31331-3.html":
               st.startQuest();
               break;
            case "31331-6.html":
               st.exitQuest(true, true);
         }

         return event;
      }
   }

   @Override
   public String onTalk(Npc npc, Player player) {
      String htmltext = getNoQuestMsg(player);
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return htmltext;
      } else {
         Clan clan = player.getClan();
         switch(st.getState()) {
            case 0:
               htmltext = clan != null && player.isClanLeader() && clan.getLevel() >= 5 ? "31331-1.htm" : "31331-0.htm";
               break;
            case 1:
               if (clan == null || !player.isClanLeader()) {
                  st.exitQuest(true);
                  return "31331-8.html";
               }

               if (!st.hasQuestItems(8767)) {
                  htmltext = "31331-4.html";
               } else {
                  int count = (int)st.getQuestItemsCount(8767);
                  int reward = count < 10 ? 30 * count : 59 + 30 * count;
                  st.playSound("ItemSound.quest_fanfare_1");
                  st.takeItems(8767, -1L);
                  clan.addReputationScore(reward, true);
                  player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_QUEST_COMPLETED_AND_S1_POINTS_GAINED).addNumber(reward));
                  clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
                  htmltext = "31331-7.html";
               }
         }

         return htmltext;
      }
   }

   @Override
   public String onKill(Npc npc, Player player, boolean isSummon) {
      if (player.getClan() == null) {
         return null;
      } else {
         QuestState st = null;
         if (player.isClanLeader()) {
            st = player.getQuestState("_510_AClansReputation");
         } else {
            Player pleader = player.getClan().getLeader().getPlayerInstance();
            if (pleader != null && player.isInsideRadius(pleader, 1500, true, false)) {
               st = pleader.getQuestState("_510_AClansReputation");
            }
         }

         if (st != null && st.isStarted()) {
            st.rewardItems(8767, 1L);
            st.playSound("ItemSound.quest_itemget");
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _510_AClansReputation(510, "_510_AClansReputation", "");
   }
}
