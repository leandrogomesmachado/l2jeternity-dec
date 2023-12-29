package l2e.gameserver.handler.admincommandhandlers;

import l2e.gameserver.model.actor.Player;

public interface IAdminCommandHandler {
   boolean useAdminCommand(String var1, Player var2);

   String[] getAdminCommandList();
}
