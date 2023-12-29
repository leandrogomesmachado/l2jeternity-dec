package gnu.trove.decorator;

import gnu.trove.list.TFloatList;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.AbstractList;
import java.util.List;

public class TFloatListDecorator extends AbstractList<Float> implements List<Float>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TFloatList list;

   public TFloatListDecorator() {
   }

   public TFloatListDecorator(TFloatList list) {
      this.list = list;
   }

   public TFloatList getList() {
      return this.list;
   }

   @Override
   public int size() {
      return this.list.size();
   }

   public Float get(int index) {
      float value = this.list.get(index);
      return value == this.list.getNoEntryValue() ? null : value;
   }

   public Float set(int index, Float value) {
      float previous_value = this.list.set(index, value);
      return previous_value == this.list.getNoEntryValue() ? null : previous_value;
   }

   public void add(int index, Float value) {
      this.list.insert(index, value);
   }

   public Float remove(int index) {
      float previous_value = this.list.removeAt(index);
      return previous_value == this.list.getNoEntryValue() ? null : previous_value;
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      this.list = (TFloatList)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this.list);
   }
}
