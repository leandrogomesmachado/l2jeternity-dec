package l2e.gameserver.network.clientpackets;

import l2e.gameserver.data.holder.ClanHolder;
import l2e.gameserver.data.holder.CrestHolder;
import l2e.gameserver.model.Clan;
import l2e.gameserver.model.Crest;
import l2e.gameserver.model.CrestType;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;

public final class RequestSetAllyCrest extends GameClientPacket {
   private int _length;
   private byte[] _data;

   @Override
   protected void readImpl() {
      this._length = this.readD();
      if (this._length <= 192) {
         this._data = new byte[this._length];
         this.readB(this._data);
      }
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (this._length < 0) {
            activeChar.sendMessage("File transfer error.");
         } else if (this._length > 192) {
            activeChar.sendPacket(SystemMessageId.ADJUST_IMAGE_8_12);
         } else if (activeChar.getAllyId() == 0) {
            activeChar.sendPacket(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER);
         } else {
            Clan leaderClan = ClanHolder.getInstance().getClan(activeChar.getAllyId());
            if (activeChar.getClanId() == leaderClan.getId() && activeChar.isClanLeader()) {
               if (this._length == 0) {
                  if (leaderClan.getAllyCrestId() != 0) {
                     leaderClan.changeAllyCrest(0, false);
                  }
               } else {
                  Crest crest = CrestHolder.getInstance().createCrest(this._data, CrestType.ALLY);
                  if (crest != null) {
                     leaderClan.changeAllyCrest(crest.getId(), false);
                     activeChar.sendPacket(SystemMessageId.CLAN_CREST_WAS_SUCCESSFULLY_REGISTRED);
                  }
               }
            } else {
               activeChar.sendPacket(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER);
            }
         }
      }
   }
}
