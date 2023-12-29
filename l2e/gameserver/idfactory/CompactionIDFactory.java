package l2e.gameserver.idfactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import l2e.gameserver.Config;
import l2e.gameserver.database.DatabaseFactory;

@Deprecated
public class CompactionIDFactory extends IdFactory {
   private int _curOID = 268435456;
   private final int _freeSize = 0;

   protected CompactionIDFactory() {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         int[] tmp_obj_ids = this.extractUsedObjectIDTable();
         int N = tmp_obj_ids.length;

         for(int idx = 0; idx < N; ++idx) {
            N = this.insertUntil(tmp_obj_ids, idx, N, con);
         }

         ++this._curOID;
         this._log.info(this.getClass().getSimpleName() + ": Next usable Object ID is: " + this._curOID);
         this._initialized = true;
      } catch (Exception var16) {
         this._log.severe(this.getClass().getSimpleName() + ": Could not initialize properly: " + var16.getMessage());
      }
   }

   private int insertUntil(int[] tmp_obj_ids, int idx, int N, Connection con) throws SQLException {
      int id = tmp_obj_ids[idx];
      if (id == this._curOID) {
         ++this._curOID;
         return N;
      } else {
         if (Config.BAD_ID_CHECKING) {
            for(String check : ID_CHECKS) {
               try (PreparedStatement ps = con.prepareStatement(check)) {
                  ps.setInt(1, this._curOID);
                  ps.setInt(2, id);

                  try (ResultSet rs = ps.executeQuery()) {
                     if (rs.next()) {
                        int badId = rs.getInt(1);
                        this._log.severe(this.getClass().getSimpleName() + ": Bad ID " + badId + " in DB found by: " + check);
                        throw new RuntimeException();
                     }
                  }
               }
            }
         }

         int hole = id - this._curOID;
         if (hole > N - idx) {
            hole = N - idx;
         }

         for(int i = 1; i <= hole; ++i) {
            id = tmp_obj_ids[N - i];
            this._log.info(this.getClass().getSimpleName() + ": Compacting DB object ID=" + id + " into " + this._curOID);

            for(String update : ID_UPDATES) {
               try (PreparedStatement ps = con.prepareStatement(update)) {
                  ps.setInt(1, this._curOID);
                  ps.setInt(2, id);
                  ps.execute();
               }
            }

            ++this._curOID;
         }

         if (hole < N - idx) {
            ++this._curOID;
         }

         return N - hole;
      }
   }

   @Override
   public synchronized int getNextId() {
      return this._curOID++;
   }

   @Override
   public synchronized void releaseId(int id) {
   }

   @Override
   public int size() {
      return this._freeSize + Integer.MAX_VALUE - 268435456;
   }
}
