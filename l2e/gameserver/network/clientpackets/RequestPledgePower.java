package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.serverpackets.ManagePledgePower;

public final class RequestPledgePower extends GameClientPacket {
   private int _rank;
   private int _action;
   private int _privs;

   @Override
   protected void readImpl() {
      this._rank = this.readD();
      this._action = this.readD();
      if (this._action == 2) {
         this._privs = this.readD();
      } else {
         this._privs = 0;
      }
   }

   @Override
   protected void runImpl() {
      Player player = this.getClient().getActiveChar();
      if (player != null) {
         if (this._action == 2) {
            if (player.isClanLeader()) {
               if (this._rank == 9) {
                  this._privs = (this._privs & 8) + (this._privs & 2048) + (this._privs & 65536);
               }

               player.getClan().setRankPrivs(this._rank, this._privs);
            }
         } else if (player.getClan() != null) {
            player.sendPacket(new ManagePledgePower(player, this._action, this._rank));
         } else {
            player.sendActionFailed();
         }
      }
   }
}
