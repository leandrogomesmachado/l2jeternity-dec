package l2e.gameserver.network.serverpackets;

public class AutoAttackStop extends GameServerPacket {
   private final int _targetObjId;

   public AutoAttackStop(int targetObjId) {
      this._targetObjId = targetObjId;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._targetObjId);
   }
}
