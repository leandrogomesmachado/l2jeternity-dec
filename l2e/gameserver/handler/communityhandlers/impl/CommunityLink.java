package l2e.gameserver.handler.communityhandlers.impl;

import java.util.StringTokenizer;
import l2e.gameserver.Config;
import l2e.gameserver.handler.communityhandlers.ICommunityBoardHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.Dummy_7D;
import l2e.gameserver.network.serverpackets.Dummy_8D;

public class CommunityLink extends AbstractCommunity implements ICommunityBoardHandler {
   public CommunityLink() {
      if (Config.DEBUG) {
         _log.info(this.getClass().getSimpleName() + ": Loading all functions.");
      }
   }

   @Override
   public String[] getBypassCommands() {
      return new String[]{"_bbslink", "_bbsurl", "_bbsopenurl"};
   }

   @Override
   public void onBypassCommand(String command, Player activeChar) {
      if (command.equalsIgnoreCase("_bbslink")) {
         this.sendHtm(activeChar, "data/html/community/homepage.htm");
      } else if (command.startsWith("_bbsurl")) {
         StringTokenizer st = new StringTokenizer(command, ":");
         st.nextToken();
         String url = null;

         try {
            url = st.nextToken();
         } catch (Exception var7) {
         }

         if (url != null) {
            activeChar.sendPacket(new Dummy_7D(url, Dummy_7D.ServerRequest.SC_SERVER_REQUEST_OPEN_URL));
         }
      } else if (command.startsWith("_bbsopenurl")) {
         StringTokenizer st = new StringTokenizer(command, " ");
         st.nextToken();
         String url = null;

         try {
            url = st.nextToken();
         } catch (Exception var6) {
         }

         if (url != null) {
            activeChar.sendPacket(new Dummy_8D(url));
         }
      }
   }

   @Override
   public void onWriteCommand(String command, String ar1, String ar2, String ar3, String ar4, String ar5, Player activeChar) {
   }

   public static CommunityLink getInstance() {
      return CommunityLink.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final CommunityLink _instance = new CommunityLink();
   }
}
