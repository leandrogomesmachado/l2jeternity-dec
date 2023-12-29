package l2e.gameserver.network.clientpackets;

import l2e.commons.util.Util;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.PremiumItemTemplate;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExGetPremiumItemList;

public final class RequestWithDrawPremiumItem extends GameClientPacket {
   private int _itemNum;
   private int _charId;
   private long _itemCount;

   @Override
   protected void readImpl() {
      this._itemNum = this.readD();
      this._charId = this.readD();
      this._itemCount = this.readQ();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (this._itemCount > 0L) {
            if (activeChar.getObjectId() != this._charId) {
               Util.handleIllegalPlayerAction(activeChar, "Incorrect owner, " + activeChar.getName());
            } else if (activeChar.getPremiumItemList().isEmpty()) {
               Util.handleIllegalPlayerAction(activeChar, "" + activeChar.getName() + " try to get item with empty list!");
            } else if (activeChar.getWeightPenalty() >= 3 || !activeChar.isInventoryUnder90(false)) {
               activeChar.sendPacket(SystemMessageId.YOU_CANNOT_RECEIVE_THE_VITAMIN_ITEM);
            } else if (activeChar.isProcessingTransaction()) {
               activeChar.sendPacket(SystemMessageId.YOU_CANNOT_RECEIVE_A_VITAMIN_ITEM_DURING_AN_EXCHANGE);
            } else {
               PremiumItemTemplate _item = activeChar.getPremiumItemList().get(this._itemNum);
               if (_item != null) {
                  if (_item.getCount() >= this._itemCount) {
                     long itemsLeft = _item.getCount() - this._itemCount;
                     activeChar.addItem("PremiumItem", _item.getId(), this._itemCount, activeChar.getTarget(), true);
                     if (itemsLeft > 0L) {
                        _item.updateCount(itemsLeft);
                        activeChar.updatePremiumItem(this._itemNum, itemsLeft);
                     } else {
                        activeChar.getPremiumItemList().remove(this._itemNum);
                        activeChar.deletePremiumItem(this._itemNum);
                     }

                     if (activeChar.getPremiumItemList().isEmpty()) {
                        activeChar.sendPacket(SystemMessageId.THERE_ARE_NO_MORE_VITAMIN_ITEMS_TO_BE_FOUND);
                     } else {
                        activeChar.sendPacket(new ExGetPremiumItemList(activeChar));
                     }
                  }
               }
            }
         }
      }
   }
}
