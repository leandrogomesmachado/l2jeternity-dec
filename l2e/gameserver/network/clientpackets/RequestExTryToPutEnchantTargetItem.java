package l2e.gameserver.network.clientpackets;

import java.util.logging.Level;
import l2e.gameserver.data.parser.EnchantItemParser;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.enchant.EnchantScroll;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExPutEnchantTargetItemResult;

public class RequestExTryToPutEnchantTargetItem extends GameClientPacket {
   private int _objectId = 0;

   @Override
   protected void readImpl() {
      this._objectId = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (this._objectId != 0 && activeChar != null) {
         if (!activeChar.isEnchanting()) {
            if (!activeChar.isActionsDisabled() && !activeChar.isInStoreMode() && activeChar.getActiveTradeList() == null) {
               ItemInstance item = activeChar.getInventory().getItemByObjectId(this._objectId);
               ItemInstance scroll = activeChar.getInventory().getItemByObjectId(activeChar.getActiveEnchantItemId());
               if (item != null && scroll != null) {
                  EnchantScroll scrollTemplate = EnchantItemParser.getInstance().getEnchantScroll(scroll);
                  if (scrollTemplate != null && scrollTemplate.isValid(item) && !(scrollTemplate.getChance(activeChar, item) <= 0.0)) {
                     activeChar.setIsEnchanting(true);
                     activeChar.sendPacket(new ExPutEnchantTargetItemResult(this._objectId));
                  } else {
                     activeChar.sendPacket(SystemMessageId.DOES_NOT_FIT_SCROLL_CONDITIONS);
                     activeChar.setActiveEnchantItemId(-1);
                     activeChar.sendPacket(new ExPutEnchantTargetItemResult(0));
                     if (scrollTemplate == null) {
                        _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Undefined scroll have been used id: " + scroll.getId());
                     }
                  }
               }
            } else {
               activeChar.setActiveEnchantItemId(-1);
               activeChar.sendPacket(new ExPutEnchantTargetItemResult(0));
            }
         }
      }
   }
}
