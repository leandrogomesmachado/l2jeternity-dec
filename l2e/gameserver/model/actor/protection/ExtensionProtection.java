package l2e.gameserver.model.actor.protection;

import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;

public class ExtensionProtection {
   protected static final Logger _log = Logger.getLogger(ExtensionProtection.class.getName());
   private Player _activeChar = null;

   public ExtensionProtection(Player activeChar) {
      if (activeChar == null) {
         _log.warning("[ExtensionProtection] _activeChar: There can be a null value!");
      } else {
         this._activeChar = activeChar;
         if (Config.DEBUG) {
            _log.info("[ExtensionProtection] _activeChar: " + this._activeChar.getObjectId() + " - " + this._activeChar.getName() + ".");
         }
      }
   }

   public Player getPlayer() {
      return this._activeChar;
   }
}
