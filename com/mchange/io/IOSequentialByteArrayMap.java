package com.mchange.io;

import com.mchange.util.ByteArrayBinding;
import com.mchange.util.ByteArrayComparator;
import java.io.IOException;

public interface IOSequentialByteArrayMap extends IOByteArrayMap {
   ByteArrayComparator getByteArrayComparator();

   IOSequentialByteArrayMap.Cursor getCursor();

   public interface Cursor {
      ByteArrayBinding getFirst() throws IOException;

      ByteArrayBinding getNext() throws IOException;

      ByteArrayBinding getPrevious() throws IOException;

      ByteArrayBinding getLast() throws IOException;

      ByteArrayBinding getCurrent() throws IOException;

      ByteArrayBinding find(byte[] var1) throws IOException;

      ByteArrayBinding findGreaterThanOrEqual(byte[] var1) throws IOException;

      ByteArrayBinding findLessThanOrEqual(byte[] var1) throws IOException;

      void deleteCurrent() throws IOException;

      void replaceCurrent(byte[] var1) throws IOException;
   }
}
