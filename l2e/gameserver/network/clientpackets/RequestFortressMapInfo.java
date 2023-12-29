package l2e.gameserver.network.clientpackets;

import l2e.gameserver.instancemanager.FortManager;
import l2e.gameserver.model.entity.Fort;
import l2e.gameserver.network.serverpackets.ExShowFortressMapInfo;

public class RequestFortressMapInfo extends GameClientPacket {
   private int _fortressId;

   @Override
   protected void readImpl() {
      this._fortressId = this.readD();
   }

   @Override
   protected void runImpl() {
      Fort fort = FortManager.getInstance().getFortById(this._fortressId);
      this.sendPacket(new ExShowFortressMapInfo(fort));
   }
}
