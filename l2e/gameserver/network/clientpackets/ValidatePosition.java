package l2e.gameserver.network.clientpackets;

import l2e.commons.util.Util;
import l2e.gameserver.Config;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.geodata.editor.GeoEditorListener;
import l2e.gameserver.model.TeleportWhereType;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.GetOnVehicle;
import l2e.gameserver.network.serverpackets.ValidateLocation;

public class ValidatePosition extends GameClientPacket {
   private int _clientX;
   private int _clientY;
   private int _clientZ;
   private int _clientHeading;
   private int _vehicle;

   @Override
   protected void readImpl() {
      this._clientX = this.readD();
      this._clientY = this.readD();
      this._clientZ = this.readD();
      this._clientHeading = this.readD();
      this._vehicle = this.readD();
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (!activeChar.isTeleporting() && !activeChar.inObserverMode()) {
            int realX = activeChar.getX();
            int realY = activeChar.getY();
            if (this._clientX != 0 || this._clientY != 0 || realX == 0) {
               if (activeChar.isInBoat()) {
                  if (Config.GEODATA) {
                     int dx = this._clientX - activeChar.getInVehiclePosition().getX();
                     int dy = this._clientY - activeChar.getInVehiclePosition().getY();
                     double diffSq = (double)(dx * dx + dy * dy);
                     if (diffSq > 250000.0) {
                        this.sendPacket(new GetOnVehicle(activeChar.getObjectId(), this._vehicle, activeChar.getInVehiclePosition()));
                     }
                  }
               } else if (!activeChar.isInAirShip()) {
                  if (activeChar.isFalling(this._clientZ)) {
                     activeChar.setClientX(this._clientX);
                     activeChar.setClientY(this._clientY);
                     int dz2 = Math.abs(this._clientZ - activeChar.getZ());
                     if (dz2 >= 1024) {
                        if (activeChar.getFallingLoc() != null) {
                           activeChar.teleToLocation(activeChar.getFallingLoc(), false);
                        } else {
                           activeChar.teleToLocation(TeleportWhereType.TOWN, true);
                        }
                     }
                  } else {
                     int dx = this._clientX - realX;
                     int dy = this._clientY - realY;
                     double diffSq = (double)(dx * dx + dy * dy);
                     if (Config.ACCEPT_GEOEDITOR_CONN
                        && GeoEditorListener.getInstance().getThread() != null
                        && GeoEditorListener.getInstance().getThread().isWorking()
                        && GeoEditorListener.getInstance().getThread().isSend(activeChar)) {
                        GeoEditorListener.getInstance().getThread().sendGmPosition(this._clientX, this._clientY, (short)this._clientZ);
                     }

                     if (activeChar.isFlyingMounted() && this._clientX > -166168) {
                        activeChar.untransform();
                     }

                     if (activeChar.isFlying() || activeChar.isInWater(activeChar)) {
                        activeChar.setXYZ(realX, realY, this._clientZ);
                        if (diffSq > 90000.0) {
                           activeChar.sendPacket(new ValidateLocation(activeChar));
                        }
                     } else if (diffSq < 360000.0) {
                        if (!Config.SYNC_BY_GEO) {
                           activeChar.correctCharPosition(realX, realY, this._clientZ);
                        }

                        if (!activeChar.validateMovementHeading(this._clientHeading)) {
                           activeChar.setHeading(this._clientHeading);
                        }

                        if (Util.calculateDistance(this._clientX, this._clientY, realX, realY) > 512.0) {
                           activeChar.sendPacket(new ValidateLocation(activeChar));
                        }
                     }

                     if (activeChar.hasAI()
                        && activeChar.isMoving()
                        && activeChar.getAI().getIntention() == CtrlIntention.FOLLOW
                        && Util.calculateDistance(this._clientX, this._clientY, realX, realY) > 100.0) {
                        activeChar.sendPacket(new ValidateLocation(activeChar));
                     }

                     activeChar.setClientX(this._clientX);
                     activeChar.setClientY(this._clientY);
                     activeChar.setClientZ(this._clientZ);
                     activeChar.setClientHeading(this._clientHeading);
                  }
               }
            }
         }
      }
   }
}
