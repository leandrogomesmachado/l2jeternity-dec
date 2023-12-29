package gnu.trove.stack.array;

import gnu.trove.list.array.TShortArrayList;
import gnu.trove.stack.TShortStack;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class TShortArrayStack implements TShortStack, Externalizable {
   static final long serialVersionUID = 1L;
   protected TShortArrayList _list;
   public static final int DEFAULT_CAPACITY = 10;

   public TShortArrayStack() {
      this(10);
   }

   public TShortArrayStack(int capacity) {
      this._list = new TShortArrayList(capacity);
   }

   public TShortArrayStack(int capacity, short no_entry_value) {
      this._list = new TShortArrayList(capacity, no_entry_value);
   }

   public TShortArrayStack(TShortStack stack) {
      if (stack instanceof TShortArrayStack) {
         TShortArrayStack array_stack = (TShortArrayStack)stack;
         this._list = new TShortArrayList(array_stack._list);
      } else {
         throw new UnsupportedOperationException("Only support TShortArrayStack");
      }
   }

   @Override
   public short getNoEntryValue() {
      return this._list.getNoEntryValue();
   }

   @Override
   public void push(short val) {
      this._list.add(val);
   }

   @Override
   public short pop() {
      return this._list.removeAt(this._list.size() - 1);
   }

   @Override
   public short peek() {
      return this._list.get(this._list.size() - 1);
   }

   @Override
   public int size() {
      return this._list.size();
   }

   @Override
   public void clear() {
      this._list.clear();
   }

   @Override
   public short[] toArray() {
      short[] retval = this._list.toArray();
      this.reverse(retval, 0, this.size());
      return retval;
   }

   @Override
   public void toArray(short[] dest) {
      int size = this.size();
      int start = size - dest.length;
      if (start < 0) {
         start = 0;
      }

      int length = Math.min(size, dest.length);
      this._list.toArray(dest, start, length);
      this.reverse(dest, 0, length);
      if (dest.length > size) {
         dest[size] = this._list.getNoEntryValue();
      }
   }

   private void reverse(short[] dest, int from, int to) {
      if (from != to) {
         if (from > to) {
            throw new IllegalArgumentException("from cannot be greater than to");
         } else {
            int i = from;

            for(int j = to - 1; i < j; --j) {
               this.swap(dest, i, j);
               ++i;
            }
         }
      }
   }

   private void swap(short[] dest, int i, int j) {
      short tmp = dest[i];
      dest[i] = dest[j];
      dest[j] = tmp;
   }

   @Override
   public String toString() {
      StringBuilder buf = new StringBuilder("{");

      for(int i = this._list.size() - 1; i > 0; --i) {
         buf.append(this._list.get(i));
         buf.append(", ");
      }

      if (this.size() > 0) {
         buf.append(this._list.get(0));
      }

      buf.append("}");
      return buf.toString();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         TShortArrayStack that = (TShortArrayStack)o;
         return this._list.equals(that._list);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return this._list.hashCode();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this._list);
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      this._list = (TShortArrayList)in.readObject();
   }
}
