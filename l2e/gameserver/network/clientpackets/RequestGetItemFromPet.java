package l2e.gameserver.network.clientpackets;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.PetInstance;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;

public final class RequestGetItemFromPet extends GameClientPacket {
   private int _objectId;
   private long _amount;
   protected int _unknown;

   @Override
   protected void readImpl() {
      this._objectId = this.readD();
      long amount = this.readQ();
      this._unknown = this.readD();
      if (amount < 0L) {
         Util.handleIllegalPlayerAction(this.getClient().getActiveChar(), "" + this.getClient().getActiveChar().getName() + " tried an overflow exploit!");
         this._amount = 0L;
      }

      this._amount = amount;
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (this._amount > 0L && player != null && player.hasPet()) {
         if (player.isInStoreMode()) {
            player.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
         } else if (player.isProcessingRequest()) {
            player.sendActionFailed();
         } else if (player.isFishing()) {
            player.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_2);
         } else {
            PetInstance pet = (PetInstance)player.getSummon();
            if (player.getActiveEnchantItemId() == -1) {
               if (Util.calculateDistance(player, pet, true) > 600.0) {
                  player.sendPacket(SystemMessageId.TARGET_TOO_FAR);
               } else {
                  ItemInstance item = pet.getInventory().getItemByObjectId(this._objectId);
                  if (item == null || item.getCount() < this._amount || item.isEquipped()) {
                     player.sendActionFailed();
                  } else if (this._amount > item.getCount()) {
                     Util.handleIllegalPlayerAction(
                        player,
                        ""
                           + player.getName()
                           + " of account "
                           + player.getAccountName()
                           + " tried to get item with oid "
                           + this._objectId
                           + " from pet but has invalid count "
                           + this._amount
                           + " item count: "
                           + item.getCount()
                     );
                  } else if (this._objectId == pet.getControlItem().getObjectId()) {
                     player.sendActionFailed();
                  } else {
                     if (pet.transferItem("Transfer", this._objectId, this._amount, player.getInventory(), player, pet) == null) {
                        _log.warning("Invalid item transfer request: " + pet.getName() + "(pet) --> " + player.getName());
                     }
                  }
               }
            }
         }
      }
   }
}
