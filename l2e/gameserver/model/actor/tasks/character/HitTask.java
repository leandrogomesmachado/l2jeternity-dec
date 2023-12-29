package l2e.gameserver.model.actor.tasks.character;

import l2e.gameserver.model.actor.Creature;

public final class HitTask implements Runnable {
   private final Creature _character;
   private final Creature _hitTarget;
   private final int _damage;
   private final boolean _crit;
   private final boolean _miss;
   private final byte _shld;
   private final boolean _soulshot;

   public HitTask(Creature character, Creature target, int damage, boolean crit, boolean miss, boolean soulshot, byte shld) {
      this._character = character;
      this._hitTarget = target;
      this._damage = damage;
      this._crit = crit;
      this._shld = shld;
      this._miss = miss;
      this._soulshot = soulshot;
   }

   @Override
   public void run() {
      if (this._character != null) {
         this._character.onHitTimer(this._hitTarget, this._damage, this._crit, this._miss, this._soulshot, this._shld);
      }
   }
}
