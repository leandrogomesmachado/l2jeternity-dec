package com.sun.mail.util.logging;

import java.io.ObjectStreamException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import javax.mail.Authenticator;

final class LogManagerProperties extends Properties {
   private static final long serialVersionUID = -2239983349056806252L;
   private static final LogManager LOG_MANAGER = LogManager.getLogManager();
   private static volatile String[] REFLECT_NAMES;
   private final String prefix;

   static LogManager getLogManager() {
      return LOG_MANAGER;
   }

   static String toLanguageTag(Locale locale) {
      String l = locale.getLanguage();
      String c = locale.getCountry();
      String v = locale.getVariant();
      char[] b = new char[l.length() + c.length() + v.length() + 2];
      int count = l.length();
      l.getChars(0, count, b, 0);
      if (c.length() != 0 || l.length() != 0 && v.length() != 0) {
         b[count] = '-';
         c.getChars(0, c.length(), b, ++count);
         count += c.length();
      }

      if (v.length() != 0 && (l.length() != 0 || c.length() != 0)) {
         b[count] = '-';
         v.getChars(0, v.length(), b, ++count);
         count += v.length();
      }

      return String.valueOf(b, 0, count);
   }

   static Filter newFilter(String name) throws Exception {
      return newObjectFrom(name, Filter.class);
   }

   static Formatter newFormatter(String name) throws Exception {
      return newObjectFrom(name, Formatter.class);
   }

   static Comparator<? super LogRecord> newComparator(String name) throws Exception {
      return newObjectFrom(name, Comparator.class);
   }

   static <T> Comparator<T> reverseOrder(Comparator<T> c) {
      if (c == null) {
         throw new NullPointerException();
      } else {
         Comparator<T> reverse = null;

         try {
            Method m = c.getClass().getMethod("reversed");
            if (!Modifier.isStatic(m.getModifiers()) && Comparator.class.isAssignableFrom(m.getReturnType())) {
               try {
                  reverse = (Comparator)m.invoke(c);
               } catch (ExceptionInInitializerError var4) {
                  throw wrapOrThrow(var4);
               }
            }
         } catch (NoSuchMethodException var5) {
         } catch (IllegalAccessException var6) {
         } catch (RuntimeException var7) {
         } catch (InvocationTargetException var8) {
            paramOrError(var8);
         }

         if (reverse == null) {
            reverse = Collections.reverseOrder(c);
         }

         return reverse;
      }
   }

   static ErrorManager newErrorManager(String name) throws Exception {
      return newObjectFrom(name, ErrorManager.class);
   }

   static Authenticator newAuthenticator(String name) throws Exception {
      return newObjectFrom(name, Authenticator.class);
   }

   static boolean isStaticUtilityClass(String name) throws Exception {
      Class<?> c = findClass(name);
      Class<?> obj = Object.class;
      Method[] methods = c.getMethods();
      boolean util;
      if (c != obj && methods.length != 0) {
         util = true;

         for(Method m : methods) {
            if (m.getDeclaringClass() != obj && !Modifier.isStatic(m.getModifiers())) {
               util = false;
               break;
            }
         }
      } else {
         util = false;
      }

      return util;
   }

   static boolean isReflectionClass(String name) throws Exception {
      String[] names = REFLECT_NAMES;
      if (REFLECT_NAMES == null) {
         REFLECT_NAMES = names = reflectionClassNames();
      }

      for(String rf : names) {
         if (name.equals(rf)) {
            return true;
         }
      }

      findClass(name);
      return false;
   }

   private static String[] reflectionClassNames() throws Exception {
      Class<?> thisClass = LogManagerProperties.class;

      assert Modifier.isFinal(thisClass.getModifiers()) : thisClass;

      try {
         HashSet<String> traces = new HashSet<>();
         Throwable t = Throwable.class.getConstructor().newInstance();

         for(StackTraceElement ste : t.getStackTrace()) {
            if (thisClass.getName().equals(ste.getClassName())) {
               break;
            }

            traces.add(ste.getClassName());
         }

         Throwable.class.getMethod("fillInStackTrace").invoke(t);

         for(StackTraceElement ste : t.getStackTrace()) {
            if (thisClass.getName().equals(ste.getClassName())) {
               break;
            }

            traces.add(ste.getClassName());
         }

         return traces.toArray(new String[traces.size()]);
      } catch (InvocationTargetException var7) {
         throw paramOrError(var7);
      }
   }

   private static <T> T newObjectFrom(String name, Class<T> type) throws Exception {
      try {
         Class<?> clazz = findClass(name);
         if (type.isAssignableFrom(clazz)) {
            try {
               return type.cast(clazz.getConstructor().newInstance());
            } catch (InvocationTargetException var4) {
               throw paramOrError(var4);
            }
         } else {
            throw new ClassCastException(clazz.getName() + " cannot be cast to " + type.getName());
         }
      } catch (NoClassDefFoundError var5) {
         throw new ClassNotFoundException(var5.toString(), var5);
      } catch (ExceptionInInitializerError var6) {
         throw wrapOrThrow(var6);
      }
   }

   private static Exception paramOrError(InvocationTargetException ite) {
      Throwable cause = ite.getCause();
      if (cause == null || !(cause instanceof VirtualMachineError) && !(cause instanceof ThreadDeath)) {
         return ite;
      } else {
         throw (Error)cause;
      }
   }

   private static InvocationTargetException wrapOrThrow(ExceptionInInitializerError eiie) {
      if (eiie.getCause() instanceof Error) {
         throw eiie;
      } else {
         return new InvocationTargetException(eiie);
      }
   }

   private static Class<?> findClass(String name) throws ClassNotFoundException {
      ClassLoader[] loaders = getClassLoaders();

      assert loaders.length == 2 : loaders.length;

      Class<?> clazz;
      if (loaders[0] != null) {
         try {
            clazz = Class.forName(name, false, loaders[0]);
         } catch (ClassNotFoundException var4) {
            clazz = tryLoad(name, loaders[1]);
         }
      } else {
         clazz = tryLoad(name, loaders[1]);
      }

      return clazz;
   }

   private static Class<?> tryLoad(String name, ClassLoader l) throws ClassNotFoundException {
      return l != null ? Class.forName(name, false, l) : Class.forName(name);
   }

   private static ClassLoader[] getClassLoaders() {
      return AccessController.doPrivileged(new PrivilegedAction<ClassLoader[]>() {
         public ClassLoader[] run() {
            ClassLoader[] loaders = new ClassLoader[2];

            try {
               loaders[0] = ClassLoader.getSystemClassLoader();
            } catch (SecurityException var4) {
               loaders[0] = null;
            }

            try {
               loaders[1] = Thread.currentThread().getContextClassLoader();
            } catch (SecurityException var3) {
               loaders[1] = null;
            }

            return loaders;
         }
      });
   }

   LogManagerProperties(Properties parent, String prefix) {
      super(parent);
      parent.isEmpty();
      if (prefix == null) {
         throw new NullPointerException();
      } else {
         this.prefix = prefix;
         super.isEmpty();
      }
   }

   @Override
   public synchronized Object clone() {
      return this.exportCopy(this.defaults);
   }

   @Override
   public synchronized String getProperty(String key) {
      String value = this.defaults.getProperty(key);
      if (value == null) {
         LogManager manager = getLogManager();
         if (key.length() > 0) {
            value = manager.getProperty(this.prefix + '.' + key);
         }

         if (value == null) {
            value = manager.getProperty(key);
         }

         if (value != null) {
            super.put(key, value);
         } else {
            Object v = super.get(key);
            value = v instanceof String ? (String)v : null;
         }
      }

      return value;
   }

   @Override
   public String getProperty(String key, String def) {
      String value = this.getProperty(key);
      return value == null ? def : value;
   }

   @Override
   public Object get(Object key) {
      return key instanceof String ? this.getProperty((String)key) : super.get(key);
   }

   @Override
   public synchronized Object put(Object key, Object value) {
      Object def = this.preWrite(key);
      Object man = super.put(key, value);
      return man == null ? def : man;
   }

   @Override
   public Object setProperty(String key, String value) {
      return this.put(key, value);
   }

   @Override
   public boolean containsKey(Object key) {
      if (key instanceof String) {
         return this.getProperty((String)key) != null;
      } else {
         return super.containsKey(key);
      }
   }

   @Override
   public synchronized Object remove(Object key) {
      Object def = this.preWrite(key);
      Object man = super.remove(key);
      return man == null ? def : man;
   }

   @Override
   public Enumeration<?> propertyNames() {
      assert false;

      return super.propertyNames();
   }

   @Override
   public boolean equals(Object o) {
      if (o == null) {
         return false;
      } else if (o == this) {
         return true;
      } else if (!(o instanceof Properties)) {
         return false;
      } else {
         assert false : this.prefix;

         return super.equals(o);
      }
   }

   @Override
   public int hashCode() {
      assert false : this.prefix.hashCode();

      return super.hashCode();
   }

   private Object preWrite(Object key) {
      assert Thread.holdsLock(this);

      Object value;
      if (key instanceof String && !super.containsKey(key)) {
         value = this.getProperty((String)key);
      } else {
         value = null;
      }

      return value;
   }

   private Properties exportCopy(Properties parent) {
      Thread.holdsLock(this);
      Properties child = new Properties(parent);
      child.putAll(this);
      return child;
   }

   private synchronized Object writeReplace() throws ObjectStreamException {
      assert false;

      return this.exportCopy((Properties)this.defaults.clone());
   }
}
