package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;

public final class RequestOustAlly extends GameClientPacket {
   private String _clanName;

   @Override
   protected void readImpl() {
      this._clanName = this.readS();
   }

   @Override
   protected void runImpl() {
      if (this._clanName != null) {
         Player player = this.getClient().getActiveChar();
         if (player != null) {
            if (player.getClan() == null) {
               player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
            } else {
               Clan leaderClan = player.getClan();
               if (leaderClan.getAllyId() == 0) {
                  player.sendPacket(SystemMessageId.NO_CURRENT_ALLIANCES);
               } else if (player.isClanLeader() && leaderClan.getId() == leaderClan.getAllyId()) {
                  Clan clan = ClanHolder.getInstance().getClanByName(this._clanName);
                  if (clan == null) {
                     player.sendPacket(SystemMessageId.CLAN_DOESNT_EXISTS);
                  } else if (clan.getId() == leaderClan.getId()) {
                     player.sendPacket(SystemMessageId.ALLIANCE_LEADER_CANT_WITHDRAW);
                  } else if (clan.getAllyId() != leaderClan.getAllyId()) {
                     player.sendPacket(SystemMessageId.DIFFERENT_ALLIANCE);
                  } else {
                     long currentTime = System.currentTimeMillis();
                     leaderClan.setAllyPenaltyExpiryTime(currentTime + (long)Config.ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED * 3600000L, 3);
                     leaderClan.updateClanInDB();
                     clan.setAllyId(0);
                     clan.setAllyName(null);
                     clan.changeAllyCrest(0, true);
                     clan.setAllyPenaltyExpiryTime(currentTime + (long)Config.ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED * 3600000L, 2);
                     clan.updateClanInDB();
                     player.sendPacket(SystemMessageId.YOU_HAVE_EXPELED_A_CLAN);
                  }
               } else {
                  player.sendPacket(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER);
               }
            }
         }
      }
   }
}
