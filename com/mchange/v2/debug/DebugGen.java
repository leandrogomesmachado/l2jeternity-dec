package com.mchange.v2.debug;

import com.mchange.v1.io.WriterUtils;
import com.mchange.v1.lang.BooleanUtils;
import com.mchange.v1.util.SetUtils;
import com.mchange.v1.util.StringTokenizerUtils;
import com.mchange.v2.cmdline.CommandLineUtils;
import com.mchange.v2.cmdline.ParsedCommandLine;
import com.mchange.v2.io.DirectoryDescentUtils;
import com.mchange.v2.io.FileIterator;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;

public final class DebugGen implements DebugConstants {
   static final String[] VALID = new String[]{
      "codebase", "packages", "trace", "debug", "recursive", "javac", "noclobber", "classname", "skipdirs", "outputbase"
   };
   static final String[] REQUIRED = new String[]{"codebase", "packages", "trace", "debug"};
   static final String[] ARGS = new String[]{"codebase", "packages", "trace", "debug", "classname", "outputbase"};
   static final String EOL = System.getProperty("line.separator");
   static int trace_level;
   static boolean debug;
   static boolean recursive;
   static String classname;
   static boolean clobber;
   static Set skipDirs;

   public static final synchronized void main(String[] var0) {
      try {
         ParsedCommandLine var4 = CommandLineUtils.parse(var0, "--", VALID, REQUIRED, ARGS);
         String var1 = var4.getSwitchArg("codebase");
         var1 = platify(var1);
         if (!var1.endsWith(File.separator)) {
            var1 = var1 + File.separator;
         }

         String var2 = var4.getSwitchArg("outputbase");
         if (var2 != null) {
            var2 = platify(var2);
            if (!var2.endsWith(File.separator)) {
               var2 = var2 + File.separator;
            }
         } else {
            var2 = var1;
         }

         File var5 = new File(var2);
         if (var5.exists()) {
            if (!var5.isDirectory()) {
               System.exit(-1);
            } else if (!var5.canWrite()) {
               System.err.println("Output Base '" + var5.getPath() + "' is not writable!");
               System.exit(-1);
            }
         } else if (!var5.mkdirs()) {
            System.err.println("Output Base directory '" + var5.getPath() + "' does not exist, and could not be created!");
            System.exit(-1);
         }

         String[] var6 = StringTokenizerUtils.tokenizeToArray(var4.getSwitchArg("packages"), ", \t");
         File[] var3 = new File[var6.length];
         int var7 = 0;

         for(int var8 = var6.length; var7 < var8; ++var7) {
            var3[var7] = new File(var1 + sepify(var6[var7]));
         }

         trace_level = Integer.parseInt(var4.getSwitchArg("trace"));
         debug = BooleanUtils.parseBoolean(var4.getSwitchArg("debug"));
         classname = var4.getSwitchArg("classname");
         if (classname == null) {
            classname = "Debug";
         }

         recursive = var4.includesSwitch("recursive");
         clobber = !var4.includesSwitch("noclobber");
         String var13 = var4.getSwitchArg("skipdirs");
         if (var13 != null) {
            String[] var14 = StringTokenizerUtils.tokenizeToArray(var13, ", \t");
            skipDirs = SetUtils.setFromArray(var14);
         } else {
            skipDirs = new HashSet();
            skipDirs.add("CVS");
         }

         if (var4.includesSwitch("javac")) {
            System.err.println("autorecompilation of packages not yet implemented.");
         }

         int var15 = 0;

         for(int var9 = var3.length; var15 < var9; ++var15) {
            if (recursive) {
               if (!recursivePrecheckPackages(var1, var3[var15], var2, var5)) {
                  System.err.println("One or more of the specifies packages could not be processed. Aborting. No files have been modified.");
                  System.exit(-1);
               }
            } else if (!precheckPackage(var1, var3[var15], var2, var5)) {
               System.err.println("One or more of the specifies packages could not be processed. Aborting. No files have been modified.");
               System.exit(-1);
            }
         }

         var15 = 0;

         for(int var17 = var3.length; var15 < var17; ++var15) {
            if (recursive) {
               recursiveWriteDebugFiles(var1, var3[var15], var2, var5);
            } else {
               writeDebugFile(var2, var3[var15]);
            }
         }
      } catch (Exception var10) {
         var10.printStackTrace();
         System.err.println();
         usage();
      }
   }

   private static void usage() {
      System.err.println("java " + DebugGen.class.getName() + " \\");
      System.err.println("\t--codebase=<directory under which packages live> \\  (no default)");
      System.err.println("\t--packages=<comma separated list of packages>    \\  (no default)");
      System.err.println("\t--debug=<true|false>                             \\  (no default)");
      System.err.println("\t--trace=<an int between 0 and 10>                \\  (no default)");
      System.err.println("\t--outputdir=<directory under which to generate>  \\  (defaults to same dir as codebase)");
      System.err.println("\t--recursive                                      \\  (no args)");
      System.err.println("\t--noclobber                                      \\  (no args)");
      System.err.println("\t--classname=<class to generate>                  \\  (defaults to Debug)");
      System.err.println("\t--skipdirs=<directories that should be skipped>  \\  (defaults to CVS)");
   }

   private static String ify(String var0, char var1, char var2) {
      if (var1 == var2) {
         return var0;
      } else {
         StringBuffer var3 = new StringBuffer(var0);
         int var4 = 0;

         for(int var5 = var3.length(); var4 < var5; ++var4) {
            if (var3.charAt(var4) == var1) {
               var3.setCharAt(var4, var2);
            }
         }

         return var3.toString();
      }
   }

   private static String platify(String var0) {
      String var1 = ify(var0, '/', File.separatorChar);
      var1 = ify(var1, '\\', File.separatorChar);
      return ify(var1, ':', File.separatorChar);
   }

   private static String dottify(String var0) {
      return ify(var0, File.separatorChar, '.');
   }

   private static String sepify(String var0) {
      return ify(var0, '.', File.separatorChar);
   }

   private static boolean recursivePrecheckPackages(String var0, File var1, String var2, File var3) throws IOException {
      FileIterator var4 = DirectoryDescentUtils.depthFirstEagerDescent(var1);

      while(var4.hasNext()) {
         File var5 = var4.nextFile();
         if (var5.isDirectory() && !skipDirs.contains(var5.getName())) {
            File var6 = outputDir(var0, var5, var2, var3);
            if (!var6.exists() && !var6.mkdirs()) {
               System.err.println("Required output dir: '" + var6 + "' does not exist, and could not be created.");
               return false;
            }

            if (!precheckOutputPackageDir(var6)) {
               return false;
            }
         }
      }

      return true;
   }

   private static File outputDir(String var0, File var1, String var2, File var3) {
      if (var0.equals(var2)) {
         return var1;
      } else {
         String var4 = var1.getPath();
         if (!var4.startsWith(var0)) {
            System.err.println(DebugGen.class.getName() + ": program bug. Source package path '" + var4 + "' does not begin with codebase '" + var0 + "'.");
            System.exit(-1);
         }

         return new File(var3, var4.substring(var0.length()));
      }
   }

   private static boolean precheckPackage(String var0, File var1, String var2, File var3) throws IOException {
      return precheckOutputPackageDir(outputDir(var0, var1, var2, var3));
   }

   private static boolean precheckOutputPackageDir(File var0) throws IOException {
      File var1 = new File(var0, classname + ".java");
      if (!var0.canWrite()) {
         System.err.println("File '" + var1.getPath() + "' is not writable.");
         return false;
      } else if (!clobber && var1.exists()) {
         System.err.println("File '" + var1.getPath() + "' exists, and we are in noclobber mode.");
         return false;
      } else {
         return true;
      }
   }

   private static void recursiveWriteDebugFiles(String var0, File var1, String var2, File var3) throws IOException {
      FileIterator var4 = DirectoryDescentUtils.depthFirstEagerDescent(outputDir(var0, var1, var2, var3));

      while(var4.hasNext()) {
         File var5 = var4.nextFile();
         if (var5.isDirectory() && !skipDirs.contains(var5.getName())) {
            writeDebugFile(var2, var5);
         }
      }
   }

   private static void writeDebugFile(String var0, File var1) throws IOException {
      File var2 = new File(var1, classname + ".java");
      String var3 = dottify(var1.getPath().substring(var0.length()));
      System.err.println("Writing file: " + var2.getPath());
      OutputStreamWriter var4 = null;

      try {
         var4 = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(var2)), "UTF8");
         var4.write("/********************************************************************" + EOL);
         var4.write(" * This class generated by " + DebugGen.class.getName() + EOL);
         var4.write(" * and will probably be overwritten by the same! Edit at" + EOL);
         var4.write(" * YOUR PERIL!!! Hahahahaha." + EOL);
         var4.write(" ********************************************************************/" + EOL);
         var4.write(EOL);
         var4.write("package " + var3 + ';' + EOL);
         var4.write(EOL);
         var4.write("import com.mchange.v2.debug.DebugConstants;" + EOL);
         var4.write(EOL);
         var4.write("final class " + classname + " implements DebugConstants" + EOL);
         var4.write("{" + EOL);
         var4.write("\tfinal static boolean DEBUG = " + debug + ';' + EOL);
         var4.write("\tfinal static int     TRACE = " + traceStr(trace_level) + ';' + EOL);
         var4.write(EOL);
         var4.write("\tprivate " + classname + "()" + EOL);
         var4.write("\t{}" + EOL);
         var4.write("}" + EOL);
         var4.write(EOL);
         var4.flush();
      } finally {
         WriterUtils.attemptClose(var4);
      }
   }

   private static String traceStr(int var0) {
      if (var0 == 0) {
         return "TRACE_NONE";
      } else if (var0 == 5) {
         return "TRACE_MED";
      } else {
         return var0 == 10 ? "TRACE_MAX" : String.valueOf(var0);
      }
   }
}
