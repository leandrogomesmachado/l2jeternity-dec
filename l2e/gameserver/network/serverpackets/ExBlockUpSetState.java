package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Player;

public class ExBlockUpSetState extends GameServerPacket {
   private final int _type;
   private boolean _isRedTeamWin;
   private int _timeLeft;
   private int _bluePoints;
   private int _redPoints;
   private boolean _isRedTeam;
   private Player _player;
   private int _playerPoints;

   public ExBlockUpSetState(int timeLeft, int bluePoints, int redPoints, boolean isRedTeam, Player player, int playerPoints) {
      this._timeLeft = timeLeft;
      this._bluePoints = bluePoints;
      this._redPoints = redPoints;
      this._isRedTeam = isRedTeam;
      this._player = player;
      this._playerPoints = playerPoints;
      this._type = 0;
   }

   public ExBlockUpSetState(boolean isRedTeamWin) {
      this._isRedTeamWin = isRedTeamWin;
      this._type = 1;
   }

   public ExBlockUpSetState(int timeLeft, int bluePoints, int redPoints) {
      this._timeLeft = timeLeft;
      this._bluePoints = bluePoints;
      this._redPoints = redPoints;
      this._type = 2;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._type);
      switch(this._type) {
         case 0:
            this.writeD(this._timeLeft);
            this.writeD(this._bluePoints);
            this.writeD(this._redPoints);
            this.writeD(this._isRedTeam ? 1 : 0);
            this.writeD(this._player.getObjectId());
            this.writeD(this._playerPoints);
            break;
         case 1:
            this.writeD(this._isRedTeamWin ? 1 : 0);
            break;
         case 2:
            this.writeD(this._timeLeft);
            this.writeD(this._bluePoints);
            this.writeD(this._redPoints);
      }
   }
}
