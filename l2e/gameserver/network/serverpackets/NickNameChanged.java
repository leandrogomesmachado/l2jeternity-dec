package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Creature;

public class NickNameChanged extends GameServerPacket {
   private final String _title;
   private final int _objectId;

   public NickNameChanged(Creature cha) {
      this._objectId = cha.getObjectId();
      this._title = cha.getTitle();
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._objectId);
      this.writeS(this._title);
   }
}
