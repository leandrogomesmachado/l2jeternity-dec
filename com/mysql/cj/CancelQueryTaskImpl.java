package com.mysql.cj;

import com.mysql.cj.conf.HostInfo;
import com.mysql.cj.exceptions.OperationCancelledException;
import com.mysql.cj.protocol.a.NativeMessageBuilder;
import com.mysql.cj.util.StringUtils;
import java.util.TimerTask;

public class CancelQueryTaskImpl extends TimerTask implements CancelQueryTask {
   Query queryToCancel;
   Throwable caughtWhileCancelling = null;
   boolean queryTimeoutKillsConnection = false;

   public CancelQueryTaskImpl(Query cancellee) {
      this.queryToCancel = cancellee;
      NativeSession session = (NativeSession)cancellee.getSession();
      this.queryTimeoutKillsConnection = session.getPropertySet().getBooleanProperty("queryTimeoutKillsConnection").getValue();
   }

   @Override
   public boolean cancel() {
      boolean res = super.cancel();
      this.queryToCancel = null;
      return res;
   }

   @Override
   public void run() {
      Thread cancelThread = new Thread() {
         @Override
         public void run() {
            Query localQueryToCancel = CancelQueryTaskImpl.this.queryToCancel;
            if (localQueryToCancel != null) {
               NativeSession session = (NativeSession)localQueryToCancel.getSession();
               if (session != null) {
                  try {
                     if (CancelQueryTaskImpl.this.queryTimeoutKillsConnection) {
                        localQueryToCancel.setCancelStatus(Query.CancelStatus.CANCELED_BY_TIMEOUT);
                        session.invokeCleanupListeners(new OperationCancelledException(Messages.getString("Statement.ConnectionKilledDueToTimeout")));
                     } else {
                        synchronized(localQueryToCancel.getCancelTimeoutMutex()) {
                           long origConnId = session.getThreadId();
                           HostInfo hostInfo = session.getHostInfo();
                           String database = hostInfo.getDatabase();
                           String user = StringUtils.isNullOrEmpty(hostInfo.getUser()) ? "" : hostInfo.getUser();
                           String password = StringUtils.isNullOrEmpty(hostInfo.getPassword()) ? "" : hostInfo.getPassword();
                           NativeSession newSession = new NativeSession(hostInfo, session.getPropertySet());
                           newSession.connect(hostInfo, user, password, database, 30000, new TransactionEventHandler() {
                              @Override
                              public void transactionCompleted() {
                              }

                              @Override
                              public void transactionBegun() {
                              }
                           });
                           newSession.sendCommand(
                              new NativeMessageBuilder().buildComQuery(newSession.getSharedSendPacket(), "KILL QUERY " + origConnId), false, 0
                           );
                           localQueryToCancel.setCancelStatus(Query.CancelStatus.CANCELED_BY_TIMEOUT);
                        }
                     }
                  } catch (Throwable var17) {
                     CancelQueryTaskImpl.this.caughtWhileCancelling = var17;
                  } finally {
                     CancelQueryTaskImpl.this.setQueryToCancel(null);
                  }
               }
            }
         }
      };
      cancelThread.start();
   }

   @Override
   public Throwable getCaughtWhileCancelling() {
      return this.caughtWhileCancelling;
   }

   @Override
   public void setCaughtWhileCancelling(Throwable caughtWhileCancelling) {
      this.caughtWhileCancelling = caughtWhileCancelling;
   }

   @Override
   public Query getQueryToCancel() {
      return this.queryToCancel;
   }

   @Override
   public void setQueryToCancel(Query queryToCancel) {
      this.queryToCancel = queryToCancel;
   }
}
