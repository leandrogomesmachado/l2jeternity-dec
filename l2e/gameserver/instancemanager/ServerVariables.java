package l2e.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.stats.StatsSet;

public class ServerVariables {
   private static StatsSet server_vars = null;

   public static StatsSet getVars() {
      if (server_vars == null) {
         server_vars = new StatsSet();
         LoadFromDB();
      }

      return server_vars;
   }

   private static void LoadFromDB() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT * FROM server_variables");
      ) {
         ResultSet rs = statement.executeQuery();

         while(rs.next()) {
            server_vars.set(rs.getString("name"), rs.getString("value"));
         }
      } catch (SQLException var32) {
         System.out.println("ServerVariables: Could not load table");
         var32.printStackTrace();
      }
   }

   private static void SaveToDB(String name) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         String e = getVars().getString(name, "");
         if (e.isEmpty()) {
            PreparedStatement statement = con.prepareStatement("DELETE FROM server_variables WHERE name = ?");
            statement.setString(1, name);
            statement.execute();
         } else {
            PreparedStatement statement = con.prepareStatement("REPLACE INTO server_variables (name, value) VALUES (?,?)");
            statement.setString(1, name);
            statement.setString(2, e);
            statement.execute();
         }
      } catch (SQLException var15) {
         System.out.println("ServerVariables: Could not save table");
         var15.printStackTrace();
      }
   }

   public static boolean getBool(String name) {
      return getVars().getBool(name);
   }

   public static boolean getBool(String name, boolean defult) {
      return getVars().getBool(name, defult);
   }

   public static int getInt(String name) {
      return getVars().getInteger(name);
   }

   public static int getInt(String name, int defult) {
      return getVars().getInteger(name, defult);
   }

   public static long getLong(String name) {
      return getVars().getLong(name);
   }

   public static long getLong(String name, long defult) {
      return getVars().getLong(name, defult);
   }

   public static double getFloat(String name) {
      return getVars().getDouble(name);
   }

   public static double getFloat(String name, double defult) {
      return getVars().getDouble(name, defult);
   }

   public static String getString(String name) {
      return getVars().getString(name);
   }

   public static String getString(String name, String defult) {
      return getVars().getString(name, defult);
   }

   public static void set(String name, boolean value) {
      getVars().set(name, value);
      SaveToDB(name);
   }

   public static void set(String name, int value) {
      getVars().set(name, value);
      SaveToDB(name);
   }

   public static void set(String name, long value) {
      getVars().set(name, value);
      SaveToDB(name);
   }

   public static void set(String name, double value) {
      getVars().set(name, value);
      SaveToDB(name);
   }

   public static void set(String name, String value) {
      getVars().set(name, value);
      SaveToDB(name);
   }

   public static void unset(String name) {
      getVars().unset(name);
      SaveToDB(name);
   }
}
