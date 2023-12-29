package l2e.gameserver.model.entity.events.model.template;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import l2e.gameserver.model.Party;
import l2e.gameserver.model.actor.Player;

public class FightEventPlayer implements Serializable {
   private static final long serialVersionUID = 5788010638258042361L;
   private Player _player;
   private FightEventTeam _team;
   private Party _myParty = null;
   private int _score;
   private int _playerKills;
   private final Map<String, Integer> _otherCreaturesScores = new ConcurrentHashMap<>();
   private int _deaths;
   private double _damage;
   private long _lastDamageTime;
   private boolean _invisible = false;
   private boolean _isShowRank = false;
   private boolean _isShowTutorial = false;
   private int _secondsSpentOnEvent = 0;
   private int _secondsOutsideZone = 0;
   private boolean _afk = false;
   private long _afkStartTime = 0L;
   private int _totalAfkSeconds = 0;

   public FightEventPlayer(Player player) {
      this._player = player;
   }

   public void setPlayer(Player player) {
      this._player = player;
   }

   public Player getPlayer() {
      return this._player;
   }

   public void setTeam(FightEventTeam team) {
      this._team = team;
   }

   public FightEventTeam getTeam() {
      return this._team;
   }

   public Party getParty() {
      return this._myParty;
   }

   public void setParty(Party party) {
      this._myParty = party;
   }

   public void increaseScore(int byHowMany) {
      this._score += byHowMany;
   }

   public void decreaseScore(int byHowMany) {
      this._score -= byHowMany;
   }

   public void setScore(int value) {
      this._score = value;
   }

   public int getScore() {
      return this._score;
   }

   public void increaseKills() {
      ++this._playerKills;
   }

   public void setKills(int value) {
      this._playerKills = value;
   }

   public int getKills() {
      return this._playerKills;
   }

   public void increaseEventSpecificScore(String scoreKey) {
      if (!this._otherCreaturesScores.containsKey(scoreKey)) {
         this._otherCreaturesScores.put(scoreKey, 0);
      }

      int value = this._otherCreaturesScores.get(scoreKey);
      this._otherCreaturesScores.put(scoreKey, value + 1);
   }

   public void setEventSpecificScore(String scoreKey, int value) {
      this._otherCreaturesScores.put(scoreKey, value);
   }

   public int getEventSpecificScore(String scoreKey) {
      return !this._otherCreaturesScores.containsKey(scoreKey) ? 0 : this._otherCreaturesScores.get(scoreKey);
   }

   public void increaseDeaths() {
      ++this._deaths;
   }

   public void setDeaths(int value) {
      this._deaths = value;
   }

   public int getDeaths() {
      return this._deaths;
   }

   public void increaseDamage(double damage) {
      this._damage += damage;
      this.setLastDamageTime();
   }

   public void setDamage(double damage) {
      this._damage = damage;
      if (damage == 0.0) {
         this._lastDamageTime = 0L;
      }
   }

   public double getDamage() {
      return this._damage;
   }

   public void setLastDamageTime() {
      this._lastDamageTime = System.currentTimeMillis();
   }

   public long getLastDamageTime() {
      return this._lastDamageTime;
   }

   public void setInvisible(boolean val) {
      this._invisible = val;
   }

   public boolean isInvisible() {
      return this._invisible;
   }

   public void setAfk(boolean val) {
      this._afk = val;
   }

   public boolean isAfk() {
      return this._afk;
   }

   public void setAfkStartTime(long startTime) {
      this._afkStartTime = startTime;
   }

   public long getAfkStartTime() {
      return this._afkStartTime;
   }

   public void addTotalAfkSeconds(int secsAfk) {
      this._totalAfkSeconds += secsAfk;
   }

   public int getTotalAfkSeconds() {
      return this._totalAfkSeconds;
   }

   public void setShowRank(boolean b) {
      this._isShowRank = b;
   }

   public boolean isShowRank() {
      return this._isShowRank;
   }

   public void setShowTutorial(boolean b) {
      this._isShowTutorial = b;
   }

   public boolean isShowTutorial() {
      return this._isShowTutorial;
   }

   public void incSecondsSpentOnEvent(int by) {
      this._secondsSpentOnEvent += by;
   }

   public int getSecondsSpentOnEvent() {
      return this._secondsSpentOnEvent;
   }

   public void increaseSecondsOutsideZone() {
      ++this._secondsOutsideZone;
   }

   public int getSecondsOutsideZone() {
      return this._secondsOutsideZone;
   }

   public void clearSecondsOutsideZone() {
      this._secondsOutsideZone = 0;
   }
}
