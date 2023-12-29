package l2e.gameserver.network.clientpackets;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.ShortcutType;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExVariationCancelResult;
import l2e.gameserver.network.serverpackets.InventoryUpdate;

public final class RequestRefineCancel extends GameClientPacket {
   private int _targetItemObjId;

   @Override
   protected void readImpl() {
      this._targetItemObjId = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(this._targetItemObjId);
         if (targetItem == null) {
            activeChar.sendPacket(new ExVariationCancelResult(0));
         } else if (activeChar.isActionsDisabled()) {
            activeChar.sendPacket(new ExVariationCancelResult(0));
         } else if (targetItem.getOwnerId() != activeChar.getObjectId()) {
            Util.handleIllegalPlayerAction(
               this.getClient().getActiveChar(),
               ""
                  + this.getClient().getActiveChar().getName()
                  + " of account "
                  + this.getClient().getActiveChar().getAccountName()
                  + " tryied to augment item that doesn't own."
            );
         } else if (!targetItem.isAugmented()) {
            activeChar.sendPacket(SystemMessageId.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM);
            activeChar.sendPacket(new ExVariationCancelResult(0));
         } else {
            int price = 0;
            switch(targetItem.getItem().getCrystalType()) {
               case 2:
                  if (targetItem.getCrystalCount() < 1720) {
                     price = 95000;
                  } else if (targetItem.getCrystalCount() < 2452) {
                     price = 150000;
                  } else {
                     price = 210000;
                  }
                  break;
               case 3:
                  if (targetItem.getCrystalCount() < 1746) {
                     price = 240000;
                  } else {
                     price = 270000;
                  }
                  break;
               case 4:
                  if (targetItem.getCrystalCount() < 2160) {
                     price = 330000;
                  } else if (targetItem.getCrystalCount() < 2824) {
                     price = 390000;
                  } else {
                     price = 420000;
                  }
                  break;
               case 5:
                  price = 480000;
                  break;
               case 6:
               case 7:
                  price = 920000;
                  break;
               default:
                  activeChar.sendPacket(new ExVariationCancelResult(0));
                  return;
            }

            if (!activeChar.reduceAdena("RequestRefineCancel", (long)price, null, true)) {
               activeChar.sendPacket(new ExVariationCancelResult(0));
               activeChar.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
            } else {
               boolean equipped = targetItem.isEquipped();
               if (equipped) {
                  activeChar.getInventory().unEquipItemInSlotAndRecord(targetItem.getLocationSlot());
               }

               targetItem.removeAugmentation();
               activeChar.sendPacket(new ExVariationCancelResult(1));
               if (equipped) {
                  activeChar.getInventory().equipItem(targetItem);
               }

               InventoryUpdate iu = new InventoryUpdate();
               iu.addModifiedItem(targetItem);
               activeChar.sendPacket(iu);
               activeChar.broadcastUserInfo(true);
               activeChar.updateShortCuts(targetItem.getObjectId(), ShortcutType.ITEM);
            }
         }
      }
   }
}
