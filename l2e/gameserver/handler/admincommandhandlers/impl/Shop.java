package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.logging.Logger;
import l2e.gameserver.data.parser.BuyListParser;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.items.buylist.ProductList;
import l2e.gameserver.network.serverpackets.BuyList;
import l2e.gameserver.network.serverpackets.ExBuySellList;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class Shop implements IAdminCommandHandler {
   private static final Logger _log = Logger.getLogger(Shop.class.getName());
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_buy", "admin_gmshop"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (command.startsWith("admin_buy")) {
         try {
            this.handleBuyRequest(activeChar, command.substring(10));
         } catch (IndexOutOfBoundsException var4) {
            activeChar.sendMessage("Please specify buylist.");
         }
      } else if (command.equals("admin_gmshop")) {
         NpcHtmlMessage adminhtm = new NpcHtmlMessage(5);
         adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/gmshops.htm");
         activeChar.sendPacket(adminhtm);
      }

      return true;
   }

   private void handleBuyRequest(Player activeChar, String command) {
      int val = -1;

      try {
         val = Integer.parseInt(command);
      } catch (Exception var5) {
         _log.warning("admin buylist failed:" + command);
      }

      ProductList buyList = BuyListParser.getInstance().getBuyList(val);
      if (buyList != null) {
         activeChar.sendPacket(new BuyList(buyList, activeChar.getAdena(), 0.0));
         activeChar.sendPacket(new ExBuySellList(activeChar, false));
      } else {
         _log.warning("no buylist with id:" + val);
      }

      activeChar.sendActionFailed();
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }
}
