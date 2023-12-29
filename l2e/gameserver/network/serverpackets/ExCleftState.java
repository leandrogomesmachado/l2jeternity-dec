package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.events.cleft.AerialCleftEvent;
import l2e.gameserver.model.entity.events.cleft.AerialCleftTeam;

public class ExCleftState extends GameServerPacket {
   private final ExCleftState.CleftState _cleftState;
   private AerialCleftTeam _redTeam;
   private AerialCleftTeam _blueTeam;
   private AerialCleftTeam _catTeamUpdate;
   private AerialCleftTeam _towerDestroyTeam;
   private AerialCleftEvent _event;
   private Player _towerKiller;
   private Player _killer;
   private Player _killed;
   private int _towerId;
   private int _killerTeamId;
   private int _killedTeamId;
   private AerialCleftTeam _winTeam;
   private AerialCleftTeam _loseTeam;

   public ExCleftState(ExCleftState.CleftState cleftState) {
      this._cleftState = cleftState;
   }

   public ExCleftState(ExCleftState.CleftState cleftState, AerialCleftEvent event, AerialCleftTeam blueTeam, AerialCleftTeam redTeam) {
      this._cleftState = cleftState;
      this._event = event;
      this._blueTeam = blueTeam;
      this._redTeam = redTeam;
   }

   public ExCleftState(ExCleftState.CleftState cleftState, AerialCleftEvent event, AerialCleftTeam catTeamUpdate) {
      this._cleftState = cleftState;
      this._event = event;
      this._catTeamUpdate = catTeamUpdate;
   }

   public ExCleftState(
      ExCleftState.CleftState cleftState,
      AerialCleftEvent event,
      AerialCleftTeam towerDestroyTeam,
      AerialCleftTeam blueTeam,
      AerialCleftTeam redTeam,
      int towerId,
      Player killer
   ) {
      this._cleftState = cleftState;
      this._event = event;
      this._towerDestroyTeam = towerDestroyTeam;
      this._blueTeam = blueTeam;
      this._redTeam = redTeam;
      this._towerId = towerId;
      this._towerKiller = killer;
   }

   public ExCleftState(
      ExCleftState.CleftState cleftState,
      AerialCleftEvent event,
      AerialCleftTeam blueTeam,
      AerialCleftTeam redTeam,
      Player killer,
      Player killed,
      int killerTeamId,
      int killedTeamId
   ) {
      this._cleftState = cleftState;
      this._blueTeam = blueTeam;
      this._redTeam = redTeam;
      this._event = event;
      this._killer = killer;
      this._killed = killed;
      this._killerTeamId = killerTeamId;
      this._killedTeamId = killedTeamId;
   }

   public ExCleftState(ExCleftState.CleftState cleftState, AerialCleftTeam winTeam, AerialCleftTeam loseTeam) {
      this._cleftState = cleftState;
      this._winTeam = winTeam;
      this._loseTeam = loseTeam;
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._cleftState.ordinal());
      switch(this._cleftState) {
         case TOTAL:
            this.writeD(this._event.getEventTimeEnd());
            this.writeD(this._blueTeam.getPoints());
            this.writeD(this._redTeam.getPoints());
            this.writeD(this._blueTeam.getTeamCat().getObjectId());
            this.writeD(this._redTeam.getTeamCat().getObjectId());
            this.writeS(this._blueTeam.getTeamCat().getName());
            this.writeS(this._redTeam.getTeamCat().getName());
            this.writeD(this._blueTeam.getParticipatedPlayerCount());

            for(Player player : this._blueTeam.getParticipatedPlayers().values()) {
               this.writeD(player.getObjectId());
               this.writeD(player.getCleftKills());
               this.writeD(player.getCleftDeaths());
               this.writeD(player.getCleftKillTowers());
            }

            this.writeD(this._redTeam.getParticipatedPlayerCount());

            for(Player player : this._redTeam.getParticipatedPlayers().values()) {
               this.writeD(player.getObjectId());
               this.writeD(player.getCleftKills());
               this.writeD(player.getCleftDeaths());
               this.writeD(player.getCleftKillTowers());
            }
            break;
         case TOWER_DESTROY:
            this.writeD(this._event.getEventTimeEnd());
            this.writeD(this._blueTeam.getPoints());
            this.writeD(this._redTeam.getPoints());
            this.writeD(this._towerDestroyTeam.getId());
            this.writeD(this._towerId);
            this.writeD(this._towerKiller.getObjectId());
            this.writeD(this._towerKiller.getCleftKillTowers());
            this.writeD(this._towerKiller.getCleftKills());
            this.writeD(this._towerKiller.getCleftDeaths());
            break;
         case CAT_UPDATE:
            this.writeD(this._event.getEventTimeEnd());
            this.writeD(this._catTeamUpdate.getId());
            this.writeD(this._catTeamUpdate.getTeamCat().getObjectId());
            this.writeS(this._catTeamUpdate.getTeamCat().getName());
            break;
         case RESULT:
            this.writeD(this._winTeam.getId());
            this.writeD(this._loseTeam.getId());
            break;
         case PVP_KILL:
            this.writeD(this._event.getEventTimeEnd());
            this.writeD(this._blueTeam.getPoints());
            this.writeD(this._redTeam.getPoints());
            this.writeD(this._killerTeamId);
            this.writeD(this._killer.getObjectId());
            this.writeD(this._killer.getCleftKillTowers());
            this.writeD(this._killer.getCleftKills());
            this.writeD(this._killer.getCleftDeaths());
            this.writeD(this._killedTeamId);
            this.writeD(this._killed.getObjectId());
            this.writeD(this._killed.getCleftKillTowers());
            this.writeD(this._killed.getCleftKills());
            this.writeD(this._killed.getCleftDeaths());
      }
   }

   public static enum CleftState {
      TOTAL(0),
      TOWER_DESTROY(1),
      CAT_UPDATE(2),
      RESULT(3),
      PVP_KILL(4);

      private int _cleftState;

      private CleftState(int cleftState) {
         this._cleftState = cleftState;
      }

      public int getState() {
         return this._cleftState;
      }
   }
}
