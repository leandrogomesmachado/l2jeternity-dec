package l2e.gameserver.network.clientpackets;

import l2e.gameserver.instancemanager.TerritoryWarManager;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExShowDominionRegistry;

public final class RequestJoinDominionWar extends GameClientPacket {
   private int _territoryId;
   private int _isClan;
   private int _isJoining;

   @Override
   protected void readImpl() {
      this._territoryId = this.readD();
      this._isClan = this.readD();
      this._isJoining = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         Clan clan = activeChar.getClan();
         int castleId = this._territoryId - 80;
         if (TerritoryWarManager.getInstance().getIsRegistrationOver()) {
            activeChar.sendPacket(SystemMessageId.NOT_TERRITORY_REGISTRATION_PERIOD);
         } else if (clan != null && TerritoryWarManager.getInstance().getTerritory(castleId).getOwnerClan() == clan) {
            activeChar.sendPacket(SystemMessageId.THE_TERRITORY_OWNER_CLAN_CANNOT_PARTICIPATE_AS_MERCENARIES);
         } else {
            if (this._isClan == 1) {
               if ((activeChar.getClanPrivileges() & 262144) != 262144) {
                  activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                  return;
               }

               if (clan == null) {
                  return;
               }

               if (this._isJoining == 1) {
                  if (System.currentTimeMillis() < clan.getDissolvingExpiryTime()) {
                     activeChar.sendPacket(SystemMessageId.CANT_PARTICIPATE_IN_SIEGE_WHILE_DISSOLUTION_IN_PROGRESS);
                     return;
                  }

                  if (TerritoryWarManager.getInstance().checkIsRegistered(-1, clan)) {
                     activeChar.sendPacket(SystemMessageId.YOU_ALREADY_REQUESTED_TW_REGISTRATION);
                     return;
                  }

                  TerritoryWarManager.getInstance().registerClan(castleId, clan);
               } else {
                  TerritoryWarManager.getInstance().removeClan(castleId, clan);
               }
            } else {
               if (activeChar.getLevel() < 40 || activeChar.getClassId().level() < 2) {
                  return;
               }

               if (this._isJoining == 1) {
                  if (TerritoryWarManager.getInstance().checkIsRegistered(-1, activeChar.getObjectId())) {
                     activeChar.sendPacket(SystemMessageId.YOU_ALREADY_REQUESTED_TW_REGISTRATION);
                     return;
                  }

                  if (clan != null && TerritoryWarManager.getInstance().checkIsRegistered(-1, clan)) {
                     activeChar.sendPacket(SystemMessageId.YOU_ALREADY_REQUESTED_TW_REGISTRATION);
                     return;
                  }

                  TerritoryWarManager.getInstance().registerMerc(castleId, activeChar);
               } else {
                  TerritoryWarManager.getInstance().removeMerc(castleId, activeChar);
               }
            }

            activeChar.sendPacket(new ExShowDominionRegistry(castleId, activeChar));
         }
      }
   }
}
