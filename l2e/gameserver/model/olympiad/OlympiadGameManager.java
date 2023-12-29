package l2e.gameserver.model.olympiad;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.zone.type.OlympiadStadiumZone;

public class OlympiadGameManager implements Runnable {
   private static final Logger _log = Logger.getLogger(OlympiadGameManager.class.getName());
   private volatile boolean _battleStarted = false;
   private final List<OlympiadGameTask> _tasks;

   protected OlympiadGameManager() {
      Collection<OlympiadStadiumZone> zones = ZoneManager.getInstance().getAllZones(OlympiadStadiumZone.class);
      if (zones != null && !zones.isEmpty()) {
         this._tasks = new ArrayList<>(zones.size());
         int i = 0;

         for(OlympiadStadiumZone zone : zones) {
            this._tasks.add(new OlympiadGameTask(i, zone));
            ++i;
         }

         _log.log(Level.INFO, "Olympiad System: Loaded " + this._tasks.size() + " stadiums.");
      } else {
         throw new Error("No olympiad stadium zones defined !");
      }
   }

   public static final OlympiadGameManager getInstance() {
      return OlympiadGameManager.SingletonHolder._instance;
   }

   protected final boolean isBattleStarted() {
      return this._battleStarted;
   }

   protected final void startBattle() {
      this._battleStarted = true;
   }

   @Override
   public final void run() {
      if (!Olympiad.getInstance().isOlympiadEnd()) {
         if (Olympiad.getInstance().inCompPeriod()) {
            List<List<Integer>> readyClassed = OlympiadManager.getInstance().hasEnoughRegisteredClassed();
            boolean readyNonClassed = OlympiadManager.getInstance().hasEnoughRegisteredNonClassed();
            boolean readyTeams = OlympiadManager.getInstance().hasEnoughRegisteredTeams();
            if (readyClassed != null || readyNonClassed || readyTeams) {
               Collections.shuffle(this._tasks);

               for(int i = 0; i < this._tasks.size(); ++i) {
                  OlympiadGameTask task = this._tasks.get(i);
                  synchronized(task) {
                     if (!task.isRunning()) {
                        if ((readyClassed != null || readyTeams) && task.getId() % 2 == 0) {
                           if (readyTeams && task.getId() % 4 == 0) {
                              AbstractOlympiadGame newGame = OlympiadGameTeams.createGame(
                                 task.getId(), OlympiadManager.getInstance().getRegisteredTeamsBased()
                              );
                              if (newGame != null) {
                                 task.attachGame(newGame);
                                 continue;
                              }

                              readyTeams = false;
                           }

                           if (readyClassed != null) {
                              AbstractOlympiadGame newGame = OlympiadGameClassed.createGame(task.getId(), readyClassed);
                              if (newGame != null) {
                                 task.attachGame(newGame);
                                 continue;
                              }

                              readyClassed = null;
                           }
                        }

                        if (readyNonClassed) {
                           AbstractOlympiadGame newGame = OlympiadGameNonClassed.createGame(
                              task.getId(), OlympiadManager.getInstance().getRegisteredNonClassBased()
                           );
                           if (newGame != null) {
                              task.attachGame(newGame);
                              continue;
                           }

                           readyNonClassed = false;
                        }
                     }
                  }

                  if (readyClassed == null && !readyNonClassed && !readyTeams) {
                     break;
                  }
               }
            }
         } else if (this.isAllTasksFinished() && this._battleStarted) {
            OlympiadManager.getInstance().clearRegistered();
            this._battleStarted = false;
            _log.log(Level.INFO, "Olympiad System: All current games finished.");
         }
      }
   }

   public final boolean isAllTasksFinished() {
      for(OlympiadGameTask task : this._tasks) {
         if (task.isRunning()) {
            return false;
         }
      }

      return true;
   }

   public final OlympiadGameTask getOlympiadTask(int id) {
      for(OlympiadGameTask task : this._tasks) {
         if (task.getId() == id) {
            return task;
         }
      }

      return null;
   }

   public final int getNumberOfStadiums() {
      return this._tasks.size();
   }

   public final void notifyCompetitorDamage(Player player, int damage) {
      if (player != null) {
         int id = player.getOlympiadGameId();

         for(OlympiadGameTask task : this._tasks) {
            if (task.getId() == id) {
               AbstractOlympiadGame game = task.getGame();
               if (game != null) {
                  game.addDamage(player, damage);
               }
            }
         }
      }
   }

   private static class SingletonHolder {
      protected static final OlympiadGameManager _instance = new OlympiadGameManager();
   }
}
