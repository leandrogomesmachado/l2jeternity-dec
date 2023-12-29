package l2e.gameserver.network.clientpackets;

import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExPutItemResultForVariationCancel;

public final class RequestConfirmCancelItem extends GameClientPacket {
   private int _objectId;

   @Override
   protected void readImpl() {
      this._objectId = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         ItemInstance item = activeChar.getInventory().getItemByObjectId(this._objectId);
         if (item != null) {
            if (item.getOwnerId() != activeChar.getObjectId()) {
               Util.handleIllegalPlayerAction(
                  this.getClient().getActiveChar(),
                  ""
                     + this.getClient().getActiveChar().getName()
                     + " of account "
                     + this.getClient().getActiveChar().getAccountName()
                     + " tryied to destroy augment on item that doesn't own."
               );
            } else if (!item.isAugmented()) {
               activeChar.sendPacket(SystemMessageId.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM);
            } else if (item.isPvp() && !Config.ALT_ALLOW_AUGMENT_PVP_ITEMS) {
               activeChar.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
            } else {
               int price = 0;
               switch(item.getItem().getCrystalType()) {
                  case 2:
                     if (item.getCrystalCount() < 1720) {
                        price = 95000;
                     } else if (item.getCrystalCount() < 2452) {
                        price = 150000;
                     } else {
                        price = 210000;
                     }
                     break;
                  case 3:
                     if (item.getCrystalCount() < 1746) {
                        price = 240000;
                     } else {
                        price = 270000;
                     }
                     break;
                  case 4:
                     if (item.getCrystalCount() < 2160) {
                        price = 330000;
                     } else if (item.getCrystalCount() < 2824) {
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
                     return;
               }

               activeChar.sendPacket(new ExPutItemResultForVariationCancel(item, price));
            }
         }
      }
   }
}
