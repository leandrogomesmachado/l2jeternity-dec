package gnu.trove.decorator;

import gnu.trove.list.TByteList;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.AbstractList;
import java.util.List;

public class TByteListDecorator extends AbstractList<Byte> implements List<Byte>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TByteList list;

   public TByteListDecorator() {
   }

   public TByteListDecorator(TByteList list) {
      this.list = list;
   }

   public TByteList getList() {
      return this.list;
   }

   @Override
   public int size() {
      return this.list.size();
   }

   public Byte get(int index) {
      byte value = this.list.get(index);
      return value == this.list.getNoEntryValue() ? null : value;
   }

   public Byte set(int index, Byte value) {
      byte previous_value = this.list.set(index, value);
      return previous_value == this.list.getNoEntryValue() ? null : previous_value;
   }

   public void add(int index, Byte value) {
      this.list.insert(index, value);
   }

   public Byte remove(int index) {
      byte previous_value = this.list.removeAt(index);
      return previous_value == this.list.getNoEntryValue() ? null : previous_value;
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      this.list = (TByteList)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this.list);
   }
}
