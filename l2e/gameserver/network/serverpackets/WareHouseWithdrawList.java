package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;

public final class WareHouseWithdrawList extends GameServerPacket {
   public static final int PRIVATE = 1;
   public static final int CLAN = 4;
   public static final int CASTLE = 3;
   public static final int FREIGHT = 1;
   private Player _activeChar;
   private long _playerAdena;
   private ItemInstance[] _items;
   private int _whType;
   private int agathionItems = 0;

   public WareHouseWithdrawList(Player player, int type) {
      this._activeChar = player;
      this._whType = type;
      this._playerAdena = this._activeChar.getAdena();
      if (this._activeChar.getActiveWarehouse() == null) {
         _log.warning("error while sending withdraw request to: " + this._activeChar.getName());
      } else {
         this._items = this._activeChar.getActiveWarehouse().getItems();

         for(ItemInstance item : this._items) {
            if (item.isEnergyItem()) {
               ++this.agathionItems;
            }
         }
      }
   }

   @Override
   protected final void writeImpl() {
      this.writeH(this._whType);
      this.writeQ(this._playerAdena);
      this.writeH(this._items.length);

      for(ItemInstance item : this._items) {
         this.writeD(item.getObjectId());
         this.writeD(item.getDisplayId());
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

         this.writeD(item.getObjectId());
      }

      if (this._activeChar != null && this.agathionItems > 0) {
         this._activeChar.sendPacket(new ExBrAgathionEnergyInfo(this.agathionItems, this._items));
      }
   }
}
