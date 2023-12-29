package l2e.gameserver.listener.events;

import l2e.gameserver.listener.EventListener;
import l2e.gameserver.model.actor.Player;

public class RequestBypassToServerEvent implements EventListener {
   private Player _activeChar;
   private String _command;

   public void setActiveChar(Player activeChar) {
      this._activeChar = activeChar;
   }

   public Player getActiveChar() {
      return this._activeChar;
   }

   public void setCommand(String command) {
      this._command = command;
   }

   public String getCommand() {
      return this._command;
   }
}
