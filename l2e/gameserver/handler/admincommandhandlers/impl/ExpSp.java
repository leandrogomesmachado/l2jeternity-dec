package l2e.gameserver.handler.admincommandhandlers.impl;

import java.util.StringTokenizer;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.ClassListParser;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.NpcHtmlMessage;

public class ExpSp implements IAdminCommandHandler {
   private static Logger _log = Logger.getLogger(ExpSp.class.getName());
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_add_exp_sp_to_character", "admin_add_exp_sp", "admin_remove_exp_sp"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      if (command.startsWith("admin_add_exp_sp")) {
         try {
            String val = command.substring(16);
            if (!this.adminAddExpSp(activeChar, val)) {
               activeChar.sendMessage("Usage: //add_exp_sp exp sp");
            }
         } catch (StringIndexOutOfBoundsException var5) {
            activeChar.sendMessage("Usage: //add_exp_sp exp sp");
         }
      } else if (command.startsWith("admin_remove_exp_sp")) {
         try {
            String val = command.substring(19);
            if (!this.adminRemoveExpSP(activeChar, val)) {
               activeChar.sendMessage("Usage: //remove_exp_sp exp sp");
            }
         } catch (StringIndexOutOfBoundsException var4) {
            activeChar.sendMessage("Usage: //remove_exp_sp exp sp");
         }
      }

      this.addExpSp(activeChar);
      return true;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }

   private void addExpSp(Player activeChar) {
      GameObject target = activeChar.getTarget();
      Player player = null;
      if (target instanceof Player) {
         player = (Player)target;
         NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
         adminReply.setFile(activeChar, activeChar.getLang(), "data/html/admin/expsp.htm");
         adminReply.replace("%name%", player.getName());
         adminReply.replace("%level%", String.valueOf(player.getLevel()));
         adminReply.replace("%xp%", String.valueOf(player.getExp()));
         adminReply.replace("%sp%", String.valueOf(player.getSp()));
         adminReply.replace("%class%", ClassListParser.getInstance().getClass(player.getClassId()).getClientCode());
         activeChar.sendPacket(adminReply);
      } else {
         activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
      }
   }

   private boolean adminAddExpSp(Player activeChar, String ExpSp) {
      GameObject target = activeChar.getTarget();
      Player player = null;
      if (!(target instanceof Player)) {
         activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
         return false;
      } else {
         player = (Player)target;
         StringTokenizer st = new StringTokenizer(ExpSp);
         if (st.countTokens() != 2) {
            return false;
         } else {
            String exp = st.nextToken();
            String sp = st.nextToken();
            long expval = 0L;
            int spval = 0;

            try {
               expval = Long.parseLong(exp);
               spval = Integer.parseInt(sp);
            } catch (Exception var12) {
               return false;
            }

            if (expval != 0L || spval != 0) {
               player.sendMessage("Admin is adding you " + expval + " xp and " + spval + " sp.");
               player.addExpAndSp(expval, spval);
               activeChar.sendMessage("Added " + expval + " xp and " + spval + " sp to " + player.getName() + ".");
               if (Config.DEBUG) {
                  _log.fine(
                     "GM: "
                        + activeChar.getName()
                        + "("
                        + activeChar.getObjectId()
                        + ") added "
                        + expval
                        + " xp and "
                        + spval
                        + " sp to "
                        + player.getObjectId()
                        + "."
                  );
               }
            }

            return true;
         }
      }
   }

   private boolean adminRemoveExpSP(Player activeChar, String ExpSp) {
      GameObject target = activeChar.getTarget();
      Player player = null;
      if (!(target instanceof Player)) {
         activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
         return false;
      } else {
         player = (Player)target;
         StringTokenizer st = new StringTokenizer(ExpSp);
         if (st.countTokens() != 2) {
            return false;
         } else {
            String exp = st.nextToken();
            String sp = st.nextToken();
            long expval = 0L;
            int spval = 0;

            try {
               expval = Long.parseLong(exp);
               spval = Integer.parseInt(sp);
            } catch (Exception var12) {
               return false;
            }

            if (expval != 0L || spval != 0) {
               player.sendMessage("Admin is removing you " + expval + " xp and " + spval + " sp.");
               player.removeExpAndSp(expval, spval);
               activeChar.sendMessage("Removed " + expval + " xp and " + spval + " sp from " + player.getName() + ".");
               if (Config.DEBUG) {
                  _log.fine(
                     "GM: "
                        + activeChar.getName()
                        + "("
                        + activeChar.getObjectId()
                        + ") removed "
                        + expval
                        + " xp and "
                        + spval
                        + " sp from "
                        + player.getObjectId()
                        + "."
                  );
               }
            }

            return true;
         }
      }
   }
}
