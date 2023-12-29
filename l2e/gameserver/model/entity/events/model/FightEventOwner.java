package l2e.gameserver.model.entity.events.model;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import l2e.gameserver.model.entity.events.AbstractFightEvent;

public abstract class FightEventOwner {
   private final Set<AbstractFightEvent> _events = new CopyOnWriteArraySet<>();

   public <E extends AbstractFightEvent> E getEvent(Class<E> eventClass) {
      for(AbstractFightEvent e : this._events) {
         if (e.getClass() == eventClass) {
            return (E)e;
         }

         if (eventClass.isAssignableFrom(e.getClass())) {
            return (E)e;
         }
      }

      return null;
   }

   public void addEvent(AbstractFightEvent event) {
      this._events.add(event);
   }

   public void removeEvent(AbstractFightEvent event) {
      this._events.remove(event);
   }

   public Set<AbstractFightEvent> getFightEvents() {
      return this._events;
   }
}
