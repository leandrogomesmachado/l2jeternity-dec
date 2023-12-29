package com.mysql.cj;

import com.mysql.cj.util.StringUtils;

public class ClientPreparedQuery extends AbstractPreparedQuery<ClientPreparedQueryBindings> {
   public ClientPreparedQuery(NativeSession sess) {
      super(sess);
   }

   @Override
   protected long[] computeMaxParameterSetSizeAndBatchSize(int numBatchedArgs) {
      long sizeOfEntireBatch = 0L;
      long maxSizeOfParameterSet = 0L;

      for(int i = 0; i < numBatchedArgs; ++i) {
         ClientPreparedQueryBindings qBindings = (ClientPreparedQueryBindings)this.batchedArgs.get(i);
         BindValue[] bindValues = qBindings.getBindValues();
         long sizeOfParameterSet = 0L;

         for(int j = 0; j < bindValues.length; ++j) {
            if (!bindValues[j].isNull()) {
               if (bindValues[j].isStream()) {
                  int streamLength = bindValues[j].getStreamLength();
                  if (streamLength != -1) {
                     sizeOfParameterSet += (long)(streamLength * 2);
                  } else {
                     int paramLength = qBindings.getBindValues()[j].getByteValue().length;
                     sizeOfParameterSet += (long)paramLength;
                  }
               } else {
                  sizeOfParameterSet += (long)qBindings.getBindValues()[j].getByteValue().length;
               }
            } else {
               sizeOfParameterSet += 4L;
            }
         }

         if (this.parseInfo.getValuesClause() != null) {
            sizeOfParameterSet += (long)(this.parseInfo.getValuesClause().length() + 1);
         } else {
            sizeOfParameterSet += (long)(this.originalSql.length() + 1);
         }

         sizeOfEntireBatch += sizeOfParameterSet;
         if (sizeOfParameterSet > maxSizeOfParameterSet) {
            maxSizeOfParameterSet = sizeOfParameterSet;
         }
      }

      return new long[]{maxSizeOfParameterSet, sizeOfEntireBatch};
   }

   public byte[] getBytesRepresentation(int parameterIndex) {
      BindValue bv = this.queryBindings.getBindValues()[parameterIndex];
      if (bv.isStream()) {
         return this.streamToBytes(bv.getStreamValue(), false, bv.getStreamLength(), this.useStreamLengthsInPrepStmts.getValue());
      } else {
         byte[] parameterVal = bv.getByteValue();
         if (parameterVal == null) {
            return null;
         } else if (parameterVal[0] == 39 && parameterVal[parameterVal.length - 1] == 39) {
            byte[] valNoQuotes = new byte[parameterVal.length - 2];
            System.arraycopy(parameterVal, 1, valNoQuotes, 0, parameterVal.length - 2);
            return valNoQuotes;
         } else {
            return parameterVal;
         }
      }
   }

   public byte[] getBytesRepresentationForBatch(int parameterIndex, int commandIndex) {
      Object batchedArg = this.batchedArgs.get(commandIndex);
      if (batchedArg instanceof String) {
         return StringUtils.getBytes((String)batchedArg, this.charEncoding);
      } else {
         BindValue bv = ((ClientPreparedQueryBindings)batchedArg).getBindValues()[parameterIndex];
         if (bv.isStream()) {
            return this.streamToBytes(bv.getStreamValue(), false, bv.getStreamLength(), this.useStreamLengthsInPrepStmts.getValue());
         } else {
            byte[] parameterVal = bv.getByteValue();
            if (parameterVal == null) {
               return null;
            } else if (parameterVal[0] == 39 && parameterVal[parameterVal.length - 1] == 39) {
               byte[] valNoQuotes = new byte[parameterVal.length - 2];
               System.arraycopy(parameterVal, 1, valNoQuotes, 0, parameterVal.length - 2);
               return valNoQuotes;
            } else {
               return parameterVal;
            }
         }
      }
   }
}
