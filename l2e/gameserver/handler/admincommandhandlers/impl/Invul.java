package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class Invul implements IAdminCommandHandler {
   private static Logger _log = Logger.getLogger(Invul.class.getName());
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_invul", "admin_setinvul"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (command.equals("admin_invul")) {
         this.handleInvul(activeChar);
         NpcHtmlMessage adminhtm = new NpcHtmlMessage(5);
         adminhtm.setFile(activeChar, activeChar.getLang(), "data/html/admin/gm_menu.htm");
         activeChar.sendPacket(adminhtm);
      }

      if (command.equals("admin_setinvul")) {
         GameObject target = activeChar.getTarget();
         if (target instanceof Player) {
            this.handleInvul((Player)target);
         }
      }

      return true;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }

   private void handleInvul(Player activeChar) {
      String text;
      if (activeChar.isInvul()) {
         activeChar.setIsInvul(false);
         text = activeChar.getName() + " is now mortal";
         if (Config.DEBUG) {
            _log.fine("GM: Gm removed invul mode from character " + activeChar.getName() + "(" + activeChar.getObjectId() + ")");
         }
      } else {
         activeChar.setIsInvul(true);
         text = activeChar.getName() + " is now invulnerable";
         if (Config.DEBUG) {
            _log.fine("GM: Gm activated invul mode for character " + activeChar.getName() + "(" + activeChar.getObjectId() + ")");
         }
      }

      activeChar.sendMessage(text);
   }
}
