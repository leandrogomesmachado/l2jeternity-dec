package l2e.gameserver.network.clientpackets;

import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.handler.itemhandlers.ItemHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.PetInstance;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.PetItemList;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class RequestPetUseItem extends GameClientPacket {
   private int _objectId;

   @Override
   protected void readImpl() {
      this._objectId = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null && activeChar.hasPet()) {
         if (activeChar.isActionsDisabled()) {
            activeChar.sendActionFailed();
         } else {
            PetInstance pet = (PetInstance)activeChar.getSummon();
            ItemInstance item = pet.getInventory().getItemByObjectId(this._objectId);
            if (item != null) {
               if (!item.getItem().isForNpc()) {
                  activeChar.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
               } else if (!activeChar.isAlikeDead() && !pet.isDead()) {
                  int reuseDelay = item.getReuseDelay();
                  if (reuseDelay > 0) {
                     long reuse = pet.getItemRemainingReuseTime(item.getObjectId());
                     if (reuse > 0L) {
                        return;
                     }
                  }

                  if (item.isEquipped() || item.getItem().checkCondition(pet, pet, true)) {
                     this.useItem(pet, item, activeChar);
                  }
               } else {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
                  sm.addItemName(item);
                  activeChar.sendPacket(sm);
               }
            }
         }
      }
   }

   private void useItem(PetInstance pet, ItemInstance item, Player activeChar) {
      if (item.isEquipable()) {
         if (!item.getItem().isConditionAttached()) {
            activeChar.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
            return;
         }

         if (item.isEquipped()) {
            pet.getInventory().unEquipItemInSlot(item.getLocationSlot());
         } else {
            pet.getInventory().equipItem(item);
         }

         activeChar.sendPacket(new PetItemList(pet.getInventory().getItems()));
         pet.updateAndBroadcastStatus(1);
      } else {
         IItemHandler handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
         if (handler != null) {
            if (handler.useItem(pet, item, false)) {
               int reuseDelay = item.getReuseDelay();
               if (reuseDelay > 0) {
                  activeChar.addTimeStampItem(item, (long)reuseDelay, item.isReuseByCron());
               }

               pet.updateAndBroadcastStatus(1);
            }
         } else {
            activeChar.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM);
            _log.warning("No item handler registered for itemId: " + item.getId());
         }
      }
   }
}
