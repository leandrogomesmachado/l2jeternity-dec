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

public final class MysqlxExpr {
   private static final Descriptor internal_static_Mysqlx_Expr_Expr_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(0);
   private static FieldAccessorTable internal_static_Mysqlx_Expr_Expr_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Expr_Expr_descriptor,
      new String[]{"Type", "Identifier", "Variable", "Literal", "FunctionCall", "Operator", "Position", "Object", "Array"}
   );
   private static final Descriptor internal_static_Mysqlx_Expr_Identifier_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(1);
   private static FieldAccessorTable internal_static_Mysqlx_Expr_Identifier_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Expr_Identifier_descriptor, new String[]{"Name", "SchemaName"}
   );
   private static final Descriptor internal_static_Mysqlx_Expr_DocumentPathItem_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(2);
   private static FieldAccessorTable internal_static_Mysqlx_Expr_DocumentPathItem_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Expr_DocumentPathItem_descriptor, new String[]{"Type", "Value", "Index"}
   );
   private static final Descriptor internal_static_Mysqlx_Expr_ColumnIdentifier_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(3);
   private static FieldAccessorTable internal_static_Mysqlx_Expr_ColumnIdentifier_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Expr_ColumnIdentifier_descriptor, new String[]{"DocumentPath", "Name", "TableName", "SchemaName"}
   );
   private static final Descriptor internal_static_Mysqlx_Expr_FunctionCall_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(4);
   private static FieldAccessorTable internal_static_Mysqlx_Expr_FunctionCall_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Expr_FunctionCall_descriptor, new String[]{"Name", "Param"}
   );
   private static final Descriptor internal_static_Mysqlx_Expr_Operator_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(5);
   private static FieldAccessorTable internal_static_Mysqlx_Expr_Operator_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Expr_Operator_descriptor, new String[]{"Name", "Param"}
   );
   private static final Descriptor internal_static_Mysqlx_Expr_Object_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(6);
   private static FieldAccessorTable internal_static_Mysqlx_Expr_Object_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Expr_Object_descriptor, new String[]{"Fld"}
   );
   private static final Descriptor internal_static_Mysqlx_Expr_Object_ObjectField_descriptor = (Descriptor)internal_static_Mysqlx_Expr_Object_descriptor.getNestedTypes(
         
      )
      .get(0);
   private static FieldAccessorTable internal_static_Mysqlx_Expr_Object_ObjectField_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Expr_Object_ObjectField_descriptor, new String[]{"Key", "Value"}
   );
   private static final Descriptor internal_static_Mysqlx_Expr_Array_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(7);
   private static FieldAccessorTable internal_static_Mysqlx_Expr_Array_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Expr_Array_descriptor, new String[]{"Value"}
   );
   private static FileDescriptor descriptor;

   private MysqlxExpr() {
   }

   public static void registerAllExtensions(ExtensionRegistry registry) {
   }

   public static FileDescriptor getDescriptor() {
      return descriptor;
   }

   static {
      String[] descriptorData = new String[]{
         "\n\u0011mysqlx_expr.proto\u0012\u000bMysqlx.Expr\u001a\u0016mysqlx_datatypes.proto\"Ä\u0003\n\u0004Expr\u0012$\n\u0004type\u0018\u0001 \u0002(\u000e2\u0016.Mysqlx.Expr.Expr.Type\u00121\n\nidentifier\u0018\u0002 \u0001(\u000b2\u001d.Mysqlx.Expr.ColumnIdentifier\u0012\u0010\n\bvariable\u0018\u0003 \u0001(\t\u0012)\n\u0007literal\u0018\u0004 \u0001(\u000b2\u0018.Mysqlx.Datatypes.Scalar\u00120\n\rfunction_call\u0018\u0005 \u0001(\u000b2\u0019.Mysqlx.Expr.FunctionCall\u0012'\n\boperator\u0018\u0006 \u0001(\u000b2\u0015.Mysqlx.Expr.Operator\u0012\u0010\n\bposition\u0018\u0007 \u0001(\r\u0012#\n\u0006object\u0018\b \u0001(\u000b2\u0013.Mysqlx.Expr.Object\u0012!\n\u0005array\u0018\t \u0001(\u000b2\u0012.Mysqlx.Expr.Array\"q\n\u0004",
         "Type\u0012\t\n\u0005IDENT\u0010\u0001\u0012\u000b\n\u0007LITERAL\u0010\u0002\u0012\f\n\bVARIABLE\u0010\u0003\u0012\r\n\tFUNC_CALL\u0010\u0004\u0012\f\n\bOPERATOR\u0010\u0005\u0012\u000f\n\u000bPLACEHOLDER\u0010\u0006\u0012\n\n\u0006OBJECT\u0010\u0007\u0012\t\n\u0005ARRAY\u0010\b\"/\n\nIdentifier\u0012\f\n\u0004name\u0018\u0001 \u0002(\t\u0012\u0013\n\u000bschema_name\u0018\u0002 \u0001(\t\"Ë\u0001\n\u0010DocumentPathItem\u00120\n\u0004type\u0018\u0001 \u0002(\u000e2\".Mysqlx.Expr.DocumentPathItem.Type\u0012\r\n\u0005value\u0018\u0002 \u0001(\t\u0012\r\n\u0005index\u0018\u0003 \u0001(\r\"g\n\u0004Type\u0012\n\n\u0006MEMBER\u0010\u0001\u0012\u0013\n\u000fMEMBER_ASTERISK\u0010\u0002\u0012\u000f\n\u000bARRAY_INDEX\u0010\u0003\u0012\u0018\n\u0014ARRAY_INDEX_ASTERISK\u0010\u0004\u0012\u0013\n\u000fDOUBLE_ASTERISK\u0010\u0005\"\u007f\n\u0010ColumnIdentifier\u00124\n\rdocument_p",
         "ath\u0018\u0001 \u0003(\u000b2\u001d.Mysqlx.Expr.DocumentPathItem\u0012\f\n\u0004name\u0018\u0002 \u0001(\t\u0012\u0012\n\ntable_name\u0018\u0003 \u0001(\t\u0012\u0013\n\u000bschema_name\u0018\u0004 \u0001(\t\"W\n\fFunctionCall\u0012%\n\u0004name\u0018\u0001 \u0002(\u000b2\u0017.Mysqlx.Expr.Identifier\u0012 \n\u0005param\u0018\u0002 \u0003(\u000b2\u0011.Mysqlx.Expr.Expr\":\n\bOperator\u0012\f\n\u0004name\u0018\u0001 \u0002(\t\u0012 \n\u0005param\u0018\u0002 \u0003(\u000b2\u0011.Mysqlx.Expr.Expr\"t\n\u0006Object\u0012,\n\u0003fld\u0018\u0001 \u0003(\u000b2\u001f.Mysqlx.Expr.Object.ObjectField\u001a<\n\u000bObjectField\u0012\u000b\n\u0003key\u0018\u0001 \u0002(\t\u0012 \n\u0005value\u0018\u0002 \u0002(\u000b2\u0011.Mysqlx.Expr.Expr\")\n\u0005Array\u0012 \n\u0005value\u0018\u0001 \u0003(\u000b2\u0011.Mysqlx.Expr",
         ".ExprB\u0019\n\u0017com.mysql.cj.x.protobuf"
      };
      InternalDescriptorAssigner assigner = new InternalDescriptorAssigner() {
         public ExtensionRegistry assignDescriptors(FileDescriptor root) {
            MysqlxExpr.descriptor = root;
            return null;
         }
      };
      FileDescriptor.internalBuildGeneratedFileFrom(descriptorData, new FileDescriptor[]{MysqlxDatatypes.getDescriptor()}, assigner);
      MysqlxDatatypes.getDescriptor();
   }

   public static final class Array extends GeneratedMessage implements MysqlxExpr.ArrayOrBuilder {
      private static final MysqlxExpr.Array defaultInstance = new MysqlxExpr.Array(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxExpr.Array> PARSER = new AbstractParser<MysqlxExpr.Array>() {
         public MysqlxExpr.Array parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxExpr.Array(input, extensionRegistry);
         }
      };
      public static final int VALUE_FIELD_NUMBER = 1;
      private List<MysqlxExpr.Expr> value_;
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

      public static MysqlxExpr.Array getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxExpr.Array getDefaultInstanceForType() {
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

                     this.value_.add(input.readMessage(MysqlxExpr.Expr.PARSER, extensionRegistry));
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
         return MysqlxExpr.internal_static_Mysqlx_Expr_Array_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxExpr.internal_static_Mysqlx_Expr_Array_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxExpr.Array.class, MysqlxExpr.Array.Builder.class);
      }

      public Parser<MysqlxExpr.Array> getParserForType() {
         return PARSER;
      }

      @Override
      public List<MysqlxExpr.Expr> getValueList() {
         return this.value_;
      }

      @Override
      public List<? extends MysqlxExpr.ExprOrBuilder> getValueOrBuilderList() {
         return this.value_;
      }

      @Override
      public int getValueCount() {
         return this.value_.size();
      }

      @Override
      public MysqlxExpr.Expr getValue(int index) {
         return this.value_.get(index);
      }

      @Override
      public MysqlxExpr.ExprOrBuilder getValueOrBuilder(int index) {
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

      public static MysqlxExpr.Array parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxExpr.Array)PARSER.parseFrom(data);
      }

      public static MysqlxExpr.Array parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxExpr.Array)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxExpr.Array parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxExpr.Array)PARSER.parseFrom(data);
      }

      public static MysqlxExpr.Array parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxExpr.Array)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxExpr.Array parseFrom(InputStream input) throws IOException {
         return (MysqlxExpr.Array)PARSER.parseFrom(input);
      }

      public static MysqlxExpr.Array parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxExpr.Array)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxExpr.Array parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxExpr.Array)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxExpr.Array parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxExpr.Array)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxExpr.Array parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxExpr.Array)PARSER.parseFrom(input);
      }

      public static MysqlxExpr.Array parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxExpr.Array)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxExpr.Array.Builder newBuilder() {
         return MysqlxExpr.Array.Builder.create();
      }

      public MysqlxExpr.Array.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxExpr.Array.Builder newBuilder(MysqlxExpr.Array prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxExpr.Array.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxExpr.Array.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxExpr.Array.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<MysqlxExpr.Array.Builder> implements MysqlxExpr.ArrayOrBuilder {
         private int bitField0_;
         private List<MysqlxExpr.Expr> value_ = Collections.emptyList();
         private RepeatedFieldBuilder<MysqlxExpr.Expr, MysqlxExpr.Expr.Builder, MysqlxExpr.ExprOrBuilder> valueBuilder_;

         public static final Descriptor getDescriptor() {
            return MysqlxExpr.internal_static_Mysqlx_Expr_Array_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxExpr.internal_static_Mysqlx_Expr_Array_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxExpr.Array.class, MysqlxExpr.Array.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxExpr.Array.alwaysUseFieldBuilders) {
               this.getValueFieldBuilder();
            }
         }

         private static MysqlxExpr.Array.Builder create() {
            return new MysqlxExpr.Array.Builder();
         }

         public MysqlxExpr.Array.Builder clear() {
            super.clear();
            if (this.valueBuilder_ == null) {
               this.value_ = Collections.emptyList();
               this.bitField0_ &= -2;
            } else {
               this.valueBuilder_.clear();
            }

            return this;
         }

         public MysqlxExpr.Array.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxExpr.internal_static_Mysqlx_Expr_Array_descriptor;
         }

         public MysqlxExpr.Array getDefaultInstanceForType() {
            return MysqlxExpr.Array.getDefaultInstance();
         }

         public MysqlxExpr.Array build() {
            MysqlxExpr.Array result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxExpr.Array buildPartial() {
            MysqlxExpr.Array result = new MysqlxExpr.Array(this);
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

         public MysqlxExpr.Array.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxExpr.Array) {
               return this.mergeFrom((MysqlxExpr.Array)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxExpr.Array.Builder mergeFrom(MysqlxExpr.Array other) {
            if (other == MysqlxExpr.Array.getDefaultInstance()) {
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
                     this.valueBuilder_ = MysqlxExpr.Array.alwaysUseFieldBuilders ? this.getValueFieldBuilder() : null;
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

         public MysqlxExpr.Array.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxExpr.Array parsedMessage = null;

            try {
               parsedMessage = (MysqlxExpr.Array)MysqlxExpr.Array.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxExpr.Array)var8.getUnfinishedMessage();
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
         public List<MysqlxExpr.Expr> getValueList() {
            return this.valueBuilder_ == null ? Collections.unmodifiableList(this.value_) : this.valueBuilder_.getMessageList();
         }

         @Override
         public int getValueCount() {
            return this.valueBuilder_ == null ? this.value_.size() : this.valueBuilder_.getCount();
         }

         @Override
         public MysqlxExpr.Expr getValue(int index) {
            return this.valueBuilder_ == null ? this.value_.get(index) : (MysqlxExpr.Expr)this.valueBuilder_.getMessage(index);
         }

         public MysqlxExpr.Array.Builder setValue(int index, MysqlxExpr.Expr value) {
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

         public MysqlxExpr.Array.Builder setValue(int index, MysqlxExpr.Expr.Builder builderForValue) {
            if (this.valueBuilder_ == null) {
               this.ensureValueIsMutable();
               this.value_.set(index, builderForValue.build());
               this.onChanged();
            } else {
               this.valueBuilder_.setMessage(index, builderForValue.build());
            }

            return this;
         }

         public MysqlxExpr.Array.Builder addValue(MysqlxExpr.Expr value) {
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

         public MysqlxExpr.Array.Builder addValue(int index, MysqlxExpr.Expr value) {
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

         public MysqlxExpr.Array.Builder addValue(MysqlxExpr.Expr.Builder builderForValue) {
            if (this.valueBuilder_ == null) {
               this.ensureValueIsMutable();
               this.value_.add(builderForValue.build());
               this.onChanged();
            } else {
               this.valueBuilder_.addMessage(builderForValue.build());
            }

            return this;
         }

         public MysqlxExpr.Array.Builder addValue(int index, MysqlxExpr.Expr.Builder builderForValue) {
            if (this.valueBuilder_ == null) {
               this.ensureValueIsMutable();
               this.value_.add(index, builderForValue.build());
               this.onChanged();
            } else {
               this.valueBuilder_.addMessage(index, builderForValue.build());
            }

            return this;
         }

         public MysqlxExpr.Array.Builder addAllValue(Iterable<? extends MysqlxExpr.Expr> values) {
            if (this.valueBuilder_ == null) {
               this.ensureValueIsMutable();
               com.google.protobuf.AbstractMessageLite.Builder.addAll(values, this.value_);
               this.onChanged();
            } else {
               this.valueBuilder_.addAllMessages(values);
            }

            return this;
         }

         public MysqlxExpr.Array.Builder clearValue() {
            if (this.valueBuilder_ == null) {
               this.value_ = Collections.emptyList();
               this.bitField0_ &= -2;
               this.onChanged();
            } else {
               this.valueBuilder_.clear();
            }

            return this;
         }

         public MysqlxExpr.Array.Builder removeValue(int index) {
            if (this.valueBuilder_ == null) {
               this.ensureValueIsMutable();
               this.value_.remove(index);
               this.onChanged();
            } else {
               this.valueBuilder_.remove(index);
            }

            return this;
         }

         public MysqlxExpr.Expr.Builder getValueBuilder(int index) {
            return (MysqlxExpr.Expr.Builder)this.getValueFieldBuilder().getBuilder(index);
         }

         @Override
         public MysqlxExpr.ExprOrBuilder getValueOrBuilder(int index) {
            return this.valueBuilder_ == null ? this.value_.get(index) : (MysqlxExpr.ExprOrBuilder)this.valueBuilder_.getMessageOrBuilder(index);
         }

         @Override
         public List<? extends MysqlxExpr.ExprOrBuilder> getValueOrBuilderList() {
            return this.valueBuilder_ != null ? this.valueBuilder_.getMessageOrBuilderList() : Collections.unmodifiableList(this.value_);
         }

         public MysqlxExpr.Expr.Builder addValueBuilder() {
            return (MysqlxExpr.Expr.Builder)this.getValueFieldBuilder().addBuilder(MysqlxExpr.Expr.getDefaultInstance());
         }

         public MysqlxExpr.Expr.Builder addValueBuilder(int index) {
            return (MysqlxExpr.Expr.Builder)this.getValueFieldBuilder().addBuilder(index, MysqlxExpr.Expr.getDefaultInstance());
         }

         public List<MysqlxExpr.Expr.Builder> getValueBuilderList() {
            return this.getValueFieldBuilder().getBuilderList();
         }

         private RepeatedFieldBuilder<MysqlxExpr.Expr, MysqlxExpr.Expr.Builder, MysqlxExpr.ExprOrBuilder> getValueFieldBuilder() {
            if (this.valueBuilder_ == null) {
               this.valueBuilder_ = new RepeatedFieldBuilder(this.value_, (this.bitField0_ & 1) == 1, this.getParentForChildren(), this.isClean());
               this.value_ = null;
            }

            return this.valueBuilder_;
         }
      }
   }

   public interface ArrayOrBuilder extends MessageOrBuilder {
      List<MysqlxExpr.Expr> getValueList();

      MysqlxExpr.Expr getValue(int var1);

      int getValueCount();

      List<? extends MysqlxExpr.ExprOrBuilder> getValueOrBuilderList();

      MysqlxExpr.ExprOrBuilder getValueOrBuilder(int var1);
   }

   public static final class ColumnIdentifier extends GeneratedMessage implements MysqlxExpr.ColumnIdentifierOrBuilder {
      private static final MysqlxExpr.ColumnIdentifier defaultInstance = new MysqlxExpr.ColumnIdentifier(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxExpr.ColumnIdentifier> PARSER = new AbstractParser<MysqlxExpr.ColumnIdentifier>() {
         public MysqlxExpr.ColumnIdentifier parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxExpr.ColumnIdentifier(input, extensionRegistry);
         }
      };
      private int bitField0_;
      public static final int DOCUMENT_PATH_FIELD_NUMBER = 1;
      private List<MysqlxExpr.DocumentPathItem> documentPath_;
      public static final int NAME_FIELD_NUMBER = 2;
      private java.lang.Object name_;
      public static final int TABLE_NAME_FIELD_NUMBER = 3;
      private java.lang.Object tableName_;
      public static final int SCHEMA_NAME_FIELD_NUMBER = 4;
      private java.lang.Object schemaName_;
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private ColumnIdentifier(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private ColumnIdentifier(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static MysqlxExpr.ColumnIdentifier getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxExpr.ColumnIdentifier getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private ColumnIdentifier(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                        this.documentPath_ = new ArrayList<>();
                        mutable_bitField0_ |= 1;
                     }

                     this.documentPath_.add(input.readMessage(MysqlxExpr.DocumentPathItem.PARSER, extensionRegistry));
                     break;
                  case 18: {
                     ByteString bs = input.readBytes();
                     this.bitField0_ |= 1;
                     this.name_ = bs;
                     break;
                  }
                  case 26: {
                     ByteString bs = input.readBytes();
                     this.bitField0_ |= 2;
                     this.tableName_ = bs;
                     break;
                  }
                  case 34: {
                     ByteString bs = input.readBytes();
                     this.bitField0_ |= 4;
                     this.schemaName_ = bs;
                     break;
                  }
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
            if ((mutable_bitField0_ & 1) == 1) {
               this.documentPath_ = Collections.unmodifiableList(this.documentPath_);
            }

            this.unknownFields = unknownFields.build();
            this.makeExtensionsImmutable();
         }
      }

      public static final Descriptor getDescriptor() {
         return MysqlxExpr.internal_static_Mysqlx_Expr_ColumnIdentifier_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxExpr.internal_static_Mysqlx_Expr_ColumnIdentifier_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxExpr.ColumnIdentifier.class, MysqlxExpr.ColumnIdentifier.Builder.class);
      }

      public Parser<MysqlxExpr.ColumnIdentifier> getParserForType() {
         return PARSER;
      }

      @Override
      public List<MysqlxExpr.DocumentPathItem> getDocumentPathList() {
         return this.documentPath_;
      }

      @Override
      public List<? extends MysqlxExpr.DocumentPathItemOrBuilder> getDocumentPathOrBuilderList() {
         return this.documentPath_;
      }

      @Override
      public int getDocumentPathCount() {
         return this.documentPath_.size();
      }

      @Override
      public MysqlxExpr.DocumentPathItem getDocumentPath(int index) {
         return this.documentPath_.get(index);
      }

      @Override
      public MysqlxExpr.DocumentPathItemOrBuilder getDocumentPathOrBuilder(int index) {
         return this.documentPath_.get(index);
      }

      @Override
      public boolean hasName() {
         return (this.bitField0_ & 1) == 1;
      }

      @Override
      public String getName() {
         java.lang.Object ref = this.name_;
         if (ref instanceof String) {
            return (String)ref;
         } else {
            ByteString bs = (ByteString)ref;
            String s = bs.toStringUtf8();
            if (bs.isValidUtf8()) {
               this.name_ = s;
            }

            return s;
         }
      }

      @Override
      public ByteString getNameBytes() {
         java.lang.Object ref = this.name_;
         if (ref instanceof String) {
            ByteString b = ByteString.copyFromUtf8((String)ref);
            this.name_ = b;
            return b;
         } else {
            return (ByteString)ref;
         }
      }

      @Override
      public boolean hasTableName() {
         return (this.bitField0_ & 2) == 2;
      }

      @Override
      public String getTableName() {
         java.lang.Object ref = this.tableName_;
         if (ref instanceof String) {
            return (String)ref;
         } else {
            ByteString bs = (ByteString)ref;
            String s = bs.toStringUtf8();
            if (bs.isValidUtf8()) {
               this.tableName_ = s;
            }

            return s;
         }
      }

      @Override
      public ByteString getTableNameBytes() {
         java.lang.Object ref = this.tableName_;
         if (ref instanceof String) {
            ByteString b = ByteString.copyFromUtf8((String)ref);
            this.tableName_ = b;
            return b;
         } else {
            return (ByteString)ref;
         }
      }

      @Override
      public boolean hasSchemaName() {
         return (this.bitField0_ & 4) == 4;
      }

      @Override
      public String getSchemaName() {
         java.lang.Object ref = this.schemaName_;
         if (ref instanceof String) {
            return (String)ref;
         } else {
            ByteString bs = (ByteString)ref;
            String s = bs.toStringUtf8();
            if (bs.isValidUtf8()) {
               this.schemaName_ = s;
            }

            return s;
         }
      }

      @Override
      public ByteString getSchemaNameBytes() {
         java.lang.Object ref = this.schemaName_;
         if (ref instanceof String) {
            ByteString b = ByteString.copyFromUtf8((String)ref);
            this.schemaName_ = b;
            return b;
         } else {
            return (ByteString)ref;
         }
      }

      private void initFields() {
         this.documentPath_ = Collections.emptyList();
         this.name_ = "";
         this.tableName_ = "";
         this.schemaName_ = "";
      }

      public final boolean isInitialized() {
         byte isInitialized = this.memoizedIsInitialized;
         if (isInitialized == 1) {
            return true;
         } else if (isInitialized == 0) {
            return false;
         } else {
            for(int i = 0; i < this.getDocumentPathCount(); ++i) {
               if (!this.getDocumentPath(i).isInitialized()) {
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

         for(int i = 0; i < this.documentPath_.size(); ++i) {
            output.writeMessage(1, (MessageLite)this.documentPath_.get(i));
         }

         if ((this.bitField0_ & 1) == 1) {
            output.writeBytes(2, this.getNameBytes());
         }

         if ((this.bitField0_ & 2) == 2) {
            output.writeBytes(3, this.getTableNameBytes());
         }

         if ((this.bitField0_ & 4) == 4) {
            output.writeBytes(4, this.getSchemaNameBytes());
         }

         this.getUnknownFields().writeTo(output);
      }

      public int getSerializedSize() {
         int size = this.memoizedSerializedSize;
         if (size != -1) {
            return size;
         } else {
            size = 0;

            for(int i = 0; i < this.documentPath_.size(); ++i) {
               size += CodedOutputStream.computeMessageSize(1, (MessageLite)this.documentPath_.get(i));
            }

            if ((this.bitField0_ & 1) == 1) {
               size += CodedOutputStream.computeBytesSize(2, this.getNameBytes());
            }

            if ((this.bitField0_ & 2) == 2) {
               size += CodedOutputStream.computeBytesSize(3, this.getTableNameBytes());
            }

            if ((this.bitField0_ & 4) == 4) {
               size += CodedOutputStream.computeBytesSize(4, this.getSchemaNameBytes());
            }

            size += this.getUnknownFields().getSerializedSize();
            this.memoizedSerializedSize = size;
            return size;
         }
      }

      protected java.lang.Object writeReplace() throws ObjectStreamException {
         return super.writeReplace();
      }

      public static MysqlxExpr.ColumnIdentifier parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxExpr.ColumnIdentifier)PARSER.parseFrom(data);
      }

      public static MysqlxExpr.ColumnIdentifier parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxExpr.ColumnIdentifier)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxExpr.ColumnIdentifier parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxExpr.ColumnIdentifier)PARSER.parseFrom(data);
      }

      public static MysqlxExpr.ColumnIdentifier parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxExpr.ColumnIdentifier)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxExpr.ColumnIdentifier parseFrom(InputStream input) throws IOException {
         return (MysqlxExpr.ColumnIdentifier)PARSER.parseFrom(input);
      }

      public static MysqlxExpr.ColumnIdentifier parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxExpr.ColumnIdentifier)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxExpr.ColumnIdentifier parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxExpr.ColumnIdentifier)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxExpr.ColumnIdentifier parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxExpr.ColumnIdentifier)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxExpr.ColumnIdentifier parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxExpr.ColumnIdentifier)PARSER.parseFrom(input);
      }

      public static MysqlxExpr.ColumnIdentifier parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxExpr.ColumnIdentifier)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxExpr.ColumnIdentifier.Builder newBuilder() {
         return MysqlxExpr.ColumnIdentifier.Builder.create();
      }

      public MysqlxExpr.ColumnIdentifier.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxExpr.ColumnIdentifier.Builder newBuilder(MysqlxExpr.ColumnIdentifier prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxExpr.ColumnIdentifier.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxExpr.ColumnIdentifier.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxExpr.ColumnIdentifier.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxExpr.ColumnIdentifier.Builder>
         implements MysqlxExpr.ColumnIdentifierOrBuilder {
         private int bitField0_;
         private List<MysqlxExpr.DocumentPathItem> documentPath_ = Collections.emptyList();
         private RepeatedFieldBuilder<MysqlxExpr.DocumentPathItem, MysqlxExpr.DocumentPathItem.Builder, MysqlxExpr.DocumentPathItemOrBuilder> documentPathBuilder_;
         private java.lang.Object name_ = "";
         private java.lang.Object tableName_ = "";
         private java.lang.Object schemaName_ = "";

         public static final Descriptor getDescriptor() {
            return MysqlxExpr.internal_static_Mysqlx_Expr_ColumnIdentifier_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxExpr.internal_static_Mysqlx_Expr_ColumnIdentifier_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxExpr.ColumnIdentifier.class, MysqlxExpr.ColumnIdentifier.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxExpr.ColumnIdentifier.alwaysUseFieldBuilders) {
               this.getDocumentPathFieldBuilder();
            }
         }

         private static MysqlxExpr.ColumnIdentifier.Builder create() {
            return new MysqlxExpr.ColumnIdentifier.Builder();
         }

         public MysqlxExpr.ColumnIdentifier.Builder clear() {
            super.clear();
            if (this.documentPathBuilder_ == null) {
               this.documentPath_ = Collections.emptyList();
               this.bitField0_ &= -2;
            } else {
               this.documentPathBuilder_.clear();
            }

            this.name_ = "";
            this.bitField0_ &= -3;
            this.tableName_ = "";
            this.bitField0_ &= -5;
            this.schemaName_ = "";
            this.bitField0_ &= -9;
            return this;
         }

         public MysqlxExpr.ColumnIdentifier.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxExpr.internal_static_Mysqlx_Expr_ColumnIdentifier_descriptor;
         }

         public MysqlxExpr.ColumnIdentifier getDefaultInstanceForType() {
            return MysqlxExpr.ColumnIdentifier.getDefaultInstance();
         }

         public MysqlxExpr.ColumnIdentifier build() {
            MysqlxExpr.ColumnIdentifier result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxExpr.ColumnIdentifier buildPartial() {
            MysqlxExpr.ColumnIdentifier result = new MysqlxExpr.ColumnIdentifier(this);
            int from_bitField0_ = this.bitField0_;
            int to_bitField0_ = 0;
            if (this.documentPathBuilder_ == null) {
               if ((this.bitField0_ & 1) == 1) {
                  this.documentPath_ = Collections.unmodifiableList(this.documentPath_);
                  this.bitField0_ &= -2;
               }

               result.documentPath_ = this.documentPath_;
            } else {
               result.documentPath_ = this.documentPathBuilder_.build();
            }

            if ((from_bitField0_ & 2) == 2) {
               to_bitField0_ |= 1;
            }

            result.name_ = this.name_;
            if ((from_bitField0_ & 4) == 4) {
               to_bitField0_ |= 2;
            }

            result.tableName_ = this.tableName_;
            if ((from_bitField0_ & 8) == 8) {
               to_bitField0_ |= 4;
            }

            result.schemaName_ = this.schemaName_;
            result.bitField0_ = to_bitField0_;
            this.onBuilt();
            return result;
         }

         public MysqlxExpr.ColumnIdentifier.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxExpr.ColumnIdentifier) {
               return this.mergeFrom((MysqlxExpr.ColumnIdentifier)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxExpr.ColumnIdentifier.Builder mergeFrom(MysqlxExpr.ColumnIdentifier other) {
            if (other == MysqlxExpr.ColumnIdentifier.getDefaultInstance()) {
               return this;
            } else {
               if (this.documentPathBuilder_ == null) {
                  if (!other.documentPath_.isEmpty()) {
                     if (this.documentPath_.isEmpty()) {
                        this.documentPath_ = other.documentPath_;
                        this.bitField0_ &= -2;
                     } else {
                        this.ensureDocumentPathIsMutable();
                        this.documentPath_.addAll(other.documentPath_);
                     }

                     this.onChanged();
                  }
               } else if (!other.documentPath_.isEmpty()) {
                  if (this.documentPathBuilder_.isEmpty()) {
                     this.documentPathBuilder_.dispose();
                     this.documentPathBuilder_ = null;
                     this.documentPath_ = other.documentPath_;
                     this.bitField0_ &= -2;
                     this.documentPathBuilder_ = MysqlxExpr.ColumnIdentifier.alwaysUseFieldBuilders ? this.getDocumentPathFieldBuilder() : null;
                  } else {
                     this.documentPathBuilder_.addAllMessages(other.documentPath_);
                  }
               }

               if (other.hasName()) {
                  this.bitField0_ |= 2;
                  this.name_ = other.name_;
                  this.onChanged();
               }

               if (other.hasTableName()) {
                  this.bitField0_ |= 4;
                  this.tableName_ = other.tableName_;
                  this.onChanged();
               }

               if (other.hasSchemaName()) {
                  this.bitField0_ |= 8;
                  this.schemaName_ = other.schemaName_;
                  this.onChanged();
               }

               this.mergeUnknownFields(other.getUnknownFields());
               return this;
            }
         }

         public final boolean isInitialized() {
            for(int i = 0; i < this.getDocumentPathCount(); ++i) {
               if (!this.getDocumentPath(i).isInitialized()) {
                  return false;
               }
            }

            return true;
         }

         public MysqlxExpr.ColumnIdentifier.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxExpr.ColumnIdentifier parsedMessage = null;

            try {
               parsedMessage = (MysqlxExpr.ColumnIdentifier)MysqlxExpr.ColumnIdentifier.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxExpr.ColumnIdentifier)var8.getUnfinishedMessage();
               throw var8;
            } finally {
               if (parsedMessage != null) {
                  this.mergeFrom(parsedMessage);
               }
            }

            return this;
         }

         private void ensureDocumentPathIsMutable() {
            if ((this.bitField0_ & 1) != 1) {
               this.documentPath_ = new ArrayList<>(this.documentPath_);
               this.bitField0_ |= 1;
            }
         }

         @Override
         public List<MysqlxExpr.DocumentPathItem> getDocumentPathList() {
            return this.documentPathBuilder_ == null ? Collections.unmodifiableList(this.documentPath_) : this.documentPathBuilder_.getMessageList();
         }

         @Override
         public int getDocumentPathCount() {
            return this.documentPathBuilder_ == null ? this.documentPath_.size() : this.documentPathBuilder_.getCount();
         }

         @Override
         public MysqlxExpr.DocumentPathItem getDocumentPath(int index) {
            return this.documentPathBuilder_ == null
               ? this.documentPath_.get(index)
               : (MysqlxExpr.DocumentPathItem)this.documentPathBuilder_.getMessage(index);
         }

         public MysqlxExpr.ColumnIdentifier.Builder setDocumentPath(int index, MysqlxExpr.DocumentPathItem value) {
            if (this.documentPathBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureDocumentPathIsMutable();
               this.documentPath_.set(index, value);
               this.onChanged();
            } else {
               this.documentPathBuilder_.setMessage(index, value);
            }

            return this;
         }

         public MysqlxExpr.ColumnIdentifier.Builder setDocumentPath(int index, MysqlxExpr.DocumentPathItem.Builder builderForValue) {
            if (this.documentPathBuilder_ == null) {
               this.ensureDocumentPathIsMutable();
               this.documentPath_.set(index, builderForValue.build());
               this.onChanged();
            } else {
               this.documentPathBuilder_.setMessage(index, builderForValue.build());
            }

            return this;
         }

         public MysqlxExpr.ColumnIdentifier.Builder addDocumentPath(MysqlxExpr.DocumentPathItem value) {
            if (this.documentPathBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureDocumentPathIsMutable();
               this.documentPath_.add(value);
               this.onChanged();
            } else {
               this.documentPathBuilder_.addMessage(value);
            }

            return this;
         }

         public MysqlxExpr.ColumnIdentifier.Builder addDocumentPath(int index, MysqlxExpr.DocumentPathItem value) {
            if (this.documentPathBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureDocumentPathIsMutable();
               this.documentPath_.add(index, value);
               this.onChanged();
            } else {
               this.documentPathBuilder_.addMessage(index, value);
            }

            return this;
         }

         public MysqlxExpr.ColumnIdentifier.Builder addDocumentPath(MysqlxExpr.DocumentPathItem.Builder builderForValue) {
            if (this.documentPathBuilder_ == null) {
               this.ensureDocumentPathIsMutable();
               this.documentPath_.add(builderForValue.build());
               this.onChanged();
            } else {
               this.documentPathBuilder_.addMessage(builderForValue.build());
            }

            return this;
         }

         public MysqlxExpr.ColumnIdentifier.Builder addDocumentPath(int index, MysqlxExpr.DocumentPathItem.Builder builderForValue) {
            if (this.documentPathBuilder_ == null) {
               this.ensureDocumentPathIsMutable();
               this.documentPath_.add(index, builderForValue.build());
               this.onChanged();
            } else {
               this.documentPathBuilder_.addMessage(index, builderForValue.build());
            }

            return this;
         }

         public MysqlxExpr.ColumnIdentifier.Builder addAllDocumentPath(Iterable<? extends MysqlxExpr.DocumentPathItem> values) {
            if (this.documentPathBuilder_ == null) {
               this.ensureDocumentPathIsMutable();
               com.google.protobuf.AbstractMessageLite.Builder.addAll(values, this.documentPath_);
               this.onChanged();
            } else {
               this.documentPathBuilder_.addAllMessages(values);
            }

            return this;
         }

         public MysqlxExpr.ColumnIdentifier.Builder clearDocumentPath() {
            if (this.documentPathBuilder_ == null) {
               this.documentPath_ = Collections.emptyList();
               this.bitField0_ &= -2;
               this.onChanged();
            } else {
               this.documentPathBuilder_.clear();
            }

            return this;
         }

         public MysqlxExpr.ColumnIdentifier.Builder removeDocumentPath(int index) {
            if (this.documentPathBuilder_ == null) {
               this.ensureDocumentPathIsMutable();
               this.documentPath_.remove(index);
               this.onChanged();
            } else {
               this.documentPathBuilder_.remove(index);
            }

            return this;
         }

         public MysqlxExpr.DocumentPathItem.Builder getDocumentPathBuilder(int index) {
            return (MysqlxExpr.DocumentPathItem.Builder)this.getDocumentPathFieldBuilder().getBuilder(index);
         }

         @Override
         public MysqlxExpr.DocumentPathItemOrBuilder getDocumentPathOrBuilder(int index) {
            return this.documentPathBuilder_ == null
               ? this.documentPath_.get(index)
               : (MysqlxExpr.DocumentPathItemOrBuilder)this.documentPathBuilder_.getMessageOrBuilder(index);
         }

         @Override
         public List<? extends MysqlxExpr.DocumentPathItemOrBuilder> getDocumentPathOrBuilderList() {
            return this.documentPathBuilder_ != null ? this.documentPathBuilder_.getMessageOrBuilderList() : Collections.unmodifiableList(this.documentPath_);
         }

         public MysqlxExpr.DocumentPathItem.Builder addDocumentPathBuilder() {
            return (MysqlxExpr.DocumentPathItem.Builder)this.getDocumentPathFieldBuilder().addBuilder(MysqlxExpr.DocumentPathItem.getDefaultInstance());
         }

         public MysqlxExpr.DocumentPathItem.Builder addDocumentPathBuilder(int index) {
            return (MysqlxExpr.DocumentPathItem.Builder)this.getDocumentPathFieldBuilder().addBuilder(index, MysqlxExpr.DocumentPathItem.getDefaultInstance());
         }

         public List<MysqlxExpr.DocumentPathItem.Builder> getDocumentPathBuilderList() {
            return this.getDocumentPathFieldBuilder().getBuilderList();
         }

         private RepeatedFieldBuilder<MysqlxExpr.DocumentPathItem, MysqlxExpr.DocumentPathItem.Builder, MysqlxExpr.DocumentPathItemOrBuilder> getDocumentPathFieldBuilder() {
            if (this.documentPathBuilder_ == null) {
               this.documentPathBuilder_ = new RepeatedFieldBuilder(
                  this.documentPath_, (this.bitField0_ & 1) == 1, this.getParentForChildren(), this.isClean()
               );
               this.documentPath_ = null;
            }

            return this.documentPathBuilder_;
         }

         @Override
         public boolean hasName() {
            return (this.bitField0_ & 2) == 2;
         }

         @Override
         public String getName() {
            java.lang.Object ref = this.name_;
            if (!(ref instanceof String)) {
               ByteString bs = (ByteString)ref;
               String s = bs.toStringUtf8();
               if (bs.isValidUtf8()) {
                  this.name_ = s;
               }

               return s;
            } else {
               return (String)ref;
            }
         }

         @Override
         public ByteString getNameBytes() {
            java.lang.Object ref = this.name_;
            if (ref instanceof String) {
               ByteString b = ByteString.copyFromUtf8((String)ref);
               this.name_ = b;
               return b;
            } else {
               return (ByteString)ref;
            }
         }

         public MysqlxExpr.ColumnIdentifier.Builder setName(String value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 2;
               this.name_ = value;
               this.onChanged();
               return this;
            }
         }

         public MysqlxExpr.ColumnIdentifier.Builder clearName() {
            this.bitField0_ &= -3;
            this.name_ = MysqlxExpr.ColumnIdentifier.getDefaultInstance().getName();
            this.onChanged();
            return this;
         }

         public MysqlxExpr.ColumnIdentifier.Builder setNameBytes(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 2;
               this.name_ = value;
               this.onChanged();
               return this;
            }
         }

         @Override
         public boolean hasTableName() {
            return (this.bitField0_ & 4) == 4;
         }

         @Override
         public String getTableName() {
            java.lang.Object ref = this.tableName_;
            if (!(ref instanceof String)) {
               ByteString bs = (ByteString)ref;
               String s = bs.toStringUtf8();
               if (bs.isValidUtf8()) {
                  this.tableName_ = s;
               }

               return s;
            } else {
               return (String)ref;
            }
         }

         @Override
         public ByteString getTableNameBytes() {
            java.lang.Object ref = this.tableName_;
            if (ref instanceof String) {
               ByteString b = ByteString.copyFromUtf8((String)ref);
               this.tableName_ = b;
               return b;
            } else {
               return (ByteString)ref;
            }
         }

         public MysqlxExpr.ColumnIdentifier.Builder setTableName(String value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 4;
               this.tableName_ = value;
               this.onChanged();
               return this;
            }
         }

         public MysqlxExpr.ColumnIdentifier.Builder clearTableName() {
            this.bitField0_ &= -5;
            this.tableName_ = MysqlxExpr.ColumnIdentifier.getDefaultInstance().getTableName();
            this.onChanged();
            return this;
         }

         public MysqlxExpr.ColumnIdentifier.Builder setTableNameBytes(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 4;
               this.tableName_ = value;
               this.onChanged();
               return this;
            }
         }

         @Override
         public boolean hasSchemaName() {
            return (this.bitField0_ & 8) == 8;
         }

         @Override
         public String getSchemaName() {
            java.lang.Object ref = this.schemaName_;
            if (!(ref instanceof String)) {
               ByteString bs = (ByteString)ref;
               String s = bs.toStringUtf8();
               if (bs.isValidUtf8()) {
                  this.schemaName_ = s;
               }

               return s;
            } else {
               return (String)ref;
            }
         }

         @Override
         public ByteString getSchemaNameBytes() {
            java.lang.Object ref = this.schemaName_;
            if (ref instanceof String) {
               ByteString b = ByteString.copyFromUtf8((String)ref);
               this.schemaName_ = b;
               return b;
            } else {
               return (ByteString)ref;
            }
         }

         public MysqlxExpr.ColumnIdentifier.Builder setSchemaName(String value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 8;
               this.schemaName_ = value;
               this.onChanged();
               return this;
            }
         }

         public MysqlxExpr.ColumnIdentifier.Builder clearSchemaName() {
            this.bitField0_ &= -9;
            this.schemaName_ = MysqlxExpr.ColumnIdentifier.getDefaultInstance().getSchemaName();
            this.onChanged();
            return this;
         }

         public MysqlxExpr.ColumnIdentifier.Builder setSchemaNameBytes(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 8;
               this.schemaName_ = value;
               this.onChanged();
               return this;
            }
         }
      }
   }

   public interface ColumnIdentifierOrBuilder extends MessageOrBuilder {
      List<MysqlxExpr.DocumentPathItem> getDocumentPathList();

      MysqlxExpr.DocumentPathItem getDocumentPath(int var1);

      int getDocumentPathCount();

      List<? extends MysqlxExpr.DocumentPathItemOrBuilder> getDocumentPathOrBuilderList();

      MysqlxExpr.DocumentPathItemOrBuilder getDocumentPathOrBuilder(int var1);

      boolean hasName();

      String getName();

      ByteString getNameBytes();

      boolean hasTableName();

      String getTableName();

      ByteString getTableNameBytes();

      boolean hasSchemaName();

      String getSchemaName();

      ByteString getSchemaNameBytes();
   }

   public static final class DocumentPathItem extends GeneratedMessage implements MysqlxExpr.DocumentPathItemOrBuilder {
      private static final MysqlxExpr.DocumentPathItem defaultInstance = new MysqlxExpr.DocumentPathItem(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxExpr.DocumentPathItem> PARSER = new AbstractParser<MysqlxExpr.DocumentPathItem>() {
         public MysqlxExpr.DocumentPathItem parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxExpr.DocumentPathItem(input, extensionRegistry);
         }
      };
      private int bitField0_;
      public static final int TYPE_FIELD_NUMBER = 1;
      private MysqlxExpr.DocumentPathItem.Type type_;
      public static final int VALUE_FIELD_NUMBER = 2;
      private java.lang.Object value_;
      public static final int INDEX_FIELD_NUMBER = 3;
      private int index_;
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private DocumentPathItem(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private DocumentPathItem(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static MysqlxExpr.DocumentPathItem getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxExpr.DocumentPathItem getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private DocumentPathItem(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                     MysqlxExpr.DocumentPathItem.Type value = MysqlxExpr.DocumentPathItem.Type.valueOf(rawValue);
                     if (value == null) {
                        unknownFields.mergeVarintField(1, rawValue);
                     } else {
                        this.bitField0_ |= 1;
                        this.type_ = value;
                     }
                     break;
                  case 18:
                     ByteString bs = input.readBytes();
                     this.bitField0_ |= 2;
                     this.value_ = bs;
                     break;
                  case 24:
                     this.bitField0_ |= 4;
                     this.index_ = input.readUInt32();
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
         return MysqlxExpr.internal_static_Mysqlx_Expr_DocumentPathItem_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxExpr.internal_static_Mysqlx_Expr_DocumentPathItem_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxExpr.DocumentPathItem.class, MysqlxExpr.DocumentPathItem.Builder.class);
      }

      public Parser<MysqlxExpr.DocumentPathItem> getParserForType() {
         return PARSER;
      }

      @Override
      public boolean hasType() {
         return (this.bitField0_ & 1) == 1;
      }

      @Override
      public MysqlxExpr.DocumentPathItem.Type getType() {
         return this.type_;
      }

      @Override
      public boolean hasValue() {
         return (this.bitField0_ & 2) == 2;
      }

      @Override
      public String getValue() {
         java.lang.Object ref = this.value_;
         if (ref instanceof String) {
            return (String)ref;
         } else {
            ByteString bs = (ByteString)ref;
            String s = bs.toStringUtf8();
            if (bs.isValidUtf8()) {
               this.value_ = s;
            }

            return s;
         }
      }

      @Override
      public ByteString getValueBytes() {
         java.lang.Object ref = this.value_;
         if (ref instanceof String) {
            ByteString b = ByteString.copyFromUtf8((String)ref);
            this.value_ = b;
            return b;
         } else {
            return (ByteString)ref;
         }
      }

      @Override
      public boolean hasIndex() {
         return (this.bitField0_ & 4) == 4;
      }

      @Override
      public int getIndex() {
         return this.index_;
      }

      private void initFields() {
         this.type_ = MysqlxExpr.DocumentPathItem.Type.MEMBER;
         this.value_ = "";
         this.index_ = 0;
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
            output.writeBytes(2, this.getValueBytes());
         }

         if ((this.bitField0_ & 4) == 4) {
            output.writeUInt32(3, this.index_);
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
               size += CodedOutputStream.computeBytesSize(2, this.getValueBytes());
            }

            if ((this.bitField0_ & 4) == 4) {
               size += CodedOutputStream.computeUInt32Size(3, this.index_);
            }

            size += this.getUnknownFields().getSerializedSize();
            this.memoizedSerializedSize = size;
            return size;
         }
      }

      protected java.lang.Object writeReplace() throws ObjectStreamException {
         return super.writeReplace();
      }

      public static MysqlxExpr.DocumentPathItem parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxExpr.DocumentPathItem)PARSER.parseFrom(data);
      }

      public static MysqlxExpr.DocumentPathItem parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxExpr.DocumentPathItem)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxExpr.DocumentPathItem parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxExpr.DocumentPathItem)PARSER.parseFrom(data);
      }

      public static MysqlxExpr.DocumentPathItem parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxExpr.DocumentPathItem)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxExpr.DocumentPathItem parseFrom(InputStream input) throws IOException {
         return (MysqlxExpr.DocumentPathItem)PARSER.parseFrom(input);
      }

      public static MysqlxExpr.DocumentPathItem parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxExpr.DocumentPathItem)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxExpr.DocumentPathItem parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxExpr.DocumentPathItem)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxExpr.DocumentPathItem parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxExpr.DocumentPathItem)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxExpr.DocumentPathItem parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxExpr.DocumentPathItem)PARSER.parseFrom(input);
      }

      public static MysqlxExpr.DocumentPathItem parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxExpr.DocumentPathItem)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxExpr.DocumentPathItem.Builder newBuilder() {
         return MysqlxExpr.DocumentPathItem.Builder.create();
      }

      public MysqlxExpr.DocumentPathItem.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxExpr.DocumentPathItem.Builder newBuilder(MysqlxExpr.DocumentPathItem prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxExpr.DocumentPathItem.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxExpr.DocumentPathItem.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxExpr.DocumentPathItem.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxExpr.DocumentPathItem.Builder>
         implements MysqlxExpr.DocumentPathItemOrBuilder {
         private int bitField0_;
         private MysqlxExpr.DocumentPathItem.Type type_ = MysqlxExpr.DocumentPathItem.Type.MEMBER;
         private java.lang.Object value_ = "";
         private int index_;

         public static final Descriptor getDescriptor() {
            return MysqlxExpr.internal_static_Mysqlx_Expr_DocumentPathItem_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxExpr.internal_static_Mysqlx_Expr_DocumentPathItem_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxExpr.DocumentPathItem.class, MysqlxExpr.DocumentPathItem.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxExpr.DocumentPathItem.alwaysUseFieldBuilders) {
            }
         }

         private static MysqlxExpr.DocumentPathItem.Builder create() {
            return new MysqlxExpr.DocumentPathItem.Builder();
         }

         public MysqlxExpr.DocumentPathItem.Builder clear() {
            super.clear();
            this.type_ = MysqlxExpr.DocumentPathItem.Type.MEMBER;
            this.bitField0_ &= -2;
            this.value_ = "";
            this.bitField0_ &= -3;
            this.index_ = 0;
            this.bitField0_ &= -5;
            return this;
         }

         public MysqlxExpr.DocumentPathItem.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxExpr.internal_static_Mysqlx_Expr_DocumentPathItem_descriptor;
         }

         public MysqlxExpr.DocumentPathItem getDefaultInstanceForType() {
            return MysqlxExpr.DocumentPathItem.getDefaultInstance();
         }

         public MysqlxExpr.DocumentPathItem build() {
            MysqlxExpr.DocumentPathItem result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxExpr.DocumentPathItem buildPartial() {
            MysqlxExpr.DocumentPathItem result = new MysqlxExpr.DocumentPathItem(this);
            int from_bitField0_ = this.bitField0_;
            int to_bitField0_ = 0;
            if ((from_bitField0_ & 1) == 1) {
               to_bitField0_ |= 1;
            }

            result.type_ = this.type_;
            if ((from_bitField0_ & 2) == 2) {
               to_bitField0_ |= 2;
            }

            result.value_ = this.value_;
            if ((from_bitField0_ & 4) == 4) {
               to_bitField0_ |= 4;
            }

            result.index_ = this.index_;
            result.bitField0_ = to_bitField0_;
            this.onBuilt();
            return result;
         }

         public MysqlxExpr.DocumentPathItem.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxExpr.DocumentPathItem) {
               return this.mergeFrom((MysqlxExpr.DocumentPathItem)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxExpr.DocumentPathItem.Builder mergeFrom(MysqlxExpr.DocumentPathItem other) {
            if (other == MysqlxExpr.DocumentPathItem.getDefaultInstance()) {
               return this;
            } else {
               if (other.hasType()) {
                  this.setType(other.getType());
               }

               if (other.hasValue()) {
                  this.bitField0_ |= 2;
                  this.value_ = other.value_;
                  this.onChanged();
               }

               if (other.hasIndex()) {
                  this.setIndex(other.getIndex());
               }

               this.mergeUnknownFields(other.getUnknownFields());
               return this;
            }
         }

         public final boolean isInitialized() {
            return this.hasType();
         }

         public MysqlxExpr.DocumentPathItem.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxExpr.DocumentPathItem parsedMessage = null;

            try {
               parsedMessage = (MysqlxExpr.DocumentPathItem)MysqlxExpr.DocumentPathItem.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxExpr.DocumentPathItem)var8.getUnfinishedMessage();
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
         public MysqlxExpr.DocumentPathItem.Type getType() {
            return this.type_;
         }

         public MysqlxExpr.DocumentPathItem.Builder setType(MysqlxExpr.DocumentPathItem.Type value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 1;
               this.type_ = value;
               this.onChanged();
               return this;
            }
         }

         public MysqlxExpr.DocumentPathItem.Builder clearType() {
            this.bitField0_ &= -2;
            this.type_ = MysqlxExpr.DocumentPathItem.Type.MEMBER;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasValue() {
            return (this.bitField0_ & 2) == 2;
         }

         @Override
         public String getValue() {
            java.lang.Object ref = this.value_;
            if (!(ref instanceof String)) {
               ByteString bs = (ByteString)ref;
               String s = bs.toStringUtf8();
               if (bs.isValidUtf8()) {
                  this.value_ = s;
               }

               return s;
            } else {
               return (String)ref;
            }
         }

         @Override
         public ByteString getValueBytes() {
            java.lang.Object ref = this.value_;
            if (ref instanceof String) {
               ByteString b = ByteString.copyFromUtf8((String)ref);
               this.value_ = b;
               return b;
            } else {
               return (ByteString)ref;
            }
         }

         public MysqlxExpr.DocumentPathItem.Builder setValue(String value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 2;
               this.value_ = value;
               this.onChanged();
               return this;
            }
         }

         public MysqlxExpr.DocumentPathItem.Builder clearValue() {
            this.bitField0_ &= -3;
            this.value_ = MysqlxExpr.DocumentPathItem.getDefaultInstance().getValue();
            this.onChanged();
            return this;
         }

         public MysqlxExpr.DocumentPathItem.Builder setValueBytes(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 2;
               this.value_ = value;
               this.onChanged();
               return this;
            }
         }

         @Override
         public boolean hasIndex() {
            return (this.bitField0_ & 4) == 4;
         }

         @Override
         public int getIndex() {
            return this.index_;
         }

         public MysqlxExpr.DocumentPathItem.Builder setIndex(int value) {
            this.bitField0_ |= 4;
            this.index_ = value;
            this.onChanged();
            return this;
         }

         public MysqlxExpr.DocumentPathItem.Builder clearIndex() {
            this.bitField0_ &= -5;
            this.index_ = 0;
            this.onChanged();
            return this;
         }
      }

      public static enum Type implements ProtocolMessageEnum {
         MEMBER(0, 1),
         MEMBER_ASTERISK(1, 2),
         ARRAY_INDEX(2, 3),
         ARRAY_INDEX_ASTERISK(3, 4),
         DOUBLE_ASTERISK(4, 5);

         public static final int MEMBER_VALUE = 1;
         public static final int MEMBER_ASTERISK_VALUE = 2;
         public static final int ARRAY_INDEX_VALUE = 3;
         public static final int ARRAY_INDEX_ASTERISK_VALUE = 4;
         public static final int DOUBLE_ASTERISK_VALUE = 5;
         private static EnumLiteMap<MysqlxExpr.DocumentPathItem.Type> internalValueMap = new EnumLiteMap<MysqlxExpr.DocumentPathItem.Type>() {
            public MysqlxExpr.DocumentPathItem.Type findValueByNumber(int number) {
               return MysqlxExpr.DocumentPathItem.Type.valueOf(number);
            }
         };
         private static final MysqlxExpr.DocumentPathItem.Type[] VALUES = values();
         private final int index;
         private final int value;

         public final int getNumber() {
            return this.value;
         }

         public static MysqlxExpr.DocumentPathItem.Type valueOf(int value) {
            switch(value) {
               case 1:
                  return MEMBER;
               case 2:
                  return MEMBER_ASTERISK;
               case 3:
                  return ARRAY_INDEX;
               case 4:
                  return ARRAY_INDEX_ASTERISK;
               case 5:
                  return DOUBLE_ASTERISK;
               default:
                  return null;
            }
         }

         public static EnumLiteMap<MysqlxExpr.DocumentPathItem.Type> internalGetValueMap() {
            return internalValueMap;
         }

         public final EnumValueDescriptor getValueDescriptor() {
            return (EnumValueDescriptor)getDescriptor().getValues().get(this.index);
         }

         public final EnumDescriptor getDescriptorForType() {
            return getDescriptor();
         }

         public static final EnumDescriptor getDescriptor() {
            return (EnumDescriptor)MysqlxExpr.DocumentPathItem.getDescriptor().getEnumTypes().get(0);
         }

         public static MysqlxExpr.DocumentPathItem.Type valueOf(EnumValueDescriptor desc) {
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

   public interface DocumentPathItemOrBuilder extends MessageOrBuilder {
      boolean hasType();

      MysqlxExpr.DocumentPathItem.Type getType();

      boolean hasValue();

      String getValue();

      ByteString getValueBytes();

      boolean hasIndex();

      int getIndex();
   }

   public static final class Expr extends GeneratedMessage implements MysqlxExpr.ExprOrBuilder {
      private static final MysqlxExpr.Expr defaultInstance = new MysqlxExpr.Expr(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxExpr.Expr> PARSER = new AbstractParser<MysqlxExpr.Expr>() {
         public MysqlxExpr.Expr parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxExpr.Expr(input, extensionRegistry);
         }
      };
      private int bitField0_;
      public static final int TYPE_FIELD_NUMBER = 1;
      private MysqlxExpr.Expr.Type type_;
      public static final int IDENTIFIER_FIELD_NUMBER = 2;
      private MysqlxExpr.ColumnIdentifier identifier_;
      public static final int VARIABLE_FIELD_NUMBER = 3;
      private java.lang.Object variable_;
      public static final int LITERAL_FIELD_NUMBER = 4;
      private MysqlxDatatypes.Scalar literal_;
      public static final int FUNCTION_CALL_FIELD_NUMBER = 5;
      private MysqlxExpr.FunctionCall functionCall_;
      public static final int OPERATOR_FIELD_NUMBER = 6;
      private MysqlxExpr.Operator operator_;
      public static final int POSITION_FIELD_NUMBER = 7;
      private int position_;
      public static final int OBJECT_FIELD_NUMBER = 8;
      private MysqlxExpr.Object object_;
      public static final int ARRAY_FIELD_NUMBER = 9;
      private MysqlxExpr.Array array_;
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private Expr(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private Expr(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static MysqlxExpr.Expr getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxExpr.Expr getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private Expr(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                     MysqlxExpr.Expr.Type value = MysqlxExpr.Expr.Type.valueOf(rawValue);
                     if (value == null) {
                        unknownFields.mergeVarintField(1, rawValue);
                     } else {
                        this.bitField0_ |= 1;
                        this.type_ = value;
                     }
                     break;
                  case 18:
                     MysqlxExpr.ColumnIdentifier.Builder subBuilder = null;
                     if ((this.bitField0_ & 2) == 2) {
                        subBuilder = this.identifier_.toBuilder();
                     }

                     this.identifier_ = (MysqlxExpr.ColumnIdentifier)input.readMessage(MysqlxExpr.ColumnIdentifier.PARSER, extensionRegistry);
                     if (subBuilder != null) {
                        subBuilder.mergeFrom(this.identifier_);
                        this.identifier_ = subBuilder.buildPartial();
                     }

                     this.bitField0_ |= 2;
                     break;
                  case 26:
                     ByteString bs = input.readBytes();
                     this.bitField0_ |= 4;
                     this.variable_ = bs;
                     break;
                  case 34:
                     MysqlxDatatypes.Scalar.Builder subBuilder = null;
                     if ((this.bitField0_ & 8) == 8) {
                        subBuilder = this.literal_.toBuilder();
                     }

                     this.literal_ = (MysqlxDatatypes.Scalar)input.readMessage(MysqlxDatatypes.Scalar.PARSER, extensionRegistry);
                     if (subBuilder != null) {
                        subBuilder.mergeFrom(this.literal_);
                        this.literal_ = subBuilder.buildPartial();
                     }

                     this.bitField0_ |= 8;
                     break;
                  case 42:
                     MysqlxExpr.FunctionCall.Builder subBuilder = null;
                     if ((this.bitField0_ & 16) == 16) {
                        subBuilder = this.functionCall_.toBuilder();
                     }

                     this.functionCall_ = (MysqlxExpr.FunctionCall)input.readMessage(MysqlxExpr.FunctionCall.PARSER, extensionRegistry);
                     if (subBuilder != null) {
                        subBuilder.mergeFrom(this.functionCall_);
                        this.functionCall_ = subBuilder.buildPartial();
                     }

                     this.bitField0_ |= 16;
                     break;
                  case 50:
                     MysqlxExpr.Operator.Builder subBuilder = null;
                     if ((this.bitField0_ & 32) == 32) {
                        subBuilder = this.operator_.toBuilder();
                     }

                     this.operator_ = (MysqlxExpr.Operator)input.readMessage(MysqlxExpr.Operator.PARSER, extensionRegistry);
                     if (subBuilder != null) {
                        subBuilder.mergeFrom(this.operator_);
                        this.operator_ = subBuilder.buildPartial();
                     }

                     this.bitField0_ |= 32;
                     break;
                  case 56:
                     this.bitField0_ |= 64;
                     this.position_ = input.readUInt32();
                     break;
                  case 66:
                     MysqlxExpr.Object.Builder subBuilder = null;
                     if ((this.bitField0_ & 128) == 128) {
                        subBuilder = this.object_.toBuilder();
                     }

                     this.object_ = (MysqlxExpr.Object)input.readMessage(MysqlxExpr.Object.PARSER, extensionRegistry);
                     if (subBuilder != null) {
                        subBuilder.mergeFrom(this.object_);
                        this.object_ = subBuilder.buildPartial();
                     }

                     this.bitField0_ |= 128;
                     break;
                  case 74:
                     MysqlxExpr.Array.Builder subBuilder = null;
                     if ((this.bitField0_ & 256) == 256) {
                        subBuilder = this.array_.toBuilder();
                     }

                     this.array_ = (MysqlxExpr.Array)input.readMessage(MysqlxExpr.Array.PARSER, extensionRegistry);
                     if (subBuilder != null) {
                        subBuilder.mergeFrom(this.array_);
                        this.array_ = subBuilder.buildPartial();
                     }

                     this.bitField0_ |= 256;
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
         return MysqlxExpr.internal_static_Mysqlx_Expr_Expr_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxExpr.internal_static_Mysqlx_Expr_Expr_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxExpr.Expr.class, MysqlxExpr.Expr.Builder.class);
      }

      public Parser<MysqlxExpr.Expr> getParserForType() {
         return PARSER;
      }

      @Override
      public boolean hasType() {
         return (this.bitField0_ & 1) == 1;
      }

      @Override
      public MysqlxExpr.Expr.Type getType() {
         return this.type_;
      }

      @Override
      public boolean hasIdentifier() {
         return (this.bitField0_ & 2) == 2;
      }

      @Override
      public MysqlxExpr.ColumnIdentifier getIdentifier() {
         return this.identifier_;
      }

      @Override
      public MysqlxExpr.ColumnIdentifierOrBuilder getIdentifierOrBuilder() {
         return this.identifier_;
      }

      @Override
      public boolean hasVariable() {
         return (this.bitField0_ & 4) == 4;
      }

      @Override
      public String getVariable() {
         java.lang.Object ref = this.variable_;
         if (ref instanceof String) {
            return (String)ref;
         } else {
            ByteString bs = (ByteString)ref;
            String s = bs.toStringUtf8();
            if (bs.isValidUtf8()) {
               this.variable_ = s;
            }

            return s;
         }
      }

      @Override
      public ByteString getVariableBytes() {
         java.lang.Object ref = this.variable_;
         if (ref instanceof String) {
            ByteString b = ByteString.copyFromUtf8((String)ref);
            this.variable_ = b;
            return b;
         } else {
            return (ByteString)ref;
         }
      }

      @Override
      public boolean hasLiteral() {
         return (this.bitField0_ & 8) == 8;
      }

      @Override
      public MysqlxDatatypes.Scalar getLiteral() {
         return this.literal_;
      }

      @Override
      public MysqlxDatatypes.ScalarOrBuilder getLiteralOrBuilder() {
         return this.literal_;
      }

      @Override
      public boolean hasFunctionCall() {
         return (this.bitField0_ & 16) == 16;
      }

      @Override
      public MysqlxExpr.FunctionCall getFunctionCall() {
         return this.functionCall_;
      }

      @Override
      public MysqlxExpr.FunctionCallOrBuilder getFunctionCallOrBuilder() {
         return this.functionCall_;
      }

      @Override
      public boolean hasOperator() {
         return (this.bitField0_ & 32) == 32;
      }

      @Override
      public MysqlxExpr.Operator getOperator() {
         return this.operator_;
      }

      @Override
      public MysqlxExpr.OperatorOrBuilder getOperatorOrBuilder() {
         return this.operator_;
      }

      @Override
      public boolean hasPosition() {
         return (this.bitField0_ & 64) == 64;
      }

      @Override
      public int getPosition() {
         return this.position_;
      }

      @Override
      public boolean hasObject() {
         return (this.bitField0_ & 128) == 128;
      }

      @Override
      public MysqlxExpr.Object getObject() {
         return this.object_;
      }

      @Override
      public MysqlxExpr.ObjectOrBuilder getObjectOrBuilder() {
         return this.object_;
      }

      @Override
      public boolean hasArray() {
         return (this.bitField0_ & 256) == 256;
      }

      @Override
      public MysqlxExpr.Array getArray() {
         return this.array_;
      }

      @Override
      public MysqlxExpr.ArrayOrBuilder getArrayOrBuilder() {
         return this.array_;
      }

      private void initFields() {
         this.type_ = MysqlxExpr.Expr.Type.IDENT;
         this.identifier_ = MysqlxExpr.ColumnIdentifier.getDefaultInstance();
         this.variable_ = "";
         this.literal_ = MysqlxDatatypes.Scalar.getDefaultInstance();
         this.functionCall_ = MysqlxExpr.FunctionCall.getDefaultInstance();
         this.operator_ = MysqlxExpr.Operator.getDefaultInstance();
         this.position_ = 0;
         this.object_ = MysqlxExpr.Object.getDefaultInstance();
         this.array_ = MysqlxExpr.Array.getDefaultInstance();
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
         } else if (this.hasIdentifier() && !this.getIdentifier().isInitialized()) {
            this.memoizedIsInitialized = 0;
            return false;
         } else if (this.hasLiteral() && !this.getLiteral().isInitialized()) {
            this.memoizedIsInitialized = 0;
            return false;
         } else if (this.hasFunctionCall() && !this.getFunctionCall().isInitialized()) {
            this.memoizedIsInitialized = 0;
            return false;
         } else if (this.hasOperator() && !this.getOperator().isInitialized()) {
            this.memoizedIsInitialized = 0;
            return false;
         } else if (this.hasObject() && !this.getObject().isInitialized()) {
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
            output.writeMessage(2, this.identifier_);
         }

         if ((this.bitField0_ & 4) == 4) {
            output.writeBytes(3, this.getVariableBytes());
         }

         if ((this.bitField0_ & 8) == 8) {
            output.writeMessage(4, this.literal_);
         }

         if ((this.bitField0_ & 16) == 16) {
            output.writeMessage(5, this.functionCall_);
         }

         if ((this.bitField0_ & 32) == 32) {
            output.writeMessage(6, this.operator_);
         }

         if ((this.bitField0_ & 64) == 64) {
            output.writeUInt32(7, this.position_);
         }

         if ((this.bitField0_ & 128) == 128) {
            output.writeMessage(8, this.object_);
         }

         if ((this.bitField0_ & 256) == 256) {
            output.writeMessage(9, this.array_);
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
               size += CodedOutputStream.computeMessageSize(2, this.identifier_);
            }

            if ((this.bitField0_ & 4) == 4) {
               size += CodedOutputStream.computeBytesSize(3, this.getVariableBytes());
            }

            if ((this.bitField0_ & 8) == 8) {
               size += CodedOutputStream.computeMessageSize(4, this.literal_);
            }

            if ((this.bitField0_ & 16) == 16) {
               size += CodedOutputStream.computeMessageSize(5, this.functionCall_);
            }

            if ((this.bitField0_ & 32) == 32) {
               size += CodedOutputStream.computeMessageSize(6, this.operator_);
            }

            if ((this.bitField0_ & 64) == 64) {
               size += CodedOutputStream.computeUInt32Size(7, this.position_);
            }

            if ((this.bitField0_ & 128) == 128) {
               size += CodedOutputStream.computeMessageSize(8, this.object_);
            }

            if ((this.bitField0_ & 256) == 256) {
               size += CodedOutputStream.computeMessageSize(9, this.array_);
            }

            size += this.getUnknownFields().getSerializedSize();
            this.memoizedSerializedSize = size;
            return size;
         }
      }

      protected java.lang.Object writeReplace() throws ObjectStreamException {
         return super.writeReplace();
      }

      public static MysqlxExpr.Expr parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxExpr.Expr)PARSER.parseFrom(data);
      }

      public static MysqlxExpr.Expr parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxExpr.Expr)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxExpr.Expr parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxExpr.Expr)PARSER.parseFrom(data);
      }

      public static MysqlxExpr.Expr parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxExpr.Expr)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxExpr.Expr parseFrom(InputStream input) throws IOException {
         return (MysqlxExpr.Expr)PARSER.parseFrom(input);
      }

      public static MysqlxExpr.Expr parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxExpr.Expr)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxExpr.Expr parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxExpr.Expr)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxExpr.Expr parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxExpr.Expr)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxExpr.Expr parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxExpr.Expr)PARSER.parseFrom(input);
      }

      public static MysqlxExpr.Expr parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxExpr.Expr)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxExpr.Expr.Builder newBuilder() {
         return MysqlxExpr.Expr.Builder.create();
      }

      public MysqlxExpr.Expr.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxExpr.Expr.Builder newBuilder(MysqlxExpr.Expr prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxExpr.Expr.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxExpr.Expr.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxExpr.Expr.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<MysqlxExpr.Expr.Builder> implements MysqlxExpr.ExprOrBuilder {
         private int bitField0_;
         private MysqlxExpr.Expr.Type type_ = MysqlxExpr.Expr.Type.IDENT;
         private MysqlxExpr.ColumnIdentifier identifier_ = MysqlxExpr.ColumnIdentifier.getDefaultInstance();
         private SingleFieldBuilder<MysqlxExpr.ColumnIdentifier, MysqlxExpr.ColumnIdentifier.Builder, MysqlxExpr.ColumnIdentifierOrBuilder> identifierBuilder_;
         private java.lang.Object variable_ = "";
         private MysqlxDatatypes.Scalar literal_ = MysqlxDatatypes.Scalar.getDefaultInstance();
         private SingleFieldBuilder<MysqlxDatatypes.Scalar, MysqlxDatatypes.Scalar.Builder, MysqlxDatatypes.ScalarOrBuilder> literalBuilder_;
         private MysqlxExpr.FunctionCall functionCall_ = MysqlxExpr.FunctionCall.getDefaultInstance();
         private SingleFieldBuilder<MysqlxExpr.FunctionCall, MysqlxExpr.FunctionCall.Builder, MysqlxExpr.FunctionCallOrBuilder> functionCallBuilder_;
         private MysqlxExpr.Operator operator_ = MysqlxExpr.Operator.getDefaultInstance();
         private SingleFieldBuilder<MysqlxExpr.Operator, MysqlxExpr.Operator.Builder, MysqlxExpr.OperatorOrBuilder> operatorBuilder_;
         private int position_;
         private MysqlxExpr.Object object_ = MysqlxExpr.Object.getDefaultInstance();
         private SingleFieldBuilder<MysqlxExpr.Object, MysqlxExpr.Object.Builder, MysqlxExpr.ObjectOrBuilder> objectBuilder_;
         private MysqlxExpr.Array array_ = MysqlxExpr.Array.getDefaultInstance();
         private SingleFieldBuilder<MysqlxExpr.Array, MysqlxExpr.Array.Builder, MysqlxExpr.ArrayOrBuilder> arrayBuilder_;

         public static final Descriptor getDescriptor() {
            return MysqlxExpr.internal_static_Mysqlx_Expr_Expr_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxExpr.internal_static_Mysqlx_Expr_Expr_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxExpr.Expr.class, MysqlxExpr.Expr.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxExpr.Expr.alwaysUseFieldBuilders) {
               this.getIdentifierFieldBuilder();
               this.getLiteralFieldBuilder();
               this.getFunctionCallFieldBuilder();
               this.getOperatorFieldBuilder();
               this.getObjectFieldBuilder();
               this.getArrayFieldBuilder();
            }
         }

         private static MysqlxExpr.Expr.Builder create() {
            return new MysqlxExpr.Expr.Builder();
         }

         public MysqlxExpr.Expr.Builder clear() {
            super.clear();
            this.type_ = MysqlxExpr.Expr.Type.IDENT;
            this.bitField0_ &= -2;
            if (this.identifierBuilder_ == null) {
               this.identifier_ = MysqlxExpr.ColumnIdentifier.getDefaultInstance();
            } else {
               this.identifierBuilder_.clear();
            }

            this.bitField0_ &= -3;
            this.variable_ = "";
            this.bitField0_ &= -5;
            if (this.literalBuilder_ == null) {
               this.literal_ = MysqlxDatatypes.Scalar.getDefaultInstance();
            } else {
               this.literalBuilder_.clear();
            }

            this.bitField0_ &= -9;
            if (this.functionCallBuilder_ == null) {
               this.functionCall_ = MysqlxExpr.FunctionCall.getDefaultInstance();
            } else {
               this.functionCallBuilder_.clear();
            }

            this.bitField0_ &= -17;
            if (this.operatorBuilder_ == null) {
               this.operator_ = MysqlxExpr.Operator.getDefaultInstance();
            } else {
               this.operatorBuilder_.clear();
            }

            this.bitField0_ &= -33;
            this.position_ = 0;
            this.bitField0_ &= -65;
            if (this.objectBuilder_ == null) {
               this.object_ = MysqlxExpr.Object.getDefaultInstance();
            } else {
               this.objectBuilder_.clear();
            }

            this.bitField0_ &= -129;
            if (this.arrayBuilder_ == null) {
               this.array_ = MysqlxExpr.Array.getDefaultInstance();
            } else {
               this.arrayBuilder_.clear();
            }

            this.bitField0_ &= -257;
            return this;
         }

         public MysqlxExpr.Expr.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxExpr.internal_static_Mysqlx_Expr_Expr_descriptor;
         }

         public MysqlxExpr.Expr getDefaultInstanceForType() {
            return MysqlxExpr.Expr.getDefaultInstance();
         }

         public MysqlxExpr.Expr build() {
            MysqlxExpr.Expr result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxExpr.Expr buildPartial() {
            MysqlxExpr.Expr result = new MysqlxExpr.Expr(this);
            int from_bitField0_ = this.bitField0_;
            int to_bitField0_ = 0;
            if ((from_bitField0_ & 1) == 1) {
               to_bitField0_ |= 1;
            }

            result.type_ = this.type_;
            if ((from_bitField0_ & 2) == 2) {
               to_bitField0_ |= 2;
            }

            if (this.identifierBuilder_ == null) {
               result.identifier_ = this.identifier_;
            } else {
               result.identifier_ = (MysqlxExpr.ColumnIdentifier)this.identifierBuilder_.build();
            }

            if ((from_bitField0_ & 4) == 4) {
               to_bitField0_ |= 4;
            }

            result.variable_ = this.variable_;
            if ((from_bitField0_ & 8) == 8) {
               to_bitField0_ |= 8;
            }

            if (this.literalBuilder_ == null) {
               result.literal_ = this.literal_;
            } else {
               result.literal_ = (MysqlxDatatypes.Scalar)this.literalBuilder_.build();
            }

            if ((from_bitField0_ & 16) == 16) {
               to_bitField0_ |= 16;
            }

            if (this.functionCallBuilder_ == null) {
               result.functionCall_ = this.functionCall_;
            } else {
               result.functionCall_ = (MysqlxExpr.FunctionCall)this.functionCallBuilder_.build();
            }

            if ((from_bitField0_ & 32) == 32) {
               to_bitField0_ |= 32;
            }

            if (this.operatorBuilder_ == null) {
               result.operator_ = this.operator_;
            } else {
               result.operator_ = (MysqlxExpr.Operator)this.operatorBuilder_.build();
            }

            if ((from_bitField0_ & 64) == 64) {
               to_bitField0_ |= 64;
            }

            result.position_ = this.position_;
            if ((from_bitField0_ & 128) == 128) {
               to_bitField0_ |= 128;
            }

            if (this.objectBuilder_ == null) {
               result.object_ = this.object_;
            } else {
               result.object_ = (MysqlxExpr.Object)this.objectBuilder_.build();
            }

            if ((from_bitField0_ & 256) == 256) {
               to_bitField0_ |= 256;
            }

            if (this.arrayBuilder_ == null) {
               result.array_ = this.array_;
            } else {
               result.array_ = (MysqlxExpr.Array)this.arrayBuilder_.build();
            }

            result.bitField0_ = to_bitField0_;
            this.onBuilt();
            return result;
         }

         public MysqlxExpr.Expr.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxExpr.Expr) {
               return this.mergeFrom((MysqlxExpr.Expr)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxExpr.Expr.Builder mergeFrom(MysqlxExpr.Expr other) {
            if (other == MysqlxExpr.Expr.getDefaultInstance()) {
               return this;
            } else {
               if (other.hasType()) {
                  this.setType(other.getType());
               }

               if (other.hasIdentifier()) {
                  this.mergeIdentifier(other.getIdentifier());
               }

               if (other.hasVariable()) {
                  this.bitField0_ |= 4;
                  this.variable_ = other.variable_;
                  this.onChanged();
               }

               if (other.hasLiteral()) {
                  this.mergeLiteral(other.getLiteral());
               }

               if (other.hasFunctionCall()) {
                  this.mergeFunctionCall(other.getFunctionCall());
               }

               if (other.hasOperator()) {
                  this.mergeOperator(other.getOperator());
               }

               if (other.hasPosition()) {
                  this.setPosition(other.getPosition());
               }

               if (other.hasObject()) {
                  this.mergeObject(other.getObject());
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
            } else if (this.hasIdentifier() && !this.getIdentifier().isInitialized()) {
               return false;
            } else if (this.hasLiteral() && !this.getLiteral().isInitialized()) {
               return false;
            } else if (this.hasFunctionCall() && !this.getFunctionCall().isInitialized()) {
               return false;
            } else if (this.hasOperator() && !this.getOperator().isInitialized()) {
               return false;
            } else if (this.hasObject() && !this.getObject().isInitialized()) {
               return false;
            } else {
               return !this.hasArray() || this.getArray().isInitialized();
            }
         }

         public MysqlxExpr.Expr.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxExpr.Expr parsedMessage = null;

            try {
               parsedMessage = (MysqlxExpr.Expr)MysqlxExpr.Expr.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxExpr.Expr)var8.getUnfinishedMessage();
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
         public MysqlxExpr.Expr.Type getType() {
            return this.type_;
         }

         public MysqlxExpr.Expr.Builder setType(MysqlxExpr.Expr.Type value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 1;
               this.type_ = value;
               this.onChanged();
               return this;
            }
         }

         public MysqlxExpr.Expr.Builder clearType() {
            this.bitField0_ &= -2;
            this.type_ = MysqlxExpr.Expr.Type.IDENT;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasIdentifier() {
            return (this.bitField0_ & 2) == 2;
         }

         @Override
         public MysqlxExpr.ColumnIdentifier getIdentifier() {
            return this.identifierBuilder_ == null ? this.identifier_ : (MysqlxExpr.ColumnIdentifier)this.identifierBuilder_.getMessage();
         }

         public MysqlxExpr.Expr.Builder setIdentifier(MysqlxExpr.ColumnIdentifier value) {
            if (this.identifierBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.identifier_ = value;
               this.onChanged();
            } else {
               this.identifierBuilder_.setMessage(value);
            }

            this.bitField0_ |= 2;
            return this;
         }

         public MysqlxExpr.Expr.Builder setIdentifier(MysqlxExpr.ColumnIdentifier.Builder builderForValue) {
            if (this.identifierBuilder_ == null) {
               this.identifier_ = builderForValue.build();
               this.onChanged();
            } else {
               this.identifierBuilder_.setMessage(builderForValue.build());
            }

            this.bitField0_ |= 2;
            return this;
         }

         public MysqlxExpr.Expr.Builder mergeIdentifier(MysqlxExpr.ColumnIdentifier value) {
            if (this.identifierBuilder_ == null) {
               if ((this.bitField0_ & 2) == 2 && this.identifier_ != MysqlxExpr.ColumnIdentifier.getDefaultInstance()) {
                  this.identifier_ = MysqlxExpr.ColumnIdentifier.newBuilder(this.identifier_).mergeFrom(value).buildPartial();
               } else {
                  this.identifier_ = value;
               }

               this.onChanged();
            } else {
               this.identifierBuilder_.mergeFrom(value);
            }

            this.bitField0_ |= 2;
            return this;
         }

         public MysqlxExpr.Expr.Builder clearIdentifier() {
            if (this.identifierBuilder_ == null) {
               this.identifier_ = MysqlxExpr.ColumnIdentifier.getDefaultInstance();
               this.onChanged();
            } else {
               this.identifierBuilder_.clear();
            }

            this.bitField0_ &= -3;
            return this;
         }

         public MysqlxExpr.ColumnIdentifier.Builder getIdentifierBuilder() {
            this.bitField0_ |= 2;
            this.onChanged();
            return (MysqlxExpr.ColumnIdentifier.Builder)this.getIdentifierFieldBuilder().getBuilder();
         }

         @Override
         public MysqlxExpr.ColumnIdentifierOrBuilder getIdentifierOrBuilder() {
            return (MysqlxExpr.ColumnIdentifierOrBuilder)(this.identifierBuilder_ != null
               ? (MysqlxExpr.ColumnIdentifierOrBuilder)this.identifierBuilder_.getMessageOrBuilder()
               : this.identifier_);
         }

         private SingleFieldBuilder<MysqlxExpr.ColumnIdentifier, MysqlxExpr.ColumnIdentifier.Builder, MysqlxExpr.ColumnIdentifierOrBuilder> getIdentifierFieldBuilder() {
            if (this.identifierBuilder_ == null) {
               this.identifierBuilder_ = new SingleFieldBuilder(this.getIdentifier(), this.getParentForChildren(), this.isClean());
               this.identifier_ = null;
            }

            return this.identifierBuilder_;
         }

         @Override
         public boolean hasVariable() {
            return (this.bitField0_ & 4) == 4;
         }

         @Override
         public String getVariable() {
            java.lang.Object ref = this.variable_;
            if (!(ref instanceof String)) {
               ByteString bs = (ByteString)ref;
               String s = bs.toStringUtf8();
               if (bs.isValidUtf8()) {
                  this.variable_ = s;
               }

               return s;
            } else {
               return (String)ref;
            }
         }

         @Override
         public ByteString getVariableBytes() {
            java.lang.Object ref = this.variable_;
            if (ref instanceof String) {
               ByteString b = ByteString.copyFromUtf8((String)ref);
               this.variable_ = b;
               return b;
            } else {
               return (ByteString)ref;
            }
         }

         public MysqlxExpr.Expr.Builder setVariable(String value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 4;
               this.variable_ = value;
               this.onChanged();
               return this;
            }
         }

         public MysqlxExpr.Expr.Builder clearVariable() {
            this.bitField0_ &= -5;
            this.variable_ = MysqlxExpr.Expr.getDefaultInstance().getVariable();
            this.onChanged();
            return this;
         }

         public MysqlxExpr.Expr.Builder setVariableBytes(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 4;
               this.variable_ = value;
               this.onChanged();
               return this;
            }
         }

         @Override
         public boolean hasLiteral() {
            return (this.bitField0_ & 8) == 8;
         }

         @Override
         public MysqlxDatatypes.Scalar getLiteral() {
            return this.literalBuilder_ == null ? this.literal_ : (MysqlxDatatypes.Scalar)this.literalBuilder_.getMessage();
         }

         public MysqlxExpr.Expr.Builder setLiteral(MysqlxDatatypes.Scalar value) {
            if (this.literalBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.literal_ = value;
               this.onChanged();
            } else {
               this.literalBuilder_.setMessage(value);
            }

            this.bitField0_ |= 8;
            return this;
         }

         public MysqlxExpr.Expr.Builder setLiteral(MysqlxDatatypes.Scalar.Builder builderForValue) {
            if (this.literalBuilder_ == null) {
               this.literal_ = builderForValue.build();
               this.onChanged();
            } else {
               this.literalBuilder_.setMessage(builderForValue.build());
            }

            this.bitField0_ |= 8;
            return this;
         }

         public MysqlxExpr.Expr.Builder mergeLiteral(MysqlxDatatypes.Scalar value) {
            if (this.literalBuilder_ == null) {
               if ((this.bitField0_ & 8) == 8 && this.literal_ != MysqlxDatatypes.Scalar.getDefaultInstance()) {
                  this.literal_ = MysqlxDatatypes.Scalar.newBuilder(this.literal_).mergeFrom(value).buildPartial();
               } else {
                  this.literal_ = value;
               }

               this.onChanged();
            } else {
               this.literalBuilder_.mergeFrom(value);
            }

            this.bitField0_ |= 8;
            return this;
         }

         public MysqlxExpr.Expr.Builder clearLiteral() {
            if (this.literalBuilder_ == null) {
               this.literal_ = MysqlxDatatypes.Scalar.getDefaultInstance();
               this.onChanged();
            } else {
               this.literalBuilder_.clear();
            }

            this.bitField0_ &= -9;
            return this;
         }

         public MysqlxDatatypes.Scalar.Builder getLiteralBuilder() {
            this.bitField0_ |= 8;
            this.onChanged();
            return (MysqlxDatatypes.Scalar.Builder)this.getLiteralFieldBuilder().getBuilder();
         }

         @Override
         public MysqlxDatatypes.ScalarOrBuilder getLiteralOrBuilder() {
            return (MysqlxDatatypes.ScalarOrBuilder)(this.literalBuilder_ != null
               ? (MysqlxDatatypes.ScalarOrBuilder)this.literalBuilder_.getMessageOrBuilder()
               : this.literal_);
         }

         private SingleFieldBuilder<MysqlxDatatypes.Scalar, MysqlxDatatypes.Scalar.Builder, MysqlxDatatypes.ScalarOrBuilder> getLiteralFieldBuilder() {
            if (this.literalBuilder_ == null) {
               this.literalBuilder_ = new SingleFieldBuilder(this.getLiteral(), this.getParentForChildren(), this.isClean());
               this.literal_ = null;
            }

            return this.literalBuilder_;
         }

         @Override
         public boolean hasFunctionCall() {
            return (this.bitField0_ & 16) == 16;
         }

         @Override
         public MysqlxExpr.FunctionCall getFunctionCall() {
            return this.functionCallBuilder_ == null ? this.functionCall_ : (MysqlxExpr.FunctionCall)this.functionCallBuilder_.getMessage();
         }

         public MysqlxExpr.Expr.Builder setFunctionCall(MysqlxExpr.FunctionCall value) {
            if (this.functionCallBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.functionCall_ = value;
               this.onChanged();
            } else {
               this.functionCallBuilder_.setMessage(value);
            }

            this.bitField0_ |= 16;
            return this;
         }

         public MysqlxExpr.Expr.Builder setFunctionCall(MysqlxExpr.FunctionCall.Builder builderForValue) {
            if (this.functionCallBuilder_ == null) {
               this.functionCall_ = builderForValue.build();
               this.onChanged();
            } else {
               this.functionCallBuilder_.setMessage(builderForValue.build());
            }

            this.bitField0_ |= 16;
            return this;
         }

         public MysqlxExpr.Expr.Builder mergeFunctionCall(MysqlxExpr.FunctionCall value) {
            if (this.functionCallBuilder_ == null) {
               if ((this.bitField0_ & 16) == 16 && this.functionCall_ != MysqlxExpr.FunctionCall.getDefaultInstance()) {
                  this.functionCall_ = MysqlxExpr.FunctionCall.newBuilder(this.functionCall_).mergeFrom(value).buildPartial();
               } else {
                  this.functionCall_ = value;
               }

               this.onChanged();
            } else {
               this.functionCallBuilder_.mergeFrom(value);
            }

            this.bitField0_ |= 16;
            return this;
         }

         public MysqlxExpr.Expr.Builder clearFunctionCall() {
            if (this.functionCallBuilder_ == null) {
               this.functionCall_ = MysqlxExpr.FunctionCall.getDefaultInstance();
               this.onChanged();
            } else {
               this.functionCallBuilder_.clear();
            }

            this.bitField0_ &= -17;
            return this;
         }

         public MysqlxExpr.FunctionCall.Builder getFunctionCallBuilder() {
            this.bitField0_ |= 16;
            this.onChanged();
            return (MysqlxExpr.FunctionCall.Builder)this.getFunctionCallFieldBuilder().getBuilder();
         }

         @Override
         public MysqlxExpr.FunctionCallOrBuilder getFunctionCallOrBuilder() {
            return (MysqlxExpr.FunctionCallOrBuilder)(this.functionCallBuilder_ != null
               ? (MysqlxExpr.FunctionCallOrBuilder)this.functionCallBuilder_.getMessageOrBuilder()
               : this.functionCall_);
         }

         private SingleFieldBuilder<MysqlxExpr.FunctionCall, MysqlxExpr.FunctionCall.Builder, MysqlxExpr.FunctionCallOrBuilder> getFunctionCallFieldBuilder() {
            if (this.functionCallBuilder_ == null) {
               this.functionCallBuilder_ = new SingleFieldBuilder(this.getFunctionCall(), this.getParentForChildren(), this.isClean());
               this.functionCall_ = null;
            }

            return this.functionCallBuilder_;
         }

         @Override
         public boolean hasOperator() {
            return (this.bitField0_ & 32) == 32;
         }

         @Override
         public MysqlxExpr.Operator getOperator() {
            return this.operatorBuilder_ == null ? this.operator_ : (MysqlxExpr.Operator)this.operatorBuilder_.getMessage();
         }

         public MysqlxExpr.Expr.Builder setOperator(MysqlxExpr.Operator value) {
            if (this.operatorBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.operator_ = value;
               this.onChanged();
            } else {
               this.operatorBuilder_.setMessage(value);
            }

            this.bitField0_ |= 32;
            return this;
         }

         public MysqlxExpr.Expr.Builder setOperator(MysqlxExpr.Operator.Builder builderForValue) {
            if (this.operatorBuilder_ == null) {
               this.operator_ = builderForValue.build();
               this.onChanged();
            } else {
               this.operatorBuilder_.setMessage(builderForValue.build());
            }

            this.bitField0_ |= 32;
            return this;
         }

         public MysqlxExpr.Expr.Builder mergeOperator(MysqlxExpr.Operator value) {
            if (this.operatorBuilder_ == null) {
               if ((this.bitField0_ & 32) == 32 && this.operator_ != MysqlxExpr.Operator.getDefaultInstance()) {
                  this.operator_ = MysqlxExpr.Operator.newBuilder(this.operator_).mergeFrom(value).buildPartial();
               } else {
                  this.operator_ = value;
               }

               this.onChanged();
            } else {
               this.operatorBuilder_.mergeFrom(value);
            }

            this.bitField0_ |= 32;
            return this;
         }

         public MysqlxExpr.Expr.Builder clearOperator() {
            if (this.operatorBuilder_ == null) {
               this.operator_ = MysqlxExpr.Operator.getDefaultInstance();
               this.onChanged();
            } else {
               this.operatorBuilder_.clear();
            }

            this.bitField0_ &= -33;
            return this;
         }

         public MysqlxExpr.Operator.Builder getOperatorBuilder() {
            this.bitField0_ |= 32;
            this.onChanged();
            return (MysqlxExpr.Operator.Builder)this.getOperatorFieldBuilder().getBuilder();
         }

         @Override
         public MysqlxExpr.OperatorOrBuilder getOperatorOrBuilder() {
            return (MysqlxExpr.OperatorOrBuilder)(this.operatorBuilder_ != null
               ? (MysqlxExpr.OperatorOrBuilder)this.operatorBuilder_.getMessageOrBuilder()
               : this.operator_);
         }

         private SingleFieldBuilder<MysqlxExpr.Operator, MysqlxExpr.Operator.Builder, MysqlxExpr.OperatorOrBuilder> getOperatorFieldBuilder() {
            if (this.operatorBuilder_ == null) {
               this.operatorBuilder_ = new SingleFieldBuilder(this.getOperator(), this.getParentForChildren(), this.isClean());
               this.operator_ = null;
            }

            return this.operatorBuilder_;
         }

         @Override
         public boolean hasPosition() {
            return (this.bitField0_ & 64) == 64;
         }

         @Override
         public int getPosition() {
            return this.position_;
         }

         public MysqlxExpr.Expr.Builder setPosition(int value) {
            this.bitField0_ |= 64;
            this.position_ = value;
            this.onChanged();
            return this;
         }

         public MysqlxExpr.Expr.Builder clearPosition() {
            this.bitField0_ &= -65;
            this.position_ = 0;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasObject() {
            return (this.bitField0_ & 128) == 128;
         }

         @Override
         public MysqlxExpr.Object getObject() {
            return this.objectBuilder_ == null ? this.object_ : (MysqlxExpr.Object)this.objectBuilder_.getMessage();
         }

         public MysqlxExpr.Expr.Builder setObject(MysqlxExpr.Object value) {
            if (this.objectBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.object_ = value;
               this.onChanged();
            } else {
               this.objectBuilder_.setMessage(value);
            }

            this.bitField0_ |= 128;
            return this;
         }

         public MysqlxExpr.Expr.Builder setObject(MysqlxExpr.Object.Builder builderForValue) {
            if (this.objectBuilder_ == null) {
               this.object_ = builderForValue.build();
               this.onChanged();
            } else {
               this.objectBuilder_.setMessage(builderForValue.build());
            }

            this.bitField0_ |= 128;
            return this;
         }

         public MysqlxExpr.Expr.Builder mergeObject(MysqlxExpr.Object value) {
            if (this.objectBuilder_ == null) {
               if ((this.bitField0_ & 128) == 128 && this.object_ != MysqlxExpr.Object.getDefaultInstance()) {
                  this.object_ = MysqlxExpr.Object.newBuilder(this.object_).mergeFrom(value).buildPartial();
               } else {
                  this.object_ = value;
               }

               this.onChanged();
            } else {
               this.objectBuilder_.mergeFrom(value);
            }

            this.bitField0_ |= 128;
            return this;
         }

         public MysqlxExpr.Expr.Builder clearObject() {
            if (this.objectBuilder_ == null) {
               this.object_ = MysqlxExpr.Object.getDefaultInstance();
               this.onChanged();
            } else {
               this.objectBuilder_.clear();
            }

            this.bitField0_ &= -129;
            return this;
         }

         public MysqlxExpr.Object.Builder getObjectBuilder() {
            this.bitField0_ |= 128;
            this.onChanged();
            return (MysqlxExpr.Object.Builder)this.getObjectFieldBuilder().getBuilder();
         }

         @Override
         public MysqlxExpr.ObjectOrBuilder getObjectOrBuilder() {
            return (MysqlxExpr.ObjectOrBuilder)(this.objectBuilder_ != null
               ? (MysqlxExpr.ObjectOrBuilder)this.objectBuilder_.getMessageOrBuilder()
               : this.object_);
         }

         private SingleFieldBuilder<MysqlxExpr.Object, MysqlxExpr.Object.Builder, MysqlxExpr.ObjectOrBuilder> getObjectFieldBuilder() {
            if (this.objectBuilder_ == null) {
               this.objectBuilder_ = new SingleFieldBuilder(this.getObject(), this.getParentForChildren(), this.isClean());
               this.object_ = null;
            }

            return this.objectBuilder_;
         }

         @Override
         public boolean hasArray() {
            return (this.bitField0_ & 256) == 256;
         }

         @Override
         public MysqlxExpr.Array getArray() {
            return this.arrayBuilder_ == null ? this.array_ : (MysqlxExpr.Array)this.arrayBuilder_.getMessage();
         }

         public MysqlxExpr.Expr.Builder setArray(MysqlxExpr.Array value) {
            if (this.arrayBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.array_ = value;
               this.onChanged();
            } else {
               this.arrayBuilder_.setMessage(value);
            }

            this.bitField0_ |= 256;
            return this;
         }

         public MysqlxExpr.Expr.Builder setArray(MysqlxExpr.Array.Builder builderForValue) {
            if (this.arrayBuilder_ == null) {
               this.array_ = builderForValue.build();
               this.onChanged();
            } else {
               this.arrayBuilder_.setMessage(builderForValue.build());
            }

            this.bitField0_ |= 256;
            return this;
         }

         public MysqlxExpr.Expr.Builder mergeArray(MysqlxExpr.Array value) {
            if (this.arrayBuilder_ == null) {
               if ((this.bitField0_ & 256) == 256 && this.array_ != MysqlxExpr.Array.getDefaultInstance()) {
                  this.array_ = MysqlxExpr.Array.newBuilder(this.array_).mergeFrom(value).buildPartial();
               } else {
                  this.array_ = value;
               }

               this.onChanged();
            } else {
               this.arrayBuilder_.mergeFrom(value);
            }

            this.bitField0_ |= 256;
            return this;
         }

         public MysqlxExpr.Expr.Builder clearArray() {
            if (this.arrayBuilder_ == null) {
               this.array_ = MysqlxExpr.Array.getDefaultInstance();
               this.onChanged();
            } else {
               this.arrayBuilder_.clear();
            }

            this.bitField0_ &= -257;
            return this;
         }

         public MysqlxExpr.Array.Builder getArrayBuilder() {
            this.bitField0_ |= 256;
            this.onChanged();
            return (MysqlxExpr.Array.Builder)this.getArrayFieldBuilder().getBuilder();
         }

         @Override
         public MysqlxExpr.ArrayOrBuilder getArrayOrBuilder() {
            return (MysqlxExpr.ArrayOrBuilder)(this.arrayBuilder_ != null ? (MysqlxExpr.ArrayOrBuilder)this.arrayBuilder_.getMessageOrBuilder() : this.array_);
         }

         private SingleFieldBuilder<MysqlxExpr.Array, MysqlxExpr.Array.Builder, MysqlxExpr.ArrayOrBuilder> getArrayFieldBuilder() {
            if (this.arrayBuilder_ == null) {
               this.arrayBuilder_ = new SingleFieldBuilder(this.getArray(), this.getParentForChildren(), this.isClean());
               this.array_ = null;
            }

            return this.arrayBuilder_;
         }
      }

      public static enum Type implements ProtocolMessageEnum {
         IDENT(0, 1),
         LITERAL(1, 2),
         VARIABLE(2, 3),
         FUNC_CALL(3, 4),
         OPERATOR(4, 5),
         PLACEHOLDER(5, 6),
         OBJECT(6, 7),
         ARRAY(7, 8);

         public static final int IDENT_VALUE = 1;
         public static final int LITERAL_VALUE = 2;
         public static final int VARIABLE_VALUE = 3;
         public static final int FUNC_CALL_VALUE = 4;
         public static final int OPERATOR_VALUE = 5;
         public static final int PLACEHOLDER_VALUE = 6;
         public static final int OBJECT_VALUE = 7;
         public static final int ARRAY_VALUE = 8;
         private static EnumLiteMap<MysqlxExpr.Expr.Type> internalValueMap = new EnumLiteMap<MysqlxExpr.Expr.Type>() {
            public MysqlxExpr.Expr.Type findValueByNumber(int number) {
               return MysqlxExpr.Expr.Type.valueOf(number);
            }
         };
         private static final MysqlxExpr.Expr.Type[] VALUES = values();
         private final int index;
         private final int value;

         public final int getNumber() {
            return this.value;
         }

         public static MysqlxExpr.Expr.Type valueOf(int value) {
            switch(value) {
               case 1:
                  return IDENT;
               case 2:
                  return LITERAL;
               case 3:
                  return VARIABLE;
               case 4:
                  return FUNC_CALL;
               case 5:
                  return OPERATOR;
               case 6:
                  return PLACEHOLDER;
               case 7:
                  return OBJECT;
               case 8:
                  return ARRAY;
               default:
                  return null;
            }
         }

         public static EnumLiteMap<MysqlxExpr.Expr.Type> internalGetValueMap() {
            return internalValueMap;
         }

         public final EnumValueDescriptor getValueDescriptor() {
            return (EnumValueDescriptor)getDescriptor().getValues().get(this.index);
         }

         public final EnumDescriptor getDescriptorForType() {
            return getDescriptor();
         }

         public static final EnumDescriptor getDescriptor() {
            return (EnumDescriptor)MysqlxExpr.Expr.getDescriptor().getEnumTypes().get(0);
         }

         public static MysqlxExpr.Expr.Type valueOf(EnumValueDescriptor desc) {
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

   public interface ExprOrBuilder extends MessageOrBuilder {
      boolean hasType();

      MysqlxExpr.Expr.Type getType();

      boolean hasIdentifier();

      MysqlxExpr.ColumnIdentifier getIdentifier();

      MysqlxExpr.ColumnIdentifierOrBuilder getIdentifierOrBuilder();

      boolean hasVariable();

      String getVariable();

      ByteString getVariableBytes();

      boolean hasLiteral();

      MysqlxDatatypes.Scalar getLiteral();

      MysqlxDatatypes.ScalarOrBuilder getLiteralOrBuilder();

      boolean hasFunctionCall();

      MysqlxExpr.FunctionCall getFunctionCall();

      MysqlxExpr.FunctionCallOrBuilder getFunctionCallOrBuilder();

      boolean hasOperator();

      MysqlxExpr.Operator getOperator();

      MysqlxExpr.OperatorOrBuilder getOperatorOrBuilder();

      boolean hasPosition();

      int getPosition();

      boolean hasObject();

      MysqlxExpr.Object getObject();

      MysqlxExpr.ObjectOrBuilder getObjectOrBuilder();

      boolean hasArray();

      MysqlxExpr.Array getArray();

      MysqlxExpr.ArrayOrBuilder getArrayOrBuilder();
   }

   public static final class FunctionCall extends GeneratedMessage implements MysqlxExpr.FunctionCallOrBuilder {
      private static final MysqlxExpr.FunctionCall defaultInstance = new MysqlxExpr.FunctionCall(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxExpr.FunctionCall> PARSER = new AbstractParser<MysqlxExpr.FunctionCall>() {
         public MysqlxExpr.FunctionCall parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxExpr.FunctionCall(input, extensionRegistry);
         }
      };
      private int bitField0_;
      public static final int NAME_FIELD_NUMBER = 1;
      private MysqlxExpr.Identifier name_;
      public static final int PARAM_FIELD_NUMBER = 2;
      private List<MysqlxExpr.Expr> param_;
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private FunctionCall(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private FunctionCall(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static MysqlxExpr.FunctionCall getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxExpr.FunctionCall getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private FunctionCall(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                     MysqlxExpr.Identifier.Builder subBuilder = null;
                     if ((this.bitField0_ & 1) == 1) {
                        subBuilder = this.name_.toBuilder();
                     }

                     this.name_ = (MysqlxExpr.Identifier)input.readMessage(MysqlxExpr.Identifier.PARSER, extensionRegistry);
                     if (subBuilder != null) {
                        subBuilder.mergeFrom(this.name_);
                        this.name_ = subBuilder.buildPartial();
                     }

                     this.bitField0_ |= 1;
                     break;
                  case 18:
                     if ((mutable_bitField0_ & 2) != 2) {
                        this.param_ = new ArrayList<>();
                        mutable_bitField0_ |= 2;
                     }

                     this.param_.add(input.readMessage(MysqlxExpr.Expr.PARSER, extensionRegistry));
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
            if ((mutable_bitField0_ & 2) == 2) {
               this.param_ = Collections.unmodifiableList(this.param_);
            }

            this.unknownFields = unknownFields.build();
            this.makeExtensionsImmutable();
         }
      }

      public static final Descriptor getDescriptor() {
         return MysqlxExpr.internal_static_Mysqlx_Expr_FunctionCall_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxExpr.internal_static_Mysqlx_Expr_FunctionCall_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxExpr.FunctionCall.class, MysqlxExpr.FunctionCall.Builder.class);
      }

      public Parser<MysqlxExpr.FunctionCall> getParserForType() {
         return PARSER;
      }

      @Override
      public boolean hasName() {
         return (this.bitField0_ & 1) == 1;
      }

      @Override
      public MysqlxExpr.Identifier getName() {
         return this.name_;
      }

      @Override
      public MysqlxExpr.IdentifierOrBuilder getNameOrBuilder() {
         return this.name_;
      }

      @Override
      public List<MysqlxExpr.Expr> getParamList() {
         return this.param_;
      }

      @Override
      public List<? extends MysqlxExpr.ExprOrBuilder> getParamOrBuilderList() {
         return this.param_;
      }

      @Override
      public int getParamCount() {
         return this.param_.size();
      }

      @Override
      public MysqlxExpr.Expr getParam(int index) {
         return this.param_.get(index);
      }

      @Override
      public MysqlxExpr.ExprOrBuilder getParamOrBuilder(int index) {
         return this.param_.get(index);
      }

      private void initFields() {
         this.name_ = MysqlxExpr.Identifier.getDefaultInstance();
         this.param_ = Collections.emptyList();
      }

      public final boolean isInitialized() {
         byte isInitialized = this.memoizedIsInitialized;
         if (isInitialized == 1) {
            return true;
         } else if (isInitialized == 0) {
            return false;
         } else if (!this.hasName()) {
            this.memoizedIsInitialized = 0;
            return false;
         } else if (!this.getName().isInitialized()) {
            this.memoizedIsInitialized = 0;
            return false;
         } else {
            for(int i = 0; i < this.getParamCount(); ++i) {
               if (!this.getParam(i).isInitialized()) {
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
            output.writeMessage(1, this.name_);
         }

         for(int i = 0; i < this.param_.size(); ++i) {
            output.writeMessage(2, (MessageLite)this.param_.get(i));
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
               size += CodedOutputStream.computeMessageSize(1, this.name_);
            }

            for(int i = 0; i < this.param_.size(); ++i) {
               size += CodedOutputStream.computeMessageSize(2, (MessageLite)this.param_.get(i));
            }

            size += this.getUnknownFields().getSerializedSize();
            this.memoizedSerializedSize = size;
            return size;
         }
      }

      protected java.lang.Object writeReplace() throws ObjectStreamException {
         return super.writeReplace();
      }

      public static MysqlxExpr.FunctionCall parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxExpr.FunctionCall)PARSER.parseFrom(data);
      }

      public static MysqlxExpr.FunctionCall parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxExpr.FunctionCall)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxExpr.FunctionCall parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxExpr.FunctionCall)PARSER.parseFrom(data);
      }

      public static MysqlxExpr.FunctionCall parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxExpr.FunctionCall)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxExpr.FunctionCall parseFrom(InputStream input) throws IOException {
         return (MysqlxExpr.FunctionCall)PARSER.parseFrom(input);
      }

      public static MysqlxExpr.FunctionCall parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxExpr.FunctionCall)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxExpr.FunctionCall parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxExpr.FunctionCall)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxExpr.FunctionCall parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxExpr.FunctionCall)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxExpr.FunctionCall parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxExpr.FunctionCall)PARSER.parseFrom(input);
      }

      public static MysqlxExpr.FunctionCall parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxExpr.FunctionCall)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxExpr.FunctionCall.Builder newBuilder() {
         return MysqlxExpr.FunctionCall.Builder.create();
      }

      public MysqlxExpr.FunctionCall.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxExpr.FunctionCall.Builder newBuilder(MysqlxExpr.FunctionCall prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxExpr.FunctionCall.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxExpr.FunctionCall.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxExpr.FunctionCall.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxExpr.FunctionCall.Builder>
         implements MysqlxExpr.FunctionCallOrBuilder {
         private int bitField0_;
         private MysqlxExpr.Identifier name_ = MysqlxExpr.Identifier.getDefaultInstance();
         private SingleFieldBuilder<MysqlxExpr.Identifier, MysqlxExpr.Identifier.Builder, MysqlxExpr.IdentifierOrBuilder> nameBuilder_;
         private List<MysqlxExpr.Expr> param_ = Collections.emptyList();
         private RepeatedFieldBuilder<MysqlxExpr.Expr, MysqlxExpr.Expr.Builder, MysqlxExpr.ExprOrBuilder> paramBuilder_;

         public static final Descriptor getDescriptor() {
            return MysqlxExpr.internal_static_Mysqlx_Expr_FunctionCall_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxExpr.internal_static_Mysqlx_Expr_FunctionCall_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxExpr.FunctionCall.class, MysqlxExpr.FunctionCall.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxExpr.FunctionCall.alwaysUseFieldBuilders) {
               this.getNameFieldBuilder();
               this.getParamFieldBuilder();
            }
         }

         private static MysqlxExpr.FunctionCall.Builder create() {
            return new MysqlxExpr.FunctionCall.Builder();
         }

         public MysqlxExpr.FunctionCall.Builder clear() {
            super.clear();
            if (this.nameBuilder_ == null) {
               this.name_ = MysqlxExpr.Identifier.getDefaultInstance();
            } else {
               this.nameBuilder_.clear();
            }

            this.bitField0_ &= -2;
            if (this.paramBuilder_ == null) {
               this.param_ = Collections.emptyList();
               this.bitField0_ &= -3;
            } else {
               this.paramBuilder_.clear();
            }

            return this;
         }

         public MysqlxExpr.FunctionCall.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxExpr.internal_static_Mysqlx_Expr_FunctionCall_descriptor;
         }

         public MysqlxExpr.FunctionCall getDefaultInstanceForType() {
            return MysqlxExpr.FunctionCall.getDefaultInstance();
         }

         public MysqlxExpr.FunctionCall build() {
            MysqlxExpr.FunctionCall result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxExpr.FunctionCall buildPartial() {
            MysqlxExpr.FunctionCall result = new MysqlxExpr.FunctionCall(this);
            int from_bitField0_ = this.bitField0_;
            int to_bitField0_ = 0;
            if ((from_bitField0_ & 1) == 1) {
               to_bitField0_ |= 1;
            }

            if (this.nameBuilder_ == null) {
               result.name_ = this.name_;
            } else {
               result.name_ = (MysqlxExpr.Identifier)this.nameBuilder_.build();
            }

            if (this.paramBuilder_ == null) {
               if ((this.bitField0_ & 2) == 2) {
                  this.param_ = Collections.unmodifiableList(this.param_);
                  this.bitField0_ &= -3;
               }

               result.param_ = this.param_;
            } else {
               result.param_ = this.paramBuilder_.build();
            }

            result.bitField0_ = to_bitField0_;
            this.onBuilt();
            return result;
         }

         public MysqlxExpr.FunctionCall.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxExpr.FunctionCall) {
               return this.mergeFrom((MysqlxExpr.FunctionCall)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxExpr.FunctionCall.Builder mergeFrom(MysqlxExpr.FunctionCall other) {
            if (other == MysqlxExpr.FunctionCall.getDefaultInstance()) {
               return this;
            } else {
               if (other.hasName()) {
                  this.mergeName(other.getName());
               }

               if (this.paramBuilder_ == null) {
                  if (!other.param_.isEmpty()) {
                     if (this.param_.isEmpty()) {
                        this.param_ = other.param_;
                        this.bitField0_ &= -3;
                     } else {
                        this.ensureParamIsMutable();
                        this.param_.addAll(other.param_);
                     }

                     this.onChanged();
                  }
               } else if (!other.param_.isEmpty()) {
                  if (this.paramBuilder_.isEmpty()) {
                     this.paramBuilder_.dispose();
                     this.paramBuilder_ = null;
                     this.param_ = other.param_;
                     this.bitField0_ &= -3;
                     this.paramBuilder_ = MysqlxExpr.FunctionCall.alwaysUseFieldBuilders ? this.getParamFieldBuilder() : null;
                  } else {
                     this.paramBuilder_.addAllMessages(other.param_);
                  }
               }

               this.mergeUnknownFields(other.getUnknownFields());
               return this;
            }
         }

         public final boolean isInitialized() {
            if (!this.hasName()) {
               return false;
            } else if (!this.getName().isInitialized()) {
               return false;
            } else {
               for(int i = 0; i < this.getParamCount(); ++i) {
                  if (!this.getParam(i).isInitialized()) {
                     return false;
                  }
               }

               return true;
            }
         }

         public MysqlxExpr.FunctionCall.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxExpr.FunctionCall parsedMessage = null;

            try {
               parsedMessage = (MysqlxExpr.FunctionCall)MysqlxExpr.FunctionCall.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxExpr.FunctionCall)var8.getUnfinishedMessage();
               throw var8;
            } finally {
               if (parsedMessage != null) {
                  this.mergeFrom(parsedMessage);
               }
            }

            return this;
         }

         @Override
         public boolean hasName() {
            return (this.bitField0_ & 1) == 1;
         }

         @Override
         public MysqlxExpr.Identifier getName() {
            return this.nameBuilder_ == null ? this.name_ : (MysqlxExpr.Identifier)this.nameBuilder_.getMessage();
         }

         public MysqlxExpr.FunctionCall.Builder setName(MysqlxExpr.Identifier value) {
            if (this.nameBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.name_ = value;
               this.onChanged();
            } else {
               this.nameBuilder_.setMessage(value);
            }

            this.bitField0_ |= 1;
            return this;
         }

         public MysqlxExpr.FunctionCall.Builder setName(MysqlxExpr.Identifier.Builder builderForValue) {
            if (this.nameBuilder_ == null) {
               this.name_ = builderForValue.build();
               this.onChanged();
            } else {
               this.nameBuilder_.setMessage(builderForValue.build());
            }

            this.bitField0_ |= 1;
            return this;
         }

         public MysqlxExpr.FunctionCall.Builder mergeName(MysqlxExpr.Identifier value) {
            if (this.nameBuilder_ == null) {
               if ((this.bitField0_ & 1) == 1 && this.name_ != MysqlxExpr.Identifier.getDefaultInstance()) {
                  this.name_ = MysqlxExpr.Identifier.newBuilder(this.name_).mergeFrom(value).buildPartial();
               } else {
                  this.name_ = value;
               }

               this.onChanged();
            } else {
               this.nameBuilder_.mergeFrom(value);
            }

            this.bitField0_ |= 1;
            return this;
         }

         public MysqlxExpr.FunctionCall.Builder clearName() {
            if (this.nameBuilder_ == null) {
               this.name_ = MysqlxExpr.Identifier.getDefaultInstance();
               this.onChanged();
            } else {
               this.nameBuilder_.clear();
            }

            this.bitField0_ &= -2;
            return this;
         }

         public MysqlxExpr.Identifier.Builder getNameBuilder() {
            this.bitField0_ |= 1;
            this.onChanged();
            return (MysqlxExpr.Identifier.Builder)this.getNameFieldBuilder().getBuilder();
         }

         @Override
         public MysqlxExpr.IdentifierOrBuilder getNameOrBuilder() {
            return (MysqlxExpr.IdentifierOrBuilder)(this.nameBuilder_ != null
               ? (MysqlxExpr.IdentifierOrBuilder)this.nameBuilder_.getMessageOrBuilder()
               : this.name_);
         }

         private SingleFieldBuilder<MysqlxExpr.Identifier, MysqlxExpr.Identifier.Builder, MysqlxExpr.IdentifierOrBuilder> getNameFieldBuilder() {
            if (this.nameBuilder_ == null) {
               this.nameBuilder_ = new SingleFieldBuilder(this.getName(), this.getParentForChildren(), this.isClean());
               this.name_ = null;
            }

            return this.nameBuilder_;
         }

         private void ensureParamIsMutable() {
            if ((this.bitField0_ & 2) != 2) {
               this.param_ = new ArrayList<>(this.param_);
               this.bitField0_ |= 2;
            }
         }

         @Override
         public List<MysqlxExpr.Expr> getParamList() {
            return this.paramBuilder_ == null ? Collections.unmodifiableList(this.param_) : this.paramBuilder_.getMessageList();
         }

         @Override
         public int getParamCount() {
            return this.paramBuilder_ == null ? this.param_.size() : this.paramBuilder_.getCount();
         }

         @Override
         public MysqlxExpr.Expr getParam(int index) {
            return this.paramBuilder_ == null ? this.param_.get(index) : (MysqlxExpr.Expr)this.paramBuilder_.getMessage(index);
         }

         public MysqlxExpr.FunctionCall.Builder setParam(int index, MysqlxExpr.Expr value) {
            if (this.paramBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureParamIsMutable();
               this.param_.set(index, value);
               this.onChanged();
            } else {
               this.paramBuilder_.setMessage(index, value);
            }

            return this;
         }

         public MysqlxExpr.FunctionCall.Builder setParam(int index, MysqlxExpr.Expr.Builder builderForValue) {
            if (this.paramBuilder_ == null) {
               this.ensureParamIsMutable();
               this.param_.set(index, builderForValue.build());
               this.onChanged();
            } else {
               this.paramBuilder_.setMessage(index, builderForValue.build());
            }

            return this;
         }

         public MysqlxExpr.FunctionCall.Builder addParam(MysqlxExpr.Expr value) {
            if (this.paramBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureParamIsMutable();
               this.param_.add(value);
               this.onChanged();
            } else {
               this.paramBuilder_.addMessage(value);
            }

            return this;
         }

         public MysqlxExpr.FunctionCall.Builder addParam(int index, MysqlxExpr.Expr value) {
            if (this.paramBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureParamIsMutable();
               this.param_.add(index, value);
               this.onChanged();
            } else {
               this.paramBuilder_.addMessage(index, value);
            }

            return this;
         }

         public MysqlxExpr.FunctionCall.Builder addParam(MysqlxExpr.Expr.Builder builderForValue) {
            if (this.paramBuilder_ == null) {
               this.ensureParamIsMutable();
               this.param_.add(builderForValue.build());
               this.onChanged();
            } else {
               this.paramBuilder_.addMessage(builderForValue.build());
            }

            return this;
         }

         public MysqlxExpr.FunctionCall.Builder addParam(int index, MysqlxExpr.Expr.Builder builderForValue) {
            if (this.paramBuilder_ == null) {
               this.ensureParamIsMutable();
               this.param_.add(index, builderForValue.build());
               this.onChanged();
            } else {
               this.paramBuilder_.addMessage(index, builderForValue.build());
            }

            return this;
         }

         public MysqlxExpr.FunctionCall.Builder addAllParam(Iterable<? extends MysqlxExpr.Expr> values) {
            if (this.paramBuilder_ == null) {
               this.ensureParamIsMutable();
               com.google.protobuf.AbstractMessageLite.Builder.addAll(values, this.param_);
               this.onChanged();
            } else {
               this.paramBuilder_.addAllMessages(values);
            }

            return this;
         }

         public MysqlxExpr.FunctionCall.Builder clearParam() {
            if (this.paramBuilder_ == null) {
               this.param_ = Collections.emptyList();
               this.bitField0_ &= -3;
               this.onChanged();
            } else {
               this.paramBuilder_.clear();
            }

            return this;
         }

         public MysqlxExpr.FunctionCall.Builder removeParam(int index) {
            if (this.paramBuilder_ == null) {
               this.ensureParamIsMutable();
               this.param_.remove(index);
               this.onChanged();
            } else {
               this.paramBuilder_.remove(index);
            }

            return this;
         }

         public MysqlxExpr.Expr.Builder getParamBuilder(int index) {
            return (MysqlxExpr.Expr.Builder)this.getParamFieldBuilder().getBuilder(index);
         }

         @Override
         public MysqlxExpr.ExprOrBuilder getParamOrBuilder(int index) {
            return this.paramBuilder_ == null ? this.param_.get(index) : (MysqlxExpr.ExprOrBuilder)this.paramBuilder_.getMessageOrBuilder(index);
         }

         @Override
         public List<? extends MysqlxExpr.ExprOrBuilder> getParamOrBuilderList() {
            return this.paramBuilder_ != null ? this.paramBuilder_.getMessageOrBuilderList() : Collections.unmodifiableList(this.param_);
         }

         public MysqlxExpr.Expr.Builder addParamBuilder() {
            return (MysqlxExpr.Expr.Builder)this.getParamFieldBuilder().addBuilder(MysqlxExpr.Expr.getDefaultInstance());
         }

         public MysqlxExpr.Expr.Builder addParamBuilder(int index) {
            return (MysqlxExpr.Expr.Builder)this.getParamFieldBuilder().addBuilder(index, MysqlxExpr.Expr.getDefaultInstance());
         }

         public List<MysqlxExpr.Expr.Builder> getParamBuilderList() {
            return this.getParamFieldBuilder().getBuilderList();
         }

         private RepeatedFieldBuilder<MysqlxExpr.Expr, MysqlxExpr.Expr.Builder, MysqlxExpr.ExprOrBuilder> getParamFieldBuilder() {
            if (this.paramBuilder_ == null) {
               this.paramBuilder_ = new RepeatedFieldBuilder(this.param_, (this.bitField0_ & 2) == 2, this.getParentForChildren(), this.isClean());
               this.param_ = null;
            }

            return this.paramBuilder_;
         }
      }
   }

   public interface FunctionCallOrBuilder extends MessageOrBuilder {
      boolean hasName();

      MysqlxExpr.Identifier getName();

      MysqlxExpr.IdentifierOrBuilder getNameOrBuilder();

      List<MysqlxExpr.Expr> getParamList();

      MysqlxExpr.Expr getParam(int var1);

      int getParamCount();

      List<? extends MysqlxExpr.ExprOrBuilder> getParamOrBuilderList();

      MysqlxExpr.ExprOrBuilder getParamOrBuilder(int var1);
   }

   public static final class Identifier extends GeneratedMessage implements MysqlxExpr.IdentifierOrBuilder {
      private static final MysqlxExpr.Identifier defaultInstance = new MysqlxExpr.Identifier(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxExpr.Identifier> PARSER = new AbstractParser<MysqlxExpr.Identifier>() {
         public MysqlxExpr.Identifier parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxExpr.Identifier(input, extensionRegistry);
         }
      };
      private int bitField0_;
      public static final int NAME_FIELD_NUMBER = 1;
      private java.lang.Object name_;
      public static final int SCHEMA_NAME_FIELD_NUMBER = 2;
      private java.lang.Object schemaName_;
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private Identifier(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private Identifier(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static MysqlxExpr.Identifier getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxExpr.Identifier getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private Identifier(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                  case 10: {
                     ByteString bs = input.readBytes();
                     this.bitField0_ |= 1;
                     this.name_ = bs;
                     break;
                  }
                  case 18: {
                     ByteString bs = input.readBytes();
                     this.bitField0_ |= 2;
                     this.schemaName_ = bs;
                     break;
                  }
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
         return MysqlxExpr.internal_static_Mysqlx_Expr_Identifier_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxExpr.internal_static_Mysqlx_Expr_Identifier_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxExpr.Identifier.class, MysqlxExpr.Identifier.Builder.class);
      }

      public Parser<MysqlxExpr.Identifier> getParserForType() {
         return PARSER;
      }

      @Override
      public boolean hasName() {
         return (this.bitField0_ & 1) == 1;
      }

      @Override
      public String getName() {
         java.lang.Object ref = this.name_;
         if (ref instanceof String) {
            return (String)ref;
         } else {
            ByteString bs = (ByteString)ref;
            String s = bs.toStringUtf8();
            if (bs.isValidUtf8()) {
               this.name_ = s;
            }

            return s;
         }
      }

      @Override
      public ByteString getNameBytes() {
         java.lang.Object ref = this.name_;
         if (ref instanceof String) {
            ByteString b = ByteString.copyFromUtf8((String)ref);
            this.name_ = b;
            return b;
         } else {
            return (ByteString)ref;
         }
      }

      @Override
      public boolean hasSchemaName() {
         return (this.bitField0_ & 2) == 2;
      }

      @Override
      public String getSchemaName() {
         java.lang.Object ref = this.schemaName_;
         if (ref instanceof String) {
            return (String)ref;
         } else {
            ByteString bs = (ByteString)ref;
            String s = bs.toStringUtf8();
            if (bs.isValidUtf8()) {
               this.schemaName_ = s;
            }

            return s;
         }
      }

      @Override
      public ByteString getSchemaNameBytes() {
         java.lang.Object ref = this.schemaName_;
         if (ref instanceof String) {
            ByteString b = ByteString.copyFromUtf8((String)ref);
            this.schemaName_ = b;
            return b;
         } else {
            return (ByteString)ref;
         }
      }

      private void initFields() {
         this.name_ = "";
         this.schemaName_ = "";
      }

      public final boolean isInitialized() {
         byte isInitialized = this.memoizedIsInitialized;
         if (isInitialized == 1) {
            return true;
         } else if (isInitialized == 0) {
            return false;
         } else if (!this.hasName()) {
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
            output.writeBytes(1, this.getNameBytes());
         }

         if ((this.bitField0_ & 2) == 2) {
            output.writeBytes(2, this.getSchemaNameBytes());
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
               size += CodedOutputStream.computeBytesSize(1, this.getNameBytes());
            }

            if ((this.bitField0_ & 2) == 2) {
               size += CodedOutputStream.computeBytesSize(2, this.getSchemaNameBytes());
            }

            size += this.getUnknownFields().getSerializedSize();
            this.memoizedSerializedSize = size;
            return size;
         }
      }

      protected java.lang.Object writeReplace() throws ObjectStreamException {
         return super.writeReplace();
      }

      public static MysqlxExpr.Identifier parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxExpr.Identifier)PARSER.parseFrom(data);
      }

      public static MysqlxExpr.Identifier parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxExpr.Identifier)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxExpr.Identifier parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxExpr.Identifier)PARSER.parseFrom(data);
      }

      public static MysqlxExpr.Identifier parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxExpr.Identifier)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxExpr.Identifier parseFrom(InputStream input) throws IOException {
         return (MysqlxExpr.Identifier)PARSER.parseFrom(input);
      }

      public static MysqlxExpr.Identifier parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxExpr.Identifier)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxExpr.Identifier parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxExpr.Identifier)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxExpr.Identifier parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxExpr.Identifier)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxExpr.Identifier parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxExpr.Identifier)PARSER.parseFrom(input);
      }

      public static MysqlxExpr.Identifier parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxExpr.Identifier)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxExpr.Identifier.Builder newBuilder() {
         return MysqlxExpr.Identifier.Builder.create();
      }

      public MysqlxExpr.Identifier.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxExpr.Identifier.Builder newBuilder(MysqlxExpr.Identifier prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxExpr.Identifier.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxExpr.Identifier.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxExpr.Identifier.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxExpr.Identifier.Builder>
         implements MysqlxExpr.IdentifierOrBuilder {
         private int bitField0_;
         private java.lang.Object name_ = "";
         private java.lang.Object schemaName_ = "";

         public static final Descriptor getDescriptor() {
            return MysqlxExpr.internal_static_Mysqlx_Expr_Identifier_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxExpr.internal_static_Mysqlx_Expr_Identifier_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxExpr.Identifier.class, MysqlxExpr.Identifier.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxExpr.Identifier.alwaysUseFieldBuilders) {
            }
         }

         private static MysqlxExpr.Identifier.Builder create() {
            return new MysqlxExpr.Identifier.Builder();
         }

         public MysqlxExpr.Identifier.Builder clear() {
            super.clear();
            this.name_ = "";
            this.bitField0_ &= -2;
            this.schemaName_ = "";
            this.bitField0_ &= -3;
            return this;
         }

         public MysqlxExpr.Identifier.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxExpr.internal_static_Mysqlx_Expr_Identifier_descriptor;
         }

         public MysqlxExpr.Identifier getDefaultInstanceForType() {
            return MysqlxExpr.Identifier.getDefaultInstance();
         }

         public MysqlxExpr.Identifier build() {
            MysqlxExpr.Identifier result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxExpr.Identifier buildPartial() {
            MysqlxExpr.Identifier result = new MysqlxExpr.Identifier(this);
            int from_bitField0_ = this.bitField0_;
            int to_bitField0_ = 0;
            if ((from_bitField0_ & 1) == 1) {
               to_bitField0_ |= 1;
            }

            result.name_ = this.name_;
            if ((from_bitField0_ & 2) == 2) {
               to_bitField0_ |= 2;
            }

            result.schemaName_ = this.schemaName_;
            result.bitField0_ = to_bitField0_;
            this.onBuilt();
            return result;
         }

         public MysqlxExpr.Identifier.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxExpr.Identifier) {
               return this.mergeFrom((MysqlxExpr.Identifier)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxExpr.Identifier.Builder mergeFrom(MysqlxExpr.Identifier other) {
            if (other == MysqlxExpr.Identifier.getDefaultInstance()) {
               return this;
            } else {
               if (other.hasName()) {
                  this.bitField0_ |= 1;
                  this.name_ = other.name_;
                  this.onChanged();
               }

               if (other.hasSchemaName()) {
                  this.bitField0_ |= 2;
                  this.schemaName_ = other.schemaName_;
                  this.onChanged();
               }

               this.mergeUnknownFields(other.getUnknownFields());
               return this;
            }
         }

         public final boolean isInitialized() {
            return this.hasName();
         }

         public MysqlxExpr.Identifier.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxExpr.Identifier parsedMessage = null;

            try {
               parsedMessage = (MysqlxExpr.Identifier)MysqlxExpr.Identifier.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxExpr.Identifier)var8.getUnfinishedMessage();
               throw var8;
            } finally {
               if (parsedMessage != null) {
                  this.mergeFrom(parsedMessage);
               }
            }

            return this;
         }

         @Override
         public boolean hasName() {
            return (this.bitField0_ & 1) == 1;
         }

         @Override
         public String getName() {
            java.lang.Object ref = this.name_;
            if (!(ref instanceof String)) {
               ByteString bs = (ByteString)ref;
               String s = bs.toStringUtf8();
               if (bs.isValidUtf8()) {
                  this.name_ = s;
               }

               return s;
            } else {
               return (String)ref;
            }
         }

         @Override
         public ByteString getNameBytes() {
            java.lang.Object ref = this.name_;
            if (ref instanceof String) {
               ByteString b = ByteString.copyFromUtf8((String)ref);
               this.name_ = b;
               return b;
            } else {
               return (ByteString)ref;
            }
         }

         public MysqlxExpr.Identifier.Builder setName(String value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 1;
               this.name_ = value;
               this.onChanged();
               return this;
            }
         }

         public MysqlxExpr.Identifier.Builder clearName() {
            this.bitField0_ &= -2;
            this.name_ = MysqlxExpr.Identifier.getDefaultInstance().getName();
            this.onChanged();
            return this;
         }

         public MysqlxExpr.Identifier.Builder setNameBytes(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 1;
               this.name_ = value;
               this.onChanged();
               return this;
            }
         }

         @Override
         public boolean hasSchemaName() {
            return (this.bitField0_ & 2) == 2;
         }

         @Override
         public String getSchemaName() {
            java.lang.Object ref = this.schemaName_;
            if (!(ref instanceof String)) {
               ByteString bs = (ByteString)ref;
               String s = bs.toStringUtf8();
               if (bs.isValidUtf8()) {
                  this.schemaName_ = s;
               }

               return s;
            } else {
               return (String)ref;
            }
         }

         @Override
         public ByteString getSchemaNameBytes() {
            java.lang.Object ref = this.schemaName_;
            if (ref instanceof String) {
               ByteString b = ByteString.copyFromUtf8((String)ref);
               this.schemaName_ = b;
               return b;
            } else {
               return (ByteString)ref;
            }
         }

         public MysqlxExpr.Identifier.Builder setSchemaName(String value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 2;
               this.schemaName_ = value;
               this.onChanged();
               return this;
            }
         }

         public MysqlxExpr.Identifier.Builder clearSchemaName() {
            this.bitField0_ &= -3;
            this.schemaName_ = MysqlxExpr.Identifier.getDefaultInstance().getSchemaName();
            this.onChanged();
            return this;
         }

         public MysqlxExpr.Identifier.Builder setSchemaNameBytes(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 2;
               this.schemaName_ = value;
               this.onChanged();
               return this;
            }
         }
      }
   }

   public interface IdentifierOrBuilder extends MessageOrBuilder {
      boolean hasName();

      String getName();

      ByteString getNameBytes();

      boolean hasSchemaName();

      String getSchemaName();

      ByteString getSchemaNameBytes();
   }

   public static final class Object extends GeneratedMessage implements MysqlxExpr.ObjectOrBuilder {
      private static final MysqlxExpr.Object defaultInstance = new MysqlxExpr.Object(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxExpr.Object> PARSER = new AbstractParser<MysqlxExpr.Object>() {
         public MysqlxExpr.Object parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxExpr.Object(input, extensionRegistry);
         }
      };
      public static final int FLD_FIELD_NUMBER = 1;
      private List<MysqlxExpr.Object.ObjectField> fld_;
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

      public static MysqlxExpr.Object getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxExpr.Object getDefaultInstanceForType() {
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

                     this.fld_.add(input.readMessage(MysqlxExpr.Object.ObjectField.PARSER, extensionRegistry));
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
         return MysqlxExpr.internal_static_Mysqlx_Expr_Object_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxExpr.internal_static_Mysqlx_Expr_Object_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxExpr.Object.class, MysqlxExpr.Object.Builder.class);
      }

      public Parser<MysqlxExpr.Object> getParserForType() {
         return PARSER;
      }

      @Override
      public List<MysqlxExpr.Object.ObjectField> getFldList() {
         return this.fld_;
      }

      @Override
      public List<? extends MysqlxExpr.Object.ObjectFieldOrBuilder> getFldOrBuilderList() {
         return this.fld_;
      }

      @Override
      public int getFldCount() {
         return this.fld_.size();
      }

      @Override
      public MysqlxExpr.Object.ObjectField getFld(int index) {
         return this.fld_.get(index);
      }

      @Override
      public MysqlxExpr.Object.ObjectFieldOrBuilder getFldOrBuilder(int index) {
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

      public static MysqlxExpr.Object parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxExpr.Object)PARSER.parseFrom(data);
      }

      public static MysqlxExpr.Object parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxExpr.Object)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxExpr.Object parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxExpr.Object)PARSER.parseFrom(data);
      }

      public static MysqlxExpr.Object parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxExpr.Object)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxExpr.Object parseFrom(InputStream input) throws IOException {
         return (MysqlxExpr.Object)PARSER.parseFrom(input);
      }

      public static MysqlxExpr.Object parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxExpr.Object)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxExpr.Object parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxExpr.Object)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxExpr.Object parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxExpr.Object)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxExpr.Object parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxExpr.Object)PARSER.parseFrom(input);
      }

      public static MysqlxExpr.Object parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxExpr.Object)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxExpr.Object.Builder newBuilder() {
         return MysqlxExpr.Object.Builder.create();
      }

      public MysqlxExpr.Object.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxExpr.Object.Builder newBuilder(MysqlxExpr.Object prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxExpr.Object.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxExpr.Object.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxExpr.Object.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder extends com.google.protobuf.GeneratedMessage.Builder<MysqlxExpr.Object.Builder> implements MysqlxExpr.ObjectOrBuilder {
         private int bitField0_;
         private List<MysqlxExpr.Object.ObjectField> fld_ = Collections.emptyList();
         private RepeatedFieldBuilder<MysqlxExpr.Object.ObjectField, MysqlxExpr.Object.ObjectField.Builder, MysqlxExpr.Object.ObjectFieldOrBuilder> fldBuilder_;

         public static final Descriptor getDescriptor() {
            return MysqlxExpr.internal_static_Mysqlx_Expr_Object_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxExpr.internal_static_Mysqlx_Expr_Object_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxExpr.Object.class, MysqlxExpr.Object.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxExpr.Object.alwaysUseFieldBuilders) {
               this.getFldFieldBuilder();
            }
         }

         private static MysqlxExpr.Object.Builder create() {
            return new MysqlxExpr.Object.Builder();
         }

         public MysqlxExpr.Object.Builder clear() {
            super.clear();
            if (this.fldBuilder_ == null) {
               this.fld_ = Collections.emptyList();
               this.bitField0_ &= -2;
            } else {
               this.fldBuilder_.clear();
            }

            return this;
         }

         public MysqlxExpr.Object.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxExpr.internal_static_Mysqlx_Expr_Object_descriptor;
         }

         public MysqlxExpr.Object getDefaultInstanceForType() {
            return MysqlxExpr.Object.getDefaultInstance();
         }

         public MysqlxExpr.Object build() {
            MysqlxExpr.Object result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxExpr.Object buildPartial() {
            MysqlxExpr.Object result = new MysqlxExpr.Object(this);
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

         public MysqlxExpr.Object.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxExpr.Object) {
               return this.mergeFrom((MysqlxExpr.Object)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxExpr.Object.Builder mergeFrom(MysqlxExpr.Object other) {
            if (other == MysqlxExpr.Object.getDefaultInstance()) {
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
                     this.fldBuilder_ = MysqlxExpr.Object.alwaysUseFieldBuilders ? this.getFldFieldBuilder() : null;
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

         public MysqlxExpr.Object.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxExpr.Object parsedMessage = null;

            try {
               parsedMessage = (MysqlxExpr.Object)MysqlxExpr.Object.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxExpr.Object)var8.getUnfinishedMessage();
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
         public List<MysqlxExpr.Object.ObjectField> getFldList() {
            return this.fldBuilder_ == null ? Collections.unmodifiableList(this.fld_) : this.fldBuilder_.getMessageList();
         }

         @Override
         public int getFldCount() {
            return this.fldBuilder_ == null ? this.fld_.size() : this.fldBuilder_.getCount();
         }

         @Override
         public MysqlxExpr.Object.ObjectField getFld(int index) {
            return this.fldBuilder_ == null ? this.fld_.get(index) : (MysqlxExpr.Object.ObjectField)this.fldBuilder_.getMessage(index);
         }

         public MysqlxExpr.Object.Builder setFld(int index, MysqlxExpr.Object.ObjectField value) {
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

         public MysqlxExpr.Object.Builder setFld(int index, MysqlxExpr.Object.ObjectField.Builder builderForValue) {
            if (this.fldBuilder_ == null) {
               this.ensureFldIsMutable();
               this.fld_.set(index, builderForValue.build());
               this.onChanged();
            } else {
               this.fldBuilder_.setMessage(index, builderForValue.build());
            }

            return this;
         }

         public MysqlxExpr.Object.Builder addFld(MysqlxExpr.Object.ObjectField value) {
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

         public MysqlxExpr.Object.Builder addFld(int index, MysqlxExpr.Object.ObjectField value) {
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

         public MysqlxExpr.Object.Builder addFld(MysqlxExpr.Object.ObjectField.Builder builderForValue) {
            if (this.fldBuilder_ == null) {
               this.ensureFldIsMutable();
               this.fld_.add(builderForValue.build());
               this.onChanged();
            } else {
               this.fldBuilder_.addMessage(builderForValue.build());
            }

            return this;
         }

         public MysqlxExpr.Object.Builder addFld(int index, MysqlxExpr.Object.ObjectField.Builder builderForValue) {
            if (this.fldBuilder_ == null) {
               this.ensureFldIsMutable();
               this.fld_.add(index, builderForValue.build());
               this.onChanged();
            } else {
               this.fldBuilder_.addMessage(index, builderForValue.build());
            }

            return this;
         }

         public MysqlxExpr.Object.Builder addAllFld(Iterable<? extends MysqlxExpr.Object.ObjectField> values) {
            if (this.fldBuilder_ == null) {
               this.ensureFldIsMutable();
               com.google.protobuf.AbstractMessageLite.Builder.addAll(values, this.fld_);
               this.onChanged();
            } else {
               this.fldBuilder_.addAllMessages(values);
            }

            return this;
         }

         public MysqlxExpr.Object.Builder clearFld() {
            if (this.fldBuilder_ == null) {
               this.fld_ = Collections.emptyList();
               this.bitField0_ &= -2;
               this.onChanged();
            } else {
               this.fldBuilder_.clear();
            }

            return this;
         }

         public MysqlxExpr.Object.Builder removeFld(int index) {
            if (this.fldBuilder_ == null) {
               this.ensureFldIsMutable();
               this.fld_.remove(index);
               this.onChanged();
            } else {
               this.fldBuilder_.remove(index);
            }

            return this;
         }

         public MysqlxExpr.Object.ObjectField.Builder getFldBuilder(int index) {
            return (MysqlxExpr.Object.ObjectField.Builder)this.getFldFieldBuilder().getBuilder(index);
         }

         @Override
         public MysqlxExpr.Object.ObjectFieldOrBuilder getFldOrBuilder(int index) {
            return this.fldBuilder_ == null ? this.fld_.get(index) : (MysqlxExpr.Object.ObjectFieldOrBuilder)this.fldBuilder_.getMessageOrBuilder(index);
         }

         @Override
         public List<? extends MysqlxExpr.Object.ObjectFieldOrBuilder> getFldOrBuilderList() {
            return this.fldBuilder_ != null ? this.fldBuilder_.getMessageOrBuilderList() : Collections.unmodifiableList(this.fld_);
         }

         public MysqlxExpr.Object.ObjectField.Builder addFldBuilder() {
            return (MysqlxExpr.Object.ObjectField.Builder)this.getFldFieldBuilder().addBuilder(MysqlxExpr.Object.ObjectField.getDefaultInstance());
         }

         public MysqlxExpr.Object.ObjectField.Builder addFldBuilder(int index) {
            return (MysqlxExpr.Object.ObjectField.Builder)this.getFldFieldBuilder().addBuilder(index, MysqlxExpr.Object.ObjectField.getDefaultInstance());
         }

         public List<MysqlxExpr.Object.ObjectField.Builder> getFldBuilderList() {
            return this.getFldFieldBuilder().getBuilderList();
         }

         private RepeatedFieldBuilder<MysqlxExpr.Object.ObjectField, MysqlxExpr.Object.ObjectField.Builder, MysqlxExpr.Object.ObjectFieldOrBuilder> getFldFieldBuilder() {
            if (this.fldBuilder_ == null) {
               this.fldBuilder_ = new RepeatedFieldBuilder(this.fld_, (this.bitField0_ & 1) == 1, this.getParentForChildren(), this.isClean());
               this.fld_ = null;
            }

            return this.fldBuilder_;
         }
      }

      public static final class ObjectField extends GeneratedMessage implements MysqlxExpr.Object.ObjectFieldOrBuilder {
         private static final MysqlxExpr.Object.ObjectField defaultInstance = new MysqlxExpr.Object.ObjectField(true);
         private final UnknownFieldSet unknownFields;
         public static Parser<MysqlxExpr.Object.ObjectField> PARSER = new AbstractParser<MysqlxExpr.Object.ObjectField>() {
            public MysqlxExpr.Object.ObjectField parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
               return new MysqlxExpr.Object.ObjectField(input, extensionRegistry);
            }
         };
         private int bitField0_;
         public static final int KEY_FIELD_NUMBER = 1;
         private java.lang.Object key_;
         public static final int VALUE_FIELD_NUMBER = 2;
         private MysqlxExpr.Expr value_;
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

         public static MysqlxExpr.Object.ObjectField getDefaultInstance() {
            return defaultInstance;
         }

         public MysqlxExpr.Object.ObjectField getDefaultInstanceForType() {
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
                        MysqlxExpr.Expr.Builder subBuilder = null;
                        if ((this.bitField0_ & 2) == 2) {
                           subBuilder = this.value_.toBuilder();
                        }

                        this.value_ = (MysqlxExpr.Expr)input.readMessage(MysqlxExpr.Expr.PARSER, extensionRegistry);
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
            return MysqlxExpr.internal_static_Mysqlx_Expr_Object_ObjectField_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxExpr.internal_static_Mysqlx_Expr_Object_ObjectField_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxExpr.Object.ObjectField.class, MysqlxExpr.Object.ObjectField.Builder.class);
         }

         public Parser<MysqlxExpr.Object.ObjectField> getParserForType() {
            return PARSER;
         }

         @Override
         public boolean hasKey() {
            return (this.bitField0_ & 1) == 1;
         }

         @Override
         public String getKey() {
            java.lang.Object ref = this.key_;
            if (ref instanceof String) {
               return (String)ref;
            } else {
               ByteString bs = (ByteString)ref;
               String s = bs.toStringUtf8();
               if (bs.isValidUtf8()) {
                  this.key_ = s;
               }

               return s;
            }
         }

         @Override
         public ByteString getKeyBytes() {
            java.lang.Object ref = this.key_;
            if (ref instanceof String) {
               ByteString b = ByteString.copyFromUtf8((String)ref);
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
         public MysqlxExpr.Expr getValue() {
            return this.value_;
         }

         @Override
         public MysqlxExpr.ExprOrBuilder getValueOrBuilder() {
            return this.value_;
         }

         private void initFields() {
            this.key_ = "";
            this.value_ = MysqlxExpr.Expr.getDefaultInstance();
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

         public static MysqlxExpr.Object.ObjectField parseFrom(ByteString data) throws InvalidProtocolBufferException {
            return (MysqlxExpr.Object.ObjectField)PARSER.parseFrom(data);
         }

         public static MysqlxExpr.Object.ObjectField parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (MysqlxExpr.Object.ObjectField)PARSER.parseFrom(data, extensionRegistry);
         }

         public static MysqlxExpr.Object.ObjectField parseFrom(byte[] data) throws InvalidProtocolBufferException {
            return (MysqlxExpr.Object.ObjectField)PARSER.parseFrom(data);
         }

         public static MysqlxExpr.Object.ObjectField parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return (MysqlxExpr.Object.ObjectField)PARSER.parseFrom(data, extensionRegistry);
         }

         public static MysqlxExpr.Object.ObjectField parseFrom(InputStream input) throws IOException {
            return (MysqlxExpr.Object.ObjectField)PARSER.parseFrom(input);
         }

         public static MysqlxExpr.Object.ObjectField parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (MysqlxExpr.Object.ObjectField)PARSER.parseFrom(input, extensionRegistry);
         }

         public static MysqlxExpr.Object.ObjectField parseDelimitedFrom(InputStream input) throws IOException {
            return (MysqlxExpr.Object.ObjectField)PARSER.parseDelimitedFrom(input);
         }

         public static MysqlxExpr.Object.ObjectField parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (MysqlxExpr.Object.ObjectField)PARSER.parseDelimitedFrom(input, extensionRegistry);
         }

         public static MysqlxExpr.Object.ObjectField parseFrom(CodedInputStream input) throws IOException {
            return (MysqlxExpr.Object.ObjectField)PARSER.parseFrom(input);
         }

         public static MysqlxExpr.Object.ObjectField parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            return (MysqlxExpr.Object.ObjectField)PARSER.parseFrom(input, extensionRegistry);
         }

         public static MysqlxExpr.Object.ObjectField.Builder newBuilder() {
            return MysqlxExpr.Object.ObjectField.Builder.create();
         }

         public MysqlxExpr.Object.ObjectField.Builder newBuilderForType() {
            return newBuilder();
         }

         public static MysqlxExpr.Object.ObjectField.Builder newBuilder(MysqlxExpr.Object.ObjectField prototype) {
            return newBuilder().mergeFrom(prototype);
         }

         public MysqlxExpr.Object.ObjectField.Builder toBuilder() {
            return newBuilder(this);
         }

         protected MysqlxExpr.Object.ObjectField.Builder newBuilderForType(BuilderParent parent) {
            return new MysqlxExpr.Object.ObjectField.Builder(parent);
         }

         static {
            defaultInstance.initFields();
         }

         public static final class Builder
            extends com.google.protobuf.GeneratedMessage.Builder<MysqlxExpr.Object.ObjectField.Builder>
            implements MysqlxExpr.Object.ObjectFieldOrBuilder {
            private int bitField0_;
            private java.lang.Object key_ = "";
            private MysqlxExpr.Expr value_ = MysqlxExpr.Expr.getDefaultInstance();
            private SingleFieldBuilder<MysqlxExpr.Expr, MysqlxExpr.Expr.Builder, MysqlxExpr.ExprOrBuilder> valueBuilder_;

            public static final Descriptor getDescriptor() {
               return MysqlxExpr.internal_static_Mysqlx_Expr_Object_ObjectField_descriptor;
            }

            protected FieldAccessorTable internalGetFieldAccessorTable() {
               return MysqlxExpr.internal_static_Mysqlx_Expr_Object_ObjectField_fieldAccessorTable
                  .ensureFieldAccessorsInitialized(MysqlxExpr.Object.ObjectField.class, MysqlxExpr.Object.ObjectField.Builder.class);
            }

            private Builder() {
               this.maybeForceBuilderInitialization();
            }

            private Builder(BuilderParent parent) {
               super(parent);
               this.maybeForceBuilderInitialization();
            }

            private void maybeForceBuilderInitialization() {
               if (MysqlxExpr.Object.ObjectField.alwaysUseFieldBuilders) {
                  this.getValueFieldBuilder();
               }
            }

            private static MysqlxExpr.Object.ObjectField.Builder create() {
               return new MysqlxExpr.Object.ObjectField.Builder();
            }

            public MysqlxExpr.Object.ObjectField.Builder clear() {
               super.clear();
               this.key_ = "";
               this.bitField0_ &= -2;
               if (this.valueBuilder_ == null) {
                  this.value_ = MysqlxExpr.Expr.getDefaultInstance();
               } else {
                  this.valueBuilder_.clear();
               }

               this.bitField0_ &= -3;
               return this;
            }

            public MysqlxExpr.Object.ObjectField.Builder clone() {
               return create().mergeFrom(this.buildPartial());
            }

            public Descriptor getDescriptorForType() {
               return MysqlxExpr.internal_static_Mysqlx_Expr_Object_ObjectField_descriptor;
            }

            public MysqlxExpr.Object.ObjectField getDefaultInstanceForType() {
               return MysqlxExpr.Object.ObjectField.getDefaultInstance();
            }

            public MysqlxExpr.Object.ObjectField build() {
               MysqlxExpr.Object.ObjectField result = this.buildPartial();
               if (!result.isInitialized()) {
                  throw newUninitializedMessageException(result);
               } else {
                  return result;
               }
            }

            public MysqlxExpr.Object.ObjectField buildPartial() {
               MysqlxExpr.Object.ObjectField result = new MysqlxExpr.Object.ObjectField(this);
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
                  result.value_ = (MysqlxExpr.Expr)this.valueBuilder_.build();
               }

               result.bitField0_ = to_bitField0_;
               this.onBuilt();
               return result;
            }

            public MysqlxExpr.Object.ObjectField.Builder mergeFrom(Message other) {
               if (other instanceof MysqlxExpr.Object.ObjectField) {
                  return this.mergeFrom((MysqlxExpr.Object.ObjectField)other);
               } else {
                  super.mergeFrom(other);
                  return this;
               }
            }

            public MysqlxExpr.Object.ObjectField.Builder mergeFrom(MysqlxExpr.Object.ObjectField other) {
               if (other == MysqlxExpr.Object.ObjectField.getDefaultInstance()) {
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

            public MysqlxExpr.Object.ObjectField.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
               MysqlxExpr.Object.ObjectField parsedMessage = null;

               try {
                  parsedMessage = (MysqlxExpr.Object.ObjectField)MysqlxExpr.Object.ObjectField.PARSER.parsePartialFrom(input, extensionRegistry);
               } catch (InvalidProtocolBufferException var8) {
                  parsedMessage = (MysqlxExpr.Object.ObjectField)var8.getUnfinishedMessage();
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
            public String getKey() {
               java.lang.Object ref = this.key_;
               if (!(ref instanceof String)) {
                  ByteString bs = (ByteString)ref;
                  String s = bs.toStringUtf8();
                  if (bs.isValidUtf8()) {
                     this.key_ = s;
                  }

                  return s;
               } else {
                  return (String)ref;
               }
            }

            @Override
            public ByteString getKeyBytes() {
               java.lang.Object ref = this.key_;
               if (ref instanceof String) {
                  ByteString b = ByteString.copyFromUtf8((String)ref);
                  this.key_ = b;
                  return b;
               } else {
                  return (ByteString)ref;
               }
            }

            public MysqlxExpr.Object.ObjectField.Builder setKey(String value) {
               if (value == null) {
                  throw new NullPointerException();
               } else {
                  this.bitField0_ |= 1;
                  this.key_ = value;
                  this.onChanged();
                  return this;
               }
            }

            public MysqlxExpr.Object.ObjectField.Builder clearKey() {
               this.bitField0_ &= -2;
               this.key_ = MysqlxExpr.Object.ObjectField.getDefaultInstance().getKey();
               this.onChanged();
               return this;
            }

            public MysqlxExpr.Object.ObjectField.Builder setKeyBytes(ByteString value) {
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
            public MysqlxExpr.Expr getValue() {
               return this.valueBuilder_ == null ? this.value_ : (MysqlxExpr.Expr)this.valueBuilder_.getMessage();
            }

            public MysqlxExpr.Object.ObjectField.Builder setValue(MysqlxExpr.Expr value) {
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

            public MysqlxExpr.Object.ObjectField.Builder setValue(MysqlxExpr.Expr.Builder builderForValue) {
               if (this.valueBuilder_ == null) {
                  this.value_ = builderForValue.build();
                  this.onChanged();
               } else {
                  this.valueBuilder_.setMessage(builderForValue.build());
               }

               this.bitField0_ |= 2;
               return this;
            }

            public MysqlxExpr.Object.ObjectField.Builder mergeValue(MysqlxExpr.Expr value) {
               if (this.valueBuilder_ == null) {
                  if ((this.bitField0_ & 2) == 2 && this.value_ != MysqlxExpr.Expr.getDefaultInstance()) {
                     this.value_ = MysqlxExpr.Expr.newBuilder(this.value_).mergeFrom(value).buildPartial();
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

            public MysqlxExpr.Object.ObjectField.Builder clearValue() {
               if (this.valueBuilder_ == null) {
                  this.value_ = MysqlxExpr.Expr.getDefaultInstance();
                  this.onChanged();
               } else {
                  this.valueBuilder_.clear();
               }

               this.bitField0_ &= -3;
               return this;
            }

            public MysqlxExpr.Expr.Builder getValueBuilder() {
               this.bitField0_ |= 2;
               this.onChanged();
               return (MysqlxExpr.Expr.Builder)this.getValueFieldBuilder().getBuilder();
            }

            @Override
            public MysqlxExpr.ExprOrBuilder getValueOrBuilder() {
               return (MysqlxExpr.ExprOrBuilder)(this.valueBuilder_ != null ? (MysqlxExpr.ExprOrBuilder)this.valueBuilder_.getMessageOrBuilder() : this.value_);
            }

            private SingleFieldBuilder<MysqlxExpr.Expr, MysqlxExpr.Expr.Builder, MysqlxExpr.ExprOrBuilder> getValueFieldBuilder() {
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

         String getKey();

         ByteString getKeyBytes();

         boolean hasValue();

         MysqlxExpr.Expr getValue();

         MysqlxExpr.ExprOrBuilder getValueOrBuilder();
      }
   }

   public interface ObjectOrBuilder extends MessageOrBuilder {
      List<MysqlxExpr.Object.ObjectField> getFldList();

      MysqlxExpr.Object.ObjectField getFld(int var1);

      int getFldCount();

      List<? extends MysqlxExpr.Object.ObjectFieldOrBuilder> getFldOrBuilderList();

      MysqlxExpr.Object.ObjectFieldOrBuilder getFldOrBuilder(int var1);
   }

   public static final class Operator extends GeneratedMessage implements MysqlxExpr.OperatorOrBuilder {
      private static final MysqlxExpr.Operator defaultInstance = new MysqlxExpr.Operator(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxExpr.Operator> PARSER = new AbstractParser<MysqlxExpr.Operator>() {
         public MysqlxExpr.Operator parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxExpr.Operator(input, extensionRegistry);
         }
      };
      private int bitField0_;
      public static final int NAME_FIELD_NUMBER = 1;
      private java.lang.Object name_;
      public static final int PARAM_FIELD_NUMBER = 2;
      private List<MysqlxExpr.Expr> param_;
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private Operator(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private Operator(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static MysqlxExpr.Operator getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxExpr.Operator getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private Operator(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                     this.name_ = bs;
                     break;
                  case 18:
                     if ((mutable_bitField0_ & 2) != 2) {
                        this.param_ = new ArrayList<>();
                        mutable_bitField0_ |= 2;
                     }

                     this.param_.add(input.readMessage(MysqlxExpr.Expr.PARSER, extensionRegistry));
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
            if ((mutable_bitField0_ & 2) == 2) {
               this.param_ = Collections.unmodifiableList(this.param_);
            }

            this.unknownFields = unknownFields.build();
            this.makeExtensionsImmutable();
         }
      }

      public static final Descriptor getDescriptor() {
         return MysqlxExpr.internal_static_Mysqlx_Expr_Operator_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxExpr.internal_static_Mysqlx_Expr_Operator_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxExpr.Operator.class, MysqlxExpr.Operator.Builder.class);
      }

      public Parser<MysqlxExpr.Operator> getParserForType() {
         return PARSER;
      }

      @Override
      public boolean hasName() {
         return (this.bitField0_ & 1) == 1;
      }

      @Override
      public String getName() {
         java.lang.Object ref = this.name_;
         if (ref instanceof String) {
            return (String)ref;
         } else {
            ByteString bs = (ByteString)ref;
            String s = bs.toStringUtf8();
            if (bs.isValidUtf8()) {
               this.name_ = s;
            }

            return s;
         }
      }

      @Override
      public ByteString getNameBytes() {
         java.lang.Object ref = this.name_;
         if (ref instanceof String) {
            ByteString b = ByteString.copyFromUtf8((String)ref);
            this.name_ = b;
            return b;
         } else {
            return (ByteString)ref;
         }
      }

      @Override
      public List<MysqlxExpr.Expr> getParamList() {
         return this.param_;
      }

      @Override
      public List<? extends MysqlxExpr.ExprOrBuilder> getParamOrBuilderList() {
         return this.param_;
      }

      @Override
      public int getParamCount() {
         return this.param_.size();
      }

      @Override
      public MysqlxExpr.Expr getParam(int index) {
         return this.param_.get(index);
      }

      @Override
      public MysqlxExpr.ExprOrBuilder getParamOrBuilder(int index) {
         return this.param_.get(index);
      }

      private void initFields() {
         this.name_ = "";
         this.param_ = Collections.emptyList();
      }

      public final boolean isInitialized() {
         byte isInitialized = this.memoizedIsInitialized;
         if (isInitialized == 1) {
            return true;
         } else if (isInitialized == 0) {
            return false;
         } else if (!this.hasName()) {
            this.memoizedIsInitialized = 0;
            return false;
         } else {
            for(int i = 0; i < this.getParamCount(); ++i) {
               if (!this.getParam(i).isInitialized()) {
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
            output.writeBytes(1, this.getNameBytes());
         }

         for(int i = 0; i < this.param_.size(); ++i) {
            output.writeMessage(2, (MessageLite)this.param_.get(i));
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
               size += CodedOutputStream.computeBytesSize(1, this.getNameBytes());
            }

            for(int i = 0; i < this.param_.size(); ++i) {
               size += CodedOutputStream.computeMessageSize(2, (MessageLite)this.param_.get(i));
            }

            size += this.getUnknownFields().getSerializedSize();
            this.memoizedSerializedSize = size;
            return size;
         }
      }

      protected java.lang.Object writeReplace() throws ObjectStreamException {
         return super.writeReplace();
      }

      public static MysqlxExpr.Operator parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxExpr.Operator)PARSER.parseFrom(data);
      }

      public static MysqlxExpr.Operator parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxExpr.Operator)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxExpr.Operator parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxExpr.Operator)PARSER.parseFrom(data);
      }

      public static MysqlxExpr.Operator parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxExpr.Operator)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxExpr.Operator parseFrom(InputStream input) throws IOException {
         return (MysqlxExpr.Operator)PARSER.parseFrom(input);
      }

      public static MysqlxExpr.Operator parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxExpr.Operator)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxExpr.Operator parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxExpr.Operator)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxExpr.Operator parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxExpr.Operator)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxExpr.Operator parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxExpr.Operator)PARSER.parseFrom(input);
      }

      public static MysqlxExpr.Operator parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxExpr.Operator)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxExpr.Operator.Builder newBuilder() {
         return MysqlxExpr.Operator.Builder.create();
      }

      public MysqlxExpr.Operator.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxExpr.Operator.Builder newBuilder(MysqlxExpr.Operator prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxExpr.Operator.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxExpr.Operator.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxExpr.Operator.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxExpr.Operator.Builder>
         implements MysqlxExpr.OperatorOrBuilder {
         private int bitField0_;
         private java.lang.Object name_ = "";
         private List<MysqlxExpr.Expr> param_ = Collections.emptyList();
         private RepeatedFieldBuilder<MysqlxExpr.Expr, MysqlxExpr.Expr.Builder, MysqlxExpr.ExprOrBuilder> paramBuilder_;

         public static final Descriptor getDescriptor() {
            return MysqlxExpr.internal_static_Mysqlx_Expr_Operator_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxExpr.internal_static_Mysqlx_Expr_Operator_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxExpr.Operator.class, MysqlxExpr.Operator.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxExpr.Operator.alwaysUseFieldBuilders) {
               this.getParamFieldBuilder();
            }
         }

         private static MysqlxExpr.Operator.Builder create() {
            return new MysqlxExpr.Operator.Builder();
         }

         public MysqlxExpr.Operator.Builder clear() {
            super.clear();
            this.name_ = "";
            this.bitField0_ &= -2;
            if (this.paramBuilder_ == null) {
               this.param_ = Collections.emptyList();
               this.bitField0_ &= -3;
            } else {
               this.paramBuilder_.clear();
            }

            return this;
         }

         public MysqlxExpr.Operator.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxExpr.internal_static_Mysqlx_Expr_Operator_descriptor;
         }

         public MysqlxExpr.Operator getDefaultInstanceForType() {
            return MysqlxExpr.Operator.getDefaultInstance();
         }

         public MysqlxExpr.Operator build() {
            MysqlxExpr.Operator result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxExpr.Operator buildPartial() {
            MysqlxExpr.Operator result = new MysqlxExpr.Operator(this);
            int from_bitField0_ = this.bitField0_;
            int to_bitField0_ = 0;
            if ((from_bitField0_ & 1) == 1) {
               to_bitField0_ |= 1;
            }

            result.name_ = this.name_;
            if (this.paramBuilder_ == null) {
               if ((this.bitField0_ & 2) == 2) {
                  this.param_ = Collections.unmodifiableList(this.param_);
                  this.bitField0_ &= -3;
               }

               result.param_ = this.param_;
            } else {
               result.param_ = this.paramBuilder_.build();
            }

            result.bitField0_ = to_bitField0_;
            this.onBuilt();
            return result;
         }

         public MysqlxExpr.Operator.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxExpr.Operator) {
               return this.mergeFrom((MysqlxExpr.Operator)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxExpr.Operator.Builder mergeFrom(MysqlxExpr.Operator other) {
            if (other == MysqlxExpr.Operator.getDefaultInstance()) {
               return this;
            } else {
               if (other.hasName()) {
                  this.bitField0_ |= 1;
                  this.name_ = other.name_;
                  this.onChanged();
               }

               if (this.paramBuilder_ == null) {
                  if (!other.param_.isEmpty()) {
                     if (this.param_.isEmpty()) {
                        this.param_ = other.param_;
                        this.bitField0_ &= -3;
                     } else {
                        this.ensureParamIsMutable();
                        this.param_.addAll(other.param_);
                     }

                     this.onChanged();
                  }
               } else if (!other.param_.isEmpty()) {
                  if (this.paramBuilder_.isEmpty()) {
                     this.paramBuilder_.dispose();
                     this.paramBuilder_ = null;
                     this.param_ = other.param_;
                     this.bitField0_ &= -3;
                     this.paramBuilder_ = MysqlxExpr.Operator.alwaysUseFieldBuilders ? this.getParamFieldBuilder() : null;
                  } else {
                     this.paramBuilder_.addAllMessages(other.param_);
                  }
               }

               this.mergeUnknownFields(other.getUnknownFields());
               return this;
            }
         }

         public final boolean isInitialized() {
            if (!this.hasName()) {
               return false;
            } else {
               for(int i = 0; i < this.getParamCount(); ++i) {
                  if (!this.getParam(i).isInitialized()) {
                     return false;
                  }
               }

               return true;
            }
         }

         public MysqlxExpr.Operator.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxExpr.Operator parsedMessage = null;

            try {
               parsedMessage = (MysqlxExpr.Operator)MysqlxExpr.Operator.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxExpr.Operator)var8.getUnfinishedMessage();
               throw var8;
            } finally {
               if (parsedMessage != null) {
                  this.mergeFrom(parsedMessage);
               }
            }

            return this;
         }

         @Override
         public boolean hasName() {
            return (this.bitField0_ & 1) == 1;
         }

         @Override
         public String getName() {
            java.lang.Object ref = this.name_;
            if (!(ref instanceof String)) {
               ByteString bs = (ByteString)ref;
               String s = bs.toStringUtf8();
               if (bs.isValidUtf8()) {
                  this.name_ = s;
               }

               return s;
            } else {
               return (String)ref;
            }
         }

         @Override
         public ByteString getNameBytes() {
            java.lang.Object ref = this.name_;
            if (ref instanceof String) {
               ByteString b = ByteString.copyFromUtf8((String)ref);
               this.name_ = b;
               return b;
            } else {
               return (ByteString)ref;
            }
         }

         public MysqlxExpr.Operator.Builder setName(String value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 1;
               this.name_ = value;
               this.onChanged();
               return this;
            }
         }

         public MysqlxExpr.Operator.Builder clearName() {
            this.bitField0_ &= -2;
            this.name_ = MysqlxExpr.Operator.getDefaultInstance().getName();
            this.onChanged();
            return this;
         }

         public MysqlxExpr.Operator.Builder setNameBytes(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 1;
               this.name_ = value;
               this.onChanged();
               return this;
            }
         }

         private void ensureParamIsMutable() {
            if ((this.bitField0_ & 2) != 2) {
               this.param_ = new ArrayList<>(this.param_);
               this.bitField0_ |= 2;
            }
         }

         @Override
         public List<MysqlxExpr.Expr> getParamList() {
            return this.paramBuilder_ == null ? Collections.unmodifiableList(this.param_) : this.paramBuilder_.getMessageList();
         }

         @Override
         public int getParamCount() {
            return this.paramBuilder_ == null ? this.param_.size() : this.paramBuilder_.getCount();
         }

         @Override
         public MysqlxExpr.Expr getParam(int index) {
            return this.paramBuilder_ == null ? this.param_.get(index) : (MysqlxExpr.Expr)this.paramBuilder_.getMessage(index);
         }

         public MysqlxExpr.Operator.Builder setParam(int index, MysqlxExpr.Expr value) {
            if (this.paramBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureParamIsMutable();
               this.param_.set(index, value);
               this.onChanged();
            } else {
               this.paramBuilder_.setMessage(index, value);
            }

            return this;
         }

         public MysqlxExpr.Operator.Builder setParam(int index, MysqlxExpr.Expr.Builder builderForValue) {
            if (this.paramBuilder_ == null) {
               this.ensureParamIsMutable();
               this.param_.set(index, builderForValue.build());
               this.onChanged();
            } else {
               this.paramBuilder_.setMessage(index, builderForValue.build());
            }

            return this;
         }

         public MysqlxExpr.Operator.Builder addParam(MysqlxExpr.Expr value) {
            if (this.paramBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureParamIsMutable();
               this.param_.add(value);
               this.onChanged();
            } else {
               this.paramBuilder_.addMessage(value);
            }

            return this;
         }

         public MysqlxExpr.Operator.Builder addParam(int index, MysqlxExpr.Expr value) {
            if (this.paramBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureParamIsMutable();
               this.param_.add(index, value);
               this.onChanged();
            } else {
               this.paramBuilder_.addMessage(index, value);
            }

            return this;
         }

         public MysqlxExpr.Operator.Builder addParam(MysqlxExpr.Expr.Builder builderForValue) {
            if (this.paramBuilder_ == null) {
               this.ensureParamIsMutable();
               this.param_.add(builderForValue.build());
               this.onChanged();
            } else {
               this.paramBuilder_.addMessage(builderForValue.build());
            }

            return this;
         }

         public MysqlxExpr.Operator.Builder addParam(int index, MysqlxExpr.Expr.Builder builderForValue) {
            if (this.paramBuilder_ == null) {
               this.ensureParamIsMutable();
               this.param_.add(index, builderForValue.build());
               this.onChanged();
            } else {
               this.paramBuilder_.addMessage(index, builderForValue.build());
            }

            return this;
         }

         public MysqlxExpr.Operator.Builder addAllParam(Iterable<? extends MysqlxExpr.Expr> values) {
            if (this.paramBuilder_ == null) {
               this.ensureParamIsMutable();
               com.google.protobuf.AbstractMessageLite.Builder.addAll(values, this.param_);
               this.onChanged();
            } else {
               this.paramBuilder_.addAllMessages(values);
            }

            return this;
         }

         public MysqlxExpr.Operator.Builder clearParam() {
            if (this.paramBuilder_ == null) {
               this.param_ = Collections.emptyList();
               this.bitField0_ &= -3;
               this.onChanged();
            } else {
               this.paramBuilder_.clear();
            }

            return this;
         }

         public MysqlxExpr.Operator.Builder removeParam(int index) {
            if (this.paramBuilder_ == null) {
               this.ensureParamIsMutable();
               this.param_.remove(index);
               this.onChanged();
            } else {
               this.paramBuilder_.remove(index);
            }

            return this;
         }

         public MysqlxExpr.Expr.Builder getParamBuilder(int index) {
            return (MysqlxExpr.Expr.Builder)this.getParamFieldBuilder().getBuilder(index);
         }

         @Override
         public MysqlxExpr.ExprOrBuilder getParamOrBuilder(int index) {
            return this.paramBuilder_ == null ? this.param_.get(index) : (MysqlxExpr.ExprOrBuilder)this.paramBuilder_.getMessageOrBuilder(index);
         }

         @Override
         public List<? extends MysqlxExpr.ExprOrBuilder> getParamOrBuilderList() {
            return this.paramBuilder_ != null ? this.paramBuilder_.getMessageOrBuilderList() : Collections.unmodifiableList(this.param_);
         }

         public MysqlxExpr.Expr.Builder addParamBuilder() {
            return (MysqlxExpr.Expr.Builder)this.getParamFieldBuilder().addBuilder(MysqlxExpr.Expr.getDefaultInstance());
         }

         public MysqlxExpr.Expr.Builder addParamBuilder(int index) {
            return (MysqlxExpr.Expr.Builder)this.getParamFieldBuilder().addBuilder(index, MysqlxExpr.Expr.getDefaultInstance());
         }

         public List<MysqlxExpr.Expr.Builder> getParamBuilderList() {
            return this.getParamFieldBuilder().getBuilderList();
         }

         private RepeatedFieldBuilder<MysqlxExpr.Expr, MysqlxExpr.Expr.Builder, MysqlxExpr.ExprOrBuilder> getParamFieldBuilder() {
            if (this.paramBuilder_ == null) {
               this.paramBuilder_ = new RepeatedFieldBuilder(this.param_, (this.bitField0_ & 2) == 2, this.getParentForChildren(), this.isClean());
               this.param_ = null;
            }

            return this.paramBuilder_;
         }
      }
   }

   public interface OperatorOrBuilder extends MessageOrBuilder {
      boolean hasName();

      String getName();

      ByteString getNameBytes();

      List<MysqlxExpr.Expr> getParamList();

      MysqlxExpr.Expr getParam(int var1);

      int getParamCount();

      List<? extends MysqlxExpr.ExprOrBuilder> getParamOrBuilderList();

      MysqlxExpr.ExprOrBuilder getParamOrBuilder(int var1);
   }
}
