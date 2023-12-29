package l2e.gameserver.model.stats;

import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.CubicInstance;
import l2e.gameserver.model.items.instance.ItemInstance;
import l2e.gameserver.model.skills.Skill;
import l2e.gameserver.model.skills.effects.Effect;

public final class Env {
   private double _baseValue;
   public boolean _blessedSpiritShot = false;
   public Creature _character;
   private CubicInstance _cubic;
   private Effect _effect;
   private ItemInstance _item;
   public byte _shield = 0;
   public Skill _skill;
   private boolean _skillMastery = false;
   public boolean _soulShot = false;
   public boolean _spiritShot = false;
   public Creature _target;
   public double _value;

   public Env() {
   }

   public Env(byte shield, boolean soulShot, boolean spiritShot, boolean blessedSpiritShot) {
      this._shield = shield;
      this._soulShot = soulShot;
      this._spiritShot = spiritShot;
      this._blessedSpiritShot = blessedSpiritShot;
   }

   public Env(Creature character, Creature target, Skill skill) {
      this._character = character;
      this._target = target;
      this._skill = skill;
   }

   public double getBaseValue() {
      return this._baseValue;
   }

   public Creature getCharacter() {
      return this._character;
   }

   public CubicInstance getCubic() {
      return this._cubic;
   }

   public Effect getEffect() {
      return this._effect;
   }

   public ItemInstance getItem() {
      return this._item;
   }

   public Player getPlayer() {
      return this._character == null ? null : this._character.getActingPlayer();
   }

   public byte getShield() {
      return this._shield;
   }

   public Skill getSkill() {
      return this._skill;
   }

   public Creature getTarget() {
      return this._target;
   }

   public double getValue() {
      return this._value;
   }

   public boolean isBlessedSpiritShot() {
      return this._blessedSpiritShot;
   }

   public boolean isSkillMastery() {
      return this._skillMastery;
   }

   public boolean isSoulShot() {
      return this._soulShot;
   }

   public boolean isSpiritShot() {
      return this._spiritShot;
   }

   public void setBaseValue(double baseValue) {
      this._baseValue = baseValue;
   }

   public void setBlessedSpiritShot(boolean blessedSpiritShot) {
      this._blessedSpiritShot = blessedSpiritShot;
   }

   public void setCharacter(Creature character) {
      this._character = character;
   }

   public void setCubic(CubicInstance cubic) {
      this._cubic = cubic;
   }

   public void setEffect(Effect effect) {
      this._effect = effect;
   }

   public void setItem(ItemInstance item) {
      this._item = item;
   }

   public void setShield(byte shield) {
      this._shield = shield;
   }

   public void setSkill(Skill skill) {
      this._skill = skill;
   }

   public void setSkillMastery(boolean skillMastery) {
      this._skillMastery = skillMastery;
   }

   public void setSoulShot(boolean soulShot) {
      this._soulShot = soulShot;
   }

   public void setSpiritShot(boolean spiritShot) {
      this._spiritShot = spiritShot;
   }

   public void setTarget(Creature target) {
      this._target = target;
   }

   public void setValue(double value) {
      this._value = value;
   }

   public void addValue(double value) {
      this._value += value;
   }

   public void subValue(double value) {
      this._value -= value;
   }

   public void mulValue(double value) {
      this._value *= value;
   }

   public void divValue(double value) {
      this._value /= value;
   }
}
