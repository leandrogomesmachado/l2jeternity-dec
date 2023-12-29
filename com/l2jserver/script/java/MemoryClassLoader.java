package com.l2jserver.script.java;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public final class MemoryClassLoader extends URLClassLoader {
   private final Map<String, byte[]> classBytes;

   public MemoryClassLoader(Map<String, byte[]> classBytes, String classPath, ClassLoader parent) {
      super(toURLs(classPath), parent);
      this.classBytes = classBytes;
   }

   public MemoryClassLoader(Map<String, byte[]> classBytes, String classPath) {
      this(classBytes, classPath, null);
   }

   public Class<?> load(String className) throws ClassNotFoundException {
      return this.loadClass(className);
   }

   public Iterable<Class<?>> loadAll() throws ClassNotFoundException {
      List<Class<?>> classes = new ArrayList<>(this.classBytes.size());

      for(String name : this.classBytes.keySet()) {
         classes.add(this.loadClass(name));
      }

      return classes;
   }

   @Override
   protected Class<?> findClass(String className) throws ClassNotFoundException {
      byte[] buf = (byte[])this.classBytes.get(className);
      if (buf != null) {
         this.classBytes.put(className, null);
         return this.defineClass(className, buf, 0, buf.length);
      } else {
         return super.findClass(className);
      }
   }

   private static URL[] toURLs(String classPath) {
      if (classPath == null) {
         return new URL[0];
      } else {
         List<URL> list = new ArrayList<>();
         StringTokenizer st = new StringTokenizer(classPath, File.pathSeparator);

         while(st.hasMoreTokens()) {
            String token = st.nextToken();
            File file = new File(token);
            if (file.exists()) {
               try {
                  list.add(file.toURI().toURL());
               } catch (MalformedURLException var6) {
               }
            } else {
               try {
                  list.add(new URL(token));
               } catch (MalformedURLException var7) {
               }
            }
         }

         URL[] res = new URL[list.size()];
         list.toArray(res);
         return res;
      }
   }
}
