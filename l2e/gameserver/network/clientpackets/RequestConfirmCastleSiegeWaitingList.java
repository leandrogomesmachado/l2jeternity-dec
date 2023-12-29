package l2e.gameserver.network.clientpackets;

import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.network.serverpackets.CastleSiegeDefenderList;

public final class RequestConfirmCastleSiegeWaitingList extends GameClientPacket {
   private int _approved;
   private int _castleId;
   private int _clanId;

   @Override
   protected void readImpl() {
      this._castleId = this.readD();
      this._clanId = this.readD();
      this._approved = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (activeChar.getClan() != null) {
            Castle castle = CastleManager.getInstance().getCastleById(this._castleId);
            if (castle != null) {
               if (castle.getOwnerId() == activeChar.getClanId() && activeChar.isClanLeader()) {
                  Clan clan = ClanHolder.getInstance().getClan(this._clanId);
                  if (clan != null) {
                     if (!castle.getSiege().getIsRegistrationOver()) {
                        if (this._approved == 1) {
                           if (!castle.getSiege().checkIsDefenderWaiting(clan)) {
                              return;
                           }

                           castle.getSiege().approveSiegeDefenderClan(this._clanId);
                        } else if (castle.getSiege().checkIsDefenderWaiting(clan) || castle.getSiege().checkIsDefender(clan)) {
                           castle.getSiege().removeSiegeClan(this._clanId);
                        }
                     }

                     activeChar.sendPacket(new CastleSiegeDefenderList(castle));
                  }
               }
            }
         }
      }
   }
}
