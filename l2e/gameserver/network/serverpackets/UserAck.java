package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Creature;

public class UserAck extends GameServerPacket {
   private final int _chaId;
   private final int _unk1;
   private final int _unk2;

   public UserAck(Creature cha, int unk1, int unk2) {
      this._chaId = cha.getObjectId();
      this._unk1 = unk1;
      this._unk2 = unk2;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._chaId);
      this.writeD(this._unk1);
      this.writeD(this._unk2);
   }
}
