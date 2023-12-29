package l2e.gameserver.model.actor.templates.player;

public class OlympiadTemplate {
   private final int _rank;
   private final String _name;
   private final long _points;
   private final int _win;
   private final int _lose;
   private final int _wr;

   public OlympiadTemplate(int rank, String name, long points, int win, int lose, int wr) {
      this._rank = rank;
      this._name = name;
      this._points = points;
      this._win = win;
      this._lose = lose;
      this._wr = wr;
   }

   public int getRank() {
      return this._rank;
   }

   public String getName() {
      return this._name;
   }

   public long getPoints() {
      return this._points;
   }

   public int getWin() {
      return this._win;
   }

   public int getLose() {
      return this._lose;
   }

   public int getWr() {
      return this._wr;
   }
}
