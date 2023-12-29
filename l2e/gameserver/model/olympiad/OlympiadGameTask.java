package l2e.gameserver.model.olympiad;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.zone.type.OlympiadStadiumZone;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SystemMessage;

public final class OlympiadGameTask implements Runnable {
   protected static final Logger _log = Logger.getLogger(OlympiadGameTask.class.getName());
   public static final int[] TELEPORT_TO_ARENA = new int[]{
      120,
      119,
      118,
      117,
      116,
      115,
      114,
      113,
      112,
      111,
      110,
      109,
      60,
      59,
      58,
      57,
      56,
      55,
      54,
      53,
      52,
      51,
      50,
      49,
      30,
      29,
      28,
      27,
      26,
      25,
      24,
      23,
      22,
      21,
      20,
      19,
      15,
      10,
      5,
      4,
      3,
      2,
      1,
      0
   };
   public static final int[] BATTLE_START_TIME_FIRST = new int[]{60, 55, 50, 40, 30, 20, 10, 0};
   public static final int[] BATTLE_START_TIME_SECOND = new int[]{10, 5, 4, 3, 2, 1, 0};
   public static final int[] TELEPORT_TO_TOWN = new int[]{40, 30, 20, 10, 5, 4, 3, 2, 1, 0};
   private static final List<Integer> BATTLE_END_TIME_SECOND = Arrays.asList(120, 60, 30, 10, 5, 4, 3, 2, 1, 0);
   private final OlympiadStadiumZone _zone;
   private AbstractOlympiadGame _game;
   private final int _id;
   private OlympiadGameTask.GameState _state = OlympiadGameTask.GameState.IDLE;
   private boolean _needAnnounce = false;
   private int _countDown = 0;

   public OlympiadGameTask(int id, OlympiadStadiumZone zone) {
      this._id = id;
      this._zone = zone;
      zone.registerTask(this);
   }

   public final boolean isRunning() {
      return this._state != OlympiadGameTask.GameState.IDLE;
   }

   public final boolean isPrepareTeleport() {
      return this._state == OlympiadGameTask.GameState.TELEPORT_TO_ARENA;
   }

   public final boolean isGameStarted() {
      return this._state.ordinal() >= OlympiadGameTask.GameState.GAME_STARTED.ordinal()
         && this._state.ordinal() <= OlympiadGameTask.GameState.CLEANUP.ordinal();
   }

   public final boolean isBattleStarted() {
      return this._state == OlympiadGameTask.GameState.BATTLE_IN_PROGRESS;
   }

   public final boolean isBattleFinished() {
      return this._state == OlympiadGameTask.GameState.TELEPORT_TO_TOWN;
   }

   public final boolean needAnnounce() {
      if (this._needAnnounce) {
         this._needAnnounce = false;
         return true;
      } else {
         return false;
      }
   }

   public final int getId() {
      return this._id;
   }

   public final OlympiadStadiumZone getZone() {
      return this._zone;
   }

   public final AbstractOlympiadGame getGame() {
      return this._game;
   }

   public final void attachGame(AbstractOlympiadGame game) {
      if (game != null && this._state != OlympiadGameTask.GameState.IDLE) {
         _log.log(Level.WARNING, "Attempt to overwrite non-finished game in state " + this._state);
      } else {
         this._game = game;
         this._game.setZone(this._zone);
         this._state = OlympiadGameTask.GameState.BEGIN;
         this._needAnnounce = false;
         ThreadPoolManager.getInstance().execute(this);
      }
   }

   public final void quickTeleport() {
      this._countDown = 0;
   }

   @Override
   public final void run() {
      try {
         int delay = 1;
         switch(this._state) {
            case BEGIN:
               this._state = OlympiadGameTask.GameState.TELEPORT_TO_ARENA;
               this._countDown = Config.ALT_OLY_WAIT_TIME;
               break;
            case TELEPORT_TO_ARENA:
               if (this._countDown > 0) {
                  if (Config.ALLOW_OLY_FAST_INVITE && (this._countDown == 120 || this._countDown == 60 || this._countDown == 30)) {
                     this._game.confirmDlgInvite();
                  }

                  if (this._countDown == 120
                     || this._countDown == 60
                     || this._countDown == 30
                     || this._countDown == 15
                     || this._countDown == 10
                     || this._countDown == 5
                     || this._countDown == 4
                     || this._countDown == 3
                     || this._countDown == 2
                     || this._countDown == 1) {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_WILL_ENTER_THE_OLYMPIAD_STADIUM_IN_S1_SECOND_S);
                     sm.addNumber(this._countDown);
                     this._game.broadcastPacket(sm);
                  }
               }

               delay = this.getDelay(TELEPORT_TO_ARENA);
               if (this._countDown <= 0) {
                  this._state = OlympiadGameTask.GameState.GAME_STARTED;
               }
               break;
            case GAME_STARTED:
               if (!this.startGame()) {
                  this._state = OlympiadGameTask.GameState.GAME_STOPPED;
               } else {
                  this._state = OlympiadGameTask.GameState.BATTLE_COUNTDOWN_FIRST;
                  this._countDown = BATTLE_START_TIME_FIRST[0];
                  delay = 5;
               }
               break;
            case BATTLE_COUNTDOWN_FIRST:
               if (this._countDown > 0) {
                  if (this._countDown == 55) {
                     this._game.healPlayers();
                  } else {
                     SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_WILL_START_IN_S1_SECOND_S);
                     sm.addNumber(this._countDown);
                     this._zone.broadcastPacket(sm);
                  }
               }

               delay = this.getDelay(BATTLE_START_TIME_FIRST);
               if (this._countDown <= 0) {
                  this.openDoors();
                  this._state = OlympiadGameTask.GameState.BATTLE_COUNTDOWN_SECOND;
                  this._countDown = BATTLE_START_TIME_SECOND[0];
                  delay = this.getDelay(BATTLE_START_TIME_SECOND);
               }
               break;
            case BATTLE_COUNTDOWN_SECOND:
               if (this._countDown > 0) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_WILL_START_IN_S1_SECOND_S);
                  sm.addNumber(this._countDown);
                  this._zone.broadcastPacket(sm);
               }

               delay = this.getDelay(BATTLE_START_TIME_SECOND);
               if (this._countDown <= 0) {
                  this._state = OlympiadGameTask.GameState.BATTLE_STARTED;
               }
               break;
            case BATTLE_STARTED:
               this._countDown = 0;
               this._state = OlympiadGameTask.GameState.BATTLE_IN_PROGRESS;
               if (!this.startBattle()) {
                  this._state = OlympiadGameTask.GameState.GAME_STOPPED;
               }
               break;
            case BATTLE_IN_PROGRESS:
               int remaining = (int)((Config.ALT_OLY_BATTLE * 60000L - (long)this._countDown) / 1000L);
               if (BATTLE_END_TIME_SECOND.contains(remaining)) {
                  this._game.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.GAME_ENDS_IN_S1_SECONDS).addInt(remaining));
               }

               this._countDown += 1000;
               if (this.checkBattle() || (long)this._countDown > Config.ALT_OLY_BATTLE * 60000L) {
                  this._state = OlympiadGameTask.GameState.GAME_STOPPED;
               }
               break;
            case GAME_STOPPED:
               this._state = OlympiadGameTask.GameState.TELEPORT_TO_TOWN;
               this._countDown = Config.ALT_OLY_TELE_TO_TOWN;
               this.stopGame();
               delay = this.getDelay(TELEPORT_TO_TOWN);
               break;
            case TELEPORT_TO_TOWN:
               if (this._countDown > 0) {
                  SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_WILL_BE_MOVED_TO_TOWN_IN_S1_SECONDS);
                  sm.addNumber(this._countDown);
                  this._game.broadcastPacket(sm);
               }

               delay = this.getDelay(TELEPORT_TO_TOWN);
               if (this._countDown <= 0) {
                  this._state = OlympiadGameTask.GameState.CLEANUP;
                  this.cleanupGame();
                  delay = 1;
               }
               break;
            case CLEANUP:
               this._state = OlympiadGameTask.GameState.IDLE;
               this._game = null;
               return;
         }

         ThreadPoolManager.getInstance().schedule(this, (long)(delay * 1000));
      } catch (Exception var3) {
         switch(this._state) {
            case GAME_STOPPED:
            case TELEPORT_TO_TOWN:
            case CLEANUP:
            case IDLE:
               _log.log(Level.WARNING, "Unable to return players back in town, exception: " + var3.getMessage());
               this._state = OlympiadGameTask.GameState.IDLE;
               this._game = null;
               return;
            default:
               _log.log(Level.WARNING, "Exception in " + this._state + ", trying to port players back: " + var3.getMessage(), (Throwable)var3);
               this._state = OlympiadGameTask.GameState.GAME_STOPPED;
               ThreadPoolManager.getInstance().schedule(this, 1000L);
         }
      }
   }

   private final int getDelay(int[] times) {
      for(int i = 0; i < times.length - 1; ++i) {
         int time = times[i];
         if (time < this._countDown) {
            int delay = this._countDown - time;
            this._countDown = time;
            return delay;
         }
      }

      this._countDown = -1;
      return 1;
   }

   private final boolean startGame() {
      try {
         if (this._game.checkDefaulted()) {
            return false;
         } else {
            this._zone.closeDoors();
            if (this._game.needBuffers()) {
               this._zone.spawnBuffers();
            }

            if (!this._game.portPlayersToArena(this._zone.getSpawns())) {
               return false;
            } else {
               this._game.removals();
               this._needAnnounce = true;
               OlympiadGameManager.getInstance().startBattle();
               return true;
            }
         }
      } catch (Exception var2) {
         _log.log(Level.WARNING, var2.getMessage(), (Throwable)var2);
         return false;
      }
   }

   private final void openDoors() {
      try {
         this._game.resetDamage();
         this._zone.openDoors();
      } catch (Exception var2) {
         _log.log(Level.WARNING, var2.getMessage(), (Throwable)var2);
      }
   }

   private final boolean startBattle() {
      try {
         if (this._game.needBuffers()) {
            this._zone.deleteBuffers();
         }

         if (this._game.checkBattleStatus() && this._game.makeCompetitionStart()) {
            this._game.broadcastOlympiadInfo(this._zone);
            this._zone.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.STARTS_THE_GAME));
            this._zone.updateZoneStatusForCharactersInside(false);
            return true;
         }
      } catch (Exception var2) {
         _log.log(Level.WARNING, var2.getMessage(), (Throwable)var2);
      }

      return false;
   }

   private final boolean checkBattle() {
      if (this._game._hasEnded) {
         return true;
      } else {
         try {
            return this._game.haveWinner();
         } catch (Exception var2) {
            _log.log(Level.WARNING, var2.getMessage(), (Throwable)var2);
            return true;
         }
      }
   }

   private final void stopGame() {
      if (!this._game._hasEnded) {
         this._game._hasEnded = true;

         try {
            this._zone.updateZoneStatusForCharactersInside(false);
         } catch (Exception var4) {
            _log.log(Level.WARNING, var4.getMessage(), (Throwable)var4);
         }

         try {
            this._game.cleanEffects();
         } catch (Exception var3) {
            _log.log(Level.WARNING, var3.getMessage(), (Throwable)var3);
         }

         try {
            this._game.validateWinner(this._zone);
         } catch (Exception var2) {
            _log.log(Level.WARNING, var2.getMessage(), (Throwable)var2);
         }
      }
   }

   private final void cleanupGame() {
      try {
         this._game.playersStatusBack();
      } catch (Exception var5) {
         _log.log(Level.WARNING, var5.getMessage(), (Throwable)var5);
      }

      try {
         this._game.portPlayersBack();
      } catch (Exception var4) {
         _log.log(Level.WARNING, var4.getMessage(), (Throwable)var4);
      }

      try {
         this._game.clearPlayers();
      } catch (Exception var3) {
         _log.log(Level.WARNING, var3.getMessage(), (Throwable)var3);
      }

      try {
         this._zone.closeDoors();
      } catch (Exception var2) {
         _log.log(Level.WARNING, var2.getMessage(), (Throwable)var2);
      }
   }

   private static enum GameState {
      BEGIN,
      TELEPORT_TO_ARENA,
      GAME_STARTED,
      BATTLE_COUNTDOWN_FIRST,
      BATTLE_COUNTDOWN_SECOND,
      BATTLE_STARTED,
      BATTLE_IN_PROGRESS,
      GAME_STOPPED,
      TELEPORT_TO_TOWN,
      CLEANUP,
      IDLE;
   }
}
