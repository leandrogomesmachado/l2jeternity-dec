package javax.mail;

public class Provider {
   private Provider.Type type;
   private String protocol;
   private String className;
   private String vendor;
   private String version;

   public Provider(Provider.Type type, String protocol, String classname, String vendor, String version) {
      this.type = type;
      this.protocol = protocol;
      this.className = classname;
      this.vendor = vendor;
      this.version = version;
   }

   public Provider.Type getType() {
      return this.type;
   }

   public String getProtocol() {
      return this.protocol;
   }

   public String getClassName() {
      return this.className;
   }

   public String getVendor() {
      return this.vendor;
   }

   public String getVersion() {
      return this.version;
   }

   @Override
   public String toString() {
      String s = "javax.mail.Provider[" + this.type + "," + this.protocol + "," + this.className;
      if (this.vendor != null) {
         s = s + "," + this.vendor;
      }

      if (this.version != null) {
         s = s + "," + this.version;
      }

      return s + "]";
   }

   public static class Type {
      public static final Provider.Type STORE = new Provider.Type("STORE");
      public static final Provider.Type TRANSPORT = new Provider.Type("TRANSPORT");
      private String type;

      private Type(String type) {
         this.type = type;
      }

      @Override
      public String toString() {
         return this.type;
      }
   }
}
