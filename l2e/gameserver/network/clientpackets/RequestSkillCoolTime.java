package l2e.gameserver.network.clientpackets;

import l2e.gameserver.model.actor.Player;
import l2e.gameserver.network.GameClient;
import l2e.gameserver.network.serverpackets.SkillCoolTime;

public class RequestSkillCoolTime extends GameClientPacket {
   GameClient client;

   @Override
   protected void readImpl() {
      this.client = this.getClient();
   }

   @Override
   protected void runImpl() {
      Player player = this.client.getActiveChar();
      if (player != null) {
         player.sendPacket(new SkillCoolTime(player));
      }
   }
}
