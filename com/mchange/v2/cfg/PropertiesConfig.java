package com.mchange.v2.cfg;

import java.util.Properties;

public interface PropertiesConfig {
   Properties getPropertiesByPrefix(String var1);

   String getProperty(String var1);
}
