package l2e.gameserver.model.skills.conditions;

import l2e.gameserver.model.stats.Env;

public abstract class Condition implements ConditionListener {
   private ConditionListener _listener;
   private String _msg;
   private int _msgId;
   private boolean _addName = false;
   private boolean _result;

   public final void setMessage(String msg) {
      this._msg = msg;
   }

   public final String getMessage() {
      return this._msg;
   }

   public final void setMessageId(int msgId) {
      this._msgId = msgId;
   }

   public final int getMessageId() {
      return this._msgId;
   }

   public final void addName() {
      this._addName = true;
   }

   public final boolean isAddName() {
      return this._addName;
   }

   void setListener(ConditionListener listener) {
      this._listener = listener;
      this.notifyChanged();
   }

   final ConditionListener getListener() {
      return this._listener;
   }

   public final boolean test(Env env) {
      boolean res = this.testImpl(env);
      if (this._listener != null && res != this._result) {
         this._result = res;
         this.notifyChanged();
      }

      return res;
   }

   public abstract boolean testImpl(Env var1);

   @Override
   public void notifyChanged() {
      if (this._listener != null) {
         this._listener.notifyChanged();
      }
   }
}
