package com.mchange.v1.db.sql;

import com.mchange.v1.util.UIterator;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class SimpleCursor implements UIterator {
   ResultSet rs;
   int available = -1;

   public SimpleCursor(ResultSet var1) {
      this.rs = var1;
   }

   @Override
   public boolean hasNext() throws SQLException {
      this.ratchet();
      return this.available == 1;
   }

   @Override
   public Object next() throws SQLException {
      this.ratchet();
      Object var1 = this.objectFromResultSet(this.rs);
      this.clear();
      return var1;
   }

   @Override
   public void remove() {
      throw new UnsupportedOperationException();
   }

   @Override
   public void close() throws Exception {
      this.rs.close();
      this.rs = null;
   }

   @Override
   public void finalize() throws Exception {
      if (this.rs != null) {
         this.close();
      }
   }

   protected abstract Object objectFromResultSet(ResultSet var1) throws SQLException;

   private void ratchet() throws SQLException {
      if (this.available == -1) {
         this.available = this.rs.next() ? 1 : 0;
      }
   }

   private void clear() {
      this.available = -1;
   }
}
