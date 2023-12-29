package l2e.gameserver.network.clientpackets;

import l2e.gameserver.instancemanager.CHSiegeManager;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.clanhall.SiegableHall;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CastleSiegeInfo;

public final class RequestJoinCastleSiege extends GameClientPacket {
   private int _castleId;
   private int _isAttacker;
   private int _isJoining;

   @Override
   protected void readImpl() {
      this._castleId = this.readD();
      this._isAttacker = this.readD();
      this._isJoining = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if ((activeChar.getClanPrivileges() & 262144) != 262144) {
            activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
         } else {
            Clan clan = activeChar.getClan();
            if (clan != null) {
               Castle castle = CastleManager.getInstance().getCastleById(this._castleId);
               if (castle != null) {
                  if (this._isJoining == 1) {
                     if (System.currentTimeMillis() < clan.getDissolvingExpiryTime()) {
                        activeChar.sendPacket(SystemMessageId.CANT_PARTICIPATE_IN_SIEGE_WHILE_DISSOLUTION_IN_PROGRESS);
                        return;
                     }

                     if (this._isAttacker == 1) {
                        castle.getSiege().registerAttacker(activeChar);
                     } else {
                        castle.getSiege().registerDefender(activeChar);
                     }
                  } else {
                     castle.getSiege().removeSiegeClan(activeChar);
                  }

                  castle.getSiege().listRegisterClan(activeChar);
               }

               SiegableHall hall = CHSiegeManager.getInstance().getSiegableHall(this._castleId);
               if (hall != null) {
                  if (this._isJoining == 1) {
                     if (System.currentTimeMillis() < clan.getDissolvingExpiryTime()) {
                        activeChar.sendPacket(SystemMessageId.CANT_PARTICIPATE_IN_SIEGE_WHILE_DISSOLUTION_IN_PROGRESS);
                        return;
                     }

                     CHSiegeManager.getInstance().registerClan(clan, hall, activeChar);
                  } else {
                     CHSiegeManager.getInstance().unRegisterClan(clan, hall);
                  }

                  activeChar.sendPacket(new CastleSiegeInfo(hall));
               }
            }
         }
      }
   }
}
