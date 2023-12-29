package org.mozilla.universalchardet.prober.statemachine;

public class CodingStateMachine {
   protected SMModel model;
   protected int currentState;
   protected int currentCharLen;
   protected int currentBytePos;

   public CodingStateMachine(SMModel var1) {
      this.model = var1;
      this.currentState = 0;
   }

   public int nextState(byte var1) {
      int var2 = this.model.getClass(var1);
      if (this.currentState == 0) {
         this.currentBytePos = 0;
         this.currentCharLen = this.model.getCharLen(var2);
      }

      this.currentState = this.model.getNextState(var2, this.currentState);
      ++this.currentBytePos;
      return this.currentState;
   }

   public int getCurrentCharLen() {
      return this.currentCharLen;
   }

   public void reset() {
      this.currentState = 0;
   }

   public String getCodingStateMachine() {
      return this.model.getName();
   }
}
