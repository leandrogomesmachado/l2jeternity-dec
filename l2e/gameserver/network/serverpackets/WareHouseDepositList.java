package l2e.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;

public final class WareHouseDepositList extends GameServerPacket {
   public static final int PRIVATE = 1;
   public static final int CLAN = 4;
   public static final int CASTLE = 3;
   public static final int FREIGHT = 1;
   private final long _playerAdena;
   private final List<ItemInstance> _items = new ArrayList<>();
   private final int _whType;

   public WareHouseDepositList(Player player, int type) {
      this._whType = type;
      this._playerAdena = player.getAdena();
      boolean isPrivate = this._whType == 1;

      for(ItemInstance temp : player.getInventory().getAvailableItems(true, isPrivate, false)) {
         if (temp != null && temp.isDepositable(isPrivate)) {
            this._items.add(temp);
         }
      }
   }

   @Override
   protected final void writeImpl() {
      this.writeH(this._whType);
      this.writeQ(this._playerAdena);
      int count = this._items.size();
      this.writeH(count);

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

      this._items.clear();
   }
}
