package com.mchange.v1.db.sql;

import java.sql.ResultSet;

public abstract class CBPCursor extends SimpleCursor {
   ConnectionBundle returnMe;
   ConnectionBundlePool home;

   public CBPCursor(ResultSet var1, ConnectionBundle var2, ConnectionBundlePool var3) {
      super(var1);
      this.returnMe = var2;
      this.home = var3;
   }

   @Override
   public void close() throws Exception {
      try {
         super.close();
      } finally {
         this.home.checkinBundle(this.returnMe);
      }
   }
}
