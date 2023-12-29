package l2e.gameserver.network.clientpackets;

import l2e.commons.util.Util;
import l2e.gameserver.data.parser.HennaParser;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Henna;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.InventoryUpdate;

public final class RequestHennaEquip extends GameClientPacket {
   private int _symbolId;

   @Override
   protected void readImpl() {
      this._symbolId = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getActiveChar();
      if (activeChar != null) {
         if (activeChar.getHennaEmptySlots() == 0) {
            activeChar.sendPacket(SystemMessageId.SYMBOLS_FULL);
            this.sendActionFailed();
         } else {
            Henna henna = HennaParser.getInstance().getHenna(this._symbolId);
            if (henna == null) {
               _log.warning(this.getClass().getName() + ": Invalid Henna Id: " + this._symbolId + " from player " + activeChar);
               this.sendActionFailed();
            } else {
               long _count = activeChar.getInventory().getInventoryItemCount(henna.getDyeItemId(), -1);
               if (henna.isAllowedClass(activeChar.getClassId())
                  && _count >= (long)henna.getWearCount()
                  && activeChar.getAdena() >= (long)henna.getWearFee()
                  && activeChar.addHenna(henna)) {
                  activeChar.destroyItemByItemId("Henna", henna.getDyeItemId(), (long)henna.getWearCount(), activeChar, true);
                  activeChar.getInventory().reduceAdena("Henna", (long)henna.getWearFee(), activeChar, activeChar.getLastFolkNPC());
                  InventoryUpdate iu = new InventoryUpdate();
                  iu.addModifiedItem(activeChar.getInventory().getAdenaInstance());
                  activeChar.sendPacket(iu);
                  activeChar.sendPacket(SystemMessageId.SYMBOL_ADDED);
               } else {
                  activeChar.sendPacket(SystemMessageId.CANT_DRAW_SYMBOL);
                  if (!activeChar.canOverrideCond(PcCondOverride.ITEM_CONDITIONS) && !henna.isAllowedClass(activeChar.getClassId())) {
                     Util.handleIllegalPlayerAction(
                        activeChar, "" + activeChar.getName() + " of account " + activeChar.getAccountName() + " tryed to add a forbidden henna."
                     );
                  }

                  this.sendActionFailed();
               }
            }
         }
      }
   }
}
