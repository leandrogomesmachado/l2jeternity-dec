package l2e.gameserver.network.clientpackets;

import l2e.commons.util.Util;
import l2e.fake.FakePlayer;
import l2e.gameserver.Config;
import l2e.gameserver.ai.model.CtrlIntention;
import l2e.gameserver.geodata.GeoEngine;
import l2e.gameserver.model.Location;
import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.SystemMessageId;
import l2e.gameserver.network.serverpackets.StopMove;

public class MoveBackwardToLocation extends GameClientPacket {
   private int _targetX;
   private int _targetY;
   private int _targetZ;
   private int _originX;
   private int _originY;
   private int _originZ;
   protected int _moveMovement;

   @Override
   protected void readImpl() {
      this._targetX = this.readD();
      this._targetY = this.readD();
      this._targetZ = this.readD();
      this._originX = this.readD();
      this._originY = this.readD();
      this._originZ = this.readD();
      if (this._buf.hasRemaining()) {
         this._moveMovement = this.readD();
      } else if (Config.L2WALKER_PROTECTION) {
         Player activeChar = this.getClient().getActiveChar();
         Util.handleIllegalPlayerAction(activeChar, "" + activeChar.getName() + " is trying to use L2Walker!");
      }
   }

   @Override
   protected void runImpl() {
      Player activeChar = this.getClient().getActiveChar();
      if (activeChar != null) {
         if (System.currentTimeMillis() - activeChar.getLastMovePacket() < Config.MOVE_PACKET_DELAY) {
            activeChar.sendActionFailed();
         } else {
            activeChar.isntAfk();
            if (activeChar.isAttackingNow()) {
               activeChar.getAI().setIntention(CtrlIntention.IDLE);
            }

            activeChar.setLastMovePacket();
            if (Config.PLAYER_MOVEMENT_BLOCK_TIME > 0 && !activeChar.isGM() && activeChar.getNotMoveUntil() > System.currentTimeMillis()) {
               activeChar.sendPacket(SystemMessageId.CANNOT_MOVE_WHILE_SPEAKING_TO_AN_NPC);
               activeChar.sendActionFailed();
            } else if (this._targetX == this._originX && this._targetY == this._originY && this._targetZ == this._originZ) {
               activeChar.sendPacket(new StopMove(activeChar));
            } else {
               this._targetZ = (int)((double)this._targetZ + activeChar.getColHeight());
               if (activeChar.getTeleMode() > 0) {
                  Location loc = null;
                  if (activeChar.getTeleMode() == 1) {
                     loc = GeoEngine.moveCheck(activeChar.getX(), activeChar.getY(), activeChar.getZ(), this._targetX, this._targetY, activeChar.getGeoIndex());
                     activeChar.setTeleMode(0);
                  } else if (activeChar.getTeleMode() == 2) {
                     loc = new Location(this._targetX, this._targetY, this._targetZ);
                  }

                  activeChar.sendActionFailed();
                  activeChar.teleToLocation(loc.getX(), loc.getY(), loc.getZ(), false);
               } else if (activeChar.isControllingFakePlayer()) {
                  FakePlayer fakePlayer = activeChar.getPlayerUnderControl();
                  activeChar.sendActionFailed();
                  fakePlayer.getAI().setIntention(CtrlIntention.MOVING, new Location(this._targetX, this._targetY, this._targetZ));
               } else {
                  double dx = (double)(this._targetX - activeChar.getX());
                  double dy = (double)(this._targetY - activeChar.getY());
                  if (!activeChar.isOutOfControl() && !(dx * dx + dy * dy > 9.801E7)) {
                     activeChar.setFallingLoc(new Location(this._targetX, this._targetY, this._targetZ));
                     activeChar.getAI().setIntention(CtrlIntention.MOVING, new Location(this._targetX, this._targetY, this._targetZ));
                  } else {
                     activeChar.sendActionFailed();
                  }
               }
            }
         }
      }
   }
}
