package l2e.gameserver.network.clientpackets;

import l2e.gameserver.data.parser.AugmentationParser;
import l2e.gameserver.model.Augmentation;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.base.ShortcutType;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExVariationResult;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.StatusUpdate;

public final class RequestRefine extends AbstractRefinePacket {
   private int _targetItemObjId;
   private int _refinerItemObjId;
   private int _gemStoneItemObjId;
   private long _gemStoneCount;

   @Override
   protected void readImpl() {
      this._targetItemObjId = this.readD();
      this._refinerItemObjId = this.readD();
      this._gemStoneItemObjId = this.readD();
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
               ItemInstance gemStoneItem = activeChar.getInventory().getItemByObjectId(this._gemStoneItemObjId);
               if (gemStoneItem != null) {
                  if (!isValid(activeChar, targetItem, refinerItem, gemStoneItem)) {
                     activeChar.sendPacket(new ExVariationResult(0, 0, 0));
                     activeChar.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
                  } else {
                     AbstractRefinePacket.LifeStone ls = getLifeStone(refinerItem.getId());
                     if (ls != null) {
                        int lifeStoneLevel = ls.getLevel();
                        int lifeStoneGrade = ls.getGrade();
                        if (this._gemStoneCount != (long)getGemStoneCount(targetItem.getItem().getItemGrade(), lifeStoneGrade)) {
                           activeChar.sendPacket(new ExVariationResult(0, 0, 0));
                           activeChar.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
                        } else {
                           boolean equipped = targetItem.isEquipped();
                           if (equipped) {
                              ItemInstance[] unequiped = activeChar.getInventory().unEquipItemInSlotAndRecord(targetItem.getLocationSlot());
                              InventoryUpdate iu = new InventoryUpdate();

                              for(ItemInstance itm : unequiped) {
                                 iu.addModifiedItem(itm);
                              }

                              activeChar.sendPacket(iu);
                              activeChar.broadcastCharInfo();
                           }

                           if (activeChar.destroyItem("RequestRefine", refinerItem, 1L, null, false)) {
                              if (activeChar.destroyItem("RequestRefine", gemStoneItem, this._gemStoneCount, null, false)) {
                                 Augmentation aug = AugmentationParser.getInstance()
                                    .generateRandomAugmentation(
                                       lifeStoneLevel, lifeStoneGrade, targetItem.getItem().getBodyPart(), refinerItem.getId(), targetItem
                                    );
                                 targetItem.setAugmentation(aug);
                                 int stat12 = 65535 & aug.getAugmentationId();
                                 int stat34 = aug.getAugmentationId() >> 16;
                                 activeChar.sendPacket(new ExVariationResult(stat12, stat34, 1));
                                 if (equipped) {
                                    activeChar.getInventory().equipItem(targetItem);
                                 }

                                 InventoryUpdate iu = new InventoryUpdate();
                                 iu.addModifiedItem(targetItem);
                                 activeChar.sendPacket(iu);
                                 StatusUpdate su = new StatusUpdate(activeChar);
                                 su.addAttribute(14, activeChar.getCurrentLoad());
                                 activeChar.sendPacket(su);
                                 activeChar.updateShortCuts(targetItem.getObjectId(), ShortcutType.ITEM);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
