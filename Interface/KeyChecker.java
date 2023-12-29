package Interface;

import java.lang.reflect.Method;
import java.util.Arrays;

public class KeyChecker {
   public static KeyChecker getInstance() {
      return KeyChecker.LazyHolder.INSTANCE;
   }

   private KeyChecker() {
      try {
         System.out.println("============ Interface: Start Init");
         System.out.println("========================================================== You don't have License!");
         System.out.println("============ Loading successful");
      } catch (Exception var5) {
         System.out.println("Error init Interface Key!");
      } finally {
         System.out.println("============ Interface: End Init");
      }
   }

   public static void main(String... args) throws Exception {
      if (args.length == 0) {
         System.out.println("Interface: Main class not specified!");
      } else {
         Class<?> clazz = null;

         try {
            clazz = Class.forName(args[0]);
         } catch (Exception var3) {
         }

         if (clazz == null) {
            System.out.println("Interface: Main class not found : " + args[0] + "!");
         } else {
            getInstance();
            Method main = clazz.getDeclaredMethod("main", String[].class);
            args = Arrays.copyOfRange(args, 1, args.length);
            main.invoke(null, args);
         }
      }
   }

   private static class LazyHolder {
      private static final KeyChecker INSTANCE = new KeyChecker();
   }
}
