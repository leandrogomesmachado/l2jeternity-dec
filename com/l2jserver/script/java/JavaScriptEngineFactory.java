package com.l2jserver.script.java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

public class JavaScriptEngineFactory implements ScriptEngineFactory {
   private static long nextClassNum = 0L;
   private static List<String> names = new ArrayList<>(1);
   private static List<String> extensions;
   private static List<String> mimeTypes = new ArrayList<>(0);

   @Override
   public String getEngineName() {
      return "java";
   }

   @Override
   public String getEngineVersion() {
      return "1.8";
   }

   @Override
   public List<String> getExtensions() {
      return extensions;
   }

   @Override
   public String getLanguageName() {
      return "java";
   }

   @Override
   public String getLanguageVersion() {
      return "1.8";
   }

   @Override
   public String getMethodCallSyntax(String obj, String m, String... args) {
      StringBuilder buf = new StringBuilder();
      buf.append(obj);
      buf.append('.');
      buf.append(m);
      buf.append('(');
      if (args.length != 0) {
         int i;
         for(i = 0; i < args.length - 1; ++i) {
            buf.append(args[i] + ", ");
         }

         buf.append(args[i]);
      }

      buf.append(')');
      return buf.toString();
   }

   @Override
   public List<String> getMimeTypes() {
      return mimeTypes;
   }

   @Override
   public List<String> getNames() {
      return names;
   }

   @Override
   public String getOutputStatement(String toDisplay) {
      StringBuilder buf = new StringBuilder();
      buf.append("System.out.print(\"");
      int len = toDisplay.length();

      for(int i = 0; i < len; ++i) {
         char ch = toDisplay.charAt(i);
         switch(ch) {
            case '"':
               buf.append("\\\"");
               break;
            case '\\':
               buf.append("\\\\");
               break;
            default:
               buf.append(ch);
         }
      }

      buf.append("\");");
      return buf.toString();
   }

   public String getParameter(String key) {
      if (key.equals("javax.script.engine")) {
         return this.getEngineName();
      } else if (key.equals("javax.script.engine_version")) {
         return this.getEngineVersion();
      } else if (key.equals("javax.script.name")) {
         return this.getEngineName();
      } else if (key.equals("javax.script.language")) {
         return this.getLanguageName();
      } else if (key.equals("javax.script.language_version")) {
         return this.getLanguageVersion();
      } else {
         return key.equals("THREADING") ? "MULTITHREADED" : null;
      }
   }

   @Override
   public String getProgram(String... statements) {
      StringBuilder buf = new StringBuilder();
      buf.append("class ");
      buf.append(this.getClassName());
      buf.append(" {\n");
      buf.append("    public static void main(String[] args) {\n");
      if (statements.length != 0) {
         for(String statement : statements) {
            buf.append("        ");
            buf.append(statement);
            buf.append(";\n");
         }
      }

      buf.append("    }\n");
      buf.append("}\n");
      return buf.toString();
   }

   @Override
   public ScriptEngine getScriptEngine() {
      JavaScriptEngine engine = new JavaScriptEngine();
      engine.setFactory(this);
      return engine;
   }

   private String getClassName() {
      return "com_sun_script_java_Main$" + getNextClassNumber();
   }

   private static synchronized long getNextClassNumber() {
      return (long)(nextClassNum++);
   }

   static {
      names.add("java");
      names = Collections.unmodifiableList(names);
      extensions = names;
      mimeTypes = Collections.unmodifiableList(mimeTypes);
   }
}
