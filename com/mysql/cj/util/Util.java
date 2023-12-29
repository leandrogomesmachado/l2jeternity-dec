package com.mysql.cj.util;

import com.mysql.cj.Constants;
import com.mysql.cj.Messages;
import com.mysql.cj.exceptions.CJException;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.exceptions.WrongArgumentException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Util {
   private static int jvmVersion = 8;
   private static int jvmUpdateNumber = -1;
   private static boolean isColdFusion = false;
   private static final ConcurrentMap<Class<?>, Boolean> isJdbcInterfaceCache;
   private static final ConcurrentMap<Class<?>, Class<?>[]> implementedInterfacesCache;

   public static int getJVMVersion() {
      return jvmVersion;
   }

   public static boolean jvmMeetsMinimum(int version, int updateNumber) {
      return getJVMVersion() > version || getJVMVersion() == version && getJVMUpdateNumber() >= updateNumber;
   }

   public static int getJVMUpdateNumber() {
      return jvmUpdateNumber;
   }

   public static boolean isColdFusion() {
      return isColdFusion;
   }

   public static boolean isCommunityEdition(String serverVersion) {
      return !isEnterpriseEdition(serverVersion);
   }

   public static boolean isEnterpriseEdition(String serverVersion) {
      return serverVersion.contains("enterprise") || serverVersion.contains("commercial") || serverVersion.contains("advanced");
   }

   public static String stackTraceToString(Throwable ex) {
      StringBuilder traceBuf = new StringBuilder();
      traceBuf.append(Messages.getString("Util.1"));
      if (ex != null) {
         traceBuf.append(ex.getClass().getName());
         String message = ex.getMessage();
         if (message != null) {
            traceBuf.append(Messages.getString("Util.2"));
            traceBuf.append(message);
         }

         StringWriter out = new StringWriter();
         PrintWriter printOut = new PrintWriter(out);
         ex.printStackTrace(printOut);
         traceBuf.append(Messages.getString("Util.3"));
         traceBuf.append(out.toString());
      }

      traceBuf.append(Messages.getString("Util.4"));
      return traceBuf.toString();
   }

   public static Object getInstance(String className, Class<?>[] argTypes, Object[] args, ExceptionInterceptor exceptionInterceptor, String errorMessage) {
      try {
         return handleNewInstance(Class.forName(className).getConstructor(argTypes), args, exceptionInterceptor);
      } catch (NoSuchMethodException | ClassNotFoundException | SecurityException var6) {
         throw (WrongArgumentException)ExceptionFactory.createException(WrongArgumentException.class, errorMessage, var6, exceptionInterceptor);
      }
   }

   public static Object getInstance(String className, Class<?>[] argTypes, Object[] args, ExceptionInterceptor exceptionInterceptor) {
      return getInstance(className, argTypes, args, exceptionInterceptor, "Can't instantiate required class");
   }

   public static Object handleNewInstance(Constructor<?> ctor, Object[] args, ExceptionInterceptor exceptionInterceptor) {
      try {
         return ctor.newInstance(args);
      } catch (InstantiationException | IllegalAccessException | IllegalArgumentException var5) {
         throw (WrongArgumentException)ExceptionFactory.createException(
            WrongArgumentException.class, "Can't instantiate required class", var5, exceptionInterceptor
         );
      } catch (InvocationTargetException var6) {
         Throwable target = var6.getTargetException();
         if (target instanceof ExceptionInInitializerError) {
            target = ((ExceptionInInitializerError)target).getException();
         } else if (target instanceof CJException) {
            throw (CJException)target;
         }

         throw (WrongArgumentException)ExceptionFactory.createException(WrongArgumentException.class, target.getMessage(), target, exceptionInterceptor);
      }
   }

   public static boolean interfaceExists(String hostname) {
      try {
         Class<?> networkInterfaceClass = Class.forName("java.net.NetworkInterface");
         return networkInterfaceClass.getMethod("getByName", (Class<?>[])null).invoke(networkInterfaceClass, hostname) != null;
      } catch (Throwable var2) {
         return false;
      }
   }

   public static Map<Object, Object> calculateDifferences(Map<?, ?> map1, Map<?, ?> map2) {
      Map<Object, Object> diffMap = new HashMap<>();
      Iterator var3 = map1.entrySet().iterator();

      while(true) {
         Object key;
         Object var10;
         Object var11;
         while(true) {
            if (!var3.hasNext()) {
               return diffMap;
            }

            Entry<?, ?> entry = (Entry)var3.next();
            key = entry.getKey();
            Number value1 = null;
            Number value2 = null;
            if (entry.getValue() instanceof Number) {
               var10 = (Number)entry.getValue();
               var11 = (Number)map2.get(key);
               break;
            }

            try {
               var10 = new Double(entry.getValue().toString());
               var11 = new Double(map2.get(key).toString());
               break;
            } catch (NumberFormatException var9) {
            }
         }

         if (!var10.equals(var11)) {
            if (var10 instanceof Byte) {
               diffMap.put(key, (byte)((Byte)var11 - (Byte)var10));
            } else if (var10 instanceof Short) {
               diffMap.put(key, (short)((Short)var11 - (Short)var10));
            } else if (var10 instanceof Integer) {
               diffMap.put(key, (Integer)var11 - (Integer)var10);
            } else if (var10 instanceof Long) {
               diffMap.put(key, (Long)var11 - (Long)var10);
            } else if (var10 instanceof Float) {
               diffMap.put(key, (Float)var11 - (Float)var10);
            } else if (var10 instanceof Double) {
               diffMap.put(key, (double)(((Double)var11).shortValue() - ((Double)var10).shortValue()));
            } else if (var10 instanceof BigDecimal) {
               diffMap.put(key, ((BigDecimal)var11).subtract((BigDecimal)var10));
            } else if (var10 instanceof BigInteger) {
               diffMap.put(key, ((BigInteger)var11).subtract((BigInteger)var10));
            }
         }
      }
   }

   public static <T> List<T> loadClasses(String extensionClassNames, String errorMessageKey, ExceptionInterceptor exceptionInterceptor) {
      List<T> instances = new LinkedList<>();
      List<String> interceptorsToCreate = StringUtils.split(extensionClassNames, ",", true);
      String className = null;

      try {
         int i = 0;

         for(int s = interceptorsToCreate.size(); i < s; ++i) {
            className = interceptorsToCreate.get(i);
            T instance = (T)Class.forName(className).newInstance();
            instances.add(instance);
         }

         return instances;
      } catch (Throwable var9) {
         throw (WrongArgumentException)ExceptionFactory.createException(
            WrongArgumentException.class, Messages.getString(errorMessageKey, new Object[]{className}), var9, exceptionInterceptor
         );
      }
   }

   public static boolean isJdbcInterface(Class<?> clazz) {
      if (isJdbcInterfaceCache.containsKey(clazz)) {
         return isJdbcInterfaceCache.get(clazz);
      } else {
         if (clazz.isInterface()) {
            try {
               if (isJdbcPackage(clazz.getPackage().getName())) {
                  isJdbcInterfaceCache.putIfAbsent(clazz, true);
                  return true;
               }
            } catch (Exception var5) {
            }
         }

         for(Class<?> iface : clazz.getInterfaces()) {
            if (isJdbcInterface(iface)) {
               isJdbcInterfaceCache.putIfAbsent(clazz, true);
               return true;
            }
         }

         if (clazz.getSuperclass() != null && isJdbcInterface(clazz.getSuperclass())) {
            isJdbcInterfaceCache.putIfAbsent(clazz, true);
            return true;
         } else {
            isJdbcInterfaceCache.putIfAbsent(clazz, false);
            return false;
         }
      }
   }

   public static boolean isJdbcPackage(String packageName) {
      return packageName != null && (packageName.startsWith("java.sql") || packageName.startsWith("javax.sql") || packageName.startsWith("com.mysql.cj.jdbc"));
   }

   public static Class<?>[] getImplementedInterfaces(Class<?> clazz) {
      Class<?>[] implementedInterfaces = (Class[])implementedInterfacesCache.get(clazz);
      if (implementedInterfaces != null) {
         return implementedInterfaces;
      } else {
         Set<Class<?>> interfaces = new LinkedHashSet<>();
         Class<?> superClass = clazz;

         do {
            Collections.addAll(interfaces, superClass.getInterfaces());
         } while((superClass = superClass.getSuperclass()) != null);

         implementedInterfaces = interfaces.toArray(new Class[interfaces.size()]);
         Class<?>[] oldValue = (Class[])implementedInterfacesCache.putIfAbsent(clazz, implementedInterfaces);
         if (oldValue != null) {
            implementedInterfaces = oldValue;
         }

         return implementedInterfaces;
      }
   }

   public static long secondsSinceMillis(long timeInMillis) {
      return (System.currentTimeMillis() - timeInMillis) / 1000L;
   }

   public static int truncateAndConvertToInt(long longValue) {
      return longValue > 2147483647L ? Integer.MAX_VALUE : (longValue < -2147483648L ? Integer.MIN_VALUE : (int)longValue);
   }

   public static int[] truncateAndConvertToInt(long[] longArray) {
      int[] intArray = new int[longArray.length];

      for(int i = 0; i < longArray.length; ++i) {
         intArray[i] = longArray[i] > 2147483647L ? Integer.MAX_VALUE : (longArray[i] < -2147483648L ? Integer.MIN_VALUE : (int)longArray[i]);
      }

      return intArray;
   }

   public static String getPackageName(Class<?> clazz) {
      String fqcn = clazz.getName();
      int classNameStartsAt = fqcn.lastIndexOf(46);
      return classNameStartsAt > 0 ? fqcn.substring(0, classNameStartsAt) : "";
   }

   public static boolean isRunningOnWindows() {
      return StringUtils.indexOfIgnoreCase(Constants.OS_NAME, "WINDOWS") != -1;
   }

   public static int readFully(Reader reader, char[] buf, int length) throws IOException {
      int numCharsRead;
      int count;
      for(numCharsRead = 0; numCharsRead < length; numCharsRead += count) {
         count = reader.read(buf, numCharsRead, length - numCharsRead);
         if (count < 0) {
            break;
         }
      }

      return numCharsRead;
   }

   static {
      int startPos = Constants.JVM_VERSION.indexOf(46);
      int endPos = startPos + 1;
      if (startPos != -1) {
         while(Character.isDigit(Constants.JVM_VERSION.charAt(endPos))) {
            if (++endPos >= Constants.JVM_VERSION.length()) {
               break;
            }
         }
      }

      if (endPos > ++startPos) {
         jvmVersion = Integer.parseInt(Constants.JVM_VERSION.substring(startPos, endPos));
      }

      startPos = Constants.JVM_VERSION.indexOf("_");
      endPos = startPos + 1;
      if (startPos != -1) {
         while(Character.isDigit(Constants.JVM_VERSION.charAt(endPos))) {
            if (++endPos >= Constants.JVM_VERSION.length()) {
               break;
            }
         }
      }

      if (endPos > ++startPos) {
         jvmUpdateNumber = Integer.parseInt(Constants.JVM_VERSION.substring(startPos, endPos));
      }

      String loadedFrom = stackTraceToString(new Throwable());
      if (loadedFrom != null) {
         isColdFusion = loadedFrom.indexOf("coldfusion") != -1;
      } else {
         isColdFusion = false;
      }

      isJdbcInterfaceCache = new ConcurrentHashMap<>();
      implementedInterfacesCache = new ConcurrentHashMap<>();
   }
}
