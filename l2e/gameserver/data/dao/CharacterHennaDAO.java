package l2e.gameserver.data.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.data.parser.HennaParser;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.templates.items.Henna;

public class CharacterHennaDAO {
   private static final Logger _log = Logger.getLogger(CharacterHennaDAO.class.getName());
   private static final String RESTORE_CHAR_HENNAS = "SELECT slot,symbol_id FROM character_hennas WHERE charId=? AND class_index=?";
   private static final String ADD_CHAR_HENNA = "INSERT INTO character_hennas (charId,symbol_id,slot,class_index) VALUES (?,?,?,?)";
   private static final String DELETE_CHAR_HENNA = "DELETE FROM character_hennas WHERE charId=? AND slot=? AND class_index=?";
   private static CharacterHennaDAO _instance = new CharacterHennaDAO();

   public void add(Player player, int symbolId, int slot) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("INSERT INTO character_hennas (charId,symbol_id,slot,class_index) VALUES (?,?,?,?)");
      ) {
         statement.setInt(1, player.getObjectId());
         statement.setInt(2, symbolId);
         statement.setInt(3, slot);
         statement.setInt(4, player.getClassIndex());
         statement.execute();
      } catch (Exception var36) {
         _log.log(Level.SEVERE, "Failed saving character henna.", (Throwable)var36);
      }
   }

   public void delete(Player player, int slot) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement("DELETE FROM character_hennas WHERE charId=? AND slot=? AND class_index=?");
      ) {
         statement.setInt(1, player.getObjectId());
         statement.setInt(2, slot);
         statement.setInt(3, player.getClassIndex());
         statement.execute();
      } catch (Exception var35) {
         _log.log(Level.SEVERE, "Failed remocing character henna.", (Throwable)var35);
      }
   }

   public void restore(Player player) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("SELECT slot,symbol_id FROM character_hennas WHERE charId=? AND class_index=?");
         statement.setInt(1, player.getObjectId());
         statement.setInt(2, player.getClassIndex());
         ResultSet rset = statement.executeQuery();
         Henna[] henna = new Henna[3];

         while(rset.next()) {
            int slot = rset.getInt("slot");
            if (slot >= 1 && slot <= 3) {
               int symbolId = rset.getInt("symbol_id");
               if (symbolId != 0) {
                  henna[slot - 1] = HennaParser.getInstance().getHenna(symbolId);
                  player.setHenna(henna);
               }
            }
         }

         rset.close();
         statement.close();
      } catch (Exception var19) {
         _log.log(Level.SEVERE, "Failed restoing character " + this + " hennas.", (Throwable)var19);
      }
   }

   public static CharacterHennaDAO getInstance() {
      return _instance;
   }
}
