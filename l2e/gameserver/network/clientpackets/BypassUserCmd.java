package l2e.gameserver.network.clientpackets;

import l2e.gameserver.handler.usercommandhandlers.IUserCommandHandler;
import l2e.gameserver.handler.usercommandhandlers.UserCommandHandler;
import l2e.gameserver.model.actor.Player;

public class BypassUserCmd extends GameClientPacket {
   private int _command;

   @Override
   protected void readImpl() {
      this._command = this.readD();
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         IUserCommandHandler handler = UserCommandHandler.getInstance().getHandler(this._command);
         if (handler == null) {
            if (player.isGM()) {
               player.sendMessage("User commandID " + this._command + " not implemented yet.");
            }
         } else {
            handler.useUserCommand(this._command, this.getClient().getActiveChar());
         }
      }
   }
}
