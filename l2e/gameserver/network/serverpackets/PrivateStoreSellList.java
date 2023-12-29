package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.TradeItem;
import l2e.gameserver.model.actor.Player;

public class PrivateStoreSellList extends GameServerPacket {
   private final int _objId;
   private final long _playerAdena;
   private final boolean _packageSale;
   private final TradeItem[] _items;

   public PrivateStoreSellList(Player player, Player storePlayer) {
      this._objId = storePlayer.getObjectId();
      this._playerAdena = player.getAdena();
      this._items = storePlayer.getSellList().getItems();
      this._packageSale = storePlayer.getSellList().isPackaged();
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._objId);
      this.writeD(this._packageSale ? 1 : 0);
      this.writeQ(this._playerAdena);
      this.writeD(this._items.length);

      for(TradeItem item : this._items) {
         this.writeD(item.getObjectId());
         this.writeD(item.getItem().getDisplayId());
         this.writeD(item.getLocationSlot());
         this.writeQ(item.getCount());
         this.writeH(item.getItem().getType2());
         this.writeH(item.getCustomType1());
         this.writeH(0);
         this.writeD(item.getItem().getBodyPart());
         this.writeH(item.getEnchant());
         this.writeH(item.getCustomType2());
         this.writeD(0);
         this.writeD(-1);
         this.writeD(-9999);
         this.writeH(item.getAttackElementType());
         this.writeH(item.getAttackElementPower());

         for(byte i = 0; i < 6; ++i) {
            this.writeH(item.getElementDefAttr(i));
         }

         for(int op : item.getEnchantOptions()) {
            this.writeH(op);
         }

         this.writeQ(item.getPrice());
         this.writeQ((long)(item.getItem().getReferencePrice() * 2));
      }
   }
}
