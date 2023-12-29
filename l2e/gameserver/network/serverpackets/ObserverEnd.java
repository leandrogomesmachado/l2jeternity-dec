package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Player;

public class ObserverEnd extends GameServerPacket {
   private final Player _activeChar;

   public ObserverEnd(Player observer) {
      this._activeChar = observer;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._activeChar.getLastX());
      this.writeD(this._activeChar.getLastY());
      this.writeD(this._activeChar.getLastZ());
   }
}
