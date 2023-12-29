package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExPutCommissionResultForVariationMake;

public final class RequestConfirmGemStone extends AbstractRefinePacket {
   private int _targetItemObjId;
   private int _refinerItemObjId;
   private int _gemstoneItemObjId;
   private long _gemStoneCount;

   @Override
   protected void readImpl() {
      this._targetItemObjId = this.readD();
      this._refinerItemObjId = this.readD();
      this._gemstoneItemObjId = this.readD();
      this._gemStoneCount = this.readQ();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(this._targetItemObjId);
         if (targetItem != null) {
            ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(this._refinerItemObjId);
            if (refinerItem != null) {
               ItemInstance gemStoneItem = activeChar.getInventory().getItemByObjectId(this._gemstoneItemObjId);
               if (gemStoneItem != null) {
                  if (!isValid(activeChar, targetItem, refinerItem, gemStoneItem)) {
                     activeChar.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
                  } else {
                     AbstractRefinePacket.LifeStone ls = getLifeStone(refinerItem.getId());
                     if (ls != null) {
                        if (this._gemStoneCount != (long)getGemStoneCount(targetItem.getItem().getItemGrade(), ls.getGrade())) {
                           activeChar.sendPacket(SystemMessageId.GEMSTONE_QUANTITY_IS_INCORRECT);
                        } else {
                           activeChar.sendPacket(new ExPutCommissionResultForVariationMake(this._gemstoneItemObjId, this._gemStoneCount, gemStoneItem.getId()));
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
