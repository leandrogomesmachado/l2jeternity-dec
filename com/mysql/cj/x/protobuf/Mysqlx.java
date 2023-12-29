package com.mysql.cj.x.protobuf;

import com.google.protobuf.AbstractParser;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.Parser;
import com.google.protobuf.ProtocolMessageEnum;
import com.google.protobuf.UnknownFieldSet;
import com.google.protobuf.DescriptorProtos.MessageOptions;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner;
import com.google.protobuf.GeneratedMessage.BuilderParent;
import com.google.protobuf.GeneratedMessage.FieldAccessorTable;
import com.google.protobuf.GeneratedMessage.GeneratedExtension;
import com.google.protobuf.Internal.EnumLiteMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;

public final class Mysqlx {
   public static final int CLIENT_MESSAGE_ID_FIELD_NUMBER = 100001;
   public static final GeneratedExtension<MessageOptions, Mysqlx.ClientMessages.Type> clientMessageId = GeneratedMessage.newFileScopedGeneratedExtension(
      Mysqlx.ClientMessages.Type.class, null
   );
   public static final int SERVER_MESSAGE_ID_FIELD_NUMBER = 100002;
   public static final GeneratedExtension<MessageOptions, Mysqlx.ServerMessages.Type> serverMessageId = GeneratedMessage.newFileScopedGeneratedExtension(
      Mysqlx.ServerMessages.Type.class, null
   );
   private static final Descriptor internal_static_Mysqlx_ClientMessages_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(0);
   private static FieldAccessorTable internal_static_Mysqlx_ClientMessages_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_ClientMessages_descriptor, new String[0]
   );
   private static final Descriptor internal_static_Mysqlx_ServerMessages_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(1);
   private static FieldAccessorTable internal_static_Mysqlx_ServerMessages_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_ServerMessages_descriptor, new String[0]
   );
   private static final Descriptor internal_static_Mysqlx_Ok_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(2);
   private static FieldAccessorTable internal_static_Mysqlx_Ok_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Ok_descriptor, new String[]{"Msg"}
   );
   private static final Descriptor internal_static_Mysqlx_Error_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(3);
   private static FieldAccessorTable internal_static_Mysqlx_Error_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Error_descriptor, new String[]{"Severity", "Code", "SqlState", "Msg"}
   );
   private static FileDescriptor descriptor;

   private Mysqlx() {
   }

   public static void registerAllExtensions(ExtensionRegistry registry) {
      registry.add(clientMessageId);
      registry.add(serverMessageId);
   }

   public static FileDescriptor getDescriptor() {
      return descriptor;
   }

   static {
      String[] descriptorData = new String[]{
         "\n\fmysqlx.proto\u0012\u0006Mysqlx\u001a google/protobuf/descriptor.proto\"ô\u0002\n\u000eClientMessages\"á\u0002\n\u0004Type\u0012\u0018\n\u0014CON_CAPABILITIES_GET\u0010\u0001\u0012\u0018\n\u0014CON_CAPABILITIES_SET\u0010\u0002\u0012\r\n\tCON_CLOSE\u0010\u0003\u0012\u001b\n\u0017SESS_AUTHENTICATE_START\u0010\u0004\u0012\u001e\n\u001aSESS_AUTHENTICATE_CONTINUE\u0010\u0005\u0012\u000e\n\nSESS_RESET\u0010\u0006\u0012\u000e\n\nSESS_CLOSE\u0010\u0007\u0012\u0014\n\u0010SQL_STMT_EXECUTE\u0010\f\u0012\r\n\tCRUD_FIND\u0010\u0011\u0012\u000f\n\u000bCRUD_INSERT\u0010\u0012\u0012\u000f\n\u000bCRUD_UPDATE\u0010\u0013\u0012\u000f\n\u000bCRUD_DELETE\u0010\u0014\u0012\u000f\n\u000bEXPECT_OPEN\u0010\u0018\u0012\u0010\n\fEXPECT_CLOSE\u0010\u0019\u0012\u0014\n\u0010CRUD_CREATE_VIEW\u0010\u001e\u0012\u0014\n\u0010CRUD_MO",
         "DIFY_VIEW\u0010\u001f\u0012\u0012\n\u000eCRUD_DROP_VIEW\u0010 \"â\u0002\n\u000eServerMessages\"Ï\u0002\n\u0004Type\u0012\u0006\n\u0002OK\u0010\u0000\u0012\t\n\u0005ERROR\u0010\u0001\u0012\u0015\n\u0011CONN_CAPABILITIES\u0010\u0002\u0012\u001e\n\u001aSESS_AUTHENTICATE_CONTINUE\u0010\u0003\u0012\u0018\n\u0014SESS_AUTHENTICATE_OK\u0010\u0004\u0012\n\n\u0006NOTICE\u0010\u000b\u0012\u001e\n\u001aRESULTSET_COLUMN_META_DATA\u0010\f\u0012\u0011\n\rRESULTSET_ROW\u0010\r\u0012\u0018\n\u0014RESULTSET_FETCH_DONE\u0010\u000e\u0012\u001d\n\u0019RESULTSET_FETCH_SUSPENDED\u0010\u000f\u0012(\n$RESULTSET_FETCH_DONE_MORE_RESULTSETS\u0010\u0010\u0012\u0017\n\u0013SQL_STMT_EXECUTE_OK\u0010\u0011\u0012(\n$RESULTSET_FETCH_DONE_MORE_OUT_PARAMS\u0010\u0012\"\u0017\n\u0002Ok\u0012\u000b\n\u0003ms",
         "g\u0018\u0001 \u0001(\t:\u0004\u0090ê0\u0000\"\u008e\u0001\n\u0005Error\u0012/\n\bseverity\u0018\u0001 \u0001(\u000e2\u0016.Mysqlx.Error.Severity:\u0005ERROR\u0012\f\n\u0004code\u0018\u0002 \u0002(\r\u0012\u0011\n\tsql_state\u0018\u0004 \u0002(\t\u0012\u000b\n\u0003msg\u0018\u0003 \u0002(\t\" \n\bSeverity\u0012\t\n\u0005ERROR\u0010\u0000\u0012\t\n\u0005FATAL\u0010\u0001:\u0004\u0090ê0\u0001:Y\n\u0011client_message_id\u0012\u001f.google.protobuf.MessageOptions\u0018¡\u008d\u0006 \u0001(\u000e2\u001b.Mysqlx.ClientMessages.Type:Y\n\u0011server_message_id\u0012\u001f.google.protobuf.MessageOptions\u0018¢\u008d\u0006 \u0001(\u000e2\u001b.Mysqlx.ServerMessages.TypeB\u0019\n\u0017com.mysql.cj.x.protobuf"
      };
      InternalDescriptorAssigner assigner = new InternalDescriptorAssigner() {
         public ExtensionRegistry assignDescriptors(FileDescriptor root) {
            Mysqlx.descriptor = root;
            return null;
         }
      };
      FileDescriptor.internalBuildGeneratedFileFrom(descriptorData, new FileDescriptor[]{DescriptorProtos.getDescriptor()}, assigner);
      clientMessageId.internalInit((FieldDescriptor)descriptor.getExtensions().get(0));
      serverMessageId.internalInit((FieldDescriptor)descriptor.getExtensions().get(1));
      ExtensionRegistry registry = ExtensionRegistry.newInstance();
      registry.add(serverMessageId);
      registry.add(serverMessageId);
      FileDescriptor.internalUpdateFileDescriptor(descriptor, registry);
      DescriptorProtos.getDescriptor();
   }

   public static final class ClientMessages extends GeneratedMessage implements Mysqlx.ClientMessagesOrBuilder {
      private static final Mysqlx.ClientMessages defaultInstance = new Mysqlx.ClientMessages(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<Mysqlx.ClientMessages> PARSER = new AbstractParser<Mysqlx.ClientMessages>() {
         public Mysqlx.ClientMessages parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new Mysqlx.ClientMessages(input, extensionRegistry);
         }
      };
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private ClientMessages(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private ClientMessages(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static Mysqlx.ClientMessages getDefaultInstance() {
         return defaultInstance;
      }

      public Mysqlx.ClientMessages getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private ClientMessages(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         this.initFields();
         com.google.protobuf.UnknownFieldSet.Builder unknownFields = UnknownFieldSet.newBuilder();

         try {
            boolean done = false;

            while(!done) {
               int tag = input.readTag();
               switch(tag) {
                  case 0:
                     done = true;
                     break;
                  default:
                     if (!this.parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                        done = true;
                     }
               }
            }
         } catch (InvalidProtocolBufferException var10) {
            throw var10.setUnfinishedMessage(this);
         } catch (IOException var11) {
            throw new InvalidProtocolBufferException(var11.getMessage()).setUnfinishedMessage(this);
         } finally {
            this.unknownFields = unknownFields.build();
            this.makeExtensionsImmutable();
         }
      }

      public static final Descriptor getDescriptor() {
         return Mysqlx.internal_static_Mysqlx_ClientMessages_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return Mysqlx.internal_static_Mysqlx_ClientMessages_fieldAccessorTable
            .ensureFieldAccessorsInitialized(Mysqlx.ClientMessages.class, Mysqlx.ClientMessages.Builder.class);
      }

      public Parser<Mysqlx.ClientMessages> getParserForType() {
         return PARSER;
      }

      private void initFields() {
      }

      public final boolean isInitialized() {
         byte isInitialized = this.memoizedIsInitialized;
         if (isInitialized == 1) {
            return true;
         } else if (isInitialized == 0) {
            return false;
         } else {
            this.memoizedIsInitialized = 1;
            return true;
         }
      }

      public void writeTo(CodedOutputStream output) throws IOException {
         this.getSerializedSize();
         this.getUnknownFields().writeTo(output);
      }

      public int getSerializedSize() {
         int size = this.memoizedSerializedSize;
         if (size != -1) {
            return size;
         } else {
            int var2 = 0;
            var2 += this.getUnknownFields().getSerializedSize();
            this.memoizedSerializedSize = var2;
            return var2;
         }
      }

      protected Object writeReplace() throws ObjectStreamException {
         return super.writeReplace();
      }

      public static Mysqlx.ClientMessages parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (Mysqlx.ClientMessages)PARSER.parseFrom(data);
      }

      public static Mysqlx.ClientMessages parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (Mysqlx.ClientMessages)PARSER.parseFrom(data, extensionRegistry);
      }

      public static Mysqlx.ClientMessages parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (Mysqlx.ClientMessages)PARSER.parseFrom(data);
      }

      public static Mysqlx.ClientMessages parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (Mysqlx.ClientMessages)PARSER.parseFrom(data, extensionRegistry);
      }

      public static Mysqlx.ClientMessages parseFrom(InputStream input) throws IOException {
         return (Mysqlx.ClientMessages)PARSER.parseFrom(input);
      }

      public static Mysqlx.ClientMessages parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (Mysqlx.ClientMessages)PARSER.parseFrom(input, extensionRegistry);
      }

      public static Mysqlx.ClientMessages parseDelimitedFrom(InputStream input) throws IOException {
         return (Mysqlx.ClientMessages)PARSER.parseDelimitedFrom(input);
      }

      public static Mysqlx.ClientMessages parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (Mysqlx.ClientMessages)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static Mysqlx.ClientMessages parseFrom(CodedInputStream input) throws IOException {
         return (Mysqlx.ClientMessages)PARSER.parseFrom(input);
      }

      public static Mysqlx.ClientMessages parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (Mysqlx.ClientMessages)PARSER.parseFrom(input, extensionRegistry);
      }

      public static Mysqlx.ClientMessages.Builder newBuilder() {
         return Mysqlx.ClientMessages.Builder.create();
      }

      public Mysqlx.ClientMessages.Builder newBuilderForType() {
         return newBuilder();
      }

      public static Mysqlx.ClientMessages.Builder newBuilder(Mysqlx.ClientMessages prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public Mysqlx.ClientMessages.Builder toBuilder() {
         return newBuilder(this);
      }

      protected Mysqlx.ClientMessages.Builder newBuilderForType(BuilderParent parent) {
         return new Mysqlx.ClientMessages.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<Mysqlx.ClientMessages.Builder>
         implements Mysqlx.ClientMessagesOrBuilder {
         public static final Descriptor getDescriptor() {
            return Mysqlx.internal_static_Mysqlx_ClientMessages_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return Mysqlx.internal_static_Mysqlx_ClientMessages_fieldAccessorTable
               .ensureFieldAccessorsInitialized(Mysqlx.ClientMessages.class, Mysqlx.ClientMessages.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (Mysqlx.ClientMessages.alwaysUseFieldBuilders) {
            }
         }

         private static Mysqlx.ClientMessages.Builder create() {
            return new Mysqlx.ClientMessages.Builder();
         }

         public Mysqlx.ClientMessages.Builder clear() {
            super.clear();
            return this;
         }

         public Mysqlx.ClientMessages.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return Mysqlx.internal_static_Mysqlx_ClientMessages_descriptor;
         }

         public Mysqlx.ClientMessages getDefaultInstanceForType() {
            return Mysqlx.ClientMessages.getDefaultInstance();
         }

         public Mysqlx.ClientMessages build() {
            Mysqlx.ClientMessages result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public Mysqlx.ClientMessages buildPartial() {
            Mysqlx.ClientMessages result = new Mysqlx.ClientMessages(this);
            this.onBuilt();
            return result;
         }

         public Mysqlx.ClientMessages.Builder mergeFrom(Message other) {
            if (other instanceof Mysqlx.ClientMessages) {
               return this.mergeFrom((Mysqlx.ClientMessages)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public Mysqlx.ClientMessages.Builder mergeFrom(Mysqlx.ClientMessages other) {
            if (other == Mysqlx.ClientMessages.getDefaultInstance()) {
               return this;
            } else {
               this.mergeUnknownFields(other.getUnknownFields());
               return this;
            }
         }

         public final boolean isInitialized() {
            return true;
         }

         public Mysqlx.ClientMessages.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            Mysqlx.ClientMessages parsedMessage = null;

            try {
               parsedMessage = (Mysqlx.ClientMessages)Mysqlx.ClientMessages.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (Mysqlx.ClientMessages)var8.getUnfinishedMessage();
               throw var8;
            } finally {
               if (parsedMessage != null) {
                  this.mergeFrom(parsedMessage);
               }
            }

            return this;
         }
      }

      public static enum Type implements ProtocolMessageEnum {
         CON_CAPABILITIES_GET(0, 1),
         CON_CAPABILITIES_SET(1, 2),
         CON_CLOSE(2, 3),
         SESS_AUTHENTICATE_START(3, 4),
         SESS_AUTHENTICATE_CONTINUE(4, 5),
         SESS_RESET(5, 6),
         SESS_CLOSE(6, 7),
         SQL_STMT_EXECUTE(7, 12),
         CRUD_FIND(8, 17),
         CRUD_INSERT(9, 18),
         CRUD_UPDATE(10, 19),
         CRUD_DELETE(11, 20),
         EXPECT_OPEN(12, 24),
         EXPECT_CLOSE(13, 25),
         CRUD_CREATE_VIEW(14, 30),
         CRUD_MODIFY_VIEW(15, 31),
         CRUD_DROP_VIEW(16, 32);

         public static final int CON_CAPABILITIES_GET_VALUE = 1;
         public static final int CON_CAPABILITIES_SET_VALUE = 2;
         public static final int CON_CLOSE_VALUE = 3;
         public static final int SESS_AUTHENTICATE_START_VALUE = 4;
         public static final int SESS_AUTHENTICATE_CONTINUE_VALUE = 5;
         public static final int SESS_RESET_VALUE = 6;
         public static final int SESS_CLOSE_VALUE = 7;
         public static final int SQL_STMT_EXECUTE_VALUE = 12;
         public static final int CRUD_FIND_VALUE = 17;
         public static final int CRUD_INSERT_VALUE = 18;
         public static final int CRUD_UPDATE_VALUE = 19;
         public static final int CRUD_DELETE_VALUE = 20;
         public static final int EXPECT_OPEN_VALUE = 24;
         public static final int EXPECT_CLOSE_VALUE = 25;
         public static final int CRUD_CREATE_VIEW_VALUE = 30;
         public static final int CRUD_MODIFY_VIEW_VALUE = 31;
         public static final int CRUD_DROP_VIEW_VALUE = 32;
         private static EnumLiteMap<Mysqlx.ClientMessages.Type> internalValueMap = new EnumLiteMap<Mysqlx.ClientMessages.Type>() {
            public Mysqlx.ClientMessages.Type findValueByNumber(int number) {
               return Mysqlx.ClientMessages.Type.valueOf(number);
            }
         };
         private static final Mysqlx.ClientMessages.Type[] VALUES = values();
         private final int index;
         private final int value;

         public final int getNumber() {
            return this.value;
         }

         public static Mysqlx.ClientMessages.Type valueOf(int value) {
            switch(value) {
               case 1:
                  return CON_CAPABILITIES_GET;
               case 2:
                  return CON_CAPABILITIES_SET;
               case 3:
                  return CON_CLOSE;
               case 4:
                  return SESS_AUTHENTICATE_START;
               case 5:
                  return SESS_AUTHENTICATE_CONTINUE;
               case 6:
                  return SESS_RESET;
               case 7:
                  return SESS_CLOSE;
               case 8:
               case 9:
               case 10:
               case 11:
               case 13:
               case 14:
               case 15:
               case 16:
               case 21:
               case 22:
               case 23:
               case 26:
               case 27:
               case 28:
               case 29:
               default:
                  return null;
               case 12:
                  return SQL_STMT_EXECUTE;
               case 17:
                  return CRUD_FIND;
               case 18:
                  return CRUD_INSERT;
               case 19:
                  return CRUD_UPDATE;
               case 20:
                  return CRUD_DELETE;
               case 24:
                  return EXPECT_OPEN;
               case 25:
                  return EXPECT_CLOSE;
               case 30:
                  return CRUD_CREATE_VIEW;
               case 31:
                  return CRUD_MODIFY_VIEW;
               case 32:
                  return CRUD_DROP_VIEW;
            }
         }

         public static EnumLiteMap<Mysqlx.ClientMessages.Type> internalGetValueMap() {
            return internalValueMap;
         }

         public final EnumValueDescriptor getValueDescriptor() {
            return (EnumValueDescriptor)getDescriptor().getValues().get(this.index);
         }

         public final EnumDescriptor getDescriptorForType() {
            return getDescriptor();
         }

         public static final EnumDescriptor getDescriptor() {
            return (EnumDescriptor)Mysqlx.ClientMessages.getDescriptor().getEnumTypes().get(0);
         }

         public static Mysqlx.ClientMessages.Type valueOf(EnumValueDescriptor desc) {
            if (desc.getType() != getDescriptor()) {
               throw new IllegalArgumentException("EnumValueDescriptor is not for this type.");
            } else {
               return VALUES[desc.getIndex()];
            }
         }

         private Type(int index, int value) {
            this.index = index;
            this.value = value;
         }
      }
   }

   public interface ClientMessagesOrBuilder extends MessageOrBuilder {
   }

   public static final class Error extends GeneratedMessage implements Mysqlx.ErrorOrBuilder {
      private static final Mysqlx.Error defaultInstance = new Mysqlx.Error(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<Mysqlx.Error> PARSER = new AbstractParser<Mysqlx.Error>() {
         public Mysqlx.Error parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new Mysqlx.Error(input, extensionRegistry);
         }
      };
      private int bitField0_;
      public static final int SEVERITY_FIELD_NUMBER = 1;
      private Mysqlx.Error.Severity severity_;
      public static final int CODE_FIELD_NUMBER = 2;
      private int code_;
      public static final int SQL_STATE_FIELD_NUMBER = 4;
      private Object sqlState_;
      public static final int MSG_FIELD_NUMBER = 3;
      private Object msg_;
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private Error(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private Error(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static Mysqlx.Error getDefaultInstance() {
         return defaultInstance;
      }

      public Mysqlx.Error getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private Error(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         this.initFields();
         int mutable_bitField0_ = 0;
         com.google.protobuf.UnknownFieldSet.Builder unknownFields = UnknownFieldSet.newBuilder();

         try {
            boolean done = false;

            while(!done) {
               int tag = input.readTag();
               switch(tag) {
                  case 0:
                     done = true;
                     break;
                  case 8:
                     int rawValue = input.readEnum();
                     Mysqlx.Error.Severity value = Mysqlx.Error.Severity.valueOf(rawValue);
                     if (value == null) {
                        unknownFields.mergeVarintField(1, rawValue);
                     } else {
                        this.bitField0_ |= 1;
                        this.severity_ = value;
                     }
                     break;
                  case 16:
                     this.bitField0_ |= 2;
                     this.code_ = input.readUInt32();
                     break;
                  case 26: {
                     ByteString bs = input.readBytes();
                     this.bitField0_ |= 8;
                     this.msg_ = bs;
                     break;
                  }
                  case 34: {
                     ByteString bs = input.readBytes();
                     this.bitField0_ |= 4;
                     this.sqlState_ = bs;
                     break;
                  }
                  default:
                     if (!this.parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                        done = true;
                     }
               }
            }
         } catch (InvalidProtocolBufferException var13) {
            throw var13.setUnfinishedMessage(this);
         } catch (IOException var14) {
            throw new InvalidProtocolBufferException(var14.getMessage()).setUnfinishedMessage(this);
         } finally {
            this.unknownFields = unknownFields.build();
            this.makeExtensionsImmutable();
         }
      }

      public static final Descriptor getDescriptor() {
         return Mysqlx.internal_static_Mysqlx_Error_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return Mysqlx.internal_static_Mysqlx_Error_fieldAccessorTable.ensureFieldAccessorsInitialized(Mysqlx.Error.class, Mysqlx.Error.Builder.class);
      }

      public Parser<Mysqlx.Error> getParserForType() {
         return PARSER;
      }

      @Override
      public boolean hasSeverity() {
         return (this.bitField0_ & 1) == 1;
      }

      @Override
      public Mysqlx.Error.Severity getSeverity() {
         return this.severity_;
      }

      @Override
      public boolean hasCode() {
         return (this.bitField0_ & 2) == 2;
      }

      @Override
      public int getCode() {
         return this.code_;
      }

      @Override
      public boolean hasSqlState() {
         return (this.bitField0_ & 4) == 4;
      }

      @Override
      public String getSqlState() {
         Object ref = this.sqlState_;
         if (ref instanceof String) {
            return (String)ref;
         } else {
            ByteString bs = (ByteString)ref;
            String s = bs.toStringUtf8();
            if (bs.isValidUtf8()) {
               this.sqlState_ = s;
            }

            return s;
         }
      }

      @Override
      public ByteString getSqlStateBytes() {
         Object ref = this.sqlState_;
         if (ref instanceof String) {
            ByteString b = ByteString.copyFromUtf8((String)ref);
            this.sqlState_ = b;
            return b;
         } else {
            return (ByteString)ref;
         }
      }

      @Override
      public boolean hasMsg() {
         return (this.bitField0_ & 8) == 8;
      }

      @Override
      public String getMsg() {
         Object ref = this.msg_;
         if (ref instanceof String) {
            return (String)ref;
         } else {
            ByteString bs = (ByteString)ref;
            String s = bs.toStringUtf8();
            if (bs.isValidUtf8()) {
               this.msg_ = s;
            }

            return s;
         }
      }

      @Override
      public ByteString getMsgBytes() {
         Object ref = this.msg_;
         if (ref instanceof String) {
            ByteString b = ByteString.copyFromUtf8((String)ref);
            this.msg_ = b;
            return b;
         } else {
            return (ByteString)ref;
         }
      }

      private void initFields() {
         this.severity_ = Mysqlx.Error.Severity.ERROR;
         this.code_ = 0;
         this.sqlState_ = "";
         this.msg_ = "";
      }

      public final boolean isInitialized() {
         byte isInitialized = this.memoizedIsInitialized;
         if (isInitialized == 1) {
            return true;
         } else if (isInitialized == 0) {
            return false;
         } else if (!this.hasCode()) {
            this.memoizedIsInitialized = 0;
            return false;
         } else if (!this.hasSqlState()) {
            this.memoizedIsInitialized = 0;
            return false;
         } else if (!this.hasMsg()) {
            this.memoizedIsInitialized = 0;
            return false;
         } else {
            this.memoizedIsInitialized = 1;
            return true;
         }
      }

      public void writeTo(CodedOutputStream output) throws IOException {
         this.getSerializedSize();
         if ((this.bitField0_ & 1) == 1) {
            output.writeEnum(1, this.severity_.getNumber());
         }

         if ((this.bitField0_ & 2) == 2) {
            output.writeUInt32(2, this.code_);
         }

         if ((this.bitField0_ & 8) == 8) {
            output.writeBytes(3, this.getMsgBytes());
         }

         if ((this.bitField0_ & 4) == 4) {
            output.writeBytes(4, this.getSqlStateBytes());
         }

         this.getUnknownFields().writeTo(output);
      }

      public int getSerializedSize() {
         int size = this.memoizedSerializedSize;
         if (size != -1) {
            return size;
         } else {
            size = 0;
            if ((this.bitField0_ & 1) == 1) {
               size += CodedOutputStream.computeEnumSize(1, this.severity_.getNumber());
            }

            if ((this.bitField0_ & 2) == 2) {
               size += CodedOutputStream.computeUInt32Size(2, this.code_);
            }

            if ((this.bitField0_ & 8) == 8) {
               size += CodedOutputStream.computeBytesSize(3, this.getMsgBytes());
            }

            if ((this.bitField0_ & 4) == 4) {
               size += CodedOutputStream.computeBytesSize(4, this.getSqlStateBytes());
            }

            size += this.getUnknownFields().getSerializedSize();
            this.memoizedSerializedSize = size;
            return size;
         }
      }

      protected Object writeReplace() throws ObjectStreamException {
         return super.writeReplace();
      }

      public static Mysqlx.Error parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (Mysqlx.Error)PARSER.parseFrom(data);
      }

      public static Mysqlx.Error parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (Mysqlx.Error)PARSER.parseFrom(data, extensionRegistry);
      }

      public static Mysqlx.Error parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (Mysqlx.Error)PARSER.parseFrom(data);
      }

      public static Mysqlx.Error parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (Mysqlx.Error)PARSER.parseFrom(data, extensionRegistry);
      }

      public static Mysqlx.Error parseFrom(InputStream input) throws IOException {
         return (Mysqlx.Error)PARSER.parseFrom(input);
      }

      public static Mysqlx.Error parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (Mysqlx.Error)PARSER.parseFrom(input, extensionRegistry);
      }

      public static Mysqlx.Error parseDelimitedFrom(InputStream input) throws IOException {
         return (Mysqlx.Error)PARSER.parseDelimitedFrom(input);
      }

      public static Mysqlx.Error parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (Mysqlx.Error)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static Mysqlx.Error parseFrom(CodedInputStream input) throws IOException {
         return (Mysqlx.Error)PARSER.parseFrom(input);
      }

      public static Mysqlx.Error parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (Mysqlx.Error)PARSER.parseFrom(input, extensionRegistry);
      }

      public static Mysqlx.Error.Builder newBuilder() {
         return Mysqlx.Error.Builder.create();
      }

      public Mysqlx.Error.Builder newBuilderForType() {
         return newBuilder();
      }

      public static Mysqlx.Error.Builder newBuilder(Mysqlx.Error prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public Mysqlx.Error.Builder toBuilder() {
         return newBuilder(this);
      }

      protected Mysqlx.Error.Builder newBuilderForType(BuilderParent parent) {
         return new Mysqlx.Error.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Mysqlx.Error.Builder> implements Mysqlx.ErrorOrBuilder {
         private int bitField0_;
         private Mysqlx.Error.Severity severity_ = Mysqlx.Error.Severity.ERROR;
         private int code_;
         private Object sqlState_ = "";
         private Object msg_ = "";

         public static final Descriptor getDescriptor() {
            return Mysqlx.internal_static_Mysqlx_Error_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return Mysqlx.internal_static_Mysqlx_Error_fieldAccessorTable.ensureFieldAccessorsInitialized(Mysqlx.Error.class, Mysqlx.Error.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (Mysqlx.Error.alwaysUseFieldBuilders) {
            }
         }

         private static Mysqlx.Error.Builder create() {
            return new Mysqlx.Error.Builder();
         }

         public Mysqlx.Error.Builder clear() {
            super.clear();
            this.severity_ = Mysqlx.Error.Severity.ERROR;
            this.bitField0_ &= -2;
            this.code_ = 0;
            this.bitField0_ &= -3;
            this.sqlState_ = "";
            this.bitField0_ &= -5;
            this.msg_ = "";
            this.bitField0_ &= -9;
            return this;
         }

         public Mysqlx.Error.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return Mysqlx.internal_static_Mysqlx_Error_descriptor;
         }

         public Mysqlx.Error getDefaultInstanceForType() {
            return Mysqlx.Error.getDefaultInstance();
         }

         public Mysqlx.Error build() {
            Mysqlx.Error result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public Mysqlx.Error buildPartial() {
            Mysqlx.Error result = new Mysqlx.Error(this);
            int from_bitField0_ = this.bitField0_;
            int to_bitField0_ = 0;
            if ((from_bitField0_ & 1) == 1) {
               to_bitField0_ |= 1;
            }

            result.severity_ = this.severity_;
            if ((from_bitField0_ & 2) == 2) {
               to_bitField0_ |= 2;
            }

            result.code_ = this.code_;
            if ((from_bitField0_ & 4) == 4) {
               to_bitField0_ |= 4;
            }

            result.sqlState_ = this.sqlState_;
            if ((from_bitField0_ & 8) == 8) {
               to_bitField0_ |= 8;
            }

            result.msg_ = this.msg_;
            result.bitField0_ = to_bitField0_;
            this.onBuilt();
            return result;
         }

         public Mysqlx.Error.Builder mergeFrom(Message other) {
            if (other instanceof Mysqlx.Error) {
               return this.mergeFrom((Mysqlx.Error)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public Mysqlx.Error.Builder mergeFrom(Mysqlx.Error other) {
            if (other == Mysqlx.Error.getDefaultInstance()) {
               return this;
            } else {
               if (other.hasSeverity()) {
                  this.setSeverity(other.getSeverity());
               }

               if (other.hasCode()) {
                  this.setCode(other.getCode());
               }

               if (other.hasSqlState()) {
                  this.bitField0_ |= 4;
                  this.sqlState_ = other.sqlState_;
                  this.onChanged();
               }

               if (other.hasMsg()) {
                  this.bitField0_ |= 8;
                  this.msg_ = other.msg_;
                  this.onChanged();
               }

               this.mergeUnknownFields(other.getUnknownFields());
               return this;
            }
         }

         public final boolean isInitialized() {
            if (!this.hasCode()) {
               return false;
            } else if (!this.hasSqlState()) {
               return false;
            } else {
               return this.hasMsg();
            }
         }

         public Mysqlx.Error.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            Mysqlx.Error parsedMessage = null;

            try {
               parsedMessage = (Mysqlx.Error)Mysqlx.Error.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (Mysqlx.Error)var8.getUnfinishedMessage();
               throw var8;
            } finally {
               if (parsedMessage != null) {
                  this.mergeFrom(parsedMessage);
               }
            }

            return this;
         }

         @Override
         public boolean hasSeverity() {
            return (this.bitField0_ & 1) == 1;
         }

         @Override
         public Mysqlx.Error.Severity getSeverity() {
            return this.severity_;
         }

         public Mysqlx.Error.Builder setSeverity(Mysqlx.Error.Severity value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 1;
               this.severity_ = value;
               this.onChanged();
               return this;
            }
         }

         public Mysqlx.Error.Builder clearSeverity() {
            this.bitField0_ &= -2;
            this.severity_ = Mysqlx.Error.Severity.ERROR;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasCode() {
            return (this.bitField0_ & 2) == 2;
         }

         @Override
         public int getCode() {
            return this.code_;
         }

         public Mysqlx.Error.Builder setCode(int value) {
            this.bitField0_ |= 2;
            this.code_ = value;
            this.onChanged();
            return this;
         }

         public Mysqlx.Error.Builder clearCode() {
            this.bitField0_ &= -3;
            this.code_ = 0;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasSqlState() {
            return (this.bitField0_ & 4) == 4;
         }

         @Override
         public String getSqlState() {
            Object ref = this.sqlState_;
            if (!(ref instanceof String)) {
               ByteString bs = (ByteString)ref;
               String s = bs.toStringUtf8();
               if (bs.isValidUtf8()) {
                  this.sqlState_ = s;
               }

               return s;
            } else {
               return (String)ref;
            }
         }

         @Override
         public ByteString getSqlStateBytes() {
            Object ref = this.sqlState_;
            if (ref instanceof String) {
               ByteString b = ByteString.copyFromUtf8((String)ref);
               this.sqlState_ = b;
               return b;
            } else {
               return (ByteString)ref;
            }
         }

         public Mysqlx.Error.Builder setSqlState(String value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 4;
               this.sqlState_ = value;
               this.onChanged();
               return this;
            }
         }

         public Mysqlx.Error.Builder clearSqlState() {
            this.bitField0_ &= -5;
            this.sqlState_ = Mysqlx.Error.getDefaultInstance().getSqlState();
            this.onChanged();
            return this;
         }

         public Mysqlx.Error.Builder setSqlStateBytes(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 4;
               this.sqlState_ = value;
               this.onChanged();
               return this;
            }
         }

         @Override
         public boolean hasMsg() {
            return (this.bitField0_ & 8) == 8;
         }

         @Override
         public String getMsg() {
            Object ref = this.msg_;
            if (!(ref instanceof String)) {
               ByteString bs = (ByteString)ref;
               String s = bs.toStringUtf8();
               if (bs.isValidUtf8()) {
                  this.msg_ = s;
               }

               return s;
            } else {
               return (String)ref;
            }
         }

         @Override
         public ByteString getMsgBytes() {
            Object ref = this.msg_;
            if (ref instanceof String) {
               ByteString b = ByteString.copyFromUtf8((String)ref);
               this.msg_ = b;
               return b;
            } else {
               return (ByteString)ref;
            }
         }

         public Mysqlx.Error.Builder setMsg(String value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 8;
               this.msg_ = value;
               this.onChanged();
               return this;
            }
         }

         public Mysqlx.Error.Builder clearMsg() {
            this.bitField0_ &= -9;
            this.msg_ = Mysqlx.Error.getDefaultInstance().getMsg();
            this.onChanged();
            return this;
         }

         public Mysqlx.Error.Builder setMsgBytes(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 8;
               this.msg_ = value;
               this.onChanged();
               return this;
            }
         }
      }

      public static enum Severity implements ProtocolMessageEnum {
         ERROR(0, 0),
         FATAL(1, 1);

         public static final int ERROR_VALUE = 0;
         public static final int FATAL_VALUE = 1;
         private static EnumLiteMap<Mysqlx.Error.Severity> internalValueMap = new EnumLiteMap<Mysqlx.Error.Severity>() {
            public Mysqlx.Error.Severity findValueByNumber(int number) {
               return Mysqlx.Error.Severity.valueOf(number);
            }
         };
         private static final Mysqlx.Error.Severity[] VALUES = values();
         private final int index;
         private final int value;

         public final int getNumber() {
            return this.value;
         }

         public static Mysqlx.Error.Severity valueOf(int value) {
            switch(value) {
               case 0:
                  return ERROR;
               case 1:
                  return FATAL;
               default:
                  return null;
            }
         }

         public static EnumLiteMap<Mysqlx.Error.Severity> internalGetValueMap() {
            return internalValueMap;
         }

         public final EnumValueDescriptor getValueDescriptor() {
            return (EnumValueDescriptor)getDescriptor().getValues().get(this.index);
         }

         public final EnumDescriptor getDescriptorForType() {
            return getDescriptor();
         }

         public static final EnumDescriptor getDescriptor() {
            return (EnumDescriptor)Mysqlx.Error.getDescriptor().getEnumTypes().get(0);
         }

         public static Mysqlx.Error.Severity valueOf(EnumValueDescriptor desc) {
            if (desc.getType() != getDescriptor()) {
               throw new IllegalArgumentException("EnumValueDescriptor is not for this type.");
            } else {
               return VALUES[desc.getIndex()];
            }
         }

         private Severity(int index, int value) {
            this.index = index;
            this.value = value;
         }
      }
   }

   public interface ErrorOrBuilder extends MessageOrBuilder {
      boolean hasSeverity();

      Mysqlx.Error.Severity getSeverity();

      boolean hasCode();

      int getCode();

      boolean hasSqlState();

      String getSqlState();

      ByteString getSqlStateBytes();

      boolean hasMsg();

      String getMsg();

      ByteString getMsgBytes();
   }

   public static final class Ok extends GeneratedMessage implements Mysqlx.OkOrBuilder {
      private static final Mysqlx.Ok defaultInstance = new Mysqlx.Ok(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<Mysqlx.Ok> PARSER = new AbstractParser<Mysqlx.Ok>() {
         public Mysqlx.Ok parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new Mysqlx.Ok(input, extensionRegistry);
         }
      };
      private int bitField0_;
      public static final int MSG_FIELD_NUMBER = 1;
      private Object msg_;
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private Ok(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private Ok(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static Mysqlx.Ok getDefaultInstance() {
         return defaultInstance;
      }

      public Mysqlx.Ok getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private Ok(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         this.initFields();
         int mutable_bitField0_ = 0;
         com.google.protobuf.UnknownFieldSet.Builder unknownFields = UnknownFieldSet.newBuilder();

         try {
            boolean done = false;

            while(!done) {
               int tag = input.readTag();
               switch(tag) {
                  case 0:
                     done = true;
                     break;
                  case 10:
                     ByteString bs = input.readBytes();
                     this.bitField0_ |= 1;
                     this.msg_ = bs;
                     break;
                  default:
                     if (!this.parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                        done = true;
                     }
               }
            }
         } catch (InvalidProtocolBufferException var12) {
            throw var12.setUnfinishedMessage(this);
         } catch (IOException var13) {
            throw new InvalidProtocolBufferException(var13.getMessage()).setUnfinishedMessage(this);
         } finally {
            this.unknownFields = unknownFields.build();
            this.makeExtensionsImmutable();
         }
      }

      public static final Descriptor getDescriptor() {
         return Mysqlx.internal_static_Mysqlx_Ok_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return Mysqlx.internal_static_Mysqlx_Ok_fieldAccessorTable.ensureFieldAccessorsInitialized(Mysqlx.Ok.class, Mysqlx.Ok.Builder.class);
      }

      public Parser<Mysqlx.Ok> getParserForType() {
         return PARSER;
      }

      @Override
      public boolean hasMsg() {
         return (this.bitField0_ & 1) == 1;
      }

      @Override
      public String getMsg() {
         Object ref = this.msg_;
         if (ref instanceof String) {
            return (String)ref;
         } else {
            ByteString bs = (ByteString)ref;
            String s = bs.toStringUtf8();
            if (bs.isValidUtf8()) {
               this.msg_ = s;
            }

            return s;
         }
      }

      @Override
      public ByteString getMsgBytes() {
         Object ref = this.msg_;
         if (ref instanceof String) {
            ByteString b = ByteString.copyFromUtf8((String)ref);
            this.msg_ = b;
            return b;
         } else {
            return (ByteString)ref;
         }
      }

      private void initFields() {
         this.msg_ = "";
      }

      public final boolean isInitialized() {
         byte isInitialized = this.memoizedIsInitialized;
         if (isInitialized == 1) {
            return true;
         } else if (isInitialized == 0) {
            return false;
         } else {
            this.memoizedIsInitialized = 1;
            return true;
         }
      }

      public void writeTo(CodedOutputStream output) throws IOException {
         this.getSerializedSize();
         if ((this.bitField0_ & 1) == 1) {
            output.writeBytes(1, this.getMsgBytes());
         }

         this.getUnknownFields().writeTo(output);
      }

      public int getSerializedSize() {
         int size = this.memoizedSerializedSize;
         if (size != -1) {
            return size;
         } else {
            size = 0;
            if ((this.bitField0_ & 1) == 1) {
               size += CodedOutputStream.computeBytesSize(1, this.getMsgBytes());
            }

            size += this.getUnknownFields().getSerializedSize();
            this.memoizedSerializedSize = size;
            return size;
         }
      }

      protected Object writeReplace() throws ObjectStreamException {
         return super.writeReplace();
      }

      public static Mysqlx.Ok parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (Mysqlx.Ok)PARSER.parseFrom(data);
      }

      public static Mysqlx.Ok parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (Mysqlx.Ok)PARSER.parseFrom(data, extensionRegistry);
      }

      public static Mysqlx.Ok parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (Mysqlx.Ok)PARSER.parseFrom(data);
      }

      public static Mysqlx.Ok parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (Mysqlx.Ok)PARSER.parseFrom(data, extensionRegistry);
      }

      public static Mysqlx.Ok parseFrom(InputStream input) throws IOException {
         return (Mysqlx.Ok)PARSER.parseFrom(input);
      }

      public static Mysqlx.Ok parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (Mysqlx.Ok)PARSER.parseFrom(input, extensionRegistry);
      }

      public static Mysqlx.Ok parseDelimitedFrom(InputStream input) throws IOException {
         return (Mysqlx.Ok)PARSER.parseDelimitedFrom(input);
      }

      public static Mysqlx.Ok parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (Mysqlx.Ok)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static Mysqlx.Ok parseFrom(CodedInputStream input) throws IOException {
         return (Mysqlx.Ok)PARSER.parseFrom(input);
      }

      public static Mysqlx.Ok parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (Mysqlx.Ok)PARSER.parseFrom(input, extensionRegistry);
      }

      public static Mysqlx.Ok.Builder newBuilder() {
         return Mysqlx.Ok.Builder.create();
      }

      public Mysqlx.Ok.Builder newBuilderForType() {
         return newBuilder();
      }

      public static Mysqlx.Ok.Builder newBuilder(Mysqlx.Ok prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public Mysqlx.Ok.Builder toBuilder() {
         return newBuilder(this);
      }

      protected Mysqlx.Ok.Builder newBuilderForType(BuilderParent parent) {
         return new Mysqlx.Ok.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<Mysqlx.Ok.Builder> implements Mysqlx.OkOrBuilder {
         private int bitField0_;
         private Object msg_ = "";

         public static final Descriptor getDescriptor() {
            return Mysqlx.internal_static_Mysqlx_Ok_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return Mysqlx.internal_static_Mysqlx_Ok_fieldAccessorTable.ensureFieldAccessorsInitialized(Mysqlx.Ok.class, Mysqlx.Ok.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (Mysqlx.Ok.alwaysUseFieldBuilders) {
            }
         }

         private static Mysqlx.Ok.Builder create() {
            return new Mysqlx.Ok.Builder();
         }

         public Mysqlx.Ok.Builder clear() {
            super.clear();
            this.msg_ = "";
            this.bitField0_ &= -2;
            return this;
         }

         public Mysqlx.Ok.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return Mysqlx.internal_static_Mysqlx_Ok_descriptor;
         }

         public Mysqlx.Ok getDefaultInstanceForType() {
            return Mysqlx.Ok.getDefaultInstance();
         }

         public Mysqlx.Ok build() {
            Mysqlx.Ok result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public Mysqlx.Ok buildPartial() {
            Mysqlx.Ok result = new Mysqlx.Ok(this);
            int from_bitField0_ = this.bitField0_;
            int to_bitField0_ = 0;
            if ((from_bitField0_ & 1) == 1) {
               to_bitField0_ |= 1;
            }

            result.msg_ = this.msg_;
            result.bitField0_ = to_bitField0_;
            this.onBuilt();
            return result;
         }

         public Mysqlx.Ok.Builder mergeFrom(Message other) {
            if (other instanceof Mysqlx.Ok) {
               return this.mergeFrom((Mysqlx.Ok)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public Mysqlx.Ok.Builder mergeFrom(Mysqlx.Ok other) {
            if (other == Mysqlx.Ok.getDefaultInstance()) {
               return this;
            } else {
               if (other.hasMsg()) {
                  this.bitField0_ |= 1;
                  this.msg_ = other.msg_;
                  this.onChanged();
               }

               this.mergeUnknownFields(other.getUnknownFields());
               return this;
            }
         }

         public final boolean isInitialized() {
            return true;
         }

         public Mysqlx.Ok.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            Mysqlx.Ok parsedMessage = null;

            try {
               parsedMessage = (Mysqlx.Ok)Mysqlx.Ok.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (Mysqlx.Ok)var8.getUnfinishedMessage();
               throw var8;
            } finally {
               if (parsedMessage != null) {
                  this.mergeFrom(parsedMessage);
               }
            }

            return this;
         }

         @Override
         public boolean hasMsg() {
            return (this.bitField0_ & 1) == 1;
         }

         @Override
         public String getMsg() {
            Object ref = this.msg_;
            if (!(ref instanceof String)) {
               ByteString bs = (ByteString)ref;
               String s = bs.toStringUtf8();
               if (bs.isValidUtf8()) {
                  this.msg_ = s;
               }

               return s;
            } else {
               return (String)ref;
            }
         }

         @Override
         public ByteString getMsgBytes() {
            Object ref = this.msg_;
            if (ref instanceof String) {
               ByteString b = ByteString.copyFromUtf8((String)ref);
               this.msg_ = b;
               return b;
            } else {
               return (ByteString)ref;
            }
         }

         public Mysqlx.Ok.Builder setMsg(String value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 1;
               this.msg_ = value;
               this.onChanged();
               return this;
            }
         }

         public Mysqlx.Ok.Builder clearMsg() {
            this.bitField0_ &= -2;
            this.msg_ = Mysqlx.Ok.getDefaultInstance().getMsg();
            this.onChanged();
            return this;
         }

         public Mysqlx.Ok.Builder setMsgBytes(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 1;
               this.msg_ = value;
               this.onChanged();
               return this;
            }
         }
      }
   }

   public interface OkOrBuilder extends MessageOrBuilder {
      boolean hasMsg();

      String getMsg();

      ByteString getMsgBytes();
   }

   public static final class ServerMessages extends GeneratedMessage implements Mysqlx.ServerMessagesOrBuilder {
      private static final Mysqlx.ServerMessages defaultInstance = new Mysqlx.ServerMessages(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<Mysqlx.ServerMessages> PARSER = new AbstractParser<Mysqlx.ServerMessages>() {
         public Mysqlx.ServerMessages parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new Mysqlx.ServerMessages(input, extensionRegistry);
         }
      };
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private ServerMessages(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private ServerMessages(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static Mysqlx.ServerMessages getDefaultInstance() {
         return defaultInstance;
      }

      public Mysqlx.ServerMessages getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private ServerMessages(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         this.initFields();
         com.google.protobuf.UnknownFieldSet.Builder unknownFields = UnknownFieldSet.newBuilder();

         try {
            boolean done = false;

            while(!done) {
               int tag = input.readTag();
               switch(tag) {
                  case 0:
                     done = true;
                     break;
                  default:
                     if (!this.parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                        done = true;
                     }
               }
            }
         } catch (InvalidProtocolBufferException var10) {
            throw var10.setUnfinishedMessage(this);
         } catch (IOException var11) {
            throw new InvalidProtocolBufferException(var11.getMessage()).setUnfinishedMessage(this);
         } finally {
            this.unknownFields = unknownFields.build();
            this.makeExtensionsImmutable();
         }
      }

      public static final Descriptor getDescriptor() {
         return Mysqlx.internal_static_Mysqlx_ServerMessages_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return Mysqlx.internal_static_Mysqlx_ServerMessages_fieldAccessorTable
            .ensureFieldAccessorsInitialized(Mysqlx.ServerMessages.class, Mysqlx.ServerMessages.Builder.class);
      }

      public Parser<Mysqlx.ServerMessages> getParserForType() {
         return PARSER;
      }

      private void initFields() {
      }

      public final boolean isInitialized() {
         byte isInitialized = this.memoizedIsInitialized;
         if (isInitialized == 1) {
            return true;
         } else if (isInitialized == 0) {
            return false;
         } else {
            this.memoizedIsInitialized = 1;
            return true;
         }
      }

      public void writeTo(CodedOutputStream output) throws IOException {
         this.getSerializedSize();
         this.getUnknownFields().writeTo(output);
      }

      public int getSerializedSize() {
         int size = this.memoizedSerializedSize;
         if (size != -1) {
            return size;
         } else {
            int var2 = 0;
            var2 += this.getUnknownFields().getSerializedSize();
            this.memoizedSerializedSize = var2;
            return var2;
         }
      }

      protected Object writeReplace() throws ObjectStreamException {
         return super.writeReplace();
      }

      public static Mysqlx.ServerMessages parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (Mysqlx.ServerMessages)PARSER.parseFrom(data);
      }

      public static Mysqlx.ServerMessages parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (Mysqlx.ServerMessages)PARSER.parseFrom(data, extensionRegistry);
      }

      public static Mysqlx.ServerMessages parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (Mysqlx.ServerMessages)PARSER.parseFrom(data);
      }

      public static Mysqlx.ServerMessages parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (Mysqlx.ServerMessages)PARSER.parseFrom(data, extensionRegistry);
      }

      public static Mysqlx.ServerMessages parseFrom(InputStream input) throws IOException {
         return (Mysqlx.ServerMessages)PARSER.parseFrom(input);
      }

      public static Mysqlx.ServerMessages parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (Mysqlx.ServerMessages)PARSER.parseFrom(input, extensionRegistry);
      }

      public static Mysqlx.ServerMessages parseDelimitedFrom(InputStream input) throws IOException {
         return (Mysqlx.ServerMessages)PARSER.parseDelimitedFrom(input);
      }

      public static Mysqlx.ServerMessages parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (Mysqlx.ServerMessages)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static Mysqlx.ServerMessages parseFrom(CodedInputStream input) throws IOException {
         return (Mysqlx.ServerMessages)PARSER.parseFrom(input);
      }

      public static Mysqlx.ServerMessages parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (Mysqlx.ServerMessages)PARSER.parseFrom(input, extensionRegistry);
      }

      public static Mysqlx.ServerMessages.Builder newBuilder() {
         return Mysqlx.ServerMessages.Builder.create();
      }

      public Mysqlx.ServerMessages.Builder newBuilderForType() {
         return newBuilder();
      }

      public static Mysqlx.ServerMessages.Builder newBuilder(Mysqlx.ServerMessages prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public Mysqlx.ServerMessages.Builder toBuilder() {
         return newBuilder(this);
      }

      protected Mysqlx.ServerMessages.Builder newBuilderForType(BuilderParent parent) {
         return new Mysqlx.ServerMessages.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<Mysqlx.ServerMessages.Builder>
         implements Mysqlx.ServerMessagesOrBuilder {
         public static final Descriptor getDescriptor() {
            return Mysqlx.internal_static_Mysqlx_ServerMessages_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return Mysqlx.internal_static_Mysqlx_ServerMessages_fieldAccessorTable
               .ensureFieldAccessorsInitialized(Mysqlx.ServerMessages.class, Mysqlx.ServerMessages.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (Mysqlx.ServerMessages.alwaysUseFieldBuilders) {
            }
         }

         private static Mysqlx.ServerMessages.Builder create() {
            return new Mysqlx.ServerMessages.Builder();
         }

         public Mysqlx.ServerMessages.Builder clear() {
            super.clear();
            return this;
         }

         public Mysqlx.ServerMessages.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return Mysqlx.internal_static_Mysqlx_ServerMessages_descriptor;
         }

         public Mysqlx.ServerMessages getDefaultInstanceForType() {
            return Mysqlx.ServerMessages.getDefaultInstance();
         }

         public Mysqlx.ServerMessages build() {
            Mysqlx.ServerMessages result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public Mysqlx.ServerMessages buildPartial() {
            Mysqlx.ServerMessages result = new Mysqlx.ServerMessages(this);
            this.onBuilt();
            return result;
         }

         public Mysqlx.ServerMessages.Builder mergeFrom(Message other) {
            if (other instanceof Mysqlx.ServerMessages) {
               return this.mergeFrom((Mysqlx.ServerMessages)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public Mysqlx.ServerMessages.Builder mergeFrom(Mysqlx.ServerMessages other) {
            if (other == Mysqlx.ServerMessages.getDefaultInstance()) {
               return this;
            } else {
               this.mergeUnknownFields(other.getUnknownFields());
               return this;
            }
         }

         public final boolean isInitialized() {
            return true;
         }

         public Mysqlx.ServerMessages.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            Mysqlx.ServerMessages parsedMessage = null;

            try {
               parsedMessage = (Mysqlx.ServerMessages)Mysqlx.ServerMessages.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (Mysqlx.ServerMessages)var8.getUnfinishedMessage();
               throw var8;
            } finally {
               if (parsedMessage != null) {
                  this.mergeFrom(parsedMessage);
               }
            }

            return this;
         }
      }

      public static enum Type implements ProtocolMessageEnum {
         OK(0, 0),
         ERROR(1, 1),
         CONN_CAPABILITIES(2, 2),
         SESS_AUTHENTICATE_CONTINUE(3, 3),
         SESS_AUTHENTICATE_OK(4, 4),
         NOTICE(5, 11),
         RESULTSET_COLUMN_META_DATA(6, 12),
         RESULTSET_ROW(7, 13),
         RESULTSET_FETCH_DONE(8, 14),
         RESULTSET_FETCH_SUSPENDED(9, 15),
         RESULTSET_FETCH_DONE_MORE_RESULTSETS(10, 16),
         SQL_STMT_EXECUTE_OK(11, 17),
         RESULTSET_FETCH_DONE_MORE_OUT_PARAMS(12, 18);

         public static final int OK_VALUE = 0;
         public static final int ERROR_VALUE = 1;
         public static final int CONN_CAPABILITIES_VALUE = 2;
         public static final int SESS_AUTHENTICATE_CONTINUE_VALUE = 3;
         public static final int SESS_AUTHENTICATE_OK_VALUE = 4;
         public static final int NOTICE_VALUE = 11;
         public static final int RESULTSET_COLUMN_META_DATA_VALUE = 12;
         public static final int RESULTSET_ROW_VALUE = 13;
         public static final int RESULTSET_FETCH_DONE_VALUE = 14;
         public static final int RESULTSET_FETCH_SUSPENDED_VALUE = 15;
         public static final int RESULTSET_FETCH_DONE_MORE_RESULTSETS_VALUE = 16;
         public static final int SQL_STMT_EXECUTE_OK_VALUE = 17;
         public static final int RESULTSET_FETCH_DONE_MORE_OUT_PARAMS_VALUE = 18;
         private static EnumLiteMap<Mysqlx.ServerMessages.Type> internalValueMap = new EnumLiteMap<Mysqlx.ServerMessages.Type>() {
            public Mysqlx.ServerMessages.Type findValueByNumber(int number) {
               return Mysqlx.ServerMessages.Type.valueOf(number);
            }
         };
         private static final Mysqlx.ServerMessages.Type[] VALUES = values();
         private final int index;
         private final int value;

         public final int getNumber() {
            return this.value;
         }

         public static Mysqlx.ServerMessages.Type valueOf(int value) {
            switch(value) {
               case 0:
                  return OK;
               case 1:
                  return ERROR;
               case 2:
                  return CONN_CAPABILITIES;
               case 3:
                  return SESS_AUTHENTICATE_CONTINUE;
               case 4:
                  return SESS_AUTHENTICATE_OK;
               case 5:
               case 6:
               case 7:
               case 8:
               case 9:
               case 10:
               default:
                  return null;
               case 11:
                  return NOTICE;
               case 12:
                  return RESULTSET_COLUMN_META_DATA;
               case 13:
                  return RESULTSET_ROW;
               case 14:
                  return RESULTSET_FETCH_DONE;
               case 15:
                  return RESULTSET_FETCH_SUSPENDED;
               case 16:
                  return RESULTSET_FETCH_DONE_MORE_RESULTSETS;
               case 17:
                  return SQL_STMT_EXECUTE_OK;
               case 18:
                  return RESULTSET_FETCH_DONE_MORE_OUT_PARAMS;
            }
         }

         public static EnumLiteMap<Mysqlx.ServerMessages.Type> internalGetValueMap() {
            return internalValueMap;
         }

         public final EnumValueDescriptor getValueDescriptor() {
            return (EnumValueDescriptor)getDescriptor().getValues().get(this.index);
         }

         public final EnumDescriptor getDescriptorForType() {
            return getDescriptor();
         }

         public static final EnumDescriptor getDescriptor() {
            return (EnumDescriptor)Mysqlx.ServerMessages.getDescriptor().getEnumTypes().get(0);
         }

         public static Mysqlx.ServerMessages.Type valueOf(EnumValueDescriptor desc) {
            if (desc.getType() != getDescriptor()) {
               throw new IllegalArgumentException("EnumValueDescriptor is not for this type.");
            } else {
               return VALUES[desc.getIndex()];
            }
         }

         private Type(int index, int value) {
            this.index = index;
            this.value = value;
         }
      }
   }

   public interface ServerMessagesOrBuilder extends MessageOrBuilder {
   }
}
