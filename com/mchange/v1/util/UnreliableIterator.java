package com.mchange.v1.util;

public interface UnreliableIterator extends UIterator {
   @Override
   boolean hasNext() throws UnreliableIteratorException;

   @Override
   Object next() throws UnreliableIteratorException;

   @Override
   void remove() throws UnreliableIteratorException;

   @Override
   void close() throws UnreliableIteratorException;
}
