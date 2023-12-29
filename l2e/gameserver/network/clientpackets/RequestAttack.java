package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.network.SystemMessageId;

public final class RequestAttack extends GameClientPacket {
   private int _objectId;
   protected int _originX;
   protected int _originY;
   protected int _originZ;
   protected int _attackId;

   @Override
   protected void readImpl() {
      this._objectId = this.readD();
      this._originX = this.readD();
      this._originY = this.readD();
      this._originZ = this.readD();
      this._attackId = this.readC();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getActiveChar();
      if (activeChar != null) {
         if (System.currentTimeMillis() - activeChar.getLastAttackPacket() >= Config.ATTACK_PACKET_DELAY) {
            activeChar.setLastAttackPacket();
            if (activeChar.isOutOfControl()) {
               activeChar.sendActionFailed();
            } else if (activeChar.isPlayable() && activeChar.isInBoat()) {
               activeChar.sendPacket(SystemMessageId.NOT_ALLOWED_ON_BOAT);
               activeChar.sendActionFailed();
            } else {
               Effect ef = null;
               if ((ef = activeChar.getFirstEffect(EffectType.ACTION_BLOCK)) != null && !ef.checkCondition(-1)) {
                  activeChar.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REPORTED_SO_ACTIONS_NOT_ALLOWED);
                  activeChar.sendActionFailed();
               } else {
                  GameObject target;
                  if (activeChar.getTargetId() == this._objectId) {
                     target = activeChar.getTarget();
                  } else {
                     target = World.getInstance().findObject(this._objectId);
                  }

                  if (target != null) {
                     if (activeChar.isLockedTarget()
                        && activeChar.getLockedTarget() != null
                        && activeChar.getLockedTarget() != target
                        && !activeChar.getLockedTarget().isDead()) {
                        activeChar.sendActionFailed();
                     } else if (!target.isTargetable() && !activeChar.canOverrideCond(PcCondOverride.TARGET_ALL)) {
                        activeChar.sendActionFailed();
                     } else if (target.getReflectionId() != activeChar.getReflectionId() && activeChar.getReflectionId() != -1) {
                        activeChar.sendActionFailed();
                     } else if (!target.isVisibleFor(activeChar)) {
                        activeChar.sendActionFailed();
                     } else {
                        if (activeChar.getTarget() != target) {
                           target.onAction(activeChar);
                        } else if (target.getObjectId() != activeChar.getObjectId()
                           && activeChar.getPrivateStoreType() == 0
                           && activeChar.getActiveRequester() == null) {
                           target.onForcedAttack(activeChar);
                        } else {
                           activeChar.sendActionFailed();
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
