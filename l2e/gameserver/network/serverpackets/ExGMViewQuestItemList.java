package l2e.gameserver.network.serverpackets;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;

public class ExGMViewQuestItemList extends GameServerPacket {
   private final int _size;
   private final ItemInstance[] _items;
   private final int _limit;
   private final String _name;

   public ExGMViewQuestItemList(Player player, ItemInstance[] items, int size) {
      this._items = items;
      this._size = size;
      this._name = player.getName();
      this._limit = Config.INVENTORY_MAXIMUM_QUEST_ITEMS;
   }

   @Override
   protected void writeImpl() {
      this.writeS(this._name);
      this.writeD(this._limit);
      this.writeH(this._size);

      for(ItemInstance temp : this._items) {
         if (temp.isQuestItem()) {
            this.writeItemInfo(temp);
         }
      }
   }
}
