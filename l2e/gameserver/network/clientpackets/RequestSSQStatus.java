package l2e.gameserver.network.clientpackets;

import l2e.gameserver.SevenSigns;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.SSQStatus;

public final class RequestSSQStatus extends GameClientPacket {
   private int _page;

   @Override
   protected void readImpl() {
      this._page = this.readC();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (!SevenSigns.getInstance().isSealValidationPeriod() && !SevenSigns.getInstance().isCompResultsPeriod() || this._page != 4) {
            SSQStatus ssqs = new SSQStatus(activeChar.getObjectId(), this._page);
            activeChar.sendPacket(ssqs);
         }
      }
   }
}
