package com.sun.mail.pop3;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.MethodNotSupportedException;

public class DefaultFolder extends Folder {
   DefaultFolder(POP3Store store) {
      super(store);
   }

   @Override
   public String getName() {
      return "";
   }

   @Override
   public String getFullName() {
      return "";
   }

   @Override
   public Folder getParent() {
      return null;
   }

   @Override
   public boolean exists() {
      return true;
   }

   @Override
   public Folder[] list(String pattern) throws MessagingException {
      return new Folder[]{this.getInbox()};
   }

   @Override
   public char getSeparator() {
      return '/';
   }

   @Override
   public int getType() {
      return 2;
   }

   @Override
   public boolean create(int type) throws MessagingException {
      return false;
   }

   @Override
   public boolean hasNewMessages() throws MessagingException {
      return false;
   }

   @Override
   public Folder getFolder(String name) throws MessagingException {
      if (!name.equalsIgnoreCase("INBOX")) {
         throw new MessagingException("only INBOX supported");
      } else {
         return this.getInbox();
      }
   }

   protected Folder getInbox() throws MessagingException {
      return this.getStore().getFolder("INBOX");
   }

   @Override
   public boolean delete(boolean recurse) throws MessagingException {
      throw new MethodNotSupportedException("delete");
   }

   @Override
   public boolean renameTo(Folder f) throws MessagingException {
      throw new MethodNotSupportedException("renameTo");
   }

   @Override
   public void open(int mode) throws MessagingException {
      throw new MethodNotSupportedException("open");
   }

   @Override
   public void close(boolean expunge) throws MessagingException {
      throw new MethodNotSupportedException("close");
   }

   @Override
   public boolean isOpen() {
      return false;
   }

   @Override
   public Flags getPermanentFlags() {
      return new Flags();
   }

   @Override
   public int getMessageCount() throws MessagingException {
      return 0;
   }

   @Override
   public Message getMessage(int msgno) throws MessagingException {
      throw new MethodNotSupportedException("getMessage");
   }

   @Override
   public void appendMessages(Message[] msgs) throws MessagingException {
      throw new MethodNotSupportedException("Append not supported");
   }

   @Override
   public Message[] expunge() throws MessagingException {
      throw new MethodNotSupportedException("expunge");
   }
}
