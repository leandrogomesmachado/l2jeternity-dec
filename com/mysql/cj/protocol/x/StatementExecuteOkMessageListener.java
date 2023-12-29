package com.mysql.cj.protocol.x;

import com.google.protobuf.GeneratedMessage;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.protocol.MessageListener;
import com.mysql.cj.protocol.ProtocolEntityFactory;
import com.mysql.cj.x.protobuf.Mysqlx;
import com.mysql.cj.x.protobuf.MysqlxNotice;
import com.mysql.cj.x.protobuf.MysqlxResultset;
import com.mysql.cj.x.protobuf.MysqlxSql;
import java.util.concurrent.CompletableFuture;

public class StatementExecuteOkMessageListener implements MessageListener<XMessage> {
   private StatementExecuteOkBuilder builder = new StatementExecuteOkBuilder();
   private CompletableFuture<StatementExecuteOk> future = new CompletableFuture<>();
   private ProtocolEntityFactory<Notice, XMessage> noticeFactory;

   public StatementExecuteOkMessageListener(CompletableFuture<StatementExecuteOk> future, ProtocolEntityFactory<Notice, XMessage> noticeFactory) {
      this.future = future;
      this.noticeFactory = noticeFactory;
   }

   public Boolean createFromMessage(XMessage message) {
      Class<? extends GeneratedMessage> msgClass = message.getMessage().getClass();
      if (MysqlxNotice.Frame.class.equals(msgClass)) {
         this.builder.addNotice(this.noticeFactory.createFromMessage(message));
         return false;
      } else if (MysqlxSql.StmtExecuteOk.class.equals(msgClass)) {
         this.future.complete(this.builder.build());
         return true;
      } else if (Mysqlx.Error.class.equals(msgClass)) {
         this.future.completeExceptionally(new XProtocolError(Mysqlx.Error.class.cast(message.getMessage())));
         return true;
      } else if (MysqlxResultset.FetchDone.class.equals(msgClass)) {
         return false;
      } else {
         this.future.completeExceptionally(new WrongArgumentException("Unhandled msg class (" + msgClass + ") + msg=" + message.getMessage()));
         return true;
      }
   }

   @Override
   public void error(Throwable ex) {
      this.future.completeExceptionally(ex);
   }
}
