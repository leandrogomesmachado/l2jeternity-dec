package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.TradeItem;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;

public class PrivateStoreBuyManageList extends GameServerPacket {
   private final int _objId;
   private final long _playerAdena;
   private final ItemInstance[] _itemList;
   private final TradeItem[] _buyList;

   public PrivateStoreBuyManageList(Player player) {
      this._objId = player.getObjectId();
      this._playerAdena = player.getAdena();
      this._itemList = player.getInventory().getUniqueItems(false, true);
      this._buyList = player.getBuyList().getItems();
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._objId);
      this.writeQ(this._playerAdena);
      this.writeD(this._itemList.length);

      for(ItemInstance item : this._itemList) {
         this.writeD(item.getObjectId());
         this.writeD(item.getItem().getDisplayId());
         this.writeD(item.getLocationSlot());
         this.writeQ(item.getCount());
         this.writeH(item.getItem().getType2());
         this.writeH(item.getCustomType1());
         this.writeH(item.isEquipped() ? 1 : 0);
         this.writeD(item.getItem().getBodyPart());
         this.writeH(item.getEnchantLevel());
         this.writeH(item.getCustomType2());
         if (item.isAugmented()) {
            this.writeD(item.getAugmentation().getAugmentationId());
         } else {
            this.writeD(0);
         }

         this.writeD(item.getMana());
         this.writeD(item.isTimeLimitedItem() ? (int)(item.getRemainingTime() / 1000L) : -9999);
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

      this.writeD(this._buyList.length);

      for(TradeItem item : this._buyList) {
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
         this.writeQ(item.getCount());
      }
   }
}
