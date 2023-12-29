package l2e.gameserver.handler.itemhandlers.impl;

import l2e.gameserver.data.htm.HtmCache;
import l2e.gameserver.handler.itemhandlers.IItemHandler;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class Bypass implements IItemHandler {
   @Override
   public boolean useItem(Playable playable, ItemInstance item, boolean forceUse) {
      if (!playable.isPlayer()) {
         return false;
      } else {
         Player activeChar = (Player)playable;
         int itemId = item.getId();
         String filename = "data/html/item/" + itemId + ".htm";
         String content = HtmCache.getInstance().getHtm(activeChar, activeChar.getLang(), filename);
         NpcHtmlMessage html = new NpcHtmlMessage(0, itemId);
         if (content == null) {
            html.setHtml(activeChar, "<html><body>My Text is missing:<br>" + filename + "</body></html>");
            activeChar.sendPacket(html);
         } else {
            html.setHtml(activeChar, content);
            html.replace("%itemId%", String.valueOf(item.getObjectId()));
            activeChar.sendPacket(html);
         }

         return true;
      }
   }
}
