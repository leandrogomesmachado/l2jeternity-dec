package l2e.gameserver.network.serverpackets;

public class ShowXMasSeal extends GameServerPacket {
   private final int _item;

   public ShowXMasSeal(int item) {
      this._item = item;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._item);
   }
}
