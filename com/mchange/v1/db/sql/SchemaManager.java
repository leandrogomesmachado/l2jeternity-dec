package com.mchange.v1.db.sql;

import com.mchange.util.impl.CommandLineParserImpl;
import com.mchange.v1.util.CleanupUtils;
import java.sql.Connection;
import java.sql.DriverManager;

public class SchemaManager {
   static final String[] VALID = new String[]{"create", "drop"};

   public static void main(String[] var0) {
      Connection var1 = null;

      try {
         CommandLineParserImpl var2 = new CommandLineParserImpl(var0, VALID, null, null);
         boolean var3 = var2.checkSwitch("create");
         if (!var2.checkArgv()) {
            usage();
         }

         if (!(var3 ^ var2.checkSwitch("drop"))) {
            usage();
         }

         String[] var4 = var2.findUnswitchedArgs();
         if (var4.length == 2) {
            var1 = DriverManager.getConnection(var4[0]);
         } else if (var4.length == 4) {
            var1 = DriverManager.getConnection(var4[0], var4[1], var4[2]);
         } else {
            usage();
         }

         var1.setAutoCommit(false);
         Schema var5 = (Schema)Class.forName(var4[var4.length - 1]).newInstance();
         if (var3) {
            var5.createSchema(var1);
            System.out.println("Schema created.");
         } else {
            var5.dropSchema(var1);
            System.out.println("Schema dropped.");
         }
      } catch (Exception var9) {
         var9.printStackTrace();
      } finally {
         CleanupUtils.attemptClose(var1);
      }
   }

   static void usage() {
      System.err
         .println("java -Djdbc.drivers=<driverclass> com.mchange.v1.db.sql.SchemaManager [-create | -drop] <jdbc_url> [<user> <password>] <schemaclass>");
      System.exit(-1);
   }
}
