package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;

public class ExFishingEnd extends GameServerPacket {
   private final boolean _win;
   private final Creature _activeChar;

   public ExFishingEnd(boolean win, Player character) {
      this._win = win;
      this._activeChar = character;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._activeChar.getObjectId());
      this.writeC(this._win ? 1 : 0);
   }
}
