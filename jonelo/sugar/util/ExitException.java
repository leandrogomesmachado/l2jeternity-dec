package jonelo.sugar.util;

public class ExitException extends Exception {
   private int exitcode;

   public ExitException(String var1) {
      super(var1);
      this.exitcode = 0;
   }

   public ExitException(String var1, int var2) {
      super(var1);
      this.exitcode = var2;
   }

   public int getExitCode() {
      return this.exitcode;
   }
}
