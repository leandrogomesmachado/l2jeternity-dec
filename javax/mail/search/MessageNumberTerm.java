package javax.mail.search;

import javax.mail.Message;

public final class MessageNumberTerm extends IntegerComparisonTerm {
   private static final long serialVersionUID = -5379625829658623812L;

   public MessageNumberTerm(int number) {
      super(3, number);
   }

   @Override
   public boolean match(Message msg) {
      int msgno;
      try {
         msgno = msg.getMessageNumber();
      } catch (Exception var4) {
         return false;
      }

      return super.match(msgno);
   }

   @Override
   public boolean equals(Object obj) {
      return !(obj instanceof MessageNumberTerm) ? false : super.equals(obj);
   }
}
