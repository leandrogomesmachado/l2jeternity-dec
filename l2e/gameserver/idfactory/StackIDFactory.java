package l2e.gameserver.idfactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Stack;
import l2e.gameserver.Config;
import l2e.gameserver.database.DatabaseFactory;

public class StackIDFactory extends IdFactory {
   private int _curOID;
   private int _tempOID;
   private final Stack<Integer> _freeOIDStack = new Stack<>();

   protected StackIDFactory() {
      this._curOID = 268435456;
      this._tempOID = 268435456;

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         int[] tmp_obj_ids = this.extractUsedObjectIDTable();
         if (tmp_obj_ids.length > 0) {
            this._curOID = tmp_obj_ids[tmp_obj_ids.length - 1];
         }

         this._log.info("Max Id = " + this._curOID);
         int N = tmp_obj_ids.length;

         for(int idx = 0; idx < N; ++idx) {
            N = this.insertUntil(tmp_obj_ids, idx, N, con);
         }

         ++this._curOID;
         this._log.info("IdFactory: Next usable Object ID is: " + this._curOID);
         this._initialized = true;
      } catch (Exception var16) {
         this._log.severe(this.getClass().getSimpleName() + ": Could not be initialized properly:" + var16.getMessage());
      }
   }

   private int insertUntil(int[] tmp_obj_ids, int idx, int N, Connection con) throws SQLException {
      int id = tmp_obj_ids[idx];
      if (id == this._tempOID) {
         ++this._tempOID;
         return N;
      } else {
         if (Config.BAD_ID_CHECKING) {
            for(String check : ID_CHECKS) {
               try (PreparedStatement ps = con.prepareStatement(check)) {
                  ps.setInt(1, this._tempOID);
                  ps.setInt(2, id);

                  try (ResultSet rs = ps.executeQuery()) {
                     if (rs.next()) {
                        int badId = rs.getInt(1);
                        this._log.severe("Bad ID " + badId + " in DB found by: " + check);
                        throw new RuntimeException();
                     }
                  }
               }
            }
         }

         int hole = id - this._tempOID;
         if (hole > N - idx) {
            hole = N - idx;
         }

         for(int i = 1; i <= hole; ++i) {
            this._freeOIDStack.push(this._tempOID);
            ++this._tempOID;
         }

         if (hole < N - idx) {
            ++this._tempOID;
         }

         return N - hole;
      }
   }

   public static IdFactory getInstance() {
      return _instance;
   }

   @Override
   public synchronized int getNextId() {
      int id;
      if (!this._freeOIDStack.empty()) {
         id = this._freeOIDStack.pop();
      } else {
         id = this._curOID++;
      }

      return id;
   }

   @Override
   public synchronized void releaseId(int id) {
      this._freeOIDStack.push(id);
   }

   @Override
   public int size() {
      return 1879048191 - this._curOID + 268435456 + this._freeOIDStack.size();
   }
}
