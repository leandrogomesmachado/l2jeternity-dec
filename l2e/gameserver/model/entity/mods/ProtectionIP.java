package l2e.gameserver.model.entity.mods;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.strings.server.ServerStorage;

public class ProtectionIP {
   public static void onEnterWorld(Player player) {
      String lang = player.getLang();
      String last = "";
      String curr = "";

      try {
         last = LastIP(player);
         curr = player.getIPAddress();
      } catch (Exception var5) {
      }

      player.sendMessage(
         ""
            + ServerStorage.getInstance().getString(lang, "ProtectionIP.LAST_IP")
            + " "
            + last
            + " "
            + ServerStorage.getInstance().getString(lang, "ProtectionIP.CURRENT_IP")
            + " "
            + curr
      );
      UpdateLastIP(player, player.getAccountName());
   }

   public static String LastIP(Player player) {
      String lastIp = "";

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("SELECT * FROM `accounts` WHERE login = ?");
         statement.setString(1, player.getAccountName());
         ResultSet rset = statement.executeQuery();

         while(rset.next()) {
            lastIp = rset.getString("lastIP");
         }
      } catch (Exception var16) {
         var16.printStackTrace();
      }

      return lastIp;
   }

   public static void UpdateLastIP(Player player, String user) {
      String address = player.getIPAddress();

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("UPDATE accounts SET lastIP=? WHERE login=?");
         statement.setString(1, address);
         statement.setString(2, user);
         statement.execute();
         statement.close();
      } catch (Exception var16) {
         var16.printStackTrace();
      }
   }
}
