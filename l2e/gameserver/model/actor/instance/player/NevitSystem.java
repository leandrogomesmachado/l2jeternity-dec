package l2e.gameserver.model.actor.instance.player;

import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.effects.AbnormalEffect;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.ExNavitAdventPointInfo;
import l2e.gameserver.network.serverpackets.ExNevitAdventEffect;
import l2e.gameserver.network.serverpackets.ExNevitAdventTimeChange;

public class NevitSystem {
   public static final int ADVENT_TIME = Config.NEVIT_ADVENT_TIME * 60;
   private static final int MAX_POINTS = Config.NEVIT_MAX_POINTS;
   private static final int BONUS_EFFECT_TIME = Config.NEVIT_BONUS_EFFECT_TIME;
   private final Player _player;
   private int _points = 0;
   private int _time;
   private ScheduledFuture<?> _adventTask;
   private ScheduledFuture<?> _nevitEffectTask;
   private int _percent;
   private boolean _active;

   public NevitSystem(Player player) {
      this._player = player;
   }

   public void setPoints(int points, int time) {
      if (Config.ALLOW_NEVIT_SYSTEM) {
         this._points = points;
         this._active = false;
         this._percent = this.getPercent(this._points);
         Calendar temp = Calendar.getInstance();
         temp.set(11, 6);
         temp.set(12, 30);
         temp.set(13, 0);
         temp.set(14, 0);
         if (this._player.getLastAccess() < temp.getTimeInMillis() && System.currentTimeMillis() > temp.getTimeInMillis()) {
            this._time = ADVENT_TIME;
         } else {
            this._time = time;
         }
      }
   }

   public void restartSystem() {
      if (Config.ALLOW_NEVIT_SYSTEM) {
         this._time = ADVENT_TIME;
         this._player.sendPacket(new ExNevitAdventTimeChange(this._active, this._time));
      }
   }

   public void onEnterWorld() {
      if (Config.ALLOW_NEVIT_SYSTEM) {
         this._player.sendPacket(new ExNavitAdventPointInfo(this._points));
         this._player.sendPacket(new ExNevitAdventTimeChange(this._active, this._time));
         this.startNevitEffect(this._player.getVarInt("nevit", 0));
         if (this._percent >= 45 && this._percent < 50) {
            this._player.sendPacket(SystemMessageId.YOU_ARE_STARTING_TO_FEEL_THE_EFFECTS_OF_NEVITS_ADVENT_BLESSING);
         } else if (this._percent >= 50 && this._percent < 75) {
            this._player.sendPacket(SystemMessageId.YOU_ARE_FURTHER_INFUSED_WITH_THE_BLESSINGS_OF_NEVIT);
         } else if (this._percent >= 75) {
            this._player.sendPacket(SystemMessageId.NEVITS_ADVENT_BLESSING_SHINES_STRONGLY_FROM_ABOVE);
         }
      }
   }

   public void startAdventTask() {
      if (Config.ALLOW_NEVIT_SYSTEM) {
         if (!this._active) {
            this._active = true;
            if (this._time > 0 && this._adventTask == null) {
               this._adventTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new NevitSystem.AdventTask(), 30000L, 30000L);
            }

            this._player.sendPacket(new ExNevitAdventTimeChange(this._active, this._time));
         }
      }
   }

   private void startNevitEffect(int time) {
      if (Config.ALLOW_NEVIT_SYSTEM) {
         if (this.getEffectTime() > 0) {
            this.stopNevitEffectTask(false);
            time += this.getEffectTime();
         }

         if (time > 0) {
            this._player.setVar("nevit", time);
            this._player.sendPacket(new ExNevitAdventEffect(time));
            this._player.sendPacket(SystemMessageId.FROM_NOW_ON_ANGEL_NEVIT_ABIDE_WITH_YOU);
            this._player.startAbnormalEffect(AbnormalEffect.NAVIT_ADVENT);
            this._player.updateVitalityPoints(Config.VITALITY_NEVIT_UP_POINT, true, false);
            this._nevitEffectTask = ThreadPoolManager.getInstance().schedule(new NevitSystem.NevitEffectEnd(), (long)time * 1000L);
         }
      }
   }

   public void stopTasksOnLogout() {
      this.stopNevitEffectTask(true);
      this.stopAdventTask(false);
   }

   public void stopAdventTask(boolean sendPacket) {
      if (Config.ALLOW_NEVIT_SYSTEM) {
         if (this._adventTask != null) {
            if (!this._adventTask.isDone()) {
               this._adventTask.cancel(true);
            }

            this._adventTask = null;
         }

         this._active = false;
         if (sendPacket) {
            this._player.sendPacket(new ExNevitAdventTimeChange(this._active, this._time));
         }
      }
   }

   private void stopNevitEffectTask(boolean saveTime) {
      if (Config.ALLOW_NEVIT_SYSTEM) {
         try {
            if (this._nevitEffectTask != null) {
               if (saveTime) {
                  int time = this.getEffectTime();
                  if (time > 0) {
                     this._player.setVar("nevit", time);
                  } else {
                     this._player.unsetVar("nevit");
                  }
               }

               if (!this._nevitEffectTask.isDone()) {
                  this._nevitEffectTask.cancel(true);
               }

               this._nevitEffectTask = null;
            }
         } catch (Exception var3) {
         }
      }
   }

   public boolean isActive() {
      return this._active;
   }

   public int getTime() {
      return this._time;
   }

   public int getPoints() {
      return this._points;
   }

   public void addPoints(int val) {
      if (Config.ALLOW_NEVIT_SYSTEM) {
         this._points += val;
         if (this._points < 0) {
            this._points = 0;
         }

         int percent = this.getPercent(this._points);
         if (this._percent != percent) {
            this._percent = percent;
            if (this._percent == 45) {
               this._player.sendPacket(SystemMessageId.YOU_ARE_STARTING_TO_FEEL_THE_EFFECTS_OF_NEVITS_ADVENT_BLESSING);
            } else if (this._percent == 50) {
               this._player.sendPacket(SystemMessageId.YOU_ARE_FURTHER_INFUSED_WITH_THE_BLESSINGS_OF_NEVIT);
            } else if (this._percent == 75) {
               this._player.sendPacket(SystemMessageId.NEVITS_ADVENT_BLESSING_SHINES_STRONGLY_FROM_ABOVE);
            }
         }

         if (this._points > MAX_POINTS) {
            this._percent = 0;
            this._points = 0;
            if (!this.isBlessingActive()) {
               this.startNevitEffect(BONUS_EFFECT_TIME);
            }
         }

         this._player.sendPacket(new ExNavitAdventPointInfo(this._points));
      }
   }

   public int getPercent(int points) {
      return (int)(100.0 / (double)MAX_POINTS * (double)points);
   }

   public void setTime(int time) {
      this._time = time;
   }

   public boolean isBlessingActive() {
      return this.getEffectTime() > 0;
   }

   private int getEffectTime() {
      return this._nevitEffectTask == null ? 0 : (int)Math.max(0L, this._nevitEffectTask.getDelay(TimeUnit.SECONDS));
   }

   private class AdventTask implements Runnable {
      private AdventTask() {
      }

      @Override
      public void run() {
         NevitSystem.this._time = NevitSystem.this._time - 30;
         if (NevitSystem.this._time <= 0) {
            NevitSystem.this._time = 0;
            NevitSystem.this.stopAdventTask(true);
         } else {
            NevitSystem.this.addPoints(72);
            if (NevitSystem.this._time % 60 == 0) {
               NevitSystem.this._player.sendPacket(new ExNevitAdventTimeChange(true, NevitSystem.this._time));
            }
         }
      }
   }

   private class NevitEffectEnd implements Runnable {
      private NevitEffectEnd() {
      }

      @Override
      public void run() {
         NevitSystem.this._player.sendPacket(new ExNevitAdventEffect(0));
         NevitSystem.this._player.sendPacket(new ExNavitAdventPointInfo(NevitSystem.this._points));
         NevitSystem.this._player.sendPacket(SystemMessageId.NEVITS_ADVENT_BLESSING_HAS_ENDED);
         NevitSystem.this._player.stopAbnormalEffect(AbnormalEffect.NAVIT_ADVENT);
         NevitSystem.this._player.unsetVar("nevit");
         NevitSystem.this.stopNevitEffectTask(false);
      }
   }
}
