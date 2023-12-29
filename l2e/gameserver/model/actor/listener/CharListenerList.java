package l2e.gameserver.model.actor.listener;

import l2e.commons.listener.Listener;
import l2e.commons.listener.ListenerList;
import l2e.gameserver.model.actor.Creature;

public class CharListenerList extends ListenerList<Creature> {
   static final ListenerList<Creature> _global = new ListenerList<>();
   protected final Creature _actor;

   public CharListenerList(Creature actor) {
      this._actor = actor;
   }

   public Creature getActor() {
      return this._actor;
   }

   public static final boolean addGlobal(Listener<Creature> listener) {
      return _global.add(listener);
   }

   public static final boolean removeGlobal(Listener<Creature> listener) {
      return _global.remove(listener);
   }
}
