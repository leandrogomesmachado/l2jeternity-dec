package org.apache.commons.io.output;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class ByteArrayOutputStream extends OutputStream {
   private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
   private List buffers = new ArrayList();
   private int currentBufferIndex;
   private int filledBufferSum;
   private byte[] currentBuffer;
   private int count;

   public ByteArrayOutputStream() {
      this(1024);
   }

   public ByteArrayOutputStream(int size) {
      if (size < 0) {
         throw new IllegalArgumentException("Negative initial size: " + size);
      } else {
         this.needNewBuffer(size);
      }
   }

   private byte[] getBuffer(int index) {
      return (byte[])this.buffers.get(index);
   }

   private void needNewBuffer(int newcount) {
      if (this.currentBufferIndex < this.buffers.size() - 1) {
         this.filledBufferSum += this.currentBuffer.length;
         ++this.currentBufferIndex;
         this.currentBuffer = this.getBuffer(this.currentBufferIndex);
      } else {
         int newBufferSize;
         if (this.currentBuffer == null) {
            newBufferSize = newcount;
            this.filledBufferSum = 0;
         } else {
            newBufferSize = Math.max(this.currentBuffer.length << 1, newcount - this.filledBufferSum);
            this.filledBufferSum += this.currentBuffer.length;
         }

         ++this.currentBufferIndex;
         this.currentBuffer = new byte[newBufferSize];
         this.buffers.add(this.currentBuffer);
      }
   }

   public void write(byte[] b, int off, int len) {
      if (off >= 0 && off <= b.length && len >= 0 && off + len <= b.length && off + len >= 0) {
         if (len != 0) {
            synchronized(this) {
               int newcount = this.count + len;
               int remaining = len;
               int inBufferPos = this.count - this.filledBufferSum;

               while(remaining > 0) {
                  int part = Math.min(remaining, this.currentBuffer.length - inBufferPos);
                  System.arraycopy(b, off + len - remaining, this.currentBuffer, inBufferPos, part);
                  remaining -= part;
                  if (remaining > 0) {
                     this.needNewBuffer(newcount);
                     inBufferPos = 0;
                  }
               }

               this.count = newcount;
            }
         }
      } else {
         throw new IndexOutOfBoundsException();
      }
   }

   public synchronized void write(int b) {
      int inBufferPos = this.count - this.filledBufferSum;
      if (inBufferPos == this.currentBuffer.length) {
         this.needNewBuffer(this.count + 1);
         inBufferPos = 0;
      }

      this.currentBuffer[inBufferPos] = (byte)b;
      ++this.count;
   }

   public synchronized int write(InputStream in) throws IOException {
      int readCount = 0;
      int inBufferPos = this.count - this.filledBufferSum;

      for(int n = in.read(this.currentBuffer, inBufferPos, this.currentBuffer.length - inBufferPos);
         n != -1;
         n = in.read(this.currentBuffer, inBufferPos, this.currentBuffer.length - inBufferPos)
      ) {
         readCount += n;
         inBufferPos += n;
         this.count += n;
         if (inBufferPos == this.currentBuffer.length) {
            this.needNewBuffer(this.currentBuffer.length);
            inBufferPos = 0;
         }
      }

      return readCount;
   }

   public synchronized int size() {
      return this.count;
   }

   public void close() throws IOException {
   }

   public synchronized void reset() {
      this.count = 0;
      this.filledBufferSum = 0;
      this.currentBufferIndex = 0;
      this.currentBuffer = this.getBuffer(this.currentBufferIndex);
   }

   public synchronized void writeTo(OutputStream out) throws IOException {
      int remaining = this.count;

      for(int i = 0; i < this.buffers.size(); ++i) {
         byte[] buf = this.getBuffer(i);
         int c = Math.min(buf.length, remaining);
         out.write(buf, 0, c);
         remaining -= c;
         if (remaining == 0) {
            break;
         }
      }
   }

   public synchronized byte[] toByteArray() {
      int remaining = this.count;
      if (remaining == 0) {
         return EMPTY_BYTE_ARRAY;
      } else {
         byte[] newbuf = new byte[remaining];
         int pos = 0;

         for(int i = 0; i < this.buffers.size(); ++i) {
            byte[] buf = this.getBuffer(i);
            int c = Math.min(buf.length, remaining);
            System.arraycopy(buf, 0, newbuf, pos, c);
            pos += c;
            remaining -= c;
            if (remaining == 0) {
               break;
            }
         }

         return newbuf;
      }
   }

   public String toString() {
      return new String(this.toByteArray());
   }

   public String toString(String enc) throws UnsupportedEncodingException {
      return new String(this.toByteArray(), enc);
   }
}
