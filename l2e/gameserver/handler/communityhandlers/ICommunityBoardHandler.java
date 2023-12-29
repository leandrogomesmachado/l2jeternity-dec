package l2e.gameserver.handler.communityhandlers;

import l2e.gameserver.model.actor.Player;

public interface ICommunityBoardHandler {
   String[] getBypassCommands();

   void onBypassCommand(String var1, Player var2);

   void onWriteCommand(String var1, String var2, String var3, String var4, String var5, String var6, Player var7);
}
