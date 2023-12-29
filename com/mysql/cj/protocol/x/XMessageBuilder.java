package com.mysql.cj.protocol.x;

import com.google.protobuf.ByteString;
import com.mysql.cj.MessageBuilder;
import com.mysql.cj.Messages;
import com.mysql.cj.protocol.Security;
import com.mysql.cj.util.StringUtils;
import com.mysql.cj.x.protobuf.MysqlxConnection;
import com.mysql.cj.x.protobuf.MysqlxCrud;
import com.mysql.cj.x.protobuf.MysqlxDatatypes;
import com.mysql.cj.x.protobuf.MysqlxExpr;
import com.mysql.cj.x.protobuf.MysqlxSession;
import com.mysql.cj.x.protobuf.MysqlxSql;
import com.mysql.cj.xdevapi.CreateIndexParams;
import com.mysql.cj.xdevapi.ExprUtil;
import com.mysql.cj.xdevapi.FilterParams;
import com.mysql.cj.xdevapi.InsertParams;
import com.mysql.cj.xdevapi.UpdateParams;
import com.mysql.cj.xdevapi.UpdateSpec;
import java.math.BigInteger;
import java.security.DigestException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.Sasl;
import javax.security.sasl.SaslClient;
import javax.security.sasl.SaslException;

public class XMessageBuilder implements MessageBuilder<XMessage> {
   private static final String XPLUGIN_NAMESPACE = "mysqlx";

   public XMessage buildCapabilitiesGet() {
      return new XMessage(MysqlxConnection.CapabilitiesGet.getDefaultInstance());
   }

   public XMessage buildCapabilitiesSet(String name, Object value) {
      MysqlxDatatypes.Any v = ExprUtil.argObjectToScalarAny(value);
      MysqlxConnection.Capability cap = MysqlxConnection.Capability.newBuilder().setName(name).setValue(v).build();
      MysqlxConnection.Capabilities caps = MysqlxConnection.Capabilities.newBuilder().addCapabilities(cap).build();
      return new XMessage(MysqlxConnection.CapabilitiesSet.newBuilder().setCapabilities(caps).build());
   }

   public XMessage buildDocInsert(String schemaName, String collectionName, List<String> json, boolean upsert) {
      MysqlxCrud.Insert.Builder builder = MysqlxCrud.Insert.newBuilder().setCollection(ExprUtil.buildCollection(schemaName, collectionName));
      if (upsert != builder.getUpsert()) {
         builder.setUpsert(upsert);
      }

      json.stream().map(str -> MysqlxCrud.Insert.TypedRow.newBuilder().addField(ExprUtil.argObjectToExpr(str, false)).build()).forEach(builder::addRow);
      return new XMessage(builder.build());
   }

   public XMessage buildRowInsert(String schemaName, String tableName, InsertParams insertParams) {
      MysqlxCrud.Insert.Builder builder = MysqlxCrud.Insert.newBuilder()
         .setDataModel(MysqlxCrud.DataModel.TABLE)
         .setCollection(ExprUtil.buildCollection(schemaName, tableName));
      if (insertParams.getProjection() != null) {
         builder.addAllProjection((List)insertParams.getProjection());
      }

      builder.addAllRow((List)insertParams.getRows());
      return new XMessage(builder.build());
   }

   public XMessage buildDocUpdate(FilterParams filterParams, List<UpdateSpec> updates) {
      MysqlxCrud.Update.Builder builder = MysqlxCrud.Update.newBuilder().setCollection((MysqlxCrud.Collection)filterParams.getCollection());
      updates.forEach(u -> {
         MysqlxCrud.UpdateOperation.Builder opBuilder = MysqlxCrud.UpdateOperation.newBuilder();
         opBuilder.setOperation((MysqlxCrud.UpdateOperation.UpdateType)u.getUpdateType());
         opBuilder.setSource((MysqlxExpr.ColumnIdentifier)u.getSource());
         if (u.getValue() != null) {
            opBuilder.setValue((MysqlxExpr.Expr)u.getValue());
         }

         builder.addOperation(opBuilder.build());
      });
      applyFilterParams(filterParams, builder::addAllOrder, builder::setLimit, builder::setCriteria, builder::addAllArgs);
      return new XMessage(builder.build());
   }

   public XMessage buildRowUpdate(FilterParams filterParams, UpdateParams updateParams) {
      MysqlxCrud.Update.Builder builder = MysqlxCrud.Update.newBuilder()
         .setDataModel(MysqlxCrud.DataModel.TABLE)
         .setCollection((MysqlxCrud.Collection)filterParams.getCollection());
      ((Map)updateParams.getUpdates())
         .entrySet()
         .stream()
         .map(
            e -> MysqlxCrud.UpdateOperation.newBuilder()
                  .setOperation(MysqlxCrud.UpdateOperation.UpdateType.SET)
                  .setSource((MysqlxExpr.ColumnIdentifier)e.getKey())
                  .setValue((MysqlxExpr.Expr)e.getValue())
                  .build()
         )
         .forEach(builder::addOperation);
      applyFilterParams(filterParams, builder::addAllOrder, builder::setLimit, builder::setCriteria, builder::addAllArgs);
      return new XMessage(builder.build());
   }

   public XMessage buildFind(FilterParams filterParams) {
      MysqlxCrud.Find.Builder builder = MysqlxCrud.Find.newBuilder().setCollection((MysqlxCrud.Collection)filterParams.getCollection());
      builder.setDataModel(filterParams.isRelational() ? MysqlxCrud.DataModel.TABLE : MysqlxCrud.DataModel.DOCUMENT);
      if (filterParams.getFields() != null) {
         builder.addAllProjection((List)filterParams.getFields());
      }

      if (filterParams.getGrouping() != null) {
         builder.addAllGrouping((List)filterParams.getGrouping());
      }

      if (filterParams.getGroupingCriteria() != null) {
         builder.setGroupingCriteria((MysqlxExpr.Expr)filterParams.getGroupingCriteria());
      }

      if (filterParams.getLock() != null) {
         builder.setLocking(MysqlxCrud.Find.RowLock.valueOf(filterParams.getLock().asNumber()));
      }

      if (filterParams.getLockOption() != null) {
         builder.setLockingOptions(MysqlxCrud.Find.RowLockOptions.valueOf(filterParams.getLockOption().asNumber()));
      }

      applyFilterParams(filterParams, builder::addAllOrder, builder::setLimit, builder::setCriteria, builder::addAllArgs);
      return new XMessage(builder.build());
   }

   public XMessage buildDelete(FilterParams filterParams) {
      MysqlxCrud.Delete.Builder builder = MysqlxCrud.Delete.newBuilder().setCollection((MysqlxCrud.Collection)filterParams.getCollection());
      applyFilterParams(filterParams, builder::addAllOrder, builder::setLimit, builder::setCriteria, builder::addAllArgs);
      return new XMessage(builder.build());
   }

   public XMessage buildClose() {
      return new XMessage(MysqlxSession.Close.getDefaultInstance());
   }

   public XMessage buildCreateCollection(String schemaName, String collectionName) {
      if (schemaName == null) {
         throw new XProtocolError(Messages.getString("CreateTableStatement.0", new String[]{"schemaName"}));
      } else if (collectionName == null) {
         throw new XProtocolError(Messages.getString("CreateTableStatement.0", new String[]{"collectionName"}));
      } else {
         return new XMessage(
            this.buildXpluginCommand(
               XpluginStatementCommand.XPLUGIN_STMT_CREATE_COLLECTION,
               MysqlxDatatypes.Any.newBuilder()
                  .setType(MysqlxDatatypes.Any.Type.OBJECT)
                  .setObj(
                     MysqlxDatatypes.Object.newBuilder()
                        .addFld(MysqlxDatatypes.Object.ObjectField.newBuilder().setKey("name").setValue(ExprUtil.buildAny(collectionName)))
                        .addFld(MysqlxDatatypes.Object.ObjectField.newBuilder().setKey("schema").setValue(ExprUtil.buildAny(schemaName)))
                  )
                  .build()
            )
         );
      }
   }

   public XMessage buildDropCollection(String schemaName, String collectionName) {
      if (schemaName == null) {
         throw new XProtocolError(Messages.getString("CreateTableStatement.0", new String[]{"schemaName"}));
      } else if (collectionName == null) {
         throw new XProtocolError(Messages.getString("CreateTableStatement.0", new String[]{"collectionName"}));
      } else {
         return new XMessage(
            this.buildXpluginCommand(
               XpluginStatementCommand.XPLUGIN_STMT_DROP_COLLECTION,
               MysqlxDatatypes.Any.newBuilder()
                  .setType(MysqlxDatatypes.Any.Type.OBJECT)
                  .setObj(
                     MysqlxDatatypes.Object.newBuilder()
                        .addFld(MysqlxDatatypes.Object.ObjectField.newBuilder().setKey("name").setValue(ExprUtil.buildAny(collectionName)))
                        .addFld(MysqlxDatatypes.Object.ObjectField.newBuilder().setKey("schema").setValue(ExprUtil.buildAny(schemaName)))
                  )
                  .build()
            )
         );
      }
   }

   public XMessage buildListObjects(String schemaName, String pattern) {
      if (schemaName == null) {
         throw new XProtocolError(Messages.getString("CreateTableStatement.0", new String[]{"schemaName"}));
      } else {
         MysqlxDatatypes.Object.Builder obj = MysqlxDatatypes.Object.newBuilder()
            .addFld(MysqlxDatatypes.Object.ObjectField.newBuilder().setKey("schema").setValue(ExprUtil.buildAny(schemaName)));
         if (pattern != null) {
            obj.addFld(MysqlxDatatypes.Object.ObjectField.newBuilder().setKey("pattern").setValue(ExprUtil.buildAny(pattern)));
         }

         return new XMessage(
            this.buildXpluginCommand(
               XpluginStatementCommand.XPLUGIN_STMT_LIST_OBJECTS,
               MysqlxDatatypes.Any.newBuilder().setType(MysqlxDatatypes.Any.Type.OBJECT).setObj(obj).build()
            )
         );
      }
   }

   public XMessage buildEnableNotices(String... notices) {
      MysqlxDatatypes.Array.Builder abuilder = MysqlxDatatypes.Array.newBuilder();

      for(String notice : notices) {
         abuilder.addValue(ExprUtil.buildAny(notice));
      }

      return new XMessage(
         this.buildXpluginCommand(
            XpluginStatementCommand.XPLUGIN_STMT_ENABLE_NOTICES,
            MysqlxDatatypes.Any.newBuilder()
               .setType(MysqlxDatatypes.Any.Type.OBJECT)
               .setObj(
                  MysqlxDatatypes.Object.newBuilder()
                     .addFld(
                        MysqlxDatatypes.Object.ObjectField.newBuilder()
                           .setKey("notice")
                           .setValue(MysqlxDatatypes.Any.newBuilder().setType(MysqlxDatatypes.Any.Type.ARRAY).setArray(abuilder))
                     )
               )
               .build()
         )
      );
   }

   public XMessage buildDisableNotices(String... notices) {
      MysqlxDatatypes.Array.Builder abuilder = MysqlxDatatypes.Array.newBuilder();

      for(String notice : notices) {
         abuilder.addValue(ExprUtil.buildAny(notice));
      }

      return new XMessage(
         this.buildXpluginCommand(
            XpluginStatementCommand.XPLUGIN_STMT_DISABLE_NOTICES,
            MysqlxDatatypes.Any.newBuilder()
               .setType(MysqlxDatatypes.Any.Type.OBJECT)
               .setObj(
                  MysqlxDatatypes.Object.newBuilder()
                     .addFld(
                        MysqlxDatatypes.Object.ObjectField.newBuilder()
                           .setKey("notice")
                           .setValue(MysqlxDatatypes.Any.newBuilder().setType(MysqlxDatatypes.Any.Type.ARRAY).setArray(abuilder))
                     )
               )
               .build()
         )
      );
   }

   public XMessage buildListNotices() {
      return new XMessage(this.buildXpluginCommand(XpluginStatementCommand.XPLUGIN_STMT_LIST_NOTICES));
   }

   public XMessage buildCreateCollectionIndex(String schemaName, String collectionName, CreateIndexParams params) {
      MysqlxDatatypes.Object.Builder builder = MysqlxDatatypes.Object.newBuilder();
      builder.addFld(MysqlxDatatypes.Object.ObjectField.newBuilder().setKey("name").setValue(ExprUtil.buildAny(params.getIndexName())))
         .addFld(MysqlxDatatypes.Object.ObjectField.newBuilder().setKey("collection").setValue(ExprUtil.buildAny(collectionName)))
         .addFld(MysqlxDatatypes.Object.ObjectField.newBuilder().setKey("schema").setValue(ExprUtil.buildAny(schemaName)))
         .addFld(MysqlxDatatypes.Object.ObjectField.newBuilder().setKey("unique").setValue(ExprUtil.buildAny(false)));
      if (params.getIndexType() != null) {
         builder.addFld(MysqlxDatatypes.Object.ObjectField.newBuilder().setKey("type").setValue(ExprUtil.buildAny(params.getIndexType())));
      }

      MysqlxDatatypes.Array.Builder abuilder = MysqlxDatatypes.Array.newBuilder();

      for(CreateIndexParams.IndexField indexField : params.getFields()) {
         MysqlxDatatypes.Object.Builder fld = MysqlxDatatypes.Object.newBuilder()
            .addFld(MysqlxDatatypes.Object.ObjectField.newBuilder().setKey("member").setValue(ExprUtil.buildAny(indexField.getField())))
            .addFld(MysqlxDatatypes.Object.ObjectField.newBuilder().setKey("type").setValue(ExprUtil.buildAny(indexField.getType())))
            .addFld(MysqlxDatatypes.Object.ObjectField.newBuilder().setKey("required").setValue(ExprUtil.buildAny(indexField.isRequired())));
         if ("GEOJSON".equalsIgnoreCase(indexField.getType())) {
            if (indexField.getOptions() != null) {
               fld.addFld(
                  MysqlxDatatypes.Object.ObjectField.newBuilder()
                     .setKey("options")
                     .setValue(
                        MysqlxDatatypes.Any.newBuilder()
                           .setType(MysqlxDatatypes.Any.Type.SCALAR)
                           .setScalar(
                              MysqlxDatatypes.Scalar.newBuilder()
                                 .setType(MysqlxDatatypes.Scalar.Type.V_UINT)
                                 .setVUnsignedInt((long)indexField.getOptions().intValue())
                           )
                           .build()
                     )
               );
            }

            if (indexField.getSrid() != null) {
               fld.addFld(
                  MysqlxDatatypes.Object.ObjectField.newBuilder()
                     .setKey("srid")
                     .setValue(
                        MysqlxDatatypes.Any.newBuilder()
                           .setType(MysqlxDatatypes.Any.Type.SCALAR)
                           .setScalar(
                              MysqlxDatatypes.Scalar.newBuilder()
                                 .setType(MysqlxDatatypes.Scalar.Type.V_UINT)
                                 .setVUnsignedInt((long)indexField.getSrid().intValue())
                           )
                           .build()
                     )
               );
            }
         }

         abuilder.addValue(MysqlxDatatypes.Any.newBuilder().setType(MysqlxDatatypes.Any.Type.OBJECT).setObj(fld));
      }

      builder.addFld(
         MysqlxDatatypes.Object.ObjectField.newBuilder()
            .setKey("constraint")
            .setValue(MysqlxDatatypes.Any.newBuilder().setType(MysqlxDatatypes.Any.Type.ARRAY).setArray(abuilder))
      );
      return new XMessage(
         this.buildXpluginCommand(
            XpluginStatementCommand.XPLUGIN_STMT_CREATE_COLLECTION_INDEX,
            MysqlxDatatypes.Any.newBuilder().setType(MysqlxDatatypes.Any.Type.OBJECT).setObj(builder).build()
         )
      );
   }

   public XMessage buildDropCollectionIndex(String schemaName, String collectionName, String indexName) {
      return new XMessage(
         this.buildXpluginCommand(
            XpluginStatementCommand.XPLUGIN_STMT_DROP_COLLECTION_INDEX,
            MysqlxDatatypes.Any.newBuilder()
               .setType(MysqlxDatatypes.Any.Type.OBJECT)
               .setObj(
                  MysqlxDatatypes.Object.newBuilder()
                     .addFld(MysqlxDatatypes.Object.ObjectField.newBuilder().setKey("name").setValue(ExprUtil.buildAny(indexName)))
                     .addFld(MysqlxDatatypes.Object.ObjectField.newBuilder().setKey("collection").setValue(ExprUtil.buildAny(collectionName)))
                     .addFld(MysqlxDatatypes.Object.ObjectField.newBuilder().setKey("schema").setValue(ExprUtil.buildAny(schemaName)))
               )
               .build()
         )
      );
   }

   private MysqlxSql.StmtExecute buildXpluginCommand(XpluginStatementCommand command, MysqlxDatatypes.Any... args) {
      MysqlxSql.StmtExecute.Builder builder = MysqlxSql.StmtExecute.newBuilder();
      builder.setNamespace("mysqlx");
      builder.setStmt(ByteString.copyFromUtf8(command.commandName));
      Arrays.stream(args).forEach(a -> builder.addArgs(a));
      return builder.build();
   }

   public XMessage buildSqlStatement(String statement) {
      return this.buildSqlStatement(statement, null);
   }

   public XMessage buildSqlStatement(String statement, List<Object> args) {
      MysqlxSql.StmtExecute.Builder builder = MysqlxSql.StmtExecute.newBuilder();
      if (args != null) {
         List<MysqlxDatatypes.Any> anyArgs = new ArrayList<>();
         args.stream().map(ExprUtil::argObjectToScalarAny).forEach(a -> anyArgs.add(a));
         builder.addAllArgs(anyArgs);
      }

      builder.setStmt(ByteString.copyFromUtf8(statement));
      return new XMessage(builder.build());
   }

   private static void applyFilterParams(
      FilterParams filterParams,
      Consumer<List<MysqlxCrud.Order>> setOrder,
      Consumer<MysqlxCrud.Limit> setLimit,
      Consumer<MysqlxExpr.Expr> setCriteria,
      Consumer<List<MysqlxDatatypes.Scalar>> setArgs
   ) {
      filterParams.verifyAllArgsBound();
      if (filterParams.getOrder() != null) {
         setOrder.accept((List<MysqlxCrud.Order>)filterParams.getOrder());
      }

      if (filterParams.getLimit() != null) {
         MysqlxCrud.Limit.Builder lb = MysqlxCrud.Limit.newBuilder().setRowCount(filterParams.getLimit());
         if (filterParams.getOffset() != null) {
            lb.setOffset(filterParams.getOffset());
         }

         setLimit.accept(lb.build());
      }

      if (filterParams.getCriteria() != null) {
         setCriteria.accept((MysqlxExpr.Expr)filterParams.getCriteria());
      }

      if (filterParams.getArgs() != null) {
         setArgs.accept((List<MysqlxDatatypes.Scalar>)filterParams.getArgs());
      }
   }

   public XMessage buildSha256MemoryAuthStart() {
      return new XMessage(MysqlxSession.AuthenticateStart.newBuilder().setMechName("SHA256_MEMORY").build());
   }

   public XMessage buildSha256MemoryAuthContinue(String user, String password, byte[] nonce, String database) {
      String encoding = "UTF8";
      byte[] databaseBytes = database == null ? new byte[0] : StringUtils.getBytes(database, encoding);
      byte[] userBytes = user == null ? new byte[0] : StringUtils.getBytes(user, encoding);
      byte[] passwordBytes = password != null && password.length() != 0 ? StringUtils.getBytes(password, encoding) : new byte[0];

      byte[] hashedPassword;
      try {
         hashedPassword = Security.scrambleCachingSha2(passwordBytes, nonce);
      } catch (DigestException var13) {
         throw new RuntimeException(var13);
      }

      hashedPassword = StringUtils.toHexString(hashedPassword, hashedPassword.length).getBytes();
      byte[] reply = new byte[databaseBytes.length + userBytes.length + hashedPassword.length + 2];
      System.arraycopy(databaseBytes, 0, reply, 0, databaseBytes.length);
      int pos = databaseBytes.length;
      reply[pos++] = 0;
      System.arraycopy(userBytes, 0, reply, pos, userBytes.length);
      pos += userBytes.length;
      reply[pos++] = 0;
      System.arraycopy(hashedPassword, 0, reply, pos, hashedPassword.length);
      MysqlxSession.AuthenticateContinue.Builder builder = MysqlxSession.AuthenticateContinue.newBuilder();
      builder.setAuthData(ByteString.copyFrom(reply));
      return new XMessage(builder.build());
   }

   public XMessage buildMysql41AuthStart() {
      return new XMessage(MysqlxSession.AuthenticateStart.newBuilder().setMechName("MYSQL41").build());
   }

   public XMessage buildMysql41AuthContinue(String user, String password, byte[] salt, String database) {
      String encoding = "UTF8";
      byte[] userBytes = user == null ? new byte[0] : StringUtils.getBytes(user, encoding);
      byte[] passwordBytes = password != null && password.length() != 0 ? StringUtils.getBytes(password, encoding) : new byte[0];
      byte[] databaseBytes = database == null ? new byte[0] : StringUtils.getBytes(database, encoding);
      byte[] hashedPassword = passwordBytes;
      if (password != null && password.length() > 0) {
         hashedPassword = Security.scramble411(passwordBytes, salt);
         hashedPassword = String.format("*%040x", new BigInteger(1, hashedPassword)).getBytes();
      }

      byte[] reply = new byte[databaseBytes.length + userBytes.length + hashedPassword.length + 2];
      System.arraycopy(databaseBytes, 0, reply, 0, databaseBytes.length);
      int pos = databaseBytes.length;
      reply[pos++] = 0;
      System.arraycopy(userBytes, 0, reply, pos, userBytes.length);
      pos += userBytes.length;
      reply[pos++] = 0;
      System.arraycopy(hashedPassword, 0, reply, pos, hashedPassword.length);
      MysqlxSession.AuthenticateContinue.Builder builder = MysqlxSession.AuthenticateContinue.newBuilder();
      builder.setAuthData(ByteString.copyFrom(reply));
      return new XMessage(builder.build());
   }

   public XMessage buildPlainAuthStart(final String user, final String password, String database) {
      CallbackHandler callbackHandler = new CallbackHandler() {
         @Override
         public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
            for(Callback c : callbacks) {
               if (NameCallback.class.isAssignableFrom(c.getClass())) {
                  ((NameCallback)c).setName(user);
               } else {
                  if (!PasswordCallback.class.isAssignableFrom(c.getClass())) {
                     throw new UnsupportedCallbackException(c);
                  }

                  ((PasswordCallback)c).setPassword(password.toCharArray());
               }
            }
         }
      };

      try {
         String[] mechanisms = new String[]{"PLAIN"};
         String authorizationId = database != null && database.trim().length() != 0 ? database : null;
         String protocol = "X Protocol";
         Map<String, ?> props = null;
         String serverName = "<unknown>";
         SaslClient saslClient = Sasl.createSaslClient(mechanisms, authorizationId, protocol, serverName, props, callbackHandler);
         MysqlxSession.AuthenticateStart.Builder authStartBuilder = MysqlxSession.AuthenticateStart.newBuilder();
         authStartBuilder.setMechName("PLAIN");
         authStartBuilder.setAuthData(ByteString.copyFrom(saslClient.evaluateChallenge(null)));
         return new XMessage(authStartBuilder.build());
      } catch (SaslException var12) {
         throw new RuntimeException(var12);
      }
   }

   public XMessage buildExternalAuthStart(String database) {
      CallbackHandler callbackHandler = new CallbackHandler() {
         @Override
         public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
            int var3 = callbacks.length;
            byte var4 = 0;
            if (var4 < var3) {
               Callback c = callbacks[var4];
               if (NameCallback.class.isAssignableFrom(c.getClass())) {
                  throw new UnsupportedCallbackException(c);
               } else if (PasswordCallback.class.isAssignableFrom(c.getClass())) {
                  throw new UnsupportedCallbackException(c);
               } else {
                  throw new UnsupportedCallbackException(c);
               }
            }
         }
      };

      try {
         String[] mechanisms = new String[]{"EXTERNAL"};
         String authorizationId = database != null && database.trim().length() != 0 ? database : null;
         String protocol = "X Protocol";
         Map<String, ?> props = null;
         String serverName = "<unknown>";
         SaslClient saslClient = Sasl.createSaslClient(mechanisms, authorizationId, protocol, serverName, props, callbackHandler);
         MysqlxSession.AuthenticateStart.Builder authStartBuilder = MysqlxSession.AuthenticateStart.newBuilder();
         authStartBuilder.setMechName("EXTERNAL");
         authStartBuilder.setAuthData(ByteString.copyFrom(saslClient.evaluateChallenge(null)));
         return new XMessage(authStartBuilder.build());
      } catch (SaslException var10) {
         throw new RuntimeException(var10);
      }
   }
}
