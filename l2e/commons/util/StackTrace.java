package l2e.commons.util;

import java.util.logging.Level;
import java.util.logging.Logger;

public class StackTrace {
   private static final Logger _log = Logger.getLogger(StackTrace.class.getName());

   public static boolean displayStackTraceInformation(Throwable ex) {
      return displayStackTraceInformation(ex, false);
   }

   public static boolean displayStackTraceInformation(Throwable ex, boolean displayAll) {
      if (ex == null) {
         return false;
      } else {
         _log.log(Level.INFO, "", ex);
         if (!displayAll) {
            return true;
         } else {
            StackTraceElement[] stackElements = ex.getStackTrace();
            _log.log(Level.INFO, "The " + stackElements.length + " element" + (stackElements.length == 1 ? "" : "s") + " of the stack trace:\n");

            for(StackTraceElement stackElement : stackElements) {
               _log.log(Level.INFO, "File name: " + stackElement.getFileName());
               _log.log(Level.INFO, "Line number: " + stackElement.getLineNumber());
               String className = stackElement.getClassName();
               String packageName = extractPackageName(className);
               String simpleClassName = extractSimpleClassName(className);
               _log.log(Level.INFO, "Package name: " + ("".equals(packageName) ? "[default package]" : packageName));
               _log.log(Level.INFO, "Full class name: " + className);
               _log.log(Level.INFO, "Simple class name: " + simpleClassName);
               _log.log(Level.INFO, "Unmunged class name: " + unmungeSimpleClassName(simpleClassName));
               _log.log(Level.INFO, "Direct class name: " + extractDirectClassName(simpleClassName));
               _log.log(Level.INFO, "Method name: " + stackElement.getMethodName());
               _log.log(Level.INFO, "Native method?: " + stackElement.isNativeMethod());
               _log.log(Level.INFO, "toString(): " + stackElement.toString());
               _log.log(Level.INFO, "");
            }

            _log.log(Level.INFO, "");
            return true;
         }
      }
   }

   private static String extractPackageName(String fullClassName) {
      if (null != fullClassName && !fullClassName.isEmpty()) {
         int lastDot = fullClassName.lastIndexOf(46);
         return 0 >= lastDot ? "" : fullClassName.substring(0, lastDot);
      } else {
         return "";
      }
   }

   private static String extractSimpleClassName(String fullClassName) {
      if (null != fullClassName && !fullClassName.isEmpty()) {
         int lastDot = fullClassName.lastIndexOf(46);
         return 0 > lastDot ? fullClassName : fullClassName.substring(++lastDot);
      } else {
         return "";
      }
   }

   private static String extractDirectClassName(String simpleClassName) {
      if (null != simpleClassName && !simpleClassName.isEmpty()) {
         int lastSign = simpleClassName.lastIndexOf(36);
         return 0 > lastSign ? simpleClassName : simpleClassName.substring(++lastSign);
      } else {
         return "";
      }
   }

   private static String unmungeSimpleClassName(String simpleClassName) {
      return null != simpleClassName && !simpleClassName.isEmpty() ? simpleClassName.replace('$', '.') : "";
   }
}
