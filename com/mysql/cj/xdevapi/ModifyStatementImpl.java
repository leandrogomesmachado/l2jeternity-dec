package com.mysql.cj.xdevapi;

import com.mysql.cj.Messages;
import com.mysql.cj.MysqlxSession;
import com.mysql.cj.protocol.x.StatementExecuteOk;
import com.mysql.cj.protocol.x.XMessage;
import com.mysql.cj.protocol.x.XMessageBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ModifyStatementImpl extends FilterableStatement<ModifyStatement, Result> implements ModifyStatement {
   private MysqlxSession mysqlxSession;
   private List<UpdateSpec> updates = new ArrayList<>();

   ModifyStatementImpl(MysqlxSession mysqlxSession, String schema, String collection, String criteria) {
      super(new DocFilterParams(schema, collection));
      if (criteria != null && criteria.trim().length() != 0) {
         this.filterParams.setCriteria(criteria);
         this.mysqlxSession = mysqlxSession;
      } else {
         throw new XDevAPIError(Messages.getString("ModifyStatement.0", new String[]{"criteria"}));
      }
   }

   public Result execute() {
      StatementExecuteOk ok = this.mysqlxSession
         .sendMessage(((XMessageBuilder)this.mysqlxSession.<XMessage>getMessageBuilder()).buildDocUpdate(this.filterParams, this.updates));
      return new UpdateResult(ok);
   }

   @Override
   public CompletableFuture<Result> executeAsync() {
      CompletableFuture<StatementExecuteOk> okF = this.mysqlxSession
         .asyncSendMessage(((XMessageBuilder)this.mysqlxSession.<XMessage>getMessageBuilder()).buildDocUpdate(this.filterParams, this.updates));
      return okF.thenApply(ok -> new UpdateResult(ok));
   }

   @Override
   public ModifyStatement set(String docPath, Object value) {
      this.updates.add(new UpdateSpec(UpdateType.ITEM_SET, docPath).setValue(value));
      return this;
   }

   @Override
   public ModifyStatement change(String docPath, Object value) {
      this.updates.add(new UpdateSpec(UpdateType.ITEM_REPLACE, docPath).setValue(value));
      return this;
   }

   @Override
   public ModifyStatement unset(String... fields) {
      this.updates.addAll(Arrays.stream(fields).map(docPath -> new UpdateSpec(UpdateType.ITEM_REMOVE, docPath)).collect(Collectors.toList()));
      return this;
   }

   @Override
   public ModifyStatement patch(DbDoc document) {
      return this.patch(document.toString());
   }

   @Override
   public ModifyStatement patch(String document) {
      this.updates.add(new UpdateSpec(UpdateType.MERGE_PATCH, "").setValue(Expression.expr(document)));
      return this;
   }

   @Override
   public ModifyStatement arrayInsert(String field, Object value) {
      this.updates.add(new UpdateSpec(UpdateType.ARRAY_INSERT, field).setValue(value));
      return this;
   }

   @Override
   public ModifyStatement arrayAppend(String docPath, Object value) {
      this.updates.add(new UpdateSpec(UpdateType.ARRAY_APPEND, docPath).setValue(value));
      return this;
   }
}
