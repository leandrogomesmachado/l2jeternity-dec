package l2e.gameserver.handler.voicedcommandhandlers.impl;

import l2e.gameserver.Config;
import l2e.gameserver.handler.communityhandlers.CommunityBoardHandler;
import l2e.gameserver.handler.communityhandlers.ICommunityBoardHandler;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.model.actor.Player;

public class FacebookPanel implements IVoicedCommandHandler {
   private static final String[] COMMANDS = new String[]{"fb", "facebook"};

   @Override
   public boolean useVoicedCommand(String command, Player player, String args) {
      if (!Config.ALLOW_FACEBOOK_SYSTEM) {
         return false;
      } else {
         ICommunityBoardHandler handler = CommunityBoardHandler.getInstance().getHandler("_bbsfacebook");
         if (handler != null) {
            handler.onBypassCommand("_bbsfacebook_main", player);
         }

         return true;
      }
   }

   @Override
   public String[] getVoicedCommandList() {
      return COMMANDS;
   }
}
