package l2e.gameserver.handler.admincommandhandlers.impl;

import l2e.gameserver.data.parser.AdminParser;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.CreatureSay;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class GmChat implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_gmchat", "admin_snoop", "admin_gmchat_menu"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (command.startsWith("admin_gmchat")) {
         this.handleGmChat(command, activeChar);
      } else if (command.startsWith("admin_snoop")) {
         this.snoop(command, activeChar);
      }

      if (command.startsWith("admin_gmchat_menu")) {
         NpcHtmlMessage adminhtm = new NpcHtmlMessage(5);
         adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/gm_menu.htm");
         activeChar.sendPacket(adminhtm);
      }

      return true;
   }

   private void snoop(String command, Player activeChar) {
      GameObject target = null;
      if (command.length() > 12) {
         target = World.getInstance().getPlayer(command.substring(12));
      }

      if (target == null) {
         target = activeChar.getTarget();
      }

      if (target == null) {
         activeChar.sendPacket(SystemMessageId.SELECT_TARGET);
      } else if (!(target instanceof Player)) {
         activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
      } else {
         Player player = (Player)target;
         player.addSnooper(activeChar);
         activeChar.addSnooped(player);
      }
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }

   private void handleGmChat(String command, Player activeChar) {
      try {
         int offset = 0;
         byte var7;
         if (command.startsWith("admin_gmchat_menu")) {
            var7 = 18;
         } else {
            var7 = 13;
         }

         String text = command.substring(var7);
         CreatureSay cs = new CreatureSay(0, 9, activeChar.getName(), text);
         AdminParser.getInstance().broadcastToGMs(cs);
      } catch (StringIndexOutOfBoundsException var6) {
      }
   }
}
