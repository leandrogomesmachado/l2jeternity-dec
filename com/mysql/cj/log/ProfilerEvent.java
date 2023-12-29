package com.mysql.cj.log;

public interface ProfilerEvent {
   byte TYPE_WARN = 0;
   byte TYPE_OBJECT_CREATION = 1;
   byte TYPE_PREPARE = 2;
   byte TYPE_QUERY = 3;
   byte TYPE_EXECUTE = 4;
   byte TYPE_FETCH = 5;
   byte TYPE_SLOW_QUERY = 6;

   byte getEventType();

   void setEventType(byte var1);

   long getEventDuration();

   String getDurationUnits();

   long getConnectionId();

   int getResultSetId();

   int getStatementId();

   String getMessage();

   long getEventCreationTime();

   String getCatalog();

   String getEventCreationPointAsString();

   byte[] pack();
}
