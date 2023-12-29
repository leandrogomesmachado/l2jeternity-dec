package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;

public class PackageSendableList extends GameServerPacket {
   private final ItemInstance[] _items;
   private final int _playerObjId;

   public PackageSendableList(ItemInstance[] items, int playerObjId) {
      this._items = items;
      this._playerObjId = playerObjId;
   }

   @Override
   protected void writeImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         this.writeD(this._playerObjId);
         this.writeQ(activeChar.getAdena());
         this.writeD(this._items.length);

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
}
