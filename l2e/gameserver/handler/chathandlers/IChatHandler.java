package l2e.gameserver.handler.chathandlers;

import l2e.gameserver.model.actor.Player;

public interface IChatHandler {
   void handleChat(int var1, Player var2, String var3, String var4, boolean var5);

   int[] getChatTypeList();
}
