package org.apache.commons.dbcp.datasources;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

abstract class InstanceKeyObjectFactory implements ObjectFactory {
   private static final Map instanceMap = new HashMap();

   static synchronized String registerNewInstance(InstanceKeyDataSource ds) {
      int max = 0;

      for(Object obj : instanceMap.keySet()) {
         if (obj instanceof String) {
            try {
               max = Math.max(max, Integer.valueOf((String)obj));
            } catch (NumberFormatException var5) {
            }
         }
      }

      String instanceKey = String.valueOf(max + 1);
      instanceMap.put(instanceKey, ds);
      return instanceKey;
   }

   static void removeInstance(String key) {
      instanceMap.remove(key);
   }

   public static void closeAll() throws Exception {
      Iterator instanceIterator = instanceMap.entrySet().iterator();

      while(instanceIterator.hasNext()) {
         ((InstanceKeyDataSource)((Entry)instanceIterator.next()).getValue()).close();
      }

      instanceMap.clear();
   }

   @Override
   public Object getObjectInstance(Object refObj, Name name, Context context, Hashtable env) throws IOException, ClassNotFoundException {
      Object obj = null;
      if (refObj instanceof Reference) {
         Reference ref = (Reference)refObj;
         if (this.isCorrectClass(ref.getClassName())) {
            RefAddr ra = ref.get("instanceKey");
            if (ra != null && ra.getContent() != null) {
               obj = instanceMap.get(ra.getContent());
            } else {
               String key = null;
               if (name != null) {
                  key = name.toString();
                  obj = instanceMap.get(key);
               }

               if (obj == null) {
                  InstanceKeyDataSource ds = this.getNewInstance(ref);
                  this.setCommonProperties(ref, ds);
                  obj = ds;
                  if (key != null) {
                     instanceMap.put(key, ds);
                  }
               }
            }
         }
      }

      return obj;
   }

   private void setCommonProperties(Reference ref, InstanceKeyDataSource ikds) throws IOException, ClassNotFoundException {
      RefAddr ra = ref.get("dataSourceName");
      if (ra != null && ra.getContent() != null) {
         ikds.setDataSourceName(ra.getContent().toString());
      }

      ra = ref.get("defaultAutoCommit");
      if (ra != null && ra.getContent() != null) {
         ikds.setDefaultAutoCommit(Boolean.valueOf(ra.getContent().toString()));
      }

      ra = ref.get("defaultReadOnly");
      if (ra != null && ra.getContent() != null) {
         ikds.setDefaultReadOnly(Boolean.valueOf(ra.getContent().toString()));
      }

      ra = ref.get("description");
      if (ra != null && ra.getContent() != null) {
         ikds.setDescription(ra.getContent().toString());
      }

      ra = ref.get("jndiEnvironment");
      if (ra != null && ra.getContent() != null) {
         byte[] serialized = (byte[])ra.getContent();
         ikds.jndiEnvironment = (Properties)deserialize(serialized);
      }

      ra = ref.get("loginTimeout");
      if (ra != null && ra.getContent() != null) {
         ikds.setLoginTimeout(Integer.parseInt(ra.getContent().toString()));
      }

      ra = ref.get("testOnBorrow");
      if (ra != null && ra.getContent() != null) {
         ikds.setTestOnBorrow(Boolean.valueOf(ra.getContent().toString()));
      }

      ra = ref.get("testOnReturn");
      if (ra != null && ra.getContent() != null) {
         ikds.setTestOnReturn(Boolean.valueOf(ra.getContent().toString()));
      }

      ra = ref.get("timeBetweenEvictionRunsMillis");
      if (ra != null && ra.getContent() != null) {
         ikds.setTimeBetweenEvictionRunsMillis(Integer.parseInt(ra.getContent().toString()));
      }

      ra = ref.get("numTestsPerEvictionRun");
      if (ra != null && ra.getContent() != null) {
         ikds.setNumTestsPerEvictionRun(Integer.parseInt(ra.getContent().toString()));
      }

      ra = ref.get("minEvictableIdleTimeMillis");
      if (ra != null && ra.getContent() != null) {
         ikds.setMinEvictableIdleTimeMillis(Integer.parseInt(ra.getContent().toString()));
      }

      ra = ref.get("testWhileIdle");
      if (ra != null && ra.getContent() != null) {
         ikds.setTestWhileIdle(Boolean.valueOf(ra.getContent().toString()));
      }

      ra = ref.get("validationQuery");
      if (ra != null && ra.getContent() != null) {
         ikds.setValidationQuery(ra.getContent().toString());
      }
   }

   protected abstract boolean isCorrectClass(String var1);

   protected abstract InstanceKeyDataSource getNewInstance(Reference var1) throws IOException, ClassNotFoundException;

   protected static final Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
      ObjectInputStream in = null;

      Object var2;
      try {
         in = new ObjectInputStream(new ByteArrayInputStream(data));
         var2 = in.readObject();
      } finally {
         if (in != null) {
            try {
               in.close();
            } catch (IOException var9) {
            }
         }
      }

      return var2;
   }
}
