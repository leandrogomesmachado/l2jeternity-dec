package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;

public final class RequestWithdrawAlly extends GameClientPacket {
   @Override
   protected void readImpl() {
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         if (player.getClan() == null) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
         } else if (!player.isClanLeader()) {
            player.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_WITHDRAW_ALLY);
         } else {
            Clan clan = player.getClan();
            if (clan.getAllyId() == 0) {
               player.sendPacket(SystemMessageId.NO_CURRENT_ALLIANCES);
            } else if (clan.getId() == clan.getAllyId()) {
               player.sendPacket(SystemMessageId.ALLIANCE_LEADER_CANT_WITHDRAW);
            } else {
               long currentTime = System.currentTimeMillis();
               clan.setAllyId(0);
               clan.setAllyName(null);
               clan.changeAllyCrest(0, true);
               clan.setAllyPenaltyExpiryTime(currentTime + (long)Config.ALT_ALLY_JOIN_DAYS_WHEN_LEAVED * 3600000L, 1);
               clan.updateClanInDB();
               player.sendPacket(SystemMessageId.YOU_HAVE_WITHDRAWN_FROM_ALLIANCE);
            }
         }
      }
   }
}
