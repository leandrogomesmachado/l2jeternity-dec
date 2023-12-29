package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExPutIntensiveResultForVariationMake;

public class RequestConfirmRefinerItem extends AbstractRefinePacket {
   private int _targetItemObjId;
   private int _refinerItemObjId;

   @Override
   protected void readImpl() {
      this._targetItemObjId = this.readD();
      this._refinerItemObjId = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(this._targetItemObjId);
         if (targetItem != null) {
            ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(this._refinerItemObjId);
            if (refinerItem != null) {
               if (!isValid(activeChar, targetItem, refinerItem)) {
                  activeChar.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
               } else {
                  int refinerItemId = refinerItem.getItem().getId();
                  int grade = targetItem.getItem().getItemGrade();
                  AbstractRefinePacket.LifeStone ls = getLifeStone(refinerItemId);
                  int gemStoneId = getGemStoneId(grade);
                  int gemStoneCount = getGemStoneCount(grade, ls.getGrade());
                  activeChar.sendPacket(new ExPutIntensiveResultForVariationMake(this._refinerItemObjId, refinerItemId, gemStoneId, gemStoneCount));
               }
            }
         }
      }
   }
}
