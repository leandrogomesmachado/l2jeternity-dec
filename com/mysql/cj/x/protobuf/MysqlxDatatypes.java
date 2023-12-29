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
import com.google.protobuf.SingleFieldBuilder;
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

public final class MysqlxDatatypes {
   private static final Descriptor internal_static_Mysqlx_Datatypes_Scalar_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(0);
   private static FieldAccessorTable internal_static_Mysqlx_Datatypes_Scalar_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Datatypes_Scalar_descriptor,
      new java.lang.String[]{"Type", "VSignedInt", "VUnsignedInt", "VOctets", "VDouble", "VFloat", "VBool", "VString"}
   );
   private static final Descriptor internal_static_Mysqlx_Datatypes_Scalar_String_descriptor = (Descriptor)internal_static_Mysqlx_Datatypes_Scalar_descriptor.getNestedTypes(
         
      )
      .get(0);
   private static FieldAccessorTable internal_static_Mysqlx_Datatypes_Scalar_String_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Datatypes_Scalar_String_descriptor, new java.lang.String[]{"Value", "Collation"}
   );
   private static final Descriptor internal_static_Mysqlx_Datatypes_Scalar_Octets_descriptor = (Descriptor)internal_static_Mysqlx_Datatypes_Scalar_descriptor.getNestedTypes(
         
      )
      .get(1);
   private static FieldAccessorTable internal_static_Mysqlx_Datatypes_Scalar_Octets_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Datatypes_Scalar_Octets_descriptor, new java.lang.String[]{"Value", "ContentType"}
   );
   private static final Descriptor internal_static_Mysqlx_Datatypes_Object_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(1);
   private static FieldAccessorTable internal_static_Mysqlx_Datatypes_Object_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Datatypes_Object_descriptor, new java.lang.String[]{"Fld"}
   );
   private static final Descriptor internal_static_Mysqlx_Datatypes_Object_ObjectField_descriptor = (Descriptor)internal_static_Mysqlx_Datatypes_Object_descriptor.getNestedTypes(
         
      )
      .get(0);
   private static FieldAccessorTable internal_static_Mysqlx_Datatypes_Object_ObjectField_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Datatypes_Object_ObjectField_descriptor, new java.lang.String[]{"Key", "Value"}
   );
   private static final Descriptor internal_static_Mysqlx_Datatypes_Array_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(2);
   private static FieldAccessorTable internal_static_Mysqlx_Datatypes_Array_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Datatypes_Array_descriptor, new java.lang.String[]{"Value"}
   );
   private static final Descriptor internal_static_Mysqlx_Datatypes_Any_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(3);
   private static FieldAccessorTable internal_static_Mysqlx_Datatypes_Any_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Datatypes_Any_descriptor, new java.lang.String[]{"Type", "Scalar", "Obj", "Array"}
   );
   private static FileDescriptor descriptor;

   private MysqlxDatatypes() {
   }

   public static void registerAllExtensions(ExtensionRegistry registry) {
   }

   public static FileDescriptor getDescriptor() {
      return descriptor;
   }

   static {
      java.lang.String[] descriptorData = new java.lang.String[]{
         "\n\u0016mysqlx_datatypes.proto\u0012\u0010Mysqlx.Datatypes\"Æ\u0003\n\u0006Scalar\u0012+\n\u0004type\u0018\u0001 \u0002(\u000e2\u001d.Mysqlx.Datatypes.Scalar.Type\u0012\u0014\n\fv_signed_int\u0018\u0002 \u0001(\u0012\u0012\u0016\n\u000ev_unsigned_int\u0018\u0003 \u0001(\u0004\u00121\n\bv_octets\u0018\u0005 \u0001(\u000b2\u001f.Mysqlx.Datatypes.Scalar.Octets\u0012\u0010\n\bv_double\u0018\u0006 \u0001(\u0001\u0012\u000f\n\u0007v_float\u0018\u0007 \u0001(\u0002\u0012\u000e\n\u0006v_bool\u0018\b \u0001(\b\u00121\n\bv_string\u0018\t \u0001(\u000b2\u001f.Mysqlx.Datatypes.Scalar.String\u001a*\n\u0006String\u0012\r\n\u0005value\u0018\u0001 \u0002(\f\u0012\u0011\n\tcollation\u0018\u0002 \u0001(\u0004\u001a-\n\u0006Octets\u0012\r\n\u0005value\u0018\u0001 \u0002(\f\u0012\u0014\n\fcontent_type\u0018\u0002 \u0001(\r\"m\n\u0004Type\u0012\n\n\u0006",
         "V_SINT\u0010\u0001\u0012\n\n\u0006V_UINT\u0010\u0002\u0012\n\n\u0006V_NULL\u0010\u0003\u0012\f\n\bV_OCTETS\u0010\u0004\u0012\f\n\bV_DOUBLE\u0010\u0005\u0012\u000b\n\u0007V_FLOAT\u0010\u0006\u0012\n\n\u0006V_BOOL\u0010\u0007\u0012\f\n\bV_STRING\u0010\b\"}\n\u0006Object\u00121\n\u0003fld\u0018\u0001 \u0003(\u000b2$.Mysqlx.Datatypes.Object.ObjectField\u001a@\n\u000bObjectField\u0012\u000b\n\u0003key\u0018\u0001 \u0002(\t\u0012$\n\u0005value\u0018\u0002 \u0002(\u000b2\u0015.Mysqlx.Datatypes.Any\"-\n\u0005Array\u0012$\n\u0005value\u0018\u0001 \u0003(\u000b2\u0015.Mysqlx.Datatypes.Any\"Ó\u0001\n\u0003Any\u0012(\n\u0004type\u0018\u0001 \u0002(\u000e2\u001a.Mysqlx.Datatypes.Any.Type\u0012(\n\u0006scalar\u0018\u0002 \u0001(\u000b2\u0018.Mysqlx.Datatypes.Scalar\u0012%\n\u0003obj\u0018\u0003 \u0001(\u000b2\u0018.Mysqlx.Datatypes.Ob",
         "ject\u0012&\n\u0005array\u0018\u0004 \u0001(\u000b2\u0017.Mysqlx.Datatypes.Array\")\n\u0004Type\u0012\n\n\u0006SCALAR\u0010\u0001\u0012\n\n\u0006OBJECT\u0010\u0002\u0012\t\n\u0005ARRAY\u0010\u0003B\u0019\n\u0017com.mysql.cj.x.protobuf"
      };
      InternalDescriptorAssigner assigner = new InternalDescriptorAssigner() {
         public ExtensionRegistry assignDescriptors(FileDescriptor root) {
            MysqlxDatatypes.descriptor = root;
            return null;
         }
      };
      FileDescriptor.internalBuildGeneratedFileFrom(descriptorData, new FileDescriptor[0], assigner);
   }

   public static final class Any extends GeneratedMessage implements MysqlxDatatypes.AnyOrBuilder {
      private static final MysqlxDatatypes.Any defaultInstance = new MysqlxDatatypes.Any(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxDatatypes.Any> PARSER = new AbstractParser<MysqlxDatatypes.Any>() {
         public MysqlxDatatypes.Any parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxDatatypes.Any(input, extensionRegistry);
         }
      };
      private int bitField0_;
      public static final int TYPE_FIELD_NUMBER = 1;
      private MysqlxDatatypes.Any.Type type_;
      public static final int SCALAR_FIELD_NUMBER = 2;
      private MysqlxDatatypes.Scalar scalar_;
      public static final int OBJ_FIELD_NUMBER = 3;
      private MysqlxDatatypes.Object obj_;
      public static final int ARRAY_FIELD_NUMBER = 4;
      private MysqlxDatatypes.Array array_;
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private Any(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private Any(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static MysqlxDatatypes.Any getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxDatatypes.Any getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private Any(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                     MysqlxDatatypes.Any.Type value = MysqlxDatatypes.Any.Type.valueOf(rawValue);
                     if (value == null) {
                        unknownFields.mergeVarintField(1, rawValue);
                     } else {
                        this.bitField0_ |= 1;
                        this.type_ = value;
                     }
                     break;
                  case 18:
                     MysqlxDatatypes.Scalar.Builder subBuilder = null;
                     if ((this.bitField0_ & 2) == 2) {
                        subBuilder = this.scalar_.toBuilder();
                     }

                     this.scalar_ = (MysqlxDatatypes.Scalar)input.readMessage(MysqlxDatatypes.Scalar.PARSER, extensionRegistry);
                     if (subBuilder != null) {
                        subBuilder.mergeFrom(this.scalar_);
                        this.scalar_ = subBuilder.buildPartial();
                     }

                     this.bitField0_ |= 2;
                     break;
                  case 26:
                     MysqlxDatatypes.Object.Builder subBuilder = null;
                     if ((this.bitField0_ & 4) == 4) {
                        subBuilder = this.obj_.toBuilder();
                     }

                     this.obj_ = (MysqlxDatatypes.Object)input.readMessage(MysqlxDatatypes.Object.PARSER, extensionRegistry);
                     if (subBuilder != null) {
                        subBuilder.mergeFrom(this.obj_);
                        this.obj_ = subBuilder.buildPartial();
                     }

                     this.bitField0_ |= 4;
                     break;
                  case 34:
                     MysqlxDatatypes.Array.Builder subBuilder = null;
                     if ((this.bitField0_ & 8) == 8) {
                        subBuilder = this.array_.toBuilder();
                     }

                     this.array_ = (MysqlxDatatypes.Array)input.readMessage(MysqlxDatatypes.Array.PARSER, extensionRegistry);
                     if (subBuilder != null) {
                        subBuilder.mergeFrom(this.array_);
                        this.array_ = subBuilder.buildPartial();
                     }

                     this.bitField0_ |= 8;
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
         return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Any_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Any_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxDatatypes.Any.class, MysqlxDatatypes.Any.Builder.class);
      }

      public Parser<MysqlxDatatypes.Any> getParserForType() {
         return PARSER;
      }

      @Override
      public boolean hasType() {
         return (this.bitField0_ & 1) == 1;
      }

      @Override
      public MysqlxDatatypes.Any.Type getType() {
         return this.type_;
      }

      @Override
      public boolean hasScalar() {
         return (this.bitField0_ & 2) == 2;
      }

      @Override
      public MysqlxDatatypes.Scalar getScalar() {
         return this.scalar_;
      }

      @Override
      public MysqlxDatatypes.ScalarOrBuilder getScalarOrBuilder() {
         return this.scalar_;
      }

      @Override
      public boolean hasObj() {
         return (this.bitField0_ & 4) == 4;
      }

      @Override
      public MysqlxDatatypes.Object getObj() {
         return this.obj_;
      }

      @Override
      public MysqlxDatatypes.ObjectOrBuilder getObjOrBuilder() {
         return this.obj_;
      }

      @Override
      public boolean hasArray() {
         return (this.bitField0_ & 8) == 8;
      }

      @Override
      public MysqlxDatatypes.Array getArray() {
         return this.array_;
      }

      @Override
      public MysqlxDatatypes.ArrayOrBuilder getArrayOrBuilder() {
         return this.array_;
      }

      private void initFields() {
         this.type_ = MysqlxDatatypes.Any.Type.SCALAR;
         this.scalar_ = MysqlxDatatypes.Scalar.getDefaultInstance();
         this.obj_ = MysqlxDatatypes.Object.getDefaultInstance();
         this.array_ = MysqlxDatatypes.Array.getDefaultInstance();
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
         } else if (this.hasScalar() && !this.getScalar().isInitialized()) {
            this.memoizedIsInitialized = 0;
            return false;
         } else if (this.hasObj() && !this.getObj().isInitialized()) {
            this.memoizedIsInitialized = 0;
            return false;
         } else if (this.hasArray() && !this.getArray().isInitialized()) {
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
            output.writeMessage(2, this.scalar_);
         }

         if ((this.bitField0_ & 4) == 4) {
            output.writeMessage(3, this.obj_);
         }

         if ((this.bitField0_ & 8) == 8) {
            output.writeMessage(4, this.array_);
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
               size += CodedOutputStream.computeMessageSize(2, this.scalar_);
            }

            if ((this.bitField0_ & 4) == 4) {
               size += CodedOutputStream.computeMessageSize(3, this.obj_);
            }

            if ((this.bitField0_ & 8) == 8) {
               size += CodedOutputStream.computeMessageSize(4, this.array_);
            }

            size += this.getUnknownFields().getSerializedSize();
            this.memoizedSerializedSize = size;
            return size;
         }
      }

      protected java.lang.Object writeReplace() throws ObjectStreamException {
         return super.writeReplace();
      }

      public static MysqlxDatatypes.Any parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Any)PARSER.parseFrom(data);
      }

      public static MysqlxDatatypes.Any parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Any)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxDatatypes.Any parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Any)PARSER.parseFrom(data);
      }

      public static MysqlxDatatypes.Any parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Any)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxDatatypes.Any parseFrom(InputStream input) throws IOException {
         return (MysqlxDatatypes.Any)PARSER.parseFrom(input);
      }

      public static MysqlxDatatypes.Any parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxDatatypes.Any)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxDatatypes.Any parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxDatatypes.Any)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxDatatypes.Any parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxDatatypes.Any)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxDatatypes.Any parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxDatatypes.Any)PARSER.parseFrom(input);
      }

      public static MysqlxDatatypes.Any parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxDatatypes.Any)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxDatatypes.Any.Builder newBuilder() {
         return MysqlxDatatypes.Any.Builder.create();
      }

      public MysqlxDatatypes.Any.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxDatatypes.Any.Builder newBuilder(MysqlxDatatypes.Any prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxDatatypes.Any.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxDatatypes.Any.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxDatatypes.Any.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxDatatypes.Any.Builder>
         implements MysqlxDatatypes.AnyOrBuilder {
         private int bitField0_;
         private MysqlxDatatypes.Any.Type type_ = MysqlxDatatypes.Any.Type.SCALAR;
         private MysqlxDatatypes.Scalar scalar_ = MysqlxDatatypes.Scalar.getDefaultInstance();
         private SingleFieldBuilder<MysqlxDatatypes.Scalar, MysqlxDatatypes.Scalar.Builder, MysqlxDatatypes.ScalarOrBuilder> scalarBuilder_;
         private MysqlxDatatypes.Object obj_ = MysqlxDatatypes.Object.getDefaultInstance();
         private SingleFieldBuilder<MysqlxDatatypes.Object, MysqlxDatatypes.Object.Builder, MysqlxDatatypes.ObjectOrBuilder> objBuilder_;
         private MysqlxDatatypes.Array array_ = MysqlxDatatypes.Array.getDefaultInstance();
         private SingleFieldBuilder<MysqlxDatatypes.Array, MysqlxDatatypes.Array.Builder, MysqlxDatatypes.ArrayOrBuilder> arrayBuilder_;

         public static final Descriptor getDescriptor() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Any_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Any_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxDatatypes.Any.class, MysqlxDatatypes.Any.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxDatatypes.Any.alwaysUseFieldBuilders) {
               this.getScalarFieldBuilder();
               this.getObjFieldBuilder();
               this.getArrayFieldBuilder();
            }
         }

         private static MysqlxDatatypes.Any.Builder create() {
            return new MysqlxDatatypes.Any.Builder();
         }

         public MysqlxDatatypes.Any.Builder clear() {
            super.clear();
            this.type_ = MysqlxDatatypes.Any.Type.SCALAR;
            this.bitField0_ &= -2;
            if (this.scalarBuilder_ == null) {
               this.scalar_ = MysqlxDatatypes.Scalar.getDefaultInstance();
            } else {
               this.scalarBuilder_.clear();
            }

            this.bitField0_ &= -3;
            if (this.objBuilder_ == null) {
               this.obj_ = MysqlxDatatypes.Object.getDefaultInstance();
            } else {
               this.objBuilder_.clear();
            }

            this.bitField0_ &= -5;
            if (this.arrayBuilder_ == null) {
               this.array_ = MysqlxDatatypes.Array.getDefaultInstance();
            } else {
               this.arrayBuilder_.clear();
            }

            this.bitField0_ &= -9;
            return this;
         }

         public MysqlxDatatypes.Any.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Any_descriptor;
         }

         public MysqlxDatatypes.Any getDefaultInstanceForType() {
            return MysqlxDatatypes.Any.getDefaultInstance();
         }

         public MysqlxDatatypes.Any build() {
            MysqlxDatatypes.Any result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxDatatypes.Any buildPartial() {
            MysqlxDatatypes.Any result = new MysqlxDatatypes.Any(this);
            int from_bitField0_ = this.bitField0_;
            int to_bitField0_ = 0;
            if ((from_bitField0_ & 1) == 1) {
               to_bitField0_ |= 1;
            }

            result.type_ = this.type_;
            if ((from_bitField0_ & 2) == 2) {
               to_bitField0_ |= 2;
            }

            if (this.scalarBuilder_ == null) {
               result.scalar_ = this.scalar_;
            } else {
               result.scalar_ = (MysqlxDatatypes.Scalar)this.scalarBuilder_.build();
            }

            if ((from_bitField0_ & 4) == 4) {
               to_bitField0_ |= 4;
            }

            if (this.objBuilder_ == null) {
               result.obj_ = this.obj_;
            } else {
               result.obj_ = (MysqlxDatatypes.Object)this.objBuilder_.build();
            }

            if ((from_bitField0_ & 8) == 8) {
               to_bitField0_ |= 8;
            }

            if (this.arrayBuilder_ == null) {
               result.array_ = this.array_;
            } else {
               result.array_ = (MysqlxDatatypes.Array)this.arrayBuilder_.build();
            }

            result.bitField0_ = to_bitField0_;
            this.onBuilt();
            return result;
         }

         public MysqlxDatatypes.Any.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxDatatypes.Any) {
               return this.mergeFrom((MysqlxDatatypes.Any)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxDatatypes.Any.Builder mergeFrom(MysqlxDatatypes.Any other) {
            if (other == MysqlxDatatypes.Any.getDefaultInstance()) {
               return this;
            } else {
               if (other.hasType()) {
                  this.setType(other.getType());
               }

               if (other.hasScalar()) {
                  this.mergeScalar(other.getScalar());
               }

               if (other.hasObj()) {
                  this.mergeObj(other.getObj());
               }

               if (other.hasArray()) {
                  this.mergeArray(other.getArray());
               }

               this.mergeUnknownFields(other.getUnknownFields());
               return this;
            }
         }

         public final boolean isInitialized() {
            if (!this.hasType()) {
               return false;
            } else if (this.hasScalar() && !this.getScalar().isInitialized()) {
               return false;
            } else if (this.hasObj() && !this.getObj().isInitialized()) {
               return false;
            } else {
               return !this.hasArray() || this.getArray().isInitialized();
            }
         }

         public MysqlxDatatypes.Any.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxDatatypes.Any parsedMessage = null;

            try {
               parsedMessage = (MysqlxDatatypes.Any)MysqlxDatatypes.Any.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxDatatypes.Any)var8.getUnfinishedMessage();
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
         public MysqlxDatatypes.Any.Type getType() {
            return this.type_;
         }

         public MysqlxDatatypes.Any.Builder setType(MysqlxDatatypes.Any.Type value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 1;
               this.type_ = value;
               this.onChanged();
               return this;
            }
         }

         public MysqlxDatatypes.Any.Builder clearType() {
            this.bitField0_ &= -2;
            this.type_ = MysqlxDatatypes.Any.Type.SCALAR;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasScalar() {
            return (this.bitField0_ & 2) == 2;
         }

         @Override
         public MysqlxDatatypes.Scalar getScalar() {
            return this.scalarBuilder_ == null ? this.scalar_ : (MysqlxDatatypes.Scalar)this.scalarBuilder_.getMessage();
         }

         public MysqlxDatatypes.Any.Builder setScalar(MysqlxDatatypes.Scalar value) {
            if (this.scalarBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.scalar_ = value;
               this.onChanged();
            } else {
               this.scalarBuilder_.setMessage(value);
            }

            this.bitField0_ |= 2;
            return this;
         }

         public MysqlxDatatypes.Any.Builder setScalar(MysqlxDatatypes.Scalar.Builder builderForValue) {
            if (this.scalarBuilder_ == null) {
               this.scalar_ = builderForValue.build();
               this.onChanged();
            } else {
               this.scalarBuilder_.setMessage(builderForValue.build());
            }

            this.bitField0_ |= 2;
            return this;
         }

         public MysqlxDatatypes.Any.Builder mergeScalar(MysqlxDatatypes.Scalar value) {
            if (this.scalarBuilder_ == null) {
               if ((this.bitField0_ & 2) == 2 && this.scalar_ != MysqlxDatatypes.Scalar.getDefaultInstance()) {
                  this.scalar_ = MysqlxDatatypes.Scalar.newBuilder(this.scalar_).mergeFrom(value).buildPartial();
               } else {
                  this.scalar_ = value;
               }

               this.onChanged();
            } else {
               this.scalarBuilder_.mergeFrom(value);
            }

            this.bitField0_ |= 2;
            return this;
         }

         public MysqlxDatatypes.Any.Builder clearScalar() {
            if (this.scalarBuilder_ == null) {
               this.scalar_ = MysqlxDatatypes.Scalar.getDefaultInstance();
               this.onChanged();
            } else {
               this.scalarBuilder_.clear();
            }

            this.bitField0_ &= -3;
            return this;
         }

         public MysqlxDatatypes.Scalar.Builder getScalarBuilder() {
            this.bitField0_ |= 2;
            this.onChanged();
            return (MysqlxDatatypes.Scalar.Builder)this.getScalarFieldBuilder().getBuilder();
         }

         @Override
         public MysqlxDatatypes.ScalarOrBuilder getScalarOrBuilder() {
            return (MysqlxDatatypes.ScalarOrBuilder)(this.scalarBuilder_ != null
               ? (MysqlxDatatypes.ScalarOrBuilder)this.scalarBuilder_.getMessageOrBuilder()
               : this.scalar_);
         }

         private SingleFieldBuilder<MysqlxDatatypes.Scalar, MysqlxDatatypes.Scalar.Builder, MysqlxDatatypes.ScalarOrBuilder> getScalarFieldBuilder() {
            if (this.scalarBuilder_ == null) {
               this.scalarBuilder_ = new SingleFieldBuilder(this.getScalar(), this.getParentForChildren(), this.isClean());
               this.scalar_ = null;
            }

            return this.scalarBuilder_;
         }

         @Override
         public boolean hasObj() {
            return (this.bitField0_ & 4) == 4;
         }

         @Override
         public MysqlxDatatypes.Object getObj() {
            return this.objBuilder_ == null ? this.obj_ : (MysqlxDatatypes.Object)this.objBuilder_.getMessage();
         }

         public MysqlxDatatypes.Any.Builder setObj(MysqlxDatatypes.Object value) {
            if (this.objBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.obj_ = value;
               this.onChanged();
            } else {
               this.objBuilder_.setMessage(value);
            }

            this.bitField0_ |= 4;
            return this;
         }

         public MysqlxDatatypes.Any.Builder setObj(MysqlxDatatypes.Object.Builder builderForValue) {
            if (this.objBuilder_ == null) {
               this.obj_ = builderForValue.build();
               this.onChanged();
            } else {
               this.objBuilder_.setMessage(builderForValue.build());
            }

            this.bitField0_ |= 4;
            return this;
         }

         public MysqlxDatatypes.Any.Builder mergeObj(MysqlxDatatypes.Object value) {
            if (this.objBuilder_ == null) {
               if ((this.bitField0_ & 4) == 4 && this.obj_ != MysqlxDatatypes.Object.getDefaultInstance()) {
                  this.obj_ = MysqlxDatatypes.Object.newBuilder(this.obj_).mergeFrom(value).buildPartial();
               } else {
                  this.obj_ = value;
               }

               this.onChanged();
            } else {
               this.objBuilder_.mergeFrom(value);
            }

            this.bitField0_ |= 4;
            return this;
         }

         public MysqlxDatatypes.Any.Builder clearObj() {
            if (this.objBuilder_ == null) {
               this.obj_ = MysqlxDatatypes.Object.getDefaultInstance();
               this.onChanged();
            } else {
               this.objBuilder_.clear();
            }

            this.bitField0_ &= -5;
            return this;
         }

         public MysqlxDatatypes.Object.Builder getObjBuilder() {
            this.bitField0_ |= 4;
            this.onChanged();
            return (MysqlxDatatypes.Object.Builder)this.getObjFieldBuilder().getBuilder();
         }

         @Override
         public MysqlxDatatypes.ObjectOrBuilder getObjOrBuilder() {
            return (MysqlxDatatypes.ObjectOrBuilder)(this.objBuilder_ != null
               ? (MysqlxDatatypes.ObjectOrBuilder)this.objBuilder_.getMessageOrBuilder()
               : this.obj_);
         }

         private SingleFieldBuilder<MysqlxDatatypes.Object, MysqlxDatatypes.Object.Builder, MysqlxDatatypes.ObjectOrBuilder> getObjFieldBuilder() {
            if (this.objBuilder_ == null) {
               this.objBuilder_ = new SingleFieldBuilder(this.getObj(), this.getParentForChildren(), this.isClean());
               this.obj_ = null;
            }

            return this.objBuilder_;
         }

         @Override
         public boolean hasArray() {
            return (this.bitField0_ & 8) == 8;
         }

         @Override
         public MysqlxDatatypes.Array getArray() {
            return this.arrayBuilder_ == null ? this.array_ : (MysqlxDatatypes.Array)this.arrayBuilder_.getMessage();
         }

         public MysqlxDatatypes.Any.Builder setArray(MysqlxDatatypes.Array value) {
            if (this.arrayBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.array_ = value;
               this.onChanged();
            } else {
               this.arrayBuilder_.setMessage(value);
            }

            this.bitField0_ |= 8;
            return this;
         }

         public MysqlxDatatypes.Any.Builder setArray(MysqlxDatatypes.Array.Builder builderForValue) {
            if (this.arrayBuilder_ == null) {
               this.array_ = builderForValue.build();
               this.onChanged();
            } else {
               this.arrayBuilder_.setMessage(builderForValue.build());
            }

            this.bitField0_ |= 8;
            return this;
         }

         public MysqlxDatatypes.Any.Builder mergeArray(MysqlxDatatypes.Array value) {
            if (this.arrayBuilder_ == null) {
               if ((this.bitField0_ & 8) == 8 && this.array_ != MysqlxDatatypes.Array.getDefaultInstance()) {
                  this.array_ = MysqlxDatatypes.Array.newBuilder(this.array_).mergeFrom(value).buildPartial();
               } else {
                  this.array_ = value;
               }

               this.onChanged();
            } else {
               this.arrayBuilder_.mergeFrom(value);
            }

            this.bitField0_ |= 8;
            return this;
         }

         public MysqlxDatatypes.Any.Builder clearArray() {
            if (this.arrayBuilder_ == null) {
               this.array_ = MysqlxDatatypes.Array.getDefaultInstance();
               this.onChanged();
            } else {
               this.arrayBuilder_.clear();
            }

            this.bitField0_ &= -9;
            return this;
         }

         public MysqlxDatatypes.Array.Builder getArrayBuilder() {
            this.bitField0_ |= 8;
            this.onChanged();
            return (MysqlxDatatypes.Array.Builder)this.getArrayFieldBuilder().getBuilder();
         }

         @Override
         public MysqlxDatatypes.ArrayOrBuilder getArrayOrBuilder() {
            return (MysqlxDatatypes.ArrayOrBuilder)(this.arrayBuilder_ != null
               ? (MysqlxDatatypes.ArrayOrBuilder)this.arrayBuilder_.getMessageOrBuilder()
               : this.array_);
         }

         private SingleFieldBuilder<MysqlxDatatypes.Array, MysqlxDatatypes.Array.Builder, MysqlxDatatypes.ArrayOrBuilder> getArrayFieldBuilder() {
            if (this.arrayBuilder_ == null) {
               this.arrayBuilder_ = new SingleFieldBuilder(this.getArray(), this.getParentForChildren(), this.isClean());
               this.array_ = null;
            }

            return this.arrayBuilder_;
         }
      }

      public static enum Type implements ProtocolMessageEnum {
         SCALAR(0, 1),
         OBJECT(1, 2),
         ARRAY(2, 3);

         public static final int SCALAR_VALUE = 1;
         public static final int OBJECT_VALUE = 2;
         public static final int ARRAY_VALUE = 3;
         private static EnumLiteMap<MysqlxDatatypes.Any.Type> internalValueMap = new EnumLiteMap<MysqlxDatatypes.Any.Type>() {
            public MysqlxDatatypes.Any.Type findValueByNumber(int number) {
               return MysqlxDatatypes.Any.Type.valueOf(number);
            }
         };
         private static final MysqlxDatatypes.Any.Type[] VALUES = values();
         private final int index;
         private final int value;

         public final int getNumber() {
            return this.value;
         }

         public static MysqlxDatatypes.Any.Type valueOf(int value) {
            switch(value) {
               case 1:
                  return SCALAR;
               case 2:
                  return OBJECT;
               case 3:
                  return ARRAY;
               default:
                  return null;
            }
         }

         public static EnumLiteMap<MysqlxDatatypes.Any.Type> internalGetValueMap() {
            return internalValueMap;
         }

         public final EnumValueDescriptor getValueDescriptor() {
            return (EnumValueDescriptor)getDescriptor().getValues().get(this.index);
         }

         public final EnumDescriptor getDescriptorForType() {
            return getDescriptor();
         }

         public static final EnumDescriptor getDescriptor() {
            return (EnumDescriptor)MysqlxDatatypes.Any.getDescriptor().getEnumTypes().get(0);
         }

         public static MysqlxDatatypes.Any.Type valueOf(EnumValueDescriptor desc) {
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

   public interface AnyOrBuilder extends MessageOrBuilder {
      boolean hasType();

      MysqlxDatatypes.Any.Type getType();

      boolean hasScalar();

      MysqlxDatatypes.Scalar getScalar();

      MysqlxDatatypes.ScalarOrBuilder getScalarOrBuilder();

      boolean hasObj();

      MysqlxDatatypes.Object getObj();

      MysqlxDatatypes.ObjectOrBuilder getObjOrBuilder();

      boolean hasArray();

      MysqlxDatatypes.Array getArray();

      MysqlxDatatypes.ArrayOrBuilder getArrayOrBuilder();
   }

   public static final class Array extends GeneratedMessage implements MysqlxDatatypes.ArrayOrBuilder {
      private static final MysqlxDatatypes.Array defaultInstance = new MysqlxDatatypes.Array(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxDatatypes.Array> PARSER = new AbstractParser<MysqlxDatatypes.Array>() {
         public MysqlxDatatypes.Array parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxDatatypes.Array(input, extensionRegistry);
         }
      };
      public static final int VALUE_FIELD_NUMBER = 1;
      private List<MysqlxDatatypes.Any> value_;
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private Array(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private Array(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static MysqlxDatatypes.Array getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxDatatypes.Array getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private Array(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                        this.value_ = new ArrayList<>();
                        mutable_bitField0_ |= 1;
                     }

                     this.value_.add(input.readMessage(MysqlxDatatypes.Any.PARSER, extensionRegistry));
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
               this.value_ = Collections.unmodifiableList(this.value_);
            }

            this.unknownFields = unknownFields.build();
            this.makeExtensionsImmutable();
         }
      }

      public static final Descriptor getDescriptor() {
         return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Array_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Array_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxDatatypes.Array.class, MysqlxDatatypes.Array.Builder.class);
      }

      public Parser<MysqlxDatatypes.Array> getParserForType() {
         return PARSER;
      }

      @Override
      public List<MysqlxDatatypes.Any> getValueList() {
         return this.value_;
      }

      @Override
      public List<? extends MysqlxDatatypes.AnyOrBuilder> getValueOrBuilderList() {
         return this.value_;
      }

      @Override
      public int getValueCount() {
         return this.value_.size();
      }

      @Override
      public MysqlxDatatypes.Any getValue(int index) {
         return this.value_.get(index);
      }

      @Override
      public MysqlxDatatypes.AnyOrBuilder getValueOrBuilder(int index) {
         return this.value_.get(index);
      }

      private void initFields() {
         this.value_ = Collections.emptyList();
      }

      public final boolean isInitialized() {
         byte isInitialized = this.memoizedIsInitialized;
         if (isInitialized == 1) {
            return true;
         } else if (isInitialized == 0) {
            return false;
         } else {
            for(int i = 0; i < this.getValueCount(); ++i) {
               if (!this.getValue(i).isInitialized()) {
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

         for(int i = 0; i < this.value_.size(); ++i) {
            output.writeMessage(1, (MessageLite)this.value_.get(i));
         }

         this.getUnknownFields().writeTo(output);
      }

      public int getSerializedSize() {
         int size = this.memoizedSerializedSize;
         if (size != -1) {
            return size;
         } else {
            size = 0;

            for(int i = 0; i < this.value_.size(); ++i) {
               size += CodedOutputStream.computeMessageSize(1, (MessageLite)this.value_.get(i));
            }

            size += this.getUnknownFields().getSerializedSize();
            this.memoizedSerializedSize = size;
            return size;
         }
      }

      protected java.lang.Object writeReplace() throws ObjectStreamException {
         return super.writeReplace();
      }

      public static MysqlxDatatypes.Array parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Array)PARSER.parseFrom(data);
      }

      public static MysqlxDatatypes.Array parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Array)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxDatatypes.Array parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Array)PARSER.parseFrom(data);
      }

      public static MysqlxDatatypes.Array parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Array)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxDatatypes.Array parseFrom(InputStream input) throws IOException {
         return (MysqlxDatatypes.Array)PARSER.parseFrom(input);
      }

      public static MysqlxDatatypes.Array parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxDatatypes.Array)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxDatatypes.Array parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxDatatypes.Array)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxDatatypes.Array parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxDatatypes.Array)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxDatatypes.Array parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxDatatypes.Array)PARSER.parseFrom(input);
      }

      public static MysqlxDatatypes.Array parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxDatatypes.Array)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxDatatypes.Array.Builder newBuilder() {
         return MysqlxDatatypes.Array.Builder.create();
      }

      public MysqlxDatatypes.Array.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxDatatypes.Array.Builder newBuilder(MysqlxDatatypes.Array prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxDatatypes.Array.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxDatatypes.Array.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxDatatypes.Array.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxDatatypes.Array.Builder>
         implements MysqlxDatatypes.ArrayOrBuilder {
         private int bitField0_;
         private List<MysqlxDatatypes.Any> value_ = Collections.emptyList();
         private RepeatedFieldBuilder<MysqlxDatatypes.Any, MysqlxDatatypes.Any.Builder, MysqlxDatatypes.AnyOrBuilder> valueBuilder_;

         public static final Descriptor getDescriptor() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Array_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Array_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxDatatypes.Array.class, MysqlxDatatypes.Array.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxDatatypes.Array.alwaysUseFieldBuilders) {
               this.getValueFieldBuilder();
            }
         }

         private static MysqlxDatatypes.Array.Builder create() {
            return new MysqlxDatatypes.Array.Builder();
         }

         public MysqlxDatatypes.Array.Builder clear() {
            super.clear();
            if (this.valueBuilder_ == null) {
               this.value_ = Collections.emptyList();
               this.bitField0_ &= -2;
            } else {
               this.valueBuilder_.clear();
            }

            return this;
         }

         public MysqlxDatatypes.Array.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Array_descriptor;
         }

         public MysqlxDatatypes.Array getDefaultInstanceForType() {
            return MysqlxDatatypes.Array.getDefaultInstance();
         }

         public MysqlxDatatypes.Array build() {
            MysqlxDatatypes.Array result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxDatatypes.Array buildPartial() {
            MysqlxDatatypes.Array result = new MysqlxDatatypes.Array(this);
            int from_bitField0_ = this.bitField0_;
            if (this.valueBuilder_ == null) {
               if ((this.bitField0_ & 1) == 1) {
                  this.value_ = Collections.unmodifiableList(this.value_);
                  this.bitField0_ &= -2;
               }

               result.value_ = this.value_;
            } else {
               result.value_ = this.valueBuilder_.build();
            }

            this.onBuilt();
            return result;
         }

         public MysqlxDatatypes.Array.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxDatatypes.Array) {
               return this.mergeFrom((MysqlxDatatypes.Array)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxDatatypes.Array.Builder mergeFrom(MysqlxDatatypes.Array other) {
            if (other == MysqlxDatatypes.Array.getDefaultInstance()) {
               return this;
            } else {
               if (this.valueBuilder_ == null) {
                  if (!other.value_.isEmpty()) {
                     if (this.value_.isEmpty()) {
                        this.value_ = other.value_;
                        this.bitField0_ &= -2;
                     } else {
                        this.ensureValueIsMutable();
                        this.value_.addAll(other.value_);
                     }

                     this.onChanged();
                  }
               } else if (!other.value_.isEmpty()) {
                  if (this.valueBuilder_.isEmpty()) {
                     this.valueBuilder_.dispose();
                     this.valueBuilder_ = null;
                     this.value_ = other.value_;
                     this.bitField0_ &= -2;
                     this.valueBuilder_ = MysqlxDatatypes.Array.alwaysUseFieldBuilders ? this.getValueFieldBuilder() : null;
                  } else {
                     this.valueBuilder_.addAllMessages(other.value_);
                  }
               }

               this.mergeUnknownFields(other.getUnknownFields());
               return this;
            }
         }

         public final boolean isInitialized() {
            for(int i = 0; i < this.getValueCount(); ++i) {
               if (!this.getValue(i).isInitialized()) {
                  return false;
               }
            }

            return true;
         }

         public MysqlxDatatypes.Array.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxDatatypes.Array parsedMessage = null;

            try {
               parsedMessage = (MysqlxDatatypes.Array)MysqlxDatatypes.Array.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxDatatypes.Array)var8.getUnfinishedMessage();
               throw var8;
            } finally {
               if (parsedMessage != null) {
                  this.mergeFrom(parsedMessage);
               }
            }

            return this;
         }

         private void ensureValueIsMutable() {
            if ((this.bitField0_ & 1) != 1) {
               this.value_ = new ArrayList<>(this.value_);
               this.bitField0_ |= 1;
            }
         }

         @Override
         public List<MysqlxDatatypes.Any> getValueList() {
            return this.valueBuilder_ == null ? Collections.unmodifiableList(this.value_) : this.valueBuilder_.getMessageList();
         }

         @Override
         public int getValueCount() {
            return this.valueBuilder_ == null ? this.value_.size() : this.valueBuilder_.getCount();
         }

         @Override
         public MysqlxDatatypes.Any getValue(int index) {
            return this.valueBuilder_ == null ? this.value_.get(index) : (MysqlxDatatypes.Any)this.valueBuilder_.getMessage(index);
         }

         public MysqlxDatatypes.Array.Builder setValue(int index, MysqlxDatatypes.Any value) {
            if (this.valueBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureValueIsMutable();
               this.value_.set(index, value);
               this.onChanged();
            } else {
               this.valueBuilder_.setMessage(index, value);
            }

            return this;
         }

         public MysqlxDatatypes.Array.Builder setValue(int index, MysqlxDatatypes.Any.Builder builderForValue) {
            if (this.valueBuilder_ == null) {
               this.ensureValueIsMutable();
               this.value_.set(index, builderForValue.build());
               this.onChanged();
            } else {
               this.valueBuilder_.setMessage(index, builderForValue.build());
            }

            return this;
         }

         public MysqlxDatatypes.Array.Builder addValue(MysqlxDatatypes.Any value) {
            if (this.valueBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureValueIsMutable();
               this.value_.add(value);
               this.onChanged();
            } else {
               this.valueBuilder_.addMessage(value);
            }

            return this;
         }

         public MysqlxDatatypes.Array.Builder addValue(int index, MysqlxDatatypes.Any value) {
            if (this.valueBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureValueIsMutable();
               this.value_.add(index, value);
               this.onChanged();
            } else {
               this.valueBuilder_.addMessage(index, value);
            }

            return this;
         }

         public MysqlxDatatypes.Array.Builder addValue(MysqlxDatatypes.Any.Builder builderForValue) {
            if (this.valueBuilder_ == null) {
               this.ensureValueIsMutable();
               this.value_.add(builderForValue.build());
               this.onChanged();
            } else {
               this.valueBuilder_.addMessage(builderForValue.build());
            }

            return this;
         }

         public MysqlxDatatypes.Array.Builder addValue(int index, MysqlxDatatypes.Any.Builder builderForValue) {
            if (this.valueBuilder_ == null) {
               this.ensureValueIsMutable();
               this.value_.add(index, builderForValue.build());
               this.onChanged();
            } else {
               this.valueBuilder_.addMessage(index, builderForValue.build());
            }

            return this;
         }

         public MysqlxDatatypes.Array.Builder addAllValue(Iterable<? extends MysqlxDatatypes.Any> values) {
            if (this.valueBuilder_ == null) {
               this.ensureValueIsMutable();
               com.google.protobuf.AbstractMessageLite.Builder.addAll(values, this.value_);
               this.onChanged();
            } else {
               this.valueBuilder_.addAllMessages(values);
            }

            return this;
         }

         public MysqlxDatatypes.Array.Builder clearValue() {
            if (this.valueBuilder_ == null) {
               this.value_ = Collections.emptyList();
               this.bitField0_ &= -2;
               this.onChanged();
            } else {
               this.valueBuilder_.clear();
            }

            return this;
         }

         public MysqlxDatatypes.Array.Builder removeValue(int index) {
            if (this.valueBuilder_ == null) {
               this.ensureValueIsMutable();
               this.value_.remove(index);
               this.onChanged();
            } else {
               this.valueBuilder_.remove(index);
            }

            return this;
         }

         public MysqlxDatatypes.Any.Builder getValueBuilder(int index) {
            return (MysqlxDatatypes.Any.Builder)this.getValueFieldBuilder().getBuilder(index);
         }

         @Override
         public MysqlxDatatypes.AnyOrBuilder getValueOrBuilder(int index) {
            return this.valueBuilder_ == null ? this.value_.get(index) : (MysqlxDatatypes.AnyOrBuilder)this.valueBuilder_.getMessageOrBuilder(index);
         }

         @Override
         public List<? extends MysqlxDatatypes.AnyOrBuilder> getValueOrBuilderList() {
            return this.valueBuilder_ != null ? this.valueBuilder_.getMessageOrBuilderList() : Collections.unmodifiableList(this.value_);
         }

         public MysqlxDatatypes.Any.Builder addValueBuilder() {
            return (MysqlxDatatypes.Any.Builder)this.getValueFieldBuilder().addBuilder(MysqlxDatatypes.Any.getDefaultInstance());
         }

         public MysqlxDatatypes.Any.Builder addValueBuilder(int index) {
            return (MysqlxDatatypes.Any.Builder)this.getValueFieldBuilder().addBuilder(index, MysqlxDatatypes.Any.getDefaultInstance());
         }

         public List<MysqlxDatatypes.Any.Builder> getValueBuilderList() {
            return this.getValueFieldBuilder().getBuilderList();
         }

         private RepeatedFieldBuilder<MysqlxDatatypes.Any, MysqlxDatatypes.Any.Builder, MysqlxDatatypes.AnyOrBuilder> getValueFieldBuilder() {
            if (this.valueBuilder_ == null) {
               this.valueBuilder_ = new RepeatedFieldBuilder(this.value_, (this.bitField0_ & 1) == 1, this.getParentForChildren(), this.isClean());
               this.value_ = null;
            }

            return this.valueBuilder_;
         }
      }
   }

   public interface ArrayOrBuilder extends MessageOrBuilder {
      List<MysqlxDatatypes.Any> getValueList();

      MysqlxDatatypes.Any getValue(int var1);

      int getValueCount();

      List<? extends MysqlxDatatypes.AnyOrBuilder> getValueOrBuilderList();

      MysqlxDatatypes.AnyOrBuilder getValueOrBuilder(int var1);
   }

   public static final class Object extends GeneratedMessage implements MysqlxDatatypes.ObjectOrBuilder {
      private static final MysqlxDatatypes.Object defaultInstance = new MysqlxDatatypes.Object(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxDatatypes.Object> PARSER = new AbstractParser<MysqlxDatatypes.Object>() {
         public MysqlxDatatypes.Object parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxDatatypes.Object(input, extensionRegistry);
         }
      };
      public static final int FLD_FIELD_NUMBER = 1;
      private List<MysqlxDatatypes.Object.ObjectField> fld_;
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private Object(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private Object(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static MysqlxDatatypes.Object getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxDatatypes.Object getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private Object(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                        this.fld_ = new ArrayList<>();
                        mutable_bitField0_ |= 1;
                     }

                     this.fld_.add(input.readMessage(MysqlxDatatypes.Object.ObjectField.PARSER, extensionRegistry));
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
               this.fld_ = Collections.unmodifiableList(this.fld_);
            }

            this.unknownFields = unknownFields.build();
            this.makeExtensionsImmutable();
         }
      }

      public static final Descriptor getDescriptor() {
         return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Object_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Object_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxDatatypes.Object.class, MysqlxDatatypes.Object.Builder.class);
      }

      public Parser<MysqlxDatatypes.Object> getParserForType() {
         return PARSER;
      }

      @Override
      public List<MysqlxDatatypes.Object.ObjectField> getFldList() {
         return this.fld_;
      }

      @Override
      public List<? extends MysqlxDatatypes.Object.ObjectFieldOrBuilder> getFldOrBuilderList() {
         return this.fld_;
      }

      @Override
      public int getFldCount() {
         return this.fld_.size();
      }

      @Override
      public MysqlxDatatypes.Object.ObjectField getFld(int index) {
         return this.fld_.get(index);
      }

      @Override
      public MysqlxDatatypes.Object.ObjectFieldOrBuilder getFldOrBuilder(int index) {
         return this.fld_.get(index);
      }

      private void initFields() {
         this.fld_ = Collections.emptyList();
      }

      public final boolean isInitialized() {
         byte isInitialized = this.memoizedIsInitialized;
         if (isInitialized == 1) {
            return true;
         } else if (isInitialized == 0) {
            return false;
         } else {
            for(int i = 0; i < this.getFldCount(); ++i) {
               if (!this.getFld(i).isInitialized()) {
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

         for(int i = 0; i < this.fld_.size(); ++i) {
            output.writeMessage(1, (MessageLite)this.fld_.get(i));
         }

         this.getUnknownFields().writeTo(output);
      }

      public int getSerializedSize() {
         int size = this.memoizedSerializedSize;
         if (size != -1) {
            return size;
         } else {
            size = 0;

            for(int i = 0; i < this.fld_.size(); ++i) {
               size += CodedOutputStream.computeMessageSize(1, (MessageLite)this.fld_.get(i));
            }

            size += this.getUnknownFields().getSerializedSize();
            this.memoizedSerializedSize = size;
            return size;
         }
      }

      protected java.lang.Object writeReplace() throws ObjectStreamException {
         return super.writeReplace();
      }

      public static MysqlxDatatypes.Object parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Object)PARSER.parseFrom(data);
      }

      public static MysqlxDatatypes.Object parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Object)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxDatatypes.Object parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Object)PARSER.parseFrom(data);
      }

      public static MysqlxDatatypes.Object parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Object)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxDatatypes.Object parseFrom(InputStream input) throws IOException {
         return (MysqlxDatatypes.Object)PARSER.parseFrom(input);
      }

      public static MysqlxDatatypes.Object parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxDatatypes.Object)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxDatatypes.Object parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxDatatypes.Object)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxDatatypes.Object parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxDatatypes.Object)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxDatatypes.Object parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxDatatypes.Object)PARSER.parseFrom(input);
      }

      public static MysqlxDatatypes.Object parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxDatatypes.Object)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxDatatypes.Object.Builder newBuilder() {
         return MysqlxDatatypes.Object.Builder.create();
      }

      public MysqlxDatatypes.Object.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxDatatypes.Object.Builder newBuilder(MysqlxDatatypes.Object prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxDatatypes.Object.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxDatatypes.Object.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxDatatypes.Object.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxDatatypes.Object.Builder>
         implements MysqlxDatatypes.ObjectOrBuilder {
         private int bitField0_;
         private List<MysqlxDatatypes.Object.ObjectField> fld_ = Collections.emptyList();
         private RepeatedFieldBuilder<MysqlxDatatypes.Object.ObjectField, MysqlxDatatypes.Object.ObjectField.Builder, MysqlxDatatypes.Object.ObjectFieldOrBuilder> fldBuilder_;

         public static final Descriptor getDescriptor() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Object_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Object_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxDatatypes.Object.class, MysqlxDatatypes.Object.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxDatatypes.Object.alwaysUseFieldBuilders) {
               this.getFldFieldBuilder();
            }
         }

         private static MysqlxDatatypes.Object.Builder create() {
            return new MysqlxDatatypes.Object.Builder();
         }

         public MysqlxDatatypes.Object.Builder clear() {
            super.clear();
            if (this.fldBuilder_ == null) {
               this.fld_ = Collections.emptyList();
               this.bitField0_ &= -2;
            } else {
               this.fldBuilder_.clear();
            }

            return this;
         }

         public MysqlxDatatypes.Object.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Object_descriptor;
         }

         public MysqlxDatatypes.Object getDefaultInstanceForType() {
            return MysqlxDatatypes.Object.getDefaultInstance();
         }

         public MysqlxDatatypes.Object build() {
            MysqlxDatatypes.Object result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxDatatypes.Object buildPartial() {
            MysqlxDatatypes.Object result = new MysqlxDatatypes.Object(this);
            int from_bitField0_ = this.bitField0_;
            if (this.fldBuilder_ == null) {
               if ((this.bitField0_ & 1) == 1) {
                  this.fld_ = Collections.unmodifiableList(this.fld_);
                  this.bitField0_ &= -2;
               }

               result.fld_ = this.fld_;
            } else {
               result.fld_ = this.fldBuilder_.build();
            }

            this.onBuilt();
            return result;
         }

         public MysqlxDatatypes.Object.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxDatatypes.Object) {
               return this.mergeFrom((MysqlxDatatypes.Object)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxDatatypes.Object.Builder mergeFrom(MysqlxDatatypes.Object other) {
            if (other == MysqlxDatatypes.Object.getDefaultInstance()) {
               return this;
            } else {
               if (this.fldBuilder_ == null) {
                  if (!other.fld_.isEmpty()) {
                     if (this.fld_.isEmpty()) {
                        this.fld_ = other.fld_;
                        this.bitField0_ &= -2;
                     } else {
                        this.ensureFldIsMutable();
                        this.fld_.addAll(other.fld_);
                     }

                     this.onChanged();
                  }
               } else if (!other.fld_.isEmpty()) {
                  if (this.fldBuilder_.isEmpty()) {
                     this.fldBuilder_.dispose();
                     this.fldBuilder_ = null;
                     this.fld_ = other.fld_;
                     this.bitField0_ &= -2;
                     this.fldBuilder_ = MysqlxDatatypes.Object.alwaysUseFieldBuilders ? this.getFldFieldBuilder() : null;
                  } else {
                     this.fldBuilder_.addAllMessages(other.fld_);
                  }
               }

               this.mergeUnknownFields(other.getUnknownFields());
               return this;
            }
         }

         public final boolean isInitialized() {
            for(int i = 0; i < this.getFldCount(); ++i) {
               if (!this.getFld(i).isInitialized()) {
                  return false;
               }
            }

            return true;
         }

         public MysqlxDatatypes.Object.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxDatatypes.Object parsedMessage = null;

            try {
               parsedMessage = (MysqlxDatatypes.Object)MysqlxDatatypes.Object.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxDatatypes.Object)var8.getUnfinishedMessage();
               throw var8;
            } finally {
               if (parsedMessage != null) {
                  this.mergeFrom(parsedMessage);
               }
            }

            return this;
         }

         private void ensureFldIsMutable() {
            if ((this.bitField0_ & 1) != 1) {
               this.fld_ = new ArrayList<>(this.fld_);
               this.bitField0_ |= 1;
            }
         }

         @Override
         public List<MysqlxDatatypes.Object.ObjectField> getFldList() {
            return this.fldBuilder_ == null ? Collections.unmodifiableList(this.fld_) : this.fldBuilder_.getMessageList();
         }

         @Override
         public int getFldCount() {
            return this.fldBuilder_ == null ? this.fld_.size() : this.fldBuilder_.getCount();
         }

         @Override
         public MysqlxDatatypes.Object.ObjectField getFld(int index) {
            return this.fldBuilder_ == null ? this.fld_.get(index) : (MysqlxDatatypes.Object.ObjectField)this.fldBuilder_.getMessage(index);
         }

         public MysqlxDatatypes.Object.Builder setFld(int index, MysqlxDatatypes.Object.ObjectField value) {
            if (this.fldBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureFldIsMutable();
               this.fld_.set(index, value);
               this.onChanged();
            } else {
               this.fldBuilder_.setMessage(index, value);
            }

            return this;
         }

         public MysqlxDatatypes.Object.Builder setFld(int index, MysqlxDatatypes.Object.ObjectField.Builder builderForValue) {
            if (this.fldBuilder_ == null) {
               this.ensureFldIsMutable();
               this.fld_.set(index, builderForValue.build());
               this.onChanged();
            } else {
               this.fldBuilder_.setMessage(index, builderForValue.build());
            }

            return this;
         }

         public MysqlxDatatypes.Object.Builder addFld(MysqlxDatatypes.Object.ObjectField value) {
            if (this.fldBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureFldIsMutable();
               this.fld_.add(value);
               this.onChanged();
            } else {
               this.fldBuilder_.addMessage(value);
            }

            return this;
         }

         public MysqlxDatatypes.Object.Builder addFld(int index, MysqlxDatatypes.Object.ObjectField value) {
            if (this.fldBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureFldIsMutable();
               this.fld_.add(index, value);
               this.onChanged();
            } else {
               this.fldBuilder_.addMessage(index, value);
            }

            return this;
         }

         public MysqlxDatatypes.Object.Builder addFld(MysqlxDatatypes.Object.ObjectField.Builder builderForValue) {
            if (this.fldBuilder_ == null) {
               this.ensureFldIsMutable();
               this.fld_.add(builderForValue.build());
               this.onChanged();
            } else {
               this.fldBuilder_.addMessage(builderForValue.build());
            }

            return this;
         }

         public MysqlxDatatypes.Object.Builder addFld(int index, MysqlxDatatypes.Object.ObjectField.Builder builderForValue) {
            if (this.fldBuilder_ == null) {
               this.ensureFldIsMutable();
               this.fld_.add(index, builderForValue.build());
               this.onChanged();
            } else {
               this.fldBuilder_.addMessage(index, builderForValue.build());
            }

            return this;
         }

         public MysqlxDatatypes.Object.Builder addAllFld(Iterable<? extends MysqlxDatatypes.Object.ObjectField> values) {
            if (this.fldBuilder_ == null) {
               this.ensureFldIsMutable();
               com.google.protobuf.AbstractMessageLite.Builder.addAll(values, this.fld_);
               this.onChanged();
            } else {
               this.fldBuilder_.addAllMessages(values);
            }

            return this;
         }

         public MysqlxDatatypes.Object.Builder clearFld() {
            if (this.fldBuilder_ == null) {
               this.fld_ = Collections.emptyList();
               this.bitField0_ &= -2;
               this.onChanged();
            } else {
               this.fldBuilder_.clear();
            }

            return this;
         }

         public MysqlxDatatypes.Object.Builder removeFld(int index) {
            if (this.fldBuilder_ == null) {
               this.ensureFldIsMutable();
               this.fld_.remove(index);
               this.onChanged();
            } else {
               this.fldBuilder_.remove(index);
            }

            return this;
         }

         public MysqlxDatatypes.Object.ObjectField.Builder getFldBuilder(int index) {
            return (MysqlxDatatypes.Object.ObjectField.Builder)this.getFldFieldBuilder().getBuilder(index);
         }

         @Override
         public MysqlxDatatypes.Object.ObjectFieldOrBuilder getFldOrBuilder(int index) {
            return this.fldBuilder_ == null ? this.fld_.get(index) : (MysqlxDatatypes.Object.ObjectFieldOrBuilder)this.fldBuilder_.getMessageOrBuilder(index);
         }

         @Override
         public List<? extends MysqlxDatatypes.Object.ObjectFieldOrBuilder> getFldOrBuilderList() {
            return this.fldBuilder_ != null ? this.fldBuilder_.getMessageOrBuilderList() : Collections.unmodifiableList(this.fld_);
         }

         public MysqlxDatatypes.Object.ObjectField.Builder addFldBuilder() {
            return (MysqlxDatatypes.Object.ObjectField.Builder)this.getFldFieldBuilder().addBuilder(MysqlxDatatypes.Object.ObjectField.getDefaultInstance());
         }

         public MysqlxDatatypes.Object.ObjectField.Builder addFldBuilder(int index) {
            return (MysqlxDatatypes.Object.ObjectField.Builder)this.getFldFieldBuilder()
               .addBuilder(index, MysqlxDatatypes.Object.ObjectField.getDefaultInstance());
         }

         public List<MysqlxDatatypes.Object.ObjectField.Builder> getFldBuilderList() {
            return this.getFldFieldBuilder().getBuilderList();
         }

         private RepeatedFieldBuilder<MysqlxDatatypes.Object.ObjectField, MysqlxDatatypes.Object.ObjectField.Builder, MysqlxDatatypes.Object.ObjectFieldOrBuilder> getFldFieldBuilder() {
            if (this.fldBuilder_ == null) {
               this.fldBuilder_ = new RepeatedFieldBuilder(this.fld_, (this.bitField0_ & 1) == 1, this.getParentForChildren(), this.isClean());
               this.fld_ = null;
            }

            return this.fldBuilder_;
         }
      }

      public static final class ObjectField extends GeneratedMessage implements MysqlxDatatypes.Object.ObjectFieldOrBuilder {
         private static final MysqlxDatatypes.Object.ObjectField defaultInstance = new MysqlxDatatypes.Object.ObjectField(true);
         private final UnknownFieldSet unknownFields;
         public static Parser<MysqlxDatatypes.Object.ObjectField> PARSER = new AbstractParser<MysqlxDatatypes.Object.ObjectField>() {
            public MysqlxDatatypes.Object.ObjectField parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
               return new MysqlxDatatypes.Object.ObjectField(input, extensionRegistry);
            }
         };
         private int bitField0_;
         public static final int KEY_FIELD_NUMBER = 1;
         private java.lang.Object key_;
         public static final int VALUE_FIELD_NUMBER = 2;
         private MysqlxDatatypes.Any value_;
         private byte memoizedIsInitialized = -1;
         private int memoizedSerializedSize = -1;
         private static final long serialVersionUID = 0L;

         private ObjectField(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
            super(builder);
            this.unknownFields = builder.getUnknownFields();
         }

         private ObjectField(boolean noInit) {
            this.unknownFields = UnknownFieldSet.getDefaultInstance();
         }

         public static MysqlxDatatypes.Object.ObjectField getDefaultInstance() {
            return defaultInstance;
         }

         public MysqlxDatatypes.Object.ObjectField getDefaultInstanceForType() {
            return defaultInstance;
         }

         public final UnknownFieldSet getUnknownFields() {
            return this.unknownFields;
         }

         private ObjectField(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                        this.key_ = bs;
                        break;
                     case 18:
                        MysqlxDatatypes.Any.Builder subBuilder = null;
                        if ((this.bitField0_ & 2) == 2) {
                           subBuilder = this.value_.toBuilder();
                        }

                        this.value_ = (MysqlxDatatypes.Any)input.readMessage(MysqlxDatatypes.Any.PARSER, extensionRegistry);
                        if (subBuilder != null) {
                           subBuilder.mergeFrom(this.value_);
                           this.value_ = subBuilder.buildPartial();
                        }

                        this.bitField0_ |= 2;
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
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Object_ObjectField_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Object_ObjectField_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxDatatypes.Object.ObjectField.class, MysqlxDatatypes.Object.ObjectField.Builder.class);
         }

         public Parser<MysqlxDatatypes.Object.ObjectField> getParserForType() {
            return PARSER;
         }

         @Override
         public boolean hasKey() {
            return (this.bitField0_ & 1) == 1;
         }

         @Override
         public java.lang.String getKey() {
            java.lang.Object ref = this.key_;
            if (ref instanceof java.lang.String) {
               return (java.lang.String)ref;
            } else {
               ByteString bs = (ByteString)ref;
               java.lang.String s = bs.toStringUtf8();
               if (bs.isValidUtf8()) {
                  this.key_ = s;
               }

               return s;
            }
         }

         @Override
         public ByteString getKeyBytes() {
            java.lang.Object ref = this.key_;
            if (ref instanceof java.lang.String) {
               ByteString b = ByteString.copyFromUtf8((java.lang.String)ref);
               this.key_ = b;
               return b;
            } else {
               return (ByteString)ref;
            }
         }

         @Override
         public boolean hasValue() {
            return (this.bitField0_ & 2) == 2;
         }

         @Override
         public MysqlxDatatypes.Any getValue() {
            return this.value_;
         }

         @Override
         public MysqlxDatatypes.AnyOrBuilder getValueOrBuilder() {
            return this.value_;
         }

         private void initFields() {
            this.key_ = "";
            this.value_ = MysqlxDatatypes.Any.getDefaultInstance();
         }

         public final boolean isInitialized() {
            byte isInitialized = this.memoizedIsInitialized;
            if (isInitialized == 1) {
               return true;
            } else if (isInitialized == 0) {
               return false;
            } else if (!this.hasKey()) {
               this.memoizedIsInitialized = 0;
               return false;
            } else if (!this.hasValue()) {
               this.memoizedIsInitialized = 0;
               return false;
            } else if (!this.getValue().isInitialized()) {
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
               output.writeBytes(1, this.getKeyBytes());
            }

            if ((this.bitField0_ & 2) == 2) {
               output.writeMessage(2, this.value_);
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
                  size += CodedOutputStream.computeBytesSize(1, this.getKeyBytes());
               }

               if ((this.bitField0_ & 2) == 2) {
                  size += CodedOutputStream.computeMessageSize(2, this.value_);
               }

               size += this.getUnknownFields().getSerializedSize();
               this.memoizedSerializedSize = size;
               return size;
            }
         }

         protected java.lang.Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
         }

         public static MysqlxDatatypes.Object.ObjectField parseFrom(ByteString data) throws InvalidProtocolBufferException {
            return (MysqlxDatatypes.Object.ObjectField)PARSER.parseFrom(data);
         }

         public static MysqlxDatatypes.Object.ObjectField parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (MysqlxDatatypes.Object.ObjectField)PARSER.parseFrom(data, extensionRegistry);
         }

         public static MysqlxDatatypes.Object.ObjectField parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return (MysqlxDatatypes.Object.ObjectField)PARSER.parseFrom(data);
         }

         public static MysqlxDatatypes.Object.ObjectField parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (MysqlxDatatypes.Object.ObjectField)PARSER.parseFrom(data, extensionRegistry);
         }

         public static MysqlxDatatypes.Object.ObjectField parseFrom(InputStream input) throws IOException {
            return (MysqlxDatatypes.Object.ObjectField)PARSER.parseFrom(input);
         }

         public static MysqlxDatatypes.Object.ObjectField parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (MysqlxDatatypes.Object.ObjectField)PARSER.parseFrom(input, extensionRegistry);
         }

         public static MysqlxDatatypes.Object.ObjectField parseDelimitedFrom(InputStream input) throws IOException {
            return (MysqlxDatatypes.Object.ObjectField)PARSER.parseDelimitedFrom(input);
         }

         public static MysqlxDatatypes.Object.ObjectField parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (MysqlxDatatypes.Object.ObjectField)PARSER.parseDelimitedFrom(input, extensionRegistry);
         }

         public static MysqlxDatatypes.Object.ObjectField parseFrom(CodedInputStream input) throws IOException {
            return (MysqlxDatatypes.Object.ObjectField)PARSER.parseFrom(input);
         }

         public static MysqlxDatatypes.Object.ObjectField parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (MysqlxDatatypes.Object.ObjectField)PARSER.parseFrom(input, extensionRegistry);
         }

         public static MysqlxDatatypes.Object.ObjectField.Builder newBuilder() {
            return MysqlxDatatypes.Object.ObjectField.Builder.create();
         }

         public MysqlxDatatypes.Object.ObjectField.Builder newBuilderForType() {
            return newBuilder();
         }

         public static MysqlxDatatypes.Object.ObjectField.Builder newBuilder(MysqlxDatatypes.Object.ObjectField prototype) {
            return newBuilder().mergeFrom(prototype);
         }

         public MysqlxDatatypes.Object.ObjectField.Builder toBuilder() {
            return newBuilder(this);
         }

         protected MysqlxDatatypes.Object.ObjectField.Builder newBuilderForType(BuilderParent parent) {
            return new MysqlxDatatypes.Object.ObjectField.Builder(parent);
         }

         static {
            defaultInstance.initFields();
         }

         public static final class Builder
            extends com.google.protobuf.GeneratedMessage.Builder<MysqlxDatatypes.Object.ObjectField.Builder>
            implements MysqlxDatatypes.Object.ObjectFieldOrBuilder {
            private int bitField0_;
            private java.lang.Object key_ = "";
            private MysqlxDatatypes.Any value_ = MysqlxDatatypes.Any.getDefaultInstance();
            private SingleFieldBuilder<MysqlxDatatypes.Any, MysqlxDatatypes.Any.Builder, MysqlxDatatypes.AnyOrBuilder> valueBuilder_;

            public static final Descriptor getDescriptor() {
               return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Object_ObjectField_descriptor;
            }

            protected FieldAccessorTable internalGetFieldAccessorTable() {
               return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Object_ObjectField_fieldAccessorTable
                  .ensureFieldAccessorsInitialized(MysqlxDatatypes.Object.ObjectField.class, MysqlxDatatypes.Object.ObjectField.Builder.class);
            }

            private Builder() {
               this.maybeForceBuilderInitialization();
            }

            private Builder(BuilderParent parent) {
               super(parent);
               this.maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
               if (MysqlxDatatypes.Object.ObjectField.alwaysUseFieldBuilders) {
                  this.getValueFieldBuilder();
               }
            }

            private static MysqlxDatatypes.Object.ObjectField.Builder create() {
               return new MysqlxDatatypes.Object.ObjectField.Builder();
            }

            public MysqlxDatatypes.Object.ObjectField.Builder clear() {
               super.clear();
               this.key_ = "";
               this.bitField0_ &= -2;
               if (this.valueBuilder_ == null) {
                  this.value_ = MysqlxDatatypes.Any.getDefaultInstance();
               } else {
                  this.valueBuilder_.clear();
               }

               this.bitField0_ &= -3;
               return this;
            }

            public MysqlxDatatypes.Object.ObjectField.Builder clone() {
               return create().mergeFrom(this.buildPartial());
            }

            public Descriptor getDescriptorForType() {
               return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Object_ObjectField_descriptor;
            }

            public MysqlxDatatypes.Object.ObjectField getDefaultInstanceForType() {
               return MysqlxDatatypes.Object.ObjectField.getDefaultInstance();
            }

            public MysqlxDatatypes.Object.ObjectField build() {
               MysqlxDatatypes.Object.ObjectField result = this.buildPartial();
               if (!result.isInitialized()) {
                  throw newUninitializedMessageException(result);
               } else {
                  return result;
               }
            }

            public MysqlxDatatypes.Object.ObjectField buildPartial() {
               MysqlxDatatypes.Object.ObjectField result = new MysqlxDatatypes.Object.ObjectField(this);
               int from_bitField0_ = this.bitField0_;
               int to_bitField0_ = 0;
               if ((from_bitField0_ & 1) == 1) {
                  to_bitField0_ |= 1;
               }

               result.key_ = this.key_;
               if ((from_bitField0_ & 2) == 2) {
                  to_bitField0_ |= 2;
               }

               if (this.valueBuilder_ == null) {
                  result.value_ = this.value_;
               } else {
                  result.value_ = (MysqlxDatatypes.Any)this.valueBuilder_.build();
               }

               result.bitField0_ = to_bitField0_;
               this.onBuilt();
               return result;
            }

            public MysqlxDatatypes.Object.ObjectField.Builder mergeFrom(Message other) {
               if (other instanceof MysqlxDatatypes.Object.ObjectField) {
                  return this.mergeFrom((MysqlxDatatypes.Object.ObjectField)other);
               } else {
                  super.mergeFrom(other);
                  return this;
               }
            }

            public MysqlxDatatypes.Object.ObjectField.Builder mergeFrom(MysqlxDatatypes.Object.ObjectField other) {
               if (other == MysqlxDatatypes.Object.ObjectField.getDefaultInstance()) {
                  return this;
               } else {
                  if (other.hasKey()) {
                     this.bitField0_ |= 1;
                     this.key_ = other.key_;
                     this.onChanged();
                  }

                  if (other.hasValue()) {
                     this.mergeValue(other.getValue());
                  }

                  this.mergeUnknownFields(other.getUnknownFields());
                  return this;
               }
            }

            public final boolean isInitialized() {
               if (!this.hasKey()) {
                  return false;
               } else if (!this.hasValue()) {
                  return false;
               } else {
                  return this.getValue().isInitialized();
               }
            }

            public MysqlxDatatypes.Object.ObjectField.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
               MysqlxDatatypes.Object.ObjectField parsedMessage = null;

               try {
                  parsedMessage = (MysqlxDatatypes.Object.ObjectField)MysqlxDatatypes.Object.ObjectField.PARSER.parsePartialFrom(input, extensionRegistry);
               } catch (InvalidProtocolBufferException var8) {
                  parsedMessage = (MysqlxDatatypes.Object.ObjectField)var8.getUnfinishedMessage();
                  throw var8;
               } finally {
                  if (parsedMessage != null) {
                     this.mergeFrom(parsedMessage);
                  }
               }

               return this;
            }

            @Override
            public boolean hasKey() {
               return (this.bitField0_ & 1) == 1;
            }

            @Override
            public java.lang.String getKey() {
               java.lang.Object ref = this.key_;
               if (!(ref instanceof java.lang.String)) {
                  ByteString bs = (ByteString)ref;
                  java.lang.String s = bs.toStringUtf8();
                  if (bs.isValidUtf8()) {
                     this.key_ = s;
                  }

                  return s;
               } else {
                  return (java.lang.String)ref;
               }
            }

            @Override
            public ByteString getKeyBytes() {
               java.lang.Object ref = this.key_;
               if (ref instanceof java.lang.String) {
                  ByteString b = ByteString.copyFromUtf8((java.lang.String)ref);
                  this.key_ = b;
                  return b;
               } else {
                  return (ByteString)ref;
               }
            }

            public MysqlxDatatypes.Object.ObjectField.Builder setKey(java.lang.String value) {
               if (value == null) {
                  throw new NullPointerException();
               } else {
                  this.bitField0_ |= 1;
                  this.key_ = value;
                  this.onChanged();
                  return this;
               }
            }

            public MysqlxDatatypes.Object.ObjectField.Builder clearKey() {
               this.bitField0_ &= -2;
               this.key_ = MysqlxDatatypes.Object.ObjectField.getDefaultInstance().getKey();
               this.onChanged();
               return this;
            }

            public MysqlxDatatypes.Object.ObjectField.Builder setKeyBytes(ByteString value) {
               if (value == null) {
                  throw new NullPointerException();
               } else {
                  this.bitField0_ |= 1;
                  this.key_ = value;
                  this.onChanged();
                  return this;
               }
            }

            @Override
            public boolean hasValue() {
               return (this.bitField0_ & 2) == 2;
            }

            @Override
            public MysqlxDatatypes.Any getValue() {
               return this.valueBuilder_ == null ? this.value_ : (MysqlxDatatypes.Any)this.valueBuilder_.getMessage();
            }

            public MysqlxDatatypes.Object.ObjectField.Builder setValue(MysqlxDatatypes.Any value) {
               if (this.valueBuilder_ == null) {
                  if (value == null) {
                     throw new NullPointerException();
                  }

                  this.value_ = value;
                  this.onChanged();
               } else {
                  this.valueBuilder_.setMessage(value);
               }

               this.bitField0_ |= 2;
               return this;
            }

            public MysqlxDatatypes.Object.ObjectField.Builder setValue(MysqlxDatatypes.Any.Builder builderForValue) {
               if (this.valueBuilder_ == null) {
                  this.value_ = builderForValue.build();
                  this.onChanged();
               } else {
                  this.valueBuilder_.setMessage(builderForValue.build());
               }

               this.bitField0_ |= 2;
               return this;
            }

            public MysqlxDatatypes.Object.ObjectField.Builder mergeValue(MysqlxDatatypes.Any value) {
               if (this.valueBuilder_ == null) {
                  if ((this.bitField0_ & 2) == 2 && this.value_ != MysqlxDatatypes.Any.getDefaultInstance()) {
                     this.value_ = MysqlxDatatypes.Any.newBuilder(this.value_).mergeFrom(value).buildPartial();
                  } else {
                     this.value_ = value;
                  }

                  this.onChanged();
               } else {
                  this.valueBuilder_.mergeFrom(value);
               }

               this.bitField0_ |= 2;
               return this;
            }

            public MysqlxDatatypes.Object.ObjectField.Builder clearValue() {
               if (this.valueBuilder_ == null) {
                  this.value_ = MysqlxDatatypes.Any.getDefaultInstance();
                  this.onChanged();
               } else {
                  this.valueBuilder_.clear();
               }

               this.bitField0_ &= -3;
               return this;
            }

            public MysqlxDatatypes.Any.Builder getValueBuilder() {
               this.bitField0_ |= 2;
               this.onChanged();
               return (MysqlxDatatypes.Any.Builder)this.getValueFieldBuilder().getBuilder();
            }

            @Override
            public MysqlxDatatypes.AnyOrBuilder getValueOrBuilder() {
               return (MysqlxDatatypes.AnyOrBuilder)(this.valueBuilder_ != null
                  ? (MysqlxDatatypes.AnyOrBuilder)this.valueBuilder_.getMessageOrBuilder()
                  : this.value_);
            }

            private SingleFieldBuilder<MysqlxDatatypes.Any, MysqlxDatatypes.Any.Builder, MysqlxDatatypes.AnyOrBuilder> getValueFieldBuilder() {
               if (this.valueBuilder_ == null) {
                  this.valueBuilder_ = new SingleFieldBuilder(this.getValue(), this.getParentForChildren(), this.isClean());
                  this.value_ = null;
               }

               return this.valueBuilder_;
            }
         }
      }

      public interface ObjectFieldOrBuilder extends MessageOrBuilder {
         boolean hasKey();

         java.lang.String getKey();

         ByteString getKeyBytes();

         boolean hasValue();

         MysqlxDatatypes.Any getValue();

         MysqlxDatatypes.AnyOrBuilder getValueOrBuilder();
      }
   }

   public interface ObjectOrBuilder extends MessageOrBuilder {
      List<MysqlxDatatypes.Object.ObjectField> getFldList();

      MysqlxDatatypes.Object.ObjectField getFld(int var1);

      int getFldCount();

      List<? extends MysqlxDatatypes.Object.ObjectFieldOrBuilder> getFldOrBuilderList();

      MysqlxDatatypes.Object.ObjectFieldOrBuilder getFldOrBuilder(int var1);
   }

   public static final class Scalar extends GeneratedMessage implements MysqlxDatatypes.ScalarOrBuilder {
      private static final MysqlxDatatypes.Scalar defaultInstance = new MysqlxDatatypes.Scalar(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxDatatypes.Scalar> PARSER = new AbstractParser<MysqlxDatatypes.Scalar>() {
         public MysqlxDatatypes.Scalar parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxDatatypes.Scalar(input, extensionRegistry);
         }
      };
      private int bitField0_;
      public static final int TYPE_FIELD_NUMBER = 1;
      private MysqlxDatatypes.Scalar.Type type_;
      public static final int V_SIGNED_INT_FIELD_NUMBER = 2;
      private long vSignedInt_;
      public static final int V_UNSIGNED_INT_FIELD_NUMBER = 3;
      private long vUnsignedInt_;
      public static final int V_OCTETS_FIELD_NUMBER = 5;
      private MysqlxDatatypes.Scalar.Octets vOctets_;
      public static final int V_DOUBLE_FIELD_NUMBER = 6;
      private double vDouble_;
      public static final int V_FLOAT_FIELD_NUMBER = 7;
      private float vFloat_;
      public static final int V_BOOL_FIELD_NUMBER = 8;
      private boolean vBool_;
      public static final int V_STRING_FIELD_NUMBER = 9;
      private MysqlxDatatypes.Scalar.String vString_;
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private Scalar(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private Scalar(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static MysqlxDatatypes.Scalar getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxDatatypes.Scalar getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private Scalar(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                     MysqlxDatatypes.Scalar.Type value = MysqlxDatatypes.Scalar.Type.valueOf(rawValue);
                     if (value == null) {
                        unknownFields.mergeVarintField(1, rawValue);
                     } else {
                        this.bitField0_ |= 1;
                        this.type_ = value;
                     }
                     break;
                  case 16:
                     this.bitField0_ |= 2;
                     this.vSignedInt_ = input.readSInt64();
                     break;
                  case 24:
                     this.bitField0_ |= 4;
                     this.vUnsignedInt_ = input.readUInt64();
                     break;
                  case 42:
                     MysqlxDatatypes.Scalar.Octets.Builder subBuilder = null;
                     if ((this.bitField0_ & 8) == 8) {
                        subBuilder = this.vOctets_.toBuilder();
                     }

                     this.vOctets_ = (MysqlxDatatypes.Scalar.Octets)input.readMessage(MysqlxDatatypes.Scalar.Octets.PARSER, extensionRegistry);
                     if (subBuilder != null) {
                        subBuilder.mergeFrom(this.vOctets_);
                        this.vOctets_ = subBuilder.buildPartial();
                     }

                     this.bitField0_ |= 8;
                     break;
                  case 49:
                     this.bitField0_ |= 16;
                     this.vDouble_ = input.readDouble();
                     break;
                  case 61:
                     this.bitField0_ |= 32;
                     this.vFloat_ = input.readFloat();
                     break;
                  case 64:
                     this.bitField0_ |= 64;
                     this.vBool_ = input.readBool();
                     break;
                  case 74:
                     MysqlxDatatypes.Scalar.String.Builder subBuilder = null;
                     if ((this.bitField0_ & 128) == 128) {
                        subBuilder = this.vString_.toBuilder();
                     }

                     this.vString_ = (MysqlxDatatypes.Scalar.String)input.readMessage(MysqlxDatatypes.Scalar.String.PARSER, extensionRegistry);
                     if (subBuilder != null) {
                        subBuilder.mergeFrom(this.vString_);
                        this.vString_ = subBuilder.buildPartial();
                     }

                     this.bitField0_ |= 128;
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
         return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Scalar_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Scalar_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxDatatypes.Scalar.class, MysqlxDatatypes.Scalar.Builder.class);
      }

      public Parser<MysqlxDatatypes.Scalar> getParserForType() {
         return PARSER;
      }

      @Override
      public boolean hasType() {
         return (this.bitField0_ & 1) == 1;
      }

      @Override
      public MysqlxDatatypes.Scalar.Type getType() {
         return this.type_;
      }

      @Override
      public boolean hasVSignedInt() {
         return (this.bitField0_ & 2) == 2;
      }

      @Override
      public long getVSignedInt() {
         return this.vSignedInt_;
      }

      @Override
      public boolean hasVUnsignedInt() {
         return (this.bitField0_ & 4) == 4;
      }

      @Override
      public long getVUnsignedInt() {
         return this.vUnsignedInt_;
      }

      @Override
      public boolean hasVOctets() {
         return (this.bitField0_ & 8) == 8;
      }

      @Override
      public MysqlxDatatypes.Scalar.Octets getVOctets() {
         return this.vOctets_;
      }

      @Override
      public MysqlxDatatypes.Scalar.OctetsOrBuilder getVOctetsOrBuilder() {
         return this.vOctets_;
      }

      @Override
      public boolean hasVDouble() {
         return (this.bitField0_ & 16) == 16;
      }

      @Override
      public double getVDouble() {
         return this.vDouble_;
      }

      @Override
      public boolean hasVFloat() {
         return (this.bitField0_ & 32) == 32;
      }

      @Override
      public float getVFloat() {
         return this.vFloat_;
      }

      @Override
      public boolean hasVBool() {
         return (this.bitField0_ & 64) == 64;
      }

      @Override
      public boolean getVBool() {
         return this.vBool_;
      }

      @Override
      public boolean hasVString() {
         return (this.bitField0_ & 128) == 128;
      }

      @Override
      public MysqlxDatatypes.Scalar.String getVString() {
         return this.vString_;
      }

      @Override
      public MysqlxDatatypes.Scalar.StringOrBuilder getVStringOrBuilder() {
         return this.vString_;
      }

      private void initFields() {
         this.type_ = MysqlxDatatypes.Scalar.Type.V_SINT;
         this.vSignedInt_ = 0L;
         this.vUnsignedInt_ = 0L;
         this.vOctets_ = MysqlxDatatypes.Scalar.Octets.getDefaultInstance();
         this.vDouble_ = 0.0;
         this.vFloat_ = 0.0F;
         this.vBool_ = false;
         this.vString_ = MysqlxDatatypes.Scalar.String.getDefaultInstance();
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
         } else if (this.hasVOctets() && !this.getVOctets().isInitialized()) {
            this.memoizedIsInitialized = 0;
            return false;
         } else if (this.hasVString() && !this.getVString().isInitialized()) {
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
            output.writeSInt64(2, this.vSignedInt_);
         }

         if ((this.bitField0_ & 4) == 4) {
            output.writeUInt64(3, this.vUnsignedInt_);
         }

         if ((this.bitField0_ & 8) == 8) {
            output.writeMessage(5, this.vOctets_);
         }

         if ((this.bitField0_ & 16) == 16) {
            output.writeDouble(6, this.vDouble_);
         }

         if ((this.bitField0_ & 32) == 32) {
            output.writeFloat(7, this.vFloat_);
         }

         if ((this.bitField0_ & 64) == 64) {
            output.writeBool(8, this.vBool_);
         }

         if ((this.bitField0_ & 128) == 128) {
            output.writeMessage(9, this.vString_);
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
               size += CodedOutputStream.computeSInt64Size(2, this.vSignedInt_);
            }

            if ((this.bitField0_ & 4) == 4) {
               size += CodedOutputStream.computeUInt64Size(3, this.vUnsignedInt_);
            }

            if ((this.bitField0_ & 8) == 8) {
               size += CodedOutputStream.computeMessageSize(5, this.vOctets_);
            }

            if ((this.bitField0_ & 16) == 16) {
               size += CodedOutputStream.computeDoubleSize(6, this.vDouble_);
            }

            if ((this.bitField0_ & 32) == 32) {
               size += CodedOutputStream.computeFloatSize(7, this.vFloat_);
            }

            if ((this.bitField0_ & 64) == 64) {
               size += CodedOutputStream.computeBoolSize(8, this.vBool_);
            }

            if ((this.bitField0_ & 128) == 128) {
               size += CodedOutputStream.computeMessageSize(9, this.vString_);
            }

            size += this.getUnknownFields().getSerializedSize();
            this.memoizedSerializedSize = size;
            return size;
         }
      }

      protected java.lang.Object writeReplace() throws ObjectStreamException {
         return super.writeReplace();
      }

      public static MysqlxDatatypes.Scalar parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Scalar)PARSER.parseFrom(data);
      }

      public static MysqlxDatatypes.Scalar parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Scalar)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxDatatypes.Scalar parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Scalar)PARSER.parseFrom(data);
      }

      public static MysqlxDatatypes.Scalar parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxDatatypes.Scalar)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxDatatypes.Scalar parseFrom(InputStream input) throws IOException {
         return (MysqlxDatatypes.Scalar)PARSER.parseFrom(input);
      }

      public static MysqlxDatatypes.Scalar parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxDatatypes.Scalar)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxDatatypes.Scalar parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxDatatypes.Scalar)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxDatatypes.Scalar parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxDatatypes.Scalar)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxDatatypes.Scalar parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxDatatypes.Scalar)PARSER.parseFrom(input);
      }

      public static MysqlxDatatypes.Scalar parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxDatatypes.Scalar)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxDatatypes.Scalar.Builder newBuilder() {
         return MysqlxDatatypes.Scalar.Builder.create();
      }

      public MysqlxDatatypes.Scalar.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxDatatypes.Scalar.Builder newBuilder(MysqlxDatatypes.Scalar prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxDatatypes.Scalar.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxDatatypes.Scalar.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxDatatypes.Scalar.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxDatatypes.Scalar.Builder>
         implements MysqlxDatatypes.ScalarOrBuilder {
         private int bitField0_;
         private MysqlxDatatypes.Scalar.Type type_ = MysqlxDatatypes.Scalar.Type.V_SINT;
         private long vSignedInt_;
         private long vUnsignedInt_;
         private MysqlxDatatypes.Scalar.Octets vOctets_ = MysqlxDatatypes.Scalar.Octets.getDefaultInstance();
         private SingleFieldBuilder<MysqlxDatatypes.Scalar.Octets, MysqlxDatatypes.Scalar.Octets.Builder, MysqlxDatatypes.Scalar.OctetsOrBuilder> vOctetsBuilder_;
         private double vDouble_;
         private float vFloat_;
         private boolean vBool_;
         private MysqlxDatatypes.Scalar.String vString_ = MysqlxDatatypes.Scalar.String.getDefaultInstance();
         private SingleFieldBuilder<MysqlxDatatypes.Scalar.String, MysqlxDatatypes.Scalar.String.Builder, MysqlxDatatypes.Scalar.StringOrBuilder> vStringBuilder_;

         public static final Descriptor getDescriptor() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Scalar_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Scalar_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxDatatypes.Scalar.class, MysqlxDatatypes.Scalar.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxDatatypes.Scalar.alwaysUseFieldBuilders) {
               this.getVOctetsFieldBuilder();
               this.getVStringFieldBuilder();
            }
         }

         private static MysqlxDatatypes.Scalar.Builder create() {
            return new MysqlxDatatypes.Scalar.Builder();
         }

         public MysqlxDatatypes.Scalar.Builder clear() {
            super.clear();
            this.type_ = MysqlxDatatypes.Scalar.Type.V_SINT;
            this.bitField0_ &= -2;
            this.vSignedInt_ = 0L;
            this.bitField0_ &= -3;
            this.vUnsignedInt_ = 0L;
            this.bitField0_ &= -5;
            if (this.vOctetsBuilder_ == null) {
               this.vOctets_ = MysqlxDatatypes.Scalar.Octets.getDefaultInstance();
            } else {
               this.vOctetsBuilder_.clear();
            }

            this.bitField0_ &= -9;
            this.vDouble_ = 0.0;
            this.bitField0_ &= -17;
            this.vFloat_ = 0.0F;
            this.bitField0_ &= -33;
            this.vBool_ = false;
            this.bitField0_ &= -65;
            if (this.vStringBuilder_ == null) {
               this.vString_ = MysqlxDatatypes.Scalar.String.getDefaultInstance();
            } else {
               this.vStringBuilder_.clear();
            }

            this.bitField0_ &= -129;
            return this;
         }

         public MysqlxDatatypes.Scalar.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Scalar_descriptor;
         }

         public MysqlxDatatypes.Scalar getDefaultInstanceForType() {
            return MysqlxDatatypes.Scalar.getDefaultInstance();
         }

         public MysqlxDatatypes.Scalar build() {
            MysqlxDatatypes.Scalar result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxDatatypes.Scalar buildPartial() {
            MysqlxDatatypes.Scalar result = new MysqlxDatatypes.Scalar(this);
            int from_bitField0_ = this.bitField0_;
            int to_bitField0_ = 0;
            if ((from_bitField0_ & 1) == 1) {
               to_bitField0_ |= 1;
            }

            result.type_ = this.type_;
            if ((from_bitField0_ & 2) == 2) {
               to_bitField0_ |= 2;
            }

            result.vSignedInt_ = this.vSignedInt_;
            if ((from_bitField0_ & 4) == 4) {
               to_bitField0_ |= 4;
            }

            result.vUnsignedInt_ = this.vUnsignedInt_;
            if ((from_bitField0_ & 8) == 8) {
               to_bitField0_ |= 8;
            }

            if (this.vOctetsBuilder_ == null) {
               result.vOctets_ = this.vOctets_;
            } else {
               result.vOctets_ = (MysqlxDatatypes.Scalar.Octets)this.vOctetsBuilder_.build();
            }

            if ((from_bitField0_ & 16) == 16) {
               to_bitField0_ |= 16;
            }

            result.vDouble_ = this.vDouble_;
            if ((from_bitField0_ & 32) == 32) {
               to_bitField0_ |= 32;
            }

            result.vFloat_ = this.vFloat_;
            if ((from_bitField0_ & 64) == 64) {
               to_bitField0_ |= 64;
            }

            result.vBool_ = this.vBool_;
            if ((from_bitField0_ & 128) == 128) {
               to_bitField0_ |= 128;
            }

            if (this.vStringBuilder_ == null) {
               result.vString_ = this.vString_;
            } else {
               result.vString_ = (MysqlxDatatypes.Scalar.String)this.vStringBuilder_.build();
            }

            result.bitField0_ = to_bitField0_;
            this.onBuilt();
            return result;
         }

         public MysqlxDatatypes.Scalar.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxDatatypes.Scalar) {
               return this.mergeFrom((MysqlxDatatypes.Scalar)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxDatatypes.Scalar.Builder mergeFrom(MysqlxDatatypes.Scalar other) {
            if (other == MysqlxDatatypes.Scalar.getDefaultInstance()) {
               return this;
            } else {
               if (other.hasType()) {
                  this.setType(other.getType());
               }

               if (other.hasVSignedInt()) {
                  this.setVSignedInt(other.getVSignedInt());
               }

               if (other.hasVUnsignedInt()) {
                  this.setVUnsignedInt(other.getVUnsignedInt());
               }

               if (other.hasVOctets()) {
                  this.mergeVOctets(other.getVOctets());
               }

               if (other.hasVDouble()) {
                  this.setVDouble(other.getVDouble());
               }

               if (other.hasVFloat()) {
                  this.setVFloat(other.getVFloat());
               }

               if (other.hasVBool()) {
                  this.setVBool(other.getVBool());
               }

               if (other.hasVString()) {
                  this.mergeVString(other.getVString());
               }

               this.mergeUnknownFields(other.getUnknownFields());
               return this;
            }
         }

         public final boolean isInitialized() {
            if (!this.hasType()) {
               return false;
            } else if (this.hasVOctets() && !this.getVOctets().isInitialized()) {
               return false;
            } else {
               return !this.hasVString() || this.getVString().isInitialized();
            }
         }

         public MysqlxDatatypes.Scalar.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxDatatypes.Scalar parsedMessage = null;

            try {
               parsedMessage = (MysqlxDatatypes.Scalar)MysqlxDatatypes.Scalar.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxDatatypes.Scalar)var8.getUnfinishedMessage();
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
         public MysqlxDatatypes.Scalar.Type getType() {
            return this.type_;
         }

         public MysqlxDatatypes.Scalar.Builder setType(MysqlxDatatypes.Scalar.Type value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 1;
               this.type_ = value;
               this.onChanged();
               return this;
            }
         }

         public MysqlxDatatypes.Scalar.Builder clearType() {
            this.bitField0_ &= -2;
            this.type_ = MysqlxDatatypes.Scalar.Type.V_SINT;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasVSignedInt() {
            return (this.bitField0_ & 2) == 2;
         }

         @Override
         public long getVSignedInt() {
            return this.vSignedInt_;
         }

         public MysqlxDatatypes.Scalar.Builder setVSignedInt(long value) {
            this.bitField0_ |= 2;
            this.vSignedInt_ = value;
            this.onChanged();
            return this;
         }

         public MysqlxDatatypes.Scalar.Builder clearVSignedInt() {
            this.bitField0_ &= -3;
            this.vSignedInt_ = 0L;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasVUnsignedInt() {
            return (this.bitField0_ & 4) == 4;
         }

         @Override
         public long getVUnsignedInt() {
            return this.vUnsignedInt_;
         }

         public MysqlxDatatypes.Scalar.Builder setVUnsignedInt(long value) {
            this.bitField0_ |= 4;
            this.vUnsignedInt_ = value;
            this.onChanged();
            return this;
         }

         public MysqlxDatatypes.Scalar.Builder clearVUnsignedInt() {
            this.bitField0_ &= -5;
            this.vUnsignedInt_ = 0L;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasVOctets() {
            return (this.bitField0_ & 8) == 8;
         }

         @Override
         public MysqlxDatatypes.Scalar.Octets getVOctets() {
            return this.vOctetsBuilder_ == null ? this.vOctets_ : (MysqlxDatatypes.Scalar.Octets)this.vOctetsBuilder_.getMessage();
         }

         public MysqlxDatatypes.Scalar.Builder setVOctets(MysqlxDatatypes.Scalar.Octets value) {
            if (this.vOctetsBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.vOctets_ = value;
               this.onChanged();
            } else {
               this.vOctetsBuilder_.setMessage(value);
            }

            this.bitField0_ |= 8;
            return this;
         }

         public MysqlxDatatypes.Scalar.Builder setVOctets(MysqlxDatatypes.Scalar.Octets.Builder builderForValue) {
            if (this.vOctetsBuilder_ == null) {
               this.vOctets_ = builderForValue.build();
               this.onChanged();
            } else {
               this.vOctetsBuilder_.setMessage(builderForValue.build());
            }

            this.bitField0_ |= 8;
            return this;
         }

         public MysqlxDatatypes.Scalar.Builder mergeVOctets(MysqlxDatatypes.Scalar.Octets value) {
            if (this.vOctetsBuilder_ == null) {
               if ((this.bitField0_ & 8) == 8 && this.vOctets_ != MysqlxDatatypes.Scalar.Octets.getDefaultInstance()) {
                  this.vOctets_ = MysqlxDatatypes.Scalar.Octets.newBuilder(this.vOctets_).mergeFrom(value).buildPartial();
               } else {
                  this.vOctets_ = value;
               }

               this.onChanged();
            } else {
               this.vOctetsBuilder_.mergeFrom(value);
            }

            this.bitField0_ |= 8;
            return this;
         }

         public MysqlxDatatypes.Scalar.Builder clearVOctets() {
            if (this.vOctetsBuilder_ == null) {
               this.vOctets_ = MysqlxDatatypes.Scalar.Octets.getDefaultInstance();
               this.onChanged();
            } else {
               this.vOctetsBuilder_.clear();
            }

            this.bitField0_ &= -9;
            return this;
         }

         public MysqlxDatatypes.Scalar.Octets.Builder getVOctetsBuilder() {
            this.bitField0_ |= 8;
            this.onChanged();
            return (MysqlxDatatypes.Scalar.Octets.Builder)this.getVOctetsFieldBuilder().getBuilder();
         }

         @Override
         public MysqlxDatatypes.Scalar.OctetsOrBuilder getVOctetsOrBuilder() {
            return (MysqlxDatatypes.Scalar.OctetsOrBuilder)(this.vOctetsBuilder_ != null
               ? (MysqlxDatatypes.Scalar.OctetsOrBuilder)this.vOctetsBuilder_.getMessageOrBuilder()
               : this.vOctets_);
         }

         private SingleFieldBuilder<MysqlxDatatypes.Scalar.Octets, MysqlxDatatypes.Scalar.Octets.Builder, MysqlxDatatypes.Scalar.OctetsOrBuilder> getVOctetsFieldBuilder() {
            if (this.vOctetsBuilder_ == null) {
               this.vOctetsBuilder_ = new SingleFieldBuilder(this.getVOctets(), this.getParentForChildren(), this.isClean());
               this.vOctets_ = null;
            }

            return this.vOctetsBuilder_;
         }

         @Override
         public boolean hasVDouble() {
            return (this.bitField0_ & 16) == 16;
         }

         @Override
         public double getVDouble() {
            return this.vDouble_;
         }

         public MysqlxDatatypes.Scalar.Builder setVDouble(double value) {
            this.bitField0_ |= 16;
            this.vDouble_ = value;
            this.onChanged();
            return this;
         }

         public MysqlxDatatypes.Scalar.Builder clearVDouble() {
            this.bitField0_ &= -17;
            this.vDouble_ = 0.0;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasVFloat() {
            return (this.bitField0_ & 32) == 32;
         }

         @Override
         public float getVFloat() {
            return this.vFloat_;
         }

         public MysqlxDatatypes.Scalar.Builder setVFloat(float value) {
            this.bitField0_ |= 32;
            this.vFloat_ = value;
            this.onChanged();
            return this;
         }

         public MysqlxDatatypes.Scalar.Builder clearVFloat() {
            this.bitField0_ &= -33;
            this.vFloat_ = 0.0F;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasVBool() {
            return (this.bitField0_ & 64) == 64;
         }

         @Override
         public boolean getVBool() {
            return this.vBool_;
         }

         public MysqlxDatatypes.Scalar.Builder setVBool(boolean value) {
            this.bitField0_ |= 64;
            this.vBool_ = value;
            this.onChanged();
            return this;
         }

         public MysqlxDatatypes.Scalar.Builder clearVBool() {
            this.bitField0_ &= -65;
            this.vBool_ = false;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasVString() {
            return (this.bitField0_ & 128) == 128;
         }

         @Override
         public MysqlxDatatypes.Scalar.String getVString() {
            return this.vStringBuilder_ == null ? this.vString_ : (MysqlxDatatypes.Scalar.String)this.vStringBuilder_.getMessage();
         }

         public MysqlxDatatypes.Scalar.Builder setVString(MysqlxDatatypes.Scalar.String value) {
            if (this.vStringBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.vString_ = value;
               this.onChanged();
            } else {
               this.vStringBuilder_.setMessage(value);
            }

            this.bitField0_ |= 128;
            return this;
         }

         public MysqlxDatatypes.Scalar.Builder setVString(MysqlxDatatypes.Scalar.String.Builder builderForValue) {
            if (this.vStringBuilder_ == null) {
               this.vString_ = builderForValue.build();
               this.onChanged();
            } else {
               this.vStringBuilder_.setMessage(builderForValue.build());
            }

            this.bitField0_ |= 128;
            return this;
         }

         public MysqlxDatatypes.Scalar.Builder mergeVString(MysqlxDatatypes.Scalar.String value) {
            if (this.vStringBuilder_ == null) {
               if ((this.bitField0_ & 128) == 128 && this.vString_ != MysqlxDatatypes.Scalar.String.getDefaultInstance()) {
                  this.vString_ = MysqlxDatatypes.Scalar.String.newBuilder(this.vString_).mergeFrom(value).buildPartial();
               } else {
                  this.vString_ = value;
               }

               this.onChanged();
            } else {
               this.vStringBuilder_.mergeFrom(value);
            }

            this.bitField0_ |= 128;
            return this;
         }

         public MysqlxDatatypes.Scalar.Builder clearVString() {
            if (this.vStringBuilder_ == null) {
               this.vString_ = MysqlxDatatypes.Scalar.String.getDefaultInstance();
               this.onChanged();
            } else {
               this.vStringBuilder_.clear();
            }

            this.bitField0_ &= -129;
            return this;
         }

         public MysqlxDatatypes.Scalar.String.Builder getVStringBuilder() {
            this.bitField0_ |= 128;
            this.onChanged();
            return (MysqlxDatatypes.Scalar.String.Builder)this.getVStringFieldBuilder().getBuilder();
         }

         @Override
         public MysqlxDatatypes.Scalar.StringOrBuilder getVStringOrBuilder() {
            return (MysqlxDatatypes.Scalar.StringOrBuilder)(this.vStringBuilder_ != null
               ? (MysqlxDatatypes.Scalar.StringOrBuilder)this.vStringBuilder_.getMessageOrBuilder()
               : this.vString_);
         }

         private SingleFieldBuilder<MysqlxDatatypes.Scalar.String, MysqlxDatatypes.Scalar.String.Builder, MysqlxDatatypes.Scalar.StringOrBuilder> getVStringFieldBuilder() {
            if (this.vStringBuilder_ == null) {
               this.vStringBuilder_ = new SingleFieldBuilder(this.getVString(), this.getParentForChildren(), this.isClean());
               this.vString_ = null;
            }

            return this.vStringBuilder_;
         }
      }

      public static final class Octets extends GeneratedMessage implements MysqlxDatatypes.Scalar.OctetsOrBuilder {
         private static final MysqlxDatatypes.Scalar.Octets defaultInstance = new MysqlxDatatypes.Scalar.Octets(true);
         private final UnknownFieldSet unknownFields;
         public static Parser<MysqlxDatatypes.Scalar.Octets> PARSER = new AbstractParser<MysqlxDatatypes.Scalar.Octets>() {
            public MysqlxDatatypes.Scalar.Octets parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
               return new MysqlxDatatypes.Scalar.Octets(input, extensionRegistry);
            }
         };
         private int bitField0_;
         public static final int VALUE_FIELD_NUMBER = 1;
         private ByteString value_;
         public static final int CONTENT_TYPE_FIELD_NUMBER = 2;
         private int contentType_;
         private byte memoizedIsInitialized = -1;
         private int memoizedSerializedSize = -1;
         private static final long serialVersionUID = 0L;

         private Octets(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
            super(builder);
            this.unknownFields = builder.getUnknownFields();
         }

         private Octets(boolean noInit) {
            this.unknownFields = UnknownFieldSet.getDefaultInstance();
         }

         public static MysqlxDatatypes.Scalar.Octets getDefaultInstance() {
            return defaultInstance;
         }

         public MysqlxDatatypes.Scalar.Octets getDefaultInstanceForType() {
            return defaultInstance;
         }

         public final UnknownFieldSet getUnknownFields() {
            return this.unknownFields;
         }

         private Octets(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                        this.value_ = input.readBytes();
                        break;
                     case 16:
                        this.bitField0_ |= 2;
                        this.contentType_ = input.readUInt32();
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
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Scalar_Octets_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Scalar_Octets_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxDatatypes.Scalar.Octets.class, MysqlxDatatypes.Scalar.Octets.Builder.class);
         }

         public Parser<MysqlxDatatypes.Scalar.Octets> getParserForType() {
            return PARSER;
         }

         @Override
         public boolean hasValue() {
            return (this.bitField0_ & 1) == 1;
         }

         @Override
         public ByteString getValue() {
            return this.value_;
         }

         @Override
         public boolean hasContentType() {
            return (this.bitField0_ & 2) == 2;
         }

         @Override
         public int getContentType() {
            return this.contentType_;
         }

         private void initFields() {
            this.value_ = ByteString.EMPTY;
            this.contentType_ = 0;
         }

         public final boolean isInitialized() {
            byte isInitialized = this.memoizedIsInitialized;
            if (isInitialized == 1) {
               return true;
            } else if (isInitialized == 0) {
               return false;
            } else if (!this.hasValue()) {
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
               output.writeBytes(1, this.value_);
            }

            if ((this.bitField0_ & 2) == 2) {
               output.writeUInt32(2, this.contentType_);
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
                  size += CodedOutputStream.computeBytesSize(1, this.value_);
               }

               if ((this.bitField0_ & 2) == 2) {
                  size += CodedOutputStream.computeUInt32Size(2, this.contentType_);
               }

               size += this.getUnknownFields().getSerializedSize();
               this.memoizedSerializedSize = size;
               return size;
            }
         }

         protected java.lang.Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
         }

         public static MysqlxDatatypes.Scalar.Octets parseFrom(ByteString data) throws InvalidProtocolBufferException {
            return (MysqlxDatatypes.Scalar.Octets)PARSER.parseFrom(data);
         }

         public static MysqlxDatatypes.Scalar.Octets parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (MysqlxDatatypes.Scalar.Octets)PARSER.parseFrom(data, extensionRegistry);
         }

         public static MysqlxDatatypes.Scalar.Octets parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return (MysqlxDatatypes.Scalar.Octets)PARSER.parseFrom(data);
         }

         public static MysqlxDatatypes.Scalar.Octets parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (MysqlxDatatypes.Scalar.Octets)PARSER.parseFrom(data, extensionRegistry);
         }

         public static MysqlxDatatypes.Scalar.Octets parseFrom(InputStream input) throws IOException {
            return (MysqlxDatatypes.Scalar.Octets)PARSER.parseFrom(input);
         }

         public static MysqlxDatatypes.Scalar.Octets parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (MysqlxDatatypes.Scalar.Octets)PARSER.parseFrom(input, extensionRegistry);
         }

         public static MysqlxDatatypes.Scalar.Octets parseDelimitedFrom(InputStream input) throws IOException {
            return (MysqlxDatatypes.Scalar.Octets)PARSER.parseDelimitedFrom(input);
         }

         public static MysqlxDatatypes.Scalar.Octets parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (MysqlxDatatypes.Scalar.Octets)PARSER.parseDelimitedFrom(input, extensionRegistry);
         }

         public static MysqlxDatatypes.Scalar.Octets parseFrom(CodedInputStream input) throws IOException {
            return (MysqlxDatatypes.Scalar.Octets)PARSER.parseFrom(input);
         }

         public static MysqlxDatatypes.Scalar.Octets parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (MysqlxDatatypes.Scalar.Octets)PARSER.parseFrom(input, extensionRegistry);
         }

         public static MysqlxDatatypes.Scalar.Octets.Builder newBuilder() {
            return MysqlxDatatypes.Scalar.Octets.Builder.create();
         }

         public MysqlxDatatypes.Scalar.Octets.Builder newBuilderForType() {
            return newBuilder();
         }

         public static MysqlxDatatypes.Scalar.Octets.Builder newBuilder(MysqlxDatatypes.Scalar.Octets prototype) {
            return newBuilder().mergeFrom(prototype);
         }

         public MysqlxDatatypes.Scalar.Octets.Builder toBuilder() {
            return newBuilder(this);
         }

         protected MysqlxDatatypes.Scalar.Octets.Builder newBuilderForType(BuilderParent parent) {
            return new MysqlxDatatypes.Scalar.Octets.Builder(parent);
         }

         static {
            defaultInstance.initFields();
         }

         public static final class Builder
            extends com.google.protobuf.GeneratedMessage.Builder<MysqlxDatatypes.Scalar.Octets.Builder>
            implements MysqlxDatatypes.Scalar.OctetsOrBuilder {
            private int bitField0_;
            private ByteString value_ = ByteString.EMPTY;
            private int contentType_;

            public static final Descriptor getDescriptor() {
               return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Scalar_Octets_descriptor;
            }

            protected FieldAccessorTable internalGetFieldAccessorTable() {
               return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Scalar_Octets_fieldAccessorTable
                  .ensureFieldAccessorsInitialized(MysqlxDatatypes.Scalar.Octets.class, MysqlxDatatypes.Scalar.Octets.Builder.class);
            }

            private Builder() {
               this.maybeForceBuilderInitialization();
            }

            private Builder(BuilderParent parent) {
               super(parent);
               this.maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
               if (MysqlxDatatypes.Scalar.Octets.alwaysUseFieldBuilders) {
               }
            }

            private static MysqlxDatatypes.Scalar.Octets.Builder create() {
               return new MysqlxDatatypes.Scalar.Octets.Builder();
            }

            public MysqlxDatatypes.Scalar.Octets.Builder clear() {
               super.clear();
               this.value_ = ByteString.EMPTY;
               this.bitField0_ &= -2;
               this.contentType_ = 0;
               this.bitField0_ &= -3;
               return this;
            }

            public MysqlxDatatypes.Scalar.Octets.Builder clone() {
               return create().mergeFrom(this.buildPartial());
            }

            public Descriptor getDescriptorForType() {
               return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Scalar_Octets_descriptor;
            }

            public MysqlxDatatypes.Scalar.Octets getDefaultInstanceForType() {
               return MysqlxDatatypes.Scalar.Octets.getDefaultInstance();
            }

            public MysqlxDatatypes.Scalar.Octets build() {
               MysqlxDatatypes.Scalar.Octets result = this.buildPartial();
               if (!result.isInitialized()) {
                  throw newUninitializedMessageException(result);
               } else {
                  return result;
               }
            }

            public MysqlxDatatypes.Scalar.Octets buildPartial() {
               MysqlxDatatypes.Scalar.Octets result = new MysqlxDatatypes.Scalar.Octets(this);
               int from_bitField0_ = this.bitField0_;
               int to_bitField0_ = 0;
               if ((from_bitField0_ & 1) == 1) {
                  to_bitField0_ |= 1;
               }

               result.value_ = this.value_;
               if ((from_bitField0_ & 2) == 2) {
                  to_bitField0_ |= 2;
               }

               result.contentType_ = this.contentType_;
               result.bitField0_ = to_bitField0_;
               this.onBuilt();
               return result;
            }

            public MysqlxDatatypes.Scalar.Octets.Builder mergeFrom(Message other) {
               if (other instanceof MysqlxDatatypes.Scalar.Octets) {
                  return this.mergeFrom((MysqlxDatatypes.Scalar.Octets)other);
               } else {
                  super.mergeFrom(other);
                  return this;
               }
            }

            public MysqlxDatatypes.Scalar.Octets.Builder mergeFrom(MysqlxDatatypes.Scalar.Octets other) {
               if (other == MysqlxDatatypes.Scalar.Octets.getDefaultInstance()) {
                  return this;
               } else {
                  if (other.hasValue()) {
                     this.setValue(other.getValue());
                  }

                  if (other.hasContentType()) {
                     this.setContentType(other.getContentType());
                  }

                  this.mergeUnknownFields(other.getUnknownFields());
                  return this;
               }
            }

            public final boolean isInitialized() {
               return this.hasValue();
            }

            public MysqlxDatatypes.Scalar.Octets.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
               MysqlxDatatypes.Scalar.Octets parsedMessage = null;

               try {
                  parsedMessage = (MysqlxDatatypes.Scalar.Octets)MysqlxDatatypes.Scalar.Octets.PARSER.parsePartialFrom(input, extensionRegistry);
               } catch (InvalidProtocolBufferException var8) {
                  parsedMessage = (MysqlxDatatypes.Scalar.Octets)var8.getUnfinishedMessage();
                  throw var8;
               } finally {
                  if (parsedMessage != null) {
                     this.mergeFrom(parsedMessage);
                  }
               }

               return this;
            }

            @Override
            public boolean hasValue() {
               return (this.bitField0_ & 1) == 1;
            }

            @Override
            public ByteString getValue() {
               return this.value_;
            }

            public MysqlxDatatypes.Scalar.Octets.Builder setValue(ByteString value) {
               if (value == null) {
                  throw new NullPointerException();
               } else {
                  this.bitField0_ |= 1;
                  this.value_ = value;
                  this.onChanged();
                  return this;
               }
            }

            public MysqlxDatatypes.Scalar.Octets.Builder clearValue() {
               this.bitField0_ &= -2;
               this.value_ = MysqlxDatatypes.Scalar.Octets.getDefaultInstance().getValue();
               this.onChanged();
               return this;
            }

            @Override
            public boolean hasContentType() {
               return (this.bitField0_ & 2) == 2;
            }

            @Override
            public int getContentType() {
               return this.contentType_;
            }

            public MysqlxDatatypes.Scalar.Octets.Builder setContentType(int value) {
               this.bitField0_ |= 2;
               this.contentType_ = value;
               this.onChanged();
               return this;
            }

            public MysqlxDatatypes.Scalar.Octets.Builder clearContentType() {
               this.bitField0_ &= -3;
               this.contentType_ = 0;
               this.onChanged();
               return this;
            }
         }
      }

      public interface OctetsOrBuilder extends MessageOrBuilder {
         boolean hasValue();

         ByteString getValue();

         boolean hasContentType();

         int getContentType();
      }

      public static final class String extends GeneratedMessage implements MysqlxDatatypes.Scalar.StringOrBuilder {
         private static final MysqlxDatatypes.Scalar.String defaultInstance = new MysqlxDatatypes.Scalar.String(true);
         private final UnknownFieldSet unknownFields;
         public static Parser<MysqlxDatatypes.Scalar.String> PARSER = new AbstractParser<MysqlxDatatypes.Scalar.String>() {
            public MysqlxDatatypes.Scalar.String parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
               return new MysqlxDatatypes.Scalar.String(input, extensionRegistry);
            }
         };
         private int bitField0_;
         public static final int VALUE_FIELD_NUMBER = 1;
         private ByteString value_;
         public static final int COLLATION_FIELD_NUMBER = 2;
         private long collation_;
         private byte memoizedIsInitialized = -1;
         private int memoizedSerializedSize = -1;
         private static final long serialVersionUID = 0L;

         private String(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
            super(builder);
            this.unknownFields = builder.getUnknownFields();
         }

         private String(boolean noInit) {
            this.unknownFields = UnknownFieldSet.getDefaultInstance();
         }

         public static MysqlxDatatypes.Scalar.String getDefaultInstance() {
            return defaultInstance;
         }

         public MysqlxDatatypes.Scalar.String getDefaultInstanceForType() {
            return defaultInstance;
         }

         public final UnknownFieldSet getUnknownFields() {
            return this.unknownFields;
         }

         private String(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                        this.value_ = input.readBytes();
                        break;
                     case 16:
                        this.bitField0_ |= 2;
                        this.collation_ = input.readUInt64();
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
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Scalar_String_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Scalar_String_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxDatatypes.Scalar.String.class, MysqlxDatatypes.Scalar.String.Builder.class);
         }

         public Parser<MysqlxDatatypes.Scalar.String> getParserForType() {
            return PARSER;
         }

         @Override
         public boolean hasValue() {
            return (this.bitField0_ & 1) == 1;
         }

         @Override
         public ByteString getValue() {
            return this.value_;
         }

         @Override
         public boolean hasCollation() {
            return (this.bitField0_ & 2) == 2;
         }

         @Override
         public long getCollation() {
            return this.collation_;
         }

         private void initFields() {
            this.value_ = ByteString.EMPTY;
            this.collation_ = 0L;
         }

         public final boolean isInitialized() {
            byte isInitialized = this.memoizedIsInitialized;
            if (isInitialized == 1) {
               return true;
            } else if (isInitialized == 0) {
               return false;
            } else if (!this.hasValue()) {
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
               output.writeBytes(1, this.value_);
            }

            if ((this.bitField0_ & 2) == 2) {
               output.writeUInt64(2, this.collation_);
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
                  size += CodedOutputStream.computeBytesSize(1, this.value_);
               }

               if ((this.bitField0_ & 2) == 2) {
                  size += CodedOutputStream.computeUInt64Size(2, this.collation_);
               }

               size += this.getUnknownFields().getSerializedSize();
               this.memoizedSerializedSize = size;
               return size;
            }
         }

         protected java.lang.Object writeReplace() throws ObjectStreamException {
            return super.writeReplace();
         }

         public static MysqlxDatatypes.Scalar.String parseFrom(ByteString data) throws InvalidProtocolBufferException {
            return (MysqlxDatatypes.Scalar.String)PARSER.parseFrom(data);
         }

         public static MysqlxDatatypes.Scalar.String parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (MysqlxDatatypes.Scalar.String)PARSER.parseFrom(data, extensionRegistry);
         }

         public static MysqlxDatatypes.Scalar.String parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return (MysqlxDatatypes.Scalar.String)PARSER.parseFrom(data);
         }

         public static MysqlxDatatypes.Scalar.String parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (MysqlxDatatypes.Scalar.String)PARSER.parseFrom(data, extensionRegistry);
         }

         public static MysqlxDatatypes.Scalar.String parseFrom(InputStream input) throws IOException {
            return (MysqlxDatatypes.Scalar.String)PARSER.parseFrom(input);
         }

         public static MysqlxDatatypes.Scalar.String parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (MysqlxDatatypes.Scalar.String)PARSER.parseFrom(input, extensionRegistry);
         }

         public static MysqlxDatatypes.Scalar.String parseDelimitedFrom(InputStream input) throws IOException {
            return (MysqlxDatatypes.Scalar.String)PARSER.parseDelimitedFrom(input);
         }

         public static MysqlxDatatypes.Scalar.String parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (MysqlxDatatypes.Scalar.String)PARSER.parseDelimitedFrom(input, extensionRegistry);
         }

         public static MysqlxDatatypes.Scalar.String parseFrom(CodedInputStream input) throws IOException {
            return (MysqlxDatatypes.Scalar.String)PARSER.parseFrom(input);
         }

         public static MysqlxDatatypes.Scalar.String parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (MysqlxDatatypes.Scalar.String)PARSER.parseFrom(input, extensionRegistry);
         }

         public static MysqlxDatatypes.Scalar.String.Builder newBuilder() {
            return MysqlxDatatypes.Scalar.String.Builder.create();
         }

         public MysqlxDatatypes.Scalar.String.Builder newBuilderForType() {
            return newBuilder();
         }

         public static MysqlxDatatypes.Scalar.String.Builder newBuilder(MysqlxDatatypes.Scalar.String prototype) {
            return newBuilder().mergeFrom(prototype);
         }

         public MysqlxDatatypes.Scalar.String.Builder toBuilder() {
            return newBuilder(this);
         }

         protected MysqlxDatatypes.Scalar.String.Builder newBuilderForType(BuilderParent parent) {
            return new MysqlxDatatypes.Scalar.String.Builder(parent);
         }

         static {
            defaultInstance.initFields();
         }

         public static final class Builder
            extends com.google.protobuf.GeneratedMessage.Builder<MysqlxDatatypes.Scalar.String.Builder>
            implements MysqlxDatatypes.Scalar.StringOrBuilder {
            private int bitField0_;
            private ByteString value_ = ByteString.EMPTY;
            private long collation_;

            public static final Descriptor getDescriptor() {
               return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Scalar_String_descriptor;
            }

            protected FieldAccessorTable internalGetFieldAccessorTable() {
               return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Scalar_String_fieldAccessorTable
                  .ensureFieldAccessorsInitialized(MysqlxDatatypes.Scalar.String.class, MysqlxDatatypes.Scalar.String.Builder.class);
            }

            private Builder() {
               this.maybeForceBuilderInitialization();
            }

            private Builder(BuilderParent parent) {
               super(parent);
               this.maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
               if (MysqlxDatatypes.Scalar.String.alwaysUseFieldBuilders) {
               }
            }

            private static MysqlxDatatypes.Scalar.String.Builder create() {
               return new MysqlxDatatypes.Scalar.String.Builder();
            }

            public MysqlxDatatypes.Scalar.String.Builder clear() {
               super.clear();
               this.value_ = ByteString.EMPTY;
               this.bitField0_ &= -2;
               this.collation_ = 0L;
               this.bitField0_ &= -3;
               return this;
            }

            public MysqlxDatatypes.Scalar.String.Builder clone() {
               return create().mergeFrom(this.buildPartial());
            }

            public Descriptor getDescriptorForType() {
               return MysqlxDatatypes.internal_static_Mysqlx_Datatypes_Scalar_String_descriptor;
            }

            public MysqlxDatatypes.Scalar.String getDefaultInstanceForType() {
               return MysqlxDatatypes.Scalar.String.getDefaultInstance();
            }

            public MysqlxDatatypes.Scalar.String build() {
               MysqlxDatatypes.Scalar.String result = this.buildPartial();
               if (!result.isInitialized()) {
                  throw newUninitializedMessageException(result);
               } else {
                  return result;
               }
            }

            public MysqlxDatatypes.Scalar.String buildPartial() {
               MysqlxDatatypes.Scalar.String result = new MysqlxDatatypes.Scalar.String(this);
               int from_bitField0_ = this.bitField0_;
               int to_bitField0_ = 0;
               if ((from_bitField0_ & 1) == 1) {
                  to_bitField0_ |= 1;
               }

               result.value_ = this.value_;
               if ((from_bitField0_ & 2) == 2) {
                  to_bitField0_ |= 2;
               }

               result.collation_ = this.collation_;
               result.bitField0_ = to_bitField0_;
               this.onBuilt();
               return result;
            }

            public MysqlxDatatypes.Scalar.String.Builder mergeFrom(Message other) {
               if (other instanceof MysqlxDatatypes.Scalar.String) {
                  return this.mergeFrom((MysqlxDatatypes.Scalar.String)other);
               } else {
                  super.mergeFrom(other);
                  return this;
               }
            }

            public MysqlxDatatypes.Scalar.String.Builder mergeFrom(MysqlxDatatypes.Scalar.String other) {
               if (other == MysqlxDatatypes.Scalar.String.getDefaultInstance()) {
                  return this;
               } else {
                  if (other.hasValue()) {
                     this.setValue(other.getValue());
                  }

                  if (other.hasCollation()) {
                     this.setCollation(other.getCollation());
                  }

                  this.mergeUnknownFields(other.getUnknownFields());
                  return this;
               }
            }

            public final boolean isInitialized() {
               return this.hasValue();
            }

            public MysqlxDatatypes.Scalar.String.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
               MysqlxDatatypes.Scalar.String parsedMessage = null;

               try {
                  parsedMessage = (MysqlxDatatypes.Scalar.String)MysqlxDatatypes.Scalar.String.PARSER.parsePartialFrom(input, extensionRegistry);
               } catch (InvalidProtocolBufferException var8) {
                  parsedMessage = (MysqlxDatatypes.Scalar.String)var8.getUnfinishedMessage();
                  throw var8;
               } finally {
                  if (parsedMessage != null) {
                     this.mergeFrom(parsedMessage);
                  }
               }

               return this;
            }

            @Override
            public boolean hasValue() {
               return (this.bitField0_ & 1) == 1;
            }

            @Override
            public ByteString getValue() {
               return this.value_;
            }

            public MysqlxDatatypes.Scalar.String.Builder setValue(ByteString value) {
               if (value == null) {
                  throw new NullPointerException();
               } else {
                  this.bitField0_ |= 1;
                  this.value_ = value;
                  this.onChanged();
                  return this;
               }
            }

            public MysqlxDatatypes.Scalar.String.Builder clearValue() {
               this.bitField0_ &= -2;
               this.value_ = MysqlxDatatypes.Scalar.String.getDefaultInstance().getValue();
               this.onChanged();
               return this;
            }

            @Override
            public boolean hasCollation() {
               return (this.bitField0_ & 2) == 2;
            }

            @Override
            public long getCollation() {
               return this.collation_;
            }

            public MysqlxDatatypes.Scalar.String.Builder setCollation(long value) {
               this.bitField0_ |= 2;
               this.collation_ = value;
               this.onChanged();
               return this;
            }

            public MysqlxDatatypes.Scalar.String.Builder clearCollation() {
               this.bitField0_ &= -3;
               this.collation_ = 0L;
               this.onChanged();
               return this;
            }
         }
      }

      public interface StringOrBuilder extends MessageOrBuilder {
         boolean hasValue();

         ByteString getValue();

         boolean hasCollation();

         long getCollation();
      }

      public static enum Type implements ProtocolMessageEnum {
         V_SINT(0, 1),
         V_UINT(1, 2),
         V_NULL(2, 3),
         V_OCTETS(3, 4),
         V_DOUBLE(4, 5),
         V_FLOAT(5, 6),
         V_BOOL(6, 7),
         V_STRING(7, 8);

         public static final int V_SINT_VALUE = 1;
         public static final int V_UINT_VALUE = 2;
         public static final int V_NULL_VALUE = 3;
         public static final int V_OCTETS_VALUE = 4;
         public static final int V_DOUBLE_VALUE = 5;
         public static final int V_FLOAT_VALUE = 6;
         public static final int V_BOOL_VALUE = 7;
         public static final int V_STRING_VALUE = 8;
         private static EnumLiteMap<MysqlxDatatypes.Scalar.Type> internalValueMap = new EnumLiteMap<MysqlxDatatypes.Scalar.Type>() {
            public MysqlxDatatypes.Scalar.Type findValueByNumber(int number) {
               return MysqlxDatatypes.Scalar.Type.valueOf(number);
            }
         };
         private static final MysqlxDatatypes.Scalar.Type[] VALUES = values();
         private final int index;
         private final int value;

         public final int getNumber() {
            return this.value;
         }

         public static MysqlxDatatypes.Scalar.Type valueOf(int value) {
            switch(value) {
               case 1:
                  return V_SINT;
               case 2:
                  return V_UINT;
               case 3:
                  return V_NULL;
               case 4:
                  return V_OCTETS;
               case 5:
                  return V_DOUBLE;
               case 6:
                  return V_FLOAT;
               case 7:
                  return V_BOOL;
               case 8:
                  return V_STRING;
               default:
                  return null;
            }
         }

         public static EnumLiteMap<MysqlxDatatypes.Scalar.Type> internalGetValueMap() {
            return internalValueMap;
         }

         public final EnumValueDescriptor getValueDescriptor() {
            return (EnumValueDescriptor)getDescriptor().getValues().get(this.index);
         }

         public final EnumDescriptor getDescriptorForType() {
            return getDescriptor();
         }

         public static final EnumDescriptor getDescriptor() {
            return (EnumDescriptor)MysqlxDatatypes.Scalar.getDescriptor().getEnumTypes().get(0);
         }

         public static MysqlxDatatypes.Scalar.Type valueOf(EnumValueDescriptor desc) {
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

   public interface ScalarOrBuilder extends MessageOrBuilder {
      boolean hasType();

      MysqlxDatatypes.Scalar.Type getType();

      boolean hasVSignedInt();

      long getVSignedInt();

      boolean hasVUnsignedInt();

      long getVUnsignedInt();

      boolean hasVOctets();

      MysqlxDatatypes.Scalar.Octets getVOctets();

      MysqlxDatatypes.Scalar.OctetsOrBuilder getVOctetsOrBuilder();

      boolean hasVDouble();

      double getVDouble();

      boolean hasVFloat();

      float getVFloat();

      boolean hasVBool();

      boolean getVBool();

      boolean hasVString();

      MysqlxDatatypes.Scalar.String getVString();

      MysqlxDatatypes.Scalar.StringOrBuilder getVStringOrBuilder();
   }
}
