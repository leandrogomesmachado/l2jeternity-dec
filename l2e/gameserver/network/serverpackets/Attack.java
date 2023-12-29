package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.actor.Creature;

public class Attack extends GameServerPacket {
   public final int _attackerId;
   public final boolean _soulshot;
   private final int _grade;
   private final int _x;
   private final int _y;
   private final int _z;
   private final int _tx;
   private final int _ty;
   private final int _tz;
   private Attack.Hit[] _hits;

   public Attack(Creature attacker, Creature target, boolean useShots, int ssGrade) {
      this._attackerId = attacker.getObjectId();
      this._soulshot = useShots;
      this._grade = ssGrade;
      this._x = attacker.getX();
      this._y = attacker.getY();
      this._z = attacker.getZ();
      this._tx = target.getX();
      this._ty = target.getY();
      this._tz = target.getZ();
      this._hits = new Attack.Hit[0];
   }

   public void addHit(Creature target, int damage, boolean miss, boolean crit, byte shld) {
      int pos = this._hits.length;
      Attack.Hit[] tmp = new Attack.Hit[pos + 1];
      System.arraycopy(this._hits, 0, tmp, 0, this._hits.length);
      tmp[pos] = new Attack.Hit(target, damage, miss, crit, shld);
      this._hits = tmp;
   }

   public boolean hasHits() {
      return this._hits.length > 0;
   }

   public boolean hasSoulshot() {
      return this._soulshot;
   }

   @Override
   protected final void writeImpl() {
      boolean shouldSeeShots = this.getClient().getActiveChar() == null || !this.getClient().getActiveChar().getNotShowBuffsAnimation();
      this.writeD(this._attackerId);
      this.writeD(this._hits[0]._targetId);
      this.writeD(this._hits[0]._damage);
      this.writeC(shouldSeeShots ? this._hits[0]._flags : 0);
      this.writeD(this._x);
      this.writeD(this._y);
      this.writeD(this._z);
      this.writeH(this._hits.length - 1);

      for(int i = 1; i < this._hits.length; ++i) {
         this.writeD(this._hits[i]._targetId);
         this.writeD(this._hits[i]._damage);
         this.writeC(shouldSeeShots ? this._hits[i]._flags : 0);
      }

      this.writeD(this._tx);
      this.writeD(this._ty);
      this.writeD(this._tz);
   }

   private class Hit {
      int _targetId;
      int _damage;
      int _flags;

      Hit(GameObject target, int damage, boolean miss, boolean crit, byte shld) {
         this._targetId = target.getObjectId();
         this._damage = damage;
         if (Attack.this._soulshot) {
            this._flags |= 16 | Attack.this._grade;
         }

         if (crit) {
            this._flags |= 32;
         }

         if (shld > 0) {
            this._flags |= 64;
         }

         if (miss) {
            this._flags |= 128;
         }
      }
   }
}
