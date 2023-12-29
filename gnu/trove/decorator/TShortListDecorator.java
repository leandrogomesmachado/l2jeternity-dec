package gnu.trove.decorator;

import gnu.trove.list.TShortList;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.AbstractList;
import java.util.List;

public class TShortListDecorator extends AbstractList<Short> implements List<Short>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TShortList list;

   public TShortListDecorator() {
   }

   public TShortListDecorator(TShortList list) {
      this.list = list;
   }

   public TShortList getList() {
      return this.list;
   }

   @Override
   public int size() {
      return this.list.size();
   }

   public Short get(int index) {
      short value = this.list.get(index);
      return value == this.list.getNoEntryValue() ? null : value;
   }

   public Short set(int index, Short value) {
      short previous_value = this.list.set(index, value);
      return previous_value == this.list.getNoEntryValue() ? null : previous_value;
   }

   public void add(int index, Short value) {
      this.list.insert(index, value);
   }

   public Short remove(int index) {
      short previous_value = this.list.removeAt(index);
      return previous_value == this.list.getNoEntryValue() ? null : previous_value;
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      this.list = (TShortList)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this.list);
   }
}
