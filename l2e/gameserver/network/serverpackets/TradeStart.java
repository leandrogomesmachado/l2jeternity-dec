package l2e.gameserver.network.serverpackets;

import l2e.gameserver.Config;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;

public final class TradeStart extends GameServerPacket {
   private final Player _activeChar;
   private final ItemInstance[] _itemList;

   public TradeStart(Player player) {
      this._activeChar = player;
      this._itemList = this._activeChar
         .getInventory()
         .getAvailableItems(true, this._activeChar.canOverrideCond(PcCondOverride.ITEM_CONDITIONS) && Config.GM_TRADE_RESTRICTED_ITEMS, false);
   }

   @Override
   protected final void writeImpl() {
      if (this._activeChar.getActiveTradeList() != null && this._activeChar.getActiveTradeList().getPartner() != null) {
         this.writeD(this._activeChar.getActiveTradeList().getPartner().getObjectId());
         this.writeH(this._itemList.length);

         for(ItemInstance item : this._itemList) {
            this.writeD(item.getObjectId());
            this.writeD(item.getItem().getDisplayId());
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
         }
      }
   }
}
