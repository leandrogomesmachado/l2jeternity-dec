package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.TradeItem;
import l2e.gameserver.model.actor.Player;

public class PrivateStoreSellManageList extends GameServerPacket {
   private final int _objId;
   private final long _playerAdena;
   private final boolean _packageSale;
   private final TradeItem[] _itemList;
   private final TradeItem[] _sellList;

   public PrivateStoreSellManageList(Player player, boolean isPackageSale) {
      this._objId = player.getObjectId();
      this._playerAdena = player.getAdena();
      player.getSellList().updateItems();
      this._packageSale = isPackageSale;
      this._itemList = player.getInventory().getAvailableItems(player.getSellList());
      this._sellList = player.getSellList().getItems();
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._objId);
      this.writeD(this._packageSale ? 1 : 0);
      this.writeQ(this._playerAdena);
      this.writeD(this._itemList.length);

      for(TradeItem item : this._itemList) {
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

         this.writeQ((long)(item.getItem().getReferencePrice() * 2));
      }

      this.writeD(this._sellList.length);

      for(TradeItem item : this._sellList) {
         this.writeD(item.getObjectId());
         this.writeD(item.getItem().getId());
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

         this.writeH(0);
         this.writeH(0);
         this.writeH(0);
         this.writeQ(item.getPrice());
         this.writeQ((long)(item.getItem().getReferencePrice() * 2));
      }
   }
}
