package l2e.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.stats.StatsSet;

public class GlobalVariablesManager extends StatsSet {
   private static final long serialVersionUID = 8318815614688926070L;
   private static final Logger _log = Logger.getLogger(GlobalVariablesManager.class.getName());
   private static final String LOAD_VAR = "SELECT var,value FROM global_variables";
   private static final String SAVE_VAR = "INSERT INTO global_variables (var,value) VALUES (?,?) ON DUPLICATE KEY UPDATE value=?";
   private final Map<String, String> _variablesMap = new ConcurrentHashMap<>();

   protected GlobalVariablesManager() {
      this.restoreMe();
   }

   public boolean restoreMe() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         Statement statement = con.createStatement();
         ResultSet rset = statement.executeQuery("SELECT var,value FROM global_variables");
      ) {
         while(rset.next()) {
            String var = rset.getString(1);
            String value = rset.getString(2);
            this._variablesMap.put(var, value);
         }

         return true;
      } catch (Exception var60) {
         _log.warning(this.getClass().getSimpleName() + ": problem while loading variables: " + var60);
         return false;
      }
   }

   public boolean storeMe() {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("INSERT INTO global_variables (var,value) VALUES (?,?) ON DUPLICATE KEY UPDATE value=?");
      ) {
         for(String var : this._variablesMap.keySet()) {
            statement.setString(1, var);
            statement.setString(2, this._variablesMap.get(var));
            statement.setString(3, this._variablesMap.get(var));
            statement.execute();
            statement.clearParameters();
         }

         return true;
      } catch (Exception var34) {
         _log.warning(this.getClass().getSimpleName() + ": problem while saving variables: " + var34);
         return false;
      }
   }

   public void storeVariable(String var, String value) {
      this._variablesMap.put(var, value);
   }

   public boolean isVariableStored(String var) {
      return this._variablesMap.containsKey(var);
   }

   public String getStoredVariable(String var) {
      return this._variablesMap.get(var);
   }

   public static final GlobalVariablesManager getInstance() {
      return GlobalVariablesManager.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final GlobalVariablesManager _instance = new GlobalVariablesManager();
   }
}
