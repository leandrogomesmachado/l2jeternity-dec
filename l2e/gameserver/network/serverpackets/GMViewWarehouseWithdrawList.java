package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.Clan;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;

public class GMViewWarehouseWithdrawList extends GameServerPacket {
   private final ItemInstance[] _items;
   private final String _playerName;
   private Player _activeChar;
   private final long _money;

   public GMViewWarehouseWithdrawList(Player cha) {
      this._activeChar = cha;
      this._items = this._activeChar.getWarehouse().getItems();
      this._playerName = this._activeChar.getName();
      this._money = this._activeChar.getWarehouse().getAdena();
   }

   public GMViewWarehouseWithdrawList(Clan clan) {
      this._playerName = clan.getLeaderName();
      this._items = clan.getWarehouse().getItems();
      this._money = clan.getWarehouse().getAdena();
   }

   @Override
   protected final void writeImpl() {
      this.writeS(this._playerName);
      this.writeQ(this._money);
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
   }
}
