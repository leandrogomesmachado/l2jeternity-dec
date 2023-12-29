package com.mysql.cj.xdevapi;

import com.mysql.cj.Messages;
import com.mysql.cj.MysqlxSession;
import com.mysql.cj.exceptions.AssertionFailedException;
import com.mysql.cj.exceptions.FeatureNotAvailableException;
import com.mysql.cj.protocol.x.StatementExecuteOk;
import com.mysql.cj.protocol.x.XMessage;
import com.mysql.cj.protocol.x.XMessageBuilder;
import com.mysql.cj.protocol.x.XProtocolError;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

public class CollectionImpl implements Collection {
   private MysqlxSession mysqlxSession;
   private XMessageBuilder xbuilder;
   private SchemaImpl schema;
   private String name;

   CollectionImpl(MysqlxSession mysqlxSession, SchemaImpl schema, String name) {
      this.mysqlxSession = mysqlxSession;
      this.schema = schema;
      this.name = name;
      this.xbuilder = (XMessageBuilder)this.mysqlxSession.<XMessage>getMessageBuilder();
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
   public AddStatement add(Map<String, ?> doc) {
      throw new FeatureNotAvailableException("TODO: ");
   }

   @Override
   public AddStatement add(String... jsonString) {
      try {
         DbDoc[] docs = new DbDoc[jsonString.length];

         for(int i = 0; i < jsonString.length; ++i) {
            docs[i] = JsonParser.parseDoc(new StringReader(jsonString[i]));
         }

         return this.add(docs);
      } catch (IOException var4) {
         throw AssertionFailedException.shouldNotHappen(var4);
      }
   }

   @Override
   public AddStatement add(DbDoc doc) {
      return new AddStatementImpl(this.mysqlxSession, this.schema.getName(), this.name, doc);
   }

   @Override
   public AddStatement add(DbDoc... docs) {
      return new AddStatementImpl(this.mysqlxSession, this.schema.getName(), this.name, docs);
   }

   @Override
   public FindStatement find() {
      return this.find(null);
   }

   @Override
   public FindStatement find(String searchCondition) {
      return new FindStatementImpl(this.mysqlxSession, this.schema.getName(), this.name, searchCondition);
   }

   @Override
   public ModifyStatement modify(String searchCondition) {
      return new ModifyStatementImpl(this.mysqlxSession, this.schema.getName(), this.name, searchCondition);
   }

   @Override
   public RemoveStatement remove(String searchCondition) {
      return new RemoveStatementImpl(this.mysqlxSession, this.schema.getName(), this.name, searchCondition);
   }

   @Override
   public Result createIndex(String indexName, DbDoc indexDefinition) {
      StatementExecuteOk ok = this.mysqlxSession
         .sendMessage(this.xbuilder.buildCreateCollectionIndex(this.schema.getName(), this.name, new CreateIndexParams(indexName, indexDefinition)));
      return new UpdateResult(ok);
   }

   @Override
   public Result createIndex(String indexName, String jsonIndexDefinition) {
      StatementExecuteOk ok = this.mysqlxSession
         .sendMessage(this.xbuilder.buildCreateCollectionIndex(this.schema.getName(), this.name, new CreateIndexParams(indexName, jsonIndexDefinition)));
      return new UpdateResult(ok);
   }

   @Override
   public void dropIndex(String indexName) {
      try {
         this.mysqlxSession.sendMessage(this.xbuilder.buildDropCollectionIndex(this.schema.getName(), this.name, indexName));
      } catch (XProtocolError var3) {
         if (var3.getErrorCode() != 1091) {
            throw var3;
         }
      }
   }

   @Override
   public long count() {
      return this.mysqlxSession.getDataStoreMetadata().getTableRowCount(this.schema.getName(), this.name);
   }

   @Override
   public DbDoc newDoc() {
      return new DbDocImpl();
   }

   @Override
   public boolean equals(Object other) {
      return other != null
         && other.getClass() == CollectionImpl.class
         && ((CollectionImpl)other).schema.equals(this.schema)
         && ((CollectionImpl)other).mysqlxSession == this.mysqlxSession
         && this.name.equals(((CollectionImpl)other).name);
   }

   @Override
   public int hashCode() {
      assert false : "hashCode not designed";

      return 0;
   }

   @Override
   public String toString() {
      StringBuilder sb = new StringBuilder("Collection(");
      sb.append(ExprUnparser.quoteIdentifier(this.schema.getName()));
      sb.append(".");
      sb.append(ExprUnparser.quoteIdentifier(this.name));
      sb.append(")");
      return sb.toString();
   }

   @Override
   public Result replaceOne(String id, DbDoc doc) {
      if (id == null) {
         throw new XDevAPIError(Messages.getString("CreateTableStatement.0", new String[]{"id"}));
      } else if (doc == null) {
         throw new XDevAPIError(Messages.getString("CreateTableStatement.0", new String[]{"doc"}));
      } else {
         return this.modify("_id = :id").set("$", doc).bind("id", id).execute();
      }
   }

   @Override
   public Result replaceOne(String id, String jsonString) {
      if (id == null) {
         throw new XDevAPIError(Messages.getString("CreateTableStatement.0", new String[]{"id"}));
      } else if (jsonString == null) {
         throw new XDevAPIError(Messages.getString("CreateTableStatement.0", new String[]{"jsonString"}));
      } else {
         try {
            return this.replaceOne(id, JsonParser.parseDoc(new StringReader(jsonString)));
         } catch (IOException var4) {
            throw AssertionFailedException.shouldNotHappen(var4);
         }
      }
   }

   @Override
   public Result addOrReplaceOne(String id, DbDoc doc) {
      if (id == null) {
         throw new XDevAPIError(Messages.getString("CreateTableStatement.0", new String[]{"id"}));
      } else if (doc == null) {
         throw new XDevAPIError(Messages.getString("CreateTableStatement.0", new String[]{"doc"}));
      } else {
         if (doc.get("_id") == null) {
            doc.add("_id", new JsonString().setValue(id));
         } else if (!id.equals(((JsonString)doc.get("_id")).getString())) {
            throw new XDevAPIError("Document already has an _id that doesn't match to id parameter");
         }

         return this.add(doc).setUpsert(true).execute();
      }
   }

   @Override
   public Result addOrReplaceOne(String id, String jsonString) {
      if (id == null) {
         throw new XDevAPIError(Messages.getString("CreateTableStatement.0", new String[]{"id"}));
      } else if (jsonString == null) {
         throw new XDevAPIError(Messages.getString("CreateTableStatement.0", new String[]{"jsonString"}));
      } else {
         try {
            return this.addOrReplaceOne(id, JsonParser.parseDoc(new StringReader(jsonString)));
         } catch (IOException var4) {
            throw AssertionFailedException.shouldNotHappen(var4);
         }
      }
   }

   @Override
   public DbDoc getOne(String id) {
      return this.find("_id = :id").bind("id", id).execute().fetchOne();
   }

   @Override
   public Result removeOne(String id) {
      return this.remove("_id = :id").bind("id", id).execute();
   }
}
