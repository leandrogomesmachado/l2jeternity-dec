package com.l2jserver.script.java;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

public class JavaScriptEngine extends AbstractScriptEngine implements Compilable {
   private final JavaCompiler compiler = new JavaCompiler();
   private ScriptEngineFactory factory;
   private static final String SYSPROP_PREFIX = "com.sun.script.java.";
   private static final String[] EMPTY_STRING_ARRAY = new String[0];
   private static final String ARGUMENTS = "arguments";
   private static final String SOURCEPATH = "sourcepath";
   private static final String CLASSPATH = "classpath";
   private static final String MAINCLASS = "mainClass";
   private static final String PARENTLOADER = "parentLoader";

   @Override
   public CompiledScript compile(String script) throws ScriptException {
      return this.compile(script, this.context);
   }

   @Override
   public CompiledScript compile(Reader reader) throws ScriptException {
      return this.compile(this.readFully(reader));
   }

   @Override
   public Object eval(String str, ScriptContext ctx) throws ScriptException {
      Class<?> clazz = this.parse(str, ctx);
      return evalClass(clazz, ctx);
   }

   @Override
   public Object eval(Reader reader, ScriptContext ctx) throws ScriptException {
      return this.eval(this.readFully(reader), ctx);
   }

   @Override
   public ScriptEngineFactory getFactory() {
      synchronized(this) {
         if (this.factory == null) {
            this.factory = new JavaScriptEngineFactory();
         }
      }

      return this.factory;
   }

   @Override
   public Bindings createBindings() {
      return new SimpleBindings();
   }

   void setFactory(ScriptEngineFactory factory) {
      this.factory = factory;
   }

   private Class<?> parse(String str, ScriptContext ctx) throws ScriptException {
      String fileName = getFileName(ctx);
      String sourcePath = getSourcePath(ctx);
      String classPath = getClassPath(ctx);
      Writer err = ctx.getErrorWriter();
      if (err == null) {
         err = new StringWriter();
      }

      Map<String, byte[]> classBytes = this.compiler.compile(fileName, str, err, sourcePath, classPath);
      if (classBytes == null) {
         if (err instanceof StringWriter) {
            throw new ScriptException(((StringWriter)err).toString());
         } else {
            throw new ScriptException("compilation failed");
         }
      } else {
         MemoryClassLoader loader = new MemoryClassLoader(classBytes, classPath, getParentLoader(ctx));
         return parseMain(loader, ctx);
      }
   }

   protected static Class<?> parseMain(MemoryClassLoader loader, ScriptContext ctx) throws ScriptException {
      String mainClassName = getMainClassName(ctx);
      if (mainClassName != null) {
         try {
            Class<?> clazz = loader.load(mainClassName);
            Method mainMethod = findMainMethod(clazz);
            if (mainMethod == null) {
               throw new ScriptException("no main method in " + mainClassName);
            } else {
               return clazz;
            }
         } catch (ClassNotFoundException var6) {
            var6.printStackTrace();
            throw new ScriptException(var6);
         }
      } else {
         Iterable<Class<?>> classes;
         try {
            classes = loader.loadAll();
         } catch (ClassNotFoundException var7) {
            throw new ScriptException(var7);
         }

         Class<?> c = findMainClass(classes);
         if (c != null) {
            return c;
         } else {
            Iterator<Class<?>> itr = classes.iterator();
            return itr.hasNext() ? itr.next() : null;
         }
      }
   }

   private JavaScriptEngine.JavaCompiledScript compile(String str, ScriptContext ctx) throws ScriptException {
      String fileName = getFileName(ctx);
      String sourcePath = getSourcePath(ctx);
      String classPath = getClassPath(ctx);
      Writer err = ctx.getErrorWriter();
      if (err == null) {
         err = new StringWriter();
      }

      Map<String, byte[]> classBytes = this.compiler.compile(fileName, str, err, sourcePath, classPath);
      if (classBytes == null) {
         if (err instanceof StringWriter) {
            throw new ScriptException(((StringWriter)err).toString());
         } else {
            throw new ScriptException("compilation failed");
         }
      } else {
         return new JavaScriptEngine.JavaCompiledScript(this, classBytes, classPath);
      }
   }

   private static Class<?> findMainClass(Iterable<Class<?>> classes) {
      for(Class<?> clazz : classes) {
         int modifiers = clazz.getModifiers();
         if (Modifier.isPublic(modifiers)) {
            Method mainMethod = findMainMethod(clazz);
            if (mainMethod != null) {
               return clazz;
            }
         }
      }

      for(Class<?> clazz : classes) {
         Method mainMethod = findMainMethod(clazz);
         if (mainMethod != null) {
            return clazz;
         }
      }

      return null;
   }

   private static Method findMainMethod(Class<?> clazz) {
      try {
         Method mainMethod = clazz.getMethod("main", String[].class);
         int modifiers = mainMethod.getModifiers();
         if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers)) {
            return mainMethod;
         }
      } catch (NoSuchMethodException var3) {
      }

      return null;
   }

   private static Method findSetScriptContextMethod(Class<?> clazz) {
      try {
         Method setCtxMethod = clazz.getMethod("setScriptContext", ScriptContext.class);
         int modifiers = setCtxMethod.getModifiers();
         if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers)) {
            return setCtxMethod;
         }
      } catch (NoSuchMethodException var3) {
      }

      return null;
   }

   private static String getFileName(ScriptContext ctx) {
      int scope = ctx.getAttributesScope("javax.script.filename");
      return scope != -1 ? ctx.getAttribute("javax.script.filename", scope).toString() : "$unnamed.java";
   }

   private static String[] getArguments(ScriptContext ctx) {
      int scope = ctx.getAttributesScope("arguments");
      if (scope != -1) {
         Object obj = ctx.getAttribute("arguments", scope);
         if (obj instanceof String[]) {
            return (String[])obj;
         }
      }

      return EMPTY_STRING_ARRAY;
   }

   private static String getSourcePath(ScriptContext ctx) {
      int scope = ctx.getAttributesScope("sourcepath");
      return scope != -1 ? ctx.getAttribute("sourcepath").toString() : System.getProperty("com.sun.script.java.sourcepath");
   }

   private static String getClassPath(ScriptContext ctx) {
      int scope = ctx.getAttributesScope("classpath");
      if (scope != -1) {
         return ctx.getAttribute("classpath").toString();
      } else {
         String res = System.getProperty("com.sun.script.java.classpath");
         if (res == null) {
            res = System.getProperty("java.class.path");
         }

         return res;
      }
   }

   private static String getMainClassName(ScriptContext ctx) {
      int scope = ctx.getAttributesScope("mainClass");
      return scope != -1 ? ctx.getAttribute("mainClass").toString() : System.getProperty("com.sun.script.java.mainClass");
   }

   protected static ClassLoader getParentLoader(ScriptContext ctx) {
      int scope = ctx.getAttributesScope("parentLoader");
      if (scope != -1) {
         Object loader = ctx.getAttribute("parentLoader");
         if (loader instanceof ClassLoader) {
            return (ClassLoader)loader;
         }
      }

      return ClassLoader.getSystemClassLoader();
   }

   protected static Object evalClass(Class<?> clazz, ScriptContext ctx) throws ScriptException {
      ctx.setAttribute("context", ctx, 100);
      if (clazz == null) {
         return null;
      } else {
         try {
            boolean isPublicClazz = Modifier.isPublic(clazz.getModifiers());
            Method setCtxMethod = findSetScriptContextMethod(clazz);
            if (setCtxMethod != null) {
               if (!isPublicClazz) {
                  setCtxMethod.setAccessible(true);
               }

               setCtxMethod.invoke(null, ctx);
            }

            Method mainMethod = findMainMethod(clazz);
            if (mainMethod != null) {
               if (!isPublicClazz) {
                  mainMethod.setAccessible(true);
               }

               String[] args = getArguments(ctx);
               mainMethod.invoke(null, args);
            }

            return clazz;
         } catch (Exception var6) {
            var6.printStackTrace();
            throw new ScriptException(var6);
         }
      }
   }

   private String readFully(Reader reader) throws ScriptException {
      char[] arr = new char[8192];
      StringBuilder buf = new StringBuilder();

      int numChars;
      try {
         while((numChars = reader.read(arr, 0, arr.length)) > 0) {
            buf.append(arr, 0, numChars);
         }
      } catch (IOException var6) {
         throw new ScriptException(var6);
      }

      return buf.toString();
   }

   private static class JavaCompiledScript extends CompiledScript implements Serializable {
      private static final long serialVersionUID = 1L;
      private final transient JavaScriptEngine _engine;
      private transient Class<?> _class;
      private final Map<String, byte[]> _classBytes;
      private final String _classPath;

      JavaCompiledScript(JavaScriptEngine engine, Map<String, byte[]> classBytes, String classPath) {
         this._engine = engine;
         this._classBytes = classBytes;
         this._classPath = classPath;
      }

      @Override
      public ScriptEngine getEngine() {
         return this._engine;
      }

      @Override
      public Object eval(ScriptContext ctx) throws ScriptException {
         if (this._class == null) {
            Map<String, byte[]> classBytesCopy = new HashMap<>();
            classBytesCopy.putAll(this._classBytes);
            MemoryClassLoader loader = new MemoryClassLoader(classBytesCopy, this._classPath, JavaScriptEngine.getParentLoader(ctx));
            this._class = JavaScriptEngine.parseMain(loader, ctx);
         }

         return JavaScriptEngine.evalClass(this._class, ctx);
      }
   }
}
