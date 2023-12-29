package l2e.gameserver.handler.itemhandlers.impl;

import java.util.List;
import java.util.logging.Logger;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.ExtractableProductTemplate;
import l2e.gameserver.model.actor.templates.items.EtcItem;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;

public class ExtractableItems implements IItemHandler {
   private static Logger _log = Logger.getLogger(ExtractableItems.class.getName());

   @Override
   public boolean useItem(Playable playable, ItemInstance item, boolean forceUse) {
      if (!playable.isPlayer()) {
         playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
         return false;
      } else {
         Player activeChar = playable.getActingPlayer();
         EtcItem etcitem = (EtcItem)item.getItem();
         List<ExtractableProductTemplate> exitem = etcitem.getExtractableItems();
         if (exitem == null) {
            _log.info("No extractable data defined for " + etcitem);
            return false;
         } else if (!activeChar.destroyItem("Extract", item.getObjectId(), 1L, activeChar, true)) {
            return false;
         } else {
            boolean created = false;

            for(ExtractableProductTemplate expi : exitem) {
               if (Rnd.get(100000) <= expi.getChance()) {
                  int min = (int)((double)expi.getMin() * Config.RATE_EXTRACTABLE);
                  int max = (int)((double)expi.getMax() * Config.RATE_EXTRACTABLE);
                  int createitemAmount = max == min ? min : Rnd.get(max - min + 1) + min;
                  activeChar.addItem("Extract", expi.getId(), (long)createitemAmount, activeChar, true);
                  created = true;
               }
            }

            if (!created) {
               activeChar.sendPacket(SystemMessageId.NOTHING_INSIDE_THAT);
            }

            return true;
         }
      }
   }
}
