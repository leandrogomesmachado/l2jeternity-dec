package gnu.trove.stack.array;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.stack.TIntStack;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class TIntArrayStack implements TIntStack, Externalizable {
   static final long serialVersionUID = 1L;
   protected TIntArrayList _list;
   public static final int DEFAULT_CAPACITY = 10;

   public TIntArrayStack() {
      this(10);
   }

   public TIntArrayStack(int capacity) {
      this._list = new TIntArrayList(capacity);
   }

   public TIntArrayStack(int capacity, int no_entry_value) {
      this._list = new TIntArrayList(capacity, no_entry_value);
   }

   public TIntArrayStack(TIntStack stack) {
      if (stack instanceof TIntArrayStack) {
         TIntArrayStack array_stack = (TIntArrayStack)stack;
         this._list = new TIntArrayList(array_stack._list);
      } else {
         throw new UnsupportedOperationException("Only support TIntArrayStack");
      }
   }

   @Override
   public int getNoEntryValue() {
      return this._list.getNoEntryValue();
   }

   @Override
   public void push(int val) {
      this._list.add(val);
   }

   @Override
   public int pop() {
      return this._list.removeAt(this._list.size() - 1);
   }

   @Override
   public int peek() {
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
   public int[] toArray() {
      int[] retval = this._list.toArray();
      this.reverse(retval, 0, this.size());
      return retval;
   }

   @Override
   public void toArray(int[] dest) {
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

   private void reverse(int[] dest, int from, int to) {
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

   private void swap(int[] dest, int i, int j) {
      int tmp = dest[i];
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
         TIntArrayStack that = (TIntArrayStack)o;
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
      this._list = (TIntArrayList)in.readObject();
   }
}
