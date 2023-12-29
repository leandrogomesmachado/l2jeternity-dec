package jonelo.jacksum.ui;

public class Verbose {
   private boolean warnings;
   private boolean details;
   private boolean summary;

   public Verbose() {
      this.reset();
   }

   public void reset() {
      this.warnings = true;
      this.details = true;
      this.summary = false;
   }

   public void setWarnings(boolean var1) {
      this.warnings = var1;
   }

   public boolean getWarnings() {
      return this.warnings;
   }

   public void setDetails(boolean var1) {
      this.details = var1;
   }

   public boolean getDetails() {
      return this.details;
   }

   public void setSummary(boolean var1) {
      this.summary = var1;
   }

   public boolean getSummary() {
      return this.summary;
   }
}
