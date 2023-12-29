package l2e.gameserver.model.entity.events.model.template;

import l2e.gameserver.model.actor.Player;

public class FightEventLastPlayerStats {
   private final String _playerNickName;
   private final int _classId;
   private final String _clanName;
   private final String _allyName;
   private final String _typeName;
   private int _score;

   public FightEventLastPlayerStats(Player player, String typeName, int score) {
      this._playerNickName = player.getName();
      this._clanName = player.getClan() != null ? player.getClan().getName() : "<br>";
      this._allyName = player.getClan() != null && player.getClan().getAllyId() > 0 ? player.getClan().getAllyName() : "<br>";
      this._classId = player.getClassId().getId();
      this._typeName = typeName;
      this._score = score;
   }

   public FightEventLastPlayerStats(String playerName, String clanName, String allyName, int classId, int score) {
      this._playerNickName = playerName;
      this._clanName = clanName;
      this._allyName = allyName;
      this._classId = classId;
      this._typeName = "Kill Player";
      this._score = score;
   }

   public boolean isMyStat(Player player) {
      return this._playerNickName.equals(player.getName());
   }

   public String getPlayerName() {
      return this._playerNickName;
   }

   public String getClanName() {
      return this._clanName;
   }

   public String getAllyName() {
      return this._allyName;
   }

   public int getClassId() {
      return this._classId;
   }

   public String getTypeName() {
      return this._typeName;
   }

   public int getScore() {
      return this._score;
   }

   public void setScore(int i) {
      this._score = i;
   }
}
