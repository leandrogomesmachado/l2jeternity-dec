package com.mysql.cj.protocol;

public interface Resultset extends ProtocolEntity {
   void setColumnDefinition(ColumnDefinition var1);

   ColumnDefinition getColumnDefinition();

   boolean hasRows();

   ResultsetRows getRows();

   void initRowsWithMetadata();

   int getResultId();

   void setNextResultset(Resultset var1);

   Resultset getNextResultset();

   void clearNextResultset();

   long getUpdateCount();

   long getUpdateID();

   String getServerInfo();

   public static enum Concurrency {
      READ_ONLY(1007),
      UPDATABLE(1008);

      private int value;

      private Concurrency(int jdbcRsConcur) {
         this.value = jdbcRsConcur;
      }

      public int getIntValue() {
         return this.value;
      }

      public static Resultset.Concurrency fromValue(int concurMode, Resultset.Concurrency backupValue) {
         for(Resultset.Concurrency c : values()) {
            if (c.getIntValue() == concurMode) {
               return c;
            }
         }

         return backupValue;
      }
   }

   public static enum Type {
      FORWARD_ONLY(1003),
      SCROLL_INSENSITIVE(1004),
      SCROLL_SENSITIVE(1005);

      private int value;

      private Type(int jdbcRsType) {
         this.value = jdbcRsType;
      }

      public int getIntValue() {
         return this.value;
      }

      public static Resultset.Type fromValue(int rsType, Resultset.Type backupValue) {
         for(Resultset.Type t : values()) {
            if (t.getIntValue() == rsType) {
               return t;
            }
         }

         return backupValue;
      }
   }
}
