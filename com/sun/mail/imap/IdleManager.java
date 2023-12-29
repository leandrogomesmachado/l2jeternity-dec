package com.sun.mail.imap;

import com.sun.mail.util.MailLogger;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;

public class IdleManager {
   private Executor es;
   private Selector selector;
   private MailLogger logger;
   private volatile boolean die = false;
   private Queue<IMAPFolder> toWatch = new ConcurrentLinkedQueue<>();
   private Queue<IMAPFolder> toAbort = new ConcurrentLinkedQueue<>();

   public IdleManager(Session session, Executor es) throws IOException {
      this.logger = new MailLogger(this.getClass(), "DEBUG IMAP", session);
      this.es = es;
      this.selector = Selector.open();
      es.execute(new Runnable() {
         @Override
         public void run() {
            IdleManager.this.select();
         }
      });
   }

   public synchronized void watch(Folder folder) throws IOException, MessagingException {
      if (!(folder instanceof IMAPFolder)) {
         throw new MessagingException("Can only watch IMAP folders");
      } else {
         IMAPFolder ifolder = (IMAPFolder)folder;
         SocketChannel sc = ifolder.getChannel();
         if (sc == null) {
            throw new MessagingException("Folder is not using SocketChannels");
         } else {
            this.logger.log(Level.FINEST, "IdleManager watching {0}", ifolder);
            ifolder.startIdle(this);
            this.toWatch.add(ifolder);
            this.selector.wakeup();
         }
      }
   }

   synchronized void requestAbort(IMAPFolder folder) {
      this.toAbort.add(folder);
      this.selector.wakeup();
   }

   private void select() {
      this.die = false;

      try {
         while(!this.die) {
            this.watchAll();
            this.logger.finest("IdleManager waiting...");
            int ns = this.selector.select();
            if (this.logger.isLoggable(Level.FINEST)) {
               this.logger.log(Level.FINEST, "IdleManager selected {0} channels", ns);
            }

            if (this.die || Thread.currentThread().isInterrupted()) {
               break;
            }

            while(this.processKeys() && this.selector.selectNow() > 0) {
            }
         }
      } catch (InterruptedIOException var13) {
         this.logger.log(Level.FINE, "IdleManager interrupted", (Throwable)var13);
      } catch (IOException var14) {
         this.logger.log(Level.FINE, "IdleManager got exception", (Throwable)var14);
      } finally {
         try {
            this.unwatchAll();
            this.selector.close();
         } catch (IOException var12) {
         }

         this.logger.fine("IdleManager exiting");
      }
   }

   private void watchAll() {
      IMAPFolder folder;
      while((folder = this.toWatch.poll()) != null) {
         this.logger.log(Level.FINEST, "IdleManager adding {0} to selector", folder);
         SocketChannel sc = folder.getChannel();
         if (sc != null) {
            try {
               sc.configureBlocking(false);
               sc.register(this.selector, 1, folder);
            } catch (IOException var4) {
               this.logger.log(Level.FINEST, "IdleManager can't register folder", (Throwable)var4);
            }
         }
      }
   }

   private boolean processKeys() throws IOException {
      boolean more = false;

      IMAPFolder folder;
      while((folder = this.toAbort.poll()) != null) {
         this.logger.log(Level.FINE, "IdleManager aborting IDLE for folder: {0}", folder);
         SocketChannel sc = folder.getChannel();
         if (sc != null) {
            SelectionKey sk = sc.keyFor(this.selector);
            if (sk != null) {
               sk.cancel();
            }

            sc.configureBlocking(true);
            folder.idleAbort();
            this.toWatch.add(folder);
            more = true;
         }
      }

      Set<SelectionKey> selectedKeys = this.selector.selectedKeys();

      for(SelectionKey sk : selectedKeys) {
         selectedKeys.remove(sk);
         sk.cancel();
         folder = (IMAPFolder)sk.attachment();
         this.logger.log(Level.FINE, "IdleManager selected folder: {0}", folder);
         SelectableChannel sc = sk.channel();
         sc.configureBlocking(true);

         try {
            if (folder.handleIdle(false)) {
               this.toWatch.add(folder);
               more = true;
            } else {
               this.logger.log(Level.FINE, "IdleManager done watching folder {0}", folder);
            }
         } catch (MessagingException var8) {
            this.logger.log(Level.FINE, "IdleManager got exception for folder: " + folder, (Throwable)var8);
         }
      }

      return more;
   }

   private void unwatchAll() {
      for(SelectionKey sk : this.selector.keys()) {
         sk.cancel();
         IMAPFolder folder = (IMAPFolder)sk.attachment();
         this.logger.log(Level.FINE, "IdleManager no longer watching folder: {0}", folder);
         SelectableChannel sc = sk.channel();

         try {
            sc.configureBlocking(true);
         } catch (IOException var7) {
         }
      }
   }

   public synchronized void stop() {
      this.die = true;
      this.logger.finest("IdleManager stopping");
      this.selector.wakeup();
   }
}
