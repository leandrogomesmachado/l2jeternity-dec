package l2e.gameserver.network.serverpackets;

import java.util.List;
import l2e.gameserver.model.actor.Player;

public class ExBlockUpSetList extends GameServerPacket {
   private List<Player> _bluePlayers;
   private List<Player> _redPlayers;
   private int _roomNumber;
   private Player _player;
   private boolean _isRedTeam;
   private int _seconds;
   private final int _type;

   public ExBlockUpSetList(List<Player> redPlayers, List<Player> bluePlayers, int roomNumber) {
      this._redPlayers = redPlayers;
      this._bluePlayers = bluePlayers;
      this._roomNumber = roomNumber - 1;
      this._type = 0;
   }

   public ExBlockUpSetList(Player player, boolean isRedTeam, boolean remove) {
      this._player = player;
      this._isRedTeam = isRedTeam;
      this._type = !remove ? 1 : 2;
   }

   public ExBlockUpSetList(int seconds) {
      this._seconds = seconds;
      this._type = 3;
   }

   public ExBlockUpSetList(boolean isExCubeGameCloseUI) {
      this._type = isExCubeGameCloseUI ? -1 : 4;
   }

   public ExBlockUpSetList(Player player, boolean fromRedTeam) {
      this._player = player;
      this._isRedTeam = fromRedTeam;
      this._type = 5;
   }

   @Override
   protected void writeImpl() {
      this.writeD(this._type);
      switch(this._type) {
         case -1:
            this.writeD(-1);
            break;
         case 0:
            this.writeD(-1);
            this.writeD(this._roomNumber);
            this.writeD(this._bluePlayers.size());

            for(Player player : this._bluePlayers) {
               this.writeD(player.getObjectId());
               this.writeS(player.getName());
            }

            this.writeD(this._redPlayers.size());

            for(Player player : this._redPlayers) {
               this.writeD(player.getObjectId());
               this.writeS(player.getName());
            }
            break;
         case 1:
            this.writeD(-1);
            this.writeD(this._isRedTeam ? 1 : 0);
            this.writeD(this._player.getObjectId());
            this.writeS(this._player.getName());
            break;
         case 2:
            this.writeD(-1);
            this.writeD(this._isRedTeam ? 1 : 0);
            this.writeD(this._player.getObjectId());
            break;
         case 3:
            this.writeD(this._seconds);
         case 4:
         default:
            break;
         case 5:
            this.writeD(this._player.getObjectId());
            this.writeD(this._isRedTeam ? 1 : 0);
            this.writeD(this._isRedTeam ? 0 : 1);
      }
   }
}
