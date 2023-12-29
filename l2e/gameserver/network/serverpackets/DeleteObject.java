package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;

public final class DeleteObject extends GameServerPacket {
   private final int _objectId;

   public DeleteObject(GameObject obj) {
      this._objectId = obj.getObjectId();
   }

   public DeleteObject(int objectId) {
      this._objectId = objectId;
   }

   @Override
   protected final void writeImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null && activeChar.getObjectId() != this._objectId) {
         this.writeD(this._objectId);
         this.writeD(1);
      }
   }
}
