package l2e.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;
import l2e.gameserver.model.olympiad.AbstractOlympiadGame;
import l2e.gameserver.model.olympiad.OlympiadGameClassed;
import l2e.gameserver.model.olympiad.OlympiadGameManager;
import l2e.gameserver.model.olympiad.OlympiadGameNonClassed;
import l2e.gameserver.model.olympiad.OlympiadGameTask;
import l2e.gameserver.model.olympiad.OlympiadGameTeams;
import l2e.gameserver.model.olympiad.OlympiadInfo;
import l2e.gameserver.network.ServerPacketOpcodes;

public abstract class ExReceiveOlympiadList extends GameServerPacket {
   @Override
   protected ServerPacketOpcodes getOpcodes() {
      return ServerPacketOpcodes.ExReceiveOlympiadList;
   }

   public static class OlympiadList extends ExReceiveOlympiadList {
      List<OlympiadGameTask> _games = new ArrayList<>();

      public OlympiadList() {
         for(int i = 0; i < OlympiadGameManager.getInstance().getNumberOfStadiums(); ++i) {
            OlympiadGameTask task = OlympiadGameManager.getInstance().getOlympiadTask(i);
            if (task != null && task.isGameStarted() && !task.isBattleFinished()) {
               this._games.add(task);
            }
         }
      }

      @Override
      protected final void writeImpl() {
         this.writeD(0);
         this.writeD(this._games.size());
         this.writeD(0);

         for(OlympiadGameTask curGame : this._games) {
            AbstractOlympiadGame game = curGame.getGame();
            if (game != null) {
               this.writeD(game.getStadiumId());
               if (game instanceof OlympiadGameNonClassed) {
                  this.writeD(1);
               } else if (game instanceof OlympiadGameClassed) {
                  this.writeD(2);
               } else if (game instanceof OlympiadGameTeams) {
                  this.writeD(-1);
               } else {
                  this.writeD(0);
               }

               this.writeD(curGame.isRunning() ? 2 : 1);
               this.writeS(game.getPlayerNames()[0]);
               this.writeS(game.getPlayerNames()[1]);
            }
         }
      }
   }

   public static class OlympiadResult extends ExReceiveOlympiadList {
      private final boolean _tie;
      private int _winTeam;
      private int _loseTeam = 2;
      private final List<OlympiadInfo> _winnerList;
      private final List<OlympiadInfo> _loserList;

      public OlympiadResult(boolean tie, int winTeam, List<OlympiadInfo> winnerList, List<OlympiadInfo> loserList) {
         this._tie = tie;
         this._winTeam = winTeam;
         this._winnerList = winnerList;
         this._loserList = loserList;
         if (this._winTeam == 2) {
            this._loseTeam = 1;
         } else if (this._winTeam == 0) {
            this._winTeam = 1;
         }
      }

      @Override
      protected void writeImpl() {
         this.writeD(1);
         this.writeD(this._tie ? 1 : 0);
         this.writeS(this._winnerList.get(0).getName());
         this.writeD(this._winTeam);
         this.writeD(this._winnerList.size());

         for(OlympiadInfo info : this._winnerList) {
            this.writeS(info.getName());
            this.writeS(info.getClanName());
            this.writeD(info.getClanId());
            this.writeD(info.getClassId());
            this.writeD(info.getDamage());
            this.writeD(info.getCurrentPoints());
            this.writeD(info.getDiffPoints());
         }

         this.writeD(this._loseTeam);
         this.writeD(this._loserList.size());

         for(OlympiadInfo info : this._loserList) {
            this.writeS(info.getName());
            this.writeS(info.getClanName());
            this.writeD(info.getClanId());
            this.writeD(info.getClassId());
            this.writeD(info.getDamage());
            this.writeD(info.getCurrentPoints());
            this.writeD(info.getDiffPoints());
         }
      }
   }
}
