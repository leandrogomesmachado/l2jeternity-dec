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
import com.google.protobuf.ProtocolMessageEnum;
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

public final class MysqlxResultset {
   private static final Descriptor internal_static_Mysqlx_Resultset_FetchDoneMoreOutParams_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(0);
   private static FieldAccessorTable internal_static_Mysqlx_Resultset_FetchDoneMoreOutParams_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Resultset_FetchDoneMoreOutParams_descriptor, new String[0]
   );
   private static final Descriptor internal_static_Mysqlx_Resultset_FetchDoneMoreResultsets_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(1);
   private static FieldAccessorTable internal_static_Mysqlx_Resultset_FetchDoneMoreResultsets_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Resultset_FetchDoneMoreResultsets_descriptor, new String[0]
   );
   private static final Descriptor internal_static_Mysqlx_Resultset_FetchDone_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(2);
   private static FieldAccessorTable internal_static_Mysqlx_Resultset_FetchDone_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Resultset_FetchDone_descriptor, new String[0]
   );
   private static final Descriptor internal_static_Mysqlx_Resultset_ColumnMetaData_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(3);
   private static FieldAccessorTable internal_static_Mysqlx_Resultset_ColumnMetaData_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Resultset_ColumnMetaData_descriptor,
      new String[]{
         "Type", "Name", "OriginalName", "Table", "OriginalTable", "Schema", "Catalog", "Collation", "FractionalDigits", "Length", "Flags", "ContentType"
      }
   );
   private static final Descriptor internal_static_Mysqlx_Resultset_Row_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(4);
   private static FieldAccessorTable internal_static_Mysqlx_Resultset_Row_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Resultset_Row_descriptor, new String[]{"Field"}
   );
   private static FileDescriptor descriptor;

   private MysqlxResultset() {
   }

   public static void registerAllExtensions(ExtensionRegistry registry) {
   }

   public static FileDescriptor getDescriptor() {
      return descriptor;
   }

   static {
      String[] descriptorData = new String[]{
         "\n\u0016mysqlx_resultset.proto\u0012\u0010Mysqlx.Resultset\u001a\fmysqlx.proto\"\u001e\n\u0016FetchDoneMoreOutParams:\u0004\u0090ê0\u0012\"\u001f\n\u0017FetchDoneMoreResultsets:\u0004\u0090ê0\u0010\"\u0011\n\tFetchDone:\u0004\u0090ê0\u000e\"¥\u0003\n\u000eColumnMetaData\u00128\n\u0004type\u0018\u0001 \u0002(\u000e2*.Mysqlx.Resultset.ColumnMetaData.FieldType\u0012\f\n\u0004name\u0018\u0002 \u0001(\f\u0012\u0015\n\roriginal_name\u0018\u0003 \u0001(\f\u0012\r\n\u0005table\u0018\u0004 \u0001(\f\u0012\u0016\n\u000eoriginal_table\u0018\u0005 \u0001(\f\u0012\u000e\n\u0006schema\u0018\u0006 \u0001(\f\u0012\u000f\n\u0007catalog\u0018\u0007 \u0001(\f\u0012\u0011\n\tcollation\u0018\b \u0001(\u0004\u0012\u0019\n\u0011fractional_digits\u0018\t \u0001(\r\u0012\u000e\n\u0006length\u0018\n \u0001(\r\u0012\r\n\u0005flags\u0018\u000b ",
         "\u0001(\r\u0012\u0014\n\fcontent_type\u0018\f \u0001(\r\"\u0082\u0001\n\tFieldType\u0012\b\n\u0004SINT\u0010\u0001\u0012\b\n\u0004UINT\u0010\u0002\u0012\n\n\u0006DOUBLE\u0010\u0005\u0012\t\n\u0005FLOAT\u0010\u0006\u0012\t\n\u0005BYTES\u0010\u0007\u0012\b\n\u0004TIME\u0010\n\u0012\f\n\bDATETIME\u0010\f\u0012\u0007\n\u0003SET\u0010\u000f\u0012\b\n\u0004ENUM\u0010\u0010\u0012\u0007\n\u0003BIT\u0010\u0011\u0012\u000b\n\u0007DECIMAL\u0010\u0012:\u0004\u0090ê0\f\"\u001a\n\u0003Row\u0012\r\n\u0005field\u0018\u0001 \u0003(\f:\u0004\u0090ê0\r*4\n\u0011ContentType_BYTES\u0012\f\n\bGEOMETRY\u0010\u0001\u0012\b\n\u0004JSON\u0010\u0002\u0012\u0007\n\u0003XML\u0010\u0003*.\n\u0014ContentType_DATETIME\u0012\b\n\u0004DATE\u0010\u0001\u0012\f\n\bDATETIME\u0010\u0002B\u0019\n\u0017com.mysql.cj.x.protobuf"
      };
      InternalDescriptorAssigner assigner = new InternalDescriptorAssigner() {
         public ExtensionRegistry assignDescriptors(FileDescriptor root) {
            MysqlxResultset.descriptor = root;
            return null;
         }
      };
      FileDescriptor.internalBuildGeneratedFileFrom(descriptorData, new FileDescriptor[]{Mysqlx.getDescriptor()}, assigner);
      ExtensionRegistry registry = ExtensionRegistry.newInstance();
      registry.add(Mysqlx.serverMessageId);
      registry.add(Mysqlx.serverMessageId);
      registry.add(Mysqlx.serverMessageId);
      registry.add(Mysqlx.serverMessageId);
      registry.add(Mysqlx.serverMessageId);
      FileDescriptor.internalUpdateFileDescriptor(descriptor, registry);
      Mysqlx.getDescriptor();
   }

   public static final class ColumnMetaData extends GeneratedMessage implements MysqlxResultset.ColumnMetaDataOrBuilder {
      private static final MysqlxResultset.ColumnMetaData defaultInstance = new MysqlxResultset.ColumnMetaData(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxResultset.ColumnMetaData> PARSER = new AbstractParser<MysqlxResultset.ColumnMetaData>() {
         public MysqlxResultset.ColumnMetaData parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxResultset.ColumnMetaData(input, extensionRegistry);
         }
      };
      private int bitField0_;
      public static final int TYPE_FIELD_NUMBER = 1;
      private MysqlxResultset.ColumnMetaData.FieldType type_;
      public static final int NAME_FIELD_NUMBER = 2;
      private ByteString name_;
      public static final int ORIGINAL_NAME_FIELD_NUMBER = 3;
      private ByteString originalName_;
      public static final int TABLE_FIELD_NUMBER = 4;
      private ByteString table_;
      public static final int ORIGINAL_TABLE_FIELD_NUMBER = 5;
      private ByteString originalTable_;
      public static final int SCHEMA_FIELD_NUMBER = 6;
      private ByteString schema_;
      public static final int CATALOG_FIELD_NUMBER = 7;
      private ByteString catalog_;
      public static final int COLLATION_FIELD_NUMBER = 8;
      private long collation_;
      public static final int FRACTIONAL_DIGITS_FIELD_NUMBER = 9;
      private int fractionalDigits_;
      public static final int LENGTH_FIELD_NUMBER = 10;
      private int length_;
      public static final int FLAGS_FIELD_NUMBER = 11;
      private int flags_;
      public static final int CONTENT_TYPE_FIELD_NUMBER = 12;
      private int contentType_;
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private ColumnMetaData(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private ColumnMetaData(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static MysqlxResultset.ColumnMetaData getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxResultset.ColumnMetaData getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private ColumnMetaData(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                     MysqlxResultset.ColumnMetaData.FieldType value = MysqlxResultset.ColumnMetaData.FieldType.valueOf(rawValue);
                     if (value == null) {
                        unknownFields.mergeVarintField(1, rawValue);
                     } else {
                        this.bitField0_ |= 1;
                        this.type_ = value;
                     }
                     break;
                  case 18:
                     this.bitField0_ |= 2;
                     this.name_ = input.readBytes();
                     break;
                  case 26:
                     this.bitField0_ |= 4;
                     this.originalName_ = input.readBytes();
                     break;
                  case 34:
                     this.bitField0_ |= 8;
                     this.table_ = input.readBytes();
                     break;
                  case 42:
                     this.bitField0_ |= 16;
                     this.originalTable_ = input.readBytes();
                     break;
                  case 50:
                     this.bitField0_ |= 32;
                     this.schema_ = input.readBytes();
                     break;
                  case 58:
                     this.bitField0_ |= 64;
                     this.catalog_ = input.readBytes();
                     break;
                  case 64:
                     this.bitField0_ |= 128;
                     this.collation_ = input.readUInt64();
                     break;
                  case 72:
                     this.bitField0_ |= 256;
                     this.fractionalDigits_ = input.readUInt32();
                     break;
                  case 80:
                     this.bitField0_ |= 512;
                     this.length_ = input.readUInt32();
                     break;
                  case 88:
                     this.bitField0_ |= 1024;
                     this.flags_ = input.readUInt32();
                     break;
                  case 96:
                     this.bitField0_ |= 2048;
                     this.contentType_ = input.readUInt32();
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
         return MysqlxResultset.internal_static_Mysqlx_Resultset_ColumnMetaData_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxResultset.internal_static_Mysqlx_Resultset_ColumnMetaData_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxResultset.ColumnMetaData.class, MysqlxResultset.ColumnMetaData.Builder.class);
      }

      public Parser<MysqlxResultset.ColumnMetaData> getParserForType() {
         return PARSER;
      }

      @Override
      public boolean hasType() {
         return (this.bitField0_ & 1) == 1;
      }

      @Override
      public MysqlxResultset.ColumnMetaData.FieldType getType() {
         return this.type_;
      }

      @Override
      public boolean hasName() {
         return (this.bitField0_ & 2) == 2;
      }

      @Override
      public ByteString getName() {
         return this.name_;
      }

      @Override
      public boolean hasOriginalName() {
         return (this.bitField0_ & 4) == 4;
      }

      @Override
      public ByteString getOriginalName() {
         return this.originalName_;
      }

      @Override
      public boolean hasTable() {
         return (this.bitField0_ & 8) == 8;
      }

      @Override
      public ByteString getTable() {
         return this.table_;
      }

      @Override
      public boolean hasOriginalTable() {
         return (this.bitField0_ & 16) == 16;
      }

      @Override
      public ByteString getOriginalTable() {
         return this.originalTable_;
      }

      @Override
      public boolean hasSchema() {
         return (this.bitField0_ & 32) == 32;
      }

      @Override
      public ByteString getSchema() {
         return this.schema_;
      }

      @Override
      public boolean hasCatalog() {
         return (this.bitField0_ & 64) == 64;
      }

      @Override
      public ByteString getCatalog() {
         return this.catalog_;
      }

      @Override
      public boolean hasCollation() {
         return (this.bitField0_ & 128) == 128;
      }

      @Override
      public long getCollation() {
         return this.collation_;
      }

      @Override
      public boolean hasFractionalDigits() {
         return (this.bitField0_ & 256) == 256;
      }

      @Override
      public int getFractionalDigits() {
         return this.fractionalDigits_;
      }

      @Override
      public boolean hasLength() {
         return (this.bitField0_ & 512) == 512;
      }

      @Override
      public int getLength() {
         return this.length_;
      }

      @Override
      public boolean hasFlags() {
         return (this.bitField0_ & 1024) == 1024;
      }

      @Override
      public int getFlags() {
         return this.flags_;
      }

      @Override
      public boolean hasContentType() {
         return (this.bitField0_ & 2048) == 2048;
      }

      @Override
      public int getContentType() {
         return this.contentType_;
      }

      private void initFields() {
         this.type_ = MysqlxResultset.ColumnMetaData.FieldType.SINT;
         this.name_ = ByteString.EMPTY;
         this.originalName_ = ByteString.EMPTY;
         this.table_ = ByteString.EMPTY;
         this.originalTable_ = ByteString.EMPTY;
         this.schema_ = ByteString.EMPTY;
         this.catalog_ = ByteString.EMPTY;
         this.collation_ = 0L;
         this.fractionalDigits_ = 0;
         this.length_ = 0;
         this.flags_ = 0;
         this.contentType_ = 0;
      }

      public final boolean isInitialized() {
         byte isInitialized = this.memoizedIsInitialized;
         if (isInitialized == 1) {
            return true;
         } else if (isInitialized == 0) {
            return false;
         } else if (!this.hasType()) {
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
            output.writeEnum(1, this.type_.getNumber());
         }

         if ((this.bitField0_ & 2) == 2) {
            output.writeBytes(2, this.name_);
         }

         if ((this.bitField0_ & 4) == 4) {
            output.writeBytes(3, this.originalName_);
         }

         if ((this.bitField0_ & 8) == 8) {
            output.writeBytes(4, this.table_);
         }

         if ((this.bitField0_ & 16) == 16) {
            output.writeBytes(5, this.originalTable_);
         }

         if ((this.bitField0_ & 32) == 32) {
            output.writeBytes(6, this.schema_);
         }

         if ((this.bitField0_ & 64) == 64) {
            output.writeBytes(7, this.catalog_);
         }

         if ((this.bitField0_ & 128) == 128) {
            output.writeUInt64(8, this.collation_);
         }

         if ((this.bitField0_ & 256) == 256) {
            output.writeUInt32(9, this.fractionalDigits_);
         }

         if ((this.bitField0_ & 512) == 512) {
            output.writeUInt32(10, this.length_);
         }

         if ((this.bitField0_ & 1024) == 1024) {
            output.writeUInt32(11, this.flags_);
         }

         if ((this.bitField0_ & 2048) == 2048) {
            output.writeUInt32(12, this.contentType_);
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
               size += CodedOutputStream.computeEnumSize(1, this.type_.getNumber());
            }

            if ((this.bitField0_ & 2) == 2) {
               size += CodedOutputStream.computeBytesSize(2, this.name_);
            }

            if ((this.bitField0_ & 4) == 4) {
               size += CodedOutputStream.computeBytesSize(3, this.originalName_);
            }

            if ((this.bitField0_ & 8) == 8) {
               size += CodedOutputStream.computeBytesSize(4, this.table_);
            }

            if ((this.bitField0_ & 16) == 16) {
               size += CodedOutputStream.computeBytesSize(5, this.originalTable_);
            }

            if ((this.bitField0_ & 32) == 32) {
               size += CodedOutputStream.computeBytesSize(6, this.schema_);
            }

            if ((this.bitField0_ & 64) == 64) {
               size += CodedOutputStream.computeBytesSize(7, this.catalog_);
            }

            if ((this.bitField0_ & 128) == 128) {
               size += CodedOutputStream.computeUInt64Size(8, this.collation_);
            }

            if ((this.bitField0_ & 256) == 256) {
               size += CodedOutputStream.computeUInt32Size(9, this.fractionalDigits_);
            }

            if ((this.bitField0_ & 512) == 512) {
               size += CodedOutputStream.computeUInt32Size(10, this.length_);
            }

            if ((this.bitField0_ & 1024) == 1024) {
               size += CodedOutputStream.computeUInt32Size(11, this.flags_);
            }

            if ((this.bitField0_ & 2048) == 2048) {
               size += CodedOutputStream.computeUInt32Size(12, this.contentType_);
            }

            size += this.getUnknownFields().getSerializedSize();
            this.memoizedSerializedSize = size;
            return size;
         }
      }

      protected Object writeReplace() throws ObjectStreamException {
         return super.writeReplace();
      }

      public static MysqlxResultset.ColumnMetaData parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxResultset.ColumnMetaData)PARSER.parseFrom(data);
      }

      public static MysqlxResultset.ColumnMetaData parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxResultset.ColumnMetaData)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxResultset.ColumnMetaData parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxResultset.ColumnMetaData)PARSER.parseFrom(data);
      }

      public static MysqlxResultset.ColumnMetaData parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxResultset.ColumnMetaData)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxResultset.ColumnMetaData parseFrom(InputStream input) throws IOException {
         return (MysqlxResultset.ColumnMetaData)PARSER.parseFrom(input);
      }

      public static MysqlxResultset.ColumnMetaData parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxResultset.ColumnMetaData)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxResultset.ColumnMetaData parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxResultset.ColumnMetaData)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxResultset.ColumnMetaData parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxResultset.ColumnMetaData)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxResultset.ColumnMetaData parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxResultset.ColumnMetaData)PARSER.parseFrom(input);
      }

      public static MysqlxResultset.ColumnMetaData parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxResultset.ColumnMetaData)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxResultset.ColumnMetaData.Builder newBuilder() {
         return MysqlxResultset.ColumnMetaData.Builder.create();
      }

      public MysqlxResultset.ColumnMetaData.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxResultset.ColumnMetaData.Builder newBuilder(MysqlxResultset.ColumnMetaData prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxResultset.ColumnMetaData.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxResultset.ColumnMetaData.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxResultset.ColumnMetaData.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxResultset.ColumnMetaData.Builder>
         implements MysqlxResultset.ColumnMetaDataOrBuilder {
         private int bitField0_;
         private MysqlxResultset.ColumnMetaData.FieldType type_ = MysqlxResultset.ColumnMetaData.FieldType.SINT;
         private ByteString name_ = ByteString.EMPTY;
         private ByteString originalName_ = ByteString.EMPTY;
         private ByteString table_ = ByteString.EMPTY;
         private ByteString originalTable_ = ByteString.EMPTY;
         private ByteString schema_ = ByteString.EMPTY;
         private ByteString catalog_ = ByteString.EMPTY;
         private long collation_;
         private int fractionalDigits_;
         private int length_;
         private int flags_;
         private int contentType_;

         public static final Descriptor getDescriptor() {
            return MysqlxResultset.internal_static_Mysqlx_Resultset_ColumnMetaData_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxResultset.internal_static_Mysqlx_Resultset_ColumnMetaData_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxResultset.ColumnMetaData.class, MysqlxResultset.ColumnMetaData.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxResultset.ColumnMetaData.alwaysUseFieldBuilders) {
            }
         }

         private static MysqlxResultset.ColumnMetaData.Builder create() {
            return new MysqlxResultset.ColumnMetaData.Builder();
         }

         public MysqlxResultset.ColumnMetaData.Builder clear() {
            super.clear();
            this.type_ = MysqlxResultset.ColumnMetaData.FieldType.SINT;
            this.bitField0_ &= -2;
            this.name_ = ByteString.EMPTY;
            this.bitField0_ &= -3;
            this.originalName_ = ByteString.EMPTY;
            this.bitField0_ &= -5;
            this.table_ = ByteString.EMPTY;
            this.bitField0_ &= -9;
            this.originalTable_ = ByteString.EMPTY;
            this.bitField0_ &= -17;
            this.schema_ = ByteString.EMPTY;
            this.bitField0_ &= -33;
            this.catalog_ = ByteString.EMPTY;
            this.bitField0_ &= -65;
            this.collation_ = 0L;
            this.bitField0_ &= -129;
            this.fractionalDigits_ = 0;
            this.bitField0_ &= -257;
            this.length_ = 0;
            this.bitField0_ &= -513;
            this.flags_ = 0;
            this.bitField0_ &= -1025;
            this.contentType_ = 0;
            this.bitField0_ &= -2049;
            return this;
         }

         public MysqlxResultset.ColumnMetaData.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxResultset.internal_static_Mysqlx_Resultset_ColumnMetaData_descriptor;
         }

         public MysqlxResultset.ColumnMetaData getDefaultInstanceForType() {
            return MysqlxResultset.ColumnMetaData.getDefaultInstance();
         }

         public MysqlxResultset.ColumnMetaData build() {
            MysqlxResultset.ColumnMetaData result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxResultset.ColumnMetaData buildPartial() {
            MysqlxResultset.ColumnMetaData result = new MysqlxResultset.ColumnMetaData(this);
            int from_bitField0_ = this.bitField0_;
            int to_bitField0_ = 0;
            if ((from_bitField0_ & 1) == 1) {
               to_bitField0_ |= 1;
            }

            result.type_ = this.type_;
            if ((from_bitField0_ & 2) == 2) {
               to_bitField0_ |= 2;
            }

            result.name_ = this.name_;
            if ((from_bitField0_ & 4) == 4) {
               to_bitField0_ |= 4;
            }

            result.originalName_ = this.originalName_;
            if ((from_bitField0_ & 8) == 8) {
               to_bitField0_ |= 8;
            }

            result.table_ = this.table_;
            if ((from_bitField0_ & 16) == 16) {
               to_bitField0_ |= 16;
            }

            result.originalTable_ = this.originalTable_;
            if ((from_bitField0_ & 32) == 32) {
               to_bitField0_ |= 32;
            }

            result.schema_ = this.schema_;
            if ((from_bitField0_ & 64) == 64) {
               to_bitField0_ |= 64;
            }

            result.catalog_ = this.catalog_;
            if ((from_bitField0_ & 128) == 128) {
               to_bitField0_ |= 128;
            }

            result.collation_ = this.collation_;
            if ((from_bitField0_ & 256) == 256) {
               to_bitField0_ |= 256;
            }

            result.fractionalDigits_ = this.fractionalDigits_;
            if ((from_bitField0_ & 512) == 512) {
               to_bitField0_ |= 512;
            }

            result.length_ = this.length_;
            if ((from_bitField0_ & 1024) == 1024) {
               to_bitField0_ |= 1024;
            }

            result.flags_ = this.flags_;
            if ((from_bitField0_ & 2048) == 2048) {
               to_bitField0_ |= 2048;
            }

            result.contentType_ = this.contentType_;
            result.bitField0_ = to_bitField0_;
            this.onBuilt();
            return result;
         }

         public MysqlxResultset.ColumnMetaData.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxResultset.ColumnMetaData) {
               return this.mergeFrom((MysqlxResultset.ColumnMetaData)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxResultset.ColumnMetaData.Builder mergeFrom(MysqlxResultset.ColumnMetaData other) {
            if (other == MysqlxResultset.ColumnMetaData.getDefaultInstance()) {
               return this;
            } else {
               if (other.hasType()) {
                  this.setType(other.getType());
               }

               if (other.hasName()) {
                  this.setName(other.getName());
               }

               if (other.hasOriginalName()) {
                  this.setOriginalName(other.getOriginalName());
               }

               if (other.hasTable()) {
                  this.setTable(other.getTable());
               }

               if (other.hasOriginalTable()) {
                  this.setOriginalTable(other.getOriginalTable());
               }

               if (other.hasSchema()) {
                  this.setSchema(other.getSchema());
               }

               if (other.hasCatalog()) {
                  this.setCatalog(other.getCatalog());
               }

               if (other.hasCollation()) {
                  this.setCollation(other.getCollation());
               }

               if (other.hasFractionalDigits()) {
                  this.setFractionalDigits(other.getFractionalDigits());
               }

               if (other.hasLength()) {
                  this.setLength(other.getLength());
               }

               if (other.hasFlags()) {
                  this.setFlags(other.getFlags());
               }

               if (other.hasContentType()) {
                  this.setContentType(other.getContentType());
               }

               this.mergeUnknownFields(other.getUnknownFields());
               return this;
            }
         }

         public final boolean isInitialized() {
            return this.hasType();
         }

         public MysqlxResultset.ColumnMetaData.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxResultset.ColumnMetaData parsedMessage = null;

            try {
               parsedMessage = (MysqlxResultset.ColumnMetaData)MysqlxResultset.ColumnMetaData.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxResultset.ColumnMetaData)var8.getUnfinishedMessage();
               throw var8;
            } finally {
               if (parsedMessage != null) {
                  this.mergeFrom(parsedMessage);
               }
            }

            return this;
         }

         @Override
         public boolean hasType() {
            return (this.bitField0_ & 1) == 1;
         }

         @Override
         public MysqlxResultset.ColumnMetaData.FieldType getType() {
            return this.type_;
         }

         public MysqlxResultset.ColumnMetaData.Builder setType(MysqlxResultset.ColumnMetaData.FieldType value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 1;
               this.type_ = value;
               this.onChanged();
               return this;
            }
         }

         public MysqlxResultset.ColumnMetaData.Builder clearType() {
            this.bitField0_ &= -2;
            this.type_ = MysqlxResultset.ColumnMetaData.FieldType.SINT;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasName() {
            return (this.bitField0_ & 2) == 2;
         }

         @Override
         public ByteString getName() {
            return this.name_;
         }

         public MysqlxResultset.ColumnMetaData.Builder setName(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 2;
               this.name_ = value;
               this.onChanged();
               return this;
            }
         }

         public MysqlxResultset.ColumnMetaData.Builder clearName() {
            this.bitField0_ &= -3;
            this.name_ = MysqlxResultset.ColumnMetaData.getDefaultInstance().getName();
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasOriginalName() {
            return (this.bitField0_ & 4) == 4;
         }

         @Override
         public ByteString getOriginalName() {
            return this.originalName_;
         }

         public MysqlxResultset.ColumnMetaData.Builder setOriginalName(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 4;
               this.originalName_ = value;
               this.onChanged();
               return this;
            }
         }

         public MysqlxResultset.ColumnMetaData.Builder clearOriginalName() {
            this.bitField0_ &= -5;
            this.originalName_ = MysqlxResultset.ColumnMetaData.getDefaultInstance().getOriginalName();
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasTable() {
            return (this.bitField0_ & 8) == 8;
         }

         @Override
         public ByteString getTable() {
            return this.table_;
         }

         public MysqlxResultset.ColumnMetaData.Builder setTable(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 8;
               this.table_ = value;
               this.onChanged();
               return this;
            }
         }

         public MysqlxResultset.ColumnMetaData.Builder clearTable() {
            this.bitField0_ &= -9;
            this.table_ = MysqlxResultset.ColumnMetaData.getDefaultInstance().getTable();
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasOriginalTable() {
            return (this.bitField0_ & 16) == 16;
         }

         @Override
         public ByteString getOriginalTable() {
            return this.originalTable_;
         }

         public MysqlxResultset.ColumnMetaData.Builder setOriginalTable(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 16;
               this.originalTable_ = value;
               this.onChanged();
               return this;
            }
         }

         public MysqlxResultset.ColumnMetaData.Builder clearOriginalTable() {
            this.bitField0_ &= -17;
            this.originalTable_ = MysqlxResultset.ColumnMetaData.getDefaultInstance().getOriginalTable();
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasSchema() {
            return (this.bitField0_ & 32) == 32;
         }

         @Override
         public ByteString getSchema() {
            return this.schema_;
         }

         public MysqlxResultset.ColumnMetaData.Builder setSchema(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 32;
               this.schema_ = value;
               this.onChanged();
               return this;
            }
         }

         public MysqlxResultset.ColumnMetaData.Builder clearSchema() {
            this.bitField0_ &= -33;
            this.schema_ = MysqlxResultset.ColumnMetaData.getDefaultInstance().getSchema();
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasCatalog() {
            return (this.bitField0_ & 64) == 64;
         }

         @Override
         public ByteString getCatalog() {
            return this.catalog_;
         }

         public MysqlxResultset.ColumnMetaData.Builder setCatalog(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 64;
               this.catalog_ = value;
               this.onChanged();
               return this;
            }
         }

         public MysqlxResultset.ColumnMetaData.Builder clearCatalog() {
            this.bitField0_ &= -65;
            this.catalog_ = MysqlxResultset.ColumnMetaData.getDefaultInstance().getCatalog();
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasCollation() {
            return (this.bitField0_ & 128) == 128;
         }

         @Override
         public long getCollation() {
            return this.collation_;
         }

         public MysqlxResultset.ColumnMetaData.Builder setCollation(long value) {
            this.bitField0_ |= 128;
            this.collation_ = value;
            this.onChanged();
            return this;
         }

         public MysqlxResultset.ColumnMetaData.Builder clearCollation() {
            this.bitField0_ &= -129;
            this.collation_ = 0L;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasFractionalDigits() {
            return (this.bitField0_ & 256) == 256;
         }

         @Override
         public int getFractionalDigits() {
            return this.fractionalDigits_;
         }

         public MysqlxResultset.ColumnMetaData.Builder setFractionalDigits(int value) {
            this.bitField0_ |= 256;
            this.fractionalDigits_ = value;
            this.onChanged();
            return this;
         }

         public MysqlxResultset.ColumnMetaData.Builder clearFractionalDigits() {
            this.bitField0_ &= -257;
            this.fractionalDigits_ = 0;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasLength() {
            return (this.bitField0_ & 512) == 512;
         }

         @Override
         public int getLength() {
            return this.length_;
         }

         public MysqlxResultset.ColumnMetaData.Builder setLength(int value) {
            this.bitField0_ |= 512;
            this.length_ = value;
            this.onChanged();
            return this;
         }

         public MysqlxResultset.ColumnMetaData.Builder clearLength() {
            this.bitField0_ &= -513;
            this.length_ = 0;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasFlags() {
            return (this.bitField0_ & 1024) == 1024;
         }

         @Override
         public int getFlags() {
            return this.flags_;
         }

         public MysqlxResultset.ColumnMetaData.Builder setFlags(int value) {
            this.bitField0_ |= 1024;
            this.flags_ = value;
            this.onChanged();
            return this;
         }

         public MysqlxResultset.ColumnMetaData.Builder clearFlags() {
            this.bitField0_ &= -1025;
            this.flags_ = 0;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasContentType() {
            return (this.bitField0_ & 2048) == 2048;
         }

         @Override
         public int getContentType() {
            return this.contentType_;
         }

         public MysqlxResultset.ColumnMetaData.Builder setContentType(int value) {
            this.bitField0_ |= 2048;
            this.contentType_ = value;
            this.onChanged();
            return this;
         }

         public MysqlxResultset.ColumnMetaData.Builder clearContentType() {
            this.bitField0_ &= -2049;
            this.contentType_ = 0;
            this.onChanged();
            return this;
         }
      }

      public static enum FieldType implements ProtocolMessageEnum {
         SINT(0, 1),
         UINT(1, 2),
         DOUBLE(2, 5),
         FLOAT(3, 6),
         BYTES(4, 7),
         TIME(5, 10),
         DATETIME(6, 12),
         SET(7, 15),
         ENUM(8, 16),
         BIT(9, 17),
         DECIMAL(10, 18);

         public static final int SINT_VALUE = 1;
         public static final int UINT_VALUE = 2;
         public static final int DOUBLE_VALUE = 5;
         public static final int FLOAT_VALUE = 6;
         public static final int BYTES_VALUE = 7;
         public static final int TIME_VALUE = 10;
         public static final int DATETIME_VALUE = 12;
         public static final int SET_VALUE = 15;
         public static final int ENUM_VALUE = 16;
         public static final int BIT_VALUE = 17;
         public static final int DECIMAL_VALUE = 18;
         private static EnumLiteMap<MysqlxResultset.ColumnMetaData.FieldType> internalValueMap = new EnumLiteMap<MysqlxResultset.ColumnMetaData.FieldType>() {
            public MysqlxResultset.ColumnMetaData.FieldType findValueByNumber(int number) {
               return MysqlxResultset.ColumnMetaData.FieldType.valueOf(number);
            }
         };
         private static final MysqlxResultset.ColumnMetaData.FieldType[] VALUES = values();
         private final int index;
         private final int value;

         public final int getNumber() {
            return this.value;
         }

         public static MysqlxResultset.ColumnMetaData.FieldType valueOf(int value) {
            switch(value) {
               case 1:
                  return SINT;
               case 2:
                  return UINT;
               case 3:
               case 4:
               case 8:
               case 9:
               case 11:
               case 13:
               case 14:
               default:
                  return null;
               case 5:
                  return DOUBLE;
               case 6:
                  return FLOAT;
               case 7:
                  return BYTES;
               case 10:
                  return TIME;
               case 12:
                  return DATETIME;
               case 15:
                  return SET;
               case 16:
                  return ENUM;
               case 17:
                  return BIT;
               case 18:
                  return DECIMAL;
            }
         }

         public static EnumLiteMap<MysqlxResultset.ColumnMetaData.FieldType> internalGetValueMap() {
            return internalValueMap;
         }

         public final EnumValueDescriptor getValueDescriptor() {
            return (EnumValueDescriptor)getDescriptor().getValues().get(this.index);
         }

         public final EnumDescriptor getDescriptorForType() {
            return getDescriptor();
         }

         public static final EnumDescriptor getDescriptor() {
            return (EnumDescriptor)MysqlxResultset.ColumnMetaData.getDescriptor().getEnumTypes().get(0);
         }

         public static MysqlxResultset.ColumnMetaData.FieldType valueOf(EnumValueDescriptor desc) {
            if (desc.getType() != getDescriptor()) {
               throw new IllegalArgumentException("EnumValueDescriptor is not for this type.");
            } else {
               return VALUES[desc.getIndex()];
            }
         }

         private FieldType(int index, int value) {
            this.index = index;
            this.value = value;
         }
      }
   }

   public interface ColumnMetaDataOrBuilder extends MessageOrBuilder {
      boolean hasType();

      MysqlxResultset.ColumnMetaData.FieldType getType();

      boolean hasName();

      ByteString getName();

      boolean hasOriginalName();

      ByteString getOriginalName();

      boolean hasTable();

      ByteString getTable();

      boolean hasOriginalTable();

      ByteString getOriginalTable();

      boolean hasSchema();

      ByteString getSchema();

      boolean hasCatalog();

      ByteString getCatalog();

      boolean hasCollation();

      long getCollation();

      boolean hasFractionalDigits();

      int getFractionalDigits();

      boolean hasLength();

      int getLength();

      boolean hasFlags();

      int getFlags();

      boolean hasContentType();

      int getContentType();
   }

   public static enum ContentType_BYTES implements ProtocolMessageEnum {
      GEOMETRY(0, 1),
      JSON(1, 2),
      XML(2, 3);

      public static final int GEOMETRY_VALUE = 1;
      public static final int JSON_VALUE = 2;
      public static final int XML_VALUE = 3;
      private static EnumLiteMap<MysqlxResultset.ContentType_BYTES> internalValueMap = new EnumLiteMap<MysqlxResultset.ContentType_BYTES>() {
         public MysqlxResultset.ContentType_BYTES findValueByNumber(int number) {
            return MysqlxResultset.ContentType_BYTES.valueOf(number);
         }
      };
      private static final MysqlxResultset.ContentType_BYTES[] VALUES = values();
      private final int index;
      private final int value;

      public final int getNumber() {
         return this.value;
      }

      public static MysqlxResultset.ContentType_BYTES valueOf(int value) {
         switch(value) {
            case 1:
               return GEOMETRY;
            case 2:
               return JSON;
            case 3:
               return XML;
            default:
               return null;
         }
      }

      public static EnumLiteMap<MysqlxResultset.ContentType_BYTES> internalGetValueMap() {
         return internalValueMap;
      }

      public final EnumValueDescriptor getValueDescriptor() {
         return (EnumValueDescriptor)getDescriptor().getValues().get(this.index);
      }

      public final EnumDescriptor getDescriptorForType() {
         return getDescriptor();
      }

      public static final EnumDescriptor getDescriptor() {
         return (EnumDescriptor)MysqlxResultset.getDescriptor().getEnumTypes().get(0);
      }

      public static MysqlxResultset.ContentType_BYTES valueOf(EnumValueDescriptor desc) {
         if (desc.getType() != getDescriptor()) {
            throw new IllegalArgumentException("EnumValueDescriptor is not for this type.");
         } else {
            return VALUES[desc.getIndex()];
         }
      }

      private ContentType_BYTES(int index, int value) {
         this.index = index;
         this.value = value;
      }
   }

   public static enum ContentType_DATETIME implements ProtocolMessageEnum {
      DATE(0, 1),
      DATETIME(1, 2);

      public static final int DATE_VALUE = 1;
      public static final int DATETIME_VALUE = 2;
      private static EnumLiteMap<MysqlxResultset.ContentType_DATETIME> internalValueMap = new EnumLiteMap<MysqlxResultset.ContentType_DATETIME>() {
         public MysqlxResultset.ContentType_DATETIME findValueByNumber(int number) {
            return MysqlxResultset.ContentType_DATETIME.valueOf(number);
         }
      };
      private static final MysqlxResultset.ContentType_DATETIME[] VALUES = values();
      private final int index;
      private final int value;

      public final int getNumber() {
         return this.value;
      }

      public static MysqlxResultset.ContentType_DATETIME valueOf(int value) {
         switch(value) {
            case 1:
               return DATE;
            case 2:
               return DATETIME;
            default:
               return null;
         }
      }

      public static EnumLiteMap<MysqlxResultset.ContentType_DATETIME> internalGetValueMap() {
         return internalValueMap;
      }

      public final EnumValueDescriptor getValueDescriptor() {
         return (EnumValueDescriptor)getDescriptor().getValues().get(this.index);
      }

      public final EnumDescriptor getDescriptorForType() {
         return getDescriptor();
      }

      public static final EnumDescriptor getDescriptor() {
         return (EnumDescriptor)MysqlxResultset.getDescriptor().getEnumTypes().get(1);
      }

      public static MysqlxResultset.ContentType_DATETIME valueOf(EnumValueDescriptor desc) {
         if (desc.getType() != getDescriptor()) {
            throw new IllegalArgumentException("EnumValueDescriptor is not for this type.");
         } else {
            return VALUES[desc.getIndex()];
         }
      }

      private ContentType_DATETIME(int index, int value) {
         this.index = index;
         this.value = value;
      }
   }

   public static final class FetchDone extends GeneratedMessage implements MysqlxResultset.FetchDoneOrBuilder {
      private static final MysqlxResultset.FetchDone defaultInstance = new MysqlxResultset.FetchDone(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxResultset.FetchDone> PARSER = new AbstractParser<MysqlxResultset.FetchDone>() {
         public MysqlxResultset.FetchDone parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxResultset.FetchDone(input, extensionRegistry);
         }
      };
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private FetchDone(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private FetchDone(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static MysqlxResultset.FetchDone getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxResultset.FetchDone getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private FetchDone(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
         return MysqlxResultset.internal_static_Mysqlx_Resultset_FetchDone_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxResultset.internal_static_Mysqlx_Resultset_FetchDone_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxResultset.FetchDone.class, MysqlxResultset.FetchDone.Builder.class);
      }

      public Parser<MysqlxResultset.FetchDone> getParserForType() {
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

      public static MysqlxResultset.FetchDone parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxResultset.FetchDone)PARSER.parseFrom(data);
      }

      public static MysqlxResultset.FetchDone parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxResultset.FetchDone)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxResultset.FetchDone parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxResultset.FetchDone)PARSER.parseFrom(data);
      }

      public static MysqlxResultset.FetchDone parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxResultset.FetchDone)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxResultset.FetchDone parseFrom(InputStream input) throws IOException {
         return (MysqlxResultset.FetchDone)PARSER.parseFrom(input);
      }

      public static MysqlxResultset.FetchDone parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxResultset.FetchDone)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxResultset.FetchDone parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxResultset.FetchDone)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxResultset.FetchDone parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxResultset.FetchDone)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxResultset.FetchDone parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxResultset.FetchDone)PARSER.parseFrom(input);
      }

      public static MysqlxResultset.FetchDone parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxResultset.FetchDone)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxResultset.FetchDone.Builder newBuilder() {
         return MysqlxResultset.FetchDone.Builder.create();
      }

      public MysqlxResultset.FetchDone.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxResultset.FetchDone.Builder newBuilder(MysqlxResultset.FetchDone prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxResultset.FetchDone.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxResultset.FetchDone.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxResultset.FetchDone.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxResultset.FetchDone.Builder>
         implements MysqlxResultset.FetchDoneOrBuilder {
         public static final Descriptor getDescriptor() {
            return MysqlxResultset.internal_static_Mysqlx_Resultset_FetchDone_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxResultset.internal_static_Mysqlx_Resultset_FetchDone_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxResultset.FetchDone.class, MysqlxResultset.FetchDone.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxResultset.FetchDone.alwaysUseFieldBuilders) {
            }
         }

         private static MysqlxResultset.FetchDone.Builder create() {
            return new MysqlxResultset.FetchDone.Builder();
         }

         public MysqlxResultset.FetchDone.Builder clear() {
            super.clear();
            return this;
         }

         public MysqlxResultset.FetchDone.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxResultset.internal_static_Mysqlx_Resultset_FetchDone_descriptor;
         }

         public MysqlxResultset.FetchDone getDefaultInstanceForType() {
            return MysqlxResultset.FetchDone.getDefaultInstance();
         }

         public MysqlxResultset.FetchDone build() {
            MysqlxResultset.FetchDone result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxResultset.FetchDone buildPartial() {
            MysqlxResultset.FetchDone result = new MysqlxResultset.FetchDone(this);
            this.onBuilt();
            return result;
         }

         public MysqlxResultset.FetchDone.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxResultset.FetchDone) {
               return this.mergeFrom((MysqlxResultset.FetchDone)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxResultset.FetchDone.Builder mergeFrom(MysqlxResultset.FetchDone other) {
            if (other == MysqlxResultset.FetchDone.getDefaultInstance()) {
               return this;
            } else {
               this.mergeUnknownFields(other.getUnknownFields());
               return this;
            }
         }

         public final boolean isInitialized() {
            return true;
         }

         public MysqlxResultset.FetchDone.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxResultset.FetchDone parsedMessage = null;

            try {
               parsedMessage = (MysqlxResultset.FetchDone)MysqlxResultset.FetchDone.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxResultset.FetchDone)var8.getUnfinishedMessage();
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

   public static final class FetchDoneMoreOutParams extends GeneratedMessage implements MysqlxResultset.FetchDoneMoreOutParamsOrBuilder {
      private static final MysqlxResultset.FetchDoneMoreOutParams defaultInstance = new MysqlxResultset.FetchDoneMoreOutParams(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxResultset.FetchDoneMoreOutParams> PARSER = new AbstractParser<MysqlxResultset.FetchDoneMoreOutParams>() {
         public MysqlxResultset.FetchDoneMoreOutParams parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxResultset.FetchDoneMoreOutParams(input, extensionRegistry);
         }
      };
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private FetchDoneMoreOutParams(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private FetchDoneMoreOutParams(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static MysqlxResultset.FetchDoneMoreOutParams getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxResultset.FetchDoneMoreOutParams getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private FetchDoneMoreOutParams(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
         return MysqlxResultset.internal_static_Mysqlx_Resultset_FetchDoneMoreOutParams_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxResultset.internal_static_Mysqlx_Resultset_FetchDoneMoreOutParams_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxResultset.FetchDoneMoreOutParams.class, MysqlxResultset.FetchDoneMoreOutParams.Builder.class);
      }

      public Parser<MysqlxResultset.FetchDoneMoreOutParams> getParserForType() {
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

      public static MysqlxResultset.FetchDoneMoreOutParams parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxResultset.FetchDoneMoreOutParams)PARSER.parseFrom(data);
      }

      public static MysqlxResultset.FetchDoneMoreOutParams parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxResultset.FetchDoneMoreOutParams)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxResultset.FetchDoneMoreOutParams parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxResultset.FetchDoneMoreOutParams)PARSER.parseFrom(data);
      }

      public static MysqlxResultset.FetchDoneMoreOutParams parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxResultset.FetchDoneMoreOutParams)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxResultset.FetchDoneMoreOutParams parseFrom(InputStream input) throws IOException {
         return (MysqlxResultset.FetchDoneMoreOutParams)PARSER.parseFrom(input);
      }

      public static MysqlxResultset.FetchDoneMoreOutParams parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxResultset.FetchDoneMoreOutParams)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxResultset.FetchDoneMoreOutParams parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxResultset.FetchDoneMoreOutParams)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxResultset.FetchDoneMoreOutParams parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxResultset.FetchDoneMoreOutParams)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxResultset.FetchDoneMoreOutParams parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxResultset.FetchDoneMoreOutParams)PARSER.parseFrom(input);
      }

      public static MysqlxResultset.FetchDoneMoreOutParams parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxResultset.FetchDoneMoreOutParams)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxResultset.FetchDoneMoreOutParams.Builder newBuilder() {
         return MysqlxResultset.FetchDoneMoreOutParams.Builder.create();
      }

      public MysqlxResultset.FetchDoneMoreOutParams.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxResultset.FetchDoneMoreOutParams.Builder newBuilder(MysqlxResultset.FetchDoneMoreOutParams prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxResultset.FetchDoneMoreOutParams.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxResultset.FetchDoneMoreOutParams.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxResultset.FetchDoneMoreOutParams.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxResultset.FetchDoneMoreOutParams.Builder>
         implements MysqlxResultset.FetchDoneMoreOutParamsOrBuilder {
         public static final Descriptor getDescriptor() {
            return MysqlxResultset.internal_static_Mysqlx_Resultset_FetchDoneMoreOutParams_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxResultset.internal_static_Mysqlx_Resultset_FetchDoneMoreOutParams_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxResultset.FetchDoneMoreOutParams.class, MysqlxResultset.FetchDoneMoreOutParams.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxResultset.FetchDoneMoreOutParams.alwaysUseFieldBuilders) {
            }
         }

         private static MysqlxResultset.FetchDoneMoreOutParams.Builder create() {
            return new MysqlxResultset.FetchDoneMoreOutParams.Builder();
         }

         public MysqlxResultset.FetchDoneMoreOutParams.Builder clear() {
            super.clear();
            return this;
         }

         public MysqlxResultset.FetchDoneMoreOutParams.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxResultset.internal_static_Mysqlx_Resultset_FetchDoneMoreOutParams_descriptor;
         }

         public MysqlxResultset.FetchDoneMoreOutParams getDefaultInstanceForType() {
            return MysqlxResultset.FetchDoneMoreOutParams.getDefaultInstance();
         }

         public MysqlxResultset.FetchDoneMoreOutParams build() {
            MysqlxResultset.FetchDoneMoreOutParams result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxResultset.FetchDoneMoreOutParams buildPartial() {
            MysqlxResultset.FetchDoneMoreOutParams result = new MysqlxResultset.FetchDoneMoreOutParams(this);
            this.onBuilt();
            return result;
         }

         public MysqlxResultset.FetchDoneMoreOutParams.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxResultset.FetchDoneMoreOutParams) {
               return this.mergeFrom((MysqlxResultset.FetchDoneMoreOutParams)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxResultset.FetchDoneMoreOutParams.Builder mergeFrom(MysqlxResultset.FetchDoneMoreOutParams other) {
            if (other == MysqlxResultset.FetchDoneMoreOutParams.getDefaultInstance()) {
               return this;
            } else {
               this.mergeUnknownFields(other.getUnknownFields());
               return this;
            }
         }

         public final boolean isInitialized() {
            return true;
         }

         public MysqlxResultset.FetchDoneMoreOutParams.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxResultset.FetchDoneMoreOutParams parsedMessage = null;

            try {
               parsedMessage = (MysqlxResultset.FetchDoneMoreOutParams)MysqlxResultset.FetchDoneMoreOutParams.PARSER
                  .parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxResultset.FetchDoneMoreOutParams)var8.getUnfinishedMessage();
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

   public interface FetchDoneMoreOutParamsOrBuilder extends MessageOrBuilder {
   }

   public static final class FetchDoneMoreResultsets extends GeneratedMessage implements MysqlxResultset.FetchDoneMoreResultsetsOrBuilder {
      private static final MysqlxResultset.FetchDoneMoreResultsets defaultInstance = new MysqlxResultset.FetchDoneMoreResultsets(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxResultset.FetchDoneMoreResultsets> PARSER = new AbstractParser<MysqlxResultset.FetchDoneMoreResultsets>() {
         public MysqlxResultset.FetchDoneMoreResultsets parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxResultset.FetchDoneMoreResultsets(input, extensionRegistry);
         }
      };
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private FetchDoneMoreResultsets(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private FetchDoneMoreResultsets(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static MysqlxResultset.FetchDoneMoreResultsets getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxResultset.FetchDoneMoreResultsets getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private FetchDoneMoreResultsets(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
         return MysqlxResultset.internal_static_Mysqlx_Resultset_FetchDoneMoreResultsets_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxResultset.internal_static_Mysqlx_Resultset_FetchDoneMoreResultsets_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxResultset.FetchDoneMoreResultsets.class, MysqlxResultset.FetchDoneMoreResultsets.Builder.class);
      }

      public Parser<MysqlxResultset.FetchDoneMoreResultsets> getParserForType() {
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

      public static MysqlxResultset.FetchDoneMoreResultsets parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxResultset.FetchDoneMoreResultsets)PARSER.parseFrom(data);
      }

      public static MysqlxResultset.FetchDoneMoreResultsets parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxResultset.FetchDoneMoreResultsets)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxResultset.FetchDoneMoreResultsets parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxResultset.FetchDoneMoreResultsets)PARSER.parseFrom(data);
      }

      public static MysqlxResultset.FetchDoneMoreResultsets parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxResultset.FetchDoneMoreResultsets)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxResultset.FetchDoneMoreResultsets parseFrom(InputStream input) throws IOException {
         return (MysqlxResultset.FetchDoneMoreResultsets)PARSER.parseFrom(input);
      }

      public static MysqlxResultset.FetchDoneMoreResultsets parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxResultset.FetchDoneMoreResultsets)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxResultset.FetchDoneMoreResultsets parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxResultset.FetchDoneMoreResultsets)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxResultset.FetchDoneMoreResultsets parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxResultset.FetchDoneMoreResultsets)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxResultset.FetchDoneMoreResultsets parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxResultset.FetchDoneMoreResultsets)PARSER.parseFrom(input);
      }

      public static MysqlxResultset.FetchDoneMoreResultsets parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxResultset.FetchDoneMoreResultsets)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxResultset.FetchDoneMoreResultsets.Builder newBuilder() {
         return MysqlxResultset.FetchDoneMoreResultsets.Builder.create();
      }

      public MysqlxResultset.FetchDoneMoreResultsets.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxResultset.FetchDoneMoreResultsets.Builder newBuilder(MysqlxResultset.FetchDoneMoreResultsets prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxResultset.FetchDoneMoreResultsets.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxResultset.FetchDoneMoreResultsets.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxResultset.FetchDoneMoreResultsets.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxResultset.FetchDoneMoreResultsets.Builder>
         implements MysqlxResultset.FetchDoneMoreResultsetsOrBuilder {
         public static final Descriptor getDescriptor() {
            return MysqlxResultset.internal_static_Mysqlx_Resultset_FetchDoneMoreResultsets_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxResultset.internal_static_Mysqlx_Resultset_FetchDoneMoreResultsets_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxResultset.FetchDoneMoreResultsets.class, MysqlxResultset.FetchDoneMoreResultsets.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxResultset.FetchDoneMoreResultsets.alwaysUseFieldBuilders) {
            }
         }

         private static MysqlxResultset.FetchDoneMoreResultsets.Builder create() {
            return new MysqlxResultset.FetchDoneMoreResultsets.Builder();
         }

         public MysqlxResultset.FetchDoneMoreResultsets.Builder clear() {
            super.clear();
            return this;
         }

         public MysqlxResultset.FetchDoneMoreResultsets.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxResultset.internal_static_Mysqlx_Resultset_FetchDoneMoreResultsets_descriptor;
         }

         public MysqlxResultset.FetchDoneMoreResultsets getDefaultInstanceForType() {
            return MysqlxResultset.FetchDoneMoreResultsets.getDefaultInstance();
         }

         public MysqlxResultset.FetchDoneMoreResultsets build() {
            MysqlxResultset.FetchDoneMoreResultsets result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxResultset.FetchDoneMoreResultsets buildPartial() {
            MysqlxResultset.FetchDoneMoreResultsets result = new MysqlxResultset.FetchDoneMoreResultsets(this);
            this.onBuilt();
            return result;
         }

         public MysqlxResultset.FetchDoneMoreResultsets.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxResultset.FetchDoneMoreResultsets) {
               return this.mergeFrom((MysqlxResultset.FetchDoneMoreResultsets)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxResultset.FetchDoneMoreResultsets.Builder mergeFrom(MysqlxResultset.FetchDoneMoreResultsets other) {
            if (other == MysqlxResultset.FetchDoneMoreResultsets.getDefaultInstance()) {
               return this;
            } else {
               this.mergeUnknownFields(other.getUnknownFields());
               return this;
            }
         }

         public final boolean isInitialized() {
            return true;
         }

         public MysqlxResultset.FetchDoneMoreResultsets.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxResultset.FetchDoneMoreResultsets parsedMessage = null;

            try {
               parsedMessage = (MysqlxResultset.FetchDoneMoreResultsets)MysqlxResultset.FetchDoneMoreResultsets.PARSER
                  .parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxResultset.FetchDoneMoreResultsets)var8.getUnfinishedMessage();
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

   public interface FetchDoneMoreResultsetsOrBuilder extends MessageOrBuilder {
   }

   public interface FetchDoneOrBuilder extends MessageOrBuilder {
   }

   public static final class Row extends GeneratedMessage implements MysqlxResultset.RowOrBuilder {
      private static final MysqlxResultset.Row defaultInstance = new MysqlxResultset.Row(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxResultset.Row> PARSER = new AbstractParser<MysqlxResultset.Row>() {
         public MysqlxResultset.Row parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxResultset.Row(input, extensionRegistry);
         }
      };
      public static final int FIELD_FIELD_NUMBER = 1;
      private List<ByteString> field_;
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private Row(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private Row(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static MysqlxResultset.Row getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxResultset.Row getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private Row(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                     if ((mutable_bitField0_ & 1) != 1) {
                        this.field_ = new ArrayList();
                        mutable_bitField0_ |= 1;
                     }

                     this.field_.add(input.readBytes());
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
            if ((mutable_bitField0_ & 1) == 1) {
               this.field_ = Collections.unmodifiableList(this.field_);
            }

            this.unknownFields = unknownFields.build();
            this.makeExtensionsImmutable();
         }
      }

      public static final Descriptor getDescriptor() {
         return MysqlxResultset.internal_static_Mysqlx_Resultset_Row_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxResultset.internal_static_Mysqlx_Resultset_Row_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxResultset.Row.class, MysqlxResultset.Row.Builder.class);
      }

      public Parser<MysqlxResultset.Row> getParserForType() {
         return PARSER;
      }

      @Override
      public List<ByteString> getFieldList() {
         return this.field_;
      }

      @Override
      public int getFieldCount() {
         return this.field_.size();
      }

      @Override
      public ByteString getField(int index) {
         return (ByteString)this.field_.get(index);
      }

      private void initFields() {
         this.field_ = Collections.emptyList();
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

         for(int i = 0; i < this.field_.size(); ++i) {
            output.writeBytes(1, (ByteString)this.field_.get(i));
         }

         this.getUnknownFields().writeTo(output);
      }

      public int getSerializedSize() {
         int size = this.memoizedSerializedSize;
         if (size != -1) {
            return size;
         } else {
            int var4 = 0;
            int dataSize = 0;

            for(int i = 0; i < this.field_.size(); ++i) {
               dataSize += CodedOutputStream.computeBytesSizeNoTag((ByteString)this.field_.get(i));
            }

            var4 += dataSize;
            var4 += 1 * this.getFieldList().size();
            var4 += this.getUnknownFields().getSerializedSize();
            this.memoizedSerializedSize = var4;
            return var4;
         }
      }

      protected Object writeReplace() throws ObjectStreamException {
         return super.writeReplace();
      }

      public static MysqlxResultset.Row parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxResultset.Row)PARSER.parseFrom(data);
      }

      public static MysqlxResultset.Row parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxResultset.Row)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxResultset.Row parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxResultset.Row)PARSER.parseFrom(data);
      }

      public static MysqlxResultset.Row parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxResultset.Row)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxResultset.Row parseFrom(InputStream input) throws IOException {
         return (MysqlxResultset.Row)PARSER.parseFrom(input);
      }

      public static MysqlxResultset.Row parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxResultset.Row)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxResultset.Row parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxResultset.Row)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxResultset.Row parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxResultset.Row)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxResultset.Row parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxResultset.Row)PARSER.parseFrom(input);
      }

      public static MysqlxResultset.Row parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxResultset.Row)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxResultset.Row.Builder newBuilder() {
         return MysqlxResultset.Row.Builder.create();
      }

      public MysqlxResultset.Row.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxResultset.Row.Builder newBuilder(MysqlxResultset.Row prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxResultset.Row.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxResultset.Row.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxResultset.Row.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxResultset.Row.Builder>
         implements MysqlxResultset.RowOrBuilder {
         private int bitField0_;
         private List<ByteString> field_ = Collections.emptyList();

         public static final Descriptor getDescriptor() {
            return MysqlxResultset.internal_static_Mysqlx_Resultset_Row_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxResultset.internal_static_Mysqlx_Resultset_Row_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxResultset.Row.class, MysqlxResultset.Row.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxResultset.Row.alwaysUseFieldBuilders) {
            }
         }

         private static MysqlxResultset.Row.Builder create() {
            return new MysqlxResultset.Row.Builder();
         }

         public MysqlxResultset.Row.Builder clear() {
            super.clear();
            this.field_ = Collections.emptyList();
            this.bitField0_ &= -2;
            return this;
         }

         public MysqlxResultset.Row.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxResultset.internal_static_Mysqlx_Resultset_Row_descriptor;
         }

         public MysqlxResultset.Row getDefaultInstanceForType() {
            return MysqlxResultset.Row.getDefaultInstance();
         }

         public MysqlxResultset.Row build() {
            MysqlxResultset.Row result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxResultset.Row buildPartial() {
            MysqlxResultset.Row result = new MysqlxResultset.Row(this);
            int from_bitField0_ = this.bitField0_;
            if ((this.bitField0_ & 1) == 1) {
               this.field_ = Collections.unmodifiableList(this.field_);
               this.bitField0_ &= -2;
            }

            result.field_ = this.field_;
            this.onBuilt();
            return result;
         }

         public MysqlxResultset.Row.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxResultset.Row) {
               return this.mergeFrom((MysqlxResultset.Row)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxResultset.Row.Builder mergeFrom(MysqlxResultset.Row other) {
            if (other == MysqlxResultset.Row.getDefaultInstance()) {
               return this;
            } else {
               if (!other.field_.isEmpty()) {
                  if (this.field_.isEmpty()) {
                     this.field_ = other.field_;
                     this.bitField0_ &= -2;
                  } else {
                     this.ensureFieldIsMutable();
                     this.field_.addAll(other.field_);
                  }

                  this.onChanged();
               }

               this.mergeUnknownFields(other.getUnknownFields());
               return this;
            }
         }

         public final boolean isInitialized() {
            return true;
         }

         public MysqlxResultset.Row.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxResultset.Row parsedMessage = null;

            try {
               parsedMessage = (MysqlxResultset.Row)MysqlxResultset.Row.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxResultset.Row)var8.getUnfinishedMessage();
               throw var8;
            } finally {
               if (parsedMessage != null) {
                  this.mergeFrom(parsedMessage);
               }
            }

            return this;
         }

         private void ensureFieldIsMutable() {
            if ((this.bitField0_ & 1) != 1) {
               this.field_ = new ArrayList(this.field_);
               this.bitField0_ |= 1;
            }
         }

         @Override
         public List<ByteString> getFieldList() {
            return Collections.unmodifiableList(this.field_);
         }

         @Override
         public int getFieldCount() {
            return this.field_.size();
         }

         @Override
         public ByteString getField(int index) {
            return (ByteString)this.field_.get(index);
         }

         public MysqlxResultset.Row.Builder setField(int index, ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.ensureFieldIsMutable();
               this.field_.set(index, value);
               this.onChanged();
               return this;
            }
         }

         public MysqlxResultset.Row.Builder addField(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.ensureFieldIsMutable();
               this.field_.add(value);
               this.onChanged();
               return this;
            }
         }

         public MysqlxResultset.Row.Builder addAllField(Iterable<? extends ByteString> values) {
            this.ensureFieldIsMutable();
            com.google.protobuf.AbstractMessageLite.Builder.addAll(values, this.field_);
            this.onChanged();
            return this;
         }

         public MysqlxResultset.Row.Builder clearField() {
            this.field_ = Collections.emptyList();
            this.bitField0_ &= -2;
            this.onChanged();
            return this;
         }
      }
   }

   public interface RowOrBuilder extends MessageOrBuilder {
      List<ByteString> getFieldList();

      int getFieldCount();

      ByteString getField(int var1);
   }
}
