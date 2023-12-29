package l2e.gameserver.model.olympiad;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import l2e.commons.util.Rnd;
import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.database.DatabaseFactory;
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

public abstract class OlympiadGameNormal extends AbstractOlympiadGame {
   protected int _damageP1 = 0;
   protected int _damageP2 = 0;
   protected Participant _playerOne;
   protected Participant _playerTwo;
   protected int _confirmInvites;
   protected int _currentInvites;

   protected OlympiadGameNormal(int id, Participant[] opponents) {
      super(id);
      this._playerOne = opponents[0];
      this._playerTwo = opponents[1];
      this._playerOne.getPlayer().setOlympiadGameId(id);
      this._playerTwo.getPlayer().setOlympiadGameId(id);
      this._currentInvites = 0;
      this._confirmInvites = 2;
   }

   protected static final Participant[] createListOfParticipants(List<Integer> list) {
      if (list != null && !list.isEmpty() && list.size() >= 2) {
         int playerOneObjectId = 0;
         Player playerOne = null;
         Player playerTwo = null;

         while(list.size() > 1) {
            playerOneObjectId = list.remove(Rnd.nextInt(list.size()));
            playerOne = World.getInstance().getPlayer(playerOneObjectId);
            if (playerOne != null && playerOne.isOnline()) {
               playerTwo = World.getInstance().getPlayer(list.remove(Rnd.nextInt(list.size())));
               if (playerTwo != null && playerTwo.isOnline()) {
                  return new Participant[]{new Participant(playerOne, 1), new Participant(playerTwo, 2)};
               }

               list.add(playerOneObjectId);
            }
         }

         return null;
      } else {
         return null;
      }
   }

   @Override
   public final boolean containsParticipant(int playerId) {
      return this._playerOne != null && this._playerOne.getObjectId() == playerId || this._playerTwo != null && this._playerTwo.getObjectId() == playerId;
   }

   @Override
   public final boolean containsHwidParticipant(Player player) {
      return player != null && this._playerOne.getPlayer() != null && this._playerOne.getPlayer().getHWID().equalsIgnoreCase(player.getHWID())
         || this._playerTwo.getPlayer() != null && this._playerTwo.getPlayer().getHWID().equalsIgnoreCase(player.getHWID());
   }

   @Override
   public final void sendOlympiadInfo(Creature player) {
      player.sendPacket(new ExOlympiadUserInfo(this._playerOne));
      player.sendPacket(new ExOlympiadUserInfo(this._playerTwo));
   }

   @Override
   public final void broadcastOlympiadInfo(OlympiadStadiumZone stadium) {
      stadium.broadcastPacket(new ExOlympiadUserInfo(this._playerOne));
      stadium.broadcastPacket(new ExOlympiadUserInfo(this._playerTwo));
   }

   @Override
   protected final void broadcastPacket(GameServerPacket packet) {
      if (this._playerOne.updatePlayer()) {
         this._playerOne.getPlayer().sendPacket(packet);
      }

      if (this._playerTwo.updatePlayer()) {
         this._playerTwo.getPlayer().sendPacket(packet);
      }
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
      if (this._playerOne.getPlayer() != null) {
         this._playerOne
            .getPlayer()
            .sendConfirmDlg(
               new OlympiadGameNormal.AnswerClick(this._playerOne.getPlayer()),
               10000,
               new ServerMessage("Olympiad.INVITE", this._playerOne.getPlayer().getLang()).toString()
            );
      }

      if (this._playerTwo.getPlayer() != null) {
         this._playerTwo
            .getPlayer()
            .sendConfirmDlg(
               new OlympiadGameNormal.AnswerClick(this._playerTwo.getPlayer()),
               10000,
               new ServerMessage("Olympiad.INVITE", this._playerTwo.getPlayer().getLang()).toString()
            );
      }
   }

   @Override
   protected final boolean portPlayersToArena(List<Location> spawns) {
      boolean result = true;

      try {
         this.setOlympiadGame(this._playerOne, this);
         this.setOlympiadGame(this._playerTwo, this);
         result &= portPlayerToArena(this._playerOne, spawns.get(0), this._stadiumID);
         return result & portPlayerToArena(this._playerTwo, spawns.get(spawns.size() / 2), this._stadiumID);
      } catch (Exception var4) {
         _log.log(Level.WARNING, "", (Throwable)var4);
         return false;
      }
   }

   @Override
   protected boolean needBuffers() {
      return true;
   }

   @Override
   protected final void removals() {
      if (!this._aborted) {
         removals(this._playerOne.getPlayer(), true);
         removals(this._playerTwo.getPlayer(), true);
      }
   }

   @Override
   protected final boolean makeCompetitionStart() {
      if (!super.makeCompetitionStart()) {
         return false;
      } else if (this._playerOne.getPlayer() != null && this._playerTwo.getPlayer() != null) {
         this._playerOne.getPlayer().setIsOlympiadStart(true);
         this._playerOne.getPlayer().updateEffectIcons();
         this._playerTwo.getPlayer().setIsOlympiadStart(true);
         this._playerTwo.getPlayer().updateEffectIcons();
         return true;
      } else {
         return false;
      }
   }

   @Override
   protected final void restorePlayers() {
      if (this._playerOne.getPlayer() != null
         && !this._playerOne.isDefaulted()
         && !this._playerOne.isDisconnected()
         && this._playerOne.getPlayer().getOlympiadGameId() == this._stadiumID) {
         restorePlayer(this._playerOne.getPlayer());
      }

      if (this._playerTwo.getPlayer() != null
         && !this._playerTwo.isDefaulted()
         && !this._playerTwo.isDisconnected()
         && this._playerTwo.getPlayer().getOlympiadGameId() == this._stadiumID) {
         restorePlayer(this._playerTwo.getPlayer());
      }
   }

   @Override
   protected final void cleanEffects() {
      if (this._playerOne.getPlayer() != null
         && !this._playerOne.isDefaulted()
         && !this._playerOne.isDisconnected()
         && this._playerOne.getPlayer().getOlympiadGameId() == this._stadiumID) {
         cleanEffects(this._playerOne.getPlayer());
      }

      if (this._playerTwo.getPlayer() != null
         && !this._playerTwo.isDefaulted()
         && !this._playerTwo.isDisconnected()
         && this._playerTwo.getPlayer().getOlympiadGameId() == this._stadiumID) {
         cleanEffects(this._playerTwo.getPlayer());
      }
   }

   @Override
   protected final void portPlayersBack() {
      if (this._playerOne.getPlayer() != null && !this._playerOne.isDefaulted() && !this._playerOne.isDisconnected()) {
         portPlayerBack(this._playerOne.getPlayer());
      }

      if (this._playerTwo.getPlayer() != null && !this._playerTwo.isDefaulted() && !this._playerTwo.isDisconnected()) {
         portPlayerBack(this._playerTwo.getPlayer());
      }
   }

   @Override
   protected final void playersStatusBack() {
      if (this._playerOne.getPlayer() != null
         && !this._playerOne.isDefaulted()
         && !this._playerOne.isDisconnected()
         && this._playerOne.getPlayer().getOlympiadGameId() == this._stadiumID) {
         playerStatusBack(this._playerOne.getPlayer());
      }

      if (this._playerTwo.getPlayer() != null
         && !this._playerTwo.isDefaulted()
         && !this._playerTwo.isDisconnected()
         && this._playerTwo.getPlayer().getOlympiadGameId() == this._stadiumID) {
         playerStatusBack(this._playerTwo.getPlayer());
      }
   }

   @Override
   protected final void clearPlayers() {
      this._playerOne.setPlayer(null);
      this._playerOne = null;
      this._playerTwo.setPlayer(null);
      this._playerTwo = null;
   }

   @Override
   public void healPlayers() {
      Player playerOne = this._playerOne.getPlayer();
      if (playerOne != null) {
         playerOne.setCurrentCp(playerOne.getMaxCp());
         playerOne.setCurrentHp(playerOne.getMaxHp());
         playerOne.setCurrentMp(playerOne.getMaxMp());
         if (Config.OLY_PRINT_CLASS_OPPONENT && this._playerTwo.getPlayer() != null) {
            ServerMessage msg = new ServerMessage("Olympiad.OPPONENT", playerOne.getLang());
            msg.add(Util.className(playerOne, this._playerTwo.getPlayer().getClassId().getId()));
            playerOne.sendPacket(new ExShowScreenMessage(msg.toString(), 5000, (byte)2, true));
         }
      }

      Player playerTwo = this._playerTwo.getPlayer();
      if (playerTwo != null) {
         playerTwo.setCurrentCp(playerTwo.getMaxCp());
         playerTwo.setCurrentHp(playerTwo.getMaxHp());
         playerTwo.setCurrentMp(playerTwo.getMaxMp());
         if (Config.OLY_PRINT_CLASS_OPPONENT && this._playerOne.getPlayer() != null) {
            ServerMessage msg = new ServerMessage("Olympiad.OPPONENT", playerTwo.getLang());
            msg.add(Util.className(playerTwo, this._playerOne.getPlayer().getClassId().getId()));
            playerTwo.sendPacket(new ExShowScreenMessage(msg.toString(), 5000, (byte)2, true));
         }
      }
   }

   @Override
   protected final void handleDisconnect(Player player) {
      if (player.getObjectId() == this._playerOne.getObjectId()) {
         this._playerOne.setDisconnected(true);
      } else if (player.getObjectId() == this._playerTwo.getObjectId()) {
         this._playerTwo.setDisconnected(true);
      }
   }

   @Override
   protected final boolean checkBattleStatus() {
      if (this._aborted) {
         return false;
      } else if (this._playerOne.getPlayer() == null || this._playerOne.isDisconnected()) {
         return false;
      } else {
         return this._playerTwo.getPlayer() != null && !this._playerTwo.isDisconnected();
      }
   }

   @Override
   protected final boolean haveWinner() {
      if (!this.checkBattleStatus()) {
         return true;
      } else {
         boolean playerOneLost = true;

         try {
            if (this._playerOne.getPlayer().getOlympiadGameId() == this._stadiumID) {
               playerOneLost = this._playerOne.getPlayer().isDead();
            }
         } catch (Exception var5) {
            playerOneLost = true;
         }

         boolean playerTwoLost = true;

         try {
            if (this._playerTwo.getPlayer().getOlympiadGameId() == this._stadiumID) {
               playerTwoLost = this._playerTwo.getPlayer().isDead();
            }
         } catch (Exception var4) {
            playerTwoLost = true;
         }

         return playerOneLost || playerTwoLost;
      }
   }

   @Override
   protected void validateWinner(OlympiadStadiumZone stadium) {
      if (!this._aborted) {
         ExReceiveOlympiadList.OlympiadResult result = null;
         boolean tie = false;
         int winside = 0;
         List<OlympiadInfo> list1 = new ArrayList<>(1);
         List<OlympiadInfo> list2 = new ArrayList<>(1);
         boolean _pOneCrash = this._playerOne.getPlayer() == null || this._playerOne.isDisconnected();
         boolean _pTwoCrash = this._playerTwo.getPlayer() == null || this._playerTwo.isDisconnected();
         int playerOnePoints = this._playerOne.getStats().getInteger("olympiad_points");
         int playerTwoPoints = this._playerTwo.getStats().getInteger("olympiad_points");
         int pointDiff = Math.min(playerOnePoints, playerTwoPoints) / this.getDivider();
         if (pointDiff <= 0) {
            pointDiff = 1;
         } else if (pointDiff > Config.ALT_OLY_MAX_POINTS) {
            pointDiff = Config.ALT_OLY_MAX_POINTS;
         }

         if (this._playerOne.isDefaulted() || this._playerTwo.isDefaulted()) {
            try {
               if (this._playerOne.isDefaulted()) {
                  try {
                     int points = Math.min(playerOnePoints / 3, Config.ALT_OLY_MAX_POINTS);
                     this.removePointsFromParticipant(this._playerOne, points);
                     list1.add(
                        new OlympiadInfo(
                           this._playerOne.getName(),
                           this._playerOne.getClanName(),
                           this._playerOne.getClanId(),
                           this._playerOne.getBaseClass(),
                           this._damageP1,
                           playerOnePoints - points,
                           -points
                        )
                     );
                     winside = 2;
                     if (Config.ALT_OLY_LOG_FIGHTS) {
                        LogRecord record = new LogRecord(Level.INFO, this._playerOne.getName() + " default");
                        record.setParameters(new Object[]{this._playerOne.getName(), this._playerTwo.getName(), 0, 0, 0, 0, points, this.getType().toString()});
                        _logResults.log(record);
                     }
                  } catch (Exception var23) {
                     _log.log(Level.WARNING, "Exception on validateWinner(): " + var23.getMessage(), (Throwable)var23);
                  }
               }

               if (this._playerTwo.isDefaulted()) {
                  try {
                     int points = Math.min(playerTwoPoints / 3, Config.ALT_OLY_MAX_POINTS);
                     this.removePointsFromParticipant(this._playerTwo, points);
                     list2.add(
                        new OlympiadInfo(
                           this._playerTwo.getName(),
                           this._playerTwo.getClanName(),
                           this._playerTwo.getClanId(),
                           this._playerTwo.getBaseClass(),
                           this._damageP2,
                           playerTwoPoints - points,
                           -points
                        )
                     );
                     if (winside == 2) {
                        tie = true;
                     } else {
                        winside = 1;
                     }

                     if (Config.ALT_OLY_LOG_FIGHTS) {
                        LogRecord record = new LogRecord(Level.INFO, this._playerTwo.getName() + " default");
                        record.setParameters(new Object[]{this._playerOne.getName(), this._playerTwo.getName(), 0, 0, 0, 0, points, this.getType().toString()});
                        _logResults.log(record);
                     }
                  } catch (Exception var22) {
                     _log.log(Level.WARNING, "Exception on validateWinner(): " + var22.getMessage(), (Throwable)var22);
                  }
               }

               if (winside == 1) {
                  result = new ExReceiveOlympiadList.OlympiadResult(tie, winside, list1, list2);
               } else {
                  result = new ExReceiveOlympiadList.OlympiadResult(tie, winside, list2, list1);
               }

               stadium.broadcastPacket(result);
            } catch (Exception var24) {
               _log.log(Level.WARNING, "Exception on validateWinner(): " + var24.getMessage(), (Throwable)var24);
            }
         } else if (!_pOneCrash && !_pTwoCrash) {
            try {
               String winner = "draw";
               long _fightTime = System.currentTimeMillis() - this._startTime;
               double playerOneHp = 0.0;
               if (this._playerOne.getPlayer() != null && !this._playerOne.getPlayer().isDead()) {
                  playerOneHp = this._playerOne.getPlayer().getCurrentHp() + this._playerOne.getPlayer().getCurrentCp();
                  if (playerOneHp < 0.5) {
                     playerOneHp = 0.0;
                  }
               }

               double playerTwoHp = 0.0;
               if (this._playerTwo.getPlayer() != null && !this._playerTwo.getPlayer().isDead()) {
                  playerTwoHp = this._playerTwo.getPlayer().getCurrentHp() + this._playerTwo.getPlayer().getCurrentCp();
                  if (playerTwoHp < 0.5) {
                     playerTwoHp = 0.0;
                  }
               }

               this._playerOne.updatePlayer();
               this._playerTwo.updatePlayer();
               if (this._playerOne.getPlayer() != null && this._playerOne.getPlayer().isOnline()
                  || this._playerTwo.getPlayer() != null && this._playerTwo.getPlayer().isOnline()) {
                  if (this._playerTwo.getPlayer() != null
                     && this._playerTwo.getPlayer().isOnline()
                     && (playerTwoHp != 0.0 || playerOneHp == 0.0)
                     && (this._damageP1 >= this._damageP2 || playerTwoHp == 0.0 || playerOneHp == 0.0)) {
                     if (this._playerOne.getPlayer() == null
                        || !this._playerOne.getPlayer().isOnline()
                        || playerOneHp == 0.0 && playerTwoHp != 0.0
                        || this._damageP2 < this._damageP1 && playerOneHp != 0.0 && playerTwoHp != 0.0) {
                        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_WON_THE_GAME);
                        sm.addString(this._playerTwo.getName());
                        stadium.broadcastPacket(sm);
                        this._playerTwo.updateStat("competitions_won", 1);
                        this._playerOne.updateStat("competitions_lost", 1);
                        if (this._playerTwo.getPlayer() != null) {
                           this._playerTwo.getPlayer().getCounters().addAchivementInfo("olympiadWon", 0, -1L, false, false, false);
                           this._playerTwo.getPlayer().getCounters().addAchivementInfo("olympiadPoints", 0, (long)pointDiff, false, false, false);
                        }

                        if (this._playerOne.getPlayer() != null) {
                           this._playerOne.getPlayer().getCounters().addAchivementInfo("olympiadLose", 0, -1L, false, false, false);
                        }

                        this.addPointsToParticipant(this._playerTwo, pointDiff);
                        list2.add(
                           new OlympiadInfo(
                              this._playerTwo.getName(),
                              this._playerTwo.getClanName(),
                              this._playerTwo.getClanId(),
                              this._playerTwo.getBaseClass(),
                              this._damageP2,
                              playerTwoPoints + pointDiff,
                              pointDiff
                           )
                        );
                        this.removePointsFromParticipant(this._playerOne, pointDiff);
                        list1.add(
                           new OlympiadInfo(
                              this._playerOne.getName(),
                              this._playerOne.getClanName(),
                              this._playerOne.getClanId(),
                              this._playerOne.getBaseClass(),
                              this._damageP1,
                              playerOnePoints - pointDiff,
                              -pointDiff
                           )
                        );
                        winner = this._playerTwo.getName() + " won";
                        winside = 2;
                        saveResults(this._playerOne, this._playerTwo, 2, this._startTime, _fightTime, this.getType());
                        rewardParticipant(this._playerTwo.getPlayer(), this.getReward());
                     } else {
                        saveResults(this._playerOne, this._playerTwo, 0, this._startTime, _fightTime, this.getType());
                        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_ENDED_IN_A_TIE);
                        stadium.broadcastPacket(sm);
                        int value = Math.min(playerOnePoints / this.getDivider(), Config.ALT_OLY_MAX_POINTS);
                        this.removePointsFromParticipant(this._playerOne, value);
                        list1.add(
                           new OlympiadInfo(
                              this._playerOne.getName(),
                              this._playerOne.getClanName(),
                              this._playerOne.getClanId(),
                              this._playerOne.getBaseClass(),
                              this._damageP1,
                              playerOnePoints - value,
                              -value
                           )
                        );
                        value = Math.min(playerTwoPoints / this.getDivider(), Config.ALT_OLY_MAX_POINTS);
                        this.removePointsFromParticipant(this._playerTwo, value);
                        list2.add(
                           new OlympiadInfo(
                              this._playerTwo.getName(),
                              this._playerTwo.getClanName(),
                              this._playerTwo.getClanId(),
                              this._playerTwo.getBaseClass(),
                              this._damageP2,
                              playerTwoPoints - value,
                              -value
                           )
                        );
                        tie = true;
                     }
                  } else {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_WON_THE_GAME);
                     sm.addString(this._playerOne.getName());
                     stadium.broadcastPacket(sm);
                     this._playerOne.updateStat("competitions_won", 1);
                     this._playerTwo.updateStat("competitions_lost", 1);
                     if (this._playerOne.getPlayer() != null) {
                        this._playerOne.getPlayer().getCounters().addAchivementInfo("olympiadWon", 0, -1L, false, false, false);
                        this._playerOne.getPlayer().getCounters().addAchivementInfo("olympiadPoints", 0, (long)pointDiff, false, false, false);
                     }

                     if (this._playerTwo.getPlayer() != null) {
                        this._playerTwo.getPlayer().getCounters().addAchivementInfo("olympiadLose", 0, -1L, false, false, false);
                     }

                     this.addPointsToParticipant(this._playerOne, pointDiff);
                     list1.add(
                        new OlympiadInfo(
                           this._playerOne.getName(),
                           this._playerOne.getClanName(),
                           this._playerOne.getClanId(),
                           this._playerOne.getBaseClass(),
                           this._damageP1,
                           playerOnePoints + pointDiff,
                           pointDiff
                        )
                     );
                     this.removePointsFromParticipant(this._playerTwo, pointDiff);
                     list2.add(
                        new OlympiadInfo(
                           this._playerTwo.getName(),
                           this._playerTwo.getClanName(),
                           this._playerTwo.getClanId(),
                           this._playerTwo.getBaseClass(),
                           this._damageP2,
                           playerTwoPoints - pointDiff,
                           -pointDiff
                        )
                     );
                     winner = this._playerOne.getName() + " won";
                     winside = 1;
                     saveResults(this._playerOne, this._playerTwo, 1, this._startTime, _fightTime, this.getType());
                     rewardParticipant(this._playerOne.getPlayer(), this.getReward());
                  }
               } else {
                  this._playerOne.updateStat("competitions_drawn", 1);
                  this._playerTwo.updateStat("competitions_drawn", 1);
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_ENDED_IN_A_TIE);
                  stadium.broadcastPacket(sm);
               }

               this._playerOne.updateStat("competitions_done", 1);
               this._playerTwo.updateStat("competitions_done", 1);
               this._playerOne.updateStat("competitions_done_week", 1);
               this._playerTwo.updateStat("competitions_done_week", 1);
               this._playerOne.updateStat(this.getWeeklyMatchType(), 1);
               this._playerTwo.updateStat(this.getWeeklyMatchType(), 1);
               if (winside == 1) {
                  result = new ExReceiveOlympiadList.OlympiadResult(tie, winside, list1, list2);
               } else {
                  result = new ExReceiveOlympiadList.OlympiadResult(tie, winside, list2, list1);
               }

               stadium.broadcastPacket(result);
               if (Config.ALT_OLY_LOG_FIGHTS) {
                  LogRecord record = new LogRecord(Level.INFO, winner);
                  record.setParameters(
                     new Object[]{
                        this._playerOne.getName(),
                        this._playerTwo.getName(),
                        playerOneHp,
                        playerTwoHp,
                        this._damageP1,
                        this._damageP2,
                        pointDiff,
                        this.getType().toString()
                     }
                  );
                  _logResults.log(record);
               }
            } catch (Exception var25) {
               _log.log(Level.WARNING, "Exception on validateWinner(): " + var25.getMessage(), (Throwable)var25);
            }
         } else {
            try {
               if (_pTwoCrash && !_pOneCrash) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_WON_THE_GAME);
                  sm.addString(this._playerOne.getName());
                  stadium.broadcastPacket(sm);
                  this._playerOne.updateStat("competitions_won", 1);
                  this.addPointsToParticipant(this._playerOne, pointDiff);
                  list1.add(
                     new OlympiadInfo(
                        this._playerOne.getName(),
                        this._playerOne.getClanName(),
                        this._playerOne.getClanId(),
                        this._playerOne.getBaseClass(),
                        this._damageP1,
                        playerOnePoints + pointDiff,
                        pointDiff
                     )
                  );
                  this._playerTwo.updateStat("competitions_lost", 1);
                  this.removePointsFromParticipant(this._playerTwo, pointDiff);
                  list2.add(
                     new OlympiadInfo(
                        this._playerTwo.getName(),
                        this._playerTwo.getClanName(),
                        this._playerTwo.getClanId(),
                        this._playerTwo.getBaseClass(),
                        this._damageP2,
                        playerTwoPoints - pointDiff,
                        -pointDiff
                     )
                  );
                  winside = 1;
                  rewardParticipant(this._playerOne.getPlayer(), this.getReward());
                  if (Config.ALT_OLY_LOG_FIGHTS) {
                     LogRecord record = new LogRecord(Level.INFO, this._playerTwo.getName() + " crash");
                     record.setParameters(new Object[]{this._playerOne.getName(), this._playerTwo.getName(), 0, 0, 0, 0, pointDiff, this.getType().toString()});
                     _logResults.log(record);
                  }
               } else if (_pOneCrash && !_pTwoCrash) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_WON_THE_GAME);
                  sm.addString(this._playerTwo.getName());
                  stadium.broadcastPacket(sm);
                  this._playerTwo.updateStat("competitions_won", 1);
                  this.addPointsToParticipant(this._playerTwo, pointDiff);
                  list2.add(
                     new OlympiadInfo(
                        this._playerTwo.getName(),
                        this._playerTwo.getClanName(),
                        this._playerTwo.getClanId(),
                        this._playerTwo.getBaseClass(),
                        this._damageP2,
                        playerTwoPoints + pointDiff,
                        pointDiff
                     )
                  );
                  this._playerOne.updateStat("competitions_lost", 1);
                  this.removePointsFromParticipant(this._playerOne, pointDiff);
                  list1.add(
                     new OlympiadInfo(
                        this._playerOne.getName(),
                        this._playerOne.getClanName(),
                        this._playerOne.getClanId(),
                        this._playerOne.getBaseClass(),
                        this._damageP1,
                        playerOnePoints - pointDiff,
                        -pointDiff
                     )
                  );
                  winside = 2;
                  rewardParticipant(this._playerTwo.getPlayer(), this.getReward());
                  if (Config.ALT_OLY_LOG_FIGHTS) {
                     LogRecord record = new LogRecord(Level.INFO, this._playerOne.getName() + " crash");
                     record.setParameters(new Object[]{this._playerOne.getName(), this._playerTwo.getName(), 0, 0, 0, 0, pointDiff, this.getType().toString()});
                     _logResults.log(record);
                  }
               } else if (_pOneCrash && _pTwoCrash) {
                  stadium.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_ENDED_IN_A_TIE));
                  this._playerOne.updateStat("competitions_lost", 1);
                  this.removePointsFromParticipant(this._playerOne, pointDiff);
                  list1.add(
                     new OlympiadInfo(
                        this._playerOne.getName(),
                        this._playerOne.getClanName(),
                        this._playerOne.getClanId(),
                        this._playerOne.getBaseClass(),
                        this._damageP1,
                        playerOnePoints - pointDiff,
                        -pointDiff
                     )
                  );
                  this._playerTwo.updateStat("competitions_lost", 1);
                  this.removePointsFromParticipant(this._playerTwo, pointDiff);
                  list2.add(
                     new OlympiadInfo(
                        this._playerTwo.getName(),
                        this._playerTwo.getClanName(),
                        this._playerTwo.getClanId(),
                        this._playerTwo.getBaseClass(),
                        this._damageP2,
                        playerTwoPoints - pointDiff,
                        -pointDiff
                     )
                  );
                  tie = true;
                  if (Config.ALT_OLY_LOG_FIGHTS) {
                     LogRecord record = new LogRecord(Level.INFO, "both crash");
                     record.setParameters(new Object[]{this._playerOne.getName(), this._playerTwo.getName(), 0, 0, 0, 0, pointDiff, this.getType().toString()});
                     _logResults.log(record);
                  }
               }

               this._playerOne.updateStat("competitions_done", 1);
               this._playerTwo.updateStat("competitions_done", 1);
               this._playerOne.updateStat("competitions_done_week", 1);
               this._playerTwo.updateStat("competitions_done_week", 1);
               this._playerOne.updateStat(this.getWeeklyMatchType(), 1);
               this._playerTwo.updateStat(this.getWeeklyMatchType(), 1);
               if (winside == 1) {
                  result = new ExReceiveOlympiadList.OlympiadResult(tie, winside, list1, list2);
               } else {
                  result = new ExReceiveOlympiadList.OlympiadResult(tie, winside, list2, list1);
               }

               stadium.broadcastPacket(result);
            } catch (Exception var26) {
               _log.log(Level.WARNING, "Exception on validateWinner(): " + var26.getMessage(), (Throwable)var26);
            }
         }
      }
   }

   @Override
   protected final void addDamage(Player player, int damage) {
      if (this._playerOne.getPlayer() != null && this._playerTwo.getPlayer() != null) {
         if (player == this._playerOne.getPlayer()) {
            this._damageP1 += damage;
         } else if (player == this._playerTwo.getPlayer()) {
            this._damageP2 += damage;
         }
      }
   }

   @Override
   public final String[] getPlayerNames() {
      return new String[]{this._playerOne.getName(), this._playerTwo.getName()};
   }

   @Override
   public boolean checkDefaulted() {
      this._playerOne.updatePlayer();
      this._playerTwo.updatePlayer();
      SystemMessage reason = checkDefaulted(this._playerOne.getPlayer());
      if (reason != null) {
         this._playerOne.setDefaulted(true);
         if (this._playerTwo.getPlayer() != null) {
            this._playerTwo.getPlayer().sendPacket(reason);
         }
      }

      reason = checkDefaulted(this._playerTwo.getPlayer());
      if (reason != null) {
         this._playerTwo.setDefaulted(true);
         if (this._playerOne.getPlayer() != null) {
            this._playerOne.getPlayer().sendPacket(reason);
         }
      }

      return this._playerOne.isDefaulted() || this._playerTwo.isDefaulted();
   }

   @Override
   public final void resetDamage() {
      this._damageP1 = 0;
      this._damageP2 = 0;
   }

   protected static final void saveResults(Participant one, Participant two, int _winner, long _startTime, long _fightTime, CompetitionType type) {
      try (
         Connection con = DatabaseFactory.getInstance().getConnection();
         PreparedStatement statement = con.prepareStatement(
            "INSERT INTO olympiad_fights (charOneId, charTwoId, charOneClass, charTwoClass, winner, start, time, classed) values(?,?,?,?,?,?,?,?)"
         );
      ) {
         statement.setInt(1, one.getObjectId());
         statement.setInt(2, two.getObjectId());
         statement.setInt(3, one.getBaseClass());
         statement.setInt(4, two.getBaseClass());
         statement.setInt(5, _winner);
         statement.setLong(6, _startTime);
         statement.setLong(7, _fightTime);
         statement.setInt(8, type == CompetitionType.CLASSED ? 1 : 0);
         statement.execute();
      } catch (SQLException var40) {
         if (_log.isLoggable(Level.SEVERE)) {
            _log.log(Level.SEVERE, "SQL exception while saving olympiad fight.", (Throwable)var40);
         }
      }
   }

   private class AnswerClick implements OnAnswerListener {
      private final Player _player;

      private AnswerClick(Player player) {
         this._player = player;
      }

      @Override
      public void sayYes() {
         ++OlympiadGameNormal.this._currentInvites;
         OlympiadGameNormal.this.checkAnswers(this._player);
      }

      @Override
      public void sayNo() {
      }
   }
}
