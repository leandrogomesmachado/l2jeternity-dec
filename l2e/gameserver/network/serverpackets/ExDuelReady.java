package l2e.gameserver.network.serverpackets;

public class ExDuelReady extends GameServerPacket {
   private final int _unk1;

   public ExDuelReady(int unk1) {
      this._unk1 = unk1;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._unk1);
   }
}
