package com.mysql.cj.jdbc.ha;

import com.mysql.cj.exceptions.CJCommunicationsException;
import com.mysql.cj.jdbc.exceptions.CommunicationsException;
import com.mysql.cj.util.StringUtils;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class StandardLoadBalanceExceptionChecker implements LoadBalanceExceptionChecker {
   private List<String> sqlStateList;
   private List<Class<?>> sqlExClassList;

   @Override
   public boolean shouldExceptionTriggerFailover(Throwable ex) {
      String sqlState = ex instanceof SQLException ? ((SQLException)ex).getSQLState() : null;
      if (sqlState != null) {
         if (sqlState.startsWith("08")) {
            return true;
         }

         if (this.sqlStateList != null) {
            Iterator<String> i = this.sqlStateList.iterator();

            while(i.hasNext()) {
               if (sqlState.startsWith(i.next().toString())) {
                  return true;
               }
            }
         }
      }

      if (!(ex instanceof CommunicationsException) && !(ex instanceof CJCommunicationsException)) {
         if (this.sqlExClassList != null) {
            Iterator<Class<?>> i = this.sqlExClassList.iterator();

            while(i.hasNext()) {
               if (i.next().isInstance(ex)) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return true;
      }
   }

   @Override
   public void destroy() {
   }

   @Override
   public void init(Properties props) {
      this.configureSQLStateList(props.getProperty("loadBalanceSQLStateFailover", null));
      this.configureSQLExceptionSubclassList(props.getProperty("loadBalanceSQLExceptionSubclassFailover", null));
   }

   private void configureSQLStateList(String sqlStates) {
      if (sqlStates != null && !"".equals(sqlStates)) {
         List<String> states = StringUtils.split(sqlStates, ",", true);
         List<String> newStates = new ArrayList<>();

         for(String state : states) {
            if (state.length() > 0) {
               newStates.add(state);
            }
         }

         if (newStates.size() > 0) {
            this.sqlStateList = newStates;
         }
      }
   }

   private void configureSQLExceptionSubclassList(String sqlExClasses) {
      if (sqlExClasses != null && !"".equals(sqlExClasses)) {
         List<String> classes = StringUtils.split(sqlExClasses, ",", true);
         List<Class<?>> newClasses = new ArrayList<>();

         for(String exClass : classes) {
            try {
               Class<?> c = Class.forName(exClass);
               newClasses.add(c);
            } catch (Exception var7) {
            }
         }

         if (newClasses.size() > 0) {
            this.sqlExClassList = newClasses;
         }
      }
   }
}
