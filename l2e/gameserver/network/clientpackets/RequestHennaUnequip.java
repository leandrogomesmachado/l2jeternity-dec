package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Henna;
import l2e.gameserver.network.SystemMessageId;

public final class RequestHennaUnequip extends GameClientPacket {
   private int _symbolId;

   @Override
   protected void readImpl() {
      this._symbolId = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getActiveChar();
      if (activeChar != null) {
         boolean found = false;

         for(int i = 1; i <= 3; ++i) {
            Henna henna = activeChar.getHenna(i);
            if (henna != null && henna.getDyeId() == this._symbolId) {
               if (activeChar.getAdena() >= (long)henna.getCancelFee()) {
                  activeChar.removeHenna(i);
               } else {
                  activeChar.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
                  this.sendActionFailed();
               }

               found = true;
               break;
            }
         }

         if (!found) {
            _log.warning(this.getClass().getSimpleName() + ": Player " + activeChar + " requested Henna Draw remove without any henna.");
            this.sendActionFailed();
         }
      }
   }
}
