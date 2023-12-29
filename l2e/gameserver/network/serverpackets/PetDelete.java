package l2e.gameserver.network.serverpackets;

public class PetDelete extends GameServerPacket {
   private final int _petType;
   private final int _petObjId;

   public PetDelete(int petType, int petObjId) {
      this._petType = petType;
      this._petObjId = petObjId;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._petType);
      this.writeD(this._petObjId);
   }
}
