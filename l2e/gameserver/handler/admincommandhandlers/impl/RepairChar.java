package l2e.gameserver.handler.admincommandhandlers.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.handler.admincommandhandlers.IAdminCommandHandler;
import l2e.gameserver.model.actor.Player;

public class RepairChar implements IAdminCommandHandler {
   private static Logger _log = Logger.getLogger(RepairChar.class.getName());
   private static final String[] ADMIN_COMMANDS = new String[]{"admin_restore", "admin_repair"};

   @Override
   public boolean useAdminCommand(String command, Player activeChar) {
      this.handleRepair(command);
      return true;
   }

   @Override
   public String[] getAdminCommandList() {
      return ADMIN_COMMANDS;
   }

   private void handleRepair(String command) {
      String[] parts = command.split(" ");
      if (parts.length == 2) {
         String cmd = "UPDATE characters SET x=-84318, y=244579, z=-3730 WHERE char_name=?";

         try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            PreparedStatement statement = con.prepareStatement("UPDATE characters SET x=-84318, y=244579, z=-3730 WHERE char_name=?");
            statement.setString(1, parts[1]);
            statement.execute();
            statement.close();
            statement = con.prepareStatement("SELECT charId FROM characters where char_name=?");
            statement.setString(1, parts[1]);
            ResultSet rset = statement.executeQuery();
            int objId = 0;
            if (rset.next()) {
               objId = rset.getInt(1);
            }

            rset.close();
            statement.close();
            if (objId == 0) {
               return;
            }

            statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE charId=?");
            statement.setInt(1, objId);
            statement.execute();
            statement.close();
            statement = con.prepareStatement("UPDATE items SET loc=\"INVENTORY\" WHERE owner_id=?");
            statement.setInt(1, objId);
            statement.execute();
            statement.close();
         } catch (Exception var21) {
            _log.log(java.util.logging.Level.WARNING, "could not repair char:", (Throwable)var21);
         }
      }
   }
}
