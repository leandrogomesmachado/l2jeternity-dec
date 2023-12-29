package l2e.gameserver.model.zone.type;

import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.model.zone.AbstractZoneSettings;
import l2e.gameserver.model.zone.TaskZoneSettings;
import l2e.gameserver.model.zone.ZoneType;

public class DamageZone extends ZoneType {
   private int _damageHPPerSec = 200;
   private int _damageMPPerSec = 0;
   private int _castleId;
   private Castle _castle;
   private int _startTask = 10;
   private int _reuseTask = 5000;
   protected boolean _enabled;

   public DamageZone(int id) {
      super(id);
      this._castleId = 0;
      this._castle = null;
      this._enabled = true;
      this.setTargetType(GameObject.InstanceType.Playable);
      AbstractZoneSettings settings = ZoneManager.getSettings(this.getName());
      if (settings == null) {
         settings = new TaskZoneSettings();
      }

      this.setSettings(settings);
   }

   public TaskZoneSettings getSettings() {
      return (TaskZoneSettings)super.getSettings();
   }

   @Override
   public void setParameter(String name, String value) {
      if (name.equals("dmgHPSec")) {
         this._damageHPPerSec = Integer.parseInt(value);
      } else if (name.equals("dmgMPSec")) {
         this._damageMPPerSec = Integer.parseInt(value);
      } else if (name.equals("castleId")) {
         this._castleId = Integer.parseInt(value);
      } else if (name.equalsIgnoreCase("initialDelay")) {
         this._startTask = Integer.parseInt(value);
      } else if (name.equalsIgnoreCase("reuse")) {
         this._reuseTask = Integer.parseInt(value);
      } else if (name.equalsIgnoreCase("default_enabled")) {
         this._enabled = Boolean.parseBoolean(value);
      } else {
         super.setParameter(name, value);
      }
   }

   @Override
   protected void onEnter(Creature character) {
      if (this.getSettings().getTask() == null && (this._damageHPPerSec != 0 || this._damageMPPerSec != 0)) {
         Player player = character.getActingPlayer();
         if (this.getCastle() != null && (!this.getCastle().getSiege().getIsInProgress() || player == null || player.getSiegeState() == 2)) {
            return;
         }

         synchronized(this) {
            if (this.getSettings().getTask() == null) {
               this.getSettings()
                  .setTask(ThreadPoolManager.getInstance().scheduleAtFixedRate(new DamageZone.ApplyDamage(this), (long)this._startTask, (long)this._reuseTask));
            }
         }
      }
   }

   @Override
   protected void onExit(Creature character) {
      if (this._characterList.isEmpty() && this.getSettings().getTask() != null) {
         this.stopTask();
      }
   }

   protected int getHPDamagePerSecond() {
      return this._damageHPPerSec;
   }

   protected int getMPDamagePerSecond() {
      return this._damageMPPerSec;
   }

   protected void stopTask() {
      if (this.getSettings().getTask() != null) {
         this.getSettings().getTask().cancel(false);
      }
   }

   protected Castle getCastle() {
      if (this._castleId > 0 && this._castle == null) {
         this._castle = CastleManager.getInstance().getCastleById(this._castleId);
      }

      return this._castle;
   }

   public void setEnabled(boolean state) {
      this._enabled = state;
   }

   private final class ApplyDamage implements Runnable {
      private final DamageZone _dmgZone;
      private final Castle _castle;

      protected ApplyDamage(DamageZone zone) {
         this._dmgZone = zone;
         this._castle = zone.getCastle();
      }

      @Override
      public void run() {
         if (DamageZone.this._enabled) {
            boolean siege = false;
            if (this._castle != null) {
               siege = this._castle.getSiege().getIsInProgress();
               if (!siege) {
                  this._dmgZone.stopTask();
                  return;
               }
            }

            for(Creature temp : this._dmgZone.getCharactersInside()) {
               if (temp != null && !temp.isDead()) {
                  if (siege) {
                     Player player = temp.getActingPlayer();
                     if (player != null && player.isInSiege() && player.getSiegeState() == 2) {
                        continue;
                     }
                  }

                  double multiplier = 1.0 + temp.calcStat(Stats.DAMAGE_ZONE_VULN, 0.0, null, null) / 100.0;
                  if (DamageZone.this.getHPDamagePerSecond() != 0) {
                     temp.reduceCurrentHp((double)this._dmgZone.getHPDamagePerSecond() * multiplier, null, null);
                  }

                  if (DamageZone.this.getMPDamagePerSecond() != 0) {
                     temp.reduceCurrentMp((double)this._dmgZone.getMPDamagePerSecond() * multiplier);
                  }
               }
            }
         }
      }
   }
}
