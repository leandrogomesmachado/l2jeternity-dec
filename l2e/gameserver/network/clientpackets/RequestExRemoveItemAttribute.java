package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.Elementals;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Weapon;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExBaseAttributeCancelResult;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class RequestExRemoveItemAttribute extends GameClientPacket {
   private int _objectId;
   private long _price;
   private byte _element;

   @Override
   public void readImpl() {
      this._objectId = this.readD();
      this._element = (byte)this.readD();
   }

   @Override
   public void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(this._objectId);
         if (targetItem != null) {
            if (activeChar.isActionsDisabled() || activeChar.isInStoreMode() || activeChar.getActiveTradeList() != null) {
               activeChar.sendActionFailed();
            } else if (targetItem.getElementals() != null && targetItem.getElemental(this._element) != null) {
               if (activeChar.reduceAdena("RemoveElement", this.getPrice(targetItem), activeChar, true)) {
                  if (targetItem.isEquipped()) {
                     targetItem.getElemental(this._element).removeBonus(activeChar);
                  }

                  targetItem.clearElementAttr(this._element);
                  activeChar.sendUserInfo();
                  InventoryUpdate iu = new InventoryUpdate();
                  iu.addModifiedItem(targetItem);
                  activeChar.sendPacket(iu);
                  byte realElement = targetItem.isArmor() ? Elementals.getOppositeElement(this._element) : this._element;
                  SystemMessage sm;
                  if (targetItem.getEnchantLevel() > 0) {
                     if (targetItem.isArmor()) {
                        sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2_S3_ATTRIBUTE_REMOVED_RESISTANCE_TO_S4_DECREASED);
                     } else {
                        sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2_ELEMENTAL_POWER_REMOVED);
                     }

                     sm.addNumber(targetItem.getEnchantLevel());
                     sm.addItemName(targetItem);
                     if (targetItem.isArmor()) {
                        sm.addElemental(realElement);
                        sm.addElemental(Elementals.getOppositeElement(realElement));
                     }
                  } else {
                     if (targetItem.isArmor()) {
                        sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2_ATTRIBUTE_REMOVED_RESISTANCE_S3_DECREASED);
                     } else {
                        sm = SystemMessage.getSystemMessage(SystemMessageId.S1_ELEMENTAL_POWER_REMOVED);
                     }

                     sm.addItemName(targetItem);
                     if (targetItem.isArmor()) {
                        sm.addElemental(realElement);
                        sm.addElemental(Elementals.getOppositeElement(realElement));
                     }
                  }

                  activeChar.sendPacket(sm);
                  activeChar.sendPacket(new ExBaseAttributeCancelResult(targetItem.getObjectId(), this._element));
               } else {
                  activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_FUNDS_TO_CANCEL_ATTRIBUTE);
               }
            }
         }
      }
   }

   private long getPrice(ItemInstance item) {
      switch(item.getItem().getCrystalType()) {
         case 5:
            if (item.getItem() instanceof Weapon) {
               this._price = 50000L;
            } else {
               this._price = 40000L;
            }
            break;
         case 6:
            if (item.getItem() instanceof Weapon) {
               this._price = 100000L;
            } else {
               this._price = 80000L;
            }
            break;
         case 7:
            if (item.getItem() instanceof Weapon) {
               this._price = 200000L;
            } else {
               this._price = 160000L;
            }
      }

      return this._price;
   }
}
