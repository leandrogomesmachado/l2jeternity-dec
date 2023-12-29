package l2e.gameserver.model.base;

import l2e.gameserver.Config;
import l2e.gameserver.data.parser.ExperienceParser;

public final class SubClass {
   private static final byte _maxLevel = Config.MAX_SUBCLASS_LEVEL < ExperienceParser.getInstance().getMaxLevel()
      ? Config.MAX_SUBCLASS_LEVEL
      : (byte)(ExperienceParser.getInstance().getMaxLevel() - 1);
   private PlayerClass _class;
   private long _exp = ExperienceParser.getInstance().getExpForLevel(Config.BASE_SUBCLASS_LEVEL);
   private int _sp = 0;
   private byte _level = Config.BASE_SUBCLASS_LEVEL;
   private int _classIndex = 1;

   public SubClass(int classId, long exp, int sp, byte level, int classIndex) {
      this._class = PlayerClass.values()[classId];
      this._exp = exp;
      this._sp = sp;
      this._level = level;
      this._classIndex = classIndex;
   }

   public SubClass(int classId, int classIndex) {
      this._class = PlayerClass.values()[classId];
      this._classIndex = classIndex;
   }

   public SubClass() {
   }

   public PlayerClass getClassDefinition() {
      return this._class;
   }

   public int getClassId() {
      return this._class.ordinal();
   }

   public long getExp() {
      return this._exp;
   }

   public int getSp() {
      return this._sp;
   }

   public byte getLevel() {
      return this._level;
   }

   public int getClassIndex() {
      return this._classIndex;
   }

   public void setClassId(int classId) {
      this._class = PlayerClass.values()[classId];
   }

   public void setExp(long expValue) {
      if (expValue > ExperienceParser.getInstance().getExpForLevel(_maxLevel + 1) - 1L) {
         expValue = ExperienceParser.getInstance().getExpForLevel(_maxLevel + 1) - 1L;
      }

      this._exp = expValue;
   }

   public void setSp(int spValue) {
      this._sp = spValue;
   }

   public void setClassIndex(int classIndex) {
      this._classIndex = classIndex;
   }

   public void setLevel(byte levelValue) {
      if (levelValue > _maxLevel) {
         levelValue = _maxLevel;
      } else if (levelValue < Config.BASE_SUBCLASS_LEVEL) {
         levelValue = Config.BASE_SUBCLASS_LEVEL;
      }

      this._level = levelValue;
   }

   public void incLevel() {
      if (this.getLevel() != _maxLevel) {
         ++this._level;
         this.setExp(ExperienceParser.getInstance().getExpForLevel(this.getLevel()));
      }
   }

   public void decLevel() {
      if (this.getLevel() != Config.BASE_SUBCLASS_LEVEL) {
         --this._level;
         this.setExp(ExperienceParser.getInstance().getExpForLevel(this.getLevel()));
      }
   }
}
