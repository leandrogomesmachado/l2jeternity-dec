package l2e.gameserver.network.clientpackets;

import l2e.gameserver.Config;
import l2e.gameserver.instancemanager.BoatManager;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.BoatInstance;
import l2e.gameserver.model.items.type.WeaponType;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.MoveToLocationInVehicle;
import l2e.gameserver.network.serverpackets.StopMoveInVehicle;

public final class RequestMoveToLocationInVehicle extends GameClientPacket {
   private int _boatId;
   private int _targetX;
   private int _targetY;
   private int _targetZ;
   private int _originX;
   private int _originY;
   private int _originZ;

   @Override
   protected void readImpl() {
      this._boatId = this.readD();
      this._targetX = this.readD();
      this._targetY = this.readD();
      this._targetZ = this.readD();
      this._originX = this.readD();
      this._originY = this.readD();
      this._originZ = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         activeChar.isntAfk();
         if (Config.PLAYER_MOVEMENT_BLOCK_TIME > 0 && !activeChar.isGM() && activeChar.getNotMoveUntil() > System.currentTimeMillis()) {
            activeChar.sendPacket(SystemMessageId.CANNOT_MOVE_WHILE_SPEAKING_TO_AN_NPC);
            activeChar.sendActionFailed();
         } else if (this._targetX == this._originX && this._targetY == this._originY && this._targetZ == this._originZ) {
            activeChar.sendPacket(new StopMoveInVehicle(activeChar, this._boatId));
         } else if (activeChar.isAttackingNow()
            && activeChar.getActiveWeaponItem() != null
            && activeChar.getActiveWeaponItem().getItemType() == WeaponType.BOW) {
            activeChar.sendActionFailed();
         } else if (activeChar.isSitting() || activeChar.isMovementDisabled()) {
            activeChar.sendActionFailed();
         } else if (activeChar.hasSummon()) {
            activeChar.sendPacket(SystemMessageId.RELEASE_PET_ON_BOAT);
            activeChar.sendActionFailed();
         } else if (activeChar.isTransformed()) {
            activeChar.sendPacket(SystemMessageId.CANT_POLYMORPH_ON_BOAT);
            activeChar.sendActionFailed();
         } else {
            if (activeChar.isInBoat()) {
               BoatInstance boat = activeChar.getBoat();
               if (boat.getObjectId() != this._boatId) {
                  activeChar.sendActionFailed();
                  return;
               }
            } else {
               BoatInstance boat = BoatManager.getInstance().getBoat(this._boatId);
               if (boat == null) {
                  activeChar.sendActionFailed();
                  return;
               }

               activeChar.setVehicle(boat);
            }

            Location pos = new Location(this._targetX, this._targetY, this._targetZ);
            Location originPos = new Location(this._originX, this._originY, this._originZ);
            activeChar.setInVehiclePosition(pos);
            activeChar.broadcastPacket(new MoveToLocationInVehicle(activeChar, pos, originPos));
         }
      }
   }
}
