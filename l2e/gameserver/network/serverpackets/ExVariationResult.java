package l2e.gameserver.network.serverpackets;

public class ExVariationResult extends GameServerPacket {
   private final int _stat12;
   private final int _stat34;
   private final int _unk3;

   public ExVariationResult(int unk1, int unk2, int unk3) {
      this._stat12 = unk1;
      this._stat34 = unk2;
      this._unk3 = unk3;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._stat12);
      this.writeD(this._stat34);
      this.writeD(this._unk3);
   }
}
