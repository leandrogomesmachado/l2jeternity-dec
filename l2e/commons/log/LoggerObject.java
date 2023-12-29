package l2e.commons.log;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class LoggerObject {
   public final Logger _log = Logger.getLogger(this.getClass().getName());

   public final Logger getLogger() {
      return this._log;
   }

   public void error(String st, Exception e) {
      this._log.log(Level.SEVERE, this.getClass().getSimpleName() + ": " + st, (Throwable)e);
   }

   public void error(String st) {
      this._log.log(Level.SEVERE, this.getClass().getSimpleName() + ": " + st);
   }

   public void warn(String st, Exception e) {
      this._log.log(Level.WARNING, this.getClass().getSimpleName() + ": " + st, (Throwable)e);
   }

   public void warn(String st) {
      this._log.log(Level.WARNING, this.getClass().getSimpleName() + ": " + st);
   }

   public void info(String st, Exception e) {
      this._log.log(Level.INFO, this.getClass().getSimpleName() + ": " + st, (Throwable)e);
   }

   public void info(String st) {
      this._log.info(this.getClass().getSimpleName() + ": " + st);
   }
}
