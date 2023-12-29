package com.mysql.cj.protocol.x;

import com.google.protobuf.GeneratedMessage;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.protocol.ColumnDefinition;
import com.mysql.cj.protocol.MessageListener;
import com.mysql.cj.protocol.ProtocolEntityFactory;
import com.mysql.cj.protocol.ResultListener;
import com.mysql.cj.result.DefaultColumnDefinition;
import com.mysql.cj.result.Field;
import com.mysql.cj.x.protobuf.Mysqlx;
import com.mysql.cj.x.protobuf.MysqlxNotice;
import com.mysql.cj.x.protobuf.MysqlxResultset;
import com.mysql.cj.x.protobuf.MysqlxSql;
import java.util.ArrayList;

public class ResultMessageListener implements MessageListener<XMessage> {
   private ResultListener<StatementExecuteOk> callbacks;
   private ProtocolEntityFactory<Field, XMessage> fieldFactory;
   private ProtocolEntityFactory<Notice, XMessage> noticeFactory;
   private ArrayList<Field> fields = new ArrayList<>();
   private ColumnDefinition metadata = null;
   private boolean metadataSent = false;
   private StatementExecuteOkBuilder okBuilder = new StatementExecuteOkBuilder();

   public ResultMessageListener(
      ProtocolEntityFactory<Field, XMessage> colToField, ProtocolEntityFactory<Notice, XMessage> noticeFactory, ResultListener<StatementExecuteOk> callbacks
   ) {
      this.callbacks = callbacks;
      this.fieldFactory = colToField;
      this.noticeFactory = noticeFactory;
   }

   public Boolean createFromMessage(XMessage message) {
      Class<? extends GeneratedMessage> msgClass = message.getMessage().getClass();
      if (MysqlxResultset.ColumnMetaData.class.equals(msgClass)) {
         Field f = this.fieldFactory.createFromMessage(message);
         this.fields.add(f);
         return false;
      } else {
         if (!this.metadataSent) {
            if (this.metadata == null) {
               this.metadata = new DefaultColumnDefinition(this.fields.toArray(new Field[0]));
            }

            this.callbacks.onMetadata(this.metadata);
            this.metadataSent = true;
         }

         if (MysqlxSql.StmtExecuteOk.class.equals(msgClass)) {
            this.callbacks.onComplete(this.okBuilder.build());
            return true;
         } else if (MysqlxResultset.FetchDone.class.equals(msgClass)) {
            return false;
         } else if (MysqlxResultset.Row.class.equals(msgClass)) {
            if (this.metadata == null) {
               this.metadata = new DefaultColumnDefinition(this.fields.toArray(new Field[0]));
            }

            XProtocolRow row = new XProtocolRow(this.metadata, MysqlxResultset.Row.class.cast(message.getMessage()));
            this.callbacks.onRow(row);
            return false;
         } else if (Mysqlx.Error.class.equals(msgClass)) {
            XProtocolError e = new XProtocolError(Mysqlx.Error.class.cast(message.getMessage()));
            this.callbacks.onException(e);
            return true;
         } else if (MysqlxNotice.Frame.class.equals(msgClass)) {
            this.okBuilder.addNotice(this.noticeFactory.createFromMessage(message));
            return false;
         } else {
            this.callbacks.onException(new WrongArgumentException("Unhandled msg class (" + msgClass + ") + msg=" + message.getMessage()));
            return false;
         }
      }
   }

   @Override
   public void error(Throwable ex) {
      this.callbacks.onException(ex);
   }
}
