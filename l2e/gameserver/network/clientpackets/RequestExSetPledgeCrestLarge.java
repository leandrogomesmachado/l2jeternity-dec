package l2e.gameserver.network.clientpackets;

import l2e.gameserver.data.holder.CrestHolder;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.Crest;
import l2e.gameserver.model.CrestType;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;

public final class RequestExSetPledgeCrestLarge extends GameClientPacket {
   private int _length;
   private byte[] _data;

   @Override
   protected void readImpl() {
      this._length = this.readD();
      if (this._length <= 2176) {
         this._data = new byte[this._length];
         this.readB(this._data);
      }
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         Clan clan = activeChar.getClan();
         if (clan != null) {
            if (this._length >= 0 && this._length <= 2176) {
               if (clan.getDissolvingExpiryTime() > System.currentTimeMillis()) {
                  activeChar.sendPacket(SystemMessageId.CANNOT_SET_CREST_WHILE_DISSOLUTION_IN_PROGRESS);
               } else {
                  if ((activeChar.getClanPrivileges() & 128) == 128) {
                     if (this._length != 0 && this._data != null) {
                        if (clan.getCastleId() == 0 && clan.getHideoutId() == 0) {
                           activeChar.sendMessage("Only a clan that owns a clan hall or a castle can get their emblem displayed on clan related items");
                           return;
                        }

                        Crest crest = CrestHolder.getInstance().createCrest(this._data, CrestType.PLEDGE_LARGE);
                        if (crest != null) {
                           clan.changeLargeCrest(crest.getId());
                           activeChar.sendPacket(SystemMessageId.CLAN_EMBLEM_WAS_SUCCESSFULLY_REGISTERED);
                        }
                     } else {
                        if (clan.getCrestLargeId() == 0) {
                           return;
                        }

                        if (clan.getCrestLargeId() != 0) {
                           clan.changeLargeCrest(0);
                           activeChar.sendPacket(SystemMessageId.CLAN_CREST_HAS_BEEN_DELETED);
                        }
                     }
                  }
               }
            } else {
               activeChar.sendPacket(SystemMessageId.WRONG_SIZE_UPLOADED_CREST);
            }
         }
      }
   }
}
