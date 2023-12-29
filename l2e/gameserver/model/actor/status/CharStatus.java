package l2e.gameserver.model.actor.status;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.ThreadPoolManager;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.stat.CharStat;
import l2e.gameserver.model.stats.Formulas;

public class CharStatus {
   protected static final Logger _log = Logger.getLogger(CharStatus.class.getName());
   private final Creature _activeChar;
   private double _currentHp = 0.0;
   private double _currentMp = 0.0;
   private List<Creature> _statusListener;
   private Future<?> _regTask;
   protected byte _flagsRegenActive = 0;
   protected static final byte REGEN_FLAG_CP = 4;
   private static final byte REGEN_FLAG_HP = 1;
   private static final byte REGEN_FLAG_MP = 2;

   public CharStatus(Creature activeChar) {
      this._activeChar = activeChar;
   }

   public final void addStatusListener(Creature object) {
      if (object != this.getActiveChar()) {
         this.getStatusListener().add(object);
      }
   }

   public final void removeStatusListener(Creature object) {
      this.getStatusListener().remove(object);
   }

   public final List<Creature> getStatusListener() {
      if (this._statusListener == null) {
         this._statusListener = new CopyOnWriteArrayList<>();
      }

      return this._statusListener;
   }

   public void reduceCp(int value) {
   }

   public void reduceHp(double value, Creature attacker) {
      this.reduceHp(value, attacker, true, false, false);
   }

   public void reduceHp(double value, Creature attacker, boolean isHpConsumption) {
      this.reduceHp(value, attacker, true, false, isHpConsumption);
   }

   public void reduceHp(double value, Creature attacker, boolean awake, boolean isDOT, boolean isHPConsumption) {
      if (!this.getActiveChar().isDead()) {
         if (!this.getActiveChar().isInvul() || isDOT || isHPConsumption) {
            if (attacker != null) {
               Player attackerPlayer = attacker.getActingPlayer();
               if (attackerPlayer != null && attackerPlayer.isGM() && !attackerPlayer.getAccessLevel().canGiveDamage()) {
                  return;
               }
            }

            if (!isDOT && !isHPConsumption) {
               this.getActiveChar().stopEffectsOnDamage(awake);
            }

            if (value > 0.0) {
               this.setCurrentHp(Math.max(this.getCurrentHp() - value, 0.0));
            }

            if (this.getActiveChar().getCurrentHp() < 0.5 && this.getActiveChar().isMortal()) {
               this.getActiveChar().abortAttack();
               this.getActiveChar().abortCast();
               if (Config.DEBUG) {
                  _log.fine("char is dead.");
               }

               this.getActiveChar().stopHpMpRegeneration();
               this.getActiveChar().doDie(attacker);
            }
         }
      }
   }

   public void reduceMp(double value) {
      this.setCurrentMp(Math.max(this.getCurrentMp() - value, 0.0));
   }

   public final synchronized void startHpMpRegeneration() {
      if (this._regTask == null && !this.getActiveChar().isDead()) {
         if (Config.DEBUG) {
            _log.fine("HP/MP regen started");
         }

         int period = Formulas.getRegeneratePeriod(this.getActiveChar());
         this._regTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new CharStatus.RegenTask(), (long)period, (long)period);
      }
   }

   public final synchronized void stopHpMpRegeneration() {
      if (this._regTask != null) {
         if (Config.DEBUG) {
            _log.fine("HP/MP regen stop");
         }

         this._regTask.cancel(false);
         this._regTask = null;
         this._flagsRegenActive = 0;
      }
   }

   public double getCurrentCp() {
      return 0.0;
   }

   public void setCurrentCp(double newCp) {
   }

   public final double getCurrentHp() {
      return this._currentHp;
   }

   public final void setCurrentHp(double newHp) {
      this.setCurrentHp(newHp, true);
   }

   public boolean setCurrentHp(double newHp, boolean broadcastPacket) {
      int currentHp = (int)this.getCurrentHp();
      double maxHp = this.getActiveChar().getStat().getMaxHp();
      synchronized(this) {
         if (this.getActiveChar().isDead()) {
            return false;
         }

         if (newHp >= maxHp) {
            this._currentHp = maxHp;
            this._flagsRegenActive &= -2;
            if (this._flagsRegenActive == 0) {
               this.stopHpMpRegeneration();
            }
         } else {
            this._currentHp = newHp;
            this._flagsRegenActive = (byte)(this._flagsRegenActive | 1);
            this.startHpMpRegeneration();
         }
      }

      boolean hpWasChanged = (double)currentHp != this._currentHp;
      if (hpWasChanged && broadcastPacket) {
         this.getActiveChar().broadcastStatusUpdate();
      }

      return hpWasChanged;
   }

   public final void setCurrentHpMp(double newHp, double newMp) {
      boolean hpOrMpWasChanged = this.setCurrentHp(newHp, false);
      hpOrMpWasChanged |= this.setCurrentMp(newMp, false);
      if (hpOrMpWasChanged) {
         this.getActiveChar().broadcastStatusUpdate();
      }
   }

   public final double getCurrentMp() {
      return this._currentMp;
   }

   public final void setCurrentMp(double newMp) {
      this.setCurrentMp(newMp, true);
   }

   public final boolean setCurrentMp(double newMp, boolean broadcastPacket) {
      int currentMp = (int)this.getCurrentMp();
      double maxMp = this.getActiveChar().getStat().getMaxMp();
      synchronized(this) {
         if (this.getActiveChar().isDead()) {
            return false;
         }

         if (newMp >= maxMp) {
            this._currentMp = maxMp;
            this._flagsRegenActive &= -3;
            if (this._flagsRegenActive == 0) {
               this.stopHpMpRegeneration();
            }
         } else {
            this._currentMp = newMp;
            this._flagsRegenActive = (byte)(this._flagsRegenActive | 2);
            this.startHpMpRegeneration();
         }
      }

      boolean mpWasChanged = (double)currentMp != this._currentMp;
      if (mpWasChanged && broadcastPacket) {
         this.getActiveChar().broadcastStatusUpdate();
      }

      return mpWasChanged;
   }

   protected void doRegeneration() {
      CharStat charstat = this.getActiveChar().getStat();
      if (this.getCurrentHp() < (double)charstat.getMaxRecoverableHp()) {
         this.setCurrentHp(this.getCurrentHp() + Formulas.calcHpRegen(this.getActiveChar()), false);
      }

      if (this.getCurrentMp() < (double)charstat.getMaxRecoverableMp()) {
         this.setCurrentMp(this.getCurrentMp() + Formulas.calcMpRegen(this.getActiveChar()), false);
      }

      if (!this.getActiveChar().isInActiveRegion()) {
         if (this.getCurrentHp() == (double)charstat.getMaxRecoverableHp() && this.getCurrentMp() == charstat.getMaxMp()) {
            this.stopHpMpRegeneration();
         }
      } else {
         this.getActiveChar().broadcastStatusUpdate();
      }
   }

   public Creature getActiveChar() {
      return this._activeChar;
   }

   class RegenTask implements Runnable {
      @Override
      public void run() {
         try {
            CharStatus.this.doRegeneration();
         } catch (Exception var2) {
            CharStatus._log.log(Level.SEVERE, "", (Throwable)var2);
         }
      }
   }
}
