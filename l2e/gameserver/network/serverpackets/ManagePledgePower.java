package l2e.gameserver.network.serverpackets;

import l2e.gameserver.model.actor.Player;

public class ManagePledgePower extends GameServerPacket {
   private final int _action;
   private final int _clanId;
   private final int _privs;

   public ManagePledgePower(Player player, int action, int rank) {
      this._clanId = player.getClanId();
      this._action = action;
      this._privs = player.getClan().getRankPrivs(rank);
      player.sendPacket(new PledgeReceiveUpdatePower(this._privs));
   }

   @Override
   protected final void writeImpl() {
      this.writeD(this._clanId);
      this.writeD(this._action);
      this.writeD(this._privs);
   }
}
