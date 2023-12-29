package com.mysql.cj.protocol.a.result;

import com.mysql.cj.Constants;
import com.mysql.cj.Messages;
import com.mysql.cj.exceptions.CJException;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.ExceptionInterceptor;
import com.mysql.cj.exceptions.StreamingNotifiable;
import com.mysql.cj.log.ProfilerEventHandler;
import com.mysql.cj.log.ProfilerEventHandlerFactory;
import com.mysql.cj.log.ProfilerEventImpl;
import com.mysql.cj.protocol.ColumnDefinition;
import com.mysql.cj.protocol.ProtocolEntity;
import com.mysql.cj.protocol.ProtocolEntityFactory;
import com.mysql.cj.protocol.Resultset;
import com.mysql.cj.protocol.ResultsetRow;
import com.mysql.cj.protocol.ResultsetRows;
import com.mysql.cj.protocol.a.BinaryRowFactory;
import com.mysql.cj.protocol.a.NativeMessageBuilder;
import com.mysql.cj.protocol.a.NativePacketPayload;
import com.mysql.cj.protocol.a.NativeProtocol;
import com.mysql.cj.protocol.a.TextRowFactory;
import com.mysql.cj.result.Row;
import com.mysql.cj.util.Util;

public class ResultsetRowsStreaming<T extends ProtocolEntity> extends AbstractResultsetRows implements ResultsetRows {
   private NativeProtocol protocol;
   private boolean isAfterEnd = false;
   private boolean noMoreRows = false;
   private boolean isBinaryEncoded = false;
   private Row nextRow;
   private boolean streamerClosed = false;
   private ExceptionInterceptor exceptionInterceptor;
   private ProtocolEntityFactory<T, NativePacketPayload> resultSetFactory;
   private NativeMessageBuilder commandBuilder = new NativeMessageBuilder();

   public ResultsetRowsStreaming(
      NativeProtocol io, ColumnDefinition columnDefinition, boolean isBinaryEncoded, ProtocolEntityFactory<T, NativePacketPayload> resultSetFactory
   ) {
      this.protocol = io;
      this.isBinaryEncoded = isBinaryEncoded;
      this.metadata = columnDefinition;
      this.exceptionInterceptor = this.protocol.getExceptionInterceptor();
      this.resultSetFactory = resultSetFactory;
      this.rowFactory = (ProtocolEntityFactory<ResultsetRow, NativePacketPayload>)(this.isBinaryEncoded
         ? new BinaryRowFactory(this.protocol, this.metadata, Resultset.Concurrency.READ_ONLY, true)
         : new TextRowFactory(this.protocol, this.metadata, Resultset.Concurrency.READ_ONLY, true));
   }

   @Override
   public void close() {
      Object mutex = this.owner != null && this.owner.getSyncMutex() != null ? this.owner.getSyncMutex() : this;
      boolean hadMore = false;
      int howMuchMore = 0;
      synchronized(mutex) {
         while(this.next() != null) {
            hadMore = true;
            if (++howMuchMore % 100 == 0) {
               Thread.yield();
            }
         }

         if (!this.protocol.getPropertySet().getBooleanProperty("clobberStreamingResults").getValue()
            && this.protocol.getPropertySet().getIntegerProperty("netTimeoutForStreamingResults").getValue() > 0) {
            int oldValue = this.protocol.getServerSession().getServerVariable("net_write_timeout", 60);
            this.protocol.clearInputStream();

            try {
               this.protocol
                  .sendCommand(
                     this.commandBuilder
                        .buildComQuery(
                           this.protocol.getSharedSendPacket(),
                           "SET net_write_timeout=" + oldValue,
                           this.protocol.getPropertySet().getStringProperty("characterEncoding").getValue()
                        ),
                     false,
                     0
                  );
            } catch (Exception var8) {
               throw ExceptionFactory.createException(var8.getMessage(), var8, this.exceptionInterceptor);
            }
         }

         if (this.protocol.getPropertySet().getBooleanProperty("useUsageAdvisor").getValue() && hadMore) {
            ProfilerEventHandler eventSink = ProfilerEventHandlerFactory.getInstance(this.owner.getSession());
            eventSink.consumeEvent(
               new ProfilerEventImpl(
                  (byte)0,
                  "",
                  this.owner.getCurrentCatalog(),
                  this.owner.getConnectionId(),
                  this.owner.getOwningStatementId(),
                  -1,
                  System.currentTimeMillis(),
                  0L,
                  Constants.MILLIS_I18N,
                  null,
                  null,
                  Messages.getString("RowDataDynamic.2")
                     + howMuchMore
                     + Messages.getString("RowDataDynamic.3")
                     + Messages.getString("RowDataDynamic.4")
                     + Messages.getString("RowDataDynamic.5")
                     + Messages.getString("RowDataDynamic.6")
                     + this.owner.getPointOfOrigin()
               )
            );
         }
      }

      this.metadata = null;
      this.owner = null;
   }

   @Override
   public boolean hasNext() {
      boolean hasNext = this.nextRow != null;
      if (!hasNext && !this.streamerClosed) {
         this.protocol.closeStreamer(this);
         this.streamerClosed = true;
      }

      return hasNext;
   }

   @Override
   public boolean isAfterLast() {
      return this.isAfterEnd;
   }

   @Override
   public boolean isBeforeFirst() {
      return this.currentPositionInFetchedRows < 0;
   }

   @Override
   public boolean isEmpty() {
      return this.wasEmpty;
   }

   @Override
   public boolean isFirst() {
      return this.currentPositionInFetchedRows == 0;
   }

   @Override
   public boolean isLast() {
      return !this.isBeforeFirst() && !this.isAfterLast() && this.noMoreRows;
   }

   public Row next() {
      try {
         if (!this.noMoreRows) {
            this.nextRow = this.protocol.read(ResultsetRow.class, this.rowFactory);
            if (this.nextRow == null) {
               this.noMoreRows = true;
               this.isAfterEnd = true;
               if (this.currentPositionInFetchedRows == -1) {
                  this.wasEmpty = true;
               }
            }
         } else {
            this.nextRow = null;
            this.isAfterEnd = true;
         }

         if (this.nextRow == null && !this.streamerClosed) {
            if (this.protocol.getServerSession().hasMoreResults()) {
               this.protocol.readNextResultset((T)this.owner, this.owner.getOwningStatementMaxRows(), true, this.isBinaryEncoded, this.resultSetFactory);
            } else {
               this.protocol.closeStreamer(this);
               this.streamerClosed = true;
            }
         }

         if (this.nextRow != null && this.currentPositionInFetchedRows != Integer.MAX_VALUE) {
            ++this.currentPositionInFetchedRows;
         }

         return this.nextRow;
      } catch (CJException var5) {
         if (var5 instanceof StreamingNotifiable) {
            ((StreamingNotifiable)var5).setWasStreamingResults();
         }

         this.noMoreRows = true;
         throw var5;
      } catch (Exception var6) {
         String exceptionType = var6.getClass().getName();
         String exceptionMessage = var6.getMessage();
         exceptionMessage = exceptionMessage + Messages.getString("RowDataDynamic.7");
         exceptionMessage = exceptionMessage + Util.stackTraceToString(var6);
         CJException cjEx = ExceptionFactory.createException(
            Messages.getString("RowDataDynamic.8") + exceptionType + Messages.getString("RowDataDynamic.9") + exceptionMessage,
            var6,
            this.exceptionInterceptor
         );
         throw cjEx;
      }
   }

   @Override
   public void afterLast() {
      throw ExceptionFactory.createException(Messages.getString("ResultSet.ForwardOnly"));
   }

   @Override
   public void beforeFirst() {
      throw ExceptionFactory.createException(Messages.getString("ResultSet.ForwardOnly"));
   }

   @Override
   public void beforeLast() {
      throw ExceptionFactory.createException(Messages.getString("ResultSet.ForwardOnly"));
   }

   @Override
   public void moveRowRelative(int rows) {
      throw ExceptionFactory.createException(Messages.getString("ResultSet.ForwardOnly"));
   }

   @Override
   public void setCurrentRow(int rowNumber) {
      throw ExceptionFactory.createException(Messages.getString("ResultSet.ForwardOnly"));
   }
}
