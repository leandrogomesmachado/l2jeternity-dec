package l2e.gameserver.network.serverpackets;

import l2e.gameserver.Config;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.items.instance.ItemInstance;

public final class SpawnItem extends GameServerPacket {
   private final int _objectId;
   private int _itemId;
   private final int _x;
   private final int _y;
   private final int _z;
   private int _stackable;
   private long _count;

   public SpawnItem(GameObject obj) {
      this._objectId = obj.getObjectId();
      this._x = obj.getX();
      this._y = obj.getY();
      this._z = obj.getZ();
      if (obj instanceof ItemInstance) {
         ItemInstance item = (ItemInstance)obj;
         this._itemId = item.getDisplayId();
         this._stackable = item.isStackable() ? 1 : 0;
         this._count = item.getCount();
      } else {
         this._itemId = obj.getPoly().getPolyId();
         this._stackable = 0;
         this._count = 1L;
      }
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._objectId);
      this.writeD(this._itemId);
      this.writeD(this._x);
      this.writeD(this._y);
      this.writeD(this._z - Config.CLIENT_SHIFTZ);
      this.writeD(this._stackable);
      this.writeQ(this._count);
      this.writeD(0);
      this.writeD(0);
   }
}
