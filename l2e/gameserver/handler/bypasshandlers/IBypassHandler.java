package l2e.gameserver.handler.bypasshandlers;

import java.util.logging.Logger;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;

public interface IBypassHandler {
   Logger _log = Logger.getLogger(IBypassHandler.class.getName());

   boolean useBypass(String var1, Player var2, Creature var3);

   String[] getBypassList();
}
