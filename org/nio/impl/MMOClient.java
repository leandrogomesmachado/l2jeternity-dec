package org.nio.impl;

import java.nio.ByteBuffer;

public abstract class MMOClient<T extends MMOConnection<?>> {
   private T _connection;
   private boolean isAuthed;

   public MMOClient(T con) {
      this._connection = con;
   }

   protected void setConnection(T con) {
      this._connection = con;
   }

   public T getConnection() {
      return this._connection;
   }

   public boolean isAuthed() {
      return this.isAuthed;
   }

   public void setAuthed(boolean isAuthed) {
      this.isAuthed = isAuthed;
   }

   public void closeNow(boolean error) {
      T conn = this._connection;
      if (conn != null && !conn.isClosed()) {
         conn.closeNow();
      }
   }

   public void closeLater() {
      T conn = this._connection;
      if (conn != null && !conn.isClosed()) {
         conn.closeLater();
      }
   }

   public boolean isConnected() {
      T conn = this._connection;
      return conn != null && !conn.isClosed();
   }

   public abstract boolean decrypt(ByteBuffer var1, int var2);

   public abstract boolean encrypt(ByteBuffer var1, int var2);

   protected void onDisconnection() {
   }

   protected void onForcedDisconnection() {
   }
}
