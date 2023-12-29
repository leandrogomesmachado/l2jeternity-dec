package javax.mail.event;

public abstract class ConnectionAdapter implements ConnectionListener {
   @Override
   public void opened(ConnectionEvent e) {
   }

   @Override
   public void disconnected(ConnectionEvent e) {
   }

   @Override
   public void closed(ConnectionEvent e) {
   }
}
