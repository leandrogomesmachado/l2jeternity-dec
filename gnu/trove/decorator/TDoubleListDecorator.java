package gnu.trove.decorator;

import gnu.trove.list.TDoubleList;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.AbstractList;
import java.util.List;

public class TDoubleListDecorator extends AbstractList<Double> implements List<Double>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TDoubleList list;

   public TDoubleListDecorator() {
   }

   public TDoubleListDecorator(TDoubleList list) {
      this.list = list;
   }

   public TDoubleList getList() {
      return this.list;
   }

   @Override
   public int size() {
      return this.list.size();
   }

   public Double get(int index) {
      double value = this.list.get(index);
      return value == this.list.getNoEntryValue() ? null : value;
   }

   public Double set(int index, Double value) {
      double previous_value = this.list.set(index, value);
      return previous_value == this.list.getNoEntryValue() ? null : previous_value;
   }

   public void add(int index, Double value) {
      this.list.insert(index, value);
   }

   public Double remove(int index) {
      double previous_value = this.list.removeAt(index);
      return previous_value == this.list.getNoEntryValue() ? null : previous_value;
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      this.list = (TDoubleList)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this.list);
   }
}
