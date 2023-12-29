package com.mysql.cj.xdevapi;

import com.mysql.cj.MysqlxSession;
import com.mysql.cj.protocol.x.StatementExecuteOk;
import com.mysql.cj.protocol.x.XMessage;
import com.mysql.cj.protocol.x.XMessageBuilder;
import java.util.concurrent.CompletableFuture;

public class DeleteStatementImpl extends FilterableStatement<DeleteStatement, Result> implements DeleteStatement {
   private MysqlxSession mysqlxSession;

   DeleteStatementImpl(MysqlxSession mysqlxSession, String schema, String table) {
      super(new TableFilterParams(schema, table));
      this.mysqlxSession = mysqlxSession;
   }

   public Result execute() {
      StatementExecuteOk ok = this.mysqlxSession
         .sendMessage(((XMessageBuilder)this.mysqlxSession.<XMessage>getMessageBuilder()).buildDelete(this.filterParams));
      return new UpdateResult(ok);
   }

   @Override
   public CompletableFuture<Result> executeAsync() {
      CompletableFuture<StatementExecuteOk> okF = this.mysqlxSession
         .asyncSendMessage(((XMessageBuilder)this.mysqlxSession.<XMessage>getMessageBuilder()).buildDelete(this.filterParams));
      return okF.thenApply(ok -> new UpdateResult(ok));
   }
}
