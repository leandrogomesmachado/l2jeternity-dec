package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.EtcItem;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.InventoryUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class RequestUnEquipItem extends GameClientPacket {
   private int _slot;

   @Override
   protected void readImpl() {
      this._slot = this.readD();
   }

   @Override
   protected void runImpl() {
      if (Config.DEBUG) {
         _log.fine("Request unequip slot " + this._slot);
      }

      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         ItemInstance item = activeChar.getInventory().getPaperdollItemByL2ItemId(this._slot);
         if (item != null) {
            if (activeChar.isActionsDisabled() || activeChar.isCastingSimultaneouslyNow()) {
               activeChar.sendPacket(SystemMessageId.CANNOT_CHANGE_WEAPON_DURING_AN_ATTACK);
            } else if (this._slot != 256 || !(item.getItem() instanceof EtcItem)) {
               if (this._slot != 16384 || !activeChar.isCursedWeaponEquipped() && !activeChar.isCombatFlagEquipped()) {
                  if (!activeChar.isStunned() && !activeChar.isSleeping() && !activeChar.isParalyzed() && !activeChar.isAlikeDead()) {
                     if (!activeChar.getInventory().canManipulateWithItemId(item.getId())) {
                        activeChar.sendPacket(SystemMessageId.ITEM_CANNOT_BE_TAKEN_OFF);
                     } else if (item.isWeapon() && item.getWeaponItem().isForceEquip() && !activeChar.canOverrideCond(PcCondOverride.ITEM_CONDITIONS)) {
                        activeChar.sendPacket(SystemMessageId.ITEM_CANNOT_BE_TAKEN_OFF);
                     } else {
                        ItemInstance[] unequipped = activeChar.getInventory().unEquipItemInBodySlotAndRecord(this._slot);
                        activeChar.broadcastUserInfo(true);
                        InventoryUpdate iu = new InventoryUpdate();
                        iu.addItem(item);
                        activeChar.sendPacket(iu);
                        if (unequipped.length > 0) {
                           SystemMessage sm = null;
                           if (unequipped[0].getEnchantLevel() > 0) {
                              sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
                              sm.addNumber(unequipped[0].getEnchantLevel());
                           } else {
                              sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
                           }

                           sm.addItemName(unequipped[0]);
                           activeChar.sendPacket(sm);
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
