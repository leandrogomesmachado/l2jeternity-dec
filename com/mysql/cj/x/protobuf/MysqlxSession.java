package com.mysql.cj.x.protobuf;

import com.google.protobuf.AbstractParser;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.Parser;
import com.google.protobuf.UnknownFieldSet;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner;
import com.google.protobuf.GeneratedMessage.BuilderParent;
import com.google.protobuf.GeneratedMessage.FieldAccessorTable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;

public final class MysqlxSession {
   private static final Descriptor internal_static_Mysqlx_Session_AuthenticateStart_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(0);
   private static FieldAccessorTable internal_static_Mysqlx_Session_AuthenticateStart_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Session_AuthenticateStart_descriptor, new String[]{"MechName", "AuthData", "InitialResponse"}
   );
   private static final Descriptor internal_static_Mysqlx_Session_AuthenticateContinue_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(1);
   private static FieldAccessorTable internal_static_Mysqlx_Session_AuthenticateContinue_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Session_AuthenticateContinue_descriptor, new String[]{"AuthData"}
   );
   private static final Descriptor internal_static_Mysqlx_Session_AuthenticateOk_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(2);
   private static FieldAccessorTable internal_static_Mysqlx_Session_AuthenticateOk_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Session_AuthenticateOk_descriptor, new String[]{"AuthData"}
   );
   private static final Descriptor internal_static_Mysqlx_Session_Reset_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(3);
   private static FieldAccessorTable internal_static_Mysqlx_Session_Reset_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Session_Reset_descriptor, new String[0]
   );
   private static final Descriptor internal_static_Mysqlx_Session_Close_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(4);
   private static FieldAccessorTable internal_static_Mysqlx_Session_Close_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Session_Close_descriptor, new String[0]
   );
   private static FileDescriptor descriptor;

   private MysqlxSession() {
   }

   public static void registerAllExtensions(ExtensionRegistry registry) {
   }

   public static FileDescriptor getDescriptor() {
      return descriptor;
   }

   static {
      String[] descriptorData = new String[]{
         "\n\u0014mysqlx_session.proto\u0012\u000eMysqlx.Session\u001a\fmysqlx.proto\"Y\n\u0011AuthenticateStart\u0012\u0011\n\tmech_name\u0018\u0001 \u0002(\t\u0012\u0011\n\tauth_data\u0018\u0002 \u0001(\f\u0012\u0018\n\u0010initial_response\u0018\u0003 \u0001(\f:\u0004\u0088ê0\u0004\"3\n\u0014AuthenticateContinue\u0012\u0011\n\tauth_data\u0018\u0001 \u0002(\f:\b\u0090ê0\u0003\u0088ê0\u0005\")\n\u000eAuthenticateOk\u0012\u0011\n\tauth_data\u0018\u0001 \u0001(\f:\u0004\u0090ê0\u0004\"\r\n\u0005Reset:\u0004\u0088ê0\u0006\"\r\n\u0005Close:\u0004\u0088ê0\u0007B\u0019\n\u0017com.mysql.cj.x.protobuf"
      };
      InternalDescriptorAssigner assigner = new InternalDescriptorAssigner() {
         public ExtensionRegistry assignDescriptors(FileDescriptor root) {
            MysqlxSession.descriptor = root;
            return null;
         }
      };
      FileDescriptor.internalBuildGeneratedFileFrom(descriptorData, new FileDescriptor[]{Mysqlx.getDescriptor()}, assigner);
      ExtensionRegistry registry = ExtensionRegistry.newInstance();
      registry.add(Mysqlx.clientMessageId);
      registry.add(Mysqlx.clientMessageId);
      registry.add(Mysqlx.serverMessageId);
      registry.add(Mysqlx.serverMessageId);
      registry.add(Mysqlx.clientMessageId);
      registry.add(Mysqlx.clientMessageId);
      FileDescriptor.internalUpdateFileDescriptor(descriptor, registry);
      Mysqlx.getDescriptor();
   }

   public static final class AuthenticateContinue extends GeneratedMessage implements MysqlxSession.AuthenticateContinueOrBuilder {
      private static final MysqlxSession.AuthenticateContinue defaultInstance = new MysqlxSession.AuthenticateContinue(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxSession.AuthenticateContinue> PARSER = new AbstractParser<MysqlxSession.AuthenticateContinue>() {
         public MysqlxSession.AuthenticateContinue parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxSession.AuthenticateContinue(input, extensionRegistry);
         }
      };
      private int bitField0_;
      public static final int AUTH_DATA_FIELD_NUMBER = 1;
      private ByteString authData_;
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private AuthenticateContinue(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private AuthenticateContinue(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static MysqlxSession.AuthenticateContinue getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxSession.AuthenticateContinue getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private AuthenticateContinue(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                     this.bitField0_ |= 1;
                     this.authData_ = input.readBytes();
                     break;
                  default:
                     if (!this.parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                        done = true;
                     }
               }
            }
         } catch (InvalidProtocolBufferException var11) {
            throw var11.setUnfinishedMessage(this);
         } catch (IOException var12) {
            throw new InvalidProtocolBufferException(var12.getMessage()).setUnfinishedMessage(this);
         } finally {
            this.unknownFields = unknownFields.build();
            this.makeExtensionsImmutable();
         }
      }

      public static final Descriptor getDescriptor() {
         return MysqlxSession.internal_static_Mysqlx_Session_AuthenticateContinue_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxSession.internal_static_Mysqlx_Session_AuthenticateContinue_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxSession.AuthenticateContinue.class, MysqlxSession.AuthenticateContinue.Builder.class);
      }

      public Parser<MysqlxSession.AuthenticateContinue> getParserForType() {
         return PARSER;
      }

      @Override
      public boolean hasAuthData() {
         return (this.bitField0_ & 1) == 1;
      }

      @Override
      public ByteString getAuthData() {
         return this.authData_;
      }

      private void initFields() {
         this.authData_ = ByteString.EMPTY;
      }

      public final boolean isInitialized() {
         byte isInitialized = this.memoizedIsInitialized;
         if (isInitialized == 1) {
            return true;
         } else if (isInitialized == 0) {
            return false;
         } else if (!this.hasAuthData()) {
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
            output.writeBytes(1, this.authData_);
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
               size += CodedOutputStream.computeBytesSize(1, this.authData_);
            }

            size += this.getUnknownFields().getSerializedSize();
            this.memoizedSerializedSize = size;
            return size;
         }
      }

      protected Object writeReplace() throws ObjectStreamException {
         return super.writeReplace();
      }

      public static MysqlxSession.AuthenticateContinue parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxSession.AuthenticateContinue)PARSER.parseFrom(data);
      }

      public static MysqlxSession.AuthenticateContinue parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxSession.AuthenticateContinue)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxSession.AuthenticateContinue parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxSession.AuthenticateContinue)PARSER.parseFrom(data);
      }

      public static MysqlxSession.AuthenticateContinue parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxSession.AuthenticateContinue)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxSession.AuthenticateContinue parseFrom(InputStream input) throws IOException {
         return (MysqlxSession.AuthenticateContinue)PARSER.parseFrom(input);
      }

      public static MysqlxSession.AuthenticateContinue parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxSession.AuthenticateContinue)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxSession.AuthenticateContinue parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxSession.AuthenticateContinue)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxSession.AuthenticateContinue parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxSession.AuthenticateContinue)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxSession.AuthenticateContinue parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxSession.AuthenticateContinue)PARSER.parseFrom(input);
      }

      public static MysqlxSession.AuthenticateContinue parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxSession.AuthenticateContinue)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxSession.AuthenticateContinue.Builder newBuilder() {
         return MysqlxSession.AuthenticateContinue.Builder.create();
      }

      public MysqlxSession.AuthenticateContinue.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxSession.AuthenticateContinue.Builder newBuilder(MysqlxSession.AuthenticateContinue prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxSession.AuthenticateContinue.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxSession.AuthenticateContinue.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxSession.AuthenticateContinue.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxSession.AuthenticateContinue.Builder>
         implements MysqlxSession.AuthenticateContinueOrBuilder {
         private int bitField0_;
         private ByteString authData_ = ByteString.EMPTY;

         public static final Descriptor getDescriptor() {
            return MysqlxSession.internal_static_Mysqlx_Session_AuthenticateContinue_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxSession.internal_static_Mysqlx_Session_AuthenticateContinue_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxSession.AuthenticateContinue.class, MysqlxSession.AuthenticateContinue.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxSession.AuthenticateContinue.alwaysUseFieldBuilders) {
            }
         }

         private static MysqlxSession.AuthenticateContinue.Builder create() {
            return new MysqlxSession.AuthenticateContinue.Builder();
         }

         public MysqlxSession.AuthenticateContinue.Builder clear() {
            super.clear();
            this.authData_ = ByteString.EMPTY;
            this.bitField0_ &= -2;
            return this;
         }

         public MysqlxSession.AuthenticateContinue.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxSession.internal_static_Mysqlx_Session_AuthenticateContinue_descriptor;
         }

         public MysqlxSession.AuthenticateContinue getDefaultInstanceForType() {
            return MysqlxSession.AuthenticateContinue.getDefaultInstance();
         }

         public MysqlxSession.AuthenticateContinue build() {
            MysqlxSession.AuthenticateContinue result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxSession.AuthenticateContinue buildPartial() {
            MysqlxSession.AuthenticateContinue result = new MysqlxSession.AuthenticateContinue(this);
            int from_bitField0_ = this.bitField0_;
            int to_bitField0_ = 0;
            if ((from_bitField0_ & 1) == 1) {
               to_bitField0_ |= 1;
            }

            result.authData_ = this.authData_;
            result.bitField0_ = to_bitField0_;
            this.onBuilt();
            return result;
         }

         public MysqlxSession.AuthenticateContinue.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxSession.AuthenticateContinue) {
               return this.mergeFrom((MysqlxSession.AuthenticateContinue)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxSession.AuthenticateContinue.Builder mergeFrom(MysqlxSession.AuthenticateContinue other) {
            if (other == MysqlxSession.AuthenticateContinue.getDefaultInstance()) {
               return this;
            } else {
               if (other.hasAuthData()) {
                  this.setAuthData(other.getAuthData());
               }

               this.mergeUnknownFields(other.getUnknownFields());
               return this;
            }
         }

         public final boolean isInitialized() {
            return this.hasAuthData();
         }

         public MysqlxSession.AuthenticateContinue.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxSession.AuthenticateContinue parsedMessage = null;

            try {
               parsedMessage = (MysqlxSession.AuthenticateContinue)MysqlxSession.AuthenticateContinue.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxSession.AuthenticateContinue)var8.getUnfinishedMessage();
               throw var8;
            } finally {
               if (parsedMessage != null) {
                  this.mergeFrom(parsedMessage);
               }
            }

            return this;
         }

         @Override
         public boolean hasAuthData() {
            return (this.bitField0_ & 1) == 1;
         }

         @Override
         public ByteString getAuthData() {
            return this.authData_;
         }

         public MysqlxSession.AuthenticateContinue.Builder setAuthData(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 1;
               this.authData_ = value;
               this.onChanged();
               return this;
            }
         }

         public MysqlxSession.AuthenticateContinue.Builder clearAuthData() {
            this.bitField0_ &= -2;
            this.authData_ = MysqlxSession.AuthenticateContinue.getDefaultInstance().getAuthData();
            this.onChanged();
            return this;
         }
      }
   }

   public interface AuthenticateContinueOrBuilder extends MessageOrBuilder {
      boolean hasAuthData();

      ByteString getAuthData();
   }

   public static final class AuthenticateOk extends GeneratedMessage implements MysqlxSession.AuthenticateOkOrBuilder {
      private static final MysqlxSession.AuthenticateOk defaultInstance = new MysqlxSession.AuthenticateOk(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxSession.AuthenticateOk> PARSER = new AbstractParser<MysqlxSession.AuthenticateOk>() {
         public MysqlxSession.AuthenticateOk parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxSession.AuthenticateOk(input, extensionRegistry);
         }
      };
      private int bitField0_;
      public static final int AUTH_DATA_FIELD_NUMBER = 1;
      private ByteString authData_;
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private AuthenticateOk(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private AuthenticateOk(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static MysqlxSession.AuthenticateOk getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxSession.AuthenticateOk getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private AuthenticateOk(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                     this.bitField0_ |= 1;
                     this.authData_ = input.readBytes();
                     break;
                  default:
                     if (!this.parseUnknownField(input, unknownFields, extensionRegistry, tag)) {
                        done = true;
                     }
               }
            }
         } catch (InvalidProtocolBufferException var11) {
            throw var11.setUnfinishedMessage(this);
         } catch (IOException var12) {
            throw new InvalidProtocolBufferException(var12.getMessage()).setUnfinishedMessage(this);
         } finally {
            this.unknownFields = unknownFields.build();
            this.makeExtensionsImmutable();
         }
      }

      public static final Descriptor getDescriptor() {
         return MysqlxSession.internal_static_Mysqlx_Session_AuthenticateOk_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxSession.internal_static_Mysqlx_Session_AuthenticateOk_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxSession.AuthenticateOk.class, MysqlxSession.AuthenticateOk.Builder.class);
      }

      public Parser<MysqlxSession.AuthenticateOk> getParserForType() {
         return PARSER;
      }

      @Override
      public boolean hasAuthData() {
         return (this.bitField0_ & 1) == 1;
      }

      @Override
      public ByteString getAuthData() {
         return this.authData_;
      }

      private void initFields() {
         this.authData_ = ByteString.EMPTY;
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
            output.writeBytes(1, this.authData_);
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
               size += CodedOutputStream.computeBytesSize(1, this.authData_);
            }

            size += this.getUnknownFields().getSerializedSize();
            this.memoizedSerializedSize = size;
            return size;
         }
      }

      protected Object writeReplace() throws ObjectStreamException {
         return super.writeReplace();
      }

      public static MysqlxSession.AuthenticateOk parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxSession.AuthenticateOk)PARSER.parseFrom(data);
      }

      public static MysqlxSession.AuthenticateOk parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxSession.AuthenticateOk)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxSession.AuthenticateOk parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxSession.AuthenticateOk)PARSER.parseFrom(data);
      }

      public static MysqlxSession.AuthenticateOk parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxSession.AuthenticateOk)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxSession.AuthenticateOk parseFrom(InputStream input) throws IOException {
         return (MysqlxSession.AuthenticateOk)PARSER.parseFrom(input);
      }

      public static MysqlxSession.AuthenticateOk parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxSession.AuthenticateOk)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxSession.AuthenticateOk parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxSession.AuthenticateOk)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxSession.AuthenticateOk parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxSession.AuthenticateOk)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxSession.AuthenticateOk parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxSession.AuthenticateOk)PARSER.parseFrom(input);
      }

      public static MysqlxSession.AuthenticateOk parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxSession.AuthenticateOk)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxSession.AuthenticateOk.Builder newBuilder() {
         return MysqlxSession.AuthenticateOk.Builder.create();
      }

      public MysqlxSession.AuthenticateOk.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxSession.AuthenticateOk.Builder newBuilder(MysqlxSession.AuthenticateOk prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxSession.AuthenticateOk.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxSession.AuthenticateOk.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxSession.AuthenticateOk.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxSession.AuthenticateOk.Builder>
         implements MysqlxSession.AuthenticateOkOrBuilder {
         private int bitField0_;
         private ByteString authData_ = ByteString.EMPTY;

         public static final Descriptor getDescriptor() {
            return MysqlxSession.internal_static_Mysqlx_Session_AuthenticateOk_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxSession.internal_static_Mysqlx_Session_AuthenticateOk_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxSession.AuthenticateOk.class, MysqlxSession.AuthenticateOk.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxSession.AuthenticateOk.alwaysUseFieldBuilders) {
            }
         }

         private static MysqlxSession.AuthenticateOk.Builder create() {
            return new MysqlxSession.AuthenticateOk.Builder();
         }

         public MysqlxSession.AuthenticateOk.Builder clear() {
            super.clear();
            this.authData_ = ByteString.EMPTY;
            this.bitField0_ &= -2;
            return this;
         }

         public MysqlxSession.AuthenticateOk.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxSession.internal_static_Mysqlx_Session_AuthenticateOk_descriptor;
         }

         public MysqlxSession.AuthenticateOk getDefaultInstanceForType() {
            return MysqlxSession.AuthenticateOk.getDefaultInstance();
         }

         public MysqlxSession.AuthenticateOk build() {
            MysqlxSession.AuthenticateOk result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxSession.AuthenticateOk buildPartial() {
            MysqlxSession.AuthenticateOk result = new MysqlxSession.AuthenticateOk(this);
            int from_bitField0_ = this.bitField0_;
            int to_bitField0_ = 0;
            if ((from_bitField0_ & 1) == 1) {
               to_bitField0_ |= 1;
            }

            result.authData_ = this.authData_;
            result.bitField0_ = to_bitField0_;
            this.onBuilt();
            return result;
         }

         public MysqlxSession.AuthenticateOk.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxSession.AuthenticateOk) {
               return this.mergeFrom((MysqlxSession.AuthenticateOk)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxSession.AuthenticateOk.Builder mergeFrom(MysqlxSession.AuthenticateOk other) {
            if (other == MysqlxSession.AuthenticateOk.getDefaultInstance()) {
               return this;
            } else {
               if (other.hasAuthData()) {
                  this.setAuthData(other.getAuthData());
               }

               this.mergeUnknownFields(other.getUnknownFields());
               return this;
            }
         }

         public final boolean isInitialized() {
            return true;
         }

         public MysqlxSession.AuthenticateOk.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxSession.AuthenticateOk parsedMessage = null;

            try {
               parsedMessage = (MysqlxSession.AuthenticateOk)MysqlxSession.AuthenticateOk.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxSession.AuthenticateOk)var8.getUnfinishedMessage();
               throw var8;
            } finally {
               if (parsedMessage != null) {
                  this.mergeFrom(parsedMessage);
               }
            }

            return this;
         }

         @Override
         public boolean hasAuthData() {
            return (this.bitField0_ & 1) == 1;
         }

         @Override
         public ByteString getAuthData() {
            return this.authData_;
         }

         public MysqlxSession.AuthenticateOk.Builder setAuthData(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 1;
               this.authData_ = value;
               this.onChanged();
               return this;
            }
         }

         public MysqlxSession.AuthenticateOk.Builder clearAuthData() {
            this.bitField0_ &= -2;
            this.authData_ = MysqlxSession.AuthenticateOk.getDefaultInstance().getAuthData();
            this.onChanged();
            return this;
         }
      }
   }

   public interface AuthenticateOkOrBuilder extends MessageOrBuilder {
      boolean hasAuthData();

      ByteString getAuthData();
   }

   public static final class AuthenticateStart extends GeneratedMessage implements MysqlxSession.AuthenticateStartOrBuilder {
      private static final MysqlxSession.AuthenticateStart defaultInstance = new MysqlxSession.AuthenticateStart(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxSession.AuthenticateStart> PARSER = new AbstractParser<MysqlxSession.AuthenticateStart>() {
         public MysqlxSession.AuthenticateStart parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxSession.AuthenticateStart(input, extensionRegistry);
         }
      };
      private int bitField0_;
      public static final int MECH_NAME_FIELD_NUMBER = 1;
      private Object mechName_;
      public static final int AUTH_DATA_FIELD_NUMBER = 2;
      private ByteString authData_;
      public static final int INITIAL_RESPONSE_FIELD_NUMBER = 3;
      private ByteString initialResponse_;
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private AuthenticateStart(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private AuthenticateStart(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static MysqlxSession.AuthenticateStart getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxSession.AuthenticateStart getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private AuthenticateStart(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                     this.mechName_ = bs;
                     break;
                  case 18:
                     this.bitField0_ |= 2;
                     this.authData_ = input.readBytes();
                     break;
                  case 26:
                     this.bitField0_ |= 4;
                     this.initialResponse_ = input.readBytes();
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
         return MysqlxSession.internal_static_Mysqlx_Session_AuthenticateStart_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxSession.internal_static_Mysqlx_Session_AuthenticateStart_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxSession.AuthenticateStart.class, MysqlxSession.AuthenticateStart.Builder.class);
      }

      public Parser<MysqlxSession.AuthenticateStart> getParserForType() {
         return PARSER;
      }

      @Override
      public boolean hasMechName() {
         return (this.bitField0_ & 1) == 1;
      }

      @Override
      public String getMechName() {
         Object ref = this.mechName_;
         if (ref instanceof String) {
            return (String)ref;
         } else {
            ByteString bs = (ByteString)ref;
            String s = bs.toStringUtf8();
            if (bs.isValidUtf8()) {
               this.mechName_ = s;
            }

            return s;
         }
      }

      @Override
      public ByteString getMechNameBytes() {
         Object ref = this.mechName_;
         if (ref instanceof String) {
            ByteString b = ByteString.copyFromUtf8((String)ref);
            this.mechName_ = b;
            return b;
         } else {
            return (ByteString)ref;
         }
      }

      @Override
      public boolean hasAuthData() {
         return (this.bitField0_ & 2) == 2;
      }

      @Override
      public ByteString getAuthData() {
         return this.authData_;
      }

      @Override
      public boolean hasInitialResponse() {
         return (this.bitField0_ & 4) == 4;
      }

      @Override
      public ByteString getInitialResponse() {
         return this.initialResponse_;
      }

      private void initFields() {
         this.mechName_ = "";
         this.authData_ = ByteString.EMPTY;
         this.initialResponse_ = ByteString.EMPTY;
      }

      public final boolean isInitialized() {
         byte isInitialized = this.memoizedIsInitialized;
         if (isInitialized == 1) {
            return true;
         } else if (isInitialized == 0) {
            return false;
         } else if (!this.hasMechName()) {
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
            output.writeBytes(1, this.getMechNameBytes());
         }

         if ((this.bitField0_ & 2) == 2) {
            output.writeBytes(2, this.authData_);
         }

         if ((this.bitField0_ & 4) == 4) {
            output.writeBytes(3, this.initialResponse_);
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
               size += CodedOutputStream.computeBytesSize(1, this.getMechNameBytes());
            }

            if ((this.bitField0_ & 2) == 2) {
               size += CodedOutputStream.computeBytesSize(2, this.authData_);
            }

            if ((this.bitField0_ & 4) == 4) {
               size += CodedOutputStream.computeBytesSize(3, this.initialResponse_);
            }

            size += this.getUnknownFields().getSerializedSize();
            this.memoizedSerializedSize = size;
            return size;
         }
      }

      protected Object writeReplace() throws ObjectStreamException {
         return super.writeReplace();
      }

      public static MysqlxSession.AuthenticateStart parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxSession.AuthenticateStart)PARSER.parseFrom(data);
      }

      public static MysqlxSession.AuthenticateStart parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxSession.AuthenticateStart)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxSession.AuthenticateStart parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxSession.AuthenticateStart)PARSER.parseFrom(data);
      }

      public static MysqlxSession.AuthenticateStart parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxSession.AuthenticateStart)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxSession.AuthenticateStart parseFrom(InputStream input) throws IOException {
         return (MysqlxSession.AuthenticateStart)PARSER.parseFrom(input);
      }

      public static MysqlxSession.AuthenticateStart parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxSession.AuthenticateStart)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxSession.AuthenticateStart parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxSession.AuthenticateStart)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxSession.AuthenticateStart parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxSession.AuthenticateStart)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxSession.AuthenticateStart parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxSession.AuthenticateStart)PARSER.parseFrom(input);
      }

      public static MysqlxSession.AuthenticateStart parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxSession.AuthenticateStart)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxSession.AuthenticateStart.Builder newBuilder() {
         return MysqlxSession.AuthenticateStart.Builder.create();
      }

      public MysqlxSession.AuthenticateStart.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxSession.AuthenticateStart.Builder newBuilder(MysqlxSession.AuthenticateStart prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxSession.AuthenticateStart.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxSession.AuthenticateStart.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxSession.AuthenticateStart.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxSession.AuthenticateStart.Builder>
         implements MysqlxSession.AuthenticateStartOrBuilder {
         private int bitField0_;
         private Object mechName_ = "";
         private ByteString authData_ = ByteString.EMPTY;
         private ByteString initialResponse_ = ByteString.EMPTY;

         public static final Descriptor getDescriptor() {
            return MysqlxSession.internal_static_Mysqlx_Session_AuthenticateStart_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxSession.internal_static_Mysqlx_Session_AuthenticateStart_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxSession.AuthenticateStart.class, MysqlxSession.AuthenticateStart.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxSession.AuthenticateStart.alwaysUseFieldBuilders) {
            }
         }

         private static MysqlxSession.AuthenticateStart.Builder create() {
            return new MysqlxSession.AuthenticateStart.Builder();
         }

         public MysqlxSession.AuthenticateStart.Builder clear() {
            super.clear();
            this.mechName_ = "";
            this.bitField0_ &= -2;
            this.authData_ = ByteString.EMPTY;
            this.bitField0_ &= -3;
            this.initialResponse_ = ByteString.EMPTY;
            this.bitField0_ &= -5;
            return this;
         }

         public MysqlxSession.AuthenticateStart.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxSession.internal_static_Mysqlx_Session_AuthenticateStart_descriptor;
         }

         public MysqlxSession.AuthenticateStart getDefaultInstanceForType() {
            return MysqlxSession.AuthenticateStart.getDefaultInstance();
         }

         public MysqlxSession.AuthenticateStart build() {
            MysqlxSession.AuthenticateStart result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxSession.AuthenticateStart buildPartial() {
            MysqlxSession.AuthenticateStart result = new MysqlxSession.AuthenticateStart(this);
            int from_bitField0_ = this.bitField0_;
            int to_bitField0_ = 0;
            if ((from_bitField0_ & 1) == 1) {
               to_bitField0_ |= 1;
            }

            result.mechName_ = this.mechName_;
            if ((from_bitField0_ & 2) == 2) {
               to_bitField0_ |= 2;
            }

            result.authData_ = this.authData_;
            if ((from_bitField0_ & 4) == 4) {
               to_bitField0_ |= 4;
            }

            result.initialResponse_ = this.initialResponse_;
            result.bitField0_ = to_bitField0_;
            this.onBuilt();
            return result;
         }

         public MysqlxSession.AuthenticateStart.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxSession.AuthenticateStart) {
               return this.mergeFrom((MysqlxSession.AuthenticateStart)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxSession.AuthenticateStart.Builder mergeFrom(MysqlxSession.AuthenticateStart other) {
            if (other == MysqlxSession.AuthenticateStart.getDefaultInstance()) {
               return this;
            } else {
               if (other.hasMechName()) {
                  this.bitField0_ |= 1;
                  this.mechName_ = other.mechName_;
                  this.onChanged();
               }

               if (other.hasAuthData()) {
                  this.setAuthData(other.getAuthData());
               }

               if (other.hasInitialResponse()) {
                  this.setInitialResponse(other.getInitialResponse());
               }

               this.mergeUnknownFields(other.getUnknownFields());
               return this;
            }
         }

         public final boolean isInitialized() {
            return this.hasMechName();
         }

         public MysqlxSession.AuthenticateStart.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxSession.AuthenticateStart parsedMessage = null;

            try {
               parsedMessage = (MysqlxSession.AuthenticateStart)MysqlxSession.AuthenticateStart.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxSession.AuthenticateStart)var8.getUnfinishedMessage();
               throw var8;
            } finally {
               if (parsedMessage != null) {
                  this.mergeFrom(parsedMessage);
               }
            }

            return this;
         }

         @Override
         public boolean hasMechName() {
            return (this.bitField0_ & 1) == 1;
         }

         @Override
         public String getMechName() {
            Object ref = this.mechName_;
            if (!(ref instanceof String)) {
               ByteString bs = (ByteString)ref;
               String s = bs.toStringUtf8();
               if (bs.isValidUtf8()) {
                  this.mechName_ = s;
               }

               return s;
            } else {
               return (String)ref;
            }
         }

         @Override
         public ByteString getMechNameBytes() {
            Object ref = this.mechName_;
            if (ref instanceof String) {
               ByteString b = ByteString.copyFromUtf8((String)ref);
               this.mechName_ = b;
               return b;
            } else {
               return (ByteString)ref;
            }
         }

         public MysqlxSession.AuthenticateStart.Builder setMechName(String value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 1;
               this.mechName_ = value;
               this.onChanged();
               return this;
            }
         }

         public MysqlxSession.AuthenticateStart.Builder clearMechName() {
            this.bitField0_ &= -2;
            this.mechName_ = MysqlxSession.AuthenticateStart.getDefaultInstance().getMechName();
            this.onChanged();
            return this;
         }

         public MysqlxSession.AuthenticateStart.Builder setMechNameBytes(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 1;
               this.mechName_ = value;
               this.onChanged();
               return this;
            }
         }

         @Override
         public boolean hasAuthData() {
            return (this.bitField0_ & 2) == 2;
         }

         @Override
         public ByteString getAuthData() {
            return this.authData_;
         }

         public MysqlxSession.AuthenticateStart.Builder setAuthData(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 2;
               this.authData_ = value;
               this.onChanged();
               return this;
            }
         }

         public MysqlxSession.AuthenticateStart.Builder clearAuthData() {
            this.bitField0_ &= -3;
            this.authData_ = MysqlxSession.AuthenticateStart.getDefaultInstance().getAuthData();
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasInitialResponse() {
            return (this.bitField0_ & 4) == 4;
         }

         @Override
         public ByteString getInitialResponse() {
            return this.initialResponse_;
         }

         public MysqlxSession.AuthenticateStart.Builder setInitialResponse(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 4;
               this.initialResponse_ = value;
               this.onChanged();
               return this;
            }
         }

         public MysqlxSession.AuthenticateStart.Builder clearInitialResponse() {
            this.bitField0_ &= -5;
            this.initialResponse_ = MysqlxSession.AuthenticateStart.getDefaultInstance().getInitialResponse();
            this.onChanged();
            return this;
         }
      }
   }

   public interface AuthenticateStartOrBuilder extends MessageOrBuilder {
      boolean hasMechName();

      String getMechName();

      ByteString getMechNameBytes();

      boolean hasAuthData();

      ByteString getAuthData();

      boolean hasInitialResponse();

      ByteString getInitialResponse();
   }

   public static final class Close extends GeneratedMessage implements MysqlxSession.CloseOrBuilder {
      private static final MysqlxSession.Close defaultInstance = new MysqlxSession.Close(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxSession.Close> PARSER = new AbstractParser<MysqlxSession.Close>() {
         public MysqlxSession.Close parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxSession.Close(input, extensionRegistry);
         }
      };
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private Close(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private Close(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static MysqlxSession.Close getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxSession.Close getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private Close(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
         return MysqlxSession.internal_static_Mysqlx_Session_Close_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxSession.internal_static_Mysqlx_Session_Close_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxSession.Close.class, MysqlxSession.Close.Builder.class);
      }

      public Parser<MysqlxSession.Close> getParserForType() {
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

      public static MysqlxSession.Close parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxSession.Close)PARSER.parseFrom(data);
      }

      public static MysqlxSession.Close parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxSession.Close)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxSession.Close parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxSession.Close)PARSER.parseFrom(data);
      }

      public static MysqlxSession.Close parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxSession.Close)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxSession.Close parseFrom(InputStream input) throws IOException {
         return (MysqlxSession.Close)PARSER.parseFrom(input);
      }

      public static MysqlxSession.Close parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxSession.Close)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxSession.Close parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxSession.Close)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxSession.Close parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxSession.Close)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxSession.Close parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxSession.Close)PARSER.parseFrom(input);
      }

      public static MysqlxSession.Close parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxSession.Close)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxSession.Close.Builder newBuilder() {
         return MysqlxSession.Close.Builder.create();
      }

      public MysqlxSession.Close.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxSession.Close.Builder newBuilder(MysqlxSession.Close prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxSession.Close.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxSession.Close.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxSession.Close.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxSession.Close.Builder>
         implements MysqlxSession.CloseOrBuilder {
         public static final Descriptor getDescriptor() {
            return MysqlxSession.internal_static_Mysqlx_Session_Close_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxSession.internal_static_Mysqlx_Session_Close_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxSession.Close.class, MysqlxSession.Close.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxSession.Close.alwaysUseFieldBuilders) {
            }
         }

         private static MysqlxSession.Close.Builder create() {
            return new MysqlxSession.Close.Builder();
         }

         public MysqlxSession.Close.Builder clear() {
            super.clear();
            return this;
         }

         public MysqlxSession.Close.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxSession.internal_static_Mysqlx_Session_Close_descriptor;
         }

         public MysqlxSession.Close getDefaultInstanceForType() {
            return MysqlxSession.Close.getDefaultInstance();
         }

         public MysqlxSession.Close build() {
            MysqlxSession.Close result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxSession.Close buildPartial() {
            MysqlxSession.Close result = new MysqlxSession.Close(this);
            this.onBuilt();
            return result;
         }

         public MysqlxSession.Close.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxSession.Close) {
               return this.mergeFrom((MysqlxSession.Close)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxSession.Close.Builder mergeFrom(MysqlxSession.Close other) {
            if (other == MysqlxSession.Close.getDefaultInstance()) {
               return this;
            } else {
               this.mergeUnknownFields(other.getUnknownFields());
               return this;
            }
         }

         public final boolean isInitialized() {
            return true;
         }

         public MysqlxSession.Close.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxSession.Close parsedMessage = null;

            try {
               parsedMessage = (MysqlxSession.Close)MysqlxSession.Close.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxSession.Close)var8.getUnfinishedMessage();
               throw var8;
            } finally {
               if (parsedMessage != null) {
                  this.mergeFrom(parsedMessage);
               }
            }

            return this;
         }
      }
   }

   public interface CloseOrBuilder extends MessageOrBuilder {
   }

   public static final class Reset extends GeneratedMessage implements MysqlxSession.ResetOrBuilder {
      private static final MysqlxSession.Reset defaultInstance = new MysqlxSession.Reset(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxSession.Reset> PARSER = new AbstractParser<MysqlxSession.Reset>() {
         public MysqlxSession.Reset parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxSession.Reset(input, extensionRegistry);
         }
      };
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private Reset(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private Reset(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static MysqlxSession.Reset getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxSession.Reset getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private Reset(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
         return MysqlxSession.internal_static_Mysqlx_Session_Reset_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxSession.internal_static_Mysqlx_Session_Reset_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxSession.Reset.class, MysqlxSession.Reset.Builder.class);
      }

      public Parser<MysqlxSession.Reset> getParserForType() {
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

      public static MysqlxSession.Reset parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxSession.Reset)PARSER.parseFrom(data);
      }

      public static MysqlxSession.Reset parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxSession.Reset)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxSession.Reset parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxSession.Reset)PARSER.parseFrom(data);
      }

      public static MysqlxSession.Reset parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxSession.Reset)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxSession.Reset parseFrom(InputStream input) throws IOException {
         return (MysqlxSession.Reset)PARSER.parseFrom(input);
      }

      public static MysqlxSession.Reset parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxSession.Reset)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxSession.Reset parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxSession.Reset)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxSession.Reset parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxSession.Reset)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxSession.Reset parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxSession.Reset)PARSER.parseFrom(input);
      }

      public static MysqlxSession.Reset parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxSession.Reset)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxSession.Reset.Builder newBuilder() {
         return MysqlxSession.Reset.Builder.create();
      }

      public MysqlxSession.Reset.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxSession.Reset.Builder newBuilder(MysqlxSession.Reset prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxSession.Reset.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxSession.Reset.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxSession.Reset.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxSession.Reset.Builder>
         implements MysqlxSession.ResetOrBuilder {
         public static final Descriptor getDescriptor() {
            return MysqlxSession.internal_static_Mysqlx_Session_Reset_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxSession.internal_static_Mysqlx_Session_Reset_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxSession.Reset.class, MysqlxSession.Reset.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxSession.Reset.alwaysUseFieldBuilders) {
            }
         }

         private static MysqlxSession.Reset.Builder create() {
            return new MysqlxSession.Reset.Builder();
         }

         public MysqlxSession.Reset.Builder clear() {
            super.clear();
            return this;
         }

         public MysqlxSession.Reset.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxSession.internal_static_Mysqlx_Session_Reset_descriptor;
         }

         public MysqlxSession.Reset getDefaultInstanceForType() {
            return MysqlxSession.Reset.getDefaultInstance();
         }

         public MysqlxSession.Reset build() {
            MysqlxSession.Reset result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxSession.Reset buildPartial() {
            MysqlxSession.Reset result = new MysqlxSession.Reset(this);
            this.onBuilt();
            return result;
         }

         public MysqlxSession.Reset.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxSession.Reset) {
               return this.mergeFrom((MysqlxSession.Reset)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxSession.Reset.Builder mergeFrom(MysqlxSession.Reset other) {
            if (other == MysqlxSession.Reset.getDefaultInstance()) {
               return this;
            } else {
               this.mergeUnknownFields(other.getUnknownFields());
               return this;
            }
         }

         public final boolean isInitialized() {
            return true;
         }

         public MysqlxSession.Reset.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxSession.Reset parsedMessage = null;

            try {
               parsedMessage = (MysqlxSession.Reset)MysqlxSession.Reset.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxSession.Reset)var8.getUnfinishedMessage();
               throw var8;
            } finally {
               if (parsedMessage != null) {
                  this.mergeFrom(parsedMessage);
               }
            }

            return this;
         }
      }
   }

   public interface ResetOrBuilder extends MessageOrBuilder {
   }
}
