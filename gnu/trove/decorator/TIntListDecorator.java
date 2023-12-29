package gnu.trove.decorator;

import gnu.trove.list.TIntList;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.AbstractList;
import java.util.List;

public class TIntListDecorator extends AbstractList<Integer> implements List<Integer>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TIntList list;

   public TIntListDecorator() {
   }

   public TIntListDecorator(TIntList list) {
      this.list = list;
   }

   public TIntList getList() {
      return this.list;
   }

   @Override
   public int size() {
      return this.list.size();
   }

   public Integer get(int index) {
      int value = this.list.get(index);
      return value == this.list.getNoEntryValue() ? null : value;
   }

   public Integer set(int index, Integer value) {
      int previous_value = this.list.set(index, value);
      return previous_value == this.list.getNoEntryValue() ? null : previous_value;
   }

   public void add(int index, Integer value) {
      this.list.insert(index, value);
   }

   public Integer remove(int index) {
      int previous_value = this.list.removeAt(index);
      return previous_value == this.list.getNoEntryValue() ? null : previous_value;
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      this.list = (TIntList)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this.list);
   }
}
