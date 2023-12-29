package l2e.gameserver.model.olympiad;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.idfactory.IdFactory;
import l2e.gameserver.listener.talk.OnAnswerListener;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.strings.server.ServerMessage;
import l2e.gameserver.model.zone.type.OlympiadStadiumZone;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExOlympiadUserInfo;
import l2e.gameserver.network.serverpackets.ExReceiveOlympiadList;
import l2e.gameserver.network.serverpackets.ExShowScreenMessage;
import l2e.gameserver.network.serverpackets.GameServerPacket;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class OlympiadGameTeams extends AbstractOlympiadGame {
   public static final int MAX_TEAM_SIZE = 3;
   protected boolean _teamOneDefaulted;
   protected boolean _teamTwoDefaulted;
   protected int _damageT1 = 0;
   protected int _damageT2 = 0;
   protected final int _teamOneSize;
   protected final int _teamTwoSize;
   protected final Participant[] _teamOne;
   protected final Participant[] _teamTwo;
   protected int _currentInvites;
   protected int _confirmInvites;

   protected OlympiadGameTeams(int id, Participant[] teamOne, Participant[] teamTwo) {
      super(id);
      this._teamOneSize = Math.min(teamOne.length, 3);
      this._teamTwoSize = Math.min(teamTwo.length, 3);
      this._teamOne = new Participant[3];
      this._teamTwo = new Participant[3];

      for(int i = 0; i < 3; ++i) {
         if (i < this._teamOneSize) {
            Participant par = teamOne[i];
            this._teamOne[i] = par;
            if (par.getPlayer() != null) {
               par.getPlayer().setOlympiadGameId(id);
            }
         } else {
            this._teamOne[i] = new Participant(IdFactory.getInstance().getNextId(), 1);
         }

         if (i < this._teamTwoSize) {
            Participant par = teamTwo[i];
            this._teamTwo[i] = par;
            if (par.getPlayer() != null) {
               par.getPlayer().setOlympiadGameId(id);
            }
         } else {
            this._teamTwo[i] = new Participant(IdFactory.getInstance().getNextId(), 2);
         }
      }

      this._currentInvites = 0;
      this._confirmInvites = this._teamOne.length + this._teamTwo.length;
   }

   protected static final Participant[][] createListOfParticipants(List<List<Integer>> list) {
      if (list != null && !list.isEmpty() && list.size() >= 2) {
         List<Integer> teamOne = null;
         List<Integer> teamTwo = null;
         List<Player> teamOnePlayers = new ArrayList<>(3);
         List<Player> teamTwoPlayers = new ArrayList<>(3);

         while(list.size() > 1) {
            teamOne = list.remove(Rnd.nextInt(list.size()));
            if (teamOne != null && !teamOne.isEmpty()) {
               for(int objectId : teamOne) {
                  Player player = World.getInstance().getPlayer(objectId);
                  if (player == null || !player.isOnline()) {
                     teamOnePlayers.clear();
                     break;
                  }

                  teamOnePlayers.add(player);
               }

               if (!teamOnePlayers.isEmpty()) {
                  teamTwo = list.remove(Rnd.nextInt(list.size()));
                  if (teamTwo != null && !teamTwo.isEmpty()) {
                     for(int objectId : teamTwo) {
                        Player player = World.getInstance().getPlayer(objectId);
                        if (player == null || !player.isOnline()) {
                           teamTwoPlayers.clear();
                           break;
                        }

                        teamTwoPlayers.add(player);
                     }

                     if (!teamTwoPlayers.isEmpty()) {
                        Participant[] t1 = new Participant[teamOnePlayers.size()];
                        Participant[] t2 = new Participant[teamTwoPlayers.size()];
                        Participant[][] result = new Participant[2][];

                        for(int i = 0; i < t1.length; ++i) {
                           t1[i] = new Participant(teamOnePlayers.get(i), 1);
                        }

                        for(int i = 0; i < t2.length; ++i) {
                           t2[i] = new Participant(teamTwoPlayers.get(i), 2);
                        }

                        result[0] = t1;
                        result[1] = t2;
                        return result;
                     }

                     list.add(teamOne);
                     teamOnePlayers.clear();
                  } else {
                     list.add(teamOne);
                     teamOnePlayers.clear();
                  }
               }
            }
         }

         return (Participant[][])null;
      } else {
         return (Participant[][])null;
      }
   }

   protected static OlympiadGameTeams createGame(int id, List<List<Integer>> list) {
      Participant[][] teams = createListOfParticipants(list);
      return teams == null ? null : new OlympiadGameTeams(id, teams[0], teams[1]);
   }

   @Override
   public CompetitionType getType() {
      return CompetitionType.TEAMS;
   }

   @Override
   protected int getDivider() {
      return 5;
   }

   @Override
   protected int[][] getReward() {
      return Config.ALT_OLY_TEAM_REWARD;
   }

   @Override
   protected final String getWeeklyMatchType() {
      return "competitions_done_week_team";
   }

   @Override
   public final boolean containsParticipant(int playerId) {
      int i = this._teamOneSize;

      while(--i >= 0) {
         if (this._teamOne[i].getObjectId() == playerId) {
            return true;
         }
      }

      i = this._teamTwoSize;

      while(--i >= 0) {
         if (this._teamTwo[i].getObjectId() == playerId) {
            return true;
         }
      }

      return false;
   }

   @Override
   public final boolean containsHwidParticipant(Player player) {
      int i = this._teamOneSize;

      while(--i >= 0) {
         if (this._teamOne[i].getPlayer().getHWID().equalsIgnoreCase(player.getHWID())) {
            return true;
         }
      }

      i = this._teamTwoSize;

      while(--i >= 0) {
         if (this._teamTwo[i].getPlayer().getHWID().equalsIgnoreCase(player.getHWID())) {
            return true;
         }
      }

      return false;
   }

   @Override
   public final void sendOlympiadInfo(Creature player) {
      for(int i = 0; i < 3; ++i) {
         player.sendPacket(new ExOlympiadUserInfo(this._teamOne[i]));
      }

      for(int i = 0; i < 3; ++i) {
         player.sendPacket(new ExOlympiadUserInfo(this._teamTwo[i]));
      }
   }

   @Override
   public final void broadcastOlympiadInfo(OlympiadStadiumZone stadium) {
      for(int i = 0; i < 3; ++i) {
         stadium.broadcastPacket(new ExOlympiadUserInfo(this._teamOne[i]));
      }

      for(int i = 0; i < 3; ++i) {
         stadium.broadcastPacket(new ExOlympiadUserInfo(this._teamTwo[i]));
      }
   }

   @Override
   protected final void broadcastPacket(GameServerPacket packet) {
      for(int i = 0; i < this._teamOneSize; ++i) {
         Participant par = this._teamOne[i];
         if (par.updatePlayer()) {
            par.getPlayer().sendPacket(packet);
         }
      }

      for(int i = 0; i < this._teamTwoSize; ++i) {
         Participant par = this._teamTwo[i];
         par.updatePlayer();
         if (par.getPlayer() != null) {
            par.getPlayer().sendPacket(packet);
         }
      }
   }

   @Override
   protected boolean needBuffers() {
      return false;
   }

   private void checkAnswers(Player player) {
      if (this._currentInvites >= this._confirmInvites) {
         OlympiadGameTask task = OlympiadGameManager.getInstance().getOlympiadTask(player.getOlympiadGameId());
         if (task != null && task.isPrepareTeleport()) {
            task.quickTeleport();
         }
      }
   }

   @Override
   protected final void confirmDlgInvite() {
      this._currentInvites = 0;

      for(int i = 0; i < this._teamOneSize; ++i) {
         if (this._teamOne[i].getPlayer() != null) {
            this._teamOne[i]
               .getPlayer()
               .sendConfirmDlg(
                  new OlympiadGameTeams.AnswerClick(this._teamOne[i].getPlayer()),
                  10000,
                  new ServerMessage("Olympiad.INVITE", this._teamOne[i].getPlayer().getLang()).toString()
               );
         }
      }

      for(int i = 0; i < this._teamTwoSize; ++i) {
         if (this._teamTwo[i].getPlayer() != null) {
            this._teamTwo[i]
               .getPlayer()
               .sendConfirmDlg(
                  new OlympiadGameTeams.AnswerClick(this._teamTwo[i].getPlayer()),
                  10000,
                  new ServerMessage("Olympiad.INVITE", this._teamTwo[i].getPlayer().getLang()).toString()
               );
         }
      }
   }

   @Override
   protected final boolean portPlayersToArena(List<Location> spawns) {
      boolean result = true;

      try {
         for(int i = 0; i < this._teamOneSize; ++i) {
            this.setOlympiadGame(this._teamOne[i], this);
            result &= portPlayerToArena(this._teamOne[i], spawns.get(i), this._stadiumID);
         }

         int offset = spawns.size() / 2;

         for(int i = 0; i < this._teamTwoSize; ++i) {
            this.setOlympiadGame(this._teamTwo[i], this);
            result &= portPlayerToArena(this._teamTwo[i], spawns.get(i + offset), this._stadiumID);
         }

         return result;
      } catch (Exception var5) {
         _log.log(Level.WARNING, "", (Throwable)var5);
         return false;
      }
   }

   @Override
   protected final void removals() {
      int i = this._teamOneSize;

      while(--i >= 0) {
         removals(this._teamOne[i].getPlayer(), false);
      }

      i = this._teamTwoSize;

      while(--i >= 0) {
         removals(this._teamTwo[i].getPlayer(), false);
      }
   }

   @Override
   protected final boolean makeCompetitionStart() {
      if (!super.makeCompetitionStart()) {
         return false;
      } else {
         for(int i = 0; i < this._teamOneSize; ++i) {
            Participant par = this._teamOne[i];
            if (par.getPlayer() == null) {
               return false;
            }

            par.getPlayer().setIsOlympiadStart(true);
            par.getPlayer().updateEffectIcons();
         }

         for(int i = 0; i < this._teamTwoSize; ++i) {
            Participant par = this._teamTwo[i];
            if (par.getPlayer() == null) {
               return false;
            }

            par.getPlayer().setIsOlympiadStart(true);
            par.getPlayer().updateEffectIcons();
         }

         return true;
      }
   }

   @Override
   protected final void restorePlayers() {
      int i = this._teamOneSize;

      while(--i >= 0) {
         Participant par = this._teamOne[i];
         if (par.getPlayer() != null && !par.isDefaulted() && !par.isDisconnected() && par.getPlayer().getOlympiadGameId() == this._stadiumID) {
            restorePlayer(par.getPlayer());
         }
      }

      i = this._teamTwoSize;

      while(--i >= 0) {
         Participant par = this._teamTwo[i];
         if (par.getPlayer() != null && !par.isDefaulted() && !par.isDisconnected() && par.getPlayer().getOlympiadGameId() == this._stadiumID) {
            restorePlayer(par.getPlayer());
         }
      }
   }

   @Override
   protected final void cleanEffects() {
      int i = this._teamOneSize;

      while(--i >= 0) {
         Participant par = this._teamOne[i];
         if (par.getPlayer() != null && !par.isDefaulted() && !par.isDisconnected() && par.getPlayer().getOlympiadGameId() == this._stadiumID) {
            cleanEffects(par.getPlayer());
         }
      }

      i = this._teamTwoSize;

      while(--i >= 0) {
         Participant par = this._teamTwo[i];
         if (par.getPlayer() != null && !par.isDefaulted() && !par.isDisconnected() && par.getPlayer().getOlympiadGameId() == this._stadiumID) {
            cleanEffects(par.getPlayer());
         }
      }
   }

   @Override
   protected final void portPlayersBack() {
      int i = this._teamOneSize;

      while(--i >= 0) {
         Participant par = this._teamOne[i];
         if (par.getPlayer() != null && !par.isDefaulted() && !par.isDisconnected()) {
            portPlayerBack(par.getPlayer());
         }
      }

      i = this._teamTwoSize;

      while(--i >= 0) {
         Participant par = this._teamTwo[i];
         if (par.getPlayer() != null && !par.isDefaulted() && !par.isDisconnected()) {
            portPlayerBack(par.getPlayer());
         }
      }
   }

   @Override
   protected final void playersStatusBack() {
      int i = this._teamOneSize;

      while(--i >= 0) {
         Participant par = this._teamOne[i];
         if (par.getPlayer() != null && !par.isDefaulted() && !par.isDisconnected() && par.getPlayer().getOlympiadGameId() == this._stadiumID) {
            playerStatusBack(par.getPlayer());
         }
      }

      i = this._teamTwoSize;

      while(--i >= 0) {
         Participant par = this._teamTwo[i];
         if (par.getPlayer() != null && !par.isDefaulted() && !par.isDisconnected() && par.getPlayer().getOlympiadGameId() == this._stadiumID) {
            playerStatusBack(par.getPlayer());
         }
      }
   }

   @Override
   protected final void clearPlayers() {
      for(int i = 0; i < 3; ++i) {
         if (i < this._teamOneSize) {
            this._teamOne[i].setPlayer(null);
         } else {
            IdFactory.getInstance().releaseId(this._teamOne[i].getObjectId());
         }

         if (i < this._teamTwoSize) {
            this._teamTwo[i].setPlayer(null);
         } else {
            IdFactory.getInstance().releaseId(this._teamTwo[i].getObjectId());
         }

         this._teamOne[i] = null;
         this._teamTwo[i] = null;
      }
   }

   @Override
   public void healPlayers() {
      String enMsg1 = "";
      String ruMsg1 = "";
      String enMsg2 = "";
      String ruMsg2 = "";

      for(Participant plr : this._teamOne) {
         Player player = plr.getPlayer();
         player.setCurrentCp(player.getMaxCp());
         player.setCurrentHp(player.getMaxHp());
         player.setCurrentMp(player.getMaxMp());
         if (Config.OLY_PRINT_CLASS_OPPONENT) {
            enMsg1 = enMsg1 + ", " + Util.className("en", player.getClassId().getId()) + "";
            ruMsg1 = ruMsg1 + ", " + Util.className("ru", player.getClassId().getId()) + "";
         }
      }

      for(Participant plr : this._teamTwo) {
         Player player = plr.getPlayer();
         player.setCurrentCp(player.getMaxCp());
         player.setCurrentHp(player.getMaxHp());
         player.setCurrentMp(player.getMaxMp());
         if (Config.OLY_PRINT_CLASS_OPPONENT) {
            enMsg2 = enMsg2 + ", " + Util.className("en", player.getClassId().getId()) + "";
            ruMsg2 = ruMsg2 + ", " + Util.className("ru", player.getClassId().getId()) + "";
         }
      }

      if (Config.OLY_PRINT_CLASS_OPPONENT) {
         for(Participant plr : this._teamOne) {
            ServerMessage msg = new ServerMessage("Olympiad.OPPONENTS", plr.getPlayer().getLang());
            msg.add(plr.getPlayer().getLang().equalsIgnoreCase("ru") ? ruMsg2 : enMsg2);
            plr.getPlayer().sendPacket(new ExShowScreenMessage(msg.toString(), 5000, (byte)2, false));
         }

         for(Participant plr : this._teamTwo) {
            ServerMessage msg = new ServerMessage("Olympiad.OPPONENTS", plr.getPlayer().getLang());
            msg.add(plr.getPlayer().getLang().equalsIgnoreCase("ru") ? ruMsg1 : enMsg1);
            plr.getPlayer().sendPacket(new ExShowScreenMessage(msg.toString(), 5000, (byte)2, false));
         }
      }
   }

   @Override
   protected final void handleDisconnect(Player player) {
      int i = this._teamOneSize;

      while(--i >= 0) {
         Participant par = this._teamOne[i];
         if (par.getObjectId() == player.getObjectId()) {
            par.setDisconnected(true);
            return;
         }
      }

      i = this._teamTwoSize;

      while(--i >= 0) {
         Participant par = this._teamTwo[i];
         if (par.getObjectId() == player.getObjectId()) {
            par.setDisconnected(true);
            return;
         }
      }
   }

   @Override
   protected final boolean haveWinner() {
      if (!this.checkBattleStatus()) {
         return true;
      } else {
         boolean teamOneLost = true;
         boolean teamTwoLost = true;
         int i = this._teamOneSize;

         while(--i >= 0) {
            Participant par = this._teamOne[i];
            if (!par.isDisconnected() && par.getPlayer() != null && par.getPlayer().getOlympiadGameId() == this._stadiumID) {
               teamOneLost &= par.getPlayer().isDead();
            }
         }

         i = this._teamTwoSize;

         while(--i >= 0) {
            Participant par = this._teamTwo[i];
            if (!par.isDisconnected() && par.getPlayer() != null && par.getPlayer().getOlympiadGameId() == this._stadiumID) {
               teamTwoLost &= par.getPlayer().isDead();
            }
         }

         return teamOneLost || teamTwoLost;
      }
   }

   @Override
   protected final boolean checkBattleStatus() {
      if (this._aborted) {
         return false;
      } else if (this.teamOneAllDisconnected()) {
         return false;
      } else {
         return !this.teamTwoAllDisconnected();
      }
   }

   @Override
   protected void validateWinner(OlympiadStadiumZone stadium) {
      if (!this._aborted) {
         ExReceiveOlympiadList.OlympiadResult result = null;
         boolean tie = false;
         int winside = 0;
         List<OlympiadInfo> list1 = new ArrayList<>(3);
         List<OlympiadInfo> list2 = new ArrayList<>(3);
         boolean tOneCrash = this.teamOneAllDisconnected();
         boolean tTwoCrash = this.teamTwoAllDisconnected();
         if (!this._teamOneDefaulted && !this._teamTwoDefaulted) {
            int[] pointsTeamOne = new int[this._teamOneSize];
            int[] pointsTeamTwo = new int[this._teamTwoSize];
            int[] maxPointsTeamOne = new int[this._teamOneSize];
            int[] maxPointsTeamTwo = new int[this._teamTwoSize];
            int totalPointsTeamOne = 0;
            int totalPointsTeamTwo = 0;

            for(int i = 0; i < this._teamOneSize; ++i) {
               int points = this._teamOne[i].getStats().getInteger("olympiad_points") / this.getDivider();
               if (points <= 0) {
                  points = 1;
               } else if (points > Config.ALT_OLY_MAX_POINTS) {
                  points = Config.ALT_OLY_MAX_POINTS;
               }

               totalPointsTeamOne += points;
               pointsTeamOne[i] = points;
               maxPointsTeamOne[i] = points;
            }

            int points;
            for(int i = this._teamTwoSize; --i >= 0; maxPointsTeamTwo[i] = points) {
               points = this._teamTwo[i].getStats().getInteger("olympiad_points") / this.getDivider();
               if (points <= 0) {
                  points = 1;
               } else if (points > Config.ALT_OLY_MAX_POINTS) {
                  points = Config.ALT_OLY_MAX_POINTS;
               }

               totalPointsTeamTwo += points;
               pointsTeamTwo[i] = points;
            }

            int min = Math.min(totalPointsTeamOne, totalPointsTeamTwo);
            min = min / 3 * 3;
            double dividerOne = (double)totalPointsTeamOne / (double)min;
            double dividerTwo = (double)totalPointsTeamTwo / (double)min;
            totalPointsTeamOne = min;
            totalPointsTeamTwo = min;

            for(int i = 0; i < this._teamOneSize; ++i) {
               points = Math.max((int)((double)pointsTeamOne[i] / dividerOne), 1);
               pointsTeamOne[i] = points;
               totalPointsTeamOne -= points;
            }

            for(int i = this._teamTwoSize; --i >= 0; totalPointsTeamTwo -= points) {
               points = Math.max((int)((double)pointsTeamTwo[i] / dividerTwo), 1);
               pointsTeamTwo[i] = points;
            }

            for(int i = 0; totalPointsTeamOne > 0 && i < this._teamOneSize; ++i) {
               if (pointsTeamOne[i] < maxPointsTeamOne[i]) {
                  pointsTeamOne[i]++;
                  --totalPointsTeamOne;
               }
            }

            int i = this._teamTwoSize;

            while(totalPointsTeamTwo > 0) {
               if (--i < 0) {
                  break;
               }

               if (pointsTeamTwo[i] < maxPointsTeamTwo[i]) {
                  pointsTeamTwo[i]++;
                  --totalPointsTeamTwo;
               }
            }

            if (!tOneCrash && !tTwoCrash) {
               try {
                  double teamOneHp = 0.0;
                  double teamTwoHp = 0.0;

                  Participant par;
                  for(int ix = this._teamOneSize; --ix >= 0; par.updatePlayer()) {
                     par = this._teamOne[ix];
                     if (!par.isDisconnected() && par.getPlayer() != null && !par.getPlayer().isDead()) {
                        double hp = par.getPlayer().getCurrentHp() + par.getPlayer().getCurrentCp();
                        if (hp >= 0.5) {
                           teamOneHp += hp;
                        }
                     }
                  }

                  for(int ix = this._teamTwoSize; --ix >= 0; par.updatePlayer()) {
                     par = this._teamTwo[ix];
                     if (!par.isDisconnected() && par.getPlayer() != null && !par.getPlayer().isDead()) {
                        double hp = par.getPlayer().getCurrentHp() + par.getPlayer().getCurrentCp();
                        if (hp >= 0.5) {
                           teamTwoHp += hp;
                        }
                     }
                  }

                  if ((teamTwoHp != 0.0 || teamOneHp == 0.0) && (this._damageT1 >= this._damageT2 || teamTwoHp == 0.0 || teamOneHp == 0.0)) {
                     if ((teamOneHp != 0.0 || teamTwoHp == 0.0) && (this._damageT2 >= this._damageT1 || teamOneHp == 0.0 || teamTwoHp == 0.0)) {
                        stadium.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_ENDED_IN_A_TIE));

                        for(int ix = 0; ix < this._teamOneSize; ++ix) {
                           par = this._teamOne[ix];
                           par.updateStat("competitions_drawn", 1);
                           points = Math.min(par.getStats().getInteger("olympiad_points") / this.getDivider(), Config.ALT_OLY_MAX_POINTS);
                           this.removePointsFromParticipant(par, points);
                           list1.add(
                              new OlympiadInfo(
                                 par.getName(),
                                 par.getClanName(),
                                 par.getClanId(),
                                 par.getBaseClass(),
                                 this._damageT1,
                                 par.getStats().getInteger("olympiad_points") - points,
                                 -points
                              )
                           );
                        }

                        for(int ix = 0; ix < this._teamTwoSize; ++ix) {
                           par = this._teamTwo[ix];
                           par.updateStat("competitions_drawn", 1);
                           points = Math.min(par.getStats().getInteger("olympiad_points") / this.getDivider(), Config.ALT_OLY_MAX_POINTS);
                           this.removePointsFromParticipant(par, points);
                           list2.add(
                              new OlympiadInfo(
                                 par.getName(),
                                 par.getClanName(),
                                 par.getClanId(),
                                 par.getBaseClass(),
                                 this._damageT2,
                                 par.getStats().getInteger("olympiad_points") - points,
                                 -points
                              )
                           );
                        }

                        tie = true;
                     } else {
                        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_WON_THE_GAME);
                        sm.addString(this._teamTwo[0].getName());
                        stadium.broadcastPacket(sm);

                        for(int ix = 0; ix < this._teamOneSize; ++ix) {
                           par = this._teamOne[ix];
                           par.updateStat("competitions_lost", 1);
                           points = pointsTeamOne[ix];
                           this.removePointsFromParticipant(par, points);
                           list1.add(
                              new OlympiadInfo(
                                 par.getName(),
                                 par.getClanName(),
                                 par.getClanId(),
                                 par.getBaseClass(),
                                 this._damageT1,
                                 par.getStats().getInteger("olympiad_points") - points,
                                 -points
                              )
                           );
                        }

                        points = min / 3;

                        for(int ix = 0; ix < this._teamTwoSize; ++ix) {
                           par = this._teamTwo[ix];
                           par.updateStat("competitions_won", 1);
                           this.addPointsToParticipant(par, points);
                           list2.add(
                              new OlympiadInfo(
                                 par.getName(),
                                 par.getClanName(),
                                 par.getClanId(),
                                 par.getBaseClass(),
                                 this._damageT2,
                                 par.getStats().getInteger("olympiad_points") + points,
                                 points
                              )
                           );
                        }

                        winside = 2;

                        for(int ix = 0; ix < this._teamTwoSize; ++ix) {
                           rewardParticipant(this._teamTwo[ix].getPlayer(), this.getReward());
                        }
                     }
                  } else {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_WON_THE_GAME);
                     sm.addString(this._teamOne[0].getName());
                     stadium.broadcastPacket(sm);

                     for(int ix = 0; ix < this._teamTwoSize; ++ix) {
                        par = this._teamTwo[ix];
                        par.updateStat("competitions_lost", 1);
                        points = pointsTeamTwo[ix];
                        this.removePointsFromParticipant(par, points);
                        list2.add(
                           new OlympiadInfo(
                              par.getName(),
                              par.getClanName(),
                              par.getClanId(),
                              par.getBaseClass(),
                              this._damageT2,
                              par.getStats().getInteger("olympiad_points") - points,
                              -points
                           )
                        );
                     }

                     points = min / 3;

                     for(int ix = 0; ix < this._teamOneSize; ++ix) {
                        par = this._teamOne[ix];
                        par.updateStat("competitions_won", 1);
                        this.addPointsToParticipant(par, points);
                        list1.add(
                           new OlympiadInfo(
                              par.getName(),
                              par.getClanName(),
                              par.getClanId(),
                              par.getBaseClass(),
                              this._damageT1,
                              par.getStats().getInteger("olympiad_points") + points,
                              points
                           )
                        );
                     }

                     winside = 1;

                     for(int ix = 0; ix < this._teamOneSize; ++ix) {
                        rewardParticipant(this._teamOne[ix].getPlayer(), this.getReward());
                     }
                  }

                  int ix = this._teamOneSize;

                  while(--ix >= 0) {
                     par = this._teamOne[ix];
                     par.updateStat("competitions_done", 1);
                     par.updateStat("competitions_done_week", 1);
                     par.updateStat(this.getWeeklyMatchType(), 1);
                  }

                  ix = this._teamTwoSize;

                  while(--ix >= 0) {
                     par = this._teamTwo[ix];
                     par.updateStat("competitions_done", 1);
                     par.updateStat("competitions_done_week", 1);
                     par.updateStat(this.getWeeklyMatchType(), 1);
                  }

                  if (winside == 1) {
                     result = new ExReceiveOlympiadList.OlympiadResult(tie, winside, list1, list2);
                  } else {
                     result = new ExReceiveOlympiadList.OlympiadResult(tie, winside, list2, list1);
                  }

                  stadium.broadcastPacket(result);
               } catch (Exception var30) {
                  _log.log(Level.WARNING, "Exception on validateWinner(): " + var30.getMessage(), (Throwable)var30);
               }
            } else {
               try {
                  if (tTwoCrash && !tOneCrash) {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_WON_THE_GAME);
                     sm.addString(this._teamOne[0].getName());
                     stadium.broadcastPacket(sm);

                     for(int ix = 0; ix < this._teamTwoSize; ++ix) {
                        Participant par = this._teamTwo[ix];
                        par.updateStat("competitions_lost", 1);
                        points = pointsTeamTwo[ix];
                        this.removePointsFromParticipant(par, points);
                        list2.add(
                           new OlympiadInfo(
                              par.getName(),
                              par.getClanName(),
                              par.getClanId(),
                              par.getBaseClass(),
                              this._damageT2,
                              par.getStats().getInteger("olympiad_points") - points,
                              -points
                           )
                        );
                     }

                     points = min / 3;

                     for(int ix = 0; ix < this._teamOneSize; ++ix) {
                        Participant par = this._teamOne[ix];
                        par.updateStat("competitions_won", 1);
                        this.addPointsToParticipant(par, points);
                        list1.add(
                           new OlympiadInfo(
                              par.getName(),
                              par.getClanName(),
                              par.getClanId(),
                              par.getBaseClass(),
                              this._damageT1,
                              par.getStats().getInteger("olympiad_points") + points,
                              points
                           )
                        );
                     }

                     for(int ix = 0; ix < this._teamOneSize; ++ix) {
                        rewardParticipant(this._teamOne[ix].getPlayer(), this.getReward());
                     }

                     winside = 1;
                  } else if (tOneCrash && !tTwoCrash) {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_WON_THE_GAME);
                     sm.addString(this._teamTwo[0].getName());
                     stadium.broadcastPacket(sm);

                     for(int ix = 0; ix < this._teamOneSize; ++ix) {
                        Participant par = this._teamOne[ix];
                        par.updateStat("competitions_lost", 1);
                        points = pointsTeamOne[ix];
                        this.removePointsFromParticipant(par, points);
                        list1.add(
                           new OlympiadInfo(
                              par.getName(),
                              par.getClanName(),
                              par.getClanId(),
                              par.getBaseClass(),
                              this._damageT1,
                              par.getStats().getInteger("olympiad_points") - points,
                              -points
                           )
                        );
                     }

                     points = min / 3;

                     for(int ix = 0; ix < this._teamTwoSize; ++ix) {
                        Participant par = this._teamTwo[ix];
                        par.updateStat("competitions_won", 1);
                        this.addPointsToParticipant(par, points);
                        list2.add(
                           new OlympiadInfo(
                              par.getName(),
                              par.getClanName(),
                              par.getClanId(),
                              par.getBaseClass(),
                              this._damageT2,
                              par.getStats().getInteger("olympiad_points") + points,
                              points
                           )
                        );
                     }

                     winside = 2;

                     for(int ix = 0; ix < this._teamTwoSize; ++ix) {
                        rewardParticipant(this._teamTwo[ix].getPlayer(), this.getReward());
                     }
                  } else if (tOneCrash && tTwoCrash) {
                     stadium.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_ENDED_IN_A_TIE));
                     i = this._teamOneSize;

                     while(--i >= 0) {
                        Participant par = this._teamOne[i];
                        par.updateStat("competitions_lost", 1);
                        this.removePointsFromParticipant(par, pointsTeamOne[i]);
                        list1.add(
                           new OlympiadInfo(
                              par.getName(),
                              par.getClanName(),
                              par.getClanId(),
                              par.getBaseClass(),
                              this._damageT1,
                              par.getStats().getInteger("olympiad_points") - pointsTeamOne[i],
                              -pointsTeamOne[i]
                           )
                        );
                     }

                     i = this._teamTwoSize;

                     while(--i >= 0) {
                        Participant par = this._teamTwo[i];
                        par.updateStat("competitions_lost", 1);
                        this.removePointsFromParticipant(par, pointsTeamTwo[i]);
                        list2.add(
                           new OlympiadInfo(
                              par.getName(),
                              par.getClanName(),
                              par.getClanId(),
                              par.getBaseClass(),
                              this._damageT2,
                              par.getStats().getInteger("olympiad_points") - pointsTeamOne[i],
                              -pointsTeamOne[i]
                           )
                        );
                     }

                     tie = true;
                  }

                  i = this._teamOneSize;

                  while(--i >= 0) {
                     Participant par = this._teamOne[i];
                     par.updateStat("competitions_done", 1);
                     par.updateStat("competitions_done_week", 1);
                     par.updateStat(this.getWeeklyMatchType(), 1);
                  }

                  i = this._teamTwoSize;

                  while(--i >= 0) {
                     Participant par = this._teamTwo[i];
                     par.updateStat("competitions_done", 1);
                     par.updateStat("competitions_done_week", 1);
                     par.updateStat(this.getWeeklyMatchType(), 1);
                  }
               } catch (Exception var31) {
                  _log.log(Level.WARNING, "Exception on validateWinner(): " + var31.getMessage(), (Throwable)var31);
               }

               if (winside == 1) {
                  result = new ExReceiveOlympiadList.OlympiadResult(tie, winside, list1, list2);
               } else {
                  result = new ExReceiveOlympiadList.OlympiadResult(tie, winside, list2, list1);
               }

               stadium.broadcastPacket(result);
            }
         } else {
            try {
               if (this._teamOneDefaulted) {
                  int i = this._teamOneSize;

                  while(--i >= 0) {
                     Participant par = this._teamOne[i];
                     int points = par.getStats().getInteger("olympiad_points") / this.getDivider();
                     int val = Math.min(par.getStats().getInteger("olympiad_points") / 3, Config.ALT_OLY_MAX_POINTS);
                     this.removePointsFromParticipant(par, val);
                     list1.add(new OlympiadInfo(par.getName(), par.getClanName(), par.getClanId(), par.getBaseClass(), this._damageT1, points - val, -val));
                  }

                  winside = 2;
               }

               if (this._teamTwoDefaulted) {
                  int i = this._teamTwoSize;

                  while(--i >= 0) {
                     Participant par = this._teamTwo[i];
                     int points = par.getStats().getInteger("olympiad_points") / this.getDivider();
                     int val = Math.min(par.getStats().getInteger("olympiad_points") / 3, Config.ALT_OLY_MAX_POINTS);
                     this.removePointsFromParticipant(par, val);
                     list2.add(new OlympiadInfo(par.getName(), par.getClanName(), par.getClanId(), par.getBaseClass(), this._damageT2, points - val, -val));
                  }

                  if (winside == 2) {
                     tie = true;
                  } else {
                     winside = 1;
                  }
               }

               if (winside == 1) {
                  result = new ExReceiveOlympiadList.OlympiadResult(tie, winside, list1, list2);
               } else {
                  result = new ExReceiveOlympiadList.OlympiadResult(tie, winside, list2, list1);
               }

               stadium.broadcastPacket(result);
            } catch (Exception var32) {
               _log.log(Level.WARNING, "Exception on validateWinner(): " + var32.getMessage(), (Throwable)var32);
            }
         }
      }
   }

   @Override
   protected final void addDamage(Player player, int damage) {
      int i = this._teamOneSize;

      while(--i >= 0) {
         Participant par = this._teamOne[i];
         if (par.getObjectId() == player.getObjectId()) {
            if (!par.isDisconnected()) {
               this._damageT1 += damage;
            }

            return;
         }
      }

      i = this._teamTwoSize;

      while(--i >= 0) {
         Participant par = this._teamTwo[i];
         if (par.getObjectId() == player.getObjectId()) {
            if (!par.isDisconnected()) {
               this._damageT2 += damage;
            }

            return;
         }
      }
   }

   @Override
   public final String[] getPlayerNames() {
      return new String[]{this._teamOne[0].getName(), this._teamTwo[0].getName()};
   }

   @Override
   public final boolean checkDefaulted() {
      try {
         SystemMessage reason = null;
         int i = this._teamOneSize;

         while(--i >= 0) {
            Participant par = this._teamOne[i];
            par.updatePlayer();
            reason = checkDefaulted(par.getPlayer());
            if (reason != null) {
               par.setDefaulted(true);
               if (!this._teamOneDefaulted) {
                  this._teamOneDefaulted = true;

                  for(Participant t : this._teamTwo) {
                     if (t.getPlayer() != null) {
                        t.getPlayer().sendPacket(reason);
                     }
                  }
               }
            }
         }

         reason = null;
         i = this._teamTwoSize;

         while(--i >= 0) {
            Participant par = this._teamTwo[i];
            par.updatePlayer();
            reason = checkDefaulted(par.getPlayer());
            if (reason != null) {
               par.setDefaulted(true);
               if (!this._teamTwoDefaulted) {
                  this._teamTwoDefaulted = true;

                  for(Participant t : this._teamOne) {
                     if (t.getPlayer() != null) {
                        t.getPlayer().sendPacket(reason);
                     }
                  }
               }
            }
         }

         return this._teamOneDefaulted || this._teamTwoDefaulted;
      } catch (Exception var8) {
         _log.log(Level.WARNING, "Exception on checkDefaulted(): " + var8.getMessage(), (Throwable)var8);
         return true;
      }
   }

   @Override
   public final void resetDamage() {
      this._damageT1 = 0;
      this._damageT2 = 0;
   }

   protected final boolean teamOneAllDisconnected() {
      int i = this._teamOneSize;

      while(--i >= 0) {
         if (!this._teamOne[i].isDisconnected()) {
            return false;
         }
      }

      return true;
   }

   protected final boolean teamTwoAllDisconnected() {
      int i = this._teamTwoSize;

      while(--i >= 0) {
         if (!this._teamTwo[i].isDisconnected()) {
            return false;
         }
      }

      return true;
   }

   private class AnswerClick implements OnAnswerListener {
      private final Player _player;

      private AnswerClick(Player player) {
         this._player = player;
      }

      @Override
      public void sayYes() {
         ++OlympiadGameTeams.this._currentInvites;
         OlympiadGameTeams.this.checkAnswers(this._player);
      }

      @Override
      public void sayNo() {
      }
   }
}
