package l2e.gameserver.model.actor.stat;

import l2e.gameserver.data.parser.ExperienceParser;
import l2e.gameserver.data.parser.PetsParser;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.instance.PetInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.stats.Stats;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.SocialAction;
import l2e.gameserver.network.serverpackets.StatusUpdate;
import l2e.gameserver.network.serverpackets.SystemMessage;

public class PetStat extends SummonStat {
   private double _oldMaxHp;
   private double _oldMaxMp;

   public PetStat(PetInstance activeChar) {
      super(activeChar);
   }

   public boolean addExp(int value) {
      if (!this.getActiveChar().isUncontrollable() && super.addExp((long)value)) {
         this.getActiveChar().updateAndBroadcastStatus(1);
         this.getActiveChar().updateEffectIcons(true);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean addExpAndSp(long addToExp, int addToSp) {
      if (!this.getActiveChar().isUncontrollable() && this.addExp(addToExp)) {
         SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PET_EARNED_S1_EXP);
         sm.addLong(addToExp);
         this.getActiveChar().updateAndBroadcastStatus(1);
         this.getActiveChar().sendPacket(sm);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public final boolean addLevel(byte value, boolean canLower) {
      if (this.getLevel() + value <= this.getMaxLevel() - 1 && (canLower || value >= 0)) {
         boolean levelIncreased = super.addLevel(value, canLower);
         StatusUpdate su = new StatusUpdate(this.getActiveChar());
         su.addAttribute(1, this.getLevel());
         su.addAttribute(10, this.getMaxHp());
         su.addAttribute(12, this.getMaxMp());
         this.getActiveChar().broadcastPacket(su);
         if (levelIncreased) {
            this.getActiveChar().broadcastPacket(new SocialAction(this.getActiveChar().getObjectId(), 2122));
         }

         this.getActiveChar().updateAndBroadcastStatus(1);
         if (this.getActiveChar().getControlItem() != null) {
            this.getActiveChar().getControlItem().setEnchantLevel(this.getLevel());
         }

         return levelIncreased;
      } else {
         return false;
      }
   }

   @Override
   public final long getExpForLevel(int level) {
      try {
         return PetsParser.getInstance().getPetLevelData(this.getActiveChar().getId(), level).getPetMaxExp();
      } catch (NullPointerException var3) {
         if (this.getActiveChar() != null) {
            _log.warning(
               "Pet objectId:"
                  + this.getActiveChar().getObjectId()
                  + ", NpcId:"
                  + this.getActiveChar().getId()
                  + ", level:"
                  + level
                  + " is missing data from pets_stats table!"
            );
         }

         throw var3;
      }
   }

   public PetInstance getActiveChar() {
      return (PetInstance)super.getActiveChar();
   }

   public final int getFeedBattle() {
      return this.getActiveChar().getPetLevelData().getPetFeedBattle();
   }

   public final int getFeedNormal() {
      return this.getActiveChar().getPetLevelData().getPetFeedNormal();
   }

   @Override
   public void setLevel(byte value) {
      this.getActiveChar().setPetData(PetsParser.getInstance().getPetLevelData(this.getActiveChar().getTemplate().getId(), value));
      if (this.getActiveChar().getPetLevelData() == null) {
         throw new IllegalArgumentException("No pet data for npc: " + this.getActiveChar().getTemplate().getId() + " level: " + value);
      } else {
         this.getActiveChar().stopFeed();
         super.setLevel(value);
         this.getActiveChar().startFeed();
         if (this.getActiveChar().getControlItem() != null) {
            this.getActiveChar().getControlItem().setEnchantLevel(this.getLevel());
         }
      }
   }

   public final int getMaxFeed() {
      return this.getActiveChar().getPetLevelData().getPetMaxFeed();
   }

   @Override
   public double getMaxHp() {
      double val = this.calcStat(Stats.MAX_HP, (double)this.getActiveChar().getPetLevelData().getPetMaxHP(), null, null);
      if (val != this._oldMaxHp) {
         this._oldMaxHp = val;
         if (this.getActiveChar().getStatus().getCurrentHp() != val) {
            this.getActiveChar().getStatus().setCurrentHp(this.getActiveChar().getStatus().getCurrentHp());
         }
      }

      return val;
   }

   @Override
   public double getMaxMp() {
      double val = this.calcStat(Stats.MAX_MP, (double)this.getActiveChar().getPetLevelData().getPetMaxMP(), null, null);
      if (val != this._oldMaxMp) {
         this._oldMaxMp = val;
         if (this.getActiveChar().getStatus().getCurrentMp() != val) {
            this.getActiveChar().getStatus().setCurrentMp(this.getActiveChar().getStatus().getCurrentMp());
         }
      }

      return val;
   }

   @Override
   public double getMAtk(Creature target, Skill skill) {
      return this.calcStat(Stats.MAGIC_ATTACK, (double)this.getActiveChar().getPetLevelData().getPetMAtk(), target, skill);
   }

   @Override
   public double getMDef(Creature target, Skill skill) {
      return this.calcStat(Stats.MAGIC_DEFENCE, (double)this.getActiveChar().getPetLevelData().getPetMDef(), target, skill);
   }

   @Override
   public double getPAtk(Creature target) {
      return this.calcStat(Stats.POWER_ATTACK, (double)this.getActiveChar().getPetLevelData().getPetPAtk(), target, null);
   }

   @Override
   public double getPDef(Creature target) {
      return this.calcStat(Stats.POWER_DEFENCE, (double)this.getActiveChar().getPetLevelData().getPetPDef(), target, null);
   }

   @Override
   public double getPAtkSpd() {
      double val = super.getPAtkSpd();
      if (this.getActiveChar().isHungry()) {
         val /= 2.0;
      }

      return val;
   }

   @Override
   public double getMAtkSpd() {
      double val = super.getMAtkSpd();
      if (this.getActiveChar().isHungry()) {
         val /= 2.0;
      }

      return val;
   }

   @Override
   public int getMaxLevel() {
      return ExperienceParser.getInstance().getMaxPetLevel();
   }
}
