package l2e.gameserver.network.serverpackets;

import java.util.Map;

public class ShopPreviewInfo extends GameServerPacket {
   private final Map<Integer, Integer> _itemlist;

   public ShopPreviewInfo(Map<Integer, Integer> itemlist) {
      this._itemlist = itemlist;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(25);
      this.writeD(this.getFromList(0));
      this.writeD(this.getFromList(8));
      this.writeD(this.getFromList(9));
      this.writeD(this.getFromList(4));
      this.writeD(this.getFromList(13));
      this.writeD(this.getFromList(14));
      this.writeD(this.getFromList(1));
      this.writeD(this.getFromList(5));
      this.writeD(this.getFromList(7));
      this.writeD(this.getFromList(10));
      this.writeD(this.getFromList(6));
      this.writeD(this.getFromList(11));
      this.writeD(this.getFromList(12));
      this.writeD(this.getFromList(23));
      this.writeD(this.getFromList(5));
      this.writeD(this.getFromList(2));
      this.writeD(this.getFromList(3));
      this.writeD(this.getFromList(16));
      this.writeD(this.getFromList(15));
   }

   private int getFromList(int key) {
      return this._itemlist.containsKey(key) ? this._itemlist.get(key) : 0;
   }
}
