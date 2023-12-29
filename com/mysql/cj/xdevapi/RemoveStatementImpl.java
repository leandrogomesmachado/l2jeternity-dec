package com.mysql.cj.xdevapi;

import com.mysql.cj.Messages;
import com.mysql.cj.MysqlxSession;
import com.mysql.cj.protocol.x.StatementExecuteOk;
import com.mysql.cj.protocol.x.XMessage;
import com.mysql.cj.protocol.x.XMessageBuilder;
import java.util.concurrent.CompletableFuture;

public class RemoveStatementImpl extends FilterableStatement<RemoveStatement, Result> implements RemoveStatement {
   private MysqlxSession mysqlxSession;

   RemoveStatementImpl(MysqlxSession mysqlxSession, String schema, String collection, String criteria) {
      super(new DocFilterParams(schema, collection));
      if (criteria != null && criteria.trim().length() != 0) {
         this.filterParams.setCriteria(criteria);
         this.mysqlxSession = mysqlxSession;
      } else {
         throw new XDevAPIError(Messages.getString("RemoveStatement.0", new String[]{"criteria"}));
      }
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
