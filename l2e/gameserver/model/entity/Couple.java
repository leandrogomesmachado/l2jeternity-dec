package l2e.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.model.actor.Player;

public class Couple {
   private static final Logger _log = Logger.getLogger(Couple.class.getName());
   private int _Id = 0;
   private int _player1Id = 0;
   private int _player2Id = 0;
   private boolean _maried = false;
   private Calendar _affiancedDate;
   private Calendar _weddingDate;

   public Couple(int coupleId) {
      this._Id = coupleId;

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("SELECT * FROM mods_wedding WHERE id = ?");
         statement.setInt(1, this._Id);
         ResultSet rs = statement.executeQuery();

         while(rs.next()) {
            this._player1Id = rs.getInt("player1Id");
            this._player2Id = rs.getInt("player2Id");
            this._maried = rs.getBoolean("married");
            this._affiancedDate = Calendar.getInstance();
            this._affiancedDate.setTimeInMillis(rs.getLong("affianceDate"));
            this._weddingDate = Calendar.getInstance();
            this._weddingDate.setTimeInMillis(rs.getLong("weddingDate"));
         }

         rs.close();
         statement.close();
      } catch (Exception var16) {
         _log.log(Level.SEVERE, "Exception: Couple.load(): " + var16.getMessage(), (Throwable)var16);
      }
   }

   public Couple(Player player1, Player player2) {
      int _tempPlayer1Id = player1.getObjectId();
      int _tempPlayer2Id = player2.getObjectId();
      this._player1Id = _tempPlayer1Id;
      this._player2Id = _tempPlayer2Id;
      this._affiancedDate = Calendar.getInstance();
      this._affiancedDate.setTimeInMillis(Calendar.getInstance().getTimeInMillis());
      this._weddingDate = Calendar.getInstance();
      this._weddingDate.setTimeInMillis(Calendar.getInstance().getTimeInMillis());

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         this._Id = IdFactory.getInstance().getNextId();
         PreparedStatement statement = con.prepareStatement(
            "INSERT INTO mods_wedding (id, player1Id, player2Id, married, affianceDate, weddingDate) VALUES (?, ?, ?, ?, ?, ?)"
         );
         statement.setInt(1, this._Id);
         statement.setInt(2, this._player1Id);
         statement.setInt(3, this._player2Id);
         statement.setBoolean(4, false);
         statement.setLong(5, this._affiancedDate.getTimeInMillis());
         statement.setLong(6, this._weddingDate.getTimeInMillis());
         statement.execute();
         statement.close();
      } catch (Exception var18) {
         _log.log(Level.SEVERE, "Could not create couple: " + var18.getMessage(), (Throwable)var18);
      }
   }

   public void marry() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("UPDATE mods_wedding set married = ?, weddingDate = ? where id = ?");
         statement.setBoolean(1, true);
         this._weddingDate = Calendar.getInstance();
         statement.setLong(2, this._weddingDate.getTimeInMillis());
         statement.setInt(3, this._Id);
         statement.execute();
         statement.close();
         this._maried = true;
      } catch (Exception var14) {
         _log.log(Level.SEVERE, "Could not marry: " + var14.getMessage(), (Throwable)var14);
      }
   }

   public void divorce() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("DELETE FROM mods_wedding WHERE id=?");
         statement.setInt(1, this._Id);
         statement.execute();
         statement.close();
      } catch (Exception var14) {
         _log.log(Level.SEVERE, "Exception: Couple.divorce(): " + var14.getMessage(), (Throwable)var14);
      }
   }

   public final int getId() {
      return this._Id;
   }

   public final int getPlayer1Id() {
      return this._player1Id;
   }

   public final int getPlayer2Id() {
      return this._player2Id;
   }

   public final boolean getMaried() {
      return this._maried;
   }

   public final Calendar getAffiancedDate() {
      return this._affiancedDate;
   }

   public final Calendar getWeddingDate() {
      return this._weddingDate;
   }
}
