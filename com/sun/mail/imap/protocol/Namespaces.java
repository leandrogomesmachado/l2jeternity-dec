package com.sun.mail.imap.protocol;

import com.sun.mail.iap.ProtocolException;
import com.sun.mail.iap.Response;
import java.util.ArrayList;
import java.util.List;

public class Namespaces {
   public Namespaces.Namespace[] personal;
   public Namespaces.Namespace[] otherUsers;
   public Namespaces.Namespace[] shared;

   public Namespaces(Response r) throws ProtocolException {
      this.personal = this.getNamespaces(r);
      this.otherUsers = this.getNamespaces(r);
      this.shared = this.getNamespaces(r);
   }

   private Namespaces.Namespace[] getNamespaces(Response r) throws ProtocolException {
      r.skipSpaces();
      if (r.peekByte() != 40) {
         String s = r.readAtom();
         if (s == null) {
            throw new ProtocolException("Expected NIL, got null");
         } else if (!s.equalsIgnoreCase("NIL")) {
            throw new ProtocolException("Expected NIL, got " + s);
         } else {
            return null;
         }
      } else {
         List<Namespaces.Namespace> v = new ArrayList<>();
         r.readByte();

         do {
            Namespaces.Namespace ns = new Namespaces.Namespace(r);
            v.add(ns);
         } while(r.peekByte() != 41);

         r.readByte();
         return v.toArray(new Namespaces.Namespace[v.size()]);
      }
   }

   public static class Namespace {
      public String prefix;
      public char delimiter;

      public Namespace(Response r) throws ProtocolException {
         if (r.readByte() != 40) {
            throw new ProtocolException("Missing '(' at start of Namespace");
         } else {
            this.prefix = BASE64MailboxDecoder.decode(r.readString());
            r.skipSpaces();
            if (r.peekByte() == 34) {
               r.readByte();
               this.delimiter = (char)r.readByte();
               if (this.delimiter == '\\') {
                  this.delimiter = (char)r.readByte();
               }

               if (r.readByte() != 34) {
                  throw new ProtocolException("Missing '\"' at end of QUOTED_CHAR");
               }
            } else {
               String s = r.readAtom();
               if (s == null) {
                  throw new ProtocolException("Expected NIL, got null");
               }

               if (!s.equalsIgnoreCase("NIL")) {
                  throw new ProtocolException("Expected NIL, got " + s);
               }

               this.delimiter = 0;
            }

            if (r.peekByte() != 41) {
               r.skipSpaces();
               r.readString();
               r.skipSpaces();
               r.readStringList();
            }

            if (r.readByte() != 41) {
               throw new ProtocolException("Missing ')' at end of Namespace");
            }
         }
      }
   }
}
