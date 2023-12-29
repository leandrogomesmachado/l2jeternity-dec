package com.mysql.cj.xdevapi;

import com.mysql.cj.Messages;
import com.mysql.cj.MysqlxSession;
import com.mysql.cj.conf.ConnectionUrl;
import com.mysql.cj.conf.DefaultPropertySet;
import com.mysql.cj.conf.HostInfo;
import com.mysql.cj.conf.PropertyDefinitions;
import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.conf.RuntimeProperty;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.protocol.x.XMessage;
import com.mysql.cj.protocol.x.XMessageBuilder;
import com.mysql.cj.protocol.x.XProtocolError;
import com.mysql.cj.result.StringValueFactory;
import com.mysql.cj.util.StringUtils;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SessionImpl implements Session {
   protected MysqlxSession session;
   protected String defaultSchemaName;
   private XMessageBuilder xbuilder;

   public SessionImpl(HostInfo hostInfo) {
      PropertySet pset = new DefaultPropertySet();
      pset.initializeProperties(hostInfo.exposeAsProperties());
      this.session = new MysqlxSession(hostInfo, pset);
      this.defaultSchemaName = hostInfo.getDatabase();
      this.xbuilder = (XMessageBuilder)this.session.<XMessage>getMessageBuilder();
   }

   protected SessionImpl() {
   }

   @Override
   public List<Schema> getSchemas() {
      Function<com.mysql.cj.result.Row, String> rowToName = r -> r.getValue(0, new StringValueFactory());
      Function<com.mysql.cj.result.Row, Schema> rowToSchema = rowToName.andThen(n -> new SchemaImpl(this.session, this, n));
      return this.session
         .query(this.xbuilder.buildSqlStatement("select schema_name from information_schema.schemata"), null, rowToSchema, Collectors.toList());
   }

   @Override
   public Schema getSchema(String schemaName) {
      return new SchemaImpl(this.session, this, schemaName);
   }

   @Override
   public String getDefaultSchemaName() {
      return this.defaultSchemaName;
   }

   @Override
   public Schema getDefaultSchema() {
      if (this.defaultSchemaName == null) {
         throw new WrongArgumentException("Default schema not provided");
      } else {
         return new SchemaImpl(this.session, this, this.defaultSchemaName);
      }
   }

   @Override
   public Schema createSchema(String schemaName) {
      StringBuilder stmtString = new StringBuilder("CREATE DATABASE ");
      stmtString.append(StringUtils.quoteIdentifier(schemaName, true));
      this.session.sendMessage(this.xbuilder.buildSqlStatement(stmtString.toString()));
      return this.getSchema(schemaName);
   }

   @Override
   public Schema createSchema(String schemaName, boolean reuseExistingObject) {
      try {
         return this.createSchema(schemaName);
      } catch (XProtocolError var4) {
         if (var4.getErrorCode() == 1007) {
            return this.getSchema(schemaName);
         } else {
            throw var4;
         }
      }
   }

   @Override
   public void dropSchema(String schemaName) {
      StringBuilder stmtString = new StringBuilder("DROP DATABASE ");
      stmtString.append(StringUtils.quoteIdentifier(schemaName, true));
      this.session.sendMessage(this.xbuilder.buildSqlStatement(stmtString.toString()));
   }

   @Override
   public void startTransaction() {
      this.session.sendMessage(this.xbuilder.buildSqlStatement("START TRANSACTION"));
   }

   @Override
   public void commit() {
      this.session.sendMessage(this.xbuilder.buildSqlStatement("COMMIT"));
   }

   @Override
   public void rollback() {
      this.session.sendMessage(this.xbuilder.buildSqlStatement("ROLLBACK"));
   }

   @Override
   public String setSavepoint() {
      return this.setSavepoint(StringUtils.getUniqueSavepointId());
   }

   @Override
   public String setSavepoint(String name) {
      if (name != null && name.trim().length() != 0) {
         this.session.sendMessage(this.xbuilder.buildSqlStatement("SAVEPOINT " + StringUtils.quoteIdentifier(name, true)));
         return name;
      } else {
         throw new XDevAPIError(Messages.getString("XSession.0", new String[]{"name"}));
      }
   }

   @Override
   public void rollbackTo(String name) {
      if (name != null && name.trim().length() != 0) {
         this.session.sendMessage(this.xbuilder.buildSqlStatement("ROLLBACK TO " + StringUtils.quoteIdentifier(name, true)));
      } else {
         throw new XDevAPIError(Messages.getString("XSession.0", new String[]{"name"}));
      }
   }

   @Override
   public void releaseSavepoint(String name) {
      if (name != null && name.trim().length() != 0) {
         this.session.sendMessage(this.xbuilder.buildSqlStatement("RELEASE SAVEPOINT " + StringUtils.quoteIdentifier(name, true)));
      } else {
         throw new XDevAPIError(Messages.getString("XSession.0", new String[]{"name"}));
      }
   }

   @Override
   public String getUri() {
      PropertySet pset = this.session.getPropertySet();
      StringBuilder sb = new StringBuilder(ConnectionUrl.Type.XDEVAPI_SESSION.getScheme());
      sb.append("//").append(this.session.getProcessHost()).append(":").append(this.session.getPort()).append("/").append(this.defaultSchemaName).append("?");

      for(String propName : PropertyDefinitions.PROPERTY_NAME_TO_PROPERTY_DEFINITION.keySet()) {
         RuntimeProperty<?> propToGet = pset.getProperty(propName);
         String propValue = propToGet.getStringValue();
         if (propValue != null && !propValue.equals(propToGet.getPropertyDefinition().getDefaultValue().toString())) {
            sb.append(",");
            sb.append(propName);
            sb.append("=");
            sb.append(propValue);
         }
      }

      return sb.toString();
   }

   @Override
   public boolean isOpen() {
      return !this.session.isClosed();
   }

   @Override
   public void close() {
      this.session.quit();
   }

   public SqlStatementImpl sql(String sql) {
      return new SqlStatementImpl(this.session, sql);
   }

   public MysqlxSession getSession() {
      return this.session;
   }
}
