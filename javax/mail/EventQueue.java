package javax.mail;

import java.util.Vector;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import javax.mail.event.MailEvent;

class EventQueue implements Runnable {
   private volatile BlockingQueue<EventQueue.QueueElement> q;
   private Executor executor;
   private static WeakHashMap<ClassLoader, EventQueue> appq;

   EventQueue(Executor ex) {
      this.executor = ex;
   }

   synchronized void enqueue(MailEvent event, Vector vector) {
      if (this.q == null) {
         this.q = new LinkedBlockingQueue<>();
         if (this.executor != null) {
            this.executor.execute(this);
         } else {
            Thread qThread = new Thread(this, "JavaMail-EventQueue");
            qThread.setDaemon(true);
            qThread.start();
         }
      }

      this.q.add(new EventQueue.QueueElement(event, vector));
   }

   synchronized void terminateQueue() {
      if (this.q != null) {
         Vector dummyListeners = new Vector();
         dummyListeners.setSize(1);
         this.q.add(new EventQueue.QueueElement(new EventQueue.TerminatorEvent(), dummyListeners));
         this.q = null;
      }
   }

   static synchronized EventQueue getApplicationEventQueue(Executor ex) {
      ClassLoader cl = Session.getContextClassLoader();
      if (appq == null) {
         appq = new WeakHashMap<>();
      }

      EventQueue q = appq.get(cl);
      if (q == null) {
         q = new EventQueue(ex);
         appq.put(cl, q);
      }

      return q;
   }

   @Override
   public void run() {
      BlockingQueue<EventQueue.QueueElement> bq = this.q;
      if (bq != null) {
         label34:
         while(true) {
            try {
               EventQueue.QueueElement qe = bq.take();
               MailEvent e = qe.event;
               Vector v = qe.vector;
               int i = 0;

               while(true) {
                  if (i >= v.size()) {
                     EventQueue.QueueElement var9 = null;
                     MailEvent var10 = null;
                     v = null;
                     continue label34;
                  }

                  try {
                     e.dispatch(v.elementAt(i));
                  } catch (Throwable var7) {
                     if (var7 instanceof InterruptedException) {
                        break;
                     }
                  }

                  ++i;
               }
            } catch (InterruptedException var8) {
            }

            return;
         }
      }
   }

   static class QueueElement {
      MailEvent event = null;
      Vector vector = null;

      QueueElement(MailEvent event, Vector vector) {
         this.event = event;
         this.vector = vector;
      }
   }

   static class TerminatorEvent extends MailEvent {
      TerminatorEvent() {
         super(new Object());
      }

      @Override
      public void dispatch(Object listener) {
         Thread.currentThread().interrupt();
      }
   }
}
