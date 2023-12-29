package javax.mail.internet;

import com.sun.mail.util.LineInputStream;
import com.sun.mail.util.PropUtil;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import javax.mail.Header;
import javax.mail.MessagingException;

public class InternetHeaders {
   private static final boolean ignoreWhitespaceLines = PropUtil.getBooleanSystemProperty("mail.mime.ignorewhitespacelines", false);
   protected List headers = new ArrayList(40);

   public InternetHeaders() {
      this.headers.add(new InternetHeaders.InternetHeader("Return-Path", null));
      this.headers.add(new InternetHeaders.InternetHeader("Received", null));
      this.headers.add(new InternetHeaders.InternetHeader("Resent-Date", null));
      this.headers.add(new InternetHeaders.InternetHeader("Resent-From", null));
      this.headers.add(new InternetHeaders.InternetHeader("Resent-Sender", null));
      this.headers.add(new InternetHeaders.InternetHeader("Resent-To", null));
      this.headers.add(new InternetHeaders.InternetHeader("Resent-Cc", null));
      this.headers.add(new InternetHeaders.InternetHeader("Resent-Bcc", null));
      this.headers.add(new InternetHeaders.InternetHeader("Resent-Message-Id", null));
      this.headers.add(new InternetHeaders.InternetHeader("Date", null));
      this.headers.add(new InternetHeaders.InternetHeader("From", null));
      this.headers.add(new InternetHeaders.InternetHeader("Sender", null));
      this.headers.add(new InternetHeaders.InternetHeader("Reply-To", null));
      this.headers.add(new InternetHeaders.InternetHeader("To", null));
      this.headers.add(new InternetHeaders.InternetHeader("Cc", null));
      this.headers.add(new InternetHeaders.InternetHeader("Bcc", null));
      this.headers.add(new InternetHeaders.InternetHeader("Message-Id", null));
      this.headers.add(new InternetHeaders.InternetHeader("In-Reply-To", null));
      this.headers.add(new InternetHeaders.InternetHeader("References", null));
      this.headers.add(new InternetHeaders.InternetHeader("Subject", null));
      this.headers.add(new InternetHeaders.InternetHeader("Comments", null));
      this.headers.add(new InternetHeaders.InternetHeader("Keywords", null));
      this.headers.add(new InternetHeaders.InternetHeader("Errors-To", null));
      this.headers.add(new InternetHeaders.InternetHeader("MIME-Version", null));
      this.headers.add(new InternetHeaders.InternetHeader("Content-Type", null));
      this.headers.add(new InternetHeaders.InternetHeader("Content-Transfer-Encoding", null));
      this.headers.add(new InternetHeaders.InternetHeader("Content-MD5", null));
      this.headers.add(new InternetHeaders.InternetHeader(":", null));
      this.headers.add(new InternetHeaders.InternetHeader("Content-Length", null));
      this.headers.add(new InternetHeaders.InternetHeader("Status", null));
   }

   public InternetHeaders(InputStream is) throws MessagingException {
      this.load(is);
   }

   public void load(InputStream is) throws MessagingException {
      LineInputStream lis = new LineInputStream(is);
      String prevline = null;
      StringBuffer lineBuffer = new StringBuffer();

      try {
         String line;
         do {
            line = lis.readLine();
            if (line != null && (line.startsWith(" ") || line.startsWith("\t"))) {
               if (prevline != null) {
                  lineBuffer.append(prevline);
                  prevline = null;
               }

               lineBuffer.append("\r\n");
               lineBuffer.append(line);
            } else {
               if (prevline != null) {
                  this.addHeaderLine(prevline);
               } else if (lineBuffer.length() > 0) {
                  this.addHeaderLine(lineBuffer.toString());
                  lineBuffer.setLength(0);
               }

               prevline = line;
            }
         } while(line != null && !isEmpty(line));
      } catch (IOException var7) {
         throw new MessagingException("Error in input stream", var7);
      }
   }

   private static final boolean isEmpty(String line) {
      return line.length() == 0 || ignoreWhitespaceLines && line.trim().length() == 0;
   }

   public String[] getHeader(String name) {
      Iterator e = this.headers.iterator();
      List v = new ArrayList();

      while(e.hasNext()) {
         InternetHeaders.InternetHeader h = (InternetHeaders.InternetHeader)e.next();
         if (name.equalsIgnoreCase(h.getName()) && h.line != null) {
            v.add(h.getValue());
         }
      }

      if (v.size() == 0) {
         return null;
      } else {
         String[] r = new String[v.size()];
         return v.toArray(r);
      }
   }

   public String getHeader(String name, String delimiter) {
      String[] s = this.getHeader(name);
      if (s == null) {
         return null;
      } else if (s.length != 1 && delimiter != null) {
         StringBuffer r = new StringBuffer(s[0]);

         for(int i = 1; i < s.length; ++i) {
            r.append(delimiter);
            r.append(s[i]);
         }

         return r.toString();
      } else {
         return s[0];
      }
   }

   public void setHeader(String name, String value) {
      boolean found = false;

      for(int i = 0; i < this.headers.size(); ++i) {
         InternetHeaders.InternetHeader h = (InternetHeaders.InternetHeader)this.headers.get(i);
         if (name.equalsIgnoreCase(h.getName())) {
            if (!found) {
               int j;
               if (h.line != null && (j = h.line.indexOf(58)) >= 0) {
                  h.line = h.line.substring(0, j + 1) + " " + value;
               } else {
                  h.line = name + ": " + value;
               }

               found = true;
            } else {
               this.headers.remove(i);
               --i;
            }
         }
      }

      if (!found) {
         this.addHeader(name, value);
      }
   }

   public void addHeader(String name, String value) {
      int pos = this.headers.size();
      boolean addReverse = name.equalsIgnoreCase("Received") || name.equalsIgnoreCase("Return-Path");
      if (addReverse) {
         pos = 0;
      }

      for(int i = this.headers.size() - 1; i >= 0; --i) {
         InternetHeaders.InternetHeader h = (InternetHeaders.InternetHeader)this.headers.get(i);
         if (name.equalsIgnoreCase(h.getName())) {
            if (!addReverse) {
               this.headers.add(i + 1, new InternetHeaders.InternetHeader(name, value));
               return;
            }

            pos = i;
         }

         if (!addReverse && h.getName().equals(":")) {
            pos = i;
         }
      }

      this.headers.add(pos, new InternetHeaders.InternetHeader(name, value));
   }

   public void removeHeader(String name) {
      for(int i = 0; i < this.headers.size(); ++i) {
         InternetHeaders.InternetHeader h = (InternetHeaders.InternetHeader)this.headers.get(i);
         if (name.equalsIgnoreCase(h.getName())) {
            h.line = null;
         }
      }
   }

   public Enumeration getAllHeaders() {
      return new InternetHeaders.MatchEnum(this.headers, null, false, false);
   }

   public Enumeration getMatchingHeaders(String[] names) {
      return new InternetHeaders.MatchEnum(this.headers, names, true, false);
   }

   public Enumeration getNonMatchingHeaders(String[] names) {
      return new InternetHeaders.MatchEnum(this.headers, names, false, false);
   }

   public void addHeaderLine(String line) {
      try {
         char c = line.charAt(0);
         if (c != ' ' && c != '\t') {
            this.headers.add(new InternetHeaders.InternetHeader(line));
         } else {
            InternetHeaders.InternetHeader h = (InternetHeaders.InternetHeader)this.headers.get(this.headers.size() - 1);
            h.line = h.line + "\r\n" + line;
         }
      } catch (StringIndexOutOfBoundsException var4) {
         return;
      } catch (NoSuchElementException var5) {
      }
   }

   public Enumeration getAllHeaderLines() {
      return this.getNonMatchingHeaderLines(null);
   }

   public Enumeration getMatchingHeaderLines(String[] names) {
      return new InternetHeaders.MatchEnum(this.headers, names, true, true);
   }

   public Enumeration getNonMatchingHeaderLines(String[] names) {
      return new InternetHeaders.MatchEnum(this.headers, names, false, true);
   }

   protected static final class InternetHeader extends Header {
      String line;

      public InternetHeader(String l) {
         super("", "");
         int i = l.indexOf(58);
         if (i < 0) {
            this.name = l.trim();
         } else {
            this.name = l.substring(0, i).trim();
         }

         this.line = l;
      }

      public InternetHeader(String n, String v) {
         super(n, "");
         if (v != null) {
            this.line = n + ": " + v;
         } else {
            this.line = null;
         }
      }

      @Override
      public String getValue() {
         int i = this.line.indexOf(58);
         if (i < 0) {
            return this.line;
         } else {
            int j;
            for(j = i + 1; j < this.line.length(); ++j) {
               char c = this.line.charAt(j);
               if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                  break;
               }
            }

            return this.line.substring(j);
         }
      }
   }

   static class MatchEnum implements Enumeration {
      private Iterator e;
      private String[] names;
      private boolean match;
      private boolean want_line;
      private InternetHeaders.InternetHeader next_header;

      MatchEnum(List v, String[] n, boolean m, boolean l) {
         this.e = v.iterator();
         this.names = n;
         this.match = m;
         this.want_line = l;
         this.next_header = null;
      }

      @Override
      public boolean hasMoreElements() {
         if (this.next_header == null) {
            this.next_header = this.nextMatch();
         }

         return this.next_header != null;
      }

      @Override
      public Object nextElement() {
         if (this.next_header == null) {
            this.next_header = this.nextMatch();
         }

         if (this.next_header == null) {
            throw new NoSuchElementException("No more headers");
         } else {
            InternetHeaders.InternetHeader h = this.next_header;
            this.next_header = null;
            return this.want_line ? h.line : new Header(h.getName(), h.getValue());
         }
      }

      private InternetHeaders.InternetHeader nextMatch() {
         label40:
         while(this.e.hasNext()) {
            InternetHeaders.InternetHeader h = (InternetHeaders.InternetHeader)this.e.next();
            if (h.line != null) {
               if (this.names == null) {
                  return this.match ? null : h;
               }

               for(int i = 0; i < this.names.length; ++i) {
                  if (this.names[i].equalsIgnoreCase(h.getName())) {
                     if (!this.match) {
                        continue label40;
                     }

                     return h;
                  }
               }

               if (!this.match) {
                  return h;
               }
            }
         }

         return null;
      }
   }
}
