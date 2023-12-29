package l2e.gameserver.network.clientpackets;

import l2e.gameserver.instancemanager.CHSiegeManager;
import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.model.entity.clanhall.SiegableHall;
import l2e.gameserver.network.serverpackets.CastleSiegeAttackerList;

public final class RequestCastleSiegeAttackerList extends GameClientPacket {
   private int _castleId;

   @Override
   protected void readImpl() {
      this._castleId = this.readD();
   }

   @Override
   protected void runImpl() {
      Castle castle = CastleManager.getInstance().getCastleById(this._castleId);
      if (castle != null) {
         CastleSiegeAttackerList sal = new CastleSiegeAttackerList(castle);
         this.sendPacket(sal);
      } else {
         SiegableHall hall = CHSiegeManager.getInstance().getSiegableHall(this._castleId);
         if (hall != null) {
            CastleSiegeAttackerList sal = new CastleSiegeAttackerList(hall);
            this.sendPacket(sal);
         }
      }
   }
}
