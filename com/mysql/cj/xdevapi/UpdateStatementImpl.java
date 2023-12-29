package com.mysql.cj.xdevapi;

import com.mysql.cj.MysqlxSession;
import com.mysql.cj.protocol.x.StatementExecuteOk;
import com.mysql.cj.protocol.x.XMessage;
import com.mysql.cj.protocol.x.XMessageBuilder;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class UpdateStatementImpl extends FilterableStatement<UpdateStatement, Result> implements UpdateStatement {
   private MysqlxSession mysqlxSession;
   private UpdateParams updateParams = new UpdateParams();

   UpdateStatementImpl(MysqlxSession mysqlxSession, String schema, String table) {
      super(new TableFilterParams(schema, table));
      this.mysqlxSession = mysqlxSession;
   }

   public Result execute() {
      StatementExecuteOk ok = this.mysqlxSession
         .sendMessage(((XMessageBuilder)this.mysqlxSession.<XMessage>getMessageBuilder()).buildRowUpdate(this.filterParams, this.updateParams));
      return new UpdateResult(ok);
   }

   @Override
   public CompletableFuture<Result> executeAsync() {
      CompletableFuture<StatementExecuteOk> okF = this.mysqlxSession
         .asyncSendMessage(((XMessageBuilder)this.mysqlxSession.<XMessage>getMessageBuilder()).buildRowUpdate(this.filterParams, this.updateParams));
      return okF.thenApply(ok -> new UpdateResult(ok));
   }

   @Override
   public UpdateStatement set(Map<String, Object> fieldsAndValues) {
      this.updateParams.setUpdates(fieldsAndValues);
      return this;
   }

   @Override
   public UpdateStatement set(String field, Object value) {
      this.updateParams.addUpdate(field, value);
      return this;
   }
}
