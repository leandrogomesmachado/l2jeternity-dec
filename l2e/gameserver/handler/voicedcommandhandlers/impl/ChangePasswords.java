package l2e.gameserver.handler.voicedcommandhandlers.impl;

import java.util.StringTokenizer;
import java.util.logging.Level;
import l2e.gameserver.Config;
import l2e.gameserver.handler.voicedcommandhandlers.IVoicedCommandHandler;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.communication.AuthServerCommunication;
import l2e.gameserver.network.communication.gameserverpackets.ChangePassword;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class ChangePasswords implements IVoicedCommandHandler {
   private static final String[] _voicedCommands = new String[]{"changepassword"};

   @Override
   public boolean useVoicedCommand(String command, Player activeChar, String target) {
      if (!Config.ALLOW_CHANGE_PASSWORD) {
         return false;
      } else if (target != null && !target.isEmpty()) {
         StringTokenizer st = new StringTokenizer(target);

         try {
            String curpass = null;
            String newpass = null;
            String repeatnewpass = null;
            if (st.hasMoreTokens()) {
               curpass = st.nextToken();
            }

            if (st.hasMoreTokens()) {
               newpass = st.nextToken();
            }

            if (st.hasMoreTokens()) {
               repeatnewpass = st.nextToken();
            }

            if (curpass == null || newpass == null || repeatnewpass == null) {
               activeChar.sendMessage("Invalid password data! You have to fill all boxes.");
               return false;
            }

            if (!newpass.equals(repeatnewpass)) {
               activeChar.sendMessage("The new password doesn't match with the repeated one!");
               return false;
            }

            if (newpass.length() < 3) {
               activeChar.sendMessage("The new password is shorter than 3 chars! Please try with a longer one.");
               return false;
            }

            if (newpass.length() > 30) {
               activeChar.sendMessage("The new password is longer than 30 chars! Please try with a shorter one.");
               return false;
            }

            AuthServerCommunication.getInstance().sendPacket(new ChangePassword(activeChar.getAccountName(), curpass, newpass, "0"));
         } catch (Exception var8) {
            activeChar.sendMessage("A problem occured while changing password!");
            _log.log(Level.WARNING, "", (Throwable)var8);
         }

         return true;
      } else {
         NpcHtmlMessage html = new NpcHtmlMessage(activeChar.getObjectId());
         html.setFile(activeChar, activeChar.getLang(), "data/html/mods/ChangePassword.htm");
         activeChar.sendPacket(html);
         return true;
      }
   }

   @Override
   public String[] getVoicedCommandList() {
      return _voicedCommands;
   }
}
