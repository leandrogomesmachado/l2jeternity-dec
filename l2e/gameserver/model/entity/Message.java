package l2e.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;
import l2e.commons.util.Rnd;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.data.holder.CharNameHolder;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.instancemanager.MailManager;
import l2e.gameserver.model.items.itemcontainer.Mail;

public class Message {
   private static final int EXPIRATION = Config.MAIL_EXPIRATION;
   private static final int COD_EXPIRATION = Config.MAIL_COND_EXPIRATION;
   private static final int UNLOAD_ATTACHMENTS_INTERVAL = 900000;
   public static final int DELETED = 0;
   public static final int READED = 1;
   public static final int REJECTED = 2;
   private final int _messageId;
   private final int _senderId;
   private final int _receiverId;
   private final long _expiration;
   private String _senderName = null;
   private String _receiverName = null;
   private final String _subject;
   private final String _content;
   private boolean _unread;
   private boolean _returned;
   private Message.SenderType _type = Message.SenderType.NORMAL;
   private boolean _deletedBySender;
   private boolean _deletedByReceiver;
   private final long _reqAdena;
   private boolean _hasAttachments;
   private Mail _attachments = null;
   private ScheduledFuture<?> _unloadTask = null;

   public Message(ResultSet rset) throws SQLException {
      this._messageId = rset.getInt("messageId");
      this._senderId = rset.getInt("senderId");
      this._receiverId = rset.getInt("receiverId");
      this._subject = rset.getString("subject");
      this._content = rset.getString("content");
      this._expiration = rset.getLong("expiration");
      this._reqAdena = rset.getLong("reqAdena");
      this._hasAttachments = rset.getBoolean("hasAttachments");
      this._unread = rset.getBoolean("isUnread");
      this._deletedBySender = rset.getBoolean("isDeletedBySender");
      this._deletedByReceiver = rset.getBoolean("isDeletedByReceiver");
      this._type = Message.SenderType.values()[rset.getInt("sendBySystem")];
      this._returned = rset.getBoolean("isReturned");
   }

   public Message(int senderId, int receiverId, boolean isCod, String subject, String text, long reqAdena) {
      this._messageId = IdFactory.getInstance().getNextId();
      this._senderId = senderId;
      this._receiverId = receiverId;
      this._subject = subject;
      this._content = text;
      this._expiration = isCod ? System.currentTimeMillis() + (long)(COD_EXPIRATION * 3600000) : System.currentTimeMillis() + (long)(EXPIRATION * 3600000);
      this._hasAttachments = false;
      this._unread = true;
      this._deletedBySender = false;
      this._deletedByReceiver = false;
      this._reqAdena = reqAdena;
      this._type = Message.SenderType.NORMAL;
   }

   public Message(int receiverId, String subject, String content, Message.SenderType sendBySystem) {
      this._messageId = IdFactory.getInstance().getNextId();
      this._senderId = -1;
      this._receiverId = receiverId;
      this._subject = subject;
      this._content = content;
      this._expiration = System.currentTimeMillis() + (long)(EXPIRATION * 3600000);
      this._reqAdena = 0L;
      this._hasAttachments = false;
      this._unread = true;
      this._deletedBySender = true;
      this._deletedByReceiver = false;
      this._type = sendBySystem;
      this._returned = false;
   }

   public Message(Message msg) {
      this._messageId = IdFactory.getInstance().getNextId();
      this._senderId = msg.getSenderId();
      this._receiverId = msg.getSenderId();
      this._subject = "";
      this._content = "";
      this._expiration = System.currentTimeMillis() + (long)(EXPIRATION * 3600000);
      this._unread = true;
      this._deletedBySender = true;
      this._deletedByReceiver = false;
      this._type = Message.SenderType.NORMAL;
      this._returned = true;
      this._reqAdena = 0L;
      this._hasAttachments = true;
      this._attachments = msg.getAttachments();
      msg.removeAttachments();
      this._attachments.setNewMessageId(this._messageId);
      this._unloadTask = ThreadPoolManager.getInstance().schedule(new Message.AttachmentsUnloadTask(this), (long)(900000 + Rnd.get(900000)));
   }

   public static final PreparedStatement getStatement(Message msg, Connection con) throws SQLException {
      PreparedStatement stmt = con.prepareStatement(
         "INSERT INTO messages (messageId, senderId, receiverId, subject, content, expiration, reqAdena, hasAttachments, isUnread, isDeletedBySender, isDeletedByReceiver, sendBySystem, isReturned) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
      );
      stmt.setInt(1, msg._messageId);
      stmt.setInt(2, msg._senderId);
      stmt.setInt(3, msg._receiverId);
      stmt.setString(4, msg._subject);
      stmt.setString(5, msg._content);
      stmt.setLong(6, msg._expiration);
      stmt.setLong(7, msg._reqAdena);
      stmt.setString(8, String.valueOf(msg._hasAttachments));
      stmt.setString(9, String.valueOf(msg._unread));
      stmt.setString(10, String.valueOf(msg._deletedBySender));
      stmt.setString(11, String.valueOf(msg._deletedByReceiver));
      stmt.setString(12, String.valueOf(msg._type.ordinal()));
      stmt.setString(13, String.valueOf(msg._returned));
      return stmt;
   }

   public final int getId() {
      return this._messageId;
   }

   public final int getSenderId() {
      return this._senderId;
   }

   public final int getReceiverId() {
      return this._receiverId;
   }

   public final String getSenderName() {
      if (this._senderName == null) {
         switch(this._type) {
            case NORMAL:
               this._senderName = CharNameHolder.getInstance().getNameById(this._senderId);
               if (this._senderName == null) {
                  this._senderName = "";
               }
               break;
            case NEWS_INFORMER:
            case NONE:
            case BIRTHDAY:
            default:
               this._senderName = "****";
         }
      }

      return this._senderName;
   }

   public final String getReceiverName() {
      if (this._receiverName == null) {
         this._receiverName = CharNameHolder.getInstance().getNameById(this._receiverId);
         if (this._receiverName == null) {
            this._receiverName = "";
         }
      }

      return this._receiverName;
   }

   public final String getSubject() {
      return this._subject;
   }

   public final String getContent() {
      return this._content;
   }

   public final boolean isLocked() {
      return this._reqAdena > 0L;
   }

   public final long getExpiration() {
      return this._expiration;
   }

   public final int getExpirationSeconds() {
      return (int)(this._expiration / 1000L);
   }

   public final boolean isUnread() {
      return this._unread;
   }

   public final void markAsRead() {
      if (this._unread) {
         this._unread = false;
         MailManager.getInstance().markAsReadInDb(this._messageId);
      }
   }

   public final boolean isDeletedBySender() {
      return this._deletedBySender;
   }

   public final void setDeletedBySender() {
      if (!this._deletedBySender) {
         this._deletedBySender = true;
         if (this._deletedByReceiver) {
            MailManager.getInstance().deleteMessageInDb(this._messageId);
         } else {
            MailManager.getInstance().markAsDeletedBySenderInDb(this._messageId);
         }
      }
   }

   public final boolean isDeletedByReceiver() {
      return this._deletedByReceiver;
   }

   public final void setDeletedByReceiver() {
      if (!this._deletedByReceiver) {
         this._deletedByReceiver = true;
         if (this._deletedBySender) {
            MailManager.getInstance().deleteMessageInDb(this._messageId);
         } else {
            MailManager.getInstance().markAsDeletedByReceiverInDb(this._messageId);
         }
      }
   }

   public final boolean isReturned() {
      return this._returned;
   }

   public final void setIsReturned(boolean val) {
      this._returned = val;
   }

   public final long getReqAdena() {
      return this._reqAdena;
   }

   public final synchronized Mail getAttachments() {
      if (!this._hasAttachments) {
         return null;
      } else {
         if (this._attachments == null) {
            this._attachments = new Mail(this._senderId, this._messageId);
            this._attachments.restore();
            this._unloadTask = ThreadPoolManager.getInstance().schedule(new Message.AttachmentsUnloadTask(this), (long)(900000 + Rnd.get(900000)));
         }

         return this._attachments;
      }
   }

   public final boolean hasAttachments() {
      return this._hasAttachments;
   }

   public final synchronized void removeAttachments() {
      if (this._attachments != null) {
         this._attachments = null;
         this._hasAttachments = false;
         MailManager.getInstance().removeAttachmentsInDb(this._messageId);
         if (this._unloadTask != null) {
            this._unloadTask.cancel(false);
         }
      }
   }

   public final synchronized Mail createAttachments() {
      if (!this._hasAttachments && this._attachments == null) {
         this._attachments = new Mail(this._senderId, this._messageId);
         this._hasAttachments = true;
         this._unloadTask = ThreadPoolManager.getInstance().schedule(new Message.AttachmentsUnloadTask(this), (long)(900000 + Rnd.get(900000)));
         return this._attachments;
      } else {
         return null;
      }
   }

   protected final synchronized void unloadAttachments() {
      if (this._attachments != null) {
         this._attachments.deleteMe();
         this._attachments = null;
      }
   }

   public boolean isReturnable() {
      return this._type == Message.SenderType.NORMAL && this._hasAttachments && !this._returned;
   }

   public Message.SenderType getType() {
      return this._type;
   }

   public void setType(Message.SenderType type) {
      this._type = type;
   }

   static class AttachmentsUnloadTask implements Runnable {
      private Message _msg;

      AttachmentsUnloadTask(Message msg) {
         this._msg = msg;
      }

      @Override
      public void run() {
         if (this._msg != null) {
            this._msg.unloadAttachments();
            this._msg = null;
         }
      }
   }

   public static enum SenderType {
      NORMAL,
      NEWS_INFORMER,
      NONE,
      BIRTHDAY;

      public static Message.SenderType[] VALUES = values();
   }
}
