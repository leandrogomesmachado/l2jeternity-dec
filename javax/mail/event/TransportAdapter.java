package javax.mail.event;

public abstract class TransportAdapter implements TransportListener {
   @Override
   public void messageDelivered(TransportEvent e) {
   }

   @Override
   public void messageNotDelivered(TransportEvent e) {
   }

   @Override
   public void messagePartiallyDelivered(TransportEvent e) {
   }
}
