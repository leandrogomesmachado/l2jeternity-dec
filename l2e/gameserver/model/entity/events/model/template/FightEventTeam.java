package l2e.gameserver.model.entity.events.model.template;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.Location;

public class FightEventTeam implements Serializable {
   private static final long serialVersionUID = 2265683963045484182L;
   private final int _index;
   private String _name;
   private final List<FightEventPlayer> _players = new ArrayList<>();
   private Location _spawnLoc;
   private int _score;

   public FightEventTeam(int index) {
      this._index = index;
      this.chooseName();
   }

   public int getIndex() {
      return this._index;
   }

   public String getName() {
      return this._name;
   }

   public void setName(String name) {
      this._name = name;
   }

   public String chooseName() {
      this._name = FightEventTeam.TEAM_NAMES.values()[this._index - 1].toString();
      return this._name;
   }

   public int getNickColor() {
      return FightEventTeam.TEAM_NAMES.values()[this._index - 1]._nameColor;
   }

   public List<FightEventPlayer> getPlayers() {
      return this._players;
   }

   public void addPlayer(FightEventPlayer player) {
      this._players.add(player);
   }

   public void removePlayer(FightEventPlayer player) {
      this._players.remove(player);
   }

   public void setSpawnLoc(Location loc) {
      this._spawnLoc = loc;
   }

   public Location getSpawnLoc() {
      return this._spawnLoc;
   }

   public void setScore(int newScore) {
      this._score = newScore;
   }

   public void incScore(int by) {
      this._score += by;
   }

   public int getScore() {
      return this._score;
   }

   public static enum TEAM_NAMES {
      Red(1453793),
      Blue(11877953),
      Green(4109633),
      Yellow(3079679),
      Gray(8421504),
      Orange(34809),
      Black(1447446),
      White(16777215),
      Violet(12199813),
      Cyan(14934326),
      Pink(14577135);

      public int _nameColor;

      private TEAM_NAMES(int nameColor) {
         this._nameColor = nameColor;
      }
   }
}
