package l2e.gameserver.network.clientpackets;

import l2e.gameserver.instancemanager.CastleManager;
import l2e.gameserver.model.entity.Castle;
import l2e.gameserver.network.serverpackets.CastleSiegeDefenderList;

public final class RequestCastleSiegeDefenderList extends GameClientPacket {
   private int _castleId;

   @Override
   protected void readImpl() {
      this._castleId = this.readD();
   }

   @Override
   protected void runImpl() {
      Castle castle = CastleManager.getInstance().getCastleById(this._castleId);
      if (castle != null) {
         CastleSiegeDefenderList sdl = new CastleSiegeDefenderList(castle);
         this.sendPacket(sdl);
      }
   }
}
