package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.HennaEquipList;

public final class RequestHennaItemList extends GameClientPacket {
   protected int _unknown;

   @Override
   protected void readImpl() {
      this._unknown = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getActiveChar();
      if (activeChar != null) {
         activeChar.sendPacket(new HennaEquipList(activeChar));
      }
   }
}
