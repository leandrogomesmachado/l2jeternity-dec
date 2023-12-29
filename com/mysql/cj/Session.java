package com.mysql.cj;

import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.log.Log;
import com.mysql.cj.log.ProfilerEventHandler;
import com.mysql.cj.protocol.Message;
import com.mysql.cj.protocol.ServerSession;
import com.mysql.cj.result.Row;
import java.net.SocketAddress;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;

public interface Session {
   PropertySet getPropertySet();

   <M extends Message> MessageBuilder<M> getMessageBuilder();

   void changeUser(String var1, String var2, String var3);

   ExceptionInterceptor getExceptionInterceptor();

   void setExceptionInterceptor(ExceptionInterceptor var1);

   void quit();

   void forceClose();

   boolean versionMeetsMinimum(int var1, int var2, int var3);

   long getThreadId();

   boolean isSetNeededForAutoCommitMode(boolean var1);

   Log getLog();

   ProfilerEventHandler getProfilerEventHandler();

   void setProfilerEventHandler(ProfilerEventHandler var1);

   ServerSession getServerSession();

   boolean isSSLEstablished();

   SocketAddress getRemoteSocketAddress();

   String getProcessHost();

   void addListener(Session.SessionEventListener var1);

   void removeListener(Session.SessionEventListener var1);

   boolean isClosed();

   String getIdentifierQuoteString();

   DataStoreMetadata getDataStoreMetadata();

   <M extends Message, RES_T, R> RES_T query(M var1, Predicate<Row> var2, Function<Row, R> var3, Collector<R, ?, RES_T> var4);

   public interface SessionEventListener {
      void handleNormalClose();

      void handleReconnect();

      void handleCleanup(Throwable var1);
   }
}
