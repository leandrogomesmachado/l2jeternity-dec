package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExPutItemResultForVariationMake;

public final class RequestConfirmTargetItem extends AbstractRefinePacket {
   private int _itemObjId;

   @Override
   protected void readImpl() {
      this._itemObjId = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         ItemInstance item = activeChar.getInventory().getItemByObjectId(this._itemObjId);
         if (item != null) {
            if (!isValid(activeChar, item)) {
               if (item.isAugmented()) {
                  activeChar.sendPacket(SystemMessageId.ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN);
               } else {
                  activeChar.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
               }
            } else {
               activeChar.sendPacket(new ExPutItemResultForVariationMake(this._itemObjId, item.getId()));
            }
         }
      }
   }
}
