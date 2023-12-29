package l2e.gameserver.network.clientpackets;

import l2e.gameserver.data.parser.HennaParser;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Henna;
import l2e.gameserver.network.serverpackets.HennaItemInfo;

public final class RequestHennaItemInfo extends GameClientPacket {
   private int _symbolId;

   @Override
   protected void readImpl() {
      this._symbolId = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getActiveChar();
      if (activeChar != null) {
         Henna henna = HennaParser.getInstance().getHenna(this._symbolId);
         if (henna == null) {
            if (this._symbolId != 0) {
               _log.warning(this.getClass().getSimpleName() + ": Invalid Henna Id: " + this._symbolId + " from player " + activeChar);
            }

            this.sendActionFailed();
         } else {
            activeChar.sendPacket(new HennaItemInfo(henna, activeChar));
         }
      }
   }
}
