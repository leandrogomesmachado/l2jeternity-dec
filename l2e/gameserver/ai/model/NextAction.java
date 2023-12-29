package l2e.gameserver.ai.model;

import java.util.ArrayList;
import java.util.List;

public class NextAction {
   private List<CtrlEvent> _events;
   private List<CtrlIntention> _intentions;
   private List<NextAction.NextActionCallback> _callback;

   public NextAction(ArrayList<CtrlEvent> events, ArrayList<CtrlIntention> intentions, NextAction.NextActionCallback callback) {
      this._events = events;
      this._intentions = intentions;
      this.addCallback(callback);
   }

   public NextAction(CtrlEvent event, CtrlIntention intention, NextAction.NextActionCallback callback) {
      if (this._events == null) {
         this._events = new ArrayList<>();
      }

      if (this._intentions == null) {
         this._intentions = new ArrayList<>();
      }

      if (event != null) {
         this._events.add(event);
      }

      if (intention != null) {
         this._intentions.add(intention);
      }

      this.addCallback(callback);
   }

   public void doAction() {
      if (this._callback != null && !this._callback.isEmpty()) {
         for(NextAction.NextActionCallback action : this._callback) {
            action.doWork();
         }
      }
   }

   public List<CtrlEvent> getEvents() {
      if (this._events == null) {
         this._events = new ArrayList<>();
      }

      return this._events;
   }

   public void setEvents(ArrayList<CtrlEvent> event) {
      this._events = event;
   }

   public void addEvent(CtrlEvent event) {
      if (this._events == null) {
         this._events = new ArrayList<>();
      }

      if (event != null) {
         this._events.add(event);
      }
   }

   public void removeEvent(CtrlEvent event) {
      if (this._events != null) {
         this._events.remove(event);
      }
   }

   public List<NextAction.NextActionCallback> getCallback() {
      return this._callback;
   }

   public void addCallback(NextAction.NextActionCallback callback) {
      if (this._callback == null) {
         this._callback = new ArrayList<>();
      }

      this._callback.add(callback);
   }

   public List<CtrlIntention> getIntentions() {
      if (this._intentions == null) {
         this._intentions = new ArrayList<>();
      }

      return this._intentions;
   }

   public void setIntentions(ArrayList<CtrlIntention> intentions) {
      this._intentions = intentions;
   }

   public void addIntention(CtrlIntention intention) {
      if (this._intentions == null) {
         this._intentions = new ArrayList<>();
      }

      if (intention != null) {
         this._intentions.add(intention);
      }
   }

   public void removeIntention(CtrlIntention intention) {
      if (this._intentions != null) {
         this._intentions.remove(intention);
      }
   }

   public interface NextActionCallback {
      void doWork();
   }
}
