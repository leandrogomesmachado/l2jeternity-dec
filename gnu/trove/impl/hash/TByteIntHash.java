package gnu.trove.impl.hash;

import gnu.trove.impl.HashFunctions;
import gnu.trove.procedure.TByteProcedure;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public abstract class TByteIntHash extends TPrimitiveHash {
   static final long serialVersionUID = 1L;
   public transient byte[] _set;
   protected byte no_entry_key;
   protected int no_entry_value;
   protected boolean consumeFreeSlot;

   public TByteIntHash() {
      this.no_entry_key = 0;
      this.no_entry_value = 0;
   }

   public TByteIntHash(int initialCapacity) {
      super(initialCapacity);
      this.no_entry_key = 0;
      this.no_entry_value = 0;
   }

   public TByteIntHash(int initialCapacity, float loadFactor) {
      super(initialCapacity, loadFactor);
      this.no_entry_key = 0;
      this.no_entry_value = 0;
   }

   public TByteIntHash(int initialCapacity, float loadFactor, byte no_entry_key, int no_entry_value) {
      super(initialCapacity, loadFactor);
      this.no_entry_key = no_entry_key;
      this.no_entry_value = no_entry_value;
   }

   public byte getNoEntryKey() {
      return this.no_entry_key;
   }

   public int getNoEntryValue() {
      return this.no_entry_value;
   }

   @Override
   protected int setUp(int initialCapacity) {
      int capacity = super.setUp(initialCapacity);
      this._set = new byte[capacity];
      return capacity;
   }

   public boolean contains(byte val) {
      return this.index(val) >= 0;
   }

   public boolean forEach(TByteProcedure procedure) {
      byte[] states = this._states;
      byte[] set = this._set;
      int i = set.length;

      while(i-- > 0) {
         if (states[i] == 1 && !procedure.execute(set[i])) {
            return false;
         }
      }

      return true;
   }

   @Override
   protected void removeAt(int index) {
      this._set[index] = this.no_entry_key;
      super.removeAt(index);
   }

   protected int index(byte key) {
      byte[] states = this._states;
      byte[] set = this._set;
      int length = states.length;
      int hash = HashFunctions.hash(key) & 2147483647;
      int index = hash % length;
      byte state = states[index];
      if (state == 0) {
         return -1;
      } else {
         return state == 1 && set[index] == key ? index : this.indexRehashed(key, index, hash, state);
      }
   }

   int indexRehashed(byte key, int index, int hash, byte state) {
      int length = this._set.length;
      int probe = 1 + hash % (length - 2);
      int loopIndex = index;

      do {
         index -= probe;
         if (index < 0) {
            index += length;
         }

         state = this._states[index];
         if (state == 0) {
            return -1;
         }

         if (key == this._set[index] && state != 2) {
            return index;
         }
      } while(index != loopIndex);

      return -1;
   }

   protected int insertKey(byte val) {
      int hash = HashFunctions.hash(val) & 2147483647;
      int index = hash % this._states.length;
      byte state = this._states[index];
      this.consumeFreeSlot = false;
      if (state == 0) {
         this.consumeFreeSlot = true;
         this.insertKeyAt(index, val);
         return index;
      } else {
         return state == 1 && this._set[index] == val ? -index - 1 : this.insertKeyRehash(val, index, hash, state);
      }
   }

   int insertKeyRehash(byte val, int index, int hash, byte state) {
      int length = this._set.length;
      int probe = 1 + hash % (length - 2);
      int loopIndex = index;
      int firstRemoved = -1;

      do {
         if (state == 2 && firstRemoved == -1) {
            firstRemoved = index;
         }

         index -= probe;
         if (index < 0) {
            index += length;
         }

         state = this._states[index];
         if (state == 0) {
            if (firstRemoved != -1) {
               this.insertKeyAt(firstRemoved, val);
               return firstRemoved;
            }

            this.consumeFreeSlot = true;
            this.insertKeyAt(index, val);
            return index;
         }

         if (state == 1 && this._set[index] == val) {
            return -index - 1;
         }
      } while(index != loopIndex);

      if (firstRemoved != -1) {
         this.insertKeyAt(firstRemoved, val);
         return firstRemoved;
      } else {
         throw new IllegalStateException("No free or removed slots available. Key set full?!!");
      }
   }

   void insertKeyAt(int index, byte val) {
      this._set[index] = val;
      this._states[index] = 1;
   }

   protected int XinsertKey(byte key) {
      byte[] states = this._states;
      byte[] set = this._set;
      int length = states.length;
      int hash = HashFunctions.hash(key) & 2147483647;
      int index = hash % length;
      byte state = states[index];
      this.consumeFreeSlot = false;
      if (state == 0) {
         this.consumeFreeSlot = true;
         set[index] = key;
         states[index] = 1;
         return index;
      } else if (state == 1 && set[index] == key) {
         return -index - 1;
      } else {
         int probe = 1 + hash % (length - 2);
         if (state != 2) {
            do {
               index -= probe;
               if (index < 0) {
                  index += length;
               }

               state = states[index];
            } while(state == 1 && set[index] != key);
         }

         if (state != 2) {
            if (state == 1) {
               return -index - 1;
            } else {
               this.consumeFreeSlot = true;
               set[index] = key;
               states[index] = 1;
               return index;
            }
         } else {
            int firstRemoved;
            for(firstRemoved = index; state != 0 && (state == 2 || set[index] != key); state = states[index]) {
               index -= probe;
               if (index < 0) {
                  index += length;
               }
            }

            if (state == 1) {
               return -index - 1;
            } else {
               set[index] = key;
               states[index] = 1;
               return firstRemoved;
            }
         }
      }
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      super.writeExternal(out);
      out.writeByte(this.no_entry_key);
      out.writeInt(this.no_entry_value);
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      super.readExternal(in);
      this.no_entry_key = in.readByte();
      this.no_entry_value = in.readInt();
   }
}
