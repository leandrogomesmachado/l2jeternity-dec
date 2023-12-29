package com.mysql.cj.protocol.x;

import com.mysql.cj.protocol.ColumnDefinition;
import com.mysql.cj.result.RowList;
import java.util.NoSuchElementException;

public class XProtocolRowInputStream implements RowList {
   private ColumnDefinition metadata;
   private XProtocol protocol;
   private boolean isDone = false;
   private int position = -1;
   private XProtocolRow next;

   public XProtocolRowInputStream(ColumnDefinition metadata, XProtocol protocol) {
      this.metadata = metadata;
      this.protocol = protocol;
   }

   public XProtocolRow readRow() {
      if (!this.hasNext()) {
         this.isDone = true;
         return null;
      } else {
         ++this.position;
         XProtocolRow r = this.next;
         this.next = null;
         return r;
      }
   }

   public XProtocolRow next() {
      if (!this.hasNext()) {
         throw new NoSuchElementException();
      } else {
         return this.readRow();
      }
   }

   @Override
   public boolean hasNext() {
      if (this.isDone) {
         return false;
      } else {
         if (this.next == null) {
            this.next = this.protocol.readRowOrNull(this.metadata);
         }

         return this.next != null;
      }
   }

   @Override
   public int getPosition() {
      return this.position;
   }
}
