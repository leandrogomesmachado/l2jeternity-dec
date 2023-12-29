package jonelo.sugar.util;

public final class GeneralProgram {
   public static final void requiresMinimumJavaVersion(String var0) {
      try {
         String var1 = System.getProperty("java.vm.version");
         if (isJ2SEcompatible() && var1.compareTo(var0) < 0) {
            System.out
               .println(
                  "ERROR: a newer Java VM is required.\nVendor of your Java VM:        "
                     + System.getProperty("java.vm.vendor")
                     + "\nVersion of your Java VM:       "
                     + var1
                     + "\nRequired minimum J2SE version: "
                     + var0
               );
            System.exit(1);
         }
      } catch (Throwable var2) {
         System.out.println("uncaught exception: " + var2);
         var2.printStackTrace();
      }
   }

   public static boolean isSupportFor(String var0) {
      return isJ2SEcompatible() ? System.getProperty("java.version").compareTo(var0) >= 0 : false;
   }

   public static boolean isJ2SEcompatible() {
      String var0 = System.getProperty("java.vm.vendor");
      return !var0.startsWith("Free Software Foundation") && !var0.startsWith("Kaffe.org");
   }
}
