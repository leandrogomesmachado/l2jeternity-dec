package l2e.gameserver.handler.voicedcommandhandlers;

import java.util.logging.Logger;
import l2e.gameserver.model.actor.Player;

public interface IVoicedCommandHandler {
   Logger _log = Logger.getLogger(IVoicedCommandHandler.class.getName());

   boolean useVoicedCommand(String var1, Player var2, String var3);

   String[] getVoicedCommandList();
}
