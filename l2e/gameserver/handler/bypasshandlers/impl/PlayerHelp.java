package l2e.gameserver.handler.bypasshandlers.impl;

import java.util.StringTokenizer;
import java.util.logging.Level;
import l2e.gameserver.handler.bypasshandlers.IBypassHandler;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class PlayerHelp implements IBypassHandler {
   private static final String[] COMMANDS = new String[]{"player_help"};

   @Override
   public boolean useBypass(String command, Player activeChar, Creature target) {
      try {
         if (command.length() < 13) {
            return false;
         }

         String path = command.substring(12);
         if (path.indexOf("..") != -1) {
            return false;
         }

         StringTokenizer st = new StringTokenizer(path);
         String[] cmd = st.nextToken().split("#");
         NpcHtmlMessage html;
         if (cmd.length > 1) {
            int itemId = Integer.parseInt(cmd[1]);
            html = new NpcHtmlMessage(1, itemId);
         } else {
            html = new NpcHtmlMessage(1);
         }

         html.setFile(activeChar, activeChar.getLang(), "data/html/help/" + cmd[0]);
         activeChar.sendPacket(html);
      } catch (Exception var9) {
         _log.log(Level.WARNING, "Exception in " + this.getClass().getSimpleName(), (Throwable)var9);
      }

      return true;
   }

   @Override
   public String[] getBypassList() {
      return COMMANDS;
   }
}
