package org.apache.commons.dbcp.datasources;

import javax.naming.RefAddr;
import javax.naming.Reference;

public class SharedPoolDataSourceFactory extends InstanceKeyObjectFactory {
   private static final String SHARED_POOL_CLASSNAME = SharedPoolDataSource.class.getName();

   @Override
   protected boolean isCorrectClass(String className) {
      return SHARED_POOL_CLASSNAME.equals(className);
   }

   @Override
   protected InstanceKeyDataSource getNewInstance(Reference ref) {
      SharedPoolDataSource spds = new SharedPoolDataSource();
      RefAddr ra = ref.get("maxActive");
      if (ra != null && ra.getContent() != null) {
         spds.setMaxActive(Integer.parseInt(ra.getContent().toString()));
      }

      ra = ref.get("maxIdle");
      if (ra != null && ra.getContent() != null) {
         spds.setMaxIdle(Integer.parseInt(ra.getContent().toString()));
      }

      ra = ref.get("maxWait");
      if (ra != null && ra.getContent() != null) {
         spds.setMaxWait(Integer.parseInt(ra.getContent().toString()));
      }

      return spds;
   }
}
