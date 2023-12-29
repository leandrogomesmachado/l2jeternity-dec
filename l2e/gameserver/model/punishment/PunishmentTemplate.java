package l2e.gameserver.model.punishment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.database.DatabaseFactory;

public class PunishmentTemplate {
   protected static final Logger _log = Logger.getLogger(PunishmentTemplate.class.getName());
   private static final String INSERT_QUERY = "INSERT INTO punishments (`key`, `affect`, `type`, `expiration`, `reason`, `punishedBy`) VALUES (?, ?, ?, ?, ?, ?)";
   private int _id;
   private final String _key;
   private final PunishmentAffect _affect;
   private final PunishmentType _type;
   private final long _expirationTime;
   private final String _reason;
   private final String _punishedBy;
   private boolean _isStored;

   public PunishmentTemplate(String key, PunishmentAffect affect, PunishmentType type, long expirationTime, String reason, String punishedBy) {
      this(0, key, affect, type, expirationTime, reason, punishedBy, false);
   }

   public PunishmentTemplate(
      int id, String key, PunishmentAffect affect, PunishmentType type, long expirationTime, String reason, String punishedBy, boolean isStored
   ) {
      this._id = id;
      this._key = String.valueOf(key);
      this._affect = affect;
      this._type = type;
      this._expirationTime = expirationTime;
      this._reason = reason;
      this._punishedBy = punishedBy;
      this._isStored = isStored;
      if (!isStored) {
         this.startPunishment();
      }
   }

   public int getId() {
      return this._id;
   }

   public String getKey() {
      return this._key;
   }

   public PunishmentAffect getAffect() {
      return this._affect;
   }

   public PunishmentType getType() {
      return this._type;
   }

   public final long getExpirationTime() {
      return this._expirationTime;
   }

   public String getReason() {
      return this._reason;
   }

   public String getPunishedBy() {
      return this._punishedBy;
   }

   public boolean isStored() {
      return this._isStored;
   }

   private void startPunishment() {
      if (!this._isStored) {
         try (
            Connection con = DatabaseFactory.getInstance().getConnection();
            PreparedStatement st = con.prepareStatement(
               "INSERT INTO punishments (`key`, `affect`, `type`, `expiration`, `reason`, `punishedBy`) VALUES (?, ?, ?, ?, ?, ?)", 1
            );
         ) {
            st.setString(1, this._key);
            st.setString(2, this._affect.name());
            st.setString(3, this._type.name());
            st.setLong(4, this._expirationTime);
            st.setString(5, this._reason);
            st.setString(6, this._punishedBy);
            st.execute();

            try (ResultSet rset = st.getGeneratedKeys()) {
               if (rset.next()) {
                  this._id = rset.getInt(1);
               }
            }

            this._isStored = true;
         } catch (SQLException var59) {
            _log.log(
               Level.WARNING, this.getClass().getSimpleName() + ": Couldn't store punishment task for: " + this._affect + " " + this._key, (Throwable)var59
            );
         }
      }
   }
}
