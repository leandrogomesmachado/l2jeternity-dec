package l2e.gameserver.listener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import l2e.commons.net.IPSettings;
import l2e.gameserver.Config;
import l2e.gameserver.model.quest.Quest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

public final class ScriptListenerLoader {
   private static final Logger _log = Logger.getLogger(ScriptListenerLoader.class.getName());
   public static final File SCRIPT_FOLDER = new File(Config.DATAPACK_ROOT.getAbsolutePath(), "data/scripts");
   private final Map<String, ScriptEngine> _nameEngines = new HashMap<>();
   private final Map<String, ScriptEngine> _extEngines = new HashMap<>();
   private final List<ScriptManagerLoader<?>> _scriptManagers = new LinkedList<>();
   private File _currentLoadingScript;

   protected ScriptListenerLoader() {
      ScriptEngineManager var1 = new ScriptEngineManager();

      for(ScriptEngineFactory var4 : var1.getEngineFactories()) {
         try {
            ScriptEngine var5 = var4.getScriptEngine();
            boolean var6 = false;

            for(String var8 : var4.getNames()) {
               ScriptEngine var9 = this._nameEngines.get(var8);
               if (var9 != null) {
                  double var10 = Double.parseDouble(var4.getEngineVersion());
                  double var12 = Double.parseDouble(var9.getFactory().getEngineVersion());
                  if (var10 <= var12) {
                     continue;
                  }
               }

               var6 = true;
               this._nameEngines.put(var8, var5);
            }

            if (var6) {
               _log.info("Script Engine: " + var4.getEngineName() + " " + var4.getEngineVersion() + " " + var4.getLanguageVersion());
            }

            for(String var16 : var4.getExtensions()) {
               if (!var16.equals("java") || var4.getLanguageName().equals("java")) {
                  this._extEngines.put(var16, var5);
               }
            }
         } catch (Exception var14) {
            _log.log(Level.WARNING, "Failed initializing factory: " + var14.getMessage(), (Throwable)var14);
         }
      }
   }

   private ScriptEngine getEngineByName(String var1) {
      return this._nameEngines.get(var1);
   }

   private ScriptEngine getEngineByExtension(String var1) {
      return this._extEngines.get(var1);
   }

   private List<Class<?>> executeCoreScripts() {
      ArrayList var1 = new ArrayList();

      for(Class var4 : this.getClassesForPackage("l2e.scripts")) {
         try {
            var1.add(var4);
            Method var13 = var4.getMethod("main", String[].class);
            if (var13.getDeclaringClass().equals(var4)) {
               var13.invoke(var4, new String[0]);
            }
            continue;
         } catch (NoSuchMethodException var10) {
         } catch (InvocationTargetException var11) {
            _log.log(Level.WARNING, var11.getMessage());
         } catch (IllegalAccessException var12) {
            _log.log(Level.WARNING, var12.getMessage());
         }

         try {
            Constructor var5 = var4.getConstructor();
            var5.newInstance();
         } catch (NoSuchMethodException var6) {
            _log.log(Level.WARNING, var6.getMessage());
         } catch (InvocationTargetException var7) {
            _log.log(Level.WARNING, var7.getMessage());
         } catch (IllegalAccessException var8) {
            _log.log(Level.WARNING, var8.getMessage());
         } catch (InstantiationException var9) {
            _log.log(Level.WARNING, var9.getMessage());
         }
      }

      return var1;
   }

   private List<File> executeDataScripts() {
      IPSettings.getInstance().autoIpConfig();
      ArrayList var1 = new ArrayList();
      Object var2 = Collections.emptyList();
      File var3 = new File(Config.DATAPACK_ROOT, "data/scripts/");
      if (var3.isDirectory()) {
         var2 = FileUtils.listFiles(var3, FileFilterUtils.suffixFileFilter(".java"), FileFilterUtils.directoryFileFilter());
      }

      if (!var2.isEmpty()) {
         for(File var5 : var2) {
            try {
               this.executeScript(var5);
               var1.add(var5);
            } catch (ScriptException var7) {
               this.reportScriptFileError(var5, var7);
            }
         }
      }

      return var1;
   }

   public void executeAllScriptsInDirectory(File var1) {
      this.executeAllScriptsInDirectory(var1, false, 0);
   }

   public void executeAllScriptsInDirectory(File var1, boolean var2, int var3) {
      this.executeAllScriptsInDirectory(var1, var2, var3, 0);
   }

   private void executeAllScriptsInDirectory(File var1, boolean var2, int var3, int var4) {
      if (!var1.isDirectory()) {
         throw new IllegalArgumentException("The argument directory either doesnt exists or is not an directory.");
      } else {
         for(File var8 : var1.listFiles()) {
            if (var8.isDirectory() && var2 && var3 > var4) {
               this.executeAllScriptsInDirectory(var8, var2, var3, var4 + 1);
            } else if (var8.isFile()) {
               try {
                  String var9 = var8.getName();
                  int var10 = var9.lastIndexOf(46);
                  if (var10 != -1) {
                     String var11 = var9.substring(var10 + 1);
                     ScriptEngine var12 = this.getEngineByExtension(var11);
                     if (var12 != null) {
                        this.executeScript(var12, var8);
                     }
                  }
               } catch (ScriptException var13) {
                  this.reportScriptFileError(var8, var13);
               }
            }
         }
      }
   }

   public void executeScript(File var1) throws ScriptException {
      String var3 = var1.getName();
      int var4 = var3.lastIndexOf(46);
      if (var4 != -1) {
         String var2 = var3.substring(var4 + 1);
         ScriptEngine var5 = this.getEngineByExtension(var2);
         if (var5 == null) {
            throw new ScriptException("No engine registered for extension (" + var2 + ")");
         } else {
            this.executeScript(var5, var1);
         }
      } else {
         throw new ScriptException("Script file (" + var3 + ") doesnt has an extension that identifies the ScriptEngine to be used.");
      }
   }

   public void executeScript(String var1, File var2) throws ScriptException {
      ScriptEngine var3 = this.getEngineByName(var1);
      if (var3 == null) {
         throw new ScriptException("No engine registered with name (" + var1 + ")");
      } else {
         this.executeScript(var3, var2);
      }
   }

   public void executeScript(ScriptEngine var1, File var2) throws ScriptException {
      String var3 = var2.getAbsolutePath() + ".error.log";
      File var4 = new File(var3);
      if (var4.isFile()) {
         var4.delete();
      }

      String var5 = var2.getAbsolutePath().substring(SCRIPT_FOLDER.getAbsolutePath().length() + 1).replace('\\', '/');

      try (
         FileInputStream var6 = new FileInputStream(var2);
         InputStreamReader var8 = new InputStreamReader(var6);
         BufferedReader var10 = new BufferedReader(var8);
      ) {
         if (var1 instanceof Compilable) {
            SimpleScriptContext var12 = new SimpleScriptContext();
            var12.setAttribute("mainClass", getClassForFile(var2).replace('/', '.').replace('\\', '.'), 100);
            var12.setAttribute("javax.script.filename", var5, 100);
            var12.setAttribute("classpath", SCRIPT_FOLDER.getAbsolutePath(), 100);
            var12.setAttribute("sourcepath", SCRIPT_FOLDER.getAbsolutePath(), 100);
            this.setCurrentLoadingScript(var2);
            ScriptContext var13 = var1.getContext();

            try {
               var1.setContext(var12);
               Compilable var14 = (Compilable)var1;
               CompiledScript var15 = var14.compile(var10);
               var15.eval(var12);
            } finally {
               var1.setContext(var13);
               this.setCurrentLoadingScript(null);
               var12.removeAttribute("javax.script.filename", 100);
               var12.removeAttribute("mainClass", 100);
            }
         } else {
            SimpleScriptContext var102 = new SimpleScriptContext();
            var102.setAttribute("mainClass", getClassForFile(var2).replace('/', '.').replace('\\', '.'), 100);
            var102.setAttribute("javax.script.filename", var5, 100);
            var102.setAttribute("classpath", SCRIPT_FOLDER.getAbsolutePath(), 100);
            var102.setAttribute("sourcepath", SCRIPT_FOLDER.getAbsolutePath(), 100);
            this.setCurrentLoadingScript(var2);

            try {
               var1.eval(var10, var102);
            } finally {
               this.setCurrentLoadingScript(null);
               var1.getContext().removeAttribute("javax.script.filename", 100);
               var1.getContext().removeAttribute("mainClass", 100);
            }
         }
      } catch (IOException var101) {
         _log.log(Level.WARNING, "Error executing script!", (Throwable)var101);
      }
   }

   public static String getClassForFile(File var0) {
      String var1 = var0.getAbsolutePath();
      String var2 = SCRIPT_FOLDER.getAbsolutePath();
      if (var1.startsWith(var2)) {
         int var3 = var1.lastIndexOf(46);
         return var1.substring(var2.length() + 1, var3);
      } else {
         return null;
      }
   }

   public ScriptContext getScriptContext(ScriptEngine var1) {
      return var1.getContext();
   }

   public ScriptContext getScriptContext(String var1) {
      ScriptEngine var2 = this.getEngineByName(var1);
      if (var2 == null) {
         throw new IllegalStateException("No engine registered with name (" + var1 + ")");
      } else {
         return this.getScriptContext(var2);
      }
   }

   public Object eval(ScriptEngine var1, String var2, ScriptContext var3) throws ScriptException {
      if (var1 instanceof Compilable) {
         Compilable var4 = (Compilable)var1;
         CompiledScript var5 = var4.compile(var2);
         return var3 != null ? var5.eval(var3) : var5.eval();
      } else {
         return var3 != null ? var1.eval(var2, var3) : var1.eval(var2);
      }
   }

   public Object eval(String var1, String var2) throws ScriptException {
      return this.eval(var1, var2, (ScriptContext)null);
   }

   public Object eval(String var1, String var2, ScriptContext var3) throws ScriptException {
      ScriptEngine var4 = this.getEngineByName(var1);
      if (var4 == null) {
         throw new ScriptException("No engine registered with name (" + var1 + ")");
      } else {
         return this.eval(var4, var2, var3);
      }
   }

   public Object eval(ScriptEngine var1, String var2) throws ScriptException {
      return this.eval(var1, var2, (ScriptContext)null);
   }

   public void reportScriptFileError(File var1, ScriptException var2) {
      String var3 = var1.getParent();
      String var4 = var1.getName() + ".error.log";
      if (var3 != null) {
         File var5 = new File(var3 + "/" + var4);

         try (FileOutputStream var6 = new FileOutputStream(var5)) {
            String var8 = "Error on: "
               + var5.getCanonicalPath()
               + Config.EOL
               + "Line: "
               + var2.getLineNumber()
               + " - Column: "
               + var2.getColumnNumber()
               + Config.EOL
               + Config.EOL;
            var6.write(var8.getBytes());
            var6.write(var2.getMessage().getBytes());
            _log.warning("Failed executing script: " + var1.getAbsolutePath() + ". See " + var5.getName() + " for details.");
         } catch (IOException var19) {
            _log.log(
               Level.WARNING,
               "Failed executing script: "
                  + var1.getAbsolutePath()
                  + Config.EOL
                  + var2.getMessage()
                  + "Additionally failed when trying to write an error report on script directory. Reason: "
                  + var19.getMessage(),
               (Throwable)var19
            );
         }
      } else {
         _log.log(
            Level.WARNING,
            "Failed executing script: "
               + var1.getAbsolutePath()
               + Config.EOL
               + var2.getMessage()
               + "Additionally failed when trying to write an error report on script directory.",
            (Throwable)var2
         );
      }
   }

   public void registerScriptManager(ScriptManagerLoader<?> var1) {
      this._scriptManagers.add(var1);
   }

   public void removeScriptManager(ScriptManagerLoader<?> var1) {
      this._scriptManagers.remove(var1);
   }

   public List<ScriptManagerLoader<?>> getScriptManagers() {
      return this._scriptManagers;
   }

   protected void setCurrentLoadingScript(File var1) {
      this._currentLoadingScript = var1;
   }

   public void executeScriptList() {
      List var1 = this.executeCoreScripts();
      List var2 = this.executeDataScripts();
      int var3 = var1.size() + var2.size();
      if (var3 > 0) {
         _log.info("Loaded: " + var3 + " scripts.");
      }
   }

   private static void addScript(Collection<Class<?>> var0, String var1) {
      try {
         Class var2 = Class.forName(var1);
         if (var2 != null && Quest.class.isAssignableFrom(var2)) {
            var0.add(var2);
         }
      } catch (ClassNotFoundException var3) {
         _log.log(Level.WARNING, var3.getMessage());
      } catch (Throwable var4) {
         _log.log(Level.WARNING, var4.getMessage());
      }
   }

   protected File getCurrentLoadingScript() {
      return this._currentLoadingScript;
   }

   private static Collection<Class<?>> getClassesForPackageInDir(File var0, String var1, Collection<Class<?>> var2) {
      if (!var0.exists()) {
         return var2;
      } else {
         File[] var3 = var0.listFiles();

         for(File var7 : var3) {
            if (var7.isDirectory()) {
               getClassesForPackageInDir(var7, var1 + "." + var7.getName(), var2);
            } else if (var7.getName().endsWith(".class")) {
               addScript(var2, var1 + '.' + var7.getName().substring(0, var7.getName().length() - 6));
            }
         }

         return var2;
      }
   }

   private void getClassesForPackageInJar(URL var1, String var2, Collection<Class<?>> var3) {
      JarInputStream var4 = null;

      try {
         var4 = new JarInputStream(var1.openStream());

         for(JarEntry var5 = var4.getNextJarEntry(); var5 != null; var5 = var4.getNextJarEntry()) {
            String var6 = var5.getName();
            int var7 = var6.lastIndexOf("/");
            if (var7 > 0 && var6.endsWith(".class") && var6.substring(0, var7).startsWith(var2)) {
               addScript(var3, var6.substring(0, var6.length() - 6).replace("/", "."));
            }
         }

         var4.close();
      } catch (IOException var8) {
         _log.log(Level.WARNING, "Can't get classes for url " + var1 + ": " + var8.getMessage());
      }
   }

   public Collection<Class<?>> getClassesForPackage(String var1) {
      String var2 = var1.replace(".", "/");
      ClassLoader var3 = Thread.currentThread().getContextClassLoader();
      HashSet var4 = new HashSet();

      try {
         Enumeration var5 = var3.getResources(var2);
         ArrayList var6 = new ArrayList();

         while(var5.hasMoreElements()) {
            URL var7 = (URL)var5.nextElement();
            var6.add(new File(var7.getFile()));
         }

         for(File var8 : var6) {
            getClassesForPackageInDir(var8, var1, var4);
            this.setCurrentLoadingScript(var8);
         }
      } catch (IOException var10) {
         _log.log(Level.WARNING, var10.getMessage());
      }

      ArrayList var11;
      for(var11 = new ArrayList(); var3 != null; var3 = var3.getParent()) {
         if (var3 instanceof URLClassLoader) {
            for(URL var9 : ((URLClassLoader)var3).getURLs()) {
               if (var9.getFile().endsWith(".jar")) {
                  var11.add(var9);
               }
            }
         }
      }

      for(URL var16 : var11) {
         this.getClassesForPackageInJar(var16, var2, var4);
      }

      return var4;
   }

   public static ScriptListenerLoader getInstance() {
      return ScriptListenerLoader.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final ScriptListenerLoader _instance = new ScriptListenerLoader();
   }
}
