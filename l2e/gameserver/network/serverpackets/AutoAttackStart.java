package l2e.gameserver.network.serverpackets;

public final class AutoAttackStart extends GameServerPacket {
   private final int _targetObjId;

   public AutoAttackStart(int targetId) {
      this._targetObjId = targetId;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._targetObjId);
   }
}
