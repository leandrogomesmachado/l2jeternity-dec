package org.apache.commons.dbcp;

import java.sql.SQLException;

/** @deprecated */
public class SQLNestedException extends SQLException {
   private static final long serialVersionUID = 1046151479543081202L;

   public SQLNestedException(String msg, Throwable cause) {
      super(msg);
      if (cause != null) {
         this.initCause(cause);
      }
   }
}
