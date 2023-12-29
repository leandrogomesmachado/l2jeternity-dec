package org.apache.commons.dbcp.datasources;

import java.io.Serializable;

class UserPassKey implements Serializable {
   private static final long serialVersionUID = 5142970911626584817L;
   private final String password;
   private final String username;

   UserPassKey(String username, String password) {
      this.username = username;
      this.password = password;
   }

   public String getPassword() {
      return this.password;
   }

   public String getUsername() {
      return this.username;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      } else if (obj == this) {
         return true;
      } else if (!(obj instanceof UserPassKey)) {
         return false;
      } else {
         UserPassKey key = (UserPassKey)obj;
         return this.username == null ? key.username == null : this.username.equals(key.username);
      }
   }

   @Override
   public int hashCode() {
      return this.username != null ? this.username.hashCode() : 0;
   }

   @Override
   public String toString() {
      StringBuffer sb = new StringBuffer(50);
      sb.append("UserPassKey(");
      sb.append(this.username).append(", ").append(this.password).append(')');
      return sb.toString();
   }
}
