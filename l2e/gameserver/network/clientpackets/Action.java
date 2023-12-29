package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.model.GameObject;
import l2e.gameserver.model.PcCondOverride;
import l2e.gameserver.model.World;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.skills.effects.Effect;
import l2e.gameserver.model.skills.effects.EffectType;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.DeleteObject;

public final class Action extends GameClientPacket {
   protected int _objectId;
   protected int _originX;
   protected int _originY;
   protected int _originZ;
   protected int _actionId;

   @Override
   protected void readImpl() {
      this._objectId = this.readD();
      this._originX = this.readD();
      this._originY = this.readD();
      this._originZ = this.readD();
      this._actionId = this.readC();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getActiveChar();
      if (activeChar != null) {
         if (activeChar.isOutOfControl()) {
            activeChar.sendActionFailed();
         } else if (activeChar.inObserverMode()) {
            activeChar.sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
            activeChar.sendActionFailed();
         } else {
            Effect ef = null;
            if ((ef = activeChar.getFirstEffect(EffectType.ACTION_BLOCK)) != null && !ef.checkCondition(-4)) {
               activeChar.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REPORTED_SO_ACTIONS_NOT_ALLOWED);
               activeChar.sendActionFailed();
            } else {
               GameObject obj;
               if (activeChar.getTargetId() == this._objectId) {
                  obj = activeChar.getTarget();
               } else if (activeChar.isInAirShip() && activeChar.getAirShip().getHelmObjectId() == this._objectId) {
                  obj = activeChar.getAirShip();
               } else {
                  obj = World.getInstance().findObject(this._objectId);
               }

               if (obj == null) {
                  if (activeChar.getObjectId() != this._objectId) {
                     this.sendPacket(new DeleteObject(this._objectId));
                  }

                  activeChar.sendActionFailed();
               } else if (activeChar.isLockedTarget()) {
                  if (activeChar.getLockedTarget() != null && activeChar.getLockedTarget() != obj) {
                     activeChar.sendActionFailed();
                  }
               } else if (!obj.isTargetable() && !activeChar.canOverrideCond(PcCondOverride.TARGET_ALL)) {
                  activeChar.sendActionFailed();
               } else if (obj.getReflectionId() != activeChar.getReflectionId() && activeChar.getReflectionId() != -1) {
                  activeChar.sendActionFailed();
               } else if (!obj.isVisibleFor(activeChar)) {
                  activeChar.sendActionFailed();
               } else if (activeChar.getActiveRequester() != null) {
                  activeChar.sendActionFailed();
               } else {
                  switch(this._actionId) {
                     case 0:
                        obj.onAction(activeChar);
                        break;
                     case 1:
                        if (!activeChar.isGM() && !Config.ALT_GAME_VIEWNPC && !Config.ALT_GAME_VIEWPLAYER) {
                           obj.onAction(activeChar, false);
                        } else {
                           obj.onActionShift(activeChar);
                        }
                        break;
                     default:
                        activeChar.sendActionFailed();
                  }
               }
            }
         }
      }
   }

   @Override
   protected boolean triggersOnActionRequest() {
      return false;
   }
}
