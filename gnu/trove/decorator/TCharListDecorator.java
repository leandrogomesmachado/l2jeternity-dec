package gnu.trove.decorator;

import gnu.trove.list.TCharList;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.AbstractList;
import java.util.List;

public class TCharListDecorator extends AbstractList<Character> implements List<Character>, Externalizable, Cloneable {
   static final long serialVersionUID = 1L;
   protected TCharList list;

   public TCharListDecorator() {
   }

   public TCharListDecorator(TCharList list) {
      this.list = list;
   }

   public TCharList getList() {
      return this.list;
   }

   @Override
   public int size() {
      return this.list.size();
   }

   public Character get(int index) {
      char value = this.list.get(index);
      return value == this.list.getNoEntryValue() ? null : value;
   }

   public Character set(int index, Character value) {
      char previous_value = this.list.set(index, value);
      return previous_value == this.list.getNoEntryValue() ? null : previous_value;
   }

   public void add(int index, Character value) {
      this.list.insert(index, value);
   }

   public Character remove(int index) {
      char previous_value = this.list.removeAt(index);
      return previous_value == this.list.getNoEntryValue() ? null : previous_value;
   }

   @Override
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
      in.readByte();
      this.list = (TCharList)in.readObject();
   }

   @Override
   public void writeExternal(ObjectOutput out) throws IOException {
      out.writeByte(0);
      out.writeObject(this.list);
   }
}
