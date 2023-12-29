package l2e.gameserver.handler.admincommandhandlers.impl;

import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.ExAgitAuctionCmd;
import l2e.gameserver.network.serverpackets.ExBrBuffEventState;
import l2e.gameserver.network.serverpackets.ExGoodsInventoryChangedNotify;
import l2e.gameserver.network.serverpackets.ExGoodsInventoryInfo;
import l2e.gameserver.network.serverpackets.ExGoodsInventoryResult;
import l2e.gameserver.network.serverpackets.ExSay2Fail;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class Packets implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{
      "admin_test", "admin_1_packet", "admin_2_packet", "admin_3_packet", "admin_4_packet", "admin_5_packet", "admin_6_packet"
   };

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (activeChar == null) {
         return false;
      } else if (command.startsWith("admin_test")) {
         this.showMenu(activeChar);
         return true;
      } else if (command.startsWith("admin_1_packet")) {
         activeChar.broadcastPacket(new ExAgitAuctionCmd());
         this.showMenu(activeChar);
         return true;
      } else if (command.startsWith("admin_2_packet")) {
         activeChar.broadcastPacket(new ExBrBuffEventState(10, 20573, 1, 60));
         this.showMenu(activeChar);
         return true;
      } else if (command.startsWith("admin_3_packet")) {
         activeChar.broadcastPacket(new ExSay2Fail());
         this.showMenu(activeChar);
         return true;
      } else if (command.startsWith("admin_4_packet")) {
         activeChar.broadcastPacket(new ExGoodsInventoryChangedNotify());
         this.showMenu(activeChar);
         return true;
      } else if (command.startsWith("admin_5_packet")) {
         activeChar.broadcastPacket(new ExGoodsInventoryInfo());
         this.showMenu(activeChar);
         return true;
      } else if (command.startsWith("admin_6_packet")) {
         activeChar.broadcastPacket(new ExGoodsInventoryResult(2));
         this.showMenu(activeChar);
         return true;
      } else {
         return false;
      }
   }

   private void showMenu(Player activeChar) {
      NpcHtmlMessage html = new NpcHtmlMessage(0);
      html.setFile(activeChar, activeChar.getLang(), "data/html/admin/packets-test.htm");
      activeChar.sendPacket(html);
   }
}
