package l2e.commons.listener;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class ListenerList<T> {
   protected Set<Listener<T>> listeners = new CopyOnWriteArraySet<>();

   public Collection<Listener<T>> getListeners() {
      return this.listeners;
   }

   public boolean add(Listener<T> listener) {
      return this.listeners.add(listener);
   }

   public boolean remove(Listener<T> listener) {
      return this.listeners.remove(listener);
   }
}
