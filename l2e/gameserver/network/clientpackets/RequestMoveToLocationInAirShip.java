package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.model.actor.instance.AirShipInstance;
import l2e.gameserver.model.items.type.WeaponType;
import l2e.gameserver.network.serverpackets.ExMoveToLocationInAirShip;
import l2e.gameserver.network.serverpackets.StopMoveInVehicle;

public class RequestMoveToLocationInAirShip extends GameClientPacket {
   private int _shipId;
   private int _targetX;
   private int _targetY;
   private int _targetZ;
   private int _originX;
   private int _originY;
   private int _originZ;

   @Override
   protected void readImpl() {
      this._shipId = this.readD();
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
         if (this._targetX == this._originX && this._targetY == this._originY && this._targetZ == this._originZ) {
            activeChar.sendPacket(new StopMoveInVehicle(activeChar, this._shipId));
         } else if (activeChar.isAttackingNow()
            && activeChar.getActiveWeaponItem() != null
            && activeChar.getActiveWeaponItem().getItemType() == WeaponType.BOW) {
            activeChar.sendActionFailed();
         } else if (activeChar.isSitting() || activeChar.isMovementDisabled()) {
            activeChar.sendActionFailed();
         } else if (!activeChar.isInAirShip()) {
            activeChar.sendActionFailed();
         } else {
            AirShipInstance airShip = activeChar.getAirShip();
            if (airShip.getObjectId() != this._shipId) {
               activeChar.sendActionFailed();
            } else {
               activeChar.setInVehiclePosition(new Location(this._targetX, this._targetY, this._targetZ));
               activeChar.broadcastPacket(new ExMoveToLocationInAirShip(activeChar));
            }
         }
      }
   }
}
