package l2e.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.data.parser.UIParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.actor.templates.ActionKeyTemplate;

public class UIKeysSettings {
   private static final Logger _log = Logger.getLogger(UIKeysSettings.class.getName());
   private final int _playerObjId;
   private Map<Integer, List<ActionKeyTemplate>> _storedKeys;
   private Map<Integer, List<Integer>> _storedCategories;
   private boolean _saved = true;

   public UIKeysSettings(int playerObjId) {
      this._playerObjId = playerObjId;
      this.loadFromDB();
   }

   public void storeAll(Map<Integer, List<Integer>> catMap, Map<Integer, List<ActionKeyTemplate>> keyMap) {
      this._saved = false;
      this._storedCategories = catMap;
      this._storedKeys = keyMap;
   }

   public void storeCategories(Map<Integer, List<Integer>> catMap) {
      this._saved = false;
      this._storedCategories = catMap;
   }

   public Map<Integer, List<Integer>> getCategories() {
      return this._storedCategories;
   }

   public void storeKeys(Map<Integer, List<ActionKeyTemplate>> keyMap) {
      this._saved = false;
      this._storedKeys = keyMap;
   }

   public Map<Integer, List<ActionKeyTemplate>> getKeys() {
      return this._storedKeys;
   }

   public void loadFromDB() {
      this.getCatsFromDB();
      this.getKeysFromDB();
   }

   public void saveInDB() {
      if (!this._saved) {
         String query = "REPLACE INTO character_ui_categories (`charId`, `catId`, `order`, `cmdId`) VALUES ";

         for(int category : this._storedCategories.keySet()) {
            int order = 0;

            for(int key : this._storedCategories.get(category)) {
               query = query + "(" + this._playerObjId + ", " + category + ", " + order++ + ", " + key + "),";
            }
         }

         query = query.substring(0, query.length() - 1) + "; ";

         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(query);
         ) {
            statement.execute();
         } catch (Exception var94) {
            _log.log(Level.WARNING, "Exception: saveInDB(): " + var94.getMessage(), (Throwable)var94);
         }

         query = "REPLACE INTO character_ui_actions (`charId`, `cat`, `order`, `cmd`, `key`, `tgKey1`, `tgKey2`, `show`) VALUES";

         for(List<ActionKeyTemplate> keyLst : this._storedKeys.values()) {
            int order = 0;

            for(ActionKeyTemplate key : keyLst) {
               query = query + key.getSqlSaveString(this._playerObjId, (int)(order++)) + ",";
            }
         }

         query = query.substring(0, query.length() - 1) + ";";

         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement statement = con.prepareStatement(query);
         ) {
            statement.execute();
         } catch (Exception var90) {
            _log.log(Level.WARNING, "Exception: saveInDB(): " + var90.getMessage(), (Throwable)var90);
         }

         this._saved = true;
      }
   }

   public void getCatsFromDB() {
      if (this._storedCategories == null) {
         this._storedCategories = new HashMap<>();

         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM character_ui_categories WHERE `charId` = ? ORDER BY `catId`, `order`");
         ) {
            stmt.setInt(1, this._playerObjId);

            try (ResultSet rs = stmt.executeQuery()) {
               while(rs.next()) {
                  UIParser.addCategory(this._storedCategories, rs.getInt("catId"), rs.getInt("cmdId"));
               }
            }
         } catch (Exception var59) {
            _log.log(Level.WARNING, "Exception: getCatsFromDB(): " + var59.getMessage(), (Throwable)var59);
         }

         if (this._storedCategories.isEmpty()) {
            this._storedCategories = UIParser.getInstance().getCategories();
         }
      }
   }

   public void getKeysFromDB() {
      if (this._storedKeys == null) {
         this._storedKeys = new HashMap<>();

         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM character_ui_actions WHERE `charId` = ? ORDER BY `cat`, `order`");
         ) {
            stmt.setInt(1, this._playerObjId);

            try (ResultSet rs = stmt.executeQuery()) {
               while(rs.next()) {
                  int cat = rs.getInt("cat");
                  int cmd = rs.getInt("cmd");
                  int key = rs.getInt("key");
                  int tgKey1 = rs.getInt("tgKey1");
                  int tgKey2 = rs.getInt("tgKey2");
                  int show = rs.getInt("show");
                  UIParser.addKey(this._storedKeys, cat, new ActionKeyTemplate(cat, cmd, key, tgKey1, tgKey2, show));
               }
            }
         } catch (Exception var64) {
            _log.log(Level.WARNING, "Exception: getKeysFromDB(): " + var64.getMessage(), (Throwable)var64);
         }

         if (this._storedKeys.isEmpty()) {
            this._storedKeys = UIParser.getInstance().getKeys();
         }
      }
   }

   public boolean isSaved() {
      return this._saved;
   }
}
