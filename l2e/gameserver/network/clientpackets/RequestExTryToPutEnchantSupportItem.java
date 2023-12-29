package l2e.gameserver.network.clientpackets;

import l2e.gameserver.data.parser.EnchantItemParser;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.enchant.EnchantItem;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExPutEnchantSupportItemResult;

public class RequestExTryToPutEnchantSupportItem extends GameClientPacket {
   private int _supportObjectId;
   private int _enchantObjectId;

   @Override
   protected void readImpl() {
      this._supportObjectId = this.readD();
      this._enchantObjectId = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null && activeChar.isEnchanting()) {
         ItemInstance item = activeChar.getInventory().getItemByObjectId(this._enchantObjectId);
         ItemInstance support = activeChar.getInventory().getItemByObjectId(this._supportObjectId);
         if (item == null || support == null) {
            return;
         }

         EnchantItem supportTemplate = EnchantItemParser.getInstance().getSupportItem(support);
         if (supportTemplate == null || !supportTemplate.isValid(item)) {
            activeChar.sendPacket(SystemMessageId.INAPPROPRIATE_ENCHANT_CONDITION);
            activeChar.setActiveEnchantSupportItemId(-1);
            activeChar.sendPacket(new ExPutEnchantSupportItemResult(0));
            return;
         }

         activeChar.setActiveEnchantSupportItemId(support.getObjectId());
         activeChar.sendPacket(new ExPutEnchantSupportItemResult(this._supportObjectId));
      }
   }
}
