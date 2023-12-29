package org.apache.commons.dbcp;

/** @deprecated */
public class DbcpException extends RuntimeException {
   private static final long serialVersionUID = 2477800549022838103L;
   protected Throwable cause = null;

   public DbcpException() {
   }

   public DbcpException(String message) {
      this(message, null);
   }

   public DbcpException(String message, Throwable cause) {
      super(message);
      this.cause = cause;
   }

   public DbcpException(Throwable cause) {
      super(cause == null ? (String)null : cause.toString());
      this.cause = cause;
   }

   @Override
   public Throwable getCause() {
      return this.cause;
   }
}
