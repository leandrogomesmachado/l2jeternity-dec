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

public final class MysqlxNotice {
   private static final Descriptor internal_static_Mysqlx_Notice_Frame_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(0);
   private static FieldAccessorTable internal_static_Mysqlx_Notice_Frame_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Notice_Frame_descriptor, new String[]{"Type", "Scope", "Payload"}
   );
   private static final Descriptor internal_static_Mysqlx_Notice_Warning_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(1);
   private static FieldAccessorTable internal_static_Mysqlx_Notice_Warning_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Notice_Warning_descriptor, new String[]{"Level", "Code", "Msg"}
   );
   private static final Descriptor internal_static_Mysqlx_Notice_SessionVariableChanged_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(2);
   private static FieldAccessorTable internal_static_Mysqlx_Notice_SessionVariableChanged_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Notice_SessionVariableChanged_descriptor, new String[]{"Param", "Value"}
   );
   private static final Descriptor internal_static_Mysqlx_Notice_SessionStateChanged_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(3);
   private static FieldAccessorTable internal_static_Mysqlx_Notice_SessionStateChanged_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Notice_SessionStateChanged_descriptor, new String[]{"Param", "Value"}
   );
   private static FileDescriptor descriptor;

   private MysqlxNotice() {
   }

   public static void registerAllExtensions(ExtensionRegistry registry) {
   }

   public static FileDescriptor getDescriptor() {
      return descriptor;
   }

   static {
      String[] descriptorData = new String[]{
         "\n\u0013mysqlx_notice.proto\u0012\rMysqlx.Notice\u001a\fmysqlx.proto\u001a\u0016mysqlx_datatypes.proto\"Í\u0001\n\u0005Frame\u0012\f\n\u0004type\u0018\u0001 \u0002(\r\u00121\n\u0005scope\u0018\u0002 \u0001(\u000e2\u001a.Mysqlx.Notice.Frame.Scope:\u0006GLOBAL\u0012\u000f\n\u0007payload\u0018\u0003 \u0001(\f\"\u001e\n\u0005Scope\u0012\n\n\u0006GLOBAL\u0010\u0001\u0012\t\n\u0005LOCAL\u0010\u0002\"L\n\u0004Type\u0012\u000b\n\u0007WARNING\u0010\u0001\u0012\u001c\n\u0018SESSION_VARIABLE_CHANGED\u0010\u0002\u0012\u0019\n\u0015SESSION_STATE_CHANGED\u0010\u0003:\u0004\u0090ê0\u000b\"\u0085\u0001\n\u0007Warning\u00124\n\u0005level\u0018\u0001 \u0001(\u000e2\u001c.Mysqlx.Notice.Warning.Level:\u0007WARNING\u0012\f\n\u0004code\u0018\u0002 \u0002(\r\u0012\u000b\n\u0003msg\u0018\u0003 \u0002(\t\")\n\u0005Level\u0012\b\n\u0004NOTE\u0010\u0001\u0012\u000b\n\u0007WA",
         "RNING\u0010\u0002\u0012\t\n\u0005ERROR\u0010\u0003\"P\n\u0016SessionVariableChanged\u0012\r\n\u0005param\u0018\u0001 \u0002(\t\u0012'\n\u0005value\u0018\u0002 \u0001(\u000b2\u0018.Mysqlx.Datatypes.Scalar\"ñ\u0002\n\u0013SessionStateChanged\u0012;\n\u0005param\u0018\u0001 \u0002(\u000e2,.Mysqlx.Notice.SessionStateChanged.Parameter\u0012'\n\u0005value\u0018\u0002 \u0003(\u000b2\u0018.Mysqlx.Datatypes.Scalar\"ó\u0001\n\tParameter\u0012\u0012\n\u000eCURRENT_SCHEMA\u0010\u0001\u0012\u0013\n\u000fACCOUNT_EXPIRED\u0010\u0002\u0012\u0017\n\u0013GENERATED_INSERT_ID\u0010\u0003\u0012\u0011\n\rROWS_AFFECTED\u0010\u0004\u0012\u000e\n\nROWS_FOUND\u0010\u0005\u0012\u0010\n\fROWS_MATCHED\u0010\u0006\u0012\u0011\n\rTRX_COMMITTED\u0010\u0007\u0012\u0012\n\u000eTRX_ROLLEDBACK\u0010\t\u0012\u0014",
         "\n\u0010PRODUCED_MESSAGE\u0010\n\u0012\u0016\n\u0012CLIENT_ID_ASSIGNED\u0010\u000b\u0012\u001a\n\u0016GENERATED_DOCUMENT_IDS\u0010\fB\u0019\n\u0017com.mysql.cj.x.protobuf"
      };
      InternalDescriptorAssigner assigner = new InternalDescriptorAssigner() {
         public ExtensionRegistry assignDescriptors(FileDescriptor root) {
            MysqlxNotice.descriptor = root;
            return null;
         }
      };
      FileDescriptor.internalBuildGeneratedFileFrom(descriptorData, new FileDescriptor[]{Mysqlx.getDescriptor(), MysqlxDatatypes.getDescriptor()}, assigner);
      ExtensionRegistry registry = ExtensionRegistry.newInstance();
      registry.add(Mysqlx.serverMessageId);
      FileDescriptor.internalUpdateFileDescriptor(descriptor, registry);
      Mysqlx.getDescriptor();
      MysqlxDatatypes.getDescriptor();
   }

   public static final class Frame extends GeneratedMessage implements MysqlxNotice.FrameOrBuilder {
      private static final MysqlxNotice.Frame defaultInstance = new MysqlxNotice.Frame(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxNotice.Frame> PARSER = new AbstractParser<MysqlxNotice.Frame>() {
         public MysqlxNotice.Frame parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxNotice.Frame(input, extensionRegistry);
         }
      };
      private int bitField0_;
      public static final int TYPE_FIELD_NUMBER = 1;
      private int type_;
      public static final int SCOPE_FIELD_NUMBER = 2;
      private MysqlxNotice.Frame.Scope scope_;
      public static final int PAYLOAD_FIELD_NUMBER = 3;
      private ByteString payload_;
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private Frame(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private Frame(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static MysqlxNotice.Frame getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxNotice.Frame getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private Frame(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                     this.type_ = input.readUInt32();
                     break;
                  case 16:
                     int rawValue = input.readEnum();
                     MysqlxNotice.Frame.Scope value = MysqlxNotice.Frame.Scope.valueOf(rawValue);
                     if (value == null) {
                        unknownFields.mergeVarintField(2, rawValue);
                     } else {
                        this.bitField0_ |= 2;
                        this.scope_ = value;
                     }
                     break;
                  case 26:
                     this.bitField0_ |= 4;
                     this.payload_ = input.readBytes();
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
         return MysqlxNotice.internal_static_Mysqlx_Notice_Frame_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxNotice.internal_static_Mysqlx_Notice_Frame_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxNotice.Frame.class, MysqlxNotice.Frame.Builder.class);
      }

      public Parser<MysqlxNotice.Frame> getParserForType() {
         return PARSER;
      }

      @Override
      public boolean hasType() {
         return (this.bitField0_ & 1) == 1;
      }

      @Override
      public int getType() {
         return this.type_;
      }

      @Override
      public boolean hasScope() {
         return (this.bitField0_ & 2) == 2;
      }

      @Override
      public MysqlxNotice.Frame.Scope getScope() {
         return this.scope_;
      }

      @Override
      public boolean hasPayload() {
         return (this.bitField0_ & 4) == 4;
      }

      @Override
      public ByteString getPayload() {
         return this.payload_;
      }

      private void initFields() {
         this.type_ = 0;
         this.scope_ = MysqlxNotice.Frame.Scope.GLOBAL;
         this.payload_ = ByteString.EMPTY;
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
            output.writeUInt32(1, this.type_);
         }

         if ((this.bitField0_ & 2) == 2) {
            output.writeEnum(2, this.scope_.getNumber());
         }

         if ((this.bitField0_ & 4) == 4) {
            output.writeBytes(3, this.payload_);
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
               size += CodedOutputStream.computeUInt32Size(1, this.type_);
            }

            if ((this.bitField0_ & 2) == 2) {
               size += CodedOutputStream.computeEnumSize(2, this.scope_.getNumber());
            }

            if ((this.bitField0_ & 4) == 4) {
               size += CodedOutputStream.computeBytesSize(3, this.payload_);
            }

            size += this.getUnknownFields().getSerializedSize();
            this.memoizedSerializedSize = size;
            return size;
         }
      }

      protected Object writeReplace() throws ObjectStreamException {
         return super.writeReplace();
      }

      public static MysqlxNotice.Frame parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxNotice.Frame)PARSER.parseFrom(data);
      }

      public static MysqlxNotice.Frame parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxNotice.Frame)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxNotice.Frame parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxNotice.Frame)PARSER.parseFrom(data);
      }

      public static MysqlxNotice.Frame parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxNotice.Frame)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxNotice.Frame parseFrom(InputStream input) throws IOException {
         return (MysqlxNotice.Frame)PARSER.parseFrom(input);
      }

      public static MysqlxNotice.Frame parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxNotice.Frame)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxNotice.Frame parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxNotice.Frame)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxNotice.Frame parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxNotice.Frame)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxNotice.Frame parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxNotice.Frame)PARSER.parseFrom(input);
      }

      public static MysqlxNotice.Frame parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxNotice.Frame)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxNotice.Frame.Builder newBuilder() {
         return MysqlxNotice.Frame.Builder.create();
      }

      public MysqlxNotice.Frame.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxNotice.Frame.Builder newBuilder(MysqlxNotice.Frame prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxNotice.Frame.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxNotice.Frame.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxNotice.Frame.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxNotice.Frame.Builder>
         implements MysqlxNotice.FrameOrBuilder {
         private int bitField0_;
         private int type_;
         private MysqlxNotice.Frame.Scope scope_ = MysqlxNotice.Frame.Scope.GLOBAL;
         private ByteString payload_ = ByteString.EMPTY;

         public static final Descriptor getDescriptor() {
            return MysqlxNotice.internal_static_Mysqlx_Notice_Frame_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxNotice.internal_static_Mysqlx_Notice_Frame_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxNotice.Frame.class, MysqlxNotice.Frame.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxNotice.Frame.alwaysUseFieldBuilders) {
            }
         }

         private static MysqlxNotice.Frame.Builder create() {
            return new MysqlxNotice.Frame.Builder();
         }

         public MysqlxNotice.Frame.Builder clear() {
            super.clear();
            this.type_ = 0;
            this.bitField0_ &= -2;
            this.scope_ = MysqlxNotice.Frame.Scope.GLOBAL;
            this.bitField0_ &= -3;
            this.payload_ = ByteString.EMPTY;
            this.bitField0_ &= -5;
            return this;
         }

         public MysqlxNotice.Frame.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxNotice.internal_static_Mysqlx_Notice_Frame_descriptor;
         }

         public MysqlxNotice.Frame getDefaultInstanceForType() {
            return MysqlxNotice.Frame.getDefaultInstance();
         }

         public MysqlxNotice.Frame build() {
            MysqlxNotice.Frame result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxNotice.Frame buildPartial() {
            MysqlxNotice.Frame result = new MysqlxNotice.Frame(this);
            int from_bitField0_ = this.bitField0_;
            int to_bitField0_ = 0;
            if ((from_bitField0_ & 1) == 1) {
               to_bitField0_ |= 1;
            }

            result.type_ = this.type_;
            if ((from_bitField0_ & 2) == 2) {
               to_bitField0_ |= 2;
            }

            result.scope_ = this.scope_;
            if ((from_bitField0_ & 4) == 4) {
               to_bitField0_ |= 4;
            }

            result.payload_ = this.payload_;
            result.bitField0_ = to_bitField0_;
            this.onBuilt();
            return result;
         }

         public MysqlxNotice.Frame.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxNotice.Frame) {
               return this.mergeFrom((MysqlxNotice.Frame)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxNotice.Frame.Builder mergeFrom(MysqlxNotice.Frame other) {
            if (other == MysqlxNotice.Frame.getDefaultInstance()) {
               return this;
            } else {
               if (other.hasType()) {
                  this.setType(other.getType());
               }

               if (other.hasScope()) {
                  this.setScope(other.getScope());
               }

               if (other.hasPayload()) {
                  this.setPayload(other.getPayload());
               }

               this.mergeUnknownFields(other.getUnknownFields());
               return this;
            }
         }

         public final boolean isInitialized() {
            return this.hasType();
         }

         public MysqlxNotice.Frame.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxNotice.Frame parsedMessage = null;

            try {
               parsedMessage = (MysqlxNotice.Frame)MysqlxNotice.Frame.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxNotice.Frame)var8.getUnfinishedMessage();
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
         public int getType() {
            return this.type_;
         }

         public MysqlxNotice.Frame.Builder setType(int value) {
            this.bitField0_ |= 1;
            this.type_ = value;
            this.onChanged();
            return this;
         }

         public MysqlxNotice.Frame.Builder clearType() {
            this.bitField0_ &= -2;
            this.type_ = 0;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasScope() {
            return (this.bitField0_ & 2) == 2;
         }

         @Override
         public MysqlxNotice.Frame.Scope getScope() {
            return this.scope_;
         }

         public MysqlxNotice.Frame.Builder setScope(MysqlxNotice.Frame.Scope value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 2;
               this.scope_ = value;
               this.onChanged();
               return this;
            }
         }

         public MysqlxNotice.Frame.Builder clearScope() {
            this.bitField0_ &= -3;
            this.scope_ = MysqlxNotice.Frame.Scope.GLOBAL;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasPayload() {
            return (this.bitField0_ & 4) == 4;
         }

         @Override
         public ByteString getPayload() {
            return this.payload_;
         }

         public MysqlxNotice.Frame.Builder setPayload(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 4;
               this.payload_ = value;
               this.onChanged();
               return this;
            }
         }

         public MysqlxNotice.Frame.Builder clearPayload() {
            this.bitField0_ &= -5;
            this.payload_ = MysqlxNotice.Frame.getDefaultInstance().getPayload();
            this.onChanged();
            return this;
         }
      }

      public static enum Scope implements ProtocolMessageEnum {
         GLOBAL(0, 1),
         LOCAL(1, 2);

         public static final int GLOBAL_VALUE = 1;
         public static final int LOCAL_VALUE = 2;
         private static EnumLiteMap<MysqlxNotice.Frame.Scope> internalValueMap = new EnumLiteMap<MysqlxNotice.Frame.Scope>() {
            public MysqlxNotice.Frame.Scope findValueByNumber(int number) {
               return MysqlxNotice.Frame.Scope.valueOf(number);
            }
         };
         private static final MysqlxNotice.Frame.Scope[] VALUES = values();
         private final int index;
         private final int value;

         public final int getNumber() {
            return this.value;
         }

         public static MysqlxNotice.Frame.Scope valueOf(int value) {
            switch(value) {
               case 1:
                  return GLOBAL;
               case 2:
                  return LOCAL;
               default:
                  return null;
            }
         }

         public static EnumLiteMap<MysqlxNotice.Frame.Scope> internalGetValueMap() {
            return internalValueMap;
         }

         public final EnumValueDescriptor getValueDescriptor() {
            return (EnumValueDescriptor)getDescriptor().getValues().get(this.index);
         }

         public final EnumDescriptor getDescriptorForType() {
            return getDescriptor();
         }

         public static final EnumDescriptor getDescriptor() {
            return (EnumDescriptor)MysqlxNotice.Frame.getDescriptor().getEnumTypes().get(0);
         }

         public static MysqlxNotice.Frame.Scope valueOf(EnumValueDescriptor desc) {
            if (desc.getType() != getDescriptor()) {
               throw new IllegalArgumentException("EnumValueDescriptor is not for this type.");
            } else {
               return VALUES[desc.getIndex()];
            }
         }

         private Scope(int index, int value) {
            this.index = index;
            this.value = value;
         }
      }

      public static enum Type implements ProtocolMessageEnum {
         WARNING(0, 1),
         SESSION_VARIABLE_CHANGED(1, 2),
         SESSION_STATE_CHANGED(2, 3);

         public static final int WARNING_VALUE = 1;
         public static final int SESSION_VARIABLE_CHANGED_VALUE = 2;
         public static final int SESSION_STATE_CHANGED_VALUE = 3;
         private static EnumLiteMap<MysqlxNotice.Frame.Type> internalValueMap = new EnumLiteMap<MysqlxNotice.Frame.Type>() {
            public MysqlxNotice.Frame.Type findValueByNumber(int number) {
               return MysqlxNotice.Frame.Type.valueOf(number);
            }
         };
         private static final MysqlxNotice.Frame.Type[] VALUES = values();
         private final int index;
         private final int value;

         public final int getNumber() {
            return this.value;
         }

         public static MysqlxNotice.Frame.Type valueOf(int value) {
            switch(value) {
               case 1:
                  return WARNING;
               case 2:
                  return SESSION_VARIABLE_CHANGED;
               case 3:
                  return SESSION_STATE_CHANGED;
               default:
                  return null;
            }
         }

         public static EnumLiteMap<MysqlxNotice.Frame.Type> internalGetValueMap() {
            return internalValueMap;
         }

         public final EnumValueDescriptor getValueDescriptor() {
            return (EnumValueDescriptor)getDescriptor().getValues().get(this.index);
         }

         public final EnumDescriptor getDescriptorForType() {
            return getDescriptor();
         }

         public static final EnumDescriptor getDescriptor() {
            return (EnumDescriptor)MysqlxNotice.Frame.getDescriptor().getEnumTypes().get(1);
         }

         public static MysqlxNotice.Frame.Type valueOf(EnumValueDescriptor desc) {
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

   public interface FrameOrBuilder extends MessageOrBuilder {
      boolean hasType();

      int getType();

      boolean hasScope();

      MysqlxNotice.Frame.Scope getScope();

      boolean hasPayload();

      ByteString getPayload();
   }

   public static final class SessionStateChanged extends GeneratedMessage implements MysqlxNotice.SessionStateChangedOrBuilder {
      private static final MysqlxNotice.SessionStateChanged defaultInstance = new MysqlxNotice.SessionStateChanged(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxNotice.SessionStateChanged> PARSER = new AbstractParser<MysqlxNotice.SessionStateChanged>() {
         public MysqlxNotice.SessionStateChanged parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxNotice.SessionStateChanged(input, extensionRegistry);
         }
      };
      private int bitField0_;
      public static final int PARAM_FIELD_NUMBER = 1;
      private MysqlxNotice.SessionStateChanged.Parameter param_;
      public static final int VALUE_FIELD_NUMBER = 2;
      private List<MysqlxDatatypes.Scalar> value_;
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private SessionStateChanged(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private SessionStateChanged(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static MysqlxNotice.SessionStateChanged getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxNotice.SessionStateChanged getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private SessionStateChanged(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                     MysqlxNotice.SessionStateChanged.Parameter value = MysqlxNotice.SessionStateChanged.Parameter.valueOf(rawValue);
                     if (value == null) {
                        unknownFields.mergeVarintField(1, rawValue);
                     } else {
                        this.bitField0_ |= 1;
                        this.param_ = value;
                     }
                     break;
                  case 18:
                     if ((mutable_bitField0_ & 2) != 2) {
                        this.value_ = new ArrayList<>();
                        mutable_bitField0_ |= 2;
                     }

                     this.value_.add(input.readMessage(MysqlxDatatypes.Scalar.PARSER, extensionRegistry));
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
               this.value_ = Collections.unmodifiableList(this.value_);
            }

            this.unknownFields = unknownFields.build();
            this.makeExtensionsImmutable();
         }
      }

      public static final Descriptor getDescriptor() {
         return MysqlxNotice.internal_static_Mysqlx_Notice_SessionStateChanged_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxNotice.internal_static_Mysqlx_Notice_SessionStateChanged_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxNotice.SessionStateChanged.class, MysqlxNotice.SessionStateChanged.Builder.class);
      }

      public Parser<MysqlxNotice.SessionStateChanged> getParserForType() {
         return PARSER;
      }

      @Override
      public boolean hasParam() {
         return (this.bitField0_ & 1) == 1;
      }

      @Override
      public MysqlxNotice.SessionStateChanged.Parameter getParam() {
         return this.param_;
      }

      @Override
      public List<MysqlxDatatypes.Scalar> getValueList() {
         return this.value_;
      }

      @Override
      public List<? extends MysqlxDatatypes.ScalarOrBuilder> getValueOrBuilderList() {
         return this.value_;
      }

      @Override
      public int getValueCount() {
         return this.value_.size();
      }

      @Override
      public MysqlxDatatypes.Scalar getValue(int index) {
         return this.value_.get(index);
      }

      @Override
      public MysqlxDatatypes.ScalarOrBuilder getValueOrBuilder(int index) {
         return this.value_.get(index);
      }

      private void initFields() {
         this.param_ = MysqlxNotice.SessionStateChanged.Parameter.CURRENT_SCHEMA;
         this.value_ = Collections.emptyList();
      }

      public final boolean isInitialized() {
         byte isInitialized = this.memoizedIsInitialized;
         if (isInitialized == 1) {
            return true;
         } else if (isInitialized == 0) {
            return false;
         } else if (!this.hasParam()) {
            this.memoizedIsInitialized = 0;
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
         if ((this.bitField0_ & 1) == 1) {
            output.writeEnum(1, this.param_.getNumber());
         }

         for(int i = 0; i < this.value_.size(); ++i) {
            output.writeMessage(2, (MessageLite)this.value_.get(i));
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
               size += CodedOutputStream.computeEnumSize(1, this.param_.getNumber());
            }

            for(int i = 0; i < this.value_.size(); ++i) {
               size += CodedOutputStream.computeMessageSize(2, (MessageLite)this.value_.get(i));
            }

            size += this.getUnknownFields().getSerializedSize();
            this.memoizedSerializedSize = size;
            return size;
         }
      }

      protected Object writeReplace() throws ObjectStreamException {
         return super.writeReplace();
      }

      public static MysqlxNotice.SessionStateChanged parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxNotice.SessionStateChanged)PARSER.parseFrom(data);
      }

      public static MysqlxNotice.SessionStateChanged parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxNotice.SessionStateChanged)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxNotice.SessionStateChanged parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxNotice.SessionStateChanged)PARSER.parseFrom(data);
      }

      public static MysqlxNotice.SessionStateChanged parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxNotice.SessionStateChanged)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxNotice.SessionStateChanged parseFrom(InputStream input) throws IOException {
         return (MysqlxNotice.SessionStateChanged)PARSER.parseFrom(input);
      }

      public static MysqlxNotice.SessionStateChanged parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxNotice.SessionStateChanged)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxNotice.SessionStateChanged parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxNotice.SessionStateChanged)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxNotice.SessionStateChanged parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxNotice.SessionStateChanged)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxNotice.SessionStateChanged parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxNotice.SessionStateChanged)PARSER.parseFrom(input);
      }

      public static MysqlxNotice.SessionStateChanged parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxNotice.SessionStateChanged)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxNotice.SessionStateChanged.Builder newBuilder() {
         return MysqlxNotice.SessionStateChanged.Builder.create();
      }

      public MysqlxNotice.SessionStateChanged.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxNotice.SessionStateChanged.Builder newBuilder(MysqlxNotice.SessionStateChanged prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxNotice.SessionStateChanged.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxNotice.SessionStateChanged.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxNotice.SessionStateChanged.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxNotice.SessionStateChanged.Builder>
         implements MysqlxNotice.SessionStateChangedOrBuilder {
         private int bitField0_;
         private MysqlxNotice.SessionStateChanged.Parameter param_ = MysqlxNotice.SessionStateChanged.Parameter.CURRENT_SCHEMA;
         private List<MysqlxDatatypes.Scalar> value_ = Collections.emptyList();
         private RepeatedFieldBuilder<MysqlxDatatypes.Scalar, MysqlxDatatypes.Scalar.Builder, MysqlxDatatypes.ScalarOrBuilder> valueBuilder_;

         public static final Descriptor getDescriptor() {
            return MysqlxNotice.internal_static_Mysqlx_Notice_SessionStateChanged_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxNotice.internal_static_Mysqlx_Notice_SessionStateChanged_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxNotice.SessionStateChanged.class, MysqlxNotice.SessionStateChanged.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxNotice.SessionStateChanged.alwaysUseFieldBuilders) {
               this.getValueFieldBuilder();
            }
         }

         private static MysqlxNotice.SessionStateChanged.Builder create() {
            return new MysqlxNotice.SessionStateChanged.Builder();
         }

         public MysqlxNotice.SessionStateChanged.Builder clear() {
            super.clear();
            this.param_ = MysqlxNotice.SessionStateChanged.Parameter.CURRENT_SCHEMA;
            this.bitField0_ &= -2;
            if (this.valueBuilder_ == null) {
               this.value_ = Collections.emptyList();
               this.bitField0_ &= -3;
            } else {
               this.valueBuilder_.clear();
            }

            return this;
         }

         public MysqlxNotice.SessionStateChanged.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxNotice.internal_static_Mysqlx_Notice_SessionStateChanged_descriptor;
         }

         public MysqlxNotice.SessionStateChanged getDefaultInstanceForType() {
            return MysqlxNotice.SessionStateChanged.getDefaultInstance();
         }

         public MysqlxNotice.SessionStateChanged build() {
            MysqlxNotice.SessionStateChanged result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxNotice.SessionStateChanged buildPartial() {
            MysqlxNotice.SessionStateChanged result = new MysqlxNotice.SessionStateChanged(this);
            int from_bitField0_ = this.bitField0_;
            int to_bitField0_ = 0;
            if ((from_bitField0_ & 1) == 1) {
               to_bitField0_ |= 1;
            }

            result.param_ = this.param_;
            if (this.valueBuilder_ == null) {
               if ((this.bitField0_ & 2) == 2) {
                  this.value_ = Collections.unmodifiableList(this.value_);
                  this.bitField0_ &= -3;
               }

               result.value_ = this.value_;
            } else {
               result.value_ = this.valueBuilder_.build();
            }

            result.bitField0_ = to_bitField0_;
            this.onBuilt();
            return result;
         }

         public MysqlxNotice.SessionStateChanged.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxNotice.SessionStateChanged) {
               return this.mergeFrom((MysqlxNotice.SessionStateChanged)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxNotice.SessionStateChanged.Builder mergeFrom(MysqlxNotice.SessionStateChanged other) {
            if (other == MysqlxNotice.SessionStateChanged.getDefaultInstance()) {
               return this;
            } else {
               if (other.hasParam()) {
                  this.setParam(other.getParam());
               }

               if (this.valueBuilder_ == null) {
                  if (!other.value_.isEmpty()) {
                     if (this.value_.isEmpty()) {
                        this.value_ = other.value_;
                        this.bitField0_ &= -3;
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
                     this.bitField0_ &= -3;
                     this.valueBuilder_ = MysqlxNotice.SessionStateChanged.alwaysUseFieldBuilders ? this.getValueFieldBuilder() : null;
                  } else {
                     this.valueBuilder_.addAllMessages(other.value_);
                  }
               }

               this.mergeUnknownFields(other.getUnknownFields());
               return this;
            }
         }

         public final boolean isInitialized() {
            if (!this.hasParam()) {
               return false;
            } else {
               for(int i = 0; i < this.getValueCount(); ++i) {
                  if (!this.getValue(i).isInitialized()) {
                     return false;
                  }
               }

               return true;
            }
         }

         public MysqlxNotice.SessionStateChanged.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxNotice.SessionStateChanged parsedMessage = null;

            try {
               parsedMessage = (MysqlxNotice.SessionStateChanged)MysqlxNotice.SessionStateChanged.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxNotice.SessionStateChanged)var8.getUnfinishedMessage();
               throw var8;
            } finally {
               if (parsedMessage != null) {
                  this.mergeFrom(parsedMessage);
               }
            }

            return this;
         }

         @Override
         public boolean hasParam() {
            return (this.bitField0_ & 1) == 1;
         }

         @Override
         public MysqlxNotice.SessionStateChanged.Parameter getParam() {
            return this.param_;
         }

         public MysqlxNotice.SessionStateChanged.Builder setParam(MysqlxNotice.SessionStateChanged.Parameter value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 1;
               this.param_ = value;
               this.onChanged();
               return this;
            }
         }

         public MysqlxNotice.SessionStateChanged.Builder clearParam() {
            this.bitField0_ &= -2;
            this.param_ = MysqlxNotice.SessionStateChanged.Parameter.CURRENT_SCHEMA;
            this.onChanged();
            return this;
         }

         private void ensureValueIsMutable() {
            if ((this.bitField0_ & 2) != 2) {
               this.value_ = new ArrayList<>(this.value_);
               this.bitField0_ |= 2;
            }
         }

         @Override
         public List<MysqlxDatatypes.Scalar> getValueList() {
            return this.valueBuilder_ == null ? Collections.unmodifiableList(this.value_) : this.valueBuilder_.getMessageList();
         }

         @Override
         public int getValueCount() {
            return this.valueBuilder_ == null ? this.value_.size() : this.valueBuilder_.getCount();
         }

         @Override
         public MysqlxDatatypes.Scalar getValue(int index) {
            return this.valueBuilder_ == null ? this.value_.get(index) : (MysqlxDatatypes.Scalar)this.valueBuilder_.getMessage(index);
         }

         public MysqlxNotice.SessionStateChanged.Builder setValue(int index, MysqlxDatatypes.Scalar value) {
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

         public MysqlxNotice.SessionStateChanged.Builder setValue(int index, MysqlxDatatypes.Scalar.Builder builderForValue) {
            if (this.valueBuilder_ == null) {
               this.ensureValueIsMutable();
               this.value_.set(index, builderForValue.build());
               this.onChanged();
            } else {
               this.valueBuilder_.setMessage(index, builderForValue.build());
            }

            return this;
         }

         public MysqlxNotice.SessionStateChanged.Builder addValue(MysqlxDatatypes.Scalar value) {
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

         public MysqlxNotice.SessionStateChanged.Builder addValue(int index, MysqlxDatatypes.Scalar value) {
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

         public MysqlxNotice.SessionStateChanged.Builder addValue(MysqlxDatatypes.Scalar.Builder builderForValue) {
            if (this.valueBuilder_ == null) {
               this.ensureValueIsMutable();
               this.value_.add(builderForValue.build());
               this.onChanged();
            } else {
               this.valueBuilder_.addMessage(builderForValue.build());
            }

            return this;
         }

         public MysqlxNotice.SessionStateChanged.Builder addValue(int index, MysqlxDatatypes.Scalar.Builder builderForValue) {
            if (this.valueBuilder_ == null) {
               this.ensureValueIsMutable();
               this.value_.add(index, builderForValue.build());
               this.onChanged();
            } else {
               this.valueBuilder_.addMessage(index, builderForValue.build());
            }

            return this;
         }

         public MysqlxNotice.SessionStateChanged.Builder addAllValue(Iterable<? extends MysqlxDatatypes.Scalar> values) {
            if (this.valueBuilder_ == null) {
               this.ensureValueIsMutable();
               com.google.protobuf.AbstractMessageLite.Builder.addAll(values, this.value_);
               this.onChanged();
            } else {
               this.valueBuilder_.addAllMessages(values);
            }

            return this;
         }

         public MysqlxNotice.SessionStateChanged.Builder clearValue() {
            if (this.valueBuilder_ == null) {
               this.value_ = Collections.emptyList();
               this.bitField0_ &= -3;
               this.onChanged();
            } else {
               this.valueBuilder_.clear();
            }

            return this;
         }

         public MysqlxNotice.SessionStateChanged.Builder removeValue(int index) {
            if (this.valueBuilder_ == null) {
               this.ensureValueIsMutable();
               this.value_.remove(index);
               this.onChanged();
            } else {
               this.valueBuilder_.remove(index);
            }

            return this;
         }

         public MysqlxDatatypes.Scalar.Builder getValueBuilder(int index) {
            return (MysqlxDatatypes.Scalar.Builder)this.getValueFieldBuilder().getBuilder(index);
         }

         @Override
         public MysqlxDatatypes.ScalarOrBuilder getValueOrBuilder(int index) {
            return this.valueBuilder_ == null ? this.value_.get(index) : (MysqlxDatatypes.ScalarOrBuilder)this.valueBuilder_.getMessageOrBuilder(index);
         }

         @Override
         public List<? extends MysqlxDatatypes.ScalarOrBuilder> getValueOrBuilderList() {
            return this.valueBuilder_ != null ? this.valueBuilder_.getMessageOrBuilderList() : Collections.unmodifiableList(this.value_);
         }

         public MysqlxDatatypes.Scalar.Builder addValueBuilder() {
            return (MysqlxDatatypes.Scalar.Builder)this.getValueFieldBuilder().addBuilder(MysqlxDatatypes.Scalar.getDefaultInstance());
         }

         public MysqlxDatatypes.Scalar.Builder addValueBuilder(int index) {
            return (MysqlxDatatypes.Scalar.Builder)this.getValueFieldBuilder().addBuilder(index, MysqlxDatatypes.Scalar.getDefaultInstance());
         }

         public List<MysqlxDatatypes.Scalar.Builder> getValueBuilderList() {
            return this.getValueFieldBuilder().getBuilderList();
         }

         private RepeatedFieldBuilder<MysqlxDatatypes.Scalar, MysqlxDatatypes.Scalar.Builder, MysqlxDatatypes.ScalarOrBuilder> getValueFieldBuilder() {
            if (this.valueBuilder_ == null) {
               this.valueBuilder_ = new RepeatedFieldBuilder(this.value_, (this.bitField0_ & 2) == 2, this.getParentForChildren(), this.isClean());
               this.value_ = null;
            }

            return this.valueBuilder_;
         }
      }

      public static enum Parameter implements ProtocolMessageEnum {
         CURRENT_SCHEMA(0, 1),
         ACCOUNT_EXPIRED(1, 2),
         GENERATED_INSERT_ID(2, 3),
         ROWS_AFFECTED(3, 4),
         ROWS_FOUND(4, 5),
         ROWS_MATCHED(5, 6),
         TRX_COMMITTED(6, 7),
         TRX_ROLLEDBACK(7, 9),
         PRODUCED_MESSAGE(8, 10),
         CLIENT_ID_ASSIGNED(9, 11),
         GENERATED_DOCUMENT_IDS(10, 12);

         public static final int CURRENT_SCHEMA_VALUE = 1;
         public static final int ACCOUNT_EXPIRED_VALUE = 2;
         public static final int GENERATED_INSERT_ID_VALUE = 3;
         public static final int ROWS_AFFECTED_VALUE = 4;
         public static final int ROWS_FOUND_VALUE = 5;
         public static final int ROWS_MATCHED_VALUE = 6;
         public static final int TRX_COMMITTED_VALUE = 7;
         public static final int TRX_ROLLEDBACK_VALUE = 9;
         public static final int PRODUCED_MESSAGE_VALUE = 10;
         public static final int CLIENT_ID_ASSIGNED_VALUE = 11;
         public static final int GENERATED_DOCUMENT_IDS_VALUE = 12;
         private static EnumLiteMap<MysqlxNotice.SessionStateChanged.Parameter> internalValueMap = new EnumLiteMap<MysqlxNotice.SessionStateChanged.Parameter>(
            
         ) {
            public MysqlxNotice.SessionStateChanged.Parameter findValueByNumber(int number) {
               return MysqlxNotice.SessionStateChanged.Parameter.valueOf(number);
            }
         };
         private static final MysqlxNotice.SessionStateChanged.Parameter[] VALUES = values();
         private final int index;
         private final int value;

         public final int getNumber() {
            return this.value;
         }

         public static MysqlxNotice.SessionStateChanged.Parameter valueOf(int value) {
            switch(value) {
               case 1:
                  return CURRENT_SCHEMA;
               case 2:
                  return ACCOUNT_EXPIRED;
               case 3:
                  return GENERATED_INSERT_ID;
               case 4:
                  return ROWS_AFFECTED;
               case 5:
                  return ROWS_FOUND;
               case 6:
                  return ROWS_MATCHED;
               case 7:
                  return TRX_COMMITTED;
               case 8:
               default:
                  return null;
               case 9:
                  return TRX_ROLLEDBACK;
               case 10:
                  return PRODUCED_MESSAGE;
               case 11:
                  return CLIENT_ID_ASSIGNED;
               case 12:
                  return GENERATED_DOCUMENT_IDS;
            }
         }

         public static EnumLiteMap<MysqlxNotice.SessionStateChanged.Parameter> internalGetValueMap() {
            return internalValueMap;
         }

         public final EnumValueDescriptor getValueDescriptor() {
            return (EnumValueDescriptor)getDescriptor().getValues().get(this.index);
         }

         public final EnumDescriptor getDescriptorForType() {
            return getDescriptor();
         }

         public static final EnumDescriptor getDescriptor() {
            return (EnumDescriptor)MysqlxNotice.SessionStateChanged.getDescriptor().getEnumTypes().get(0);
         }

         public static MysqlxNotice.SessionStateChanged.Parameter valueOf(EnumValueDescriptor desc) {
            if (desc.getType() != getDescriptor()) {
               throw new IllegalArgumentException("EnumValueDescriptor is not for this type.");
            } else {
               return VALUES[desc.getIndex()];
            }
         }

         private Parameter(int index, int value) {
            this.index = index;
            this.value = value;
         }
      }
   }

   public interface SessionStateChangedOrBuilder extends MessageOrBuilder {
      boolean hasParam();

      MysqlxNotice.SessionStateChanged.Parameter getParam();

      List<MysqlxDatatypes.Scalar> getValueList();

      MysqlxDatatypes.Scalar getValue(int var1);

      int getValueCount();

      List<? extends MysqlxDatatypes.ScalarOrBuilder> getValueOrBuilderList();

      MysqlxDatatypes.ScalarOrBuilder getValueOrBuilder(int var1);
   }

   public static final class SessionVariableChanged extends GeneratedMessage implements MysqlxNotice.SessionVariableChangedOrBuilder {
      private static final MysqlxNotice.SessionVariableChanged defaultInstance = new MysqlxNotice.SessionVariableChanged(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxNotice.SessionVariableChanged> PARSER = new AbstractParser<MysqlxNotice.SessionVariableChanged>() {
         public MysqlxNotice.SessionVariableChanged parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxNotice.SessionVariableChanged(input, extensionRegistry);
         }
      };
      private int bitField0_;
      public static final int PARAM_FIELD_NUMBER = 1;
      private Object param_;
      public static final int VALUE_FIELD_NUMBER = 2;
      private MysqlxDatatypes.Scalar value_;
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private SessionVariableChanged(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private SessionVariableChanged(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static MysqlxNotice.SessionVariableChanged getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxNotice.SessionVariableChanged getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private SessionVariableChanged(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                     this.param_ = bs;
                     break;
                  case 18:
                     MysqlxDatatypes.Scalar.Builder subBuilder = null;
                     if ((this.bitField0_ & 2) == 2) {
                        subBuilder = this.value_.toBuilder();
                     }

                     this.value_ = (MysqlxDatatypes.Scalar)input.readMessage(MysqlxDatatypes.Scalar.PARSER, extensionRegistry);
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
         return MysqlxNotice.internal_static_Mysqlx_Notice_SessionVariableChanged_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxNotice.internal_static_Mysqlx_Notice_SessionVariableChanged_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxNotice.SessionVariableChanged.class, MysqlxNotice.SessionVariableChanged.Builder.class);
      }

      public Parser<MysqlxNotice.SessionVariableChanged> getParserForType() {
         return PARSER;
      }

      @Override
      public boolean hasParam() {
         return (this.bitField0_ & 1) == 1;
      }

      @Override
      public String getParam() {
         Object ref = this.param_;
         if (ref instanceof String) {
            return (String)ref;
         } else {
            ByteString bs = (ByteString)ref;
            String s = bs.toStringUtf8();
            if (bs.isValidUtf8()) {
               this.param_ = s;
            }

            return s;
         }
      }

      @Override
      public ByteString getParamBytes() {
         Object ref = this.param_;
         if (ref instanceof String) {
            ByteString b = ByteString.copyFromUtf8((String)ref);
            this.param_ = b;
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
      public MysqlxDatatypes.Scalar getValue() {
         return this.value_;
      }

      @Override
      public MysqlxDatatypes.ScalarOrBuilder getValueOrBuilder() {
         return this.value_;
      }

      private void initFields() {
         this.param_ = "";
         this.value_ = MysqlxDatatypes.Scalar.getDefaultInstance();
      }

      public final boolean isInitialized() {
         byte isInitialized = this.memoizedIsInitialized;
         if (isInitialized == 1) {
            return true;
         } else if (isInitialized == 0) {
            return false;
         } else if (!this.hasParam()) {
            this.memoizedIsInitialized = 0;
            return false;
         } else if (this.hasValue() && !this.getValue().isInitialized()) {
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
            output.writeBytes(1, this.getParamBytes());
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
               size += CodedOutputStream.computeBytesSize(1, this.getParamBytes());
            }

            if ((this.bitField0_ & 2) == 2) {
               size += CodedOutputStream.computeMessageSize(2, this.value_);
            }

            size += this.getUnknownFields().getSerializedSize();
            this.memoizedSerializedSize = size;
            return size;
         }
      }

      protected Object writeReplace() throws ObjectStreamException {
         return super.writeReplace();
      }

      public static MysqlxNotice.SessionVariableChanged parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxNotice.SessionVariableChanged)PARSER.parseFrom(data);
      }

      public static MysqlxNotice.SessionVariableChanged parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxNotice.SessionVariableChanged)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxNotice.SessionVariableChanged parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxNotice.SessionVariableChanged)PARSER.parseFrom(data);
      }

      public static MysqlxNotice.SessionVariableChanged parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxNotice.SessionVariableChanged)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxNotice.SessionVariableChanged parseFrom(InputStream input) throws IOException {
         return (MysqlxNotice.SessionVariableChanged)PARSER.parseFrom(input);
      }

      public static MysqlxNotice.SessionVariableChanged parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxNotice.SessionVariableChanged)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxNotice.SessionVariableChanged parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxNotice.SessionVariableChanged)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxNotice.SessionVariableChanged parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxNotice.SessionVariableChanged)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxNotice.SessionVariableChanged parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxNotice.SessionVariableChanged)PARSER.parseFrom(input);
      }

      public static MysqlxNotice.SessionVariableChanged parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxNotice.SessionVariableChanged)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxNotice.SessionVariableChanged.Builder newBuilder() {
         return MysqlxNotice.SessionVariableChanged.Builder.create();
      }

      public MysqlxNotice.SessionVariableChanged.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxNotice.SessionVariableChanged.Builder newBuilder(MysqlxNotice.SessionVariableChanged prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxNotice.SessionVariableChanged.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxNotice.SessionVariableChanged.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxNotice.SessionVariableChanged.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxNotice.SessionVariableChanged.Builder>
         implements MysqlxNotice.SessionVariableChangedOrBuilder {
         private int bitField0_;
         private Object param_ = "";
         private MysqlxDatatypes.Scalar value_ = MysqlxDatatypes.Scalar.getDefaultInstance();
         private SingleFieldBuilder<MysqlxDatatypes.Scalar, MysqlxDatatypes.Scalar.Builder, MysqlxDatatypes.ScalarOrBuilder> valueBuilder_;

         public static final Descriptor getDescriptor() {
            return MysqlxNotice.internal_static_Mysqlx_Notice_SessionVariableChanged_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxNotice.internal_static_Mysqlx_Notice_SessionVariableChanged_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxNotice.SessionVariableChanged.class, MysqlxNotice.SessionVariableChanged.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxNotice.SessionVariableChanged.alwaysUseFieldBuilders) {
               this.getValueFieldBuilder();
            }
         }

         private static MysqlxNotice.SessionVariableChanged.Builder create() {
            return new MysqlxNotice.SessionVariableChanged.Builder();
         }

         public MysqlxNotice.SessionVariableChanged.Builder clear() {
            super.clear();
            this.param_ = "";
            this.bitField0_ &= -2;
            if (this.valueBuilder_ == null) {
               this.value_ = MysqlxDatatypes.Scalar.getDefaultInstance();
            } else {
               this.valueBuilder_.clear();
            }

            this.bitField0_ &= -3;
            return this;
         }

         public MysqlxNotice.SessionVariableChanged.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxNotice.internal_static_Mysqlx_Notice_SessionVariableChanged_descriptor;
         }

         public MysqlxNotice.SessionVariableChanged getDefaultInstanceForType() {
            return MysqlxNotice.SessionVariableChanged.getDefaultInstance();
         }

         public MysqlxNotice.SessionVariableChanged build() {
            MysqlxNotice.SessionVariableChanged result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxNotice.SessionVariableChanged buildPartial() {
            MysqlxNotice.SessionVariableChanged result = new MysqlxNotice.SessionVariableChanged(this);
            int from_bitField0_ = this.bitField0_;
            int to_bitField0_ = 0;
            if ((from_bitField0_ & 1) == 1) {
               to_bitField0_ |= 1;
            }

            result.param_ = this.param_;
            if ((from_bitField0_ & 2) == 2) {
               to_bitField0_ |= 2;
            }

            if (this.valueBuilder_ == null) {
               result.value_ = this.value_;
            } else {
               result.value_ = (MysqlxDatatypes.Scalar)this.valueBuilder_.build();
            }

            result.bitField0_ = to_bitField0_;
            this.onBuilt();
            return result;
         }

         public MysqlxNotice.SessionVariableChanged.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxNotice.SessionVariableChanged) {
               return this.mergeFrom((MysqlxNotice.SessionVariableChanged)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxNotice.SessionVariableChanged.Builder mergeFrom(MysqlxNotice.SessionVariableChanged other) {
            if (other == MysqlxNotice.SessionVariableChanged.getDefaultInstance()) {
               return this;
            } else {
               if (other.hasParam()) {
                  this.bitField0_ |= 1;
                  this.param_ = other.param_;
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
            if (!this.hasParam()) {
               return false;
            } else {
               return !this.hasValue() || this.getValue().isInitialized();
            }
         }

         public MysqlxNotice.SessionVariableChanged.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxNotice.SessionVariableChanged parsedMessage = null;

            try {
               parsedMessage = (MysqlxNotice.SessionVariableChanged)MysqlxNotice.SessionVariableChanged.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxNotice.SessionVariableChanged)var8.getUnfinishedMessage();
               throw var8;
            } finally {
               if (parsedMessage != null) {
                  this.mergeFrom(parsedMessage);
               }
            }

            return this;
         }

         @Override
         public boolean hasParam() {
            return (this.bitField0_ & 1) == 1;
         }

         @Override
         public String getParam() {
            Object ref = this.param_;
            if (!(ref instanceof String)) {
               ByteString bs = (ByteString)ref;
               String s = bs.toStringUtf8();
               if (bs.isValidUtf8()) {
                  this.param_ = s;
               }

               return s;
            } else {
               return (String)ref;
            }
         }

         @Override
         public ByteString getParamBytes() {
            Object ref = this.param_;
            if (ref instanceof String) {
               ByteString b = ByteString.copyFromUtf8((String)ref);
               this.param_ = b;
               return b;
            } else {
               return (ByteString)ref;
            }
         }

         public MysqlxNotice.SessionVariableChanged.Builder setParam(String value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 1;
               this.param_ = value;
               this.onChanged();
               return this;
            }
         }

         public MysqlxNotice.SessionVariableChanged.Builder clearParam() {
            this.bitField0_ &= -2;
            this.param_ = MysqlxNotice.SessionVariableChanged.getDefaultInstance().getParam();
            this.onChanged();
            return this;
         }

         public MysqlxNotice.SessionVariableChanged.Builder setParamBytes(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 1;
               this.param_ = value;
               this.onChanged();
               return this;
            }
         }

         @Override
         public boolean hasValue() {
            return (this.bitField0_ & 2) == 2;
         }

         @Override
         public MysqlxDatatypes.Scalar getValue() {
            return this.valueBuilder_ == null ? this.value_ : (MysqlxDatatypes.Scalar)this.valueBuilder_.getMessage();
         }

         public MysqlxNotice.SessionVariableChanged.Builder setValue(MysqlxDatatypes.Scalar value) {
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

         public MysqlxNotice.SessionVariableChanged.Builder setValue(MysqlxDatatypes.Scalar.Builder builderForValue) {
            if (this.valueBuilder_ == null) {
               this.value_ = builderForValue.build();
               this.onChanged();
            } else {
               this.valueBuilder_.setMessage(builderForValue.build());
            }

            this.bitField0_ |= 2;
            return this;
         }

         public MysqlxNotice.SessionVariableChanged.Builder mergeValue(MysqlxDatatypes.Scalar value) {
            if (this.valueBuilder_ == null) {
               if ((this.bitField0_ & 2) == 2 && this.value_ != MysqlxDatatypes.Scalar.getDefaultInstance()) {
                  this.value_ = MysqlxDatatypes.Scalar.newBuilder(this.value_).mergeFrom(value).buildPartial();
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

         public MysqlxNotice.SessionVariableChanged.Builder clearValue() {
            if (this.valueBuilder_ == null) {
               this.value_ = MysqlxDatatypes.Scalar.getDefaultInstance();
               this.onChanged();
            } else {
               this.valueBuilder_.clear();
            }

            this.bitField0_ &= -3;
            return this;
         }

         public MysqlxDatatypes.Scalar.Builder getValueBuilder() {
            this.bitField0_ |= 2;
            this.onChanged();
            return (MysqlxDatatypes.Scalar.Builder)this.getValueFieldBuilder().getBuilder();
         }

         @Override
         public MysqlxDatatypes.ScalarOrBuilder getValueOrBuilder() {
            return (MysqlxDatatypes.ScalarOrBuilder)(this.valueBuilder_ != null
               ? (MysqlxDatatypes.ScalarOrBuilder)this.valueBuilder_.getMessageOrBuilder()
               : this.value_);
         }

         private SingleFieldBuilder<MysqlxDatatypes.Scalar, MysqlxDatatypes.Scalar.Builder, MysqlxDatatypes.ScalarOrBuilder> getValueFieldBuilder() {
            if (this.valueBuilder_ == null) {
               this.valueBuilder_ = new SingleFieldBuilder(this.getValue(), this.getParentForChildren(), this.isClean());
               this.value_ = null;
            }

            return this.valueBuilder_;
         }
      }
   }

   public interface SessionVariableChangedOrBuilder extends MessageOrBuilder {
      boolean hasParam();

      String getParam();

      ByteString getParamBytes();

      boolean hasValue();

      MysqlxDatatypes.Scalar getValue();

      MysqlxDatatypes.ScalarOrBuilder getValueOrBuilder();
   }

   public static final class Warning extends GeneratedMessage implements MysqlxNotice.WarningOrBuilder {
      private static final MysqlxNotice.Warning defaultInstance = new MysqlxNotice.Warning(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxNotice.Warning> PARSER = new AbstractParser<MysqlxNotice.Warning>() {
         public MysqlxNotice.Warning parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxNotice.Warning(input, extensionRegistry);
         }
      };
      private int bitField0_;
      public static final int LEVEL_FIELD_NUMBER = 1;
      private MysqlxNotice.Warning.Level level_;
      public static final int CODE_FIELD_NUMBER = 2;
      private int code_;
      public static final int MSG_FIELD_NUMBER = 3;
      private Object msg_;
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private Warning(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private Warning(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static MysqlxNotice.Warning getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxNotice.Warning getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private Warning(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                     MysqlxNotice.Warning.Level value = MysqlxNotice.Warning.Level.valueOf(rawValue);
                     if (value == null) {
                        unknownFields.mergeVarintField(1, rawValue);
                     } else {
                        this.bitField0_ |= 1;
                        this.level_ = value;
                     }
                     break;
                  case 16:
                     this.bitField0_ |= 2;
                     this.code_ = input.readUInt32();
                     break;
                  case 26:
                     ByteString bs = input.readBytes();
                     this.bitField0_ |= 4;
                     this.msg_ = bs;
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
         return MysqlxNotice.internal_static_Mysqlx_Notice_Warning_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxNotice.internal_static_Mysqlx_Notice_Warning_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxNotice.Warning.class, MysqlxNotice.Warning.Builder.class);
      }

      public Parser<MysqlxNotice.Warning> getParserForType() {
         return PARSER;
      }

      @Override
      public boolean hasLevel() {
         return (this.bitField0_ & 1) == 1;
      }

      @Override
      public MysqlxNotice.Warning.Level getLevel() {
         return this.level_;
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
      public boolean hasMsg() {
         return (this.bitField0_ & 4) == 4;
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
         this.level_ = MysqlxNotice.Warning.Level.WARNING;
         this.code_ = 0;
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
            output.writeEnum(1, this.level_.getNumber());
         }

         if ((this.bitField0_ & 2) == 2) {
            output.writeUInt32(2, this.code_);
         }

         if ((this.bitField0_ & 4) == 4) {
            output.writeBytes(3, this.getMsgBytes());
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
               size += CodedOutputStream.computeEnumSize(1, this.level_.getNumber());
            }

            if ((this.bitField0_ & 2) == 2) {
               size += CodedOutputStream.computeUInt32Size(2, this.code_);
            }

            if ((this.bitField0_ & 4) == 4) {
               size += CodedOutputStream.computeBytesSize(3, this.getMsgBytes());
            }

            size += this.getUnknownFields().getSerializedSize();
            this.memoizedSerializedSize = size;
            return size;
         }
      }

      protected Object writeReplace() throws ObjectStreamException {
         return super.writeReplace();
      }

      public static MysqlxNotice.Warning parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxNotice.Warning)PARSER.parseFrom(data);
      }

      public static MysqlxNotice.Warning parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxNotice.Warning)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxNotice.Warning parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxNotice.Warning)PARSER.parseFrom(data);
      }

      public static MysqlxNotice.Warning parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxNotice.Warning)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxNotice.Warning parseFrom(InputStream input) throws IOException {
         return (MysqlxNotice.Warning)PARSER.parseFrom(input);
      }

      public static MysqlxNotice.Warning parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxNotice.Warning)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxNotice.Warning parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxNotice.Warning)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxNotice.Warning parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxNotice.Warning)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxNotice.Warning parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxNotice.Warning)PARSER.parseFrom(input);
      }

      public static MysqlxNotice.Warning parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxNotice.Warning)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxNotice.Warning.Builder newBuilder() {
         return MysqlxNotice.Warning.Builder.create();
      }

      public MysqlxNotice.Warning.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxNotice.Warning.Builder newBuilder(MysqlxNotice.Warning prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxNotice.Warning.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxNotice.Warning.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxNotice.Warning.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxNotice.Warning.Builder>
         implements MysqlxNotice.WarningOrBuilder {
         private int bitField0_;
         private MysqlxNotice.Warning.Level level_ = MysqlxNotice.Warning.Level.WARNING;
         private int code_;
         private Object msg_ = "";

         public static final Descriptor getDescriptor() {
            return MysqlxNotice.internal_static_Mysqlx_Notice_Warning_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxNotice.internal_static_Mysqlx_Notice_Warning_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxNotice.Warning.class, MysqlxNotice.Warning.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxNotice.Warning.alwaysUseFieldBuilders) {
            }
         }

         private static MysqlxNotice.Warning.Builder create() {
            return new MysqlxNotice.Warning.Builder();
         }

         public MysqlxNotice.Warning.Builder clear() {
            super.clear();
            this.level_ = MysqlxNotice.Warning.Level.WARNING;
            this.bitField0_ &= -2;
            this.code_ = 0;
            this.bitField0_ &= -3;
            this.msg_ = "";
            this.bitField0_ &= -5;
            return this;
         }

         public MysqlxNotice.Warning.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxNotice.internal_static_Mysqlx_Notice_Warning_descriptor;
         }

         public MysqlxNotice.Warning getDefaultInstanceForType() {
            return MysqlxNotice.Warning.getDefaultInstance();
         }

         public MysqlxNotice.Warning build() {
            MysqlxNotice.Warning result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxNotice.Warning buildPartial() {
            MysqlxNotice.Warning result = new MysqlxNotice.Warning(this);
            int from_bitField0_ = this.bitField0_;
            int to_bitField0_ = 0;
            if ((from_bitField0_ & 1) == 1) {
               to_bitField0_ |= 1;
            }

            result.level_ = this.level_;
            if ((from_bitField0_ & 2) == 2) {
               to_bitField0_ |= 2;
            }

            result.code_ = this.code_;
            if ((from_bitField0_ & 4) == 4) {
               to_bitField0_ |= 4;
            }

            result.msg_ = this.msg_;
            result.bitField0_ = to_bitField0_;
            this.onBuilt();
            return result;
         }

         public MysqlxNotice.Warning.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxNotice.Warning) {
               return this.mergeFrom((MysqlxNotice.Warning)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxNotice.Warning.Builder mergeFrom(MysqlxNotice.Warning other) {
            if (other == MysqlxNotice.Warning.getDefaultInstance()) {
               return this;
            } else {
               if (other.hasLevel()) {
                  this.setLevel(other.getLevel());
               }

               if (other.hasCode()) {
                  this.setCode(other.getCode());
               }

               if (other.hasMsg()) {
                  this.bitField0_ |= 4;
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
            } else {
               return this.hasMsg();
            }
         }

         public MysqlxNotice.Warning.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxNotice.Warning parsedMessage = null;

            try {
               parsedMessage = (MysqlxNotice.Warning)MysqlxNotice.Warning.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxNotice.Warning)var8.getUnfinishedMessage();
               throw var8;
            } finally {
               if (parsedMessage != null) {
                  this.mergeFrom(parsedMessage);
               }
            }

            return this;
         }

         @Override
         public boolean hasLevel() {
            return (this.bitField0_ & 1) == 1;
         }

         @Override
         public MysqlxNotice.Warning.Level getLevel() {
            return this.level_;
         }

         public MysqlxNotice.Warning.Builder setLevel(MysqlxNotice.Warning.Level value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 1;
               this.level_ = value;
               this.onChanged();
               return this;
            }
         }

         public MysqlxNotice.Warning.Builder clearLevel() {
            this.bitField0_ &= -2;
            this.level_ = MysqlxNotice.Warning.Level.WARNING;
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

         public MysqlxNotice.Warning.Builder setCode(int value) {
            this.bitField0_ |= 2;
            this.code_ = value;
            this.onChanged();
            return this;
         }

         public MysqlxNotice.Warning.Builder clearCode() {
            this.bitField0_ &= -3;
            this.code_ = 0;
            this.onChanged();
            return this;
         }

         @Override
         public boolean hasMsg() {
            return (this.bitField0_ & 4) == 4;
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

         public MysqlxNotice.Warning.Builder setMsg(String value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 4;
               this.msg_ = value;
               this.onChanged();
               return this;
            }
         }

         public MysqlxNotice.Warning.Builder clearMsg() {
            this.bitField0_ &= -5;
            this.msg_ = MysqlxNotice.Warning.getDefaultInstance().getMsg();
            this.onChanged();
            return this;
         }

         public MysqlxNotice.Warning.Builder setMsgBytes(ByteString value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 4;
               this.msg_ = value;
               this.onChanged();
               return this;
            }
         }
      }

      public static enum Level implements ProtocolMessageEnum {
         NOTE(0, 1),
         WARNING(1, 2),
         ERROR(2, 3);

         public static final int NOTE_VALUE = 1;
         public static final int WARNING_VALUE = 2;
         public static final int ERROR_VALUE = 3;
         private static EnumLiteMap<MysqlxNotice.Warning.Level> internalValueMap = new EnumLiteMap<MysqlxNotice.Warning.Level>() {
            public MysqlxNotice.Warning.Level findValueByNumber(int number) {
               return MysqlxNotice.Warning.Level.valueOf(number);
            }
         };
         private static final MysqlxNotice.Warning.Level[] VALUES = values();
         private final int index;
         private final int value;

         public final int getNumber() {
            return this.value;
         }

         public static MysqlxNotice.Warning.Level valueOf(int value) {
            switch(value) {
               case 1:
                  return NOTE;
               case 2:
                  return WARNING;
               case 3:
                  return ERROR;
               default:
                  return null;
            }
         }

         public static EnumLiteMap<MysqlxNotice.Warning.Level> internalGetValueMap() {
            return internalValueMap;
         }

         public final EnumValueDescriptor getValueDescriptor() {
            return (EnumValueDescriptor)getDescriptor().getValues().get(this.index);
         }

         public final EnumDescriptor getDescriptorForType() {
            return getDescriptor();
         }

         public static final EnumDescriptor getDescriptor() {
            return (EnumDescriptor)MysqlxNotice.Warning.getDescriptor().getEnumTypes().get(0);
         }

         public static MysqlxNotice.Warning.Level valueOf(EnumValueDescriptor desc) {
            if (desc.getType() != getDescriptor()) {
               throw new IllegalArgumentException("EnumValueDescriptor is not for this type.");
            } else {
               return VALUES[desc.getIndex()];
            }
         }

         private Level(int index, int value) {
            this.index = index;
            this.value = value;
         }
      }
   }

   public interface WarningOrBuilder extends MessageOrBuilder {
      boolean hasLevel();

      MysqlxNotice.Warning.Level getLevel();

      boolean hasCode();

      int getCode();

      boolean hasMsg();

      String getMsg();

      ByteString getMsgBytes();
   }
}
