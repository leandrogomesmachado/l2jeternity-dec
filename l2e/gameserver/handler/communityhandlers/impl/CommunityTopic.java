package l2e.gameserver.handler.communityhandlers.impl;

import l2e.gameserver.Config;
import l2e.gameserver.handler.communityhandlers.ICommunityBoardHandler;
import l2e.gameserver.model.actor.Player;

public class CommunityTopic extends AbstractCommunity implements ICommunityBoardHandler {
   public CommunityTopic() {
      if (Config.DEBUG) {
         _log.info(this.getClass().getSimpleName() + ": Loading all functions.");
      }
   }

   @Override
   public String[] getBypassCommands() {
      return new String[]{"_bbsmemo", "_bbstopics"};
   }

   @Override
   public void onBypassCommand(String command, Player activeChar) {
      if (command.equals("_bbsmemo") || command.equals("_bbstopics")) {
         this.sendHtm(activeChar, "data/html/community/topic.htm");
      }
   }

   @Override
   public void onWriteCommand(String command, String ar1, String ar2, String ar3, String ar4, String ar5, Player activeChar) {
   }

   public static CommunityTopic getInstance() {
      return CommunityTopic.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final CommunityTopic _instance = new CommunityTopic();
   }
}
