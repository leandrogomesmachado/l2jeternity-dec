package l2e.scripts.quests;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.quest.Quest;
import l2e.gameserver.model.quest.QuestState;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import l2e.gameserver.network.serverpackets.RadarControl;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class _508_TheClansReputation extends Quest {
   private static final String qn = "_508_TheClansReputation";
   private static final int SIR_ERIC_RODEMAI = 30868;
   private static final Map<Integer, List<Integer>> REWARD_POINTS = new HashMap<>();
   private static final int[] RAID_BOSS = new int[]{25252, 25478, 25255, 25245, 25051, 25524};

   public _508_TheClansReputation(int id, String name, String descr) {
      super(id, name, descr);
      this.addStartNpc(30868);
      this.addTalkId(30868);
      this.addKillId(RAID_BOSS);
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      QuestState st = player.getQuestState(this.getName());
      if (st == null) {
         return getNoQuestMsg(player);
      } else {
         switch(event) {
            case "30868-0.htm":
               st.startQuest();
               break;
            case "30868-1.htm":
               st.set("raid", "1");
               player.sendPacket(new RadarControl(0, 2, 192376, 22087, -3608));
               break;
            case "30868-2.htm":
               st.set("raid", "2");
               player.sendPacket(new RadarControl(0, 2, 168288, 28368, -3632));
               break;
            case "30868-3.htm":
               st.set("raid", "3");
               player.sendPacket(new RadarControl(0, 2, 170048, -24896, -3440));
               break;
            case "30868-4.htm":
               st.set("raid", "4");
               player.sendPacket(new RadarControl(0, 2, 188809, 47780, -5968));
               break;
            case "30868-5.htm":
               st.set("raid", "5");
               player.sendPacket(new RadarControl(0, 2, 117760, -9072, -3264));
               break;
            case "30868-6.htm":
               st.set("raid", "6");
               player.sendPacket(new RadarControl(0, 2, 144600, -5500, -4100));
               break;
            case "30868-7.htm":
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
               htmltext = clan != null && player.isClanLeader() && clan.getLevel() >= 5 ? "30868-0b.htm" : "30868-0a.htm";
               break;
            case 1:
               if (clan == null || !player.isClanLeader()) {
                  st.exitQuest(true);
                  return "30868-8.htm";
               }

               int raid = st.getInt("raid");
               if (REWARD_POINTS.containsKey(raid)) {
                  if (st.hasQuestItems(REWARD_POINTS.get(raid).get(1))) {
                     htmltext = "30868-" + raid + "b.htm";
                     st.playSound("ItemSound.quest_fanfare_1");
                     st.takeItems(REWARD_POINTS.get(raid).get(1), -1L);
                     int rep = REWARD_POINTS.get(raid).get(2);
                     clan.addReputationScore(rep, true);
                     player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CLAN_QUEST_COMPLETED_AND_S1_POINTS_GAINED).addNumber(rep));
                     clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
                  } else {
                     htmltext = "30868-" + raid + "a.htm";
                  }
               } else {
                  htmltext = "30868-0.htm";
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
            st = player.getQuestState("_508_TheClansReputation");
         } else {
            Player pleader = player.getClan().getLeader().getPlayerInstance();
            if (pleader != null && player.isInsideRadius(pleader, 1500, true, false)) {
               st = pleader.getQuestState("_508_TheClansReputation");
            }
         }

         if (st != null && st.isStarted()) {
            int raid = st.getInt("raid");
            if (REWARD_POINTS.containsKey(raid) && npc.getId() == REWARD_POINTS.get(raid).get(0) && !st.hasQuestItems(REWARD_POINTS.get(raid).get(1))) {
               st.rewardItems(REWARD_POINTS.get(raid).get(1), 1L);
               st.playSound("ItemSound.quest_itemget");
            }
         }

         return null;
      }
   }

   public static void main(String[] args) {
      new _508_TheClansReputation(508, "_508_TheClansReputation", "");
   }

   static {
      REWARD_POINTS.put(1, Arrays.asList(25252, 8277, 560));
      REWARD_POINTS.put(2, Arrays.asList(25478, 14883, 584));
      REWARD_POINTS.put(3, Arrays.asList(25255, 8280, 602));
      REWARD_POINTS.put(4, Arrays.asList(25245, 8281, 784));
      REWARD_POINTS.put(5, Arrays.asList(25051, 8282, 558));
      REWARD_POINTS.put(6, Arrays.asList(25524, 8494, 768));
   }
}
