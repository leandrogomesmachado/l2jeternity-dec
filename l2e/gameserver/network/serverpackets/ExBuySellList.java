package l2e.gameserver.network.serverpackets;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;

public class ExBuySellList extends GameServerPacket {
   private ItemInstance[] _sellList = null;
   private ItemInstance[] _refundList = null;
   private final boolean _done;

   public ExBuySellList(Player player, boolean done) {
      this._sellList = player.getInventory().getAvailableItems(false, false, false);
      if (player.hasRefund()) {
         this._refundList = player.getRefund().getItems();
      }

      this._done = done;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(1);
      if (this._sellList != null && this._sellList.length > 0) {
         this.writeH(this._sellList.length);

         for(ItemInstance item : this._sellList) {
            long sellPrice = (long)((double)(item.getItem().getReferencePrice() / 2) * Config.SELL_PRICE_MODIFIER);
            this.writeD(item.getObjectId());
            this.writeD(item.getDisplayId());
            this.writeD(item.getLocationSlot());
            this.writeQ(item.getCount());
            this.writeH(item.getItem().getType2());
            this.writeH(item.getCustomType1());
            this.writeH(0);
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

            this.writeQ(sellPrice);
         }
      } else {
         this.writeH(0);
      }

      if (this._refundList != null && this._refundList.length > 0) {
         this.writeH(this._refundList.length);
         int idx = 0;

         for(ItemInstance item : this._refundList) {
            long sellPrice = (long)((double)((long)(item.getItem().getReferencePrice() / 2) * item.getCount()) * Config.SELL_PRICE_MODIFIER);
            this.writeD(item.getObjectId());
            this.writeD(item.getDisplayId());
            this.writeD(0);
            this.writeQ(item.getCount());
            this.writeH(item.getItem().getType2());
            this.writeH(item.getCustomType1());
            this.writeH(0);
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

            this.writeD(idx++);
            this.writeQ(sellPrice);
         }
      } else {
         this.writeH(0);
      }

      this.writeC(this._done ? 1 : 0);
   }
}
