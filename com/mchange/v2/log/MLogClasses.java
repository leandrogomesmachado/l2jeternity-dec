package com.mchange.v2.log;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class MLogClasses {
   static final String LOG4J_CNAME = "com.mchange.v2.log.log4j.Log4jMLog";
   static final String SLF4J_CNAME = "com.mchange.v2.log.slf4j.Slf4jMLog";
   static final String JDK14_CNAME = "com.mchange.v2.log.jdk14logging.Jdk14MLog";
   static final String[] SEARCH_CLASSNAMES = new String[]{
      "com.mchange.v2.log.log4j.Log4jMLog", "com.mchange.v2.log.slf4j.Slf4jMLog", "com.mchange.v2.log.jdk14logging.Jdk14MLog"
   };
   static final Map<String, String> ALIASES;

   static String resolveIfAlias(String var0) {
      String var1 = ALIASES.get(var0.toLowerCase());
      if (var1 == null) {
         var1 = var0;
      }

      return var1;
   }

   private MLogClasses() {
   }

   static {
      HashMap var0 = new HashMap();
      var0.put("log4j", "com.mchange.v2.log.log4j.Log4jMLog");
      var0.put("slf4j", "com.mchange.v2.log.slf4j.Slf4jMLog");
      var0.put("jdk14", "com.mchange.v2.log.jdk14logging.Jdk14MLog");
      var0.put("jul", "com.mchange.v2.log.jdk14logging.Jdk14MLog");
      var0.put("java.util.logging", "com.mchange.v2.log.jdk14logging.Jdk14MLog");
      var0.put("fallback", "com.mchange.v2.log.FallbackMLog");
      ALIASES = Collections.unmodifiableMap(var0);
   }
}
