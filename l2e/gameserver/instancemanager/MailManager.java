package l2e.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.database.DatabaseFactory;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.tasks.MessageDeletionTask;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Message;
import l2e.gameserver.network.serverpackets.ExNoticePostArrived;

public final class MailManager {
   protected static final Logger _log = Logger.getLogger(MailManager.class.getName());
   private final Map<Integer, Message> _messages = new ConcurrentHashMap<>();

   protected MailManager() {
      this.load();
   }

   private void load() {
      int count = 0;

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement statement = con.prepareStatement("SELECT * FROM messages ORDER BY expiration");
         ResultSet rset1 = statement.executeQuery();

         while(rset1.next()) {
            Message msg = new Message(rset1);
            int msgId = msg.getId();
            this._messages.put(msgId, msg);
            ++count;
            long expiration = msg.getExpiration();
            if (expiration < System.currentTimeMillis()) {
               ThreadPoolManager.getInstance().schedule(new MessageDeletionTask(msgId), 10000L);
            } else {
               ThreadPoolManager.getInstance().schedule(new MessageDeletionTask(msgId), expiration - System.currentTimeMillis());
            }
         }

         rset1.close();
         statement.close();
      } catch (SQLException var20) {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Error loading from database:" + var20.getMessage(), (Throwable)var20);
      }

      _log.info(this.getClass().getSimpleName() + ": Successfully loaded " + count + " messages.");
   }

   public final Message getMessage(int msgId) {
      return this._messages.get(msgId);
   }

   public final Collection<Message> getMessages() {
      return this._messages.values();
   }

   public final boolean hasUnreadPost(Player player) {
      int objectId = player.getObjectId();

      for(Message msg : this.getMessages()) {
         if (msg != null && msg.getReceiverId() == objectId && msg.isUnread()) {
            return true;
         }
      }

      return false;
   }

   public final int getInboxSize(int objectId) {
      int size = 0;

      for(Message msg : this.getMessages()) {
         if (msg != null && msg.getReceiverId() == objectId && !msg.isDeletedByReceiver()) {
            ++size;
         }
      }

      return size;
   }

   public final int getOutboxSize(int objectId) {
      int size = 0;

      for(Message msg : this.getMessages()) {
         if (msg != null && msg.getSenderId() == objectId && !msg.isDeletedBySender()) {
            ++size;
         }
      }

      return size;
   }

   public final List<Message> getInbox(int objectId) {
      List<Message> inbox = new ArrayList<>();

      for(Message msg : this.getMessages()) {
         if (msg != null && msg.getReceiverId() == objectId && !msg.isDeletedByReceiver()) {
            inbox.add(msg);
         }
      }

      return inbox;
   }

   public final List<Message> getOutbox(int objectId) {
      List<Message> outbox = new ArrayList<>();

      for(Message msg : this.getMessages()) {
         if (msg != null && msg.getSenderId() == objectId && !msg.isDeletedBySender()) {
            outbox.add(msg);
         }
      }

      return outbox;
   }

   public void sendMessage(Message msg) {
      this._messages.put(msg.getId(), msg);

      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement stmt = Message.getStatement(msg, con);
         stmt.execute();
         stmt.close();
      } catch (SQLException var15) {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Error saving message:" + var15.getMessage(), (Throwable)var15);
      }

      Player receiver = World.getInstance().getPlayer(msg.getReceiverId());
      if (receiver != null) {
         receiver.sendPacket(ExNoticePostArrived.valueOf(true));
      }

      ThreadPoolManager.getInstance().schedule(new MessageDeletionTask(msg.getId()), msg.getExpiration() - System.currentTimeMillis());
   }

   public final void markAsReadInDb(int msgId) {
      try (Connection con = DatabaseFactory.getInstance().getConnection()) {
         PreparedStatement stmt = con.prepareStatement("UPDATE messages SET isUnread = 'false' WHERE messageId = ?");
         stmt.setInt(1, msgId);
         stmt.execute();
         stmt.close();
      } catch (SQLException var15) {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Error marking as read message:" + var15.getMessage(), (Throwable)var15);
      }
   }

   public final void markAsDeletedBySenderInDb(int msgId) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement stmt = con.prepareStatement("UPDATE messages SET isDeletedBySender = 'true' WHERE messageId = ?");
      ) {
         stmt.setInt(1, msgId);
         stmt.execute();
      } catch (SQLException var34) {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Error marking as deleted by sender message:" + var34.getMessage(), (Throwable)var34);
      }
   }

   public final void markAsDeletedByReceiverInDb(int msgId) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement stmt = con.prepareStatement("UPDATE messages SET isDeletedByReceiver = 'true' WHERE messageId = ?");
      ) {
         stmt.setInt(1, msgId);
         stmt.execute();
      } catch (SQLException var34) {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Error marking as deleted by receiver message:" + var34.getMessage(), (Throwable)var34);
      }
   }

   public final void removeAttachmentsInDb(int msgId) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement stmt = con.prepareStatement("UPDATE messages SET hasAttachments = 'false' WHERE messageId = ?");
      ) {
         stmt.setInt(1, msgId);
         stmt.execute();
      } catch (SQLException var34) {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Error removing attachments in message:" + var34.getMessage(), (Throwable)var34);
      }
   }

   public final void deleteMessageInDb(int msgId) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement stmt = con.prepareStatement("DELETE FROM messages WHERE messageId = ?");
      ) {
         stmt.setInt(1, msgId);
         stmt.execute();
      } catch (SQLException var34) {
         _log.log(Level.WARNING, this.getClass().getSimpleName() + ": Error deleting message:" + var34.getMessage(), (Throwable)var34);
      }

      this._messages.remove(msgId);
      IdFactory.getInstance().releaseId(msgId);
   }

   public static MailManager getInstance() {
      return MailManager.SingletonHolder._instance;
   }

   private static class SingletonHolder {
      protected static final MailManager _instance = new MailManager();
   }
}
