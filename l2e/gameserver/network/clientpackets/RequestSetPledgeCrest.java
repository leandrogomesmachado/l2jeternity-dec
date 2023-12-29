package l2e.gameserver.network.clientpackets;

import l2e.gameserver.data.holder.CrestHolder;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.Crest;
import l2e.gameserver.model.CrestType;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;

public final class RequestSetPledgeCrest extends GameClientPacket {
   private int _length;
   private byte[] _data;

   @Override
   protected void readImpl() {
      this._length = this.readD();
      if (this._length <= 256) {
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
            if (clan.getDissolvingExpiryTime() > System.currentTimeMillis()) {
               activeChar.sendPacket(SystemMessageId.CANNOT_SET_CREST_WHILE_DISSOLUTION_IN_PROGRESS);
            } else if (this._length < 0) {
               activeChar.sendMessage("File transfer error.");
            } else if (this._length > 256) {
               activeChar.sendPacket(SystemMessageId.THE_SIZE_OF_THE_IMAGE_FILE_IS_INAPPROPRIATE);
            } else {
               if ((activeChar.getClanPrivileges() & 128) == 128) {
                  if (this._length != 0 && this._data.length != 0) {
                     if (clan.getLevel() < 3) {
                        activeChar.sendPacket(SystemMessageId.CLAN_LVL_3_NEEDED_TO_SET_CREST);
                        return;
                     }

                     Crest crest = CrestHolder.getInstance().createCrest(this._data, CrestType.PLEDGE);
                     if (crest != null) {
                        clan.changeClanCrest(crest.getId());
                        activeChar.sendPacket(SystemMessageId.CLAN_CREST_WAS_SUCCESSFULLY_REGISTRED);
                     }
                  } else {
                     if (clan.getCrestId() == 0) {
                        return;
                     }

                     if (clan.getCrestId() != 0) {
                        clan.changeClanCrest(0);
                        activeChar.sendPacket(SystemMessageId.CLAN_CREST_HAS_BEEN_DELETED);
                     }
                  }
               }
            }
         }
      }
   }
}
