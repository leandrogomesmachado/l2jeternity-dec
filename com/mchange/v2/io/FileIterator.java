package com.mchange.v2.io;

import com.mchange.v1.util.UIterator;
import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;

public interface FileIterator extends UIterator {
   FileIterator EMPTY_FILE_ITERATOR = new FileIterator() {
      @Override
      public File nextFile() {
         throw new NoSuchElementException();
      }

      @Override
      public boolean hasNext() {
         return false;
      }

      @Override
      public Object next() {
         throw new NoSuchElementException();
      }

      @Override
      public void remove() {
         throw new IllegalStateException();
      }

      @Override
      public void close() {
      }
   };

   File nextFile() throws IOException;

   @Override
   boolean hasNext() throws IOException;

   @Override
   Object next() throws IOException;

   @Override
   void remove() throws IOException;

   @Override
   void close() throws IOException;
}
