package com.mysql.cj;

import com.mysql.cj.log.ProfilerEventHandler;
import com.mysql.cj.protocol.Message;
import com.mysql.cj.protocol.ProtocolEntityFactory;
import com.mysql.cj.protocol.Resultset;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public interface Query {
   int getId();

   void setCancelStatus(Query.CancelStatus var1);

   void checkCancelTimeout();

   <T extends Resultset, M extends Message> ProtocolEntityFactory<T, M> getResultSetFactory();

   Session getSession();

   Object getCancelTimeoutMutex();

   void resetCancelledState();

   void closeQuery();

   void addBatch(Object var1);

   List<Object> getBatchedArgs();

   void clearBatchedArgs();

   int getResultFetchSize();

   void setResultFetchSize(int var1);

   Resultset.Type getResultType();

   void setResultType(Resultset.Type var1);

   int getTimeoutInMillis();

   void setTimeoutInMillis(int var1);

   CancelQueryTask startQueryTimer(Query var1, int var2);

   ProfilerEventHandler getEventSink();

   void setEventSink(ProfilerEventHandler var1);

   AtomicBoolean getStatementExecuting();

   String getCurrentCatalog();

   void setCurrentCatalog(String var1);

   boolean isClearWarningsCalled();

   void setClearWarningsCalled(boolean var1);

   void statementBegins();

   void stopQueryTimer(CancelQueryTask var1, boolean var2, boolean var3);

   public static enum CancelStatus {
      NOT_CANCELED,
      CANCELED_BY_USER,
      CANCELED_BY_TIMEOUT;
   }
}
