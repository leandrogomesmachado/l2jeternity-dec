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
import com.google.protobuf.RepeatedFieldBuilder;
import com.google.protobuf.SingleFieldBuilder;
import com.google.protobuf.UnknownFieldSet;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner;
import com.google.protobuf.GeneratedMessage.BuilderParent;
import com.google.protobuf.GeneratedMessage.FieldAccessorTable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MysqlxConnection {
   private static final Descriptor internal_static_Mysqlx_Connection_Capability_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(0);
   private static FieldAccessorTable internal_static_Mysqlx_Connection_Capability_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Connection_Capability_descriptor, new String[]{"Name", "Value"}
   );
   private static final Descriptor internal_static_Mysqlx_Connection_Capabilities_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(1);
   private static FieldAccessorTable internal_static_Mysqlx_Connection_Capabilities_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Connection_Capabilities_descriptor, new String[]{"Capabilities"}
   );
   private static final Descriptor internal_static_Mysqlx_Connection_CapabilitiesGet_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(2);
   private static FieldAccessorTable internal_static_Mysqlx_Connection_CapabilitiesGet_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Connection_CapabilitiesGet_descriptor, new String[0]
   );
   private static final Descriptor internal_static_Mysqlx_Connection_CapabilitiesSet_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(3);
   private static FieldAccessorTable internal_static_Mysqlx_Connection_CapabilitiesSet_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Connection_CapabilitiesSet_descriptor, new String[]{"Capabilities"}
   );
   private static final Descriptor internal_static_Mysqlx_Connection_Close_descriptor = (Descriptor)getDescriptor().getMessageTypes().get(4);
   private static FieldAccessorTable internal_static_Mysqlx_Connection_Close_fieldAccessorTable = new FieldAccessorTable(
      internal_static_Mysqlx_Connection_Close_descriptor, new String[0]
   );
   private static FileDescriptor descriptor;

   private MysqlxConnection() {
   }

   public static void registerAllExtensions(ExtensionRegistry registry) {
   }

   public static FileDescriptor getDescriptor() {
      return descriptor;
   }

   static {
      String[] descriptorData = new String[]{
         "\n\u0017mysqlx_connection.proto\u0012\u0011Mysqlx.Connection\u001a\u0016mysqlx_datatypes.proto\u001a\fmysqlx.proto\"@\n\nCapability\u0012\f\n\u0004name\u0018\u0001 \u0002(\t\u0012$\n\u0005value\u0018\u0002 \u0002(\u000b2\u0015.Mysqlx.Datatypes.Any\"I\n\fCapabilities\u00123\n\fcapabilities\u0018\u0001 \u0003(\u000b2\u001d.Mysqlx.Connection.Capability:\u0004\u0090ê0\u0002\"\u0017\n\u000fCapabilitiesGet:\u0004\u0088ê0\u0001\"N\n\u000fCapabilitiesSet\u00125\n\fcapabilities\u0018\u0001 \u0002(\u000b2\u001f.Mysqlx.Connection.Capabilities:\u0004\u0088ê0\u0002\"\r\n\u0005Close:\u0004\u0088ê0\u0003B\u0019\n\u0017com.mysql.cj.x.protobuf"
      };
      InternalDescriptorAssigner assigner = new InternalDescriptorAssigner() {
         public ExtensionRegistry assignDescriptors(FileDescriptor root) {
            MysqlxConnection.descriptor = root;
            return null;
         }
      };
      FileDescriptor.internalBuildGeneratedFileFrom(descriptorData, new FileDescriptor[]{MysqlxDatatypes.getDescriptor(), Mysqlx.getDescriptor()}, assigner);
      ExtensionRegistry registry = ExtensionRegistry.newInstance();
      registry.add(Mysqlx.serverMessageId);
      registry.add(Mysqlx.clientMessageId);
      registry.add(Mysqlx.clientMessageId);
      registry.add(Mysqlx.clientMessageId);
      FileDescriptor.internalUpdateFileDescriptor(descriptor, registry);
      MysqlxDatatypes.getDescriptor();
      Mysqlx.getDescriptor();
   }

   public static final class Capabilities extends GeneratedMessage implements MysqlxConnection.CapabilitiesOrBuilder {
      private static final MysqlxConnection.Capabilities defaultInstance = new MysqlxConnection.Capabilities(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxConnection.Capabilities> PARSER = new AbstractParser<MysqlxConnection.Capabilities>() {
         public MysqlxConnection.Capabilities parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxConnection.Capabilities(input, extensionRegistry);
         }
      };
      public static final int CAPABILITIES_FIELD_NUMBER = 1;
      private List<MysqlxConnection.Capability> capabilities_;
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private Capabilities(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private Capabilities(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static MysqlxConnection.Capabilities getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxConnection.Capabilities getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private Capabilities(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                        this.capabilities_ = new ArrayList<>();
                        mutable_bitField0_ |= 1;
                     }

                     this.capabilities_.add(input.readMessage(MysqlxConnection.Capability.PARSER, extensionRegistry));
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
               this.capabilities_ = Collections.unmodifiableList(this.capabilities_);
            }

            this.unknownFields = unknownFields.build();
            this.makeExtensionsImmutable();
         }
      }

      public static final Descriptor getDescriptor() {
         return MysqlxConnection.internal_static_Mysqlx_Connection_Capabilities_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxConnection.internal_static_Mysqlx_Connection_Capabilities_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxConnection.Capabilities.class, MysqlxConnection.Capabilities.Builder.class);
      }

      public Parser<MysqlxConnection.Capabilities> getParserForType() {
         return PARSER;
      }

      @Override
      public List<MysqlxConnection.Capability> getCapabilitiesList() {
         return this.capabilities_;
      }

      @Override
      public List<? extends MysqlxConnection.CapabilityOrBuilder> getCapabilitiesOrBuilderList() {
         return this.capabilities_;
      }

      @Override
      public int getCapabilitiesCount() {
         return this.capabilities_.size();
      }

      @Override
      public MysqlxConnection.Capability getCapabilities(int index) {
         return this.capabilities_.get(index);
      }

      @Override
      public MysqlxConnection.CapabilityOrBuilder getCapabilitiesOrBuilder(int index) {
         return this.capabilities_.get(index);
      }

      private void initFields() {
         this.capabilities_ = Collections.emptyList();
      }

      public final boolean isInitialized() {
         byte isInitialized = this.memoizedIsInitialized;
         if (isInitialized == 1) {
            return true;
         } else if (isInitialized == 0) {
            return false;
         } else {
            for(int i = 0; i < this.getCapabilitiesCount(); ++i) {
               if (!this.getCapabilities(i).isInitialized()) {
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

         for(int i = 0; i < this.capabilities_.size(); ++i) {
            output.writeMessage(1, (MessageLite)this.capabilities_.get(i));
         }

         this.getUnknownFields().writeTo(output);
      }

      public int getSerializedSize() {
         int size = this.memoizedSerializedSize;
         if (size != -1) {
            return size;
         } else {
            size = 0;

            for(int i = 0; i < this.capabilities_.size(); ++i) {
               size += CodedOutputStream.computeMessageSize(1, (MessageLite)this.capabilities_.get(i));
            }

            size += this.getUnknownFields().getSerializedSize();
            this.memoizedSerializedSize = size;
            return size;
         }
      }

      protected Object writeReplace() throws ObjectStreamException {
         return super.writeReplace();
      }

      public static MysqlxConnection.Capabilities parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxConnection.Capabilities)PARSER.parseFrom(data);
      }

      public static MysqlxConnection.Capabilities parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxConnection.Capabilities)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxConnection.Capabilities parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxConnection.Capabilities)PARSER.parseFrom(data);
      }

      public static MysqlxConnection.Capabilities parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxConnection.Capabilities)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxConnection.Capabilities parseFrom(InputStream input) throws IOException {
         return (MysqlxConnection.Capabilities)PARSER.parseFrom(input);
      }

      public static MysqlxConnection.Capabilities parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxConnection.Capabilities)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxConnection.Capabilities parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxConnection.Capabilities)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxConnection.Capabilities parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxConnection.Capabilities)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxConnection.Capabilities parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxConnection.Capabilities)PARSER.parseFrom(input);
      }

      public static MysqlxConnection.Capabilities parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxConnection.Capabilities)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxConnection.Capabilities.Builder newBuilder() {
         return MysqlxConnection.Capabilities.Builder.create();
      }

      public MysqlxConnection.Capabilities.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxConnection.Capabilities.Builder newBuilder(MysqlxConnection.Capabilities prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxConnection.Capabilities.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxConnection.Capabilities.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxConnection.Capabilities.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxConnection.Capabilities.Builder>
         implements MysqlxConnection.CapabilitiesOrBuilder {
         private int bitField0_;
         private List<MysqlxConnection.Capability> capabilities_ = Collections.emptyList();
         private RepeatedFieldBuilder<MysqlxConnection.Capability, MysqlxConnection.Capability.Builder, MysqlxConnection.CapabilityOrBuilder> capabilitiesBuilder_;

         public static final Descriptor getDescriptor() {
            return MysqlxConnection.internal_static_Mysqlx_Connection_Capabilities_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxConnection.internal_static_Mysqlx_Connection_Capabilities_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxConnection.Capabilities.class, MysqlxConnection.Capabilities.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxConnection.Capabilities.alwaysUseFieldBuilders) {
               this.getCapabilitiesFieldBuilder();
            }
         }

         private static MysqlxConnection.Capabilities.Builder create() {
            return new MysqlxConnection.Capabilities.Builder();
         }

         public MysqlxConnection.Capabilities.Builder clear() {
            super.clear();
            if (this.capabilitiesBuilder_ == null) {
               this.capabilities_ = Collections.emptyList();
               this.bitField0_ &= -2;
            } else {
               this.capabilitiesBuilder_.clear();
            }

            return this;
         }

         public MysqlxConnection.Capabilities.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxConnection.internal_static_Mysqlx_Connection_Capabilities_descriptor;
         }

         public MysqlxConnection.Capabilities getDefaultInstanceForType() {
            return MysqlxConnection.Capabilities.getDefaultInstance();
         }

         public MysqlxConnection.Capabilities build() {
            MysqlxConnection.Capabilities result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxConnection.Capabilities buildPartial() {
            MysqlxConnection.Capabilities result = new MysqlxConnection.Capabilities(this);
            int from_bitField0_ = this.bitField0_;
            if (this.capabilitiesBuilder_ == null) {
               if ((this.bitField0_ & 1) == 1) {
                  this.capabilities_ = Collections.unmodifiableList(this.capabilities_);
                  this.bitField0_ &= -2;
               }

               result.capabilities_ = this.capabilities_;
            } else {
               result.capabilities_ = this.capabilitiesBuilder_.build();
            }

            this.onBuilt();
            return result;
         }

         public MysqlxConnection.Capabilities.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxConnection.Capabilities) {
               return this.mergeFrom((MysqlxConnection.Capabilities)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxConnection.Capabilities.Builder mergeFrom(MysqlxConnection.Capabilities other) {
            if (other == MysqlxConnection.Capabilities.getDefaultInstance()) {
               return this;
            } else {
               if (this.capabilitiesBuilder_ == null) {
                  if (!other.capabilities_.isEmpty()) {
                     if (this.capabilities_.isEmpty()) {
                        this.capabilities_ = other.capabilities_;
                        this.bitField0_ &= -2;
                     } else {
                        this.ensureCapabilitiesIsMutable();
                        this.capabilities_.addAll(other.capabilities_);
                     }

                     this.onChanged();
                  }
               } else if (!other.capabilities_.isEmpty()) {
                  if (this.capabilitiesBuilder_.isEmpty()) {
                     this.capabilitiesBuilder_.dispose();
                     this.capabilitiesBuilder_ = null;
                     this.capabilities_ = other.capabilities_;
                     this.bitField0_ &= -2;
                     this.capabilitiesBuilder_ = MysqlxConnection.Capabilities.alwaysUseFieldBuilders ? this.getCapabilitiesFieldBuilder() : null;
                  } else {
                     this.capabilitiesBuilder_.addAllMessages(other.capabilities_);
                  }
               }

               this.mergeUnknownFields(other.getUnknownFields());
               return this;
            }
         }

         public final boolean isInitialized() {
            for(int i = 0; i < this.getCapabilitiesCount(); ++i) {
               if (!this.getCapabilities(i).isInitialized()) {
                  return false;
               }
            }

            return true;
         }

         public MysqlxConnection.Capabilities.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxConnection.Capabilities parsedMessage = null;

            try {
               parsedMessage = (MysqlxConnection.Capabilities)MysqlxConnection.Capabilities.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxConnection.Capabilities)var8.getUnfinishedMessage();
               throw var8;
            } finally {
               if (parsedMessage != null) {
                  this.mergeFrom(parsedMessage);
               }
            }

            return this;
         }

         private void ensureCapabilitiesIsMutable() {
            if ((this.bitField0_ & 1) != 1) {
               this.capabilities_ = new ArrayList<>(this.capabilities_);
               this.bitField0_ |= 1;
            }
         }

         @Override
         public List<MysqlxConnection.Capability> getCapabilitiesList() {
            return this.capabilitiesBuilder_ == null ? Collections.unmodifiableList(this.capabilities_) : this.capabilitiesBuilder_.getMessageList();
         }

         @Override
         public int getCapabilitiesCount() {
            return this.capabilitiesBuilder_ == null ? this.capabilities_.size() : this.capabilitiesBuilder_.getCount();
         }

         @Override
         public MysqlxConnection.Capability getCapabilities(int index) {
            return this.capabilitiesBuilder_ == null
               ? this.capabilities_.get(index)
               : (MysqlxConnection.Capability)this.capabilitiesBuilder_.getMessage(index);
         }

         public MysqlxConnection.Capabilities.Builder setCapabilities(int index, MysqlxConnection.Capability value) {
            if (this.capabilitiesBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureCapabilitiesIsMutable();
               this.capabilities_.set(index, value);
               this.onChanged();
            } else {
               this.capabilitiesBuilder_.setMessage(index, value);
            }

            return this;
         }

         public MysqlxConnection.Capabilities.Builder setCapabilities(int index, MysqlxConnection.Capability.Builder builderForValue) {
            if (this.capabilitiesBuilder_ == null) {
               this.ensureCapabilitiesIsMutable();
               this.capabilities_.set(index, builderForValue.build());
               this.onChanged();
            } else {
               this.capabilitiesBuilder_.setMessage(index, builderForValue.build());
            }

            return this;
         }

         public MysqlxConnection.Capabilities.Builder addCapabilities(MysqlxConnection.Capability value) {
            if (this.capabilitiesBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureCapabilitiesIsMutable();
               this.capabilities_.add(value);
               this.onChanged();
            } else {
               this.capabilitiesBuilder_.addMessage(value);
            }

            return this;
         }

         public MysqlxConnection.Capabilities.Builder addCapabilities(int index, MysqlxConnection.Capability value) {
            if (this.capabilitiesBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.ensureCapabilitiesIsMutable();
               this.capabilities_.add(index, value);
               this.onChanged();
            } else {
               this.capabilitiesBuilder_.addMessage(index, value);
            }

            return this;
         }

         public MysqlxConnection.Capabilities.Builder addCapabilities(MysqlxConnection.Capability.Builder builderForValue) {
            if (this.capabilitiesBuilder_ == null) {
               this.ensureCapabilitiesIsMutable();
               this.capabilities_.add(builderForValue.build());
               this.onChanged();
            } else {
               this.capabilitiesBuilder_.addMessage(builderForValue.build());
            }

            return this;
         }

         public MysqlxConnection.Capabilities.Builder addCapabilities(int index, MysqlxConnection.Capability.Builder builderForValue) {
            if (this.capabilitiesBuilder_ == null) {
               this.ensureCapabilitiesIsMutable();
               this.capabilities_.add(index, builderForValue.build());
               this.onChanged();
            } else {
               this.capabilitiesBuilder_.addMessage(index, builderForValue.build());
            }

            return this;
         }

         public MysqlxConnection.Capabilities.Builder addAllCapabilities(Iterable<? extends MysqlxConnection.Capability> values) {
            if (this.capabilitiesBuilder_ == null) {
               this.ensureCapabilitiesIsMutable();
               com.google.protobuf.AbstractMessageLite.Builder.addAll(values, this.capabilities_);
               this.onChanged();
            } else {
               this.capabilitiesBuilder_.addAllMessages(values);
            }

            return this;
         }

         public MysqlxConnection.Capabilities.Builder clearCapabilities() {
            if (this.capabilitiesBuilder_ == null) {
               this.capabilities_ = Collections.emptyList();
               this.bitField0_ &= -2;
               this.onChanged();
            } else {
               this.capabilitiesBuilder_.clear();
            }

            return this;
         }

         public MysqlxConnection.Capabilities.Builder removeCapabilities(int index) {
            if (this.capabilitiesBuilder_ == null) {
               this.ensureCapabilitiesIsMutable();
               this.capabilities_.remove(index);
               this.onChanged();
            } else {
               this.capabilitiesBuilder_.remove(index);
            }

            return this;
         }

         public MysqlxConnection.Capability.Builder getCapabilitiesBuilder(int index) {
            return (MysqlxConnection.Capability.Builder)this.getCapabilitiesFieldBuilder().getBuilder(index);
         }

         @Override
         public MysqlxConnection.CapabilityOrBuilder getCapabilitiesOrBuilder(int index) {
            return this.capabilitiesBuilder_ == null
               ? this.capabilities_.get(index)
               : (MysqlxConnection.CapabilityOrBuilder)this.capabilitiesBuilder_.getMessageOrBuilder(index);
         }

         @Override
         public List<? extends MysqlxConnection.CapabilityOrBuilder> getCapabilitiesOrBuilderList() {
            return this.capabilitiesBuilder_ != null ? this.capabilitiesBuilder_.getMessageOrBuilderList() : Collections.unmodifiableList(this.capabilities_);
         }

         public MysqlxConnection.Capability.Builder addCapabilitiesBuilder() {
            return (MysqlxConnection.Capability.Builder)this.getCapabilitiesFieldBuilder().addBuilder(MysqlxConnection.Capability.getDefaultInstance());
         }

         public MysqlxConnection.Capability.Builder addCapabilitiesBuilder(int index) {
            return (MysqlxConnection.Capability.Builder)this.getCapabilitiesFieldBuilder().addBuilder(index, MysqlxConnection.Capability.getDefaultInstance());
         }

         public List<MysqlxConnection.Capability.Builder> getCapabilitiesBuilderList() {
            return this.getCapabilitiesFieldBuilder().getBuilderList();
         }

         private RepeatedFieldBuilder<MysqlxConnection.Capability, MysqlxConnection.Capability.Builder, MysqlxConnection.CapabilityOrBuilder> getCapabilitiesFieldBuilder() {
            if (this.capabilitiesBuilder_ == null) {
               this.capabilitiesBuilder_ = new RepeatedFieldBuilder(
                  this.capabilities_, (this.bitField0_ & 1) == 1, this.getParentForChildren(), this.isClean()
               );
               this.capabilities_ = null;
            }

            return this.capabilitiesBuilder_;
         }
      }
   }

   public static final class CapabilitiesGet extends GeneratedMessage implements MysqlxConnection.CapabilitiesGetOrBuilder {
      private static final MysqlxConnection.CapabilitiesGet defaultInstance = new MysqlxConnection.CapabilitiesGet(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxConnection.CapabilitiesGet> PARSER = new AbstractParser<MysqlxConnection.CapabilitiesGet>() {
         public MysqlxConnection.CapabilitiesGet parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxConnection.CapabilitiesGet(input, extensionRegistry);
         }
      };
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private CapabilitiesGet(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private CapabilitiesGet(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static MysqlxConnection.CapabilitiesGet getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxConnection.CapabilitiesGet getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private CapabilitiesGet(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
         return MysqlxConnection.internal_static_Mysqlx_Connection_CapabilitiesGet_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxConnection.internal_static_Mysqlx_Connection_CapabilitiesGet_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxConnection.CapabilitiesGet.class, MysqlxConnection.CapabilitiesGet.Builder.class);
      }

      public Parser<MysqlxConnection.CapabilitiesGet> getParserForType() {
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

      public static MysqlxConnection.CapabilitiesGet parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxConnection.CapabilitiesGet)PARSER.parseFrom(data);
      }

      public static MysqlxConnection.CapabilitiesGet parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxConnection.CapabilitiesGet)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxConnection.CapabilitiesGet parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxConnection.CapabilitiesGet)PARSER.parseFrom(data);
      }

      public static MysqlxConnection.CapabilitiesGet parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxConnection.CapabilitiesGet)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxConnection.CapabilitiesGet parseFrom(InputStream input) throws IOException {
         return (MysqlxConnection.CapabilitiesGet)PARSER.parseFrom(input);
      }

      public static MysqlxConnection.CapabilitiesGet parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxConnection.CapabilitiesGet)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxConnection.CapabilitiesGet parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxConnection.CapabilitiesGet)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxConnection.CapabilitiesGet parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxConnection.CapabilitiesGet)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxConnection.CapabilitiesGet parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxConnection.CapabilitiesGet)PARSER.parseFrom(input);
      }

      public static MysqlxConnection.CapabilitiesGet parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxConnection.CapabilitiesGet)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxConnection.CapabilitiesGet.Builder newBuilder() {
         return MysqlxConnection.CapabilitiesGet.Builder.create();
      }

      public MysqlxConnection.CapabilitiesGet.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxConnection.CapabilitiesGet.Builder newBuilder(MysqlxConnection.CapabilitiesGet prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxConnection.CapabilitiesGet.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxConnection.CapabilitiesGet.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxConnection.CapabilitiesGet.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxConnection.CapabilitiesGet.Builder>
         implements MysqlxConnection.CapabilitiesGetOrBuilder {
         public static final Descriptor getDescriptor() {
            return MysqlxConnection.internal_static_Mysqlx_Connection_CapabilitiesGet_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxConnection.internal_static_Mysqlx_Connection_CapabilitiesGet_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxConnection.CapabilitiesGet.class, MysqlxConnection.CapabilitiesGet.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxConnection.CapabilitiesGet.alwaysUseFieldBuilders) {
            }
         }

         private static MysqlxConnection.CapabilitiesGet.Builder create() {
            return new MysqlxConnection.CapabilitiesGet.Builder();
         }

         public MysqlxConnection.CapabilitiesGet.Builder clear() {
            super.clear();
            return this;
         }

         public MysqlxConnection.CapabilitiesGet.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxConnection.internal_static_Mysqlx_Connection_CapabilitiesGet_descriptor;
         }

         public MysqlxConnection.CapabilitiesGet getDefaultInstanceForType() {
            return MysqlxConnection.CapabilitiesGet.getDefaultInstance();
         }

         public MysqlxConnection.CapabilitiesGet build() {
            MysqlxConnection.CapabilitiesGet result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxConnection.CapabilitiesGet buildPartial() {
            MysqlxConnection.CapabilitiesGet result = new MysqlxConnection.CapabilitiesGet(this);
            this.onBuilt();
            return result;
         }

         public MysqlxConnection.CapabilitiesGet.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxConnection.CapabilitiesGet) {
               return this.mergeFrom((MysqlxConnection.CapabilitiesGet)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxConnection.CapabilitiesGet.Builder mergeFrom(MysqlxConnection.CapabilitiesGet other) {
            if (other == MysqlxConnection.CapabilitiesGet.getDefaultInstance()) {
               return this;
            } else {
               this.mergeUnknownFields(other.getUnknownFields());
               return this;
            }
         }

         public final boolean isInitialized() {
            return true;
         }

         public MysqlxConnection.CapabilitiesGet.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxConnection.CapabilitiesGet parsedMessage = null;

            try {
               parsedMessage = (MysqlxConnection.CapabilitiesGet)MysqlxConnection.CapabilitiesGet.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxConnection.CapabilitiesGet)var8.getUnfinishedMessage();
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

   public interface CapabilitiesGetOrBuilder extends MessageOrBuilder {
   }

   public interface CapabilitiesOrBuilder extends MessageOrBuilder {
      List<MysqlxConnection.Capability> getCapabilitiesList();

      MysqlxConnection.Capability getCapabilities(int var1);

      int getCapabilitiesCount();

      List<? extends MysqlxConnection.CapabilityOrBuilder> getCapabilitiesOrBuilderList();

      MysqlxConnection.CapabilityOrBuilder getCapabilitiesOrBuilder(int var1);
   }

   public static final class CapabilitiesSet extends GeneratedMessage implements MysqlxConnection.CapabilitiesSetOrBuilder {
      private static final MysqlxConnection.CapabilitiesSet defaultInstance = new MysqlxConnection.CapabilitiesSet(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxConnection.CapabilitiesSet> PARSER = new AbstractParser<MysqlxConnection.CapabilitiesSet>() {
         public MysqlxConnection.CapabilitiesSet parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxConnection.CapabilitiesSet(input, extensionRegistry);
         }
      };
      private int bitField0_;
      public static final int CAPABILITIES_FIELD_NUMBER = 1;
      private MysqlxConnection.Capabilities capabilities_;
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private CapabilitiesSet(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private CapabilitiesSet(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static MysqlxConnection.CapabilitiesSet getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxConnection.CapabilitiesSet getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private CapabilitiesSet(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
                     MysqlxConnection.Capabilities.Builder subBuilder = null;
                     if ((this.bitField0_ & 1) == 1) {
                        subBuilder = this.capabilities_.toBuilder();
                     }

                     this.capabilities_ = (MysqlxConnection.Capabilities)input.readMessage(MysqlxConnection.Capabilities.PARSER, extensionRegistry);
                     if (subBuilder != null) {
                        subBuilder.mergeFrom(this.capabilities_);
                        this.capabilities_ = subBuilder.buildPartial();
                     }

                     this.bitField0_ |= 1;
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
         return MysqlxConnection.internal_static_Mysqlx_Connection_CapabilitiesSet_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxConnection.internal_static_Mysqlx_Connection_CapabilitiesSet_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxConnection.CapabilitiesSet.class, MysqlxConnection.CapabilitiesSet.Builder.class);
      }

      public Parser<MysqlxConnection.CapabilitiesSet> getParserForType() {
         return PARSER;
      }

      @Override
      public boolean hasCapabilities() {
         return (this.bitField0_ & 1) == 1;
      }

      @Override
      public MysqlxConnection.Capabilities getCapabilities() {
         return this.capabilities_;
      }

      @Override
      public MysqlxConnection.CapabilitiesOrBuilder getCapabilitiesOrBuilder() {
         return this.capabilities_;
      }

      private void initFields() {
         this.capabilities_ = MysqlxConnection.Capabilities.getDefaultInstance();
      }

      public final boolean isInitialized() {
         byte isInitialized = this.memoizedIsInitialized;
         if (isInitialized == 1) {
            return true;
         } else if (isInitialized == 0) {
            return false;
         } else if (!this.hasCapabilities()) {
            this.memoizedIsInitialized = 0;
            return false;
         } else if (!this.getCapabilities().isInitialized()) {
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
            output.writeMessage(1, this.capabilities_);
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
               size += CodedOutputStream.computeMessageSize(1, this.capabilities_);
            }

            size += this.getUnknownFields().getSerializedSize();
            this.memoizedSerializedSize = size;
            return size;
         }
      }

      protected Object writeReplace() throws ObjectStreamException {
         return super.writeReplace();
      }

      public static MysqlxConnection.CapabilitiesSet parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxConnection.CapabilitiesSet)PARSER.parseFrom(data);
      }

      public static MysqlxConnection.CapabilitiesSet parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxConnection.CapabilitiesSet)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxConnection.CapabilitiesSet parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxConnection.CapabilitiesSet)PARSER.parseFrom(data);
      }

      public static MysqlxConnection.CapabilitiesSet parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxConnection.CapabilitiesSet)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxConnection.CapabilitiesSet parseFrom(InputStream input) throws IOException {
         return (MysqlxConnection.CapabilitiesSet)PARSER.parseFrom(input);
      }

      public static MysqlxConnection.CapabilitiesSet parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxConnection.CapabilitiesSet)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxConnection.CapabilitiesSet parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxConnection.CapabilitiesSet)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxConnection.CapabilitiesSet parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxConnection.CapabilitiesSet)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxConnection.CapabilitiesSet parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxConnection.CapabilitiesSet)PARSER.parseFrom(input);
      }

      public static MysqlxConnection.CapabilitiesSet parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxConnection.CapabilitiesSet)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxConnection.CapabilitiesSet.Builder newBuilder() {
         return MysqlxConnection.CapabilitiesSet.Builder.create();
      }

      public MysqlxConnection.CapabilitiesSet.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxConnection.CapabilitiesSet.Builder newBuilder(MysqlxConnection.CapabilitiesSet prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxConnection.CapabilitiesSet.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxConnection.CapabilitiesSet.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxConnection.CapabilitiesSet.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxConnection.CapabilitiesSet.Builder>
         implements MysqlxConnection.CapabilitiesSetOrBuilder {
         private int bitField0_;
         private MysqlxConnection.Capabilities capabilities_ = MysqlxConnection.Capabilities.getDefaultInstance();
         private SingleFieldBuilder<MysqlxConnection.Capabilities, MysqlxConnection.Capabilities.Builder, MysqlxConnection.CapabilitiesOrBuilder> capabilitiesBuilder_;

         public static final Descriptor getDescriptor() {
            return MysqlxConnection.internal_static_Mysqlx_Connection_CapabilitiesSet_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxConnection.internal_static_Mysqlx_Connection_CapabilitiesSet_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxConnection.CapabilitiesSet.class, MysqlxConnection.CapabilitiesSet.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxConnection.CapabilitiesSet.alwaysUseFieldBuilders) {
               this.getCapabilitiesFieldBuilder();
            }
         }

         private static MysqlxConnection.CapabilitiesSet.Builder create() {
            return new MysqlxConnection.CapabilitiesSet.Builder();
         }

         public MysqlxConnection.CapabilitiesSet.Builder clear() {
            super.clear();
            if (this.capabilitiesBuilder_ == null) {
               this.capabilities_ = MysqlxConnection.Capabilities.getDefaultInstance();
            } else {
               this.capabilitiesBuilder_.clear();
            }

            this.bitField0_ &= -2;
            return this;
         }

         public MysqlxConnection.CapabilitiesSet.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxConnection.internal_static_Mysqlx_Connection_CapabilitiesSet_descriptor;
         }

         public MysqlxConnection.CapabilitiesSet getDefaultInstanceForType() {
            return MysqlxConnection.CapabilitiesSet.getDefaultInstance();
         }

         public MysqlxConnection.CapabilitiesSet build() {
            MysqlxConnection.CapabilitiesSet result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxConnection.CapabilitiesSet buildPartial() {
            MysqlxConnection.CapabilitiesSet result = new MysqlxConnection.CapabilitiesSet(this);
            int from_bitField0_ = this.bitField0_;
            int to_bitField0_ = 0;
            if ((from_bitField0_ & 1) == 1) {
               to_bitField0_ |= 1;
            }

            if (this.capabilitiesBuilder_ == null) {
               result.capabilities_ = this.capabilities_;
            } else {
               result.capabilities_ = (MysqlxConnection.Capabilities)this.capabilitiesBuilder_.build();
            }

            result.bitField0_ = to_bitField0_;
            this.onBuilt();
            return result;
         }

         public MysqlxConnection.CapabilitiesSet.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxConnection.CapabilitiesSet) {
               return this.mergeFrom((MysqlxConnection.CapabilitiesSet)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxConnection.CapabilitiesSet.Builder mergeFrom(MysqlxConnection.CapabilitiesSet other) {
            if (other == MysqlxConnection.CapabilitiesSet.getDefaultInstance()) {
               return this;
            } else {
               if (other.hasCapabilities()) {
                  this.mergeCapabilities(other.getCapabilities());
               }

               this.mergeUnknownFields(other.getUnknownFields());
               return this;
            }
         }

         public final boolean isInitialized() {
            if (!this.hasCapabilities()) {
               return false;
            } else {
               return this.getCapabilities().isInitialized();
            }
         }

         public MysqlxConnection.CapabilitiesSet.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxConnection.CapabilitiesSet parsedMessage = null;

            try {
               parsedMessage = (MysqlxConnection.CapabilitiesSet)MysqlxConnection.CapabilitiesSet.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxConnection.CapabilitiesSet)var8.getUnfinishedMessage();
               throw var8;
            } finally {
               if (parsedMessage != null) {
                  this.mergeFrom(parsedMessage);
               }
            }

            return this;
         }

         @Override
         public boolean hasCapabilities() {
            return (this.bitField0_ & 1) == 1;
         }

         @Override
         public MysqlxConnection.Capabilities getCapabilities() {
            return this.capabilitiesBuilder_ == null ? this.capabilities_ : (MysqlxConnection.Capabilities)this.capabilitiesBuilder_.getMessage();
         }

         public MysqlxConnection.CapabilitiesSet.Builder setCapabilities(MysqlxConnection.Capabilities value) {
            if (this.capabilitiesBuilder_ == null) {
               if (value == null) {
                  throw new NullPointerException();
               }

               this.capabilities_ = value;
               this.onChanged();
            } else {
               this.capabilitiesBuilder_.setMessage(value);
            }

            this.bitField0_ |= 1;
            return this;
         }

         public MysqlxConnection.CapabilitiesSet.Builder setCapabilities(MysqlxConnection.Capabilities.Builder builderForValue) {
            if (this.capabilitiesBuilder_ == null) {
               this.capabilities_ = builderForValue.build();
               this.onChanged();
            } else {
               this.capabilitiesBuilder_.setMessage(builderForValue.build());
            }

            this.bitField0_ |= 1;
            return this;
         }

         public MysqlxConnection.CapabilitiesSet.Builder mergeCapabilities(MysqlxConnection.Capabilities value) {
            if (this.capabilitiesBuilder_ == null) {
               if ((this.bitField0_ & 1) == 1 && this.capabilities_ != MysqlxConnection.Capabilities.getDefaultInstance()) {
                  this.capabilities_ = MysqlxConnection.Capabilities.newBuilder(this.capabilities_).mergeFrom(value).buildPartial();
               } else {
                  this.capabilities_ = value;
               }

               this.onChanged();
            } else {
               this.capabilitiesBuilder_.mergeFrom(value);
            }

            this.bitField0_ |= 1;
            return this;
         }

         public MysqlxConnection.CapabilitiesSet.Builder clearCapabilities() {
            if (this.capabilitiesBuilder_ == null) {
               this.capabilities_ = MysqlxConnection.Capabilities.getDefaultInstance();
               this.onChanged();
            } else {
               this.capabilitiesBuilder_.clear();
            }

            this.bitField0_ &= -2;
            return this;
         }

         public MysqlxConnection.Capabilities.Builder getCapabilitiesBuilder() {
            this.bitField0_ |= 1;
            this.onChanged();
            return (MysqlxConnection.Capabilities.Builder)this.getCapabilitiesFieldBuilder().getBuilder();
         }

         @Override
         public MysqlxConnection.CapabilitiesOrBuilder getCapabilitiesOrBuilder() {
            return (MysqlxConnection.CapabilitiesOrBuilder)(this.capabilitiesBuilder_ != null
               ? (MysqlxConnection.CapabilitiesOrBuilder)this.capabilitiesBuilder_.getMessageOrBuilder()
               : this.capabilities_);
         }

         private SingleFieldBuilder<MysqlxConnection.Capabilities, MysqlxConnection.Capabilities.Builder, MysqlxConnection.CapabilitiesOrBuilder> getCapabilitiesFieldBuilder() {
            if (this.capabilitiesBuilder_ == null) {
               this.capabilitiesBuilder_ = new SingleFieldBuilder(this.getCapabilities(), this.getParentForChildren(), this.isClean());
               this.capabilities_ = null;
            }

            return this.capabilitiesBuilder_;
         }
      }
   }

   public interface CapabilitiesSetOrBuilder extends MessageOrBuilder {
      boolean hasCapabilities();

      MysqlxConnection.Capabilities getCapabilities();

      MysqlxConnection.CapabilitiesOrBuilder getCapabilitiesOrBuilder();
   }

   public static final class Capability extends GeneratedMessage implements MysqlxConnection.CapabilityOrBuilder {
      private static final MysqlxConnection.Capability defaultInstance = new MysqlxConnection.Capability(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxConnection.Capability> PARSER = new AbstractParser<MysqlxConnection.Capability>() {
         public MysqlxConnection.Capability parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxConnection.Capability(input, extensionRegistry);
         }
      };
      private int bitField0_;
      public static final int NAME_FIELD_NUMBER = 1;
      private Object name_;
      public static final int VALUE_FIELD_NUMBER = 2;
      private MysqlxDatatypes.Any value_;
      private byte memoizedIsInitialized = -1;
      private int memoizedSerializedSize = -1;
      private static final long serialVersionUID = 0L;

      private Capability(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
         super(builder);
         this.unknownFields = builder.getUnknownFields();
      }

      private Capability(boolean noInit) {
         this.unknownFields = UnknownFieldSet.getDefaultInstance();
      }

      public static MysqlxConnection.Capability getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxConnection.Capability getDefaultInstanceForType() {
         return defaultInstance;
      }

      public final UnknownFieldSet getUnknownFields() {
         return this.unknownFields;
      }

      private Capability(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
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
         return MysqlxConnection.internal_static_Mysqlx_Connection_Capability_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxConnection.internal_static_Mysqlx_Connection_Capability_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxConnection.Capability.class, MysqlxConnection.Capability.Builder.class);
      }

      public Parser<MysqlxConnection.Capability> getParserForType() {
         return PARSER;
      }

      @Override
      public boolean hasName() {
         return (this.bitField0_ & 1) == 1;
      }

      @Override
      public String getName() {
         Object ref = this.name_;
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
         Object ref = this.name_;
         if (ref instanceof String) {
            ByteString b = ByteString.copyFromUtf8((String)ref);
            this.name_ = b;
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
         this.name_ = "";
         this.value_ = MysqlxDatatypes.Any.getDefaultInstance();
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
            output.writeBytes(1, this.getNameBytes());
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
               size += CodedOutputStream.computeBytesSize(1, this.getNameBytes());
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

      public static MysqlxConnection.Capability parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxConnection.Capability)PARSER.parseFrom(data);
      }

      public static MysqlxConnection.Capability parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxConnection.Capability)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxConnection.Capability parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxConnection.Capability)PARSER.parseFrom(data);
      }

      public static MysqlxConnection.Capability parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxConnection.Capability)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxConnection.Capability parseFrom(InputStream input) throws IOException {
         return (MysqlxConnection.Capability)PARSER.parseFrom(input);
      }

      public static MysqlxConnection.Capability parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxConnection.Capability)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxConnection.Capability parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxConnection.Capability)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxConnection.Capability parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxConnection.Capability)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxConnection.Capability parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxConnection.Capability)PARSER.parseFrom(input);
      }

      public static MysqlxConnection.Capability parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxConnection.Capability)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxConnection.Capability.Builder newBuilder() {
         return MysqlxConnection.Capability.Builder.create();
      }

      public MysqlxConnection.Capability.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxConnection.Capability.Builder newBuilder(MysqlxConnection.Capability prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxConnection.Capability.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxConnection.Capability.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxConnection.Capability.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxConnection.Capability.Builder>
         implements MysqlxConnection.CapabilityOrBuilder {
         private int bitField0_;
         private Object name_ = "";
         private MysqlxDatatypes.Any value_ = MysqlxDatatypes.Any.getDefaultInstance();
         private SingleFieldBuilder<MysqlxDatatypes.Any, MysqlxDatatypes.Any.Builder, MysqlxDatatypes.AnyOrBuilder> valueBuilder_;

         public static final Descriptor getDescriptor() {
            return MysqlxConnection.internal_static_Mysqlx_Connection_Capability_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxConnection.internal_static_Mysqlx_Connection_Capability_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxConnection.Capability.class, MysqlxConnection.Capability.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxConnection.Capability.alwaysUseFieldBuilders) {
               this.getValueFieldBuilder();
            }
         }

         private static MysqlxConnection.Capability.Builder create() {
            return new MysqlxConnection.Capability.Builder();
         }

         public MysqlxConnection.Capability.Builder clear() {
            super.clear();
            this.name_ = "";
            this.bitField0_ &= -2;
            if (this.valueBuilder_ == null) {
               this.value_ = MysqlxDatatypes.Any.getDefaultInstance();
            } else {
               this.valueBuilder_.clear();
            }

            this.bitField0_ &= -3;
            return this;
         }

         public MysqlxConnection.Capability.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxConnection.internal_static_Mysqlx_Connection_Capability_descriptor;
         }

         public MysqlxConnection.Capability getDefaultInstanceForType() {
            return MysqlxConnection.Capability.getDefaultInstance();
         }

         public MysqlxConnection.Capability build() {
            MysqlxConnection.Capability result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxConnection.Capability buildPartial() {
            MysqlxConnection.Capability result = new MysqlxConnection.Capability(this);
            int from_bitField0_ = this.bitField0_;
            int to_bitField0_ = 0;
            if ((from_bitField0_ & 1) == 1) {
               to_bitField0_ |= 1;
            }

            result.name_ = this.name_;
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

         public MysqlxConnection.Capability.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxConnection.Capability) {
               return this.mergeFrom((MysqlxConnection.Capability)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxConnection.Capability.Builder mergeFrom(MysqlxConnection.Capability other) {
            if (other == MysqlxConnection.Capability.getDefaultInstance()) {
               return this;
            } else {
               if (other.hasName()) {
                  this.bitField0_ |= 1;
                  this.name_ = other.name_;
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
            if (!this.hasName()) {
               return false;
            } else if (!this.hasValue()) {
               return false;
            } else {
               return this.getValue().isInitialized();
            }
         }

         public MysqlxConnection.Capability.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxConnection.Capability parsedMessage = null;

            try {
               parsedMessage = (MysqlxConnection.Capability)MysqlxConnection.Capability.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxConnection.Capability)var8.getUnfinishedMessage();
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
            Object ref = this.name_;
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
            Object ref = this.name_;
            if (ref instanceof String) {
               ByteString b = ByteString.copyFromUtf8((String)ref);
               this.name_ = b;
               return b;
            } else {
               return (ByteString)ref;
            }
         }

         public MysqlxConnection.Capability.Builder setName(String value) {
            if (value == null) {
               throw new NullPointerException();
            } else {
               this.bitField0_ |= 1;
               this.name_ = value;
               this.onChanged();
               return this;
            }
         }

         public MysqlxConnection.Capability.Builder clearName() {
            this.bitField0_ &= -2;
            this.name_ = MysqlxConnection.Capability.getDefaultInstance().getName();
            this.onChanged();
            return this;
         }

         public MysqlxConnection.Capability.Builder setNameBytes(ByteString value) {
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
         public boolean hasValue() {
            return (this.bitField0_ & 2) == 2;
         }

         @Override
         public MysqlxDatatypes.Any getValue() {
            return this.valueBuilder_ == null ? this.value_ : (MysqlxDatatypes.Any)this.valueBuilder_.getMessage();
         }

         public MysqlxConnection.Capability.Builder setValue(MysqlxDatatypes.Any value) {
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

         public MysqlxConnection.Capability.Builder setValue(MysqlxDatatypes.Any.Builder builderForValue) {
            if (this.valueBuilder_ == null) {
               this.value_ = builderForValue.build();
               this.onChanged();
            } else {
               this.valueBuilder_.setMessage(builderForValue.build());
            }

            this.bitField0_ |= 2;
            return this;
         }

         public MysqlxConnection.Capability.Builder mergeValue(MysqlxDatatypes.Any value) {
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

         public MysqlxConnection.Capability.Builder clearValue() {
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

   public interface CapabilityOrBuilder extends MessageOrBuilder {
      boolean hasName();

      String getName();

      ByteString getNameBytes();

      boolean hasValue();

      MysqlxDatatypes.Any getValue();

      MysqlxDatatypes.AnyOrBuilder getValueOrBuilder();
   }

   public static final class Close extends GeneratedMessage implements MysqlxConnection.CloseOrBuilder {
      private static final MysqlxConnection.Close defaultInstance = new MysqlxConnection.Close(true);
      private final UnknownFieldSet unknownFields;
      public static Parser<MysqlxConnection.Close> PARSER = new AbstractParser<MysqlxConnection.Close>() {
         public MysqlxConnection.Close parsePartialFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
            return new MysqlxConnection.Close(input, extensionRegistry);
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

      public static MysqlxConnection.Close getDefaultInstance() {
         return defaultInstance;
      }

      public MysqlxConnection.Close getDefaultInstanceForType() {
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
         return MysqlxConnection.internal_static_Mysqlx_Connection_Close_descriptor;
      }

      protected FieldAccessorTable internalGetFieldAccessorTable() {
         return MysqlxConnection.internal_static_Mysqlx_Connection_Close_fieldAccessorTable
            .ensureFieldAccessorsInitialized(MysqlxConnection.Close.class, MysqlxConnection.Close.Builder.class);
      }

      public Parser<MysqlxConnection.Close> getParserForType() {
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

      public static MysqlxConnection.Close parseFrom(ByteString data) throws InvalidProtocolBufferException {
         return (MysqlxConnection.Close)PARSER.parseFrom(data);
      }

      public static MysqlxConnection.Close parseFrom(ByteString data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxConnection.Close)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxConnection.Close parseFrom(byte[] data) throws InvalidProtocolBufferException {
         return (MysqlxConnection.Close)PARSER.parseFrom(data);
      }

      public static MysqlxConnection.Close parseFrom(byte[] data, ExtensionRegistryLite extensionRegistry) throws InvalidProtocolBufferException {
         return (MysqlxConnection.Close)PARSER.parseFrom(data, extensionRegistry);
      }

      public static MysqlxConnection.Close parseFrom(InputStream input) throws IOException {
         return (MysqlxConnection.Close)PARSER.parseFrom(input);
      }

      public static MysqlxConnection.Close parseFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxConnection.Close)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxConnection.Close parseDelimitedFrom(InputStream input) throws IOException {
         return (MysqlxConnection.Close)PARSER.parseDelimitedFrom(input);
      }

      public static MysqlxConnection.Close parseDelimitedFrom(InputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxConnection.Close)PARSER.parseDelimitedFrom(input, extensionRegistry);
      }

      public static MysqlxConnection.Close parseFrom(CodedInputStream input) throws IOException {
         return (MysqlxConnection.Close)PARSER.parseFrom(input);
      }

      public static MysqlxConnection.Close parseFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
         return (MysqlxConnection.Close)PARSER.parseFrom(input, extensionRegistry);
      }

      public static MysqlxConnection.Close.Builder newBuilder() {
         return MysqlxConnection.Close.Builder.create();
      }

      public MysqlxConnection.Close.Builder newBuilderForType() {
         return newBuilder();
      }

      public static MysqlxConnection.Close.Builder newBuilder(MysqlxConnection.Close prototype) {
         return newBuilder().mergeFrom(prototype);
      }

      public MysqlxConnection.Close.Builder toBuilder() {
         return newBuilder(this);
      }

      protected MysqlxConnection.Close.Builder newBuilderForType(BuilderParent parent) {
         return new MysqlxConnection.Close.Builder(parent);
      }

      static {
         defaultInstance.initFields();
      }

      public static final class Builder
         extends com.google.protobuf.GeneratedMessage.Builder<MysqlxConnection.Close.Builder>
         implements MysqlxConnection.CloseOrBuilder {
         public static final Descriptor getDescriptor() {
            return MysqlxConnection.internal_static_Mysqlx_Connection_Close_descriptor;
         }

         protected FieldAccessorTable internalGetFieldAccessorTable() {
            return MysqlxConnection.internal_static_Mysqlx_Connection_Close_fieldAccessorTable
               .ensureFieldAccessorsInitialized(MysqlxConnection.Close.class, MysqlxConnection.Close.Builder.class);
         }

         private Builder() {
            this.maybeForceBuilderInitialization();
         }

         private Builder(BuilderParent parent) {
            super(parent);
            this.maybeForceBuilderInitialization();
         }

         private void maybeForceBuilderInitialization() {
            if (MysqlxConnection.Close.alwaysUseFieldBuilders) {
            }
         }

         private static MysqlxConnection.Close.Builder create() {
            return new MysqlxConnection.Close.Builder();
         }

         public MysqlxConnection.Close.Builder clear() {
            super.clear();
            return this;
         }

         public MysqlxConnection.Close.Builder clone() {
            return create().mergeFrom(this.buildPartial());
         }

         public Descriptor getDescriptorForType() {
            return MysqlxConnection.internal_static_Mysqlx_Connection_Close_descriptor;
         }

         public MysqlxConnection.Close getDefaultInstanceForType() {
            return MysqlxConnection.Close.getDefaultInstance();
         }

         public MysqlxConnection.Close build() {
            MysqlxConnection.Close result = this.buildPartial();
            if (!result.isInitialized()) {
               throw newUninitializedMessageException(result);
            } else {
               return result;
            }
         }

         public MysqlxConnection.Close buildPartial() {
            MysqlxConnection.Close result = new MysqlxConnection.Close(this);
            this.onBuilt();
            return result;
         }

         public MysqlxConnection.Close.Builder mergeFrom(Message other) {
            if (other instanceof MysqlxConnection.Close) {
               return this.mergeFrom((MysqlxConnection.Close)other);
            } else {
               super.mergeFrom(other);
               return this;
            }
         }

         public MysqlxConnection.Close.Builder mergeFrom(MysqlxConnection.Close other) {
            if (other == MysqlxConnection.Close.getDefaultInstance()) {
               return this;
            } else {
               this.mergeUnknownFields(other.getUnknownFields());
               return this;
            }
         }

         public final boolean isInitialized() {
            return true;
         }

         public MysqlxConnection.Close.Builder mergeFrom(CodedInputStream input, ExtensionRegistryLite extensionRegistry) throws IOException {
            MysqlxConnection.Close parsedMessage = null;

            try {
               parsedMessage = (MysqlxConnection.Close)MysqlxConnection.Close.PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (InvalidProtocolBufferException var8) {
               parsedMessage = (MysqlxConnection.Close)var8.getUnfinishedMessage();
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
}
