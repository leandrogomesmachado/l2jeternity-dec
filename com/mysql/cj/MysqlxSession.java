package com.mysql.cj;

import com.mysql.cj.conf.HostInfo;
import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.exceptions.CJCommunicationsException;
import com.mysql.cj.protocol.ColumnDefinition;
import com.mysql.cj.protocol.ResultListener;
import com.mysql.cj.protocol.ResultStreamer;
import com.mysql.cj.protocol.x.ResultCreatingResultListener;
import com.mysql.cj.protocol.x.StatementExecuteOk;
import com.mysql.cj.protocol.x.StatementExecuteOkBuilder;
import com.mysql.cj.protocol.x.XMessageBuilder;
import com.mysql.cj.protocol.x.XProtocol;
import com.mysql.cj.result.RowList;
import com.mysql.cj.util.StringUtils;
import com.mysql.cj.xdevapi.FilterParams;
import com.mysql.cj.xdevapi.SqlDataResult;
import com.mysql.cj.xdevapi.SqlResult;
import com.mysql.cj.xdevapi.SqlResultImpl;
import com.mysql.cj.xdevapi.SqlUpdateResult;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class MysqlxSession extends CoreSession {
   private String host;
   private int port;

   public MysqlxSession(HostInfo hostInfo, PropertySet propSet) {
      super(hostInfo, propSet);
      this.host = hostInfo.getHost();
      if (this.host == null || StringUtils.isEmptyOrWhitespaceOnly(this.host)) {
         this.host = "localhost";
      }

      this.port = hostInfo.getPort();
      if (this.port < 0) {
         this.port = 33060;
      }

      this.protocol = XProtocol.getInstance(this.host, this.port, propSet);
      this.messageBuilder = this.protocol.getMessageBuilder();
      this.protocol.connect(hostInfo.getUser(), hostInfo.getPassword(), hostInfo.getDatabase());
   }

   @Override
   public String getProcessHost() {
      return this.host;
   }

   public int getPort() {
      return this.port;
   }

   @Override
   public void quit() {
      try {
         this.protocol.send(this.messageBuilder.buildClose(), 0);
         ((XProtocol)this.protocol).readOk();
      } finally {
         try {
            this.protocol.close();
         } catch (IOException var7) {
            throw new CJCommunicationsException(var7);
         }
      }
   }

   public <T extends ResultStreamer> T find(
      FilterParams filterParams, Function<ColumnDefinition, BiFunction<RowList, Supplier<StatementExecuteOk>, T>> resultCtor
   ) {
      this.protocol.send(((XMessageBuilder)this.messageBuilder).buildFind(filterParams), 0);
      ColumnDefinition metadata = this.protocol.readMetadata();
      T res = resultCtor.apply(metadata).apply(((XProtocol)this.protocol).getRowInputStream(metadata), this.protocol::readQueryResult);
      this.protocol.setCurrentResultStreamer(res);
      return res;
   }

   public <RES_T> CompletableFuture<RES_T> asyncFind(
      FilterParams filterParams, Function<ColumnDefinition, BiFunction<RowList, Supplier<StatementExecuteOk>, RES_T>> resultCtor
   ) {
      CompletableFuture<RES_T> f = new CompletableFuture<>();
      ResultListener<StatementExecuteOk> l = new ResultCreatingResultListener<>(resultCtor, f);
      ((XProtocol)this.protocol).asyncFind(filterParams, l, f);
      return f;
   }

   public SqlResult executeSql(String sql, List<Object> args) {
      this.protocol.send(this.messageBuilder.buildSqlStatement(sql, args), 0);
      boolean[] readLastResult = new boolean[1];
      Supplier<StatementExecuteOk> okReader = () -> {
         if (readLastResult[0]) {
            throw new CJCommunicationsException("Invalid state attempting to read ok packet");
         } else if (((XProtocol)this.protocol).hasMoreResults()) {
            return new StatementExecuteOkBuilder().build();
         } else {
            readLastResult[0] = true;
            return this.protocol.readQueryResult();
         }
      };
      Supplier<SqlResult> resultStream = () -> {
         if (readLastResult[0]) {
            return null;
         } else if (((XProtocol)this.protocol).isSqlResultPending()) {
            ColumnDefinition metadata = this.protocol.readMetadata();
            return new SqlDataResult(metadata, this.protocol.getServerSession().getDefaultTimeZone(), this.protocol.getRowInputStream(metadata), okReader);
         } else {
            readLastResult[0] = true;
            return new SqlUpdateResult(this.protocol.readQueryResult());
         }
      };
      SqlResultImpl res = new SqlResultImpl(resultStream);
      this.protocol.setCurrentResultStreamer(res);
      return res;
   }

   public CompletableFuture<SqlResult> asyncExecuteSql(String sql, List<Object> args) {
      return ((XProtocol)this.protocol).asyncExecuteSql(sql, args);
   }

   @Override
   public boolean isClosed() {
      return !((XProtocol)this.protocol).isOpen();
   }
}
