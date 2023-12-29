package l2e.gameserver.model.actor.stat;

import java.util.logging.Logger;
import l2e.gameserver.Config;
import l2e.gameserver.data.parser.ExperienceParser;
import l2e.gameserver.data.parser.PetsParser;
import l2e.gameserver.instancemanager.ZoneManager;
import l2e.gameserver.model.actor.Playable;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.PetInstance;
import l2e.gameserver.model.zone.ZoneId;
import l2e.gameserver.model.zone.type.SwampZone;

public class PlayableStat extends CharStat {
   protected static final Logger _log = Logger.getLogger(PlayableStat.class.getName());

   public PlayableStat(Playable activeChar) {
      super(activeChar);
   }

   public boolean addExp(long value) {
      if (this.getExp() + value >= 0L && (value <= 0L || this.getExp() != this.getExpForLevel(this.getMaxLevel()) - 1L)) {
         if (this.getExp() + value >= this.getExpForLevel(this.getMaxLevel())) {
            value = this.getExpForLevel(this.getMaxLevel()) - 1L - this.getExp();
         }

         if (!this.getActiveChar().getEvents().onExperienceReceived(value)) {
            return false;
         } else {
            this.setExp(this.getExp() + value);
            byte minimumLevel = 1;
            if (this.getActiveChar() instanceof PetInstance) {
               minimumLevel = (byte)PetsParser.getInstance().getPetMinLevel(((PetInstance)this.getActiveChar()).getTemplate().getId());
            }

            byte level = minimumLevel;

            for(byte tmp = minimumLevel; tmp <= this.getMaxLevel(); ++tmp) {
               if (this.getExp() < this.getExpForLevel(tmp)) {
                  level = --tmp;
                  break;
               }
            }

            if (level != this.getLevel() && level >= minimumLevel) {
               this.addLevel((byte)(level - this.getLevel()), false);
            }

            return true;
         }
      } else {
         return true;
      }
   }

   public boolean removeExp(long value) {
      if (this.getExp() - value < 0L) {
         value = this.getExp() - 1L;
      }

      this.setExp(this.getExp() - value);
      byte minimumLevel = 1;
      if (this.getActiveChar() instanceof PetInstance) {
         minimumLevel = (byte)PetsParser.getInstance().getPetMinLevel(((PetInstance)this.getActiveChar()).getTemplate().getId());
      }

      byte level = minimumLevel;

      for(byte tmp = minimumLevel; tmp <= this.getMaxLevel(); ++tmp) {
         if (this.getExp() < this.getExpForLevel(tmp)) {
            level = --tmp;
            break;
         }
      }

      if (level != this.getLevel() && level >= minimumLevel) {
         this.addLevel((byte)(level - this.getLevel()), true);
      }

      return true;
   }

   public boolean addExpAndSp(long addToExp, int addToSp) {
      boolean expAdded = false;
      boolean spAdded = false;
      if (addToExp >= 0L) {
         expAdded = this.addExp(addToExp);
      }

      if (addToSp >= 0) {
         spAdded = this.addSp(addToSp);
      }

      return expAdded || spAdded;
   }

   public boolean removeExpAndSp(long removeExp, int removeSp) {
      boolean expRemoved = false;
      boolean spRemoved = false;
      if (removeExp > 0L) {
         expRemoved = this.removeExp(removeExp);
      }

      if (removeSp > 0) {
         spRemoved = this.removeSp(removeSp);
      }

      return expRemoved || spRemoved;
   }

   public boolean addLevel(byte value, boolean canLower) {
      if (this.getLevel() + value > this.getMaxLevel() - 1) {
         if (this.getLevel() >= this.getMaxLevel() - 1) {
            return false;
         }

         value = (byte)(this.getMaxLevel() - 1 - this.getLevel());
      }

      if (!canLower && value < 0) {
         return false;
      } else {
         boolean levelIncreased = this.getLevel() + value > this.getLevel();
         value = (byte)(value + this.getLevel());
         this.setLevel(value);
         if (this.getExp() >= this.getExpForLevel(this.getLevel() + 1) || this.getExpForLevel(this.getLevel()) > this.getExp()) {
            this.setExp(this.getExpForLevel(this.getLevel()));
         }

         if (!levelIncreased && this.getActiveChar().isPlayer() && !((Player)this.getActiveChar()).isGM() && Config.DECREASE_SKILL_LEVEL) {
            ((Player)this.getActiveChar()).checkPlayerSkills();
         }

         if (!levelIncreased) {
            return false;
         } else {
            this.getActiveChar().getStatus().setCurrentHp(this.getActiveChar().getStat().getMaxHp());
            this.getActiveChar().getStatus().setCurrentMp(this.getActiveChar().getStat().getMaxMp());
            return true;
         }
      }
   }

   public boolean addSp(int value) {
      if (value < 0) {
         _log.warning("wrong usage");
         return false;
      } else {
         int currentSp = this.getSp();
         if (currentSp == Integer.MAX_VALUE) {
            return false;
         } else {
            if (currentSp > Integer.MAX_VALUE - value) {
               value = Integer.MAX_VALUE - currentSp;
            }

            this.setSp(currentSp + value);
            return true;
         }
      }
   }

   public boolean removeSp(int value) {
      int currentSp = this.getSp();
      if (currentSp < value) {
         value = currentSp;
      }

      this.setSp(this.getSp() - value);
      return true;
   }

   public long getExpForLevel(int level) {
      return (long)level;
   }

   @Override
   public double getRunSpeed() {
      if (this.getActiveChar().isInsideZone(ZoneId.SWAMP)) {
         SwampZone zone = ZoneManager.getInstance().getZone(this.getActiveChar(), SwampZone.class);
         if (zone != null) {
            return super.getRunSpeed() * zone.getMoveBonus(this.getActiveChar());
         }
      }

      return super.getRunSpeed();
   }

   @Override
   public double getWalkSpeed() {
      if (this.getActiveChar().isInsideZone(ZoneId.SWAMP)) {
         SwampZone zone = ZoneManager.getInstance().getZone(this.getActiveChar(), SwampZone.class);
         if (zone != null) {
            return super.getWalkSpeed() * zone.getMoveBonus(this.getActiveChar());
         }
      }

      return super.getWalkSpeed();
   }

   public Playable getActiveChar() {
      return (Playable)super.getActiveChar();
   }

   public int getMaxLevel() {
      return ExperienceParser.getInstance().getMaxLevel();
   }
}
