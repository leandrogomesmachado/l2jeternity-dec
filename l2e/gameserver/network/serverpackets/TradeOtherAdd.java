package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.TradeItem;

public final class TradeOtherAdd extends GameServerPacket {
   private final TradeItem _item;

   public TradeOtherAdd(TradeItem item) {
      this._item = item;
   }

   @Override
   protected final void writeImpl() {
      this.writeH(1);
      this.writeH(this._item.getItem().getType1());
      this.writeD(this._item.getObjectId());
      this.writeD(this._item.getItem().getDisplayId());
      this.writeQ(this._item.getCount());
      this.writeH(this._item.getItem().getType2());
      this.writeH(this._item.getCustomType1());
      this.writeD(this._item.getItem().getBodyPart());
      this.writeH(this._item.getEnchant());
      this.writeH(0);
      this.writeH(this._item.getCustomType2());
      this.writeH(this._item.getAttackElementType());
      this.writeH(this._item.getAttackElementPower());

      for(byte i = 0; i < 6; ++i) {
         this.writeH(this._item.getElementDefAttr(i));
      }

      this.writeH(0);
      this.writeH(0);
      this.writeH(0);
   }
}
