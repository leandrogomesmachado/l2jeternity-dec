package l2e.gameserver.handler.bypasshandlers.impl;

import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.MerchantInstance;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class BuyShadowItem implements IBypassHandler {
   private static final String[] COMMANDS = new String[]{"BuyShadowItem"};

   @Override
   public boolean useBypass(String command, Player activeChar, Creature target) {
      if (!(target instanceof MerchantInstance)) {
         return false;
      } else {
         NpcHtmlMessage html = new NpcHtmlMessage(((Npc)target).getObjectId());
         if (activeChar.getLevel() < 40) {
            html.setFile(activeChar, activeChar.getLang(), "data/html/common/shadow_item-lowlevel.htm");
         } else if (activeChar.getLevel() >= 40 && activeChar.getLevel() < 46) {
            html.setFile(activeChar, activeChar.getLang(), "data/html/common/shadow_item_d.htm");
         } else if (activeChar.getLevel() >= 46 && activeChar.getLevel() < 52) {
            html.setFile(activeChar, activeChar.getLang(), "data/html/common/shadow_item_c.htm");
         } else if (activeChar.getLevel() >= 52) {
            html.setFile(activeChar, activeChar.getLang(), "data/html/common/shadow_item_b.htm");
         }

         html.replace("%objectId%", String.valueOf(((Npc)target).getObjectId()));
         activeChar.sendPacket(html);
         return true;
      }
   }

   @Override
   public String[] getBypassList() {
      return COMMANDS;
   }
}
