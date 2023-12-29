package l2e.loginserver.network;

import l2e.commons.util.Rnd;

public class SessionKey {
   public final int _playOkID1;
   public final int _playOkID2;
   public final int _loginOkID1;
   public final int _loginOkID2;
   private final int _hashCode;

   public SessionKey(int loginOK1, int loginOK2, int playOK1, int playOK2) {
      this._playOkID1 = playOK1;
      this._playOkID2 = playOK2;
      this._loginOkID1 = loginOK1;
      this._loginOkID2 = loginOK2;
      int hashCode = playOK1 * 17;
      hashCode += playOK2;
      hashCode *= 37;
      hashCode += loginOK1;
      hashCode *= 51;
      hashCode += loginOK2;
      this._hashCode = hashCode;
   }

   public boolean checkLoginPair(int loginOk1, int loginOk2) {
      return this._loginOkID1 == loginOk1 && this._loginOkID2 == loginOk2;
   }

   public static final SessionKey create() {
      return new SessionKey(Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt(), Rnd.nextInt());
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o == null) {
         return false;
      } else if (o.getClass() != this.getClass()) {
         return false;
      } else {
         SessionKey skey = (SessionKey)o;
         return this._playOkID1 == skey._playOkID1 && this._playOkID2 == skey._playOkID2 && skey.checkLoginPair(this._loginOkID1, this._loginOkID2);
      }
   }

   @Override
   public int hashCode() {
      return this._hashCode;
   }

   @Override
   public String toString() {
      return "[playOkID1: "
         + this._playOkID1
         + " playOkID2: "
         + this._playOkID2
         + " loginOkID1: "
         + this._loginOkID1
         + " loginOkID2: "
         + this._loginOkID2
         + "]";
   }
}
