package com.l2jserver.script.java;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject.Kind;
import org.eclipse.jdt.internal.compiler.tool.EclipseFileManager;

public final class MemoryJavaFileManager extends EclipseFileManager {
   private static final String EXT = ".java";
   protected Map<String, byte[]> classBytes = new HashMap<>();

   public MemoryJavaFileManager() {
      super(null, null);
   }

   public Map<String, byte[]> getClassBytes() {
      return this.classBytes;
   }

   @Override
   public void close() {
      this.classBytes = new HashMap<>();
   }

   @Override
   public void flush() {
   }

   @Override
   public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
      return (JavaFileObject)(kind == Kind.CLASS
         ? new MemoryJavaFileManager.ClassOutputBuffer(className.replace('/', '.'))
         : super.getJavaFileForOutput(location, className, kind, sibling));
   }

   static JavaFileObject makeStringSource(String name, String code) {
      return new MemoryJavaFileManager.StringInputBuffer(name, code);
   }

   static URI toURI(String name) {
      File file = new File(name);
      if (file.exists()) {
         return file.toURI();
      } else {
         try {
            StringBuilder newUri = new StringBuilder();
            newUri.append("file:///");
            newUri.append(name.replace('.', '/'));
            if (name.endsWith(".java")) {
               newUri.replace(newUri.length() - ".java".length(), newUri.length(), ".java");
            }

            return URI.create(newUri.toString());
         } catch (Exception var3) {
            return URI.create("file:///com/sun/script/java/java_source");
         }
      }
   }

   private class ClassOutputBuffer extends SimpleJavaFileObject {
      protected final String name;

      ClassOutputBuffer(String name) {
         super(MemoryJavaFileManager.toURI(name), Kind.CLASS);
         this.name = name;
      }

      @Override
      public OutputStream openOutputStream() {
         return new FilterOutputStream(new ByteArrayOutputStream()) {
            @Override
            public void close() throws IOException {
               this.out.close();
               ByteArrayOutputStream bos = (ByteArrayOutputStream)this.out;
               MemoryJavaFileManager.this.classBytes.put(ClassOutputBuffer.this.name, bos.toByteArray());
            }
         };
      }
   }

   private static class StringInputBuffer extends SimpleJavaFileObject {
      final String code;

      StringInputBuffer(String name, String code) {
         super(MemoryJavaFileManager.toURI(name), Kind.SOURCE);
         this.code = code;
      }

      public CharBuffer getCharContent(boolean ignoreEncodingErrors) {
         return CharBuffer.wrap(this.code);
      }

      public Reader openReader() {
         return new StringReader(this.code);
      }
   }
}
