package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;

public final class RequestShortCutDel extends GameClientPacket {
   private int _slot;
   private int _page;

   @Override
   protected void readImpl() {
      int id = this.readD();
      this._slot = id % 12;
      this._page = id / 12;
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (this._page <= 12 && this._page >= 0) {
            activeChar.deleteShortCut(this._slot, this._page);
         }
      }
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }
}
