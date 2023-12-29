package com.mysql.cj;

import com.mysql.cj.util.StringUtils;
import java.util.Iterator;
import java.util.LinkedList;

public class AppendingBatchVisitor implements BatchVisitor {
   LinkedList<byte[]> statementComponents = new LinkedList<>();

   @Override
   public BatchVisitor append(byte[] values) {
      this.statementComponents.addLast(values);
      return this;
   }

   @Override
   public BatchVisitor increment() {
      return this;
   }

   @Override
   public BatchVisitor decrement() {
      this.statementComponents.removeLast();
      return this;
   }

   @Override
   public BatchVisitor merge(byte[] front, byte[] back) {
      int mergedLength = front.length + back.length;
      byte[] merged = new byte[mergedLength];
      System.arraycopy(front, 0, merged, 0, front.length);
      System.arraycopy(back, 0, merged, front.length, back.length);
      this.statementComponents.addLast(merged);
      return this;
   }

   public byte[][] getStaticSqlStrings() {
      byte[][] asBytes = new byte[this.statementComponents.size()][];
      this.statementComponents.toArray(asBytes);
      return asBytes;
   }

   @Override
   public String toString() {
      StringBuilder buf = new StringBuilder();
      Iterator<byte[]> iter = this.statementComponents.iterator();

      while(iter.hasNext()) {
         buf.append(StringUtils.toString(iter.next()));
      }

      return buf.toString();
   }
}
