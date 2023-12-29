package l2e.gameserver.model.actor.templates;

public class HistoryInfoTemplate {
   private final int _raceId;
   private int _first;
   private int _second;
   private double _oddRate;

   public HistoryInfoTemplate(int raceId, int first, int second, double oddRate) {
      this._raceId = raceId;
      this._first = first;
      this._second = second;
      this._oddRate = oddRate;
   }

   public int getRaceId() {
      return this._raceId;
   }

   public int getFirst() {
      return this._first;
   }

   public int getSecond() {
      return this._second;
   }

   public double getOddRate() {
      return this._oddRate;
   }

   public void setFirst(int first) {
      this._first = first;
   }

   public void setSecond(int second) {
      this._second = second;
   }

   public void setOddRate(double oddRate) {
      this._oddRate = oddRate;
   }
}
