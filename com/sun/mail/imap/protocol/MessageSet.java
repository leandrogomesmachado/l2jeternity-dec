package com.sun.mail.imap.protocol;

import java.util.ArrayList;
import java.util.List;

public class MessageSet {
   public int start;
   public int end;

   public MessageSet() {
   }

   public MessageSet(int start, int end) {
      this.start = start;
      this.end = end;
   }

   public int size() {
      return this.end - this.start + 1;
   }

   public static MessageSet[] createMessageSets(int[] msgs) {
      List<MessageSet> v = new ArrayList<>();

      for(int i = 0; i < msgs.length; ++i) {
         MessageSet ms = new MessageSet();
         ms.start = msgs[i];
         int j = i + 1;

         while(j < msgs.length && msgs[j] == msgs[j - 1] + 1) {
            ++j;
         }

         ms.end = msgs[j - 1];
         v.add(ms);
         i = j - 1;
      }

      return v.toArray(new MessageSet[v.size()]);
   }

   public static String toString(MessageSet[] msgsets) {
      if (msgsets != null && msgsets.length != 0) {
         int i = 0;
         StringBuffer s = new StringBuffer();
         int size = msgsets.length;

         while(true) {
            int start = msgsets[i].start;
            int end = msgsets[i].end;
            if (end > start) {
               s.append(start).append(':').append(end);
            } else {
               s.append(start);
            }

            if (++i >= size) {
               return s.toString();
            }

            s.append(',');
         }
      } else {
         return null;
      }
   }

   public static int size(MessageSet[] msgsets) {
      int count = 0;
      if (msgsets == null) {
         return 0;
      } else {
         for(int i = 0; i < msgsets.length; ++i) {
            count += msgsets[i].size();
         }

         return count;
      }
   }
}
