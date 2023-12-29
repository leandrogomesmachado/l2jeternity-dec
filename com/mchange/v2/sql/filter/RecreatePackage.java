package com.mchange.v2.sql.filter;

import com.mchange.v1.lang.ClassUtils;
import com.mchange.v2.codegen.intfc.DelegatorGenerator;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.DataSource;

public final class RecreatePackage {
   static final Class[] intfcs = new Class[]{
      Connection.class, ResultSet.class, DatabaseMetaData.class, Statement.class, PreparedStatement.class, CallableStatement.class, DataSource.class
   };

   public static void main(String[] var0) {
      try {
         DelegatorGenerator var1 = new DelegatorGenerator();
         String var2 = RecreatePackage.class.getName();
         String var3 = var2.substring(0, var2.lastIndexOf(46));

         for(int var4 = 0; var4 < intfcs.length; ++var4) {
            Class var5 = intfcs[var4];
            String var6 = ClassUtils.simpleClassName(var5);
            String var7 = "Filter" + var6;
            String var8 = "SynchronizedFilter" + var6;
            BufferedWriter var9 = null;

            try {
               var9 = new BufferedWriter(new FileWriter(var7 + ".java"));
               var1.setMethodModifiers(1);
               var1.writeDelegator(var5, var3 + '.' + var7, var9);
               System.err.println(var7);
            } finally {
               try {
                  if (var9 != null) {
                     var9.close();
                  }
               } catch (Exception var29) {
                  var29.printStackTrace();
               }
            }

            try {
               var9 = new BufferedWriter(new FileWriter(var8 + ".java"));
               var1.setMethodModifiers(33);
               var1.writeDelegator(var5, var3 + '.' + var8, var9);
               System.err.println(var8);
            } finally {
               try {
                  if (var9 != null) {
                     var9.close();
                  }
               } catch (Exception var28) {
                  var28.printStackTrace();
               }
            }
         }
      } catch (Exception var32) {
         var32.printStackTrace();
      }
   }

   private RecreatePackage() {
   }
}
