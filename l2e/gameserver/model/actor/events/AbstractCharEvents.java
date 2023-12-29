package l2e.gameserver.model.actor.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import l2e.gameserver.model.actor.events.listeners.IEventListener;

public abstract class AbstractCharEvents {
   private static volatile List<IEventListener> _staticListeners = null;
   private volatile List<IEventListener> _listeners = null;

   public static void registerStaticListener(IEventListener listener) {
      if (_staticListeners == null) {
         synchronized(AbstractCharEvents.class) {
            if (_staticListeners == null) {
               _staticListeners = new CopyOnWriteArrayList<>();
            }
         }
      }

      _staticListeners.add(listener);
   }

   public final void registerListener(IEventListener listener) {
      if (this._listeners == null) {
         synchronized(this) {
            if (this._listeners == null) {
               this._listeners = new CopyOnWriteArrayList<>();
            }
         }
      }

      this._listeners.add(listener);
   }

   public static void unregisterStaticListener(IEventListener listener) {
      if (_staticListeners != null) {
         if (_staticListeners.contains(listener)) {
            _staticListeners.remove(listener);
         }

         if (_staticListeners.isEmpty()) {
            synchronized(AbstractCharEvents.class) {
               if (_staticListeners.isEmpty()) {
                  _staticListeners = null;
               }
            }
         }
      }
   }

   public final void unregisterListener(IEventListener listener) {
      if (this._listeners != null) {
         if (this._listeners.contains(listener)) {
            this._listeners.remove(listener);
         }

         if (this._listeners.isEmpty()) {
            synchronized(this) {
               if (this._listeners.isEmpty()) {
                  this._listeners = null;
               }
            }
         }
      }
   }

   protected static boolean hasStaticEventListeners() {
      return _staticListeners != null;
   }

   protected final boolean hasEventListeners() {
      return this._listeners != null;
   }

   protected final boolean hasListeners() {
      return this._listeners != null || _staticListeners != null;
   }

   protected final <T> List<T> getEventListeners(Class<T> clazz) {
      if (!this.hasListeners()) {
         return Collections.emptyList();
      } else {
         List<T> listeners = new ArrayList<>();
         if (this.hasEventListeners()) {
            for(IEventListener listener : this._listeners) {
               if (clazz.isInstance(listener)) {
                  listeners.add(clazz.cast(listener));
               }
            }
         }

         if (hasStaticEventListeners()) {
            for(IEventListener listener : _staticListeners) {
               if (clazz.isInstance(listener)) {
                  listeners.add(clazz.cast(listener));
               }
            }
         }

         return listeners;
      }
   }
}
