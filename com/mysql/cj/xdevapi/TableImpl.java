package com.mysql.cj.xdevapi;

import com.mysql.cj.Messages;
import com.mysql.cj.MysqlxSession;
import com.mysql.cj.protocol.x.XMessage;
import com.mysql.cj.protocol.x.XMessageBuilder;
import com.mysql.cj.result.StringValueFactory;
import com.mysql.cj.result.ValueFactory;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TableImpl implements Table {
   private MysqlxSession mysqlxSession;
   private SchemaImpl schema;
   private String name;
   private Boolean isView = null;
   private XMessageBuilder xbuilder;

   TableImpl(MysqlxSession mysqlxSession, SchemaImpl schema, String name) {
      if (mysqlxSession == null) {
         throw new XDevAPIError(Messages.getString("CreateTableStatement.0", new String[]{"mysqlxSession"}));
      } else if (schema == null) {
         throw new XDevAPIError(Messages.getString("CreateTableStatement.0", new String[]{"schema"}));
      } else if (name == null) {
         throw new XDevAPIError(Messages.getString("CreateTableStatement.0", new String[]{"name"}));
      } else {
         this.mysqlxSession = mysqlxSession;
         this.xbuilder = (XMessageBuilder)this.mysqlxSession.<XMessage>getMessageBuilder();
         this.schema = schema;
         this.name = name;
      }
   }

   @Override
   public Session getSession() {
      return this.schema.getSession();
   }

   @Override
   public Schema getSchema() {
      return this.schema;
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Override
   public DatabaseObject.DbObjectStatus existsInDatabase() {
      return this.mysqlxSession.getDataStoreMetadata().tableExists(this.schema.getName(), this.name)
         ? DatabaseObject.DbObjectStatus.EXISTS
         : DatabaseObject.DbObjectStatus.NOT_EXISTS;
   }

   @Override
   public InsertStatement insert() {
      return new InsertStatementImpl(this.mysqlxSession, this.schema.getName(), this.name, new String[0]);
   }

   @Override
   public InsertStatement insert(String... fields) {
      return new InsertStatementImpl(this.mysqlxSession, this.schema.getName(), this.name, fields);
   }

   @Override
   public InsertStatement insert(Map<String, Object> fieldsAndValues) {
      return new InsertStatementImpl(this.mysqlxSession, this.schema.getName(), this.name, fieldsAndValues);
   }

   @Override
   public SelectStatement select(String... projection) {
      return new SelectStatementImpl(this.mysqlxSession, this.schema.getName(), this.name, projection);
   }

   @Override
   public UpdateStatement update() {
      return new UpdateStatementImpl(this.mysqlxSession, this.schema.getName(), this.name);
   }

   @Override
   public DeleteStatement delete() {
      return new DeleteStatementImpl(this.mysqlxSession, this.schema.getName(), this.name);
   }

   @Override
   public long count() {
      return this.mysqlxSession.getDataStoreMetadata().getTableRowCount(this.schema.getName(), this.name);
   }

   @Override
   public boolean equals(Object other) {
      return other != null
         && other.getClass() == TableImpl.class
         && ((TableImpl)other).schema.equals(this.schema)
         && ((TableImpl)other).mysqlxSession == this.mysqlxSession
         && this.name.equals(((TableImpl)other).name);
   }

   @Override
   public int hashCode() {
      assert false : "hashCode not designed";

      return 0;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder("Table(");
      sb.append(ExprUnparser.quoteIdentifier(this.schema.getName()));
      sb.append(".");
      sb.append(ExprUnparser.quoteIdentifier(this.name));
      sb.append(")");
      return sb.toString();
   }

   @Override
   public boolean isView() {
      if (this.isView == null) {
         ValueFactory<String> svf = new StringValueFactory();
         Function<com.mysql.cj.result.Row, DatabaseObjectDescription> rowToDatabaseObjectDescription = r -> new DatabaseObjectDescription(
               r.getValue(0, svf), r.getValue(1, svf)
            );
         List<DatabaseObjectDescription> objects = this.mysqlxSession
            .query(this.xbuilder.buildListObjects(this.schema.getName(), this.name), null, rowToDatabaseObjectDescription, Collectors.toList());
         if (objects.isEmpty()) {
            return false;
         }

         this.isView = objects.get(0).getObjectType() == DatabaseObject.DbObjectType.VIEW
            || objects.get(0).getObjectType() == DatabaseObject.DbObjectType.COLLECTION_VIEW;
      }

      return this.isView;
   }

   public void setView(boolean isView) {
      this.isView = isView;
   }
}
