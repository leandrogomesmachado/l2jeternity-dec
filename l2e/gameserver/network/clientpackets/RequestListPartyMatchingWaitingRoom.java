package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.ExListPartyMatchingWaitingRoom;

public class RequestListPartyMatchingWaitingRoom extends GameClientPacket {
   private int _minLevel;
   private int _maxLevel;
   private int _page;
   private int[] _classes;

   @Override
   protected void readImpl() {
      this._page = this.readD();
      this._minLevel = this.readD();
      this._maxLevel = this.readD();
      int size = this.readD();
      if (size > 127 || size < 0) {
         size = 0;
      }

      this._classes = new int[size];

      for(int i = 0; i < size; ++i) {
         this._classes[i] = this.readD();
      }
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         activeChar.sendPacket(new ExListPartyMatchingWaitingRoom(activeChar, this._minLevel, this._maxLevel, this._page, this._classes));
      }
   }
}
