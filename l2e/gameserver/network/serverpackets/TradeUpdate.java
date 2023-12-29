package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.TradeItem;
import l2e.gameserver.model.actor.Player;

public class TradeUpdate extends GameServerPacket {
   private final TradeItem _item;
   private final long _count;

   public TradeUpdate(Player player, TradeItem item) {
      this._item = item;
      this._count = player.getInventory().getItemByObjectId(item.getObjectId()).getCount() - item.getCount();
   }

   @Override
   protected final void writeImpl() {
      this.writeH(1);
      this.writeH(this._count > 0L && this._item.getItem().isStackable() ? 3 : 2);
      this.writeH(this._item.getItem().getType1());
      this.writeD(this._item.getObjectId());
      this.writeD(this._item.getItem().getDisplayId());
      this.writeQ(this._count);
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
