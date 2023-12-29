package l2e.gameserver.network.serverpackets;

import l2e.gameserver.Config;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.cleft.AerialCleftTeam;

public class ExCleftList extends GameServerPacket {
   public static final ExCleftList STATIC_CLOSE = new ExCleftList(ExCleftList.CleftType.CLOSE);
   private final ExCleftList.CleftType _cleftType;
   private AerialCleftTeam _redTeam;
   private AerialCleftTeam _blueTeam;
   private int _newTeamId;
   private int _oldTeamId;
   private Player _player;
   private int _playerObjectId;

   public ExCleftList(ExCleftList.CleftType cleftType, Player player, int teamId) {
      this._cleftType = cleftType;
      this._newTeamId = teamId;
      this._player = player;
   }

   public ExCleftList(ExCleftList.CleftType cleftType, int playerObjectId, int teamId) {
      this._cleftType = cleftType;
      this._playerObjectId = playerObjectId;
      this._newTeamId = teamId;
   }

   public ExCleftList(ExCleftList.CleftType cleftType, int playerObjectId, int oldTeamId, int newTeamId) {
      this._cleftType = cleftType;
      this._playerObjectId = playerObjectId;
      this._oldTeamId = oldTeamId;
      this._newTeamId = newTeamId;
   }

   public ExCleftList(ExCleftList.CleftType cleftType, AerialCleftTeam redTeam, AerialCleftTeam blueTeam) {
      this._cleftType = cleftType;
      this._redTeam = redTeam;
      this._blueTeam = blueTeam;
   }

   public ExCleftList(ExCleftList.CleftType cleftType) {
      this._cleftType = cleftType;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._cleftType.getType());
      switch(this._cleftType) {
         case TOTAL:
            this.writeD(Config.CLEFT_MIN_TEAM_PLAYERS);
            this.writeD(-1);
            this.writeD(this._blueTeam.getParticipatedPlayerCount());

            for(Player player : this._blueTeam.getParticipatedPlayers().values()) {
               this.writeD(player.getObjectId());
               this.writeS(player.getName());
            }

            this.writeD(this._redTeam.getParticipatedPlayerCount());

            for(Player player : this._redTeam.getParticipatedPlayers().values()) {
               this.writeD(player.getObjectId());
               this.writeS(player.getName());
            }
            break;
         case ADD:
            this.writeD(this._newTeamId);
            this.writeD(this._player.getObjectId());
            this.writeS(this._player.getName());
            break;
         case REMOVE:
            this.writeD(this._newTeamId);
            this.writeD(this._playerObjectId);
            break;
         case TEAM_CHANGE:
            this.writeD(this._playerObjectId);
            this.writeD(this._oldTeamId);
            this.writeD(this._newTeamId);
         case CLOSE:
      }
   }

   public static enum CleftType {
      CLOSE(-1),
      TOTAL(0),
      ADD(1),
      REMOVE(2),
      TEAM_CHANGE(3);

      private int _type;

      private CleftType(int type) {
         this._type = type;
      }

      public int getType() {
         return this._type;
      }
   }
}
