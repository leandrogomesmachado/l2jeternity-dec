package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.PledgeReceiveWarList;

public final class RequestPledgeWarList extends GameClientPacket {
   protected int _unk1;
   private int _tab;

   @Override
   protected void readImpl() {
      this._unk1 = this.readD();
      this._tab = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (activeChar.getClan() != null) {
            activeChar.sendPacket(new PledgeReceiveWarList(activeChar.getClan(), this._tab));
         }
      }
   }
}
