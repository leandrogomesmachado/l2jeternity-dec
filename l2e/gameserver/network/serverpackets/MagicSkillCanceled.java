package l2e.gameserver.network.serverpackets;

public class MagicSkillCanceled extends GameServerPacket {
   private final int _objectId;

   public MagicSkillCanceled(int objectId) {
      this._objectId = objectId;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._objectId);
   }
}
