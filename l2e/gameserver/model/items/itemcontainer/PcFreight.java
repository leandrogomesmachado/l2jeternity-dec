package l2e.gameserver.model.items.itemcontainer;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.stats.Stats;

public class PcFreight extends ItemContainer {
   private final Player _owner;
   private final int _ownerId;

   public PcFreight(int object_id) {
      this._owner = null;
      this._ownerId = object_id;
      this.restore();
   }

   public PcFreight(Player owner) {
      this._owner = owner;
      this._ownerId = owner.getObjectId();
   }

   @Override
   public int getOwnerId() {
      return this._ownerId;
   }

   public Player getOwner() {
      return this._owner;
   }

   @Override
   public ItemInstance.ItemLocation getBaseLocation() {
      return ItemInstance.ItemLocation.FREIGHT;
   }

   @Override
   public String getName() {
      return "Freight";
   }

   @Override
   public boolean validateCapacity(long slots) {
      int curSlots = this._owner == null
         ? Config.ALT_FREIGHT_SLOTS
         : Config.ALT_FREIGHT_SLOTS + (int)this._owner.getStat().calcStat(Stats.FREIGHT_LIM, 0.0, null, null);
      return (long)this.getSize() + slots <= (long)curSlots;
   }

   @Override
   public void refreshWeight() {
   }
}
