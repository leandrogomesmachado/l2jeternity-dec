package javax.mail.search;

import javax.mail.Message;

public final class NotTerm extends SearchTerm {
   private SearchTerm term;
   private static final long serialVersionUID = 7152293214217310216L;

   public NotTerm(SearchTerm t) {
      this.term = t;
   }

   public SearchTerm getTerm() {
      return this.term;
   }

   @Override
   public boolean match(Message msg) {
      return !this.term.match(msg);
   }

   @Override
   public boolean equals(Object obj) {
      if (!(obj instanceof NotTerm)) {
         return false;
      } else {
         NotTerm nt = (NotTerm)obj;
         return nt.term.equals(this.term);
      }
   }

   @Override
   public int hashCode() {
      return this.term.hashCode() << 1;
   }
}
