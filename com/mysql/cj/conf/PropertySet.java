package com.mysql.cj.conf;

import java.util.Properties;

public interface PropertySet {
   void addProperty(RuntimeProperty<?> var1);

   void removeProperty(String var1);

   <T> RuntimeProperty<T> getProperty(String var1);

   RuntimeProperty<Boolean> getBooleanProperty(String var1);

   RuntimeProperty<Integer> getIntegerProperty(String var1);

   RuntimeProperty<Long> getLongProperty(String var1);

   RuntimeProperty<Integer> getMemorySizeProperty(String var1);

   RuntimeProperty<String> getStringProperty(String var1);

   <T extends Enum<T>> RuntimeProperty<T> getEnumProperty(String var1);

   void initializeProperties(Properties var1);

   void postInitialization();

   Properties exposeAsProperties();
}
