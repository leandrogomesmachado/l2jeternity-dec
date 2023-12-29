package l2e.scripts.events;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import l2e.commons.util.TimeUtils;
import l2e.gameserver.Announcements;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.Npc;
import l2e.gameserver.model.entity.events.AbstractWorldEvent;
import l2e.gameserver.model.entity.events.custom.achievements.AchievementManager;
import l2e.gameserver.model.entity.events.model.template.WorldEventSpawn;
import l2e.gameserver.model.entity.events.model.template.WorldEventTemplate;
import l2e.gameserver.model.strings.server.ServerMessage;

public class Achievements extends AbstractWorldEvent {
   private boolean _isActive = false;
   private WorldEventTemplate _template = null;
   private ScheduledFuture<?> _startTask = null;
   private ScheduledFuture<?> _stopTask = null;
   private final List<Npc> _npcList = new ArrayList<>();

   public Achievements(String name, String descr) {
      super(name, descr);
      this._template = this.parseSettings(this.getName());
      if (this._template != null && !this._isActive) {
         long expireTime = this.restoreStatus(this.getName());
         if (expireTime <= System.currentTimeMillis() && expireTime != -1L) {
            this.checkTimerTask(this.calcEventTime(this._template), true);
         } else {
            this.eventStart(expireTime == -1L ? -1L : expireTime - System.currentTimeMillis());
         }
      }
   }

   @Override
   public boolean isEventActive() {
      return this._isActive;
   }

   @Override
   public WorldEventTemplate getEventTemplate() {
      return this._template;
   }

   @Override
   public boolean eventStart(long totalTime) {
      if (!this._isActive && totalTime != 0L) {
         if (this._startTask != null) {
            this._startTask.cancel(false);
            this._startTask = null;
         }

         this._isActive = true;
         List<WorldEventSpawn> spawnList = this._template.getSpawnList();
         if (spawnList != null && !spawnList.isEmpty()) {
            for(WorldEventSpawn spawn : spawnList) {
               this._npcList
                  .add(
                     addSpawn(
                        spawn.getNpcId(),
                        spawn.getLocation().getX(),
                        spawn.getLocation().getY(),
                        spawn.getLocation().getZ(),
                        spawn.getLocation().getHeading(),
                        false,
                        0L
                     )
                  );
            }
         }

         ServerMessage msg = new ServerMessage("EventAchievements.START", true);
         Announcements.getInstance().announceToAll(msg);
         this.updateStatus(this.getName(), totalTime == -1L ? -1L : totalTime + System.currentTimeMillis(), true);
         AchievementManager.getInstance().setIsActive(true);
         if (totalTime > 0L) {
            this._stopTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
               @Override
               public void run() {
                  Achievements.this.eventStop();
               }
            }, totalTime);
            _log.info("Event " + this._template.getName() + " will end in: " + TimeUtils.toSimpleFormat(System.currentTimeMillis() + totalTime));
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean eventStop() {
      if (!this._isActive) {
         return false;
      } else {
         if (this._stopTask != null) {
            this._stopTask.cancel(false);
            this._stopTask = null;
         }

         this._isActive = false;
         if (!this._npcList.isEmpty()) {
            for(Npc _npc : this._npcList) {
               if (_npc != null) {
                  _npc.deleteMe();
               }
            }
         }

         this._npcList.clear();
         ServerMessage msg = new ServerMessage("EventAchievements.STOP", true);
         Announcements.getInstance().announceToAll(msg);
         AchievementManager.getInstance().setIsActive(false);
         this.updateStatus(this.getName(), 0L, false);
         this.checkTimerTask(this.calcEventTime(this._template), false);
         return true;
      }
   }

   @Override
   public void startTimerTask(long time, final boolean checkZero) {
      if (this._startTask != null) {
         this._startTask.cancel(false);
         this._startTask = null;
      }

      this._startTask = ThreadPoolManager.getInstance().schedule(new Runnable() {
         @Override
         public void run() {
            Achievements.this.eventStart(checkZero ? -1L : (long)(Achievements.this._template.getPeriod() * 3600000));
         }
      }, time - System.currentTimeMillis());
      _log.info("Event " + this._template.getName() + " will start in: " + TimeUtils.toSimpleFormat(time));
   }

   @Override
   public boolean isReloaded() {
      if (this.isEventActive()) {
         return false;
      } else {
         this._template = this.parseSettings(this.getName());
         if (this._template == null) {
            return false;
         } else {
            long expireTime = this.restoreStatus(this.getName());
            if (expireTime <= System.currentTimeMillis() && expireTime != -1L) {
               this.checkTimerTask(this.calcEventTime(this._template), true);
            } else {
               this.eventStart(expireTime == -1L ? -1L : expireTime - System.currentTimeMillis());
            }

            return true;
         }
      }
   }

   public static void main(String[] args) {
      new Achievements(Achievements.class.getSimpleName(), "events");
   }
}
