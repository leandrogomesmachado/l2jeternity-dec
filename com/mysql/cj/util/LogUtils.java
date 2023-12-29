package com.mysql.cj.util;

import com.mysql.cj.log.ProfilerEvent;

public class LogUtils {
   public static final String CALLER_INFORMATION_NOT_AVAILABLE = "Caller information not available";
   private static final String LINE_SEPARATOR = System.getProperty("line.separator");
   private static final int LINE_SEPARATOR_LENGTH = LINE_SEPARATOR.length();

   public static Object expandProfilerEventIfNecessary(Object possibleProfilerEvent) {
      if (possibleProfilerEvent instanceof ProfilerEvent) {
         StringBuilder msgBuf = new StringBuilder();
         ProfilerEvent evt = (ProfilerEvent)possibleProfilerEvent;
         String locationInformation = evt.getEventCreationPointAsString();
         if (locationInformation == null) {
            locationInformation = Util.stackTraceToString(new Throwable());
         }

         msgBuf.append("Profiler Event: [");
         switch(evt.getEventType()) {
            case 0:
               msgBuf.append("WARN");
               break;
            case 1:
               msgBuf.append("CONSTRUCT");
               break;
            case 2:
               msgBuf.append("PREPARE");
               break;
            case 3:
               msgBuf.append("QUERY");
               break;
            case 4:
               msgBuf.append("EXECUTE");
               break;
            case 5:
               msgBuf.append("FETCH");
               break;
            case 6:
               msgBuf.append("SLOW QUERY");
               break;
            default:
               msgBuf.append("UNKNOWN");
         }

         msgBuf.append("] ");
         msgBuf.append(locationInformation);
         msgBuf.append(" duration: ");
         msgBuf.append(evt.getEventDuration());
         msgBuf.append(" ");
         msgBuf.append(evt.getDurationUnits());
         msgBuf.append(", connection-id: ");
         msgBuf.append(evt.getConnectionId());
         msgBuf.append(", statement-id: ");
         msgBuf.append(evt.getStatementId());
         msgBuf.append(", resultset-id: ");
         msgBuf.append(evt.getResultSetId());
         String evtMessage = evt.getMessage();
         if (evtMessage != null) {
            msgBuf.append(", message: ");
            msgBuf.append(evtMessage);
         }

         return msgBuf;
      } else {
         return possibleProfilerEvent;
      }
   }

   public static String findCallingClassAndMethod(Throwable t) {
      String stackTraceAsString = Util.stackTraceToString(t);
      String callingClassAndMethod = "Caller information not available";
      int endInternalMethods = Math.max(
         Math.max(stackTraceAsString.lastIndexOf("com.mysql.cj"), stackTraceAsString.lastIndexOf("com.mysql.cj.core")),
         stackTraceAsString.lastIndexOf("com.mysql.cj.jdbc")
      );
      if (endInternalMethods != -1) {
         int endOfLine = stackTraceAsString.indexOf(LINE_SEPARATOR, endInternalMethods);
         if (endOfLine != -1) {
            int nextEndOfLine = stackTraceAsString.indexOf(LINE_SEPARATOR, endOfLine + LINE_SEPARATOR_LENGTH);
            callingClassAndMethod = nextEndOfLine != -1
               ? stackTraceAsString.substring(endOfLine + LINE_SEPARATOR_LENGTH, nextEndOfLine)
               : stackTraceAsString.substring(endOfLine + LINE_SEPARATOR_LENGTH);
         }
      }

      return !callingClassAndMethod.startsWith("\tat ") && !callingClassAndMethod.startsWith("at ") ? "at " + callingClassAndMethod : callingClassAndMethod;
   }
}
