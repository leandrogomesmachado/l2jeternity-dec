package com.mchange.v1.db.sql;

/** @deprecated */
class DbAuth {
   String username;
   String password;

   public DbAuth(String var1, String var2) {
      this.username = var1;
      this.password = var2;
   }

   public String getUsername() {
      return this.username;
   }

   public String getPassword() {
      return this.password;
   }

   @Override
   public boolean equals(Object var1) {
      if (var1 != null && this.getClass() == var1.getClass()) {
         DbAuth var2 = (DbAuth)var1;
         return this.username.equals(var2.username) && this.password.equals(var2.password);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this.username.hashCode() ^ this.password.hashCode();
   }
}
