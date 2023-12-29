package l2e.gameserver.handler.usercommandhandlers;

import java.util.logging.Logger;
import l2e.gameserver.model.actor.Player;

public interface IUserCommandHandler {
   Logger _log = Logger.getLogger(IUserCommandHandler.class.getName());

   boolean useUserCommand(int var1, Player var2);

   int[] getUserCommandList();
}
