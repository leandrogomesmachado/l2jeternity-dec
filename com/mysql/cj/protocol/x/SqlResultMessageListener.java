package com.mysql.cj.protocol.x;

import com.google.protobuf.GeneratedMessage;
import com.mysql.cj.protocol.ColumnDefinition;
import com.mysql.cj.protocol.MessageListener;
import com.mysql.cj.protocol.ProtocolEntityFactory;
import com.mysql.cj.result.Field;
import com.mysql.cj.result.RowList;
import com.mysql.cj.x.protobuf.Mysqlx;
import com.mysql.cj.x.protobuf.MysqlxResultset;
import com.mysql.cj.xdevapi.SqlDataResult;
import com.mysql.cj.xdevapi.SqlResult;
import com.mysql.cj.xdevapi.SqlUpdateResult;
import java.util.TimeZone;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class SqlResultMessageListener implements MessageListener<XMessage> {
   private SqlResultMessageListener.ResultType resultType;
   private CompletableFuture<SqlResult> resultF;
   private StatementExecuteOkMessageListener okListener;
   private ResultMessageListener resultListener;
   private ResultCreatingResultListener<SqlResult> resultCreator;

   public SqlResultMessageListener(
      CompletableFuture<SqlResult> resultF,
      ProtocolEntityFactory<Field, XMessage> colToField,
      ProtocolEntityFactory<Notice, XMessage> noticeFactory,
      TimeZone defaultTimeZone
   ) {
      this.resultF = resultF;
      Function<ColumnDefinition, BiFunction<RowList, Supplier<StatementExecuteOk>, SqlResult>> resultCtor = metadata -> (rows, task) -> new SqlDataResult(
               metadata, defaultTimeZone, rows, task
            );
      this.resultCreator = new ResultCreatingResultListener<>(resultCtor, resultF);
      this.resultListener = new ResultMessageListener(colToField, noticeFactory, this.resultCreator);
      CompletableFuture<StatementExecuteOk> okF = new CompletableFuture<>();
      okF.whenComplete((ok, ex) -> {
         if (ex != null) {
            this.resultF.completeExceptionally(ex);
         } else {
            this.resultF.complete(new SqlUpdateResult(ok));
         }
      });
      this.okListener = new StatementExecuteOkMessageListener(okF, noticeFactory);
   }

   public Boolean createFromMessage(XMessage message) {
      GeneratedMessage msg = (GeneratedMessage)message.getMessage();
      Class<? extends GeneratedMessage> msgClass = msg.getClass();
      if (this.resultType == null) {
         if (MysqlxResultset.ColumnMetaData.class.equals(msgClass)) {
            this.resultType = SqlResultMessageListener.ResultType.DATA;
         } else if (!Mysqlx.Error.class.equals(msgClass)) {
            this.resultType = SqlResultMessageListener.ResultType.UPDATE;
         }
      }

      return this.resultType == SqlResultMessageListener.ResultType.DATA
         ? this.resultListener.createFromMessage(message)
         : this.okListener.createFromMessage(message);
   }

   @Override
   public void error(Throwable ex) {
      this.resultF.completeExceptionally(ex);
   }

   private static enum ResultType {
      UPDATE,
      DATA;
   }
}
