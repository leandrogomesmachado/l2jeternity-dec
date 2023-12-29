package l2e.gameserver.model.actor.templates.quest;

public class QuestExperience {
   private final long _exp;
   private final long _sp;
   private final double _expRate;
   private final double _spRate;
   private final boolean _rateableExp;
   private final boolean _rateableSp;

   public QuestExperience(long exp, long sp, double expRate, double spRate, boolean rateableExp, boolean rateableSp) {
      this._exp = exp;
      this._sp = sp;
      this._expRate = expRate;
      this._spRate = spRate;
      this._rateableExp = rateableExp;
      this._rateableSp = rateableSp;
   }

   public long getExp() {
      return this._exp;
   }

   public long getSp() {
      return this._sp;
   }

   public double getExpRate() {
      return this._expRate;
   }

   public double getSpRate() {
      return this._spRate;
   }

   public boolean isExpRateable() {
      return this._rateableExp;
   }

   public boolean isSpRateable() {
      return this._rateableSp;
   }
}
