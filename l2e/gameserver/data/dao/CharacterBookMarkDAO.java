package l2e.gameserver.data.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.BookmarkTemplate;

public class CharacterBookMarkDAO {
   private static final Logger _log = Logger.getLogger(CharacterBookMarkDAO.class.getName());
   private static final String INSERT_TP_BOOKMARK = "INSERT INTO character_tpbookmark (charId,Id,x,y,z,icon,tag,name) values (?,?,?,?,?,?,?,?)";
   private static final String UPDATE_TP_BOOKMARK = "UPDATE character_tpbookmark SET icon=?,tag=?,name=? where charId=? AND Id=?";
   private static final String RESTORE_TP_BOOKMARK = "SELECT Id,x,y,z,icon,tag,name FROM character_tpbookmark WHERE charId=?";
   private static final String DELETE_TP_BOOKMARK = "DELETE FROM character_tpbookmark WHERE charId=? AND Id=?";
   private static CharacterBookMarkDAO _instance = new CharacterBookMarkDAO();

   public void add(Player player, int id, int x, int y, int z, int icon, String tag, String name) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("INSERT INTO character_tpbookmark (charId,Id,x,y,z,icon,tag,name) values (?,?,?,?,?,?,?,?)");
      ) {
         statement.setInt(1, player.getObjectId());
         statement.setInt(2, id);
         statement.setInt(3, x);
         statement.setInt(4, y);
         statement.setInt(5, z);
         statement.setInt(6, icon);
         statement.setString(7, tag);
         statement.setString(8, name);
         statement.execute();
      } catch (Exception var41) {
         _log.log(Level.WARNING, "Could not insert character teleport bookmark data: " + var41.getMessage(), (Throwable)var41);
      }
   }

   public void update(Player player, int id, int icon, String tag, String name) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("UPDATE character_tpbookmark SET icon=?,tag=?,name=? where charId=? AND Id=?");
      ) {
         statement.setInt(1, icon);
         statement.setString(2, tag);
         statement.setString(3, name);
         statement.setInt(4, player.getObjectId());
         statement.setInt(5, id);
         statement.execute();
      } catch (Exception var38) {
         _log.log(Level.WARNING, "Could not update character teleport bookmark data: " + var38.getMessage(), (Throwable)var38);
      }
   }

   public void restore(Player player) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("SELECT Id,x,y,z,icon,tag,name FROM character_tpbookmark WHERE charId=?");
      ) {
         statement.setInt(1, player.getObjectId());

         try (ResultSet rset = statement.executeQuery()) {
            while(rset.next()) {
               player.addTeleportBookmarks(
                  rset.getInt("Id"),
                  new BookmarkTemplate(
                     rset.getInt("Id"),
                     rset.getInt("x"),
                     rset.getInt("y"),
                     rset.getInt("z"),
                     rset.getInt("icon"),
                     rset.getString("tag"),
                     rset.getString("name")
                  )
               );
            }
         }
      } catch (Exception var60) {
         _log.log(Level.SEVERE, "Failed restoing character teleport bookmark.", (Throwable)var60);
      }
   }

   public void delete(Player player, int id) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM character_tpbookmark WHERE charId=? AND Id=?");
      ) {
         statement.setInt(1, player.getObjectId());
         statement.setInt(2, id);
         statement.execute();
      } catch (Exception var35) {
         _log.log(Level.WARNING, "Could not delete character teleport bookmark data: " + var35.getMessage(), (Throwable)var35);
      }
   }

   public static CharacterBookMarkDAO getInstance() {
      return _instance;
   }
}
