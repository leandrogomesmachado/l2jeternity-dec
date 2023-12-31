package gnu.trove.decorator;

import gnu.trove.list.TLongList;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.AbstractList;
import java.util.List;

public class TLongListDecorator extends AbstractList<Long> implements List<Long>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TLongList list;

   public TLongListDecorator() {
   }

   public TLongListDecorator(TLongList list) {
      this.list = list;
   }

   public TLongList getList() {
      return this.list;
   }

   @Override
   public int size() {
      return this.list.size();
   }

   public Long get(int index) {
      long value = this.list.get(index);
      return value == this.list.getNoEntryValue() ? null : value;
   }

   public Long set(int index, Long value) {
      long previous_value = this.list.set(index, value);
      return previous_value == this.list.getNoEntryValue() ? null : previous_value;
   }

   public void add(int index, Long value) {
      this.list.insert(index, value);
   }

   public Long remove(int index) {
      long previous_value = this.list.removeAt(index);
      return previous_value == this.list.getNoEntryValue() ? null : previous_value;
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      this.list = (TLongList)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this.list);
   }
}
