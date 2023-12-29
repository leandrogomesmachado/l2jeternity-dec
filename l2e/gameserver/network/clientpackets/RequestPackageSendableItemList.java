package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.serverpackets.PackageSendableList;

public class RequestPackageSendableItemList extends GameClientPacket {
   private int _objectID;

   @Override
   protected void readImpl() {
      this._objectID = this.readD();
   }

   @Override
   public void runImpl() {
      ItemInstance[] items = this.getClient().getActiveChar().getInventory().getAvailableItems(true, true, true);
      this.sendPacket(new PackageSendableList(items, this._objectID));
   }
}
