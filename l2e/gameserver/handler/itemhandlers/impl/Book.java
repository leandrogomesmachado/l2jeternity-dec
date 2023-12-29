package l2e.gameserver.handler.itemhandlers.impl;

import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class Book implements IItemHandler {
   @Override
   public boolean useItem(Playable playable, ItemInstance item, boolean forceUse) {
      if (!playable.isPlayer()) {
         playable.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
         return false;
      } else {
         Player activeChar = (Player)playable;
         int itemId = item.getId();
         String filename = "data/html/help/" + itemId + ".htm";
         String content = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), filename);
         if (content == null) {
            NpcHtmlMessage html = new NpcHtmlMessage(1);
            html.setHtml(activeChar, "<html><body>My Text is missing:<br>" + filename + "</body></html>");
            activeChar.sendPacket(html);
         } else {
            NpcHtmlMessage itemReply = new NpcHtmlMessage(5, itemId);
            itemReply.setHtml(activeChar, content);
            activeChar.sendPacket(itemReply);
         }

         activeChar.sendActionFailed();
         return true;
      }
   }
}
