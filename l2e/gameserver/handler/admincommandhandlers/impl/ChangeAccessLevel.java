package l2e.gameserver.handler.admincommandhandlers.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.AdminParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;

public class ChangeAccessLevel implements IAdminCommandHandler {
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_changelvl"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      this.handleChangeLevel(command, activeChar);
      return true;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }

   private void handleChangeLevel(String command, Player activeChar) {
      String[] parts = command.split(" ");
      if (parts.length == 2) {
         try {
            int lvl = Integer.parseInt(parts[1]);
            if (activeChar.getTarget() instanceof Player) {
               this.onLineChange(activeChar, (Player)activeChar.getTarget(), lvl);
            } else {
               activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
            }
         } catch (Exception var21) {
            activeChar.sendMessage("Usage: //changelvl <target_new_level> | <player_name> <new_level>");
         }
      } else if (parts.length == 3) {
         String name = parts[1];
         int lvl = Integer.parseInt(parts[2]);
         Player player = World.getInstance().getPlayer(name);
         if (player != null) {
            this.onLineChange(activeChar, player, lvl);
         } else {
            try (Connection con = DatabaseFactory.getInstance().getConnection()) {
               PreparedStatement statement = con.prepareStatement("UPDATE characters SET accesslevel=? WHERE char_name=?");
               statement.setInt(1, lvl);
               statement.setString(2, name);
               statement.execute();
               int count = statement.getUpdateCount();
               statement.close();
               if (count == 0) {
                  activeChar.sendMessage("Character not found or access level unaltered.");
               } else {
                  activeChar.sendMessage("Character's access level is now set to " + lvl);
               }
            } catch (SQLException var23) {
               activeChar.sendMessage("SQLException while changing character's access level");
               if (Config.DEBUG) {
                  var23.printStackTrace();
               }
            }
         }
      }
   }

   private void onLineChange(Player activeChar, Player player, int lvl) {
      if (lvl >= 0) {
         if (AdminParser.getInstance().hasAccessLevel(lvl)) {
            player.setAccessLevel(lvl);
            player.sendMessage("Your access level has been changed to " + lvl);
            activeChar.sendMessage("Character's access level is now set to " + lvl + ". Effects won't be noticeable until next session.");
         } else {
            activeChar.sendMessage("You are trying to set unexisting access level: " + lvl + " please try again with a valid one!");
         }
      } else {
         player.setAccessLevel(lvl);
         player.sendMessage("Your character has been banned. Bye.");
         player.logout();
      }
   }
}
