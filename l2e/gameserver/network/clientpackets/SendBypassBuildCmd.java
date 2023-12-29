package l2e.gameserver.network.clientpackets;

import l2e.commons.util.GMAudit;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.AdminParser;
import l2e.gameserver.handler.admincommandhandlers.AdminCommandHandler;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.actor.Player;

public final class SendBypassBuildCmd extends GameClientPacket {
   public static final int GM_MESSAGE = 9;
   public static final int ANNOUNCEMENT = 10;
   private String _command;

   @Override
   protected void readImpl() {
      this._command = this.readS();
      if (this._command != null) {
         this._command = this._command.trim();
      }
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         String command = "admin_" + this._command.split(" ")[0];
         IAdminCommandHandler ach = AdminCommandHandler.getInstance().getHandler(command);
         if (ach == null) {
            if (activeChar.isGM()) {
               activeChar.sendMessage("The command " + command.substring(6) + " does not exists!");
            }

            _log.warning("No handler registered for admin command '" + command + "'");
         } else if (!AdminParser.getInstance().hasAccess(command, activeChar.getAccessLevel())) {
            activeChar.sendMessage("You don't have the access right to use this command!");
            _log.warning("Character " + activeChar.getName() + " tryed to use admin command " + command + ", but have no access to it!");
         } else {
            if (Config.GMAUDIT) {
               GMAudit.auditGMAction(
                  activeChar.getName() + " [" + activeChar.getObjectId() + "]",
                  this._command,
                  activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target"
               );
            }

            ach.useAdminCommand("admin_" + this._command, activeChar);
         }
      }
   }
}
