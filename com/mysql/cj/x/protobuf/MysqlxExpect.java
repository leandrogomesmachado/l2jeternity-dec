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
import com.google.protobuf.MessageLite;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.Parser;
import com.google.protobuf.ProtocolMessageEnum;
import com.google.protobuf.RepeatedFieldBuilder;
import com.google.protobuf.UnknownFieldSet;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner;
import com.google.protobuf.GeneratedMessage.BuilderParent;
import com.google.protobuf.GeneratedMessage.FieldAccessorTable;
import com.google.protobuf.Internal.EnumLiteMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MysqlxExpect {
   private static final Descriptor internal_static_Mysqlx_Expect_Open_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(0);
   private static FieldAccessorTable internal_static_Mysqlx_Expect_Open_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Expect_Open_descriptor, new String[]{"Op", "Cond"}
   );
   private static final Descriptor internal_static_Mysqlx_Expect_Open_Condition_descriptor = (Descriptor)internal_static_Mysqlx_Expect_Open_descriptor.getNestedTypes(
         
      )
      .get(0);
   private static FieldAccessorTable internal_static_Mysqlx_Expect_Open_Condition_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Expect_Open_Condition_descriptor, new String[]{"ConditionKey", "ConditionValue", "Op"}
   );
   private static final Descriptor internal_static_Mysqlx_Expect_Close_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(1);
   private static FieldAccessorTable internal_static_Mysqlx_Expect_Close_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Expect_Close_descriptor, new String[0]
   );
   private static FileDescriptor descriptor;

   private MysqlxExpect() {
   }

   public static void registerAllExtensions(ExtensionRegistry registry) {
   }

   public static FileDescriptor getDescriptor() {
      return descriptor;
   }

   static {
      String[] descriptorData = new String[]{
         "\n\u0013mysqlx_expect.proto\u0012\rMysqlx.Expect\u001a\fmysqlx.proto\"Ö\u0003\n\u0004Open\u0012B\n\u0002op\u0018\u0001 \u0001(\u000e2 .Mysqlx.Expect.Open.CtxOperation:\u0014EXPECT_CTX_COPY_PREV\u0012+\n\u0004cond\u0018\u0002 \u0003(\u000b2\u001d.Mysqlx.Expect.Open.Condition\u001a\u0096\u0002\n\tCondition\u0012\u0015\n\rcondition_key\u0018\u0001 \u0002(\r\u0012\u0017\n\u000fcondition_value\u0018\u0002 \u0001(\f\u0012K\n\u0002op\u0018\u0003 \u0001(\u000e20.Mysqlx.Expect.Open.Condition.ConditionOperation:\rEXPECT_OP_SET\"N\n\u0003Key\u0012\u0013\n\u000fEXPECT_NO_ERROR\u0010\u0001\u0012\u0016\n\u0012EXPECT_FIELD_EXIST\u0010\u0002\u0012\u001a\n\u0016EXPECT_DOCID_GENERATED\u0010\u0003\"<\n\u0012Condi",
         "tionOperation\u0012\u0011\n\rEXPECT_OP_SET\u0010\u0000\u0012\u0013\n\u000fEXPECT_OP_UNSET\u0010\u0001\">\n\fCtxOperation\u0012\u0018\n\u0014EXPECT_CTX_COPY_PREV\u0010\u0000\u0012\u0014\n\u0010EXPECT_CTX_EMPTY\u0010\u0001:\u0004\u0088ê0\u0018\"\r\n\u0005Close:\u0004\u0088ê0\u0019B\u0019\n\u0017com.mysql.cj.x.protobuf"
      };
      InternalDescriptorAssigner assigner = new InternalDescriptorAssigner() {
         public ExtensionRegistry assignDescriptors(FileDescriptor root) {
            MysqlxExpect.descriptor = root;
            return null;
         }
      };
      FileDescriptor.internalBuildGeneratedFileFrom(descriptorData, new FileDescriptor[]{Mysqlx.getDescriptor()}, assigner);
      ExtensionRegistry registry = ExtensionRegistry.newInstance();
      registry.add(Mysqlx.clientMessageId);
      registry.add(Mysqlx.clientMessageId);
      FileDescriptor.internalUpdateFileDescriptor(descriptor, registry);
      Mysqlx.getDescriptor();
   }

   public static final class Close extends GeneratedMessage implements MysqlxExpect.CloseOrBuilder {
      private static final MysqlxExpect.Close defaultInstance = new MysqlxExpect.Close(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxExpect.Close> PARSER = new AbstractParser<MysqlxExpect.Close>() {
         public MysqlxExpect.Close parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxExpect.Close(input, extensionRegistry);
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

      public static MysqlxExpect.Close getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxExpect.Close getDefaultInstanceForType() {
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
         return MysqlxExpect.internal_static_Mysqlx_Expect_Close_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxExpect.internal_static_Mysqlx_Expect_Close_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxExpect.Close.class, MysqlxExpect.Close.Builder.class);
      }

      public Parser<MysqlxExpect.Close> getParserForType() {
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

      public static MysqlxExpect.Close parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxExpect.Close)PARSER.parseFrom(data);
      }

      public static MysqlxExpect.Close parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxExpect.Close)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxExpect.Close parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxExpect.Close)PARSER.parseFrom(data);
      }

      public static MysqlxExpect.Close parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxExpect.Close)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxExpect.Close parseFrom(InputStream input) throws IOException {
         return (MysqlxExpect.Close)PARSER.parseFrom(input);
      }

      public static MysqlxExpect.Close parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxExpect.Close)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxExpect.Close parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxExpect.Close)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxExpect.Close parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxExpect.Close)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxExpect.Close parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxExpect.Close)PARSER.parseFrom(input);
      }

      public static MysqlxExpect.Close parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxExpect.Close)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxExpect.Close.Builder newBuilder() {
         return MysqlxExpect.Close.Builder.create();
      }

      public MysqlxExpect.Close.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxExpect.Close.Builder newBuilder(MysqlxExpect.Close prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxExpect.Close.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxExpect.Close.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxExpect.Close.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxExpect.Close.Builder>
         implements MysqlxExpect.CloseOrBuilder {
         public static final Descriptor getDescriptor() {
            return MysqlxExpect.internal_static_Mysqlx_Expect_Close_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxExpect.internal_static_Mysqlx_Expect_Close_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxExpect.Close.class, MysqlxExpect.Close.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxExpect.Close.alwaysUseFieldBuilders) {
            }
         }

         private static MysqlxExpect.Close.Builder create() {
            return new MysqlxExpect.Close.Builder();
         }

         public MysqlxExpect.Close.Builder clear() {
            super.clear();
            return this;
         }

         public MysqlxExpect.Close.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxExpect.internal_static_Mysqlx_Expect_Close_descriptor;
         }

         public MysqlxExpect.Close getDefaultInstanceForType() {
            return MysqlxExpect.Close.getDefaultInstance();
         }

         public MysqlxExpect.Close build() {
            MysqlxExpect.Close result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxExpect.Close buildPartial() {
            MysqlxExpect.Close result = new MysqlxExpect.Close(this);
            this.onBuilt();
            return result;
         }

         public MysqlxExpect.Close.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxExpect.Close) {
               return this.mergeFrom((MysqlxExpect.Close)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxExpect.Close.Builder mergeFrom(MysqlxExpect.Close other) {
            if (other == MysqlxExpect.Close.getDefaultInstance()) {
               return this;
            } else {
               this.mergeUnknownFields(other.getUnknownFields());
               return this;
            }
         }

         public final boolean isInitialized() {
            return true;
         }

         public MysqlxExpect.Close.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxExpect.Close parsedMessage = null;

            try {
               parsedMessage = (MysqlxExpect.Close)MysqlxExpect.Close.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxExpect.Close)var8.getUnfinishedMessage();
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

   public static final class Open extends GeneratedMessage implements MysqlxExpect.OpenOrBuilder {
      private static final MysqlxExpect.Open defaultInstance = new MysqlxExpect.Open(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxExpect.Open> PARSER = new AbstractParser<MysqlxExpect.Open>() {
         public MysqlxExpect.Open parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxExpect.Open(input, extensionRegistry);
         }
      };
      private int bitField0_;
      public static final int OP_FIELD_NUMBER = 1;
      private MysqlxExpect.Open.CtxOperation op_;
      public static final int COND_FIELD_NUMBER = 2;
      private List<MysqlxExpect.Open.Condition> cond_;
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private Open(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private Open(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static MysqlxExpect.Open getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxExpect.Open getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private Open(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                     MysqlxExpect.Open.CtxOperation value = MysqlxExpect.Open.CtxOperation.valueOf(rawValue);
                     if (value == null) {
                        unknownFields.mergeVarintField(1, rawValue);
                     } else {
                        this.bitField0_ |= 1;
                        this.op_ = value;
                     }
                     break;
                  case 18:
                     if ((mutable_bitField0_ & 2) != 2) {
                        this.cond_ = new ArrayList<>();
                        mutable_bitField0_ |= 2;
                     }

                     this.cond_.add(input.readMessage(MysqlxExpect.Open.Condition.PARSER, extensionRegistry));
                     break;
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
            if ((mutable_bitField0_ & 2) == 2) {
               this.cond_ = Collections.unmodifiableList(this.cond_);
            }

            this.unknownFields = unknownFields.build();
            this.makeExtensionsImmutable();
         }
      }

      public static final Descriptor getDescriptor() {
         return MysqlxExpect.internal_static_Mysqlx_Expect_Open_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxExpect.internal_static_Mysqlx_Expect_Open_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxExpect.Open.class, MysqlxExpect.Open.Builder.class);
      }

      public Parser<MysqlxExpect.Open> getParserForType() {
         return PARSER;
      }

      @Override
      public boolean hasOp() {
         return (this.bitField0_ & 1) == 1;
      }

      @Override
      public MysqlxExpect.Open.CtxOperation getOp() {
         return this.op_;
      }

      @Override
      public List<MysqlxExpect.Open.Condition> getCondList() {
         return this.cond_;
      }

      @Override
      public List<? extends MysqlxExpect.Open.ConditionOrBuilder> getCondOrBuilderList() {
         return this.cond_;
      }

      @Override
      public int getCondCount() {
         return this.cond_.size();
      }

      @Override
      public MysqlxExpect.Open.Condition getCond(int index) {
         return this.cond_.get(index);
      }

      @Override
      public MysqlxExpect.Open.ConditionOrBuilder getCondOrBuilder(int index) {
         return this.cond_.get(index);
      }

      private void initFields() {
         this.op_ = MysqlxExpect.Open.CtxOperation.EXPECT_CTX_COPY_PREV;
         this.cond_ = Collections.emptyList();
      }

      public final boolean isInitialized() {
         byte isInitialized = this.memoizedIsInitialized;
         if (isInitialized == 1) {
            return true;
         } else if (isInitialized == 0) {
            return false;
         } else {
            for(int i = 0; i < this.getCondCount(); ++i) {
               if (!this.getCond(i).isInitialized()) {
                  this.memoizedIsInitialized = 0;
                  return false;
               }
            }

            this.memoizedIsInitialized = 1;
            return true;
         }
      }

      public void writeTo(CodedOutputStream output) throws IOException {
         this.getSerializedSize();
         if ((this.bitField0_ & 1) == 1) {
            output.writeEnum(1, this.op_.getNumber());
         }

         for(int i = 0; i < this.cond_.size(); ++i) {
            output.writeMessage(2, (MessageLite)this.cond_.get(i));
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
               size += CodedOutputStream.computeEnumSize(1, this.op_.getNumber());
            }

            for(int i = 0; i < this.cond_.size(); ++i) {
               size += CodedOutputStream.computeMessageSize(2, (MessageLite)this.cond_.get(i));
            }

            size += this.getUnknownFields().getSerializedSize();
            this.memoizedSerializedSize = size;
            return size;
         }
      }

      protected Object writeReplace() throws ObjectStreamException {
         return super.writeReplace();
      }

      public static MysqlxExpect.Open parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxExpect.Open)PARSER.parseFrom(data);
      }

      public static MysqlxExpect.Open parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxExpect.Open)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxExpect.Open parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxExpect.Open)PARSER.parseFrom(data);
      }

      public static MysqlxExpect.Open parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxExpect.Open)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxExpect.Open parseFrom(InputStream input) throws IOException {
         return (MysqlxExpect.Open)PARSER.parseFrom(input);
      }

      public static MysqlxExpect.Open parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxExpect.Open)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxExpect.Open parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxExpect.Open)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxExpect.Open parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxExpect.Open)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxExpect.Open parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxExpect.Open)PARSER.parseFrom(input);
      }

      public static MysqlxExpect.Open parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxExpect.Open)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxExpect.Open.Builder newBuilder() {
         return MysqlxExpect.Open.Builder.create();
      }

      public MysqlxExpect.Open.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxExpect.Open.Builder newBuilder(MysqlxExpect.Open prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxExpect.Open.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxExpect.Open.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxExpect.Open.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<MysqlxExpect.Open.Builder> implements MysqlxExpect.OpenOrBuilder {
         private int bitField0_;
         private MysqlxExpect.Open.CtxOperation op_ = MysqlxExpect.Open.CtxOperation.EXPECT_CTX_COPY_PREV;
         private List<MysqlxExpect.Open.Condition> cond_ = Collections.emptyList();
         private RepeatedFieldBuilder<MysqlxExpect.Open.Condition, MysqlxExpect.Open.Condition.Builder, MysqlxExpect.Open.ConditionOrBuilder> condBuilder_;

         public static final Descriptor getDescriptor() {
            return MysqlxExpect.internal_static_Mysqlx_Expect_Open_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxExpect.internal_static_Mysqlx_Expect_Open_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxExpect.Open.class, MysqlxExpect.Open.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxExpect.Open.alwaysUseFieldBuilders) {
               this.getCondFieldBuilder();
            }
         }

         private static MysqlxExpect.Open.Builder create() {
            return new MysqlxExpect.Open.Builder();
         }

         public MysqlxExpect.Open.Builder clear() {
            super.clear();
            this.op_ = MysqlxExpect.Open.CtxOperation.EXPECT_CTX_COPY_PREV;
            this.bitField0_ &= -2;
            if (this.condBuilder_ == null) {
               this.cond_ = Collections.emptyList();
               this.bitField0_ &= -3;
            } else {
               this.condBuilder_.clear();
            }

            return this;
         }

         public MysqlxExpect.Open.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxExpect.internal_static_Mysqlx_Expect_Open_descriptor;
         }

         public MysqlxExpect.Open getDefaultInstanceForType() {
            return MysqlxExpect.Open.getDefaultInstance();
         }

         public MysqlxExpect.Open build() {
            MysqlxExpect.Open result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxExpect.Open buildPartial() {
            MysqlxExpect.Open result = new MysqlxExpect.Open(this);
            int from_bitField0_ = this.bitField0_;
            int to_bitField0_ = 0;
            if ((from_bitField0_ & 1) == 1) {
               to_bitField0_ |= 1;
            }

            result.op_ = this.op_;
            if (this.condBuilder_ == null) {
               if ((this.bitField0_ & 2) == 2) {
                  this.cond_ = Collections.unmodifiableList(this.cond_);
                  this.bitField0_ &= -3;
               }

               result.cond_ = this.cond_;
            } else {
               result.cond_ = this.condBuilder_.build();
            }

            result.bitField0_ = to_bitField0_;
            this.onBuilt();
            return result;
         }

         public MysqlxExpect.Open.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxExpect.Open) {
               return this.mergeFrom((MysqlxExpect.Open)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxExpect.Open.Builder mergeFrom(MysqlxExpect.Open other) {
            if (other == MysqlxExpect.Open.getDefaultInstance()) {
               return this;
            } else {
               if (other.hasOp()) {
                  this.setOp(other.getOp());
               }

               if (this.condBuilder_ == null) {
                  if (!other.cond_.isEmpty()) {
                     if (this.cond_.isEmpty()) {
                        this.cond_ = other.cond_;
                        this.bitField0_ &= -3;
                     } else {
                        this.ensureCondIsMutable();
                        this.cond_.addAll(other.cond_);
                     }

                     this.onChanged();
                  }
               } else if (!other.cond_.isEmpty()) {
                  if (this.condBuilder_.isEmpty()) {
                     this.condBuilder_.dispose();
                     this.condBuilder_ = null;
                     this.cond_ = other.cond_;
                     this.bitField0_ &= -3;
                     this.condBuilder_ = MysqlxExpect.Open.alwaysUseFieldBuilders ? this.getCondFieldBuilder() : null;
                  } else {
                     this.condBuilder_.addAllMessages(other.cond_);
                  }
               }

               this.mergeUnknownFields(other.getUnknownFields());
               return this;
            }
         }

         public final boolean isInitialized() {
            for(int i = 0; i < this.getCondCount(); ++i) {
               if (!this.getCond(i).isInitialized()) {
                  return false;
               }
            }

            return true;
         }

         public MysqlxExpect.Open.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxExpect.Open parsedMessage = null;

            try {
               parsedMessage = (MysqlxExpect.Open)MysqlxExpect.Open.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxExpect.Open)var8.getUnfinishedMessage();
               throw var8;
            } finally {
               if (parsedMessage != null) {
                  this.mergeFrom(parsedMessage);
               }
            }

            return this;
         }

         @Override
         public boolean hasOp() {
            return (this.bitField0_ & 1) == 1;
         }

         @Override
         public MysqlxExpect.Open.CtxOperation getOp() {
            return this.op_;
         }

         public MysqlxExpect.Open.Builder setOp(MysqlxExpect.Open.CtxOperation value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 1;
               this.op_ = value;
               this.onChanged();
               return this;
            }
         }

         public MysqlxExpect.Open.Builder clearOp() {
            this.bitField0_ &= -2;
            this.op_ = MysqlxExpect.Open.CtxOperation.EXPECT_CTX_COPY_PREV;
            this.onChanged();
            return this;
         }

         private void ensureCondIsMutable() {
            if ((this.bitField0_ & 2) != 2) {
               this.cond_ = new ArrayList<>(this.cond_);
               this.bitField0_ |= 2;
            }
         }

         @Override
         public List<MysqlxExpect.Open.Condition> getCondList() {
            return this.condBuilder_ == null ? Collections.unmodifiableList(this.cond_) : this.condBuilder_.getMessageList();
         }

         @Override
         public int getCondCount() {
            return this.condBuilder_ == null ? this.cond_.size() : this.condBuilder_.getCount();
         }

         @Override
         public MysqlxExpect.Open.Condition getCond(int index) {
            return this.condBuilder_ == null ? this.cond_.get(index) : (MysqlxExpect.Open.Condition)this.condBuilder_.getMessage(index);
         }

         public MysqlxExpect.Open.Builder setCond(int index, MysqlxExpect.Open.Condition value) {
            if (this.condBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureCondIsMutable();
               this.cond_.set(index, value);
               this.onChanged();
            } else {
               this.condBuilder_.setMessage(index, value);
            }

            return this;
         }

         public MysqlxExpect.Open.Builder setCond(int index, MysqlxExpect.Open.Condition.Builder builderForValue) {
            if (this.condBuilder_ == null) {
               this.ensureCondIsMutable();
               this.cond_.set(index, builderForValue.build());
               this.onChanged();
            } else {
               this.condBuilder_.setMessage(index, builderForValue.build());
            }

            return this;
         }

         public MysqlxExpect.Open.Builder addCond(MysqlxExpect.Open.Condition value) {
            if (this.condBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureCondIsMutable();
               this.cond_.add(value);
               this.onChanged();
            } else {
               this.condBuilder_.addMessage(value);
            }

            return this;
         }

         public MysqlxExpect.Open.Builder addCond(int index, MysqlxExpect.Open.Condition value) {
            if (this.condBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureCondIsMutable();
               this.cond_.add(index, value);
               this.onChanged();
            } else {
               this.condBuilder_.addMessage(index, value);
            }

            return this;
         }

         public MysqlxExpect.Open.Builder addCond(MysqlxExpect.Open.Condition.Builder builderForValue) {
            if (this.condBuilder_ == null) {
               this.ensureCondIsMutable();
               this.cond_.add(builderForValue.build());
               this.onChanged();
            } else {
               this.condBuilder_.addMessage(builderForValue.build());
            }

            return this;
         }

         public MysqlxExpect.Open.Builder addCond(int index, MysqlxExpect.Open.Condition.Builder builderForValue) {
            if (this.condBuilder_ == null) {
               this.ensureCondIsMutable();
               this.cond_.add(index, builderForValue.build());
               this.onChanged();
            } else {
               this.condBuilder_.addMessage(index, builderForValue.build());
            }

            return this;
         }

         public MysqlxExpect.Open.Builder addAllCond(Iterable<? extends MysqlxExpect.Open.Condition> values) {
            if (this.condBuilder_ == null) {
               this.ensureCondIsMutable();
               com.google.protobuf.AbstractMessageLite.Builder.addAll(values, this.cond_);
               this.onChanged();
            } else {
               this.condBuilder_.addAllMessages(values);
            }

            return this;
         }

         public MysqlxExpect.Open.Builder clearCond() {
            if (this.condBuilder_ == null) {
               this.cond_ = Collections.emptyList();
               this.bitField0_ &= -3;
               this.onChanged();
            } else {
               this.condBuilder_.clear();
            }

            return this;
         }

         public MysqlxExpect.Open.Builder removeCond(int index) {
            if (this.condBuilder_ == null) {
               this.ensureCondIsMutable();
               this.cond_.remove(index);
               this.onChanged();
            } else {
               this.condBuilder_.remove(index);
            }

            return this;
         }

         public MysqlxExpect.Open.Condition.Builder getCondBuilder(int index) {
            return (MysqlxExpect.Open.Condition.Builder)this.getCondFieldBuilder().getBuilder(index);
         }

         @Override
         public MysqlxExpect.Open.ConditionOrBuilder getCondOrBuilder(int index) {
            return this.condBuilder_ == null ? this.cond_.get(index) : (MysqlxExpect.Open.ConditionOrBuilder)this.condBuilder_.getMessageOrBuilder(index);
         }

         @Override
         public List<? extends MysqlxExpect.Open.ConditionOrBuilder> getCondOrBuilderList() {
            return this.condBuilder_ != null ? this.condBuilder_.getMessageOrBuilderList() : Collections.unmodifiableList(this.cond_);
         }

         public MysqlxExpect.Open.Condition.Builder addCondBuilder() {
            return (MysqlxExpect.Open.Condition.Builder)this.getCondFieldBuilder().addBuilder(MysqlxExpect.Open.Condition.getDefaultInstance());
         }

         public MysqlxExpect.Open.Condition.Builder addCondBuilder(int index) {
            return (MysqlxExpect.Open.Condition.Builder)this.getCondFieldBuilder().addBuilder(index, MysqlxExpect.Open.Condition.getDefaultInstance());
         }

         public List<MysqlxExpect.Open.Condition.Builder> getCondBuilderList() {
            return this.getCondFieldBuilder().getBuilderList();
         }

         private RepeatedFieldBuilder<MysqlxExpect.Open.Condition, MysqlxExpect.Open.Condition.Builder, MysqlxExpect.Open.ConditionOrBuilder> getCondFieldBuilder() {
            if (this.condBuilder_ == null) {
               this.condBuilder_ = new RepeatedFieldBuilder(this.cond_, (this.bitField0_ & 2) == 2, this.getParentForChildren(), this.isClean());
               this.cond_ = null;
            }

            return this.condBuilder_;
         }
      }

      public static final class Condition extends GeneratedMessage implements MysqlxExpect.Open.ConditionOrBuilder {
         private static final MysqlxExpect.Open.Condition defaultInstance = new MysqlxExpect.Open.Condition(true);
         private final UnknownFieldSet unknownFields;
         public static Parser<MysqlxExpect.Open.Condition> PARSER = new AbstractParser<MysqlxExpect.Open.Condition>() {
            public MysqlxExpect.Open.Condition parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
               return new MysqlxExpect.Open.Condition(input, extensionRegistry);
            }
         };
         private int bitField0_;
         public static final int CONDITION_KEY_FIELD_NUMBER = 1;
         private int conditionKey_;
         public static final int CONDITION_VALUE_FIELD_NUMBER = 2;
         private ByteString conditionValue_;
         public static final int OP_FIELD_NUMBER = 3;
         private MysqlxExpect.Open.Condition.ConditionOperation op_;
         private byte memoizedIsInitialized = -1;
         private int memoizedSerializedSize = -1;
         private static final long serialVersionUID = 0L;

         private Condition(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
            super(builder);
            this.unknownFields = builder.getUnknownFields();
         }

         private Condition(boolean noInit) {
            this.unknownFields = UnknownFieldSet.getDefaultInstance();
         }

         public static MysqlxExpect.Open.Condition getDefaultInstance() {
            return defaultInstance;
         }

         public MysqlxExpect.Open.Condition getDefaultInstanceForType() {
            return defaultInstance;
         }

         public final UnknownFieldSet getUnknownFields() {
            return this.unknownFields;
         }

         private Condition(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                        this.bitField0_ |= 1;
                        this.conditionKey_ = input.readUInt32();
                        break;
                     case 18:
                        this.bitField0_ |= 2;
                        this.conditionValue_ = input.readBytes();
                        break;
                     case 24:
                        int rawValue = input.readEnum();
                        MysqlxExpect.Open.Condition.ConditionOperation value = MysqlxExpect.Open.Condition.ConditionOperation.valueOf(rawValue);
                        if (value == null) {
                           unknownFields.mergeVarintField(3, rawValue);
                        } else {
                           this.bitField0_ |= 4;
                           this.op_ = value;
                        }
                        break;
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
            return MysqlxExpect.internal_static_Mysqlx_Expect_Open_Condition_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxExpect.internal_static_Mysqlx_Expect_Open_Condition_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxExpect.Open.Condition.class, MysqlxExpect.Open.Condition.Builder.class);
         }

         public Parser<MysqlxExpect.Open.Condition> getParserForType() {
            return PARSER;
         }

         @Override
         public boolean hasConditionKey() {
            return (this.bitField0_ & 1) == 1;
         }

         @Override
         public int getConditionKey() {
            return this.conditionKey_;
         }

         @Override
         public boolean hasConditionValue() {
            return (this.bitField0_ & 2) == 2;
         }

         @Override
         public ByteString getConditionValue() {
            return this.conditionValue_;
         }

         @Override
         public boolean hasOp() {
            return (this.bitField0_ & 4) == 4;
         }

         @Override
         public MysqlxExpect.Open.Condition.ConditionOperation getOp() {
            return this.op_;
         }

         private void initFields() {
            this.conditionKey_ = 0;
            this.conditionValue_ = ByteString.EMPTY;
            this.op_ = MysqlxExpect.Open.Condition.ConditionOperation.EXPECT_OP_SET;
         }

         public final boolean isInitialized() {
            byte isInitialized = this.memoizedIsInitialized;
            if (isInitialized == 1) {
               return true;
            } else if (isInitialized == 0) {
               return false;
            } else if (!this.hasConditionKey()) {
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
               output.writeUInt32(1, this.conditionKey_);
            }

            if ((this.bitField0_ & 2) == 2) {
               output.writeBytes(2, this.conditionValue_);
            }

            if ((this.bitField0_ & 4) == 4) {
               output.writeEnum(3, this.op_.getNumber());
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
                  size += CodedOutputStream.computeUInt32Size(1, this.conditionKey_);
               }

               if ((this.bitField0_ & 2) == 2) {
                  size += CodedOutputStream.computeBytesSize(2, this.conditionValue_);
               }

               if ((this.bitField0_ & 4) == 4) {
                  size += CodedOutputStream.computeEnumSize(3, this.op_.getNumber());
               }

               size += this.getUnknownFields().getSerializedSize();
               this.memoizedSerializedSize = size;
               return size;
            }
         }

         protected Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
         }

         public static MysqlxExpect.Open.Condition parseFrom(ByteString data) throws InvalidProtocolBufferException {
            return (MysqlxExpect.Open.Condition)PARSER.parseFrom(data);
         }

         public static MysqlxExpect.Open.Condition parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (MysqlxExpect.Open.Condition)PARSER.parseFrom(data, extensionRegistry);
         }

         public static MysqlxExpect.Open.Condition parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return (MysqlxExpect.Open.Condition)PARSER.parseFrom(data);
         }

         public static MysqlxExpect.Open.Condition parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (MysqlxExpect.Open.Condition)PARSER.parseFrom(data, extensionRegistry);
         }

         public static MysqlxExpect.Open.Condition parseFrom(InputStream input) throws IOException {
            return (MysqlxExpect.Open.Condition)PARSER.parseFrom(input);
         }

         public static MysqlxExpect.Open.Condition parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (MysqlxExpect.Open.Condition)PARSER.parseFrom(input, extensionRegistry);
         }

         public static MysqlxExpect.Open.Condition parseDelimitedFrom(InputStream input) throws IOException {
            return (MysqlxExpect.Open.Condition)PARSER.parseDelimitedFrom(input);
         }

         public static MysqlxExpect.Open.Condition parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (MysqlxExpect.Open.Condition)PARSER.parseDelimitedFrom(input, extensionRegistry);
         }

         public static MysqlxExpect.Open.Condition parseFrom(CodedInputStream input) throws IOException {
            return (MysqlxExpect.Open.Condition)PARSER.parseFrom(input);
         }

         public static MysqlxExpect.Open.Condition parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (MysqlxExpect.Open.Condition)PARSER.parseFrom(input, extensionRegistry);
         }

         public static MysqlxExpect.Open.Condition.Builder newBuilder() {
            return MysqlxExpect.Open.Condition.Builder.create();
         }

         public MysqlxExpect.Open.Condition.Builder newBuilderForType() {
            return newBuilder();
         }

         public static MysqlxExpect.Open.Condition.Builder newBuilder(MysqlxExpect.Open.Condition prototype) {
            return newBuilder().mergeFrom(prototype);
         }

         public MysqlxExpect.Open.Condition.Builder toBuilder() {
            return newBuilder(this);
         }

         protected MysqlxExpect.Open.Condition.Builder newBuilderForType(BuilderParent parent) {
            return new MysqlxExpect.Open.Condition.Builder(parent);
         }

         static {
            defaultInstance.initFields();
         }

         public static final class Builder
            extends com.google.protobuf.GeneratedMessage.Builder<MysqlxExpect.Open.Condition.Builder>
            implements MysqlxExpect.Open.ConditionOrBuilder {
            private int bitField0_;
            private int conditionKey_;
            private ByteString conditionValue_ = ByteString.EMPTY;
            private MysqlxExpect.Open.Condition.ConditionOperation op_ = MysqlxExpect.Open.Condition.ConditionOperation.EXPECT_OP_SET;

            public static final Descriptor getDescriptor() {
               return MysqlxExpect.internal_static_Mysqlx_Expect_Open_Condition_descriptor;
            }

            protected FieldAccessorTable internalGetFieldAccessorTable() {
               return MysqlxExpect.internal_static_Mysqlx_Expect_Open_Condition_fieldAccessorTable
                  .ensureFieldAccessorsInitialized(MysqlxExpect.Open.Condition.class, MysqlxExpect.Open.Condition.Builder.class);
            }

            private Builder() {
               this.maybeForceBuilderInitialization();
            }

            private Builder(BuilderParent parent) {
               super(parent);
               this.maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
               if (MysqlxExpect.Open.Condition.alwaysUseFieldBuilders) {
               }
            }

            private static MysqlxExpect.Open.Condition.Builder create() {
               return new MysqlxExpect.Open.Condition.Builder();
            }

            public MysqlxExpect.Open.Condition.Builder clear() {
               super.clear();
               this.conditionKey_ = 0;
               this.bitField0_ &= -2;
               this.conditionValue_ = ByteString.EMPTY;
               this.bitField0_ &= -3;
               this.op_ = MysqlxExpect.Open.Condition.ConditionOperation.EXPECT_OP_SET;
               this.bitField0_ &= -5;
               return this;
            }

            public MysqlxExpect.Open.Condition.Builder clone() {
               return create().mergeFrom(this.buildPartial());
            }

            public Descriptor getDescriptorForType() {
               return MysqlxExpect.internal_static_Mysqlx_Expect_Open_Condition_descriptor;
            }

            public MysqlxExpect.Open.Condition getDefaultInstanceForType() {
               return MysqlxExpect.Open.Condition.getDefaultInstance();
            }

            public MysqlxExpect.Open.Condition build() {
               MysqlxExpect.Open.Condition result = this.buildPartial();
               if (!result.isInitialized()) {
                  throw newUninitializedMessageException(result);
               } else {
                  return result;
               }
            }

            public MysqlxExpect.Open.Condition buildPartial() {
               MysqlxExpect.Open.Condition result = new MysqlxExpect.Open.Condition(this);
               int from_bitField0_ = this.bitField0_;
               int to_bitField0_ = 0;
               if ((from_bitField0_ & 1) == 1) {
                  to_bitField0_ |= 1;
               }

               result.conditionKey_ = this.conditionKey_;
               if ((from_bitField0_ & 2) == 2) {
                  to_bitField0_ |= 2;
               }

               result.conditionValue_ = this.conditionValue_;
               if ((from_bitField0_ & 4) == 4) {
                  to_bitField0_ |= 4;
               }

               result.op_ = this.op_;
               result.bitField0_ = to_bitField0_;
               this.onBuilt();
               return result;
            }

            public MysqlxExpect.Open.Condition.Builder mergeFrom(Message other) {
               if (other instanceof MysqlxExpect.Open.Condition) {
                  return this.mergeFrom((MysqlxExpect.Open.Condition)other);
               } else {
                  super.mergeFrom(other);
                  return this;
               }
            }

            public MysqlxExpect.Open.Condition.Builder mergeFrom(MysqlxExpect.Open.Condition other) {
               if (other == MysqlxExpect.Open.Condition.getDefaultInstance()) {
                  return this;
               } else {
                  if (other.hasConditionKey()) {
                     this.setConditionKey(other.getConditionKey());
                  }

                  if (other.hasConditionValue()) {
                     this.setConditionValue(other.getConditionValue());
                  }

                  if (other.hasOp()) {
                     this.setOp(other.getOp());
                  }

                  this.mergeUnknownFields(other.getUnknownFields());
                  return this;
               }
            }

            public final boolean isInitialized() {
               return this.hasConditionKey();
            }

            public MysqlxExpect.Open.Condition.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
               MysqlxExpect.Open.Condition parsedMessage = null;

               try {
                  parsedMessage = (MysqlxExpect.Open.Condition)MysqlxExpect.Open.Condition.PARSER.parsePartialFrom(input, extensionRegistry);
               } catch (InvalidProtocolBufferException var8) {
                  parsedMessage = (MysqlxExpect.Open.Condition)var8.getUnfinishedMessage();
                  throw var8;
               } finally {
                  if (parsedMessage != null) {
                     this.mergeFrom(parsedMessage);
                  }
               }

               return this;
            }

            @Override
            public boolean hasConditionKey() {
               return (this.bitField0_ & 1) == 1;
            }

            @Override
            public int getConditionKey() {
               return this.conditionKey_;
            }

            public MysqlxExpect.Open.Condition.Builder setConditionKey(int value) {
               this.bitField0_ |= 1;
               this.conditionKey_ = value;
               this.onChanged();
               return this;
            }

            public MysqlxExpect.Open.Condition.Builder clearConditionKey() {
               this.bitField0_ &= -2;
               this.conditionKey_ = 0;
               this.onChanged();
               return this;
            }

            @Override
            public boolean hasConditionValue() {
               return (this.bitField0_ & 2) == 2;
            }

            @Override
            public ByteString getConditionValue() {
               return this.conditionValue_;
            }

            public MysqlxExpect.Open.Condition.Builder setConditionValue(ByteString value) {
               if (value == null) {
                  throw new NullPointerException();
               } else {
                  this.bitField0_ |= 2;
                  this.conditionValue_ = value;
                  this.onChanged();
                  return this;
               }
            }

            public MysqlxExpect.Open.Condition.Builder clearConditionValue() {
               this.bitField0_ &= -3;
               this.conditionValue_ = MysqlxExpect.Open.Condition.getDefaultInstance().getConditionValue();
               this.onChanged();
               return this;
            }

            @Override
            public boolean hasOp() {
               return (this.bitField0_ & 4) == 4;
            }

            @Override
            public MysqlxExpect.Open.Condition.ConditionOperation getOp() {
               return this.op_;
            }

            public MysqlxExpect.Open.Condition.Builder setOp(MysqlxExpect.Open.Condition.ConditionOperation value) {
               if (value == null) {
                  throw new NullPointerException();
               } else {
                  this.bitField0_ |= 4;
                  this.op_ = value;
                  this.onChanged();
                  return this;
               }
            }

            public MysqlxExpect.Open.Condition.Builder clearOp() {
               this.bitField0_ &= -5;
               this.op_ = MysqlxExpect.Open.Condition.ConditionOperation.EXPECT_OP_SET;
               this.onChanged();
               return this;
            }
         }

         public static enum ConditionOperation implements ProtocolMessageEnum {
            EXPECT_OP_SET(0, 0),
            EXPECT_OP_UNSET(1, 1);

            public static final int EXPECT_OP_SET_VALUE = 0;
            public static final int EXPECT_OP_UNSET_VALUE = 1;
            private static EnumLiteMap<MysqlxExpect.Open.Condition.ConditionOperation> internalValueMap = new EnumLiteMap<MysqlxExpect.Open.Condition.ConditionOperation>(
               
            ) {
               public MysqlxExpect.Open.Condition.ConditionOperation findValueByNumber(int number) {
                  return MysqlxExpect.Open.Condition.ConditionOperation.valueOf(number);
               }
            };
            private static final MysqlxExpect.Open.Condition.ConditionOperation[] VALUES = values();
            private final int index;
            private final int value;

            public final int getNumber() {
               return this.value;
            }

            public static MysqlxExpect.Open.Condition.ConditionOperation valueOf(int value) {
               switch(value) {
                  case 0:
                     return EXPECT_OP_SET;
                  case 1:
                     return EXPECT_OP_UNSET;
                  default:
                     return null;
               }
            }

            public static EnumLiteMap<MysqlxExpect.Open.Condition.ConditionOperation> internalGetValueMap() {
               return internalValueMap;
            }

            public final EnumValueDescriptor getValueDescriptor() {
               return (EnumValueDescriptor)getDescriptor().getValues().get(this.index);
            }

            public final EnumDescriptor getDescriptorForType() {
               return getDescriptor();
            }

            public static final EnumDescriptor getDescriptor() {
               return (EnumDescriptor)MysqlxExpect.Open.Condition.getDescriptor().getEnumTypes().get(1);
            }

            public static MysqlxExpect.Open.Condition.ConditionOperation valueOf(EnumValueDescriptor desc) {
               if (desc.getType() != getDescriptor()) {
                  throw new IllegalArgumentException("EnumValueDescriptor is not for this type.");
               } else {
                  return VALUES[desc.getIndex()];
               }
            }

            private ConditionOperation(int index, int value) {
               this.index = index;
               this.value = value;
            }
         }

         public static enum Key implements ProtocolMessageEnum {
            EXPECT_NO_ERROR(0, 1),
            EXPECT_FIELD_EXIST(1, 2),
            EXPECT_DOCID_GENERATED(2, 3);

            public static final int EXPECT_NO_ERROR_VALUE = 1;
            public static final int EXPECT_FIELD_EXIST_VALUE = 2;
            public static final int EXPECT_DOCID_GENERATED_VALUE = 3;
            private static EnumLiteMap<MysqlxExpect.Open.Condition.Key> internalValueMap = new EnumLiteMap<MysqlxExpect.Open.Condition.Key>() {
               public MysqlxExpect.Open.Condition.Key findValueByNumber(int number) {
                  return MysqlxExpect.Open.Condition.Key.valueOf(number);
               }
            };
            private static final MysqlxExpect.Open.Condition.Key[] VALUES = values();
            private final int index;
            private final int value;

            public final int getNumber() {
               return this.value;
            }

            public static MysqlxExpect.Open.Condition.Key valueOf(int value) {
               switch(value) {
                  case 1:
                     return EXPECT_NO_ERROR;
                  case 2:
                     return EXPECT_FIELD_EXIST;
                  case 3:
                     return EXPECT_DOCID_GENERATED;
                  default:
                     return null;
               }
            }

            public static EnumLiteMap<MysqlxExpect.Open.Condition.Key> internalGetValueMap() {
               return internalValueMap;
            }

            public final EnumValueDescriptor getValueDescriptor() {
               return (EnumValueDescriptor)getDescriptor().getValues().get(this.index);
            }

            public final EnumDescriptor getDescriptorForType() {
               return getDescriptor();
            }

            public static final EnumDescriptor getDescriptor() {
               return (EnumDescriptor)MysqlxExpect.Open.Condition.getDescriptor().getEnumTypes().get(0);
            }

            public static MysqlxExpect.Open.Condition.Key valueOf(EnumValueDescriptor desc) {
               if (desc.getType() != getDescriptor()) {
                  throw new IllegalArgumentException("EnumValueDescriptor is not for this type.");
               } else {
                  return VALUES[desc.getIndex()];
               }
            }

            private Key(int index, int value) {
               this.index = index;
               this.value = value;
            }
         }
      }

      public interface ConditionOrBuilder extends MessageOrBuilder {
         boolean hasConditionKey();

         int getConditionKey();

         boolean hasConditionValue();

         ByteString getConditionValue();

         boolean hasOp();

         MysqlxExpect.Open.Condition.ConditionOperation getOp();
      }

      public static enum CtxOperation implements ProtocolMessageEnum {
         EXPECT_CTX_COPY_PREV(0, 0),
         EXPECT_CTX_EMPTY(1, 1);

         public static final int EXPECT_CTX_COPY_PREV_VALUE = 0;
         public static final int EXPECT_CTX_EMPTY_VALUE = 1;
         private static EnumLiteMap<MysqlxExpect.Open.CtxOperation> internalValueMap = new EnumLiteMap<MysqlxExpect.Open.CtxOperation>() {
            public MysqlxExpect.Open.CtxOperation findValueByNumber(int number) {
               return MysqlxExpect.Open.CtxOperation.valueOf(number);
            }
         };
         private static final MysqlxExpect.Open.CtxOperation[] VALUES = values();
         private final int index;
         private final int value;

         public final int getNumber() {
            return this.value;
         }

         public static MysqlxExpect.Open.CtxOperation valueOf(int value) {
            switch(value) {
               case 0:
                  return EXPECT_CTX_COPY_PREV;
               case 1:
                  return EXPECT_CTX_EMPTY;
               default:
                  return null;
            }
         }

         public static EnumLiteMap<MysqlxExpect.Open.CtxOperation> internalGetValueMap() {
            return internalValueMap;
         }

         public final EnumValueDescriptor getValueDescriptor() {
            return (EnumValueDescriptor)getDescriptor().getValues().get(this.index);
         }

         public final EnumDescriptor getDescriptorForType() {
            return getDescriptor();
         }

         public static final EnumDescriptor getDescriptor() {
            return (EnumDescriptor)MysqlxExpect.Open.getDescriptor().getEnumTypes().get(0);
         }

         public static MysqlxExpect.Open.CtxOperation valueOf(EnumValueDescriptor desc) {
            if (desc.getType() != getDescriptor()) {
               throw new IllegalArgumentException("EnumValueDescriptor is not for this type.");
            } else {
               return VALUES[desc.getIndex()];
            }
         }

         private CtxOperation(int index, int value) {
            this.index = index;
            this.value = value;
         }
      }
   }

   public interface OpenOrBuilder extends MessageOrBuilder {
      boolean hasOp();

      MysqlxExpect.Open.CtxOperation getOp();

      List<MysqlxExpect.Open.Condition> getCondList();

      MysqlxExpect.Open.Condition getCond(int var1);

      int getCondCount();

      List<? extends MysqlxExpect.Open.ConditionOrBuilder> getCondOrBuilderList();

      MysqlxExpect.Open.ConditionOrBuilder getCondOrBuilder(int var1);
   }
}
