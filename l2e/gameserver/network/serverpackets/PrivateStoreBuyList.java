package l2e.gameserver.network.serverpackets;

import java.util.List;
import l2e.gameserver.model.TradeItem;
import l2e.gameserver.model.actor.Player;

public class PrivateStoreBuyList extends GameServerPacket {
   private final int _objId;
   private final long _playerAdena;
   private final List<TradeItem> _items;

   public PrivateStoreBuyList(Player player, Player storePlayer) {
      this._objId = storePlayer.getObjectId();
      this._playerAdena = player.getAdena();
      storePlayer.getSellList().updateItems();
      this._items = storePlayer.getBuyList().getAvailableItems(player.getInventory());
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._objId);
      this.writeQ(this._playerAdena);
      this.writeD(this._items.size());

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

         this.writeD(item.getObjectId());
         this.writeQ(item.getPrice());
         this.writeQ((long)(item.getItem().getReferencePrice() * 2));
         this.writeQ(item.getStoreCount());
      }
   }
}
