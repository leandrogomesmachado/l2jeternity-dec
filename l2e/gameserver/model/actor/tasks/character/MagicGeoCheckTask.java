package l2e.gameserver.model.actor.tasks.character;

import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.model.actor.Creature;
import l2e.gameserver.network.SystemMessageId;

public final class MagicGeoCheckTask implements Runnable {
   private final Creature _character;

   public MagicGeoCheckTask(Creature character) {
      this._character = character;
   }

   @Override
   public void run() {
      if (this._character != null) {
         Creature castingTarget = this._character.getCastingTarget();
         if (castingTarget != null && this._character.isCastingNow()) {
            if (!GeoEngine.canSeeTarget(this._character, castingTarget, this._character.isFlying())) {
               if (this._character.isPlayer()) {
                  this._character.sendPacket(SystemMessageId.CANT_SEE_TARGET);
               }

               this._character.abortCast();
            } else {
               this._character._skillGeoCheckTask = null;
            }
         }
      }
   }
}
