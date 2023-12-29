package l2e.gameserver.network.clientpackets;

import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.PetInstance;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;

public final class RequestGiveItemToPet extends GameClientPacket {
   private int _objectId;
   private long _amount;

   @Override
   protected void readImpl() {
      this._objectId = this.readD();
      long amount = this.readQ();
      if (amount < 0L) {
         Util.handleIllegalPlayerAction(this.getClient().getActiveChar(), "" + this.getClient().getActiveChar().getName() + " tried an overflow exploit!");
         this._amount = 0L;
      } else {
         this._amount = amount;
      }
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (this._amount > 0L && player != null && player.hasPet()) {
         if (player.getActiveEnchantItemId() == -1) {
            if (Config.ALT_GAME_KARMA_PLAYER_CAN_TRADE || player.getKarma() <= 0) {
               if (player.getPrivateStoreType() != 0) {
                  player.sendMessage("You cannot exchange items while trading.");
               } else if (player.isOutOfControl()) {
                  player.sendActionFailed();
               } else if (player.isInStoreMode()) {
                  player.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
               } else if (player.isProcessingRequest()) {
                  player.sendActionFailed();
               } else if (player.isFishing()) {
                  player.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_2);
               } else {
                  ItemInstance item = player.getInventory().getItemByObjectId(this._objectId);
                  if (item != null) {
                     if (this._amount > item.getCount()) {
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
                     } else if (!item.isAugmented()) {
                        if (!item.isHeroItem() && item.isDropable() && item.isDestroyable() && item.isTradeable()) {
                           PetInstance pet = (PetInstance)player.getSummon();
                           if (pet.isDead()) {
                              player.sendPacket(SystemMessageId.CANNOT_GIVE_ITEMS_TO_DEAD_PET);
                           } else if (!pet.getInventory().validateCapacity(item)) {
                              player.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS);
                           } else if (!pet.getInventory().validateWeight(item, this._amount)) {
                              player.sendPacket(SystemMessageId.UNABLE_TO_PLACE_ITEM_YOUR_PET_IS_TOO_ENCUMBERED);
                           } else {
                              ItemInstance petItem = pet.getControlItem();
                              if (petItem != null && this._objectId == petItem.getObjectId()) {
                                 player.sendActionFailed();
                              } else if (Util.calculateDistance(player, pet, true) > 600.0) {
                                 player.sendPacket(SystemMessageId.TARGET_TOO_FAR);
                              } else {
                                 if (player.transferItem("Transfer", this._objectId, this._amount, pet.getInventory(), pet) == null) {
                                    _log.warning("Invalid item transfer request: " + pet.getName() + "(pet) --> " + player.getName());
                                 }
                              }
                           }
                        } else {
                           player.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
