package com.mchange.v1.db.sql;

public final class TypesUtils {
   public static String getNameForSqlTypeCode(int var0) throws UnsupportedTypeException {
      switch(var0) {
         case -7:
            return "BIT";
         case -6:
            return "TINYINT";
         case -5:
            return "BIGINT";
         case -4:
            return "LONGVARBINARY";
         case -3:
            return "VARBINARY";
         case -2:
            return "BINARY";
         case -1:
            return "LONGVARCHAR";
         case 0:
            return "NULL";
         case 1:
            return "CHAR";
         case 2:
            return "NUMERIC";
         case 3:
            return "DECIMAL";
         case 4:
            return "INTEGER";
         case 5:
            return "SMALLINT";
         case 6:
            return "FLOAT";
         case 7:
            return "REAL";
         case 8:
            return "DOUBLE";
         case 12:
            return "VARCHAR";
         case 91:
            return "DATE";
         case 92:
            return "TIME";
         case 93:
            return "TIMESTAMP";
         case 1111:
            throw new UnsupportedTypeException("Type OTHER cannot be represented as a String.");
         case 2000:
            throw new UnsupportedTypeException("Type JAVA_OBJECT cannot be represented as a String.");
         case 2002:
            return "STRUCT";
         case 2003:
            return "ARRAY";
         case 2004:
            return "BLOB";
         case 2005:
            return "CLOB";
         case 2006:
            return "REF";
         default:
            throw new UnsupportedTypeException("Type code: " + var0 + " is unknown.");
      }
   }

   private TypesUtils() {
   }
}
