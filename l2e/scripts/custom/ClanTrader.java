package l2e.scripts.custom;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;
import l2e.scripts.ai.AbstractNpcAI;

public class ClanTrader extends AbstractNpcAI {
   private static final int[] CLAN_TRADER = new int[]{32024, 32025};
   private static final int BLOOD_ALLIANCE = 9911;
   private static final int BLOOD_ALLIANCE_COUNT = 1;
   private static final int BLOOD_OATH = 9910;
   private static final int BLOOD_OATH_COUNT = 10;
   private static final int KNIGHTS_EPAULETTE = 9912;
   private static final int KNIGHTS_EPAULETTE_COUNT = 100;

   private ClanTrader(String name, String descr) {
      super(name, descr);
      this.addStartNpc(CLAN_TRADER);
      this.addTalkId(CLAN_TRADER);
      this.addFirstTalkId(CLAN_TRADER);
   }

   private String giveReputation(Npc npc, Player player, int count, int itemId, int itemCount) {
      if (getQuestItemsCount(player, itemId) >= (long)itemCount) {
         takeItems(player, itemId, (long)itemCount);
         player.getClan().addReputationScore(count, true);
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_ADDED_S1S_POINTS_TO_REPUTATION_SCORE);
         sm.addNumber(count);
         player.sendPacket(sm);
         return npc.getId() + "-04.htm";
      } else {
         return npc.getId() + "-03.htm";
      }
   }

   @Override
   public String onAdvEvent(String event, Npc npc, Player player) {
      String htmltext = null;
      switch(event) {
         case "32024.htm":
         case "32024-02.htm":
         case "32025.htm":
         case "32025-02.htm":
            htmltext = event;
            break;
         case "repinfo":
            htmltext = player.getClan().getLevel() > 4 ? npc.getId() + "-02.htm" : npc.getId() + "-05.htm";
            break;
         case "exchange-ba":
            htmltext = this.giveReputation(npc, player, Config.BLOODALLIANCE_POINTS, 9911, 1);
            break;
         case "exchange-bo":
            htmltext = this.giveReputation(npc, player, Config.BLOODOATH_POINTS, 9910, 10);
            break;
         case "exchange-ke":
            htmltext = this.giveReputation(npc, player, Config.KNIGHTSEPAULETTE_POINTS, 9912, 100);
      }

      return htmltext;
   }

   @Override
   public String onFirstTalk(Npc npc, Player player) {
      return !player.isClanLeader() && (player.getClanPrivileges() & 512) != 512 ? npc.getId() + "-01.htm" : npc.getId() + ".htm";
   }

   public static void main(String[] args) {
      new ClanTrader(ClanTrader.class.getSimpleName(), "custom");
   }
}
