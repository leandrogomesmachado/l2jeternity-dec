package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Creature;

public final class MagicSkillUse extends GameServerPacket {
   private final int _targetId;
   private final int _tx;
   private final int _ty;
   private final int _tz;
   private final int _skillId;
   private final int _skillLevel;
   private final int _hitTime;
   private final int _reuseDelay;
   private final int _charObjId;
   private final int _x;
   private final int _y;
   private final int _z;

   public MagicSkillUse(Creature cha, Creature target, int skillId, int skillLevel, int hitTime, int reuseDelay) {
      this._charObjId = cha.getObjectId();
      this._targetId = target.getObjectId();
      this._skillId = skillId;
      this._skillLevel = skillLevel;
      this._hitTime = hitTime;
      this._reuseDelay = reuseDelay;
      this._x = cha.getX();
      this._y = cha.getY();
      this._z = cha.getZ();
      this._tx = target.getX();
      this._ty = target.getY();
      this._tz = target.getZ();
   }

   public MagicSkillUse(int x, int y, int z, Creature owner, int skillId, int skillLevel, int hitTime, int reuseDelay) {
      this._charObjId = owner.getObjectId();
      this._targetId = owner.getObjectId();
      this._skillId = skillId;
      this._skillLevel = skillLevel;
      this._hitTime = hitTime;
      this._reuseDelay = reuseDelay;
      this._x = x;
      this._y = y;
      this._z = z;
      this._tx = x;
      this._ty = y;
      this._tz = z;
   }

   public MagicSkillUse(Creature cha, int skillId, int skillLevel, int hitTime, int reuseDelay) {
      this._charObjId = cha.getObjectId();
      this._targetId = cha.getTargetId();
      this._skillId = skillId;
      this._skillLevel = skillLevel;
      this._hitTime = hitTime;
      this._reuseDelay = reuseDelay;
      this._x = cha.getX();
      this._y = cha.getY();
      this._z = cha.getZ();
      this._tx = cha.getX();
      this._ty = cha.getY();
      this._tz = cha.getZ();
   }

   @Override
   protected final void writeImpl() {
      if (this.getClient().getActiveChar() == null
         || !this.getClient().getActiveChar().getNotShowBuffsAnimation()
         || this.getClient().getActiveChar().getObjectId() == this._charObjId) {
         this.writeD(this._charObjId);
         this.writeD(this._targetId);
         this.writeD(this._skillId);
         this.writeD(this._skillLevel);
         this.writeD(this._hitTime);
         this.writeD(this._reuseDelay);
         this.writeD(this._x);
         this.writeD(this._y);
         this.writeD(this._z);
         this.writeD(0);
         this.writeD(this._tx);
         this.writeD(this._ty);
         this.writeD(this._tz);
      }
   }
}
